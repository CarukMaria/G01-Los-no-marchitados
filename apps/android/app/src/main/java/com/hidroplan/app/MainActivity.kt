package com.hidroplan.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.hidroplan.app.ar.ArMedicionActivity
import com.hidroplan.app.ar.ArModeloActivity

/**
 * Hybrid shell: embebe prototipo.html (offline, sin depender de GitHub Pages) en un WebView y
 * engancha los botones AR del web (medir/modelo) hacia las actividades ARCore nativas.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var loader: View

    private var pendingArClass: Class<*>? = null
    private var pendingLongitud: String? = null

    private val camReq = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchPendingAr()
        } else {
            Toast.makeText(this, R.string.ar_cam_denied, Toast.LENGTH_LONG).show()
        }
    }

    private val arReq = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val l = result.data!!.getStringExtra(ArMedicionActivity.EXTRA_LARGO)
            val a = result.data!!.getStringExtra(ArMedicionActivity.EXTRA_ANCHO)
            if (l != null && a != null) injectMeasured(l, a)
        }
    }

    /** Puente expuesto al web: window.AndroidApp.abrirMedicion(...) / abrirModelo(...) / abrirCompat(...). */
    private inner class Bridge {
        @JavascriptInterface
        fun abrirMedicion(longitud: String?) {
            requestAr(ArMedicionActivity::class.java, longitud)
        }

        @JavascriptInterface
        fun abrirModelo(longitud: String?) {
            requestAr(ArModeloActivity::class.java, longitud)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web = findViewById(R.id.web)
        loader = findViewById(R.id.loader)

        setupWeb()
        web.loadUrl("file:///android_asset/web/prototipo.html")
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWeb() {
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.settings.mediaPlaybackRequiresUserGesture = false

        web.addJavascriptInterface(Bridge(), "AndroidApp")

        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.evaluateJavascript(BOOT_JS, null)
                loader.visibility = View.GONE
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                result.confirm()
                return true
            }
        }
    }

    private fun requestAr(arClass: Class<*>, longitud: String?) {
        pendingArClass = arClass
        pendingLongitud = longitud
        val granted = checkSelfPermission(android.Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchPendingAr()
        } else {
            camReq.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchPendingAr() {
        val arClass = pendingArClass ?: return
        pendingArClass = null
        checkArAvailability { ok ->
            if (ok) {
                val i = Intent(this, arClass)
                i.putExtra("longitud", pendingLongitud)
                pendingLongitud = null
                arReq.launch(i)
            } else {
                pendingLongitud = null
            }
        }
    }

    private fun checkArAvailability(onResult: (Boolean) -> Unit) {
        try {
            ArCoreApk.getInstance().checkAvailabilityAsync(this) { availability ->
                runOnUiThread {
                    when (availability) {
                        ArCoreApk.Availability.SUPPORTED_INSTALLED -> onResult(true)
                        ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED,
                        ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> {
                            ArCoreApk.getInstance().requestInstall(this, true)
                            onResult(false)
                        }
                        else -> {
                            Toast.makeText(this, R.string.ar_no_core, Toast.LENGTH_LONG).show()
                            onResult(false)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            Toast.makeText(this, R.string.ar_no_core, Toast.LENGTH_LONG).show()
            onResult(false)
        }
    }

    /** Vuelca el resultado de la medición AR al formulario del web (mismo flujo que applyMeasure). */
    private fun injectMeasured(l: String, a: String) {
        web.evaluateJavascript("window.setMedidasApp && setMedidasApp('$l', '$a');", null)
    }

    override fun onBackPressed() {
        if (web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        /**
         * Inyectado al cargar el web (después de onPageFinished):
         * - Pisa los botones AR para disparar las actividades nativas.
         * - Expone setMedidasApp(l,a) que setea inLargo/inAncho y recalcula (como applyMeasure).
         */
        private const val BOOT_JS =
            "window.__EN_APP = true;" +
            "window.abrirARMedicion = function(){ try { AndroidApp.abrirMedicion(document.getElementById('pLargo') ? document.getElementById('pLargo').value : ''); } catch (e) { AndroidApp.abrirMedicion(''); } };" +
            "window.abrirARModelo = function(){ try { AndroidApp.abrirModelo(document.getElementById('pLargo') ? document.getElementById('pLargo').value : ''); } catch (e) { AndroidApp.abrirModelo(''); } };" +
            "window.abrirARCompat = function(){ try { AndroidApp.abrirMedicion(document.getElementById('pLargo') ? document.getElementById('pLargo').value : ''); } catch (e) { AndroidApp.abrirMedicion(''); } };" +
            "window.setMedidasApp = function(l, a){ try { var il = document.getElementById('inLargo'); var ia = document.getElementById('inAncho'); if (il) il.value = '' + l; if (ia) ia.value = '' + a; if (window.calcularMedidas) calcularMedidas(); } catch (e) {} };"
    }
}