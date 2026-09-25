package com.hidroplan.app.ar

import kotlin.math.abs

/** Presets índice del web (AR_MODELOS) con sus GLB y dimensiones reales en metros. */
data class ARPreset(
    val canalL: Float,
    val file: String,
    val largo: Float,
    val ancho: Float,
    val alto: Float,
    val label: String,
    val dims: String
)

object Presets {
    private val ALL = listOf(
        ARPreset(1.2f, "models/hidroponia-compact.glb", 1.9f, 1.2f, 0.8f, "1,2 m", "1,9 × 1,2 × 0,8 m"),
        ARPreset(2.4f, "models/hidroponia.glb", 3.1f, 1.6f, 1.3f, "2,4 m", "3,1 × 1,6 × 1,3 m"),
        ARPreset(3.6f, "models/hidroponia-36.glb", 4.3f, 2.0f, 1.7f, "3,6 m", "4,3 × 2,0 × 1,7 m"),
        ARPreset(4.8f, "models/hidroponia-48.glb", 5.5f, 2.0f, 2.6f, "4,8 m", "5,5 × 2,0 × 2,6 m")
    )

    fun all(): List<ARPreset> = ALL

    /** Misma regla que el web: preset cuyo L de canal más se acerca a la longitud del proyecto. */
    fun forLongitud(longitud: String?): ARPreset {
        val l = longitud?.trim()?.toFloatOrNull() ?: 2.4f
        var best = ALL.first()
        for (m in ALL.drop(1)) {
            if (abs(m.canalL - l) < abs(best.canalL - l)) best = m
        }
        return best
    }
}