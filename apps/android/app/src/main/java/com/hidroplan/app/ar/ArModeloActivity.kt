package com.hidroplan.app.ar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.hidroplan.app.R
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Coloca el sistema hidropónico del preset elegido (mismo tamaño real que en el web) en la
 * superficie detectada, con el largo del sistema apuntando en dirección contraria a la cámara.
 */
class ArModeloActivity : AppCompatActivity() {

    private lateinit var arSceneView: ARSceneView
    private lateinit var status: TextView
    private lateinit var presetInfo: TextView

    private var placed: AnchorNode? = null
    private var placedAnchor: Anchor? = null
    private lateinit var preset: ARPreset

    private val ui = Handler(Looper.getMainLooper())
    private val hint = object : Runnable {
        override fun run() {
            if (placed == null) {
                status.text = ArGuias.statusHint(arSceneView, 0)
                ui.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_modelo)

        arSceneView = findViewById(R.id.arView)
        status = findViewById(R.id.status)
        presetInfo = findViewById(R.id.presetInfo)

        preset = Presets.forLongitud(intent.getStringExtra("longitud"))
        presetInfo.text = "${preset.label}  ·  ${preset.dims}"

        arSceneView.lifecycle = lifecycle
        arSceneView.configureSession { _, config ->
            ArGuias.applySessionConfig(config)
        }

        val tapArea = findViewById<View>(R.id.tapArea)
        val gd = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    place(e.x, e.y)
                    return true
                }
            }
        )
        tapArea.setOnTouchListener { _, event ->
            gd.onTouchEvent(event)
            true
        }

        findViewById<Button>(R.id.btnClose).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        ui.post(hint)
    }

    override fun onPause() {
        ui.removeCallbacks(hint)
        super.onPause()
    }

    override fun onDestroy() {
        releasePlaced()
        super.onDestroy()
    }

    private fun place(x: Float, y: Float) {
        val camera = arSceneView.frame?.camera
        if (camera == null) {
            status.text = "ARCore no está listo todavía. Esperá un instante y tocá de nuevo."
            return
        }
        if (!ArGuias.isTracking(camera)) {
            status.text = ArGuias.reasonGuide(camera.trackingFailureReason)
                ?: "El tracking está iniciando. Mové el teléfono suavemente y probá de nuevo."
            return
        }
        val hit = arSceneView.hitTestAR(
            xPx = x,
            yPx = y,
            planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING),
            point = true,
            depthPoint = true,
            instantPlacementPoint = true
        )
        if (hit == null) {
            status.text = if (ArGuias.planeCount(arSceneView.session) > 0) {
                "No cayó en el piso detectado. Apuntá al piso y tocá de nuevo."
            } else {
                "Todavía no detecta el piso: barré la cámara sobre una zona con textura y tocá de nuevo."
            }
            return
        }
        placeModel(hit.createAnchorOrNull())
    }

    private fun placeModel(anchor: Anchor?) {
        if (anchor == null) return
        releasePlaced()

        val node = AnchorNode(arSceneView.engine, anchor)
        placed = node
        placedAnchor = anchor
        arSceneView.addChildNode(node)

        val yawOffset = yawAwayFromCamera(anchor.pose)

        arSceneView.modelLoader.loadModelInstanceAsync(preset.file) { instance ->
            if (instance == null) {
                Toast.makeText(this, "No se pudo cargar el modelo 3D", Toast.LENGTH_SHORT).show()
                return@loadModelInstanceAsync
            }
            ModelNode(instance).apply {
                rotation = Rotation(y = yawOffset)
                parent = node
            }
        }

        status.text = "Sistema de ${preset.label} colocado (${preset.dims}). Tocá otro lugar para moverlo."
    }

    /**
     * Alineación: se quiere que el largo del modelo (+X del GLB) quede apuntando en la dirección
     * que va del dispositvo hacia el punto (alejándose del usuario).
     */
    private fun yawAwayFromCamera(anchorPose: Pose): Float {
        val cam = arSceneView.frame?.camera ?: return 0f
        val camPose = cam.pose
        val dx = anchorPose.tx() - camPose.tx()
        val dz = anchorPose.tz() - camPose.tz()
        val len = sqrt(dx * dx + dz * dz)
        if (len < 0.0001f) return 0f
        val dxn = dx / len
        val dzn = dz / len
        // Dirección deseada para el +X del modelo (world): alpha = atan2(-dzn, dxn)
        val alpha = atan2(-dzn, dxn)
        val anchorYaw = yawOf(anchorPose)
        return Math.toDegrees((alpha - anchorYaw).toDouble()).toFloat()
    }

    /** Yaw (rotación en Y) de un Pose, derivado de su matriz de rotación. */
    private fun yawOf(pose: Pose): Float {
        val m = FloatArray(16)
        pose.toMatrix(m, 0)
        return atan2(-m[8], m[0])
    }

    private fun releasePlaced() {
        placed?.parent = null
        placed = null
        placedAnchor?.detach()
        placedAnchor = null
    }
}