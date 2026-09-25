package com.hidroplan.app.ar

import com.google.ar.core.Camera
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView

/**
 * Ayudita compartida entre las pantallas AR de medición y colocación:
 * parte del "no detecta" real es ARCore no logrando tracking (poca luz / poca
 * textura / mucho movimiento / cámara ocupada). Acá se traducen esos estados a
 * guía accionable y se puede monitorear el progreso en vivo.
 */
object ArGuias {

    /** Config mínima que hace viable la detección temprana de piso. */
    fun applySessionConfig(config: Config) {
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        // Determinista: arrancamos con colocación instantánea y depth automático. El
        // AUTOMATIC permite anclar en pisos lisos (sin textura) cuando el device lo
        // soporta; ArSession lo baja a DISABLED solo si el hardware no puede.
        config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
        config.depthMode = Config.DepthMode.AUTOMATIC
    }

    fun isTracking(camera: Camera?): Boolean = camera?.trackingState == TrackingState.TRACKING

    /** Cuenta planos horizontales TRACKING vistos por la sesión (para feedback en vivo). */
    fun planeCount(session: Session?): Int =
        session?.getAllTrackables(Plane::class.java)
            ?.count { it.trackingState == TrackingState.TRACKING }
            ?: 0

    /** Mensaje accionable según el motivo de falla de tracking de ARCore. */
    fun reasonGuide(reason: TrackingFailureReason?): String? = when (reason) {
        TrackingFailureReason.INSUFFICIENT_LIGHT ->
            "Hay poca luz: encendé una luz o acercate a una ventana y volvé a apuntar al piso."
        TrackingFailureReason.INSUFFICIENT_FEATURES ->
            "El piso tiene poca textura: buscá cerámicos, alfombra, hormigón rayado o algún objeto que marque contraste."
        TrackingFailureReason.EXCESSIVE_MOTION ->
            "Estás moviendo el teléfono muy rápido: barré la cámara despacio de lado a lado."
        TrackingFailureReason.CAMERA_UNAVAILABLE ->
            "La cámara no está disponible: cerrá esta pantalla y volvé a entrar."
        TrackingFailureReason.BAD_STATE ->
            "ARCore se desincronizó: esperá un momento o reiniciá la pantalla."
        else -> null
    }

    /** Estado textual corto del tracking para mostrar en vivo. */
    fun trackingLabel(camera: Camera?): String = when (camera?.trackingState) {
        TrackingState.TRACKING -> "cámara estable"
        TrackingState.PAUSED -> "cámara pausada"
        TrackingState.STOPPED -> "cámara detenida"
        else -> "inicializando"
    }

    /** Guía general según el estado para mostrar cuando todavía no se marcó nada. */
    fun statusHint(
        arSceneView: ARSceneView,
        tapCount: Int,
    ): String {
        if (tapCount > 0) return ""
        val camera = arSceneView.frame?.camera
        if (!isTracking(camera)) {
            return reasonGuide(camera?.trackingFailureReason)
                ?: "Inicializando la cámara AR… mové el teléfono en movimientos suaves."
        }
        val planes = planeCount(arSceneView.session)
        return if (planes == 0) {
            "Buscando el piso… apuntá a un piso con textura y mové el teléfono de un lado a otro."
        } else {
            "Piso detectado ($planes). Tocá la pantalla para marcar la primera esquina."
        }
    }
}