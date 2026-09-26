package com.hidroplan.app.ar

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import io.github.sceneview.ar.ARSceneView

/**
 * ARSceneView con teardown a prueba de crashes.
 *
 * SceneView 2.1.1 crashea (NPE en CameraNode.destroy -> CameraComponent.getCamera)
 * cuando la sesión AR no llegó a iniciar (engine sin arrancar) y la vista se destruye:
 * el LifeCycleObserver internmo lanza dentro del destroy de la Activity y tumba toda la app.
 * Acá se aisla ese fallo para que el cierre de la pantalla nunca mate el proceso.
 */
class SafeARSceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ARSceneView(context, attrs, defStyleAttr) {

    override fun destroy() {
        try {
            super.destroy()
        } catch (e: Exception) {
            Log.w("HidroPlan", "Teardown de ARSceneView incompleto (${e::class.simpleName}: ${e.message})")
        }
    }
}