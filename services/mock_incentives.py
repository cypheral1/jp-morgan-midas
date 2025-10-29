#!/usr/bin/env python3
import http.server
import socketserver
import json
from urllib.parse import urlparse

PORT = 8080

class Handler(http.server.BaseHTTPRequestHandler):
    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path != '/incentive':
            self.send_response(404)
            self.end_headers()
            return
        length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(length)
        try:
            data = json.loads(body.decode('utf-8'))
            amount = float(data.get('amount', 0.0))
        except Exception:
            amount = 0.0
        # deterministic incentive: 5% of amount, rounded to 2 decimals
        incentive = round(amount * 0.05, 2)
        resp = {'amount': incentive}
        resp_b = json.dumps(resp).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(resp_b)))
        self.end_headers()
        self.wfile.write(resp_b)

    def log_message(self, format, *args):
        # silence logs
        return

if __name__ == '__main__':
    with socketserver.TCPServer(('0.0.0.0', PORT), Handler) as httpd:
        print(f'Mock incentives API listening on http://0.0.0.0:{PORT}/incentive')
        httpd.serve_forever()
