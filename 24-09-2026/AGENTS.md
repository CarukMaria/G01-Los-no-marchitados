# G01-Los-no-marchitados — HidroPlan

## Qué es esto
- Producto single-file web: `prototipo.html` (CSS/JS inline). `index.html` solo redirige y `v.*.laza_prototipo.html` son versiones viejas: **no tocar**.
- App Android híbrida en `apps/android`: WebView offline con el web embebido + **ARCore nativo** (reemplaza el WebXR del navegador).
- `main` = GitHub Pages (pushear a main publica el sitio). Sin CI ni linters; la verificación es manual.
- Requisitos de producto: `specs/*.md`, `project.md`, `contract.md`, `PromptSpecs.txt`. Docs y commits en español.
- La UI del web se trabaja en la raíz; los detalles de la app están en `APP_ANDROID.md`.

## Web (`prototipo.html`)
- Sin CDN: `vendor/` (three, GLTFLoader, SweetAlert2) es local y la app corre 100% offline.
- Probar WebXR en navegador: `npm run ar` levanta `tools/testear_ar.sh` → `server_mobile.py` en `:8000` + `adb reverse` (http://localhost es contexto seguro en Chrome Android, no necesita certificados).

## App Android (`apps/android`)
- **Después de CADA edición del web, sincronizar la copia embebida** y reconstruir:
  `cp prototipo.html apps/android/app/src/main/assets/web/prototipo.html` (deben quedar md5 idénticos) → `./gradlew :app:installDebug`.
- APK en `app/build/outputs/apk/debug/app-debug.apk`. Requiere SDK (`apps/android/local.properties` gitignored o `ANDROID_HOME=/home/valentino/Android/Sdk`).
- **WebView cachea el asset**: tras reinstalar, `adb shell pm clear com.hidroplan.app` y relanzar para ver el HTML nuevo en el device.
- AR nativo: `MainActivity.BOOT_JS` pisa `window.abrirARMedicion/abrirARModelo/abrirARCompat` hacia el puente `AndroidApp`, sobreescribe `AREngine.supported` (referencia al binding, no a `window.AREngine`) y oculta `webarUnavailable`/`webarUnavailable4`. El manifest exige ARCore (`com.google.ar.core=required`).
- Lógica espejo web↔Kotlin que debe mantenerse idéntica: tabla de presets (`Presets.kt` ↔ `AR_MODELOS` del web), rectángulo por PCA (`MedicionMath.kt` ↔ `computeRect`), y `%.2f` ↔ `toFixed(2)` para largo/ancho.
- SceneView 2.1.1 tira NPE al cerrar si la sesión no arrancó: usar siempre `SafeARSceneView.kt`, nunca `ArSceneView` pelado.

## Validación on-device (no hay CI)
- Teléfono de referencia: `49I75LBA49MF89FA` (varía según conexión).
- JS: extraer el script inline de `prototipo.html` y `node --check`; luego rebuild + install + CDP.
- Build debug expone CDP del WebView:
  `adb forward tcp:9223 localabstract:webview_devtools_remote_$(adb shell pidof com.hidroplan.app)`
  y evaluar por el target ws (driver patrón en `/tmp/opencode/cdp.mjs`).
- Preferir evidencia de DOM/computed styles (estados, overflow, reglas CSS) antes que screenshots.

## ARCore 1.44 (hechos caros de reaprender)
- `TrackingState` = solo `TRACKING`/`PAUSED`/`STOPPED` (no existe `LIMITED`).
- `TrackingFailureReason` = `NONE`, `BAD_STATE`, `INSUFFICIENT_LIGHT`, `EXCESSIVE_MOTION`, `INSUFFICIENT_FEATURES`, `CAMERA_UNAVAILABLE`.
- `Session.configure`: `InstantPlacementMode.DISABLED` + `DepthMode.AUTOMATIC` solo cuando el HW lo soporta + planeFinding `HORIZONTAL_AND_VERTICAL`; medición prioriza planos horizontales confirmados y usa DepthPoint como respaldo, mientras que la colocación del modelo exige un plano horizontal confirmado. Lógica y textos de guía en `ArGuias.kt`; el HUD en vivo (tracking · falla · planos) lo usan `ArMedicionActivity` y `ArModeloActivity`.

## Herramientas
- `npm run model*` regenera GLB/USDZ/posters (`tools/generar_modelo.mjs`, `generar_usdz.py`, `generar_poster.mjs`). El paso USDZ necesita `tools/.venv` (gitignored). `node_modules` (three) es solo para estas tools, no para el web.

## Convenciones
- Commits/pushes solo cuando el usuario lo pida (suele decir "pushealo"). Sin comentarios en código salvo pedido.
