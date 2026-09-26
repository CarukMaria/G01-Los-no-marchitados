package com.hidroplan.app.ar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Overlay 2D encima de la cámara AR: retícula central, marcadores numerados en cada punto
 * (proyectados a pantalla) y el rectángulo medido. El rectángulo también se renderiza en 3D
 * (más preciso); acá solo se ve como guía.
 */
class MedicionOverlay : View {

    var provider: ((FloatArray) -> PointF?)? = null
    var points: List<FloatArray> = emptyList()
    var rect: RectInfo? = null
    var onTap: ((Float, Float) -> Unit)? = null

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF38bdf8.toInt()
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF22d3ee.toInt()
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFe2e8f0.toInt()
        textSize = 30f
        isFakeBoldText = true
    }
    private val rectFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x4022d3ee
        style = Paint.Style.FILL
    }
    private val rectStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF22d3ee.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val reticlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF94a3b8.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val gesture = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onTap?.invoke(e.x, e.y)
                return true
            }
        }
    )

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        isClickable = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gesture.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val px = provider ?: return
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawCircle(cx, cy, 16f, reticlePaint)
        canvas.drawCircle(cx, cy, 4f, dotPaint)
        if (points.isEmpty()) return

        val scr = points.map { px(it) }

        if (scr.size >= 2 && rect == null) {
            val path = Path()
            var started = false
            for (p in scr) {
                if (p == null) {
                    started = false
                    continue
                }
                if (!started) {
                    path.moveTo(p.x, p.y)
                    started = true
                } else {
                    path.lineTo(p.x, p.y)
                }
            }
            canvas.drawPath(path, linePaint)
        }

        for ((i, p) in scr.withIndex()) {
            if (p == null) continue
            canvas.drawCircle(p.x, p.y, 11f + i * 3f, dotPaint)
            canvas.drawText((i + 1).toString(), p.x + 14f, p.y - 12f, textPaint)
        }

        val r = rect ?: return
        val cs = MedicionMath.corners(r).map { px(it) }
        if (cs.any { it == null }) return
        val rf = Path()
        rf.moveTo(cs[0]!!.x, cs[0]!!.y)
        for (i in 1..3) rf.lineTo(cs[i]!!.x, cs[i]!!.y)
        rf.close()
        canvas.drawPath(rf, rectFill)
        canvas.drawPath(rf, rectStroke)
    }
}