#!/usr/bin/env bash
# ────────────────────────────────────────────────────────────────────────────
# Test del AR real en Android por USB — WebXR sobre http://localhost
#
# Chrome en Android trata "http://localhost" como contexto seguro (igual que
# HTTPS), así que WebXR queda habilitado SIN certificados y SIN Wi-Fi. El
# `adb reverse` mapea el localhost del teléfono hacia el puerto 8000 de la PC.
#
# Uso:
#   tools/testear_ar.sh            # levanta el server, reverse y abre Chrome
#   tools/testear_ar.sh --keep     # deja la sesión viva (para la demo)
#
# Requisitos en el teléfono: Opciones de desarrollador → Depuración USB activa,
# y autorizar el prompt RSA al conectarlo. Chrome (y ARCore) actualizados.
# ────────────────────────────────────────────────────────────────────────────
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PORT=8000
KEEP=0
[[ "${1:-}" == "--keep" ]] && KEEP=1

cd "$ROOT"

SERVER_PID=""
cleanup() {
    if [[ "$KEEP" != 1 ]]; then
        [[ -n "$SERVER_PID" ]] && kill "$SERVER_PID" 2>/dev/null || true
        adb reverse --remove "tcp:$PORT" >/dev/null 2>&1 || true
        echo ""
        echo "Sesión de prueba finalizada (server detenido y reverse removido)."
    fi
}
trap cleanup EXIT

# 1) Servidor del prototipo
if curl -s -o /dev/null --max-time 1 "http://localhost:$PORT/prototipo.html"; then
    echo "> Server ya activo en :$PORT (no lo reinicio)."
else
    echo "> Levantando server en :$PORT…"
    python3 server_mobile.py > "/tmp/hidroplan_server.log" 2>&1 &
    SERVER_PID=$!
    sleep 1.5
    curl -s -o /dev/null --max-time 2 "http://localhost:$PORT/prototipo.html" \
        || { echo "[ERROR] El server no respondió. Mirá /tmp/hidroplan_server.log"; exit 1; }
    echo "   OK → prototipo.html"
fi

# 2) Teléfono por USB
if ! adb get-state >/dev/null 2>&1; then
    echo ""
    echo "⚠️  No se detecta el teléfono por USB."
    echo "   En el celu: Ajustes → Acerca del teléfono → tocar 7 veces 'Número de compilación'"
    echo "   para habilitar Opciones de desarrollador → Depuración USB (ON)."
    echo "   Luego conectá el cable y aceptá el prompt RSA. Volvé a correr este comando."
    exit 1
fi
SERIAL="$(adb devices | awk 'NR==2 && $2=="device"{print $1}')"
if [[ -z "$SERIAL" ]]; then
    echo "⚠️  El dispositivo está en 'unauthorized' (falta aceptar el prompt RSA) o sin driver."
    adb devices
    exit 1
fi
echo "> Teléfono detectado: $SERIAL"

# 3) Reverse: puerto 8000 del teléfono → puerto 8000 de la PC
adb -s "$SERIAL" reverse "tcp:$PORT" "tcp:$PORT"
echo "> adb reverse tcp:$PORT → localhost del teléfono."

# 4) Abrir la app directamente en Chrome del celular
echo "> Abriendo http://localhost:$PORT/prototipo.html en el Chrome del teléfono…"
adb -s "$SERIAL" shell am start \
    -a android.intent.action.VIEW \
    -d "http://localhost:$PORT/prototipo.html" \
    >/dev/null 2>&1 || {
    echo "   (No se pudo auto-abrir; abrí la URL manualmente en Chrome.)"
    echo "   http://localhost:$PORT/prototipo.html"
}

echo ""
echo "────────────────────────────────────────────────────────────────────────"
echo "CHECKLIST EN EL TELÉFONO"
echo "────────────────────────────────────────────────────────────────────────"
echo "  1. Navegá hasta el creador de plataforma (Pantalla 1) y setea medidas:"
echo "     ej. largo 3,4 m · ancho 1,5 m · alto 2,4 m → 'Continuar'."
echo "  2. Andá a Pantalla 4 · 3D (tab 'Vista 3D')."
echo "     - El badge debe mostrar 'AR · ESCALA REAL 1:1 · X m' (preset según sliders)."
echo "     - El visor renderiza el GLB con el poster SVG; rotalo con el dedo."
echo "  3. Mové los sliders (largo/altura/líneas): la 3D reacts y el preset AR cambia."
echo "  4. Tocá 'Ver en tu espacio' → permití cámara → apuntá al piso."
echo "     - Chip: '📡 Apuntá al piso…' → '✅ Anclado al piso · escala real 1:1'."
echo "     - Caminá alrededor: el modelo queda anclado y a escala 1:1."
echo "────────────────────────────────────────────────────────────────────────"
[[ "$KEEP" == 1 ]] && echo "[--keep] La sesión queda viva: server y reverse activos."