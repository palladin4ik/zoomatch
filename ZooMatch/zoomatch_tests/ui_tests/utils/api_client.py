import requests
from typing import Dict, Optional

BASE_URL = "http://192.168.0.154:8000/api/v1/"

class ApiClient:
    def __init__(self):
        self.session = requests.Session()
        self.token: Optional[str] = None

    def register(self, email: str, password: str, name: str) -> Dict:
        payload = {"email": email, "password": password, "name": name}
        resp = self.session.post(f"{BASE_URL}register/", json=payload)
        resp.raise_for_status()
        return resp.json()

    def login(self, email: str, password: str) -> str:
        payload = {"email": email, "password": password}
        resp = self.session.post(f"{BASE_URL}jwt/create/", json=payload)
        resp.raise_for_status()
        data = resp.json()
        self.token = data["access"]
        return self.token # type: ignore

    def get_profile(self) -> Dict:
        headers = {"Authorization": f"Bearer {self.token}"}
        resp = self.session.get(f"{BASE_URL}me/", headers=headers)
        resp.raise_for_status()
        return resp.json()

    def delete_user(self) -> bool:
        if not self.token:
            return False
        headers = {"Authorization": f"Bearer {self.token}"}
        resp = self.session.delete(f"{BASE_URL}me/", headers=headers)
        return resp.status_code in (200, 204)