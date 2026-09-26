# HidroPlan AR — App nativa Android (WebView + ARCore)

App híbrida que empaqueta todo el proyecto web (los-no-marchitados) en un APK y reemplaza el
AR del navegador (que fallaba en los celulares del usuario) por **ARCore nativo**.

```
apps/android/
├── settings.gradle.kts / build.gradle.kts / gradle.properties   ← Gradle 8.14.3, AGP 8.13.2
├── app/
│   ├── build.gradle.kts        ← compileSdk 36, targetSdk 36, minSdk 26, ARCore + SceneView
│   └── src/main/
│       ├── AndroidManifest.xml ← CAMERA/INTERNET, requires ARCore + OpenGL ES 3.0
│       ├── assets/
│       │   ├── web/            ← index.html, prototipo.html, vendor/, modelos/ (app web embebida)
│       │   └── models/         ← hidroponia*.glb + models/ar/{punto,plano}.glb (generados)
│       ├── java/com/hidroplan/app/
│       │   ├── MainActivity.kt         ← shell WebView + puente JS ↔ nativo
│       │   └── ar/
│       │       ├── Presets.kt          ← Índice de preset (mismos datos que AR_MODELOS del web)
│       │       ├── MedicionMath.kt     ← PCA puro (puerto 1:1 de computeRect del web)
│       │       ├── MedicionOverlay.kt  ← overlay 2D (retícula, marcadores, guía del rectángulo)
│       │       ├── ArMedicionActivity.kt ← Medir con AR: toques ≥3 → rectángulo → medidas
│       │       └── ArModeloActivity.kt ← Ver en 3D: GLB del preset a escala real sobre el piso
│       └── res/                 ← layouts y estilos (Material3, HUD oscuro)
└── (gradlew generado con la dist 8.14.3 ya cacheada en ~/.gradle)
```

## Decisiones tomadas

- **Híbrida**: el 95% del producto (configuración, cálculo de bomba/volumen, guías, material)
  vive en `prototipo.html` y se reutiliza **tal cual** dentro de un WebView offline
  (`file:///android_asset/web/prototipo.html`). No depende de GitHub Pages ni de red.
- **ARCore nativo** para las dos pantallas AR, porque el navegador no garantiza WebXR ni cámara
  (ese fue el bug del sitio web). El sitio pide **Google Play Services for AR** (manifest marca
  `com.google.ar.core=required`).
- **Solo ARCore**: sin modo de medición sin AR (decisión del usuario).
- Render AR con **SceneView 2.x** (Filament): gestiona la cámara, sesión ARCore, luz y el
  pipeline glTF; nosotros solo ponemos nodos (`AnchorNode` + `ModelNode`).
- Detección de superficie: planos horizontales confirmados tienen prioridad; DepthPoint se usa
  como respaldo solo cuando el dispositivo admite Depth AUTOMATIC. Instant Placement queda
  desactivado para no confundir anclas provisionales con suelo detectado.
- Colocación 3D: al cargar cada GLB se ajustan sus límites a largo × ancho × alto del preset,
  se centra la huerta alrededor del toque y se corrige el origen vertical para apoyar la base
  sobre el plano AR, incluso después de orientar el largo hacia adelante.
- “Ver en 3D” solo acepta planos horizontales confirmados (profundidad sobre muebles o paredes
  ya no puede dejar el modelo suspendido). Los GLB se generan con medidas exactas, origen
  centrado y materiales mate con color más estable bajo la iluminación estimada de ARCore.

## Cómo se conecta el web con las actividades nativas

Al terminar de cargar el HTML se inyecta JS (`MainActivity.BOOT_JS`) que **pisa los botones AR**:

```js
window.abrirARMedicion = ... AndroidApp.abrirMedicion(...);   // Medir en AR
window.abrirARModelo   = ... AndroidApp.abrirModelo(...);     // Ver tu proyecto en 3D
window.abrirARCompat   = ... AndroidApp.abrirMedicion(...);   // Modo compatible → lo mismo
```

`AndroidApp` es un `@JavascriptInterface`. El flujo:

1. Toca "Medir en AR" → `abrirMedicion(pLargo)` → MainActivity pide permiso de cámara,
   verifica disponibilidad de ARCore (`checkAvailabilityAsync`) y abre `ArMedicionActivity`.
2. En AR el usuario **toca la superficie** y marca ≥3 puntos (los tres web: piso especialmente).
   El PCA ajusta un rectángulo (mismo algoritmo y reglas de redondeo que `computeRect` del web,
   para que los valores coincidan punto a punto con el navegador).
3. "Usar medidas" → devuelve `largo`/`ancho` (String `%.2f`, como `toFixed(2)` del web) →
   MainActivity inyecta `setMedidasApp(l, a)` que setea `#inLargo`/`#inAncho` y llama
   `calcularMedidas()` (exactamente lo que hace `applyMeasure()` en el navegador).
4. "Ver en 3D" → `abrirModelo(pLargo)` → `ArModeloActivity` coloca el GLB del preset
   apropiado (misma regla `argmin |L_canal − longitud|` que `setARModelForParams`).

El puente le pasa el valor de `#pLargo` (input visible con la longitud del proyecto), por lo que
no depende de variables internas del web.

## Algoritmos (espejo del web)

- **Selección de preset** — `Presets.forLongitud()`: mismo criterio que el sitio
  (`Math.abs(m.L − longitud)`). Tabla idéntica: compact 1,9×1,2×0,8 · 2,4 m 3,1×1,6×1,3 ·
  3,6 m 4,3×2,0×1,7 · 4,8 m 5,5×2,0×2,6.
- **Rectángulo por PCA** — `MedicionMath.computeRect()`: covarianza en XZ, eigenvectores por
  `0.5·atan2(2·cxz, cxx−czz)`, extents en u/v, `l = max`, `a = min`, redondeo a 2 decimales.
- **Alineación del modelo en AR** — el largo del GLB corre en **+X**; sobre el `hitPose` se
  suma un yaw tal que +X apunte en la dirección dispositivo→punto (alejándose del usuario).

## Build

```bash
cd apps/android
./gradlew :app:assembleDebug
# APK: apps/android/app/build/outputs/apk/debug/app-debug.apk (~40 MB)
```

- Gradle 8.14.3 (la dist ya está cacheada en `~/.gradle/wrapper/dists`) + AGP 8.13.2.
- `local.properties` con `sdk.dir` si Android Studio no está configurado, o exportar
  `ANDROID_HOME=/home/valentino/Android/Sdk`.

## Instalar y probar en un celular

1. Conectá el teléfono por USB (o `adb connect` si tenés debug inalámbrico) y:
   `adb install -r app/build/outputs/apk/debug/app-debug.apk`
2. Requisito: Android con **Google Play Services for AR** instalado (la app lo abre/lo pide en
   el primer uso de AR). Si el equipo no es compatible, la app sigue abriendo el web normal
   (solo el AR nativo no funcionará).
3. Prueba mínima:
   - Abrir la app → debe ver el sitio completo offline (tabs: Calcular, Hidroponía, Materiales…).
   - "Medir en AR": marcar 3+ esquinas del piso → "Usar medidas" → el formulario se actualiza.
   - "Ver en 3D": elegir longitud del proyecto → tocar el piso → ver el sistema a escala.

## Estado

- ✅ Proyecto compilable desde cero; `assembleDebug` genera **app-debug.apk**.
- ✅ Assets embebidos (web + modelos + marcadores), sin CDN.
- ✅ **Validado en dispositivo real (moto e14, MIUI, adb/USB)**:
  - Instalación y arranque sin crash; site v10 cargado offline (datos persistidos).
  - ARCore instalado on-demand: el tap de AR → `checkAvailabilityAsync` (SUPPORTED_NOT_INSTALLED)
    → `requestInstall(true)` → Play Store → `com.google.ar.core` instalado.
  - Botón "📏 Medir en realidad aumentada" del web → abre `ArMedicionActivity` (cámara, HUD
    Borrar/Usar medidas/Cerrar, hit-test funcional: sin superficie a la vista avisa "No detectó…").
  - `abrirARModelo()` → `ArModeloActivity` abre y cierra correctamente.
  - Ciclos abrir/cerrar sin crash (ver fix de teardown abajo).
  - Debugging WebView activo solo en build de debug (CDP por `adb forward localabstract:webview_devtools_remote_<pid>`).
- Fixes aplicados durante el test:
  - **Crash al cerrar AR**: SceneView 2.1.1 lanza NPE (`CameraNode.destroy → CameraComponent.getCamera`)
    en el LifeCycleObserver cuando la sesión no llegó a iniciar, tumbando la Activity. Se blindó con
    `ar/SafeARSceneView.kt` (destroy con try/catch) usado en ambos layouts AR.
  - **Aviso falso "Tu navegador/celular no soporta WebXR-AR"**: en la app se sobreescribe
    `AREngine.supported = () => true` (referencia directa al binding, no `window.AREngine`, que es
    un `const` de script) y se fuerzan ocultos `webarUnavailable`/`webarUnavailable4`.
  - `buildFeatures.buildConfig = true` para `BuildConfig.DEBUG`.
- ⏳ **Pendiente de prueba física (requiere apuntar la cámara a un piso real)**:
  marcar 3+ esquinas → "Usar medidas" → confirmar que `inLargo`/`inAncho` se pueblan en el web
  y se recalcula el proyecto; probar "Ver en 3D" colocando el modelo a escala; ajustar marcadores,
  retícula y alineación si hace falta. Escenarios: superficies oscuras/brillo, VERTICAL vs HORIZONTAL.
- El web site de GitHub Pages se mantiene intacto (los no-marchitados `main`).
