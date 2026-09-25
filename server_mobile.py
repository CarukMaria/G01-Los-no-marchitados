import http.server
import socket
import socketserver
import os
import sys
import ssl

PORT = 8000
# Certificados para HTTPS local: generarlos con mkcert (self-signed de plena confianza)
#   mkcert -install && mkcert localhost <IP_LAN>
CERT_FILE = 'localhost.pem'
KEY_FILE = 'localhost-key.pem'


def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(('10.255.255.255', 1))
        ip = s.getsockname()[0]
    except Exception:
        ip = '127.0.0.1'
    finally:
        s.close()
    return ip


class CustomHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=os.path.dirname(os.path.abspath(__file__)), **kwargs)

    # MIME correctos para los modelos 3D (los requiere WebXR / Scene Viewer / Quick Look)
    extensions_map = {
        **http.server.SimpleHTTPRequestHandler.extensions_map,
        '.glb': 'model/gltf-binary',
        '.gltf': 'model/gltf+json',
        '.usdz': 'model/vnd.usdz+zip',
        '.jpg': 'image/jpeg',
        '.svg': 'image/svg+xml',
    }

    def end_headers(self):
        # media coops necesarias para que WebXR y la cámara funcionen en móvil
        self.send_header('Cache-Control', 'no-store, must-revalidate')
        self.send_header('Cross-Origin-Opener-Policy', 'same-origin')
        self.send_header('Cross-Origin-Embedder-Policy', 'credentialless')
        super().end_headers()


def print_header(local_ip, use_https):
    print("=" * 70)
    print("HIDROPLAN - SERVIDOR PARA DISPOSITIVOS MOVILES")
    print("=" * 70)
    print(f"\n1. Conecta tu celular a la misma red Wi-Fi que esta PC.")
    print(f"\n2. En el navegador de tu celular (Chrome o Safari), ingresa a:")

    if use_https:
        print(f"\n   >>> https://{local_ip}:{PORT}/prototipo.html <<<\n")
        print("   [HTTPS] activo: AR (WebXR / AR Quick Look) habilitado.")
        print("\n   ANDROID: Chrome + ARCore usa WebXR; si no, cae a Google Scene Viewer.")
        print("            IMPORTANTE: Scene Viewer no puede leer IPs de LAN.")
        print("            Para ese modo necesitás un túnel HTTPS publico:")
        print(f"              cloudflared tunnel --url http://localhost:{PORT}")
    else:
        print(f"\n   >>> http://{local_ip}:{PORT}/prototipo.html <<<\n")
        print("   ⚠️  El modo AR (WebXR / AR Quick Look) NO funciona sobre http://.")
        print("       Para probar AR real: ejecutá el servidor con HTTPS:")
        print(f"         1) Generar certificado (una sola vez):")
        print(f"              mkcert -install")
        print(f"              mkcert localhost {local_ip}")
        print(f"         2) Arrancar con HTTPS:")
        print(f"              python server_mobile.py --https")
        print("\n       O exponé el sitio con un túnel HTTPS gratuito:")
        print(f"              cloudflared tunnel --url http://localhost:{PORT}")
        print(f"              ngrok http {PORT}\n")


def main():
    use_https = '--https' in sys.argv
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    local_ip = get_local_ip()

    print_header(local_ip, use_https)
    print("   [Presiona Ctrl+C para detener el servidor]")
    print("=" * 70 + "\n")

    socketserver.TCPServer.allow_reuse_address = True
    httpd = socketserver.TCPServer(("", PORT), CustomHandler)

    if use_https:
        if not (os.path.exists(CERT_FILE) and os.path.exists(KEY_FILE)):
            print(f"[ERROR] Faltan los certificados {CERT_FILE}/{KEY_FILE}.")
            print(f"        Generalos con:\n          mkcert -install\n          mkcert localhost {local_ip}")
            sys.exit(1)
        context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        context.load_cert_chain(CERT_FILE, KEY_FILE)
        try:
            httpd.socket = context.wrap_socket(httpd.socket, server_side=True)
        except Exception as e:
            print(f"[ERROR] No se pudo iniciar TLS: {e}")
            sys.exit(1)

    try:
        with httpd:
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServidor detenido.")
        sys.exit(0)


if __name__ == '__main__':
    main()