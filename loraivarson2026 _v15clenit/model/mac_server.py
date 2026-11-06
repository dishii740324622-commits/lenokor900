#!/usr/bin/env python3
import json
import os
import time
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HOST = "0.0.0.0"
PORT = 8787
LIMIT = 8
BAN_DAYS = 10
STATE_FILE = os.path.join(os.path.dirname(__file__), "lor_usage.json")


def load_state():
    try:
        with open(STATE_FILE, "r", encoding="utf-8") as state_file:
            return json.load(state_file)
    except (FileNotFoundError, json.JSONDecodeError):
        return {"devices": {}}


def save_state(state):
    temporary_file = STATE_FILE + ".tmp"
    with open(temporary_file, "w", encoding="utf-8") as state_file:
        json.dump(state, state_file, ensure_ascii=False, indent=2)
    os.replace(temporary_file, STATE_FILE)


def today():
    return datetime.now(timezone.utc).strftime("%Y-%m-%d")


def device_record(state, device_id):
    devices = state.setdefault("devices", {})
    return devices.setdefault(device_id, {"day": today(), "count": 0, "banned_until": 0, "accounts": []})


class LorHandler(BaseHTTPRequestHandler):
    def send_json(self, status, payload):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        self.end_headers()

    def do_GET(self):
        if self.path == "/health":
            self.send_json(200, {"app": "Lor Ai", "model": "Lor5.7 prime", "status": "online"})
            return
        self.send_json(404, {"error": "المسار غير موجود"})

    def do_POST(self):
        if self.path != "/generate":
            self.send_json(404, {"error": "المسار غير موجود"})
            return
        try:
            length = int(self.headers.get("Content-Length", "0"))
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
        except (ValueError, json.JSONDecodeError):
            self.send_json(400, {"error": "بيانات الطلب غير صالحة"})
            return

        device_id = str(payload.get("device_id", "")).strip()
        account_id = str(payload.get("account_id", "guest")).strip()
        prompt = str(payload.get("prompt", "")).strip()
        if not device_id or not prompt:
            self.send_json(400, {"error": "الجهاز ووصف الفيديو مطلوبان"})
            return

        state = load_state()
        record = device_record(state, device_id)
        now = time.time()
        if record.get("banned_until", 0) > now:
            remaining = int((record["banned_until"] - now) / 86400) + 1
            self.send_json(403, {"error": f"تم حظر هذا الجهاز لمدة {remaining} أيام بسبب تجاوز سياسة الاستخدام", "banned": True})
            return
        if record.get("day") != today():
            record.update({"day": today(), "count": 0, "accounts": []})
        accounts = record.setdefault("accounts", [])
        if account_id not in accounts and accounts:
            record["banned_until"] = now + BAN_DAYS * 86400
            save_state(state)
            self.send_json(403, {"error": "تم رصد محاولة تغيير الحساب لتجاوز الحد. حُظر الجهاز 10 أيام.", "banned": True})
            return
        if record["count"] >= LIMIT:
            self.send_json(429, {"error": "لقد استنفذت الحد اليومي من إنشاء الفيديوهات (8 توليدات). حاول غدًا.", "remaining": 0})
            return

        if account_id not in accounts:
            accounts.append(account_id)
        record["count"] += 1
        save_state(state)
        self.send_json(202, {"status": "queued", "model": "Lor5.7 prime", "generation_id": f"lor-{int(now)}", "remaining": LIMIT - record["count"], "message": "تم قبول الفيديو في طابور التوليد المحلي"})

    def log_message(self, format_string, *args):
        print(f"[Lor Ai] {self.address_string()} - {format_string % args}")


if __name__ == "__main__":
    print(f"Lor Ai local server listening on http://{HOST}:{PORT}")
    print("Android emulator: use http://10.0.2.2:8787")
    ThreadingHTTPServer((HOST, PORT), LorHandler).serve_forever()
