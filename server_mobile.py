import http.server
import socket
import socketserver
import os
import sys

PORT = 8000

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
    def end_headers(self):
        self.send_header('Cache-Control', 'no-store, must-revalidate')
        super().end_headers()

def main():
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    local_ip = get_local_ip()

    print("=" * 60)
    print("HIDROPLAN - SERVIDOR PARA DISPOSITIVOS MOVILES")
    print("=" * 60)
    print(f"\n1. Conecta tu celular a la misma red Wi-Fi que esta PC.")
    print(f"\n2. En el navegador de tu celular (Chrome o Safari), ingresa a:")
    print(f"\n   >>> http://{local_ip}:{PORT}/prototipo.html <<<")
    print(f"\n   O en tu PC: http://localhost:{PORT}/prototipo.html")
    print("\n   [Presiona Ctrl+C para detener el servidor]")
    print("=" * 60 + "\n")

    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), CustomHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nServidor detenido.")
            sys.exit(0)

if __name__ == '__main__':
    main()
