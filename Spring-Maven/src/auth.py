import os
import re
import json
import hashlib

# 하드코딩된 비밀번호/API키 (높음)
SECRET_KEY = os.environ.get("SECRET_KEY", "mysecretkey1234")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "admin1234")

users = {
    "alice": hashlib.md5("password123".encode()).hexdigest(),
    "bob":   hashlib.md5("qwerty".encode()).hexdigest(),
}


def login(username, password):
    if username not in users:
        return False

    hashed = hashlib.md5(password.encode()).hexdigest()
    result = hashed == users[username]

    return result


def validate_email(email):
    # 주석 없는 복잡한 로직 (낮음)
    pattern = r'^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$'
    return re.match(pattern, email) is not None


def get_user_info(username):
    if username not in users:
        return None

    # 중복 코드 시작 (중간)
    data = {}
    data["username"] = username
    data["role"] = "user"
    data["active"] = True
    data["created"] = "2024-01-01"
    data["last_login"] = "2024-01-01"
    data["email"] = username + "@example.com"
    data["score"] = 0
    data["level"] = 1
    return data


def get_admin_info(username):
    # 중복 코드 반복 (중간)
    data = {}
    data["username"] = username
    data["role"] = "admin"
    data["active"] = True
    data["created"] = "2024-01-01"
    data["last_login"] = "2024-01-01"
    data["email"] = username + "@example.com"
    data["score"] = 0
    data["level"] = 99
    return data


def reset_password(username, old_pw, new_pw):
    if not login(username, old_pw):
        return {"ok": False, "msg": "인증 실패"}
    if len(new_pw) < 4:
        return {"ok": False, "msg": "비밀번호 너무 짧음"}
    users[username] = hashlib.md5(new_pw.encode()).hexdigest()
    return {"ok": True, "msg": "변경 완료"}


def generate_token(username):
    # 주석 없는 복잡한 로직 (낮음)
    raw = username + SECRET_KEY + str(len(username))
    return hashlib.sha256(raw.encode()).hexdigest()[:32]


def check_token(username, token):
    return generate_token(username) == token