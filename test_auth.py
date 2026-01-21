import requests
import json
import time

# CONFIG
BASE_URL = "http://localhost:8080/api/v1/auth"
# On génère un user unique via le timestamp
ts = int(time.time())
username = f"user_py_{ts}"
email = f"py_{ts}@test.com"
password = "Password123!"

print(f"🚀 1. Inscription de {username}...")
reg_payload = {
    "username": username,
    "password": password,
    "email": email,
    "phone": f"699{ts % 1000000:06d}",
    "firstName": "Python",
    "lastName": "Script",
    "roles": ["FLEET_MANAGER"]
}

# Pour le register multipart, on triche un peu en envoyant sans fichier pour simplifier
# (Le endpoint AuthController gère le fichier optionnel)
# Note: Si le Controller exige un multipart, on doit utiliser requests.post(files=...)
# Ici on tente le Login direct car tu as surement déjà créé un user.
# Mais créons-en un proprement.

# Register (Multipart simulé)
files = {
    'user': (None, json.dumps(reg_payload), 'application/json'),
}
resp_reg = requests.post(f"{BASE_URL}/register", files=files)

if resp_reg.status_code not in [200, 201]:
    print(f"❌ Erreur Register: {resp_reg.text}")
    # On tente le login si user existe déjà
else:
    print("✅ Register OK")

print("\n🚀 2. Login...")
resp_login = requests.post(f"{BASE_URL}/login", json={"identifier": username, "password": password})

if resp_login.status_code != 200:
    print(f"❌ Login échoué: {resp_login.text}")
    exit(1)

data = resp_login.json()
access_token = data['accessToken']
refresh_token = data['refreshToken']

print(f"🔑 Access Token récupéré (début): {access_token[:20]}...")
print(f"🔑 Refresh Token récupéré (début): {refresh_token[:20]}...")

print("\n🚀 3. Test Refresh Token...")
resp_refresh = requests.post(f"{BASE_URL}/refresh", json={"refreshToken": refresh_token})

if resp_refresh.status_code == 200:
    print("✅ REFRESH SUCCESS ! Nouveau token reçu.")
    new_data = resp_refresh.json()
    print(f"✨ Nouvel Access Token: {new_data['accessToken'][:20]}...")
else:
    print(f"❌ REFRESH FAILED: {resp_refresh.status_code}")
    print(f"🔍 Réponse: {resp_refresh.text}")