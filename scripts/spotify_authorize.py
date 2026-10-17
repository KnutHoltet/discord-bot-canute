#!/usr/bin/env python3
"""One-time local Spotify Authorization Code flow; no third-party token service."""
import base64
import json
import os
import secrets
import time
import urllib.error
import urllib.parse
import urllib.request
import webbrowser
from http.server import BaseHTTPRequestHandler, HTTPServer


def main():
    client_id = os.environ['SPOTIFY_CLIENT_ID']
    client_secret = os.environ['SPOTIFY_CLIENT_SECRET']
    redirect_uri = 'http://127.0.0.1:8888/callback'
    state = secrets.token_urlsafe(32)
    result = {}

    class Callback(BaseHTTPRequestHandler):
        def do_GET(self):
            parsed = urllib.parse.urlparse(self.path)
            query = urllib.parse.parse_qs(parsed.query)
            if parsed.path != '/callback' or query.get('state') != [state]:
                self.send_error(400, 'Invalid authorization callback')
                return
            result.update(query)
            self.send_response(200)
            self.send_header('Content-Type', 'text/plain; charset=utf-8')
            self.end_headers()
            self.wfile.write(b'Return to your terminal. You may close this tab.')

        def log_message(self, *_args):
            pass  # Never log authorization codes.

    url = 'https://accounts.spotify.com/authorize?' + urllib.parse.urlencode({
        'client_id': client_id, 'response_type': 'code', 'redirect_uri': redirect_uri,
        'scope': 'playlist-read-private playlist-read-collaborative', 'state': state,
    })
    with HTTPServer(('127.0.0.1', 8888), Callback) as server:
        server.timeout = 1
        print('Authorize your Spotify account in your browser:\n' + url)
        webbrowser.open(url)
        deadline = time.monotonic() + 300
        while not result and time.monotonic() < deadline:
            server.handle_request()
    if 'code' not in result:
        raise SystemExit('Authorization cancelled, denied or timed out. Run again to retry.')
    data = urllib.parse.urlencode({
        'grant_type': 'authorization_code', 'code': result['code'][0], 'redirect_uri': redirect_uri,
    }).encode()
    auth = base64.b64encode(f'{client_id}:{client_secret}'.encode()).decode()
    request = urllib.request.Request('https://accounts.spotify.com/api/token', data=data,
                                     headers={'Authorization': 'Basic ' + auth})
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            token = json.load(response)['refresh_token']
    except (urllib.error.URLError, KeyError):
        raise SystemExit('Token exchange failed. Check credentials and the registered redirect URI.')
    print('\nKeep this secret; set it in the BOT process environment:')
    print('SPOTIFY_REFRESH_TOKEN=' + token)


if __name__ == '__main__':
    main()
