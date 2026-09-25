package com.hidroplan.app.ar

import android.content.Intent
import android.graphics.PointF
import android.opengl.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.hidroplan.app.R
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import java.util.Locale

/**
 * Medición por realidad aumentada: el usuario toca la superficie (suelo/mesa) y marca ≥3 esquinas;
 * el PCA (mismo algoritmo que el web) ajusta un rectángulo y devuelve largo × ancho en metros.
 */
class ArMedicionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LARGO = "largo"
        const val EXTRA_ANCHO = "ancho"
    }

    private lateinit var arSceneView: ARSceneView
    private lateinit var overlay: MedicionOverlay
    private lateinit var status: TextView
    private lateinit var btnUse: Button

    private val points = mutableListOf<FloatArray>()
    private val anchors = mutableListOf<Anchor>()
    private val pointNodes = mutableListOf<AnchorNode>()
    private var rectPlane: ModelNode? = null
    private var rect: RectInfo? = null

    private val ui = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            overlay.invalidate()
            ui.postDelayed(this, 33)
        }
    }
    private val viewProj = FloatArray(16)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_medicion)

        arSceneView = findViewById(R.id.arView)
        overlay = findViewById(R.id.overlay)
        status = findViewById(R.id.status)
        btnUse = findViewById(R.id.btnUse)

        arSceneView.lifecycle = lifecycle
        arSceneView.configureSession { _, config ->
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        }

        overlay.provider = ::project
        overlay.onTap = { x, y -> onTap(x, y) }

        findViewById<Button>(R.id.btnClear).setOnClickListener { clear() }
        findViewById<Button>(R.id.btnClose).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        btnUse.setOnClickListener { apply() }
    }

    override fun onResume() {
        super.onResume()
        ui.post(tick)
    }

    override fun onPause() {
        ui.removeCallbacks(tick)
        super.onPause()
    }

    override fun onDestroy() {
        ui.removeCallbacks(tick)
        releaseArScene()
        super.onDestroy()
    }

    private fun onTap(x: Float, y: Float) {
        val hit = arSceneView.hitTestAR(
            xPx = x,
            yPx = y,
            planeTypes = setOf(Plane.Type.HORIZONTAL_UPWARD_FACING),
            point = true,
            depthPoint = true,
            instantPlacementPoint = true
        )
        if (hit == null) {
            status.text = "No detectó superficie. Mové el teléfono despacio y probá de nuevo."
            return
        }
        val anchor = hit.createAnchorOrNull() ?: return
        addPoint(anchor)
    }

    private fun addPoint(anchor: Anchor) {
        val p = anchor.pose
        points.add(floatArrayOf(p.tx(), p.ty(), p.tz()))
        anchors.add(anchor)
        placePointMarker(anchor)
        rebuild()
    }

    private fun placePointMarker(anchor: Anchor) {
        val node = AnchorNode(arSceneView.engine, anchor)
        pointNodes.add(node)
        arSceneView.addChildNode(node)
        arSceneView.modelLoader.loadModelInstanceAsync("models/ar/punto.glb") { instance ->
            if (instance != null) {
                ModelNode(instance).apply { parent = node }
            }
        }
    }

    private fun rebuild() {
        rect = MedicionMath.computeRect(points)
        val hasRect = rect != null
        btnUse.isEnabled = hasRect
        status.text = when {
            points.isEmpty() -> "Tocá la pantalla para marcar la primera esquina"
            points.size == 1 -> "Marcá la segunda esquina del espacio"
            points.size == 2 -> "Marcá una tercera esquina para calcular la superficie"
            else -> "Puntos: ${points.size}. Tocá más puntos o usá las medidas calculadas."
        }
        rectPlane?.parent = null
        rectPlane = null
        if (hasRect) applyRect()
        overlay.points = points
        overlay.rect = rect
        overlay.invalidate()
    }

    private fun applyRect() {
        val r = rect ?: return
        arSceneView.modelLoader.loadModelInstanceAsync("models/ar/plano.glb") { instance ->
            if (instance == null) return@loadModelInstanceAsync
            val node = ModelNode(instance).apply {
                position = Position(r.cx, r.y, r.cz)
                rotation = Rotation(y = Math.toDegrees(r.rot.toDouble()).toFloat())
                scale = Scale(r.l, 1f, r.a)
            }
            rectPlane = node
            arSceneView.addChildNode(node)
        }
    }

    private fun clear() {
        for (node in pointNodes) {
            node.parent = null
        }
        pointNodes.clear()
        for (a in anchors) a.detach()
        anchors.clear()
        points.clear()
        rect = null
        rectPlane?.parent = null
        rectPlane = null
        btnUse.isEnabled = false
        overlay.points = emptyList()
        overlay.rect = null
        overlay.invalidate()
        status.text = "Tocá la pantalla para marcar la primera esquina"
    }

    private fun apply() {
        val r = rect ?: return
        val l = String.format(Locale.US, "%.2f", r.l)
        val a = String.format(Locale.US, "%.2f", r.a)
        val out = Intent()
        out.putExtra(EXTRA_LARGO, l)
        out.putExtra(EXTRA_ANCHO, a)
        setResult(RESULT_OK, out)
        finish()
    }

    /** Proyecta un punto 3D (mundo) a coordenadas de pantalla usando la cámara AR del frame actual. */
    private fun project(world: FloatArray): PointF? {
        val frame = arSceneView.frame ?: return null
        try {
            val camera = frame.camera
            if (camera.trackingState != TrackingState.TRACKING) return null
            camera.getProjectionMatrix(viewProj, 0, 0.05f, 100f)
            val view = FloatArray(16)
            camera.getViewMatrix(view, 0)
            Matrix.multiplyMM(viewProj, 0, viewProj, 0, view, 0)
            val clip = FloatArray(4)
            Matrix.multiplyMV(clip, 0, viewProj, 0, floatArrayOf(world[0], world[1], world[2], 1f), 0)
            if (clip[3] == 0f) return null
            val ndcX = clip[0] / clip[3]
            val ndcY = clip[1] / clip[3]
            if (ndcX < -1.5f || ndcX > 1.5f || ndcY < -1.5f || ndcY > 1.5f) return null
            val sx = (ndcX * 0.5f + 0.5f) * overlay.width
            val sy = (1f - (ndcY * 0.5f + 0.5f)) * overlay.height
            return PointF(sx, sy)
        } catch (_: Exception) {
            return null
        }
    }

    private fun releaseArScene() {
        rectPlane?.parent = null
        rectPlane = null
        for (node in pointNodes) node.parent = null
        pointNodes.clear()
        for (a in anchors) a.detach()
        anchors.clear()
    }
}