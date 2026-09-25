package com.hidroplan.app.ar

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin

/**
 * Rectángulo ajustado por PCA en el plano horizontal (XZ), espejo del algoritmo del web
 * (prototipo.html → computeRect). Devuelve l = intermed/extent mayor, a = menor.
 */
data class RectInfo(
    val l: Float,
    val a: Float,
    val cx: Float,
    val cz: Float,
    val y: Float,
    val rot: Float
)

object MedicionMath {

    fun computeRect(points: List<FloatArray>): RectInfo? {
        val n = points.size
        if (n < 3) return null
        var mx = 0f
        var mz = 0f
        var cxx = 0f
        var cxz = 0f
        var czz = 0f
        for (p in points) {
            mx += p[0]
            mz += p[2]
        }
        mx /= n
        mz /= n
        for (p in points) {
            val dx = p[0] - mx
            val dz = p[2] - mz
            cxx += dx * dx
            cxz += dx * dz
            czz += dz * dz
        }
        val ang = 0.5f * atan2(2 * cxz, cxx - czz)
        val ux = cos(ang)
        val uz = sin(ang)
        val vx = -sin(ang)
        val vz = cos(ang)
        var minU = Float.MAX_VALUE
        var maxU = -Float.MAX_VALUE
        var minV = Float.MAX_VALUE
        var maxV = -Float.MAX_VALUE
        for (p in points) {
            val dx = p[0] - mx
            val dz = p[2] - mz
            val u = dx * ux + dz * uz
            val v = dx * vx + dz * vz
            minU = min(minU, u)
            maxU = max(maxU, u)
            minV = min(minV, v)
            maxV = max(maxV, v)
        }
        val uw = maxU - minU
        val vw = maxV - minV
        val alongU = uw >= vw
        val l = max(uw, vw)
        val a = min(uw, vw)
        val rot = if (alongU) -ang else -(ang + PI.toFloat() / 2f)
        val cu = (minU + maxU) / 2f
        val cv = (minV + maxV) / 2f
        val cx = mx + cu * ux + cv * vx
        val cz = mz + cu * uz + cv * vz
        return RectInfo(
            l = round2(l),
            a = round2(a),
            cx = cx,
            cz = cz,
            y = points[0][1],
            rot = rot
        )
    }

    /** Las 4 esquinas del rectángulo en coordenadas del mundo (mismo orden que en el web). */
    fun corners(r: RectInfo): List<FloatArray> {
        val c = cos(r.rot)
        val s = sin(r.rot)
        val l2 = r.l / 2f
        val a2 = r.a / 2f
        val corners = ArrayList<FloatArray>(4)
        // Norte (izq), Sur (der), Suroeste, Noreste en ejes locales del rect
        corners.add(world(r, -l2, a2))
        corners.add(world(r, l2, a2))
        corners.add(world(r, l2, -a2))
        corners.add(world(r, -l2, -a2))
        return corners
    }

    private fun world(r: RectInfo, lx: Float, lz: Float): FloatArray =
        floatArrayOf(
            r.cx + Math.cos(r.rot.toDouble()).toFloat() * lx + Math.sin(r.rot.toDouble()).toFloat() * lz,
            r.y,
            r.cz - Math.sin(r.rot.toDouble()).toFloat() * lx + Math.cos(r.rot.toDouble()).toFloat() * lz
        )

    private fun round2(v: Float): Float = round(v * 100f) / 100f
}