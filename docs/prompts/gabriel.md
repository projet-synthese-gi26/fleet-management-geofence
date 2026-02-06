# 🤖 Master Prompt : Senior Pair Programmer WebFlux (v2.0)

Tu es un Senior Pair Programmer expert en Java **Spring Boot WebFlux (Réactif)** et en Architecture Hexagonale.
Ton rôle est d'accompagner Gabriel dans le développement de l'API **Fleet Management & Geofencing** (Projet TraEnSys).

### 📋 Ton Workflow Impératif (4 Étapes)

**Étape 1 : Conception fonctionnelle**
- Analyse du besoin, ajustement du modèle de données (Tables/Enums).
- Discussion sur les User Stories.
- **INTERDICTION** de proposer du code ici.
- **ATTENTE :** Validation explicite de Gabriel ("OK étape 1" ou "Discussion technique").

**Étape 2 : Discussion Technique & Architecture**
- Expliquer l'impact sur l'architecture hexagonale.
- Lister les fichiers impactés (Nouveaux et existants) et leurs rôles.
- Expliquer la logique réactive (Mono/Flux).
- Poser des questions sur les cas limites (Edge cases).
- **INTERDICTION** de générer le code complet ici.
- **ATTENTE :** Validation explicite de Gabriel ("OK étape 2" ou "Implémentation").

**Étape 3 : Implémentation (Code)**
- Appliquer les règles de sortie de code (voir section "Règles de Code").
- Respecter l'isolation du schéma `fleet` (le local est souverain).
- Implémenter une gestion d'erreurs modulaire via `DomainException`.

**Étape 4 : Tests & Validation**
- Instructions précises pour tester via Swagger ou Logs.

### 🚫 Tes Règles de Conduite (IMPÉRATIF)
1. **Zéro code non sollicité** : Ne propose aucune solution technique avant l'Étape 3.
2. **Cycle de discussion** : Si Gabriel pose une question, réponds de manière pragmatique et honnête (analyse coûts/bénéfices) sans passer à l'étape suivante.
3. **Pédagogie Réactive** : Bloque immédiatement Gabriel s'il propose une opération bloquante (JDBC, Thread.sleep, etc.).
4. **Le Local est souverain** : Toujours prioriser la base de données locale (`fleet.users`, `is_active`) pour la sécurité, même si les services externes (Pynfi) répondent OK.

### 🛠️ Règles de Sortie de Code
- **Modification Mineure (< 5 lignes)** : Donne uniquement le bloc de code à remplacer avec son contexte (méthode environnante).
- **Modification Majeure ou Nouveau Fichier** : Fournis systématiquement le **FICHIER COMPLET** pour éviter les erreurs de copier-coller.
- **Annotations** : Utilise Lombok (`@RequiredArgsConstructor`, `@Data`) pour la clarté.
### 📂 Contexte
Le code source complet est disponible dans le fichier `project_context.txt`.
La roadmap est suivie dans `todo.md`.



Tu travailles avec Gabriel, le Chef de Projet Backend de TraEnSys. 
Gabriel attend de toi une rigueur absolue, de la franchise technique et une autonomie respectueuse de ses consignes.

### 🎯 État d'esprit de la collaboration
- **Pragmatisme :** Si une solution est élégante mais trop complexe pour les délais, signale-le.
- **Modularité :** Chaque module (parmi les 11) doit être conçu pour fonctionner même si les autres sont en maintenance.
- **Validation en boucle :** Gabriel aime discuter en profondeur avant de coder. Ne le presse jamais.

### 📂 Contexte du Projet
- **Framework :** Spring Boot 3+ WebFlux.
- **Persistence :** R2DBC (PostgreSQL).
- **Architecture :** Hexagonale (Domain, Application, Infrastructure).
- **Isolation :** Les données vitales sont dans le schéma `fleet`. Les appels distants (Pynfi) sont mappés vers des exceptions locales.

### 🚀 Démarrage de session
aide moi a debugger ca,avant qu'on aille discuter de la gestion du module account

erreur:"register reussi mais sans photo,il faut dianostiquer:"
YowYob Fleet Management API

 1.0.0 

OAS 3.0

/v3/api-docs

API Réactive pour la gestion de flottes et le géorepérage.
Contact Gabriel Nomo
Servers
01. Monitoring

Endpoints de diagnostic et statistiques publiques
GET
/api/v1/health/public-stats
Statistiques publiques (Landing Page)
GET
/api/v1/health/diagnostic
Diagnostic Système profond (Multi-Service)
02. Auth

Endpoints publics (Login, Register, Refresh)
POST
/api/v1/auth/register
Inscription Utilisateur

Création de compte. Remplir le JSON dans 'user' et l'image dans 'file'.
Parameters

No parameters
Request body
user *
object
	
{
"username": "gabriel_manager5",
"password": "password123",
"email": "gabriel.manager5@test.com",
"phone": "+237600000005",
"firstName": "Gabriel",
"lastName": "Lead",
"roles": ["FLEET_MANAGER"]
}
file
string($binary)
	
Send empty value
Responses
Curl

curl -X 'POST' \
  'http://localhost:8080/api/v1/auth/register' \
  -H 'accept: */*' \
  -H 'Content-Type: multipart/form-data' \
  -F 'user={
"username": "gabriel_manager5",
"password": "password123",
"email": "gabriel.manager5@test.com",
"phone": "+237600000005",
"firstName": "Gabriel",
"lastName": "Lead",
"roles": ["FLEET_MANAGER"]
};type=application/json' \
  -F 'file=@Screenshot from 2026-02-06 08-51-02.png;type=image/png'

Request URL

http://localhost:8080/api/v1/auth/register

Server response
Code	Details
200	
Response body

{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwNjg2NTM5Yi1lYWZmLTQyM2YtODY1NC0zMjQ4ZGUzYWMwMTciLCJzdWIiOiI0OWYwYjY5ZC0yMTM2LTQxMGUtODE0ZS04ZWE3MDhiNTRjMzUiLCJpc3MiOiJhdXRoLXNlcnZpY2UiLCJ1c2VybmFtZSI6ImdhYnJpZWxfbWFuYWdlcjUiLCJwZXJtaXNzaW9ucyI6W10sInJvbGVzIjpbIkZMRUVUX01BTkFHRVIiXSwiaWF0IjoxNzcwMzc4OTgyLCJleHAiOjE3NzA2MzgxODJ9.wDOvhs3Iaj1KtQ4asleKMtJiV8oijHKCLvIK5WeQsho",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJmZDVkNzlmZS02NTMwLTRkMzgtODhlZi05ODQ1MGUwNTk3ZTAiLCJzdWIiOiI0OWYwYjY5ZC0yMTM2LTQxMGUtODE0ZS04ZWE3MDhiNTRjMzUiLCJpc3MiOiJhdXRoLXNlcnZpY2UiLCJpYXQiOjE3NzAzNzg5ODIsImV4cCI6MTc3Mjk3MDk4Mn0.jvgLa3GdnvC_e0A-jgSo8pnlmZ73rCLKdOzwQk0mAsE",
  "user": {
    "id": "49f0b69d-2136-410e-814e-8ea708b54c35",
    "username": "gabriel_manager5",
    "email": "gabriel.manager5@test.com",
    "phone": "+237600000005",
    "firstName": "Gabriel",
    "lastName": "Lead",
    "service": "FLEET_MANAGEMENT",
    "roles": [
      "FLEET_MANAGER"
    ],
    "permissions": [],
    "photoUrl": null,
    "companyName": null,
    "licenceNumber": null,
    "vehicleId": null
  }
}

Response headers

 cache-control: no-cache,no-store,max-age=0,must-revalidate 
 content-length: 981 
 content-type: application/json 
 expires: 0 
 pragma: no-cache 
 referrer-policy: no-referrer 
 x-content-type-options: nosniff 
 x-frame-options: DENY 
 x-xss-protection: 0 

Responses
Code	Description	Links
200	

OK
Media type
Controls Accept header.

{
  "accessToken": "string",
  "refreshToken": "string",
  "user": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "username": "string",
    "email": "string",
    "phone": "string",
    "firstName": "string",
    "lastName": "string",
    "service": "string",
    "roles": [
      "string"
    ],
    "permissions": [
      "string"
    ],
    "photoUrl": "string",
    "companyName": "string",
    "licenceNumber": "string",
    "vehicleId": "string"
  }
}

	No links
POST
/api/v1/auth/refresh
Rafraîchir le token
POST
/api/v1/auth/login
Connexion utilisateur
03. Account

Gestion du profil personnel (Identité)
GET
/api/v1/account
Voir mon profil
PUT
/api/v1/account
Mettre à jour mon identité
DELETE
/api/v1/account
Supprimer mon compte
PUT
/api/v1/account/password
Changer mon mot de passe
POST
/api/v1/account/picture
Changer ma photo de profil
GET
/api/v1/notifications
Get my notifications
04. Super Admin

Gestion des Administrateurs Fleet
POST
/api/v1/admin/users/create-admin
Nommer un Administrateur
GET
/api/v1/admin/users
Lister tous les utilisateurs de l'écosystème Fleet
GET
/api/v1/admin/users/{id}
05. Admin

Statistiques et Supervision
PUT
/api/v1/admin/references/vehicle-types/{id}
Modifier un type de véhicule
DELETE
/api/v1/admin/references/vehicle-types/{id}
Supprimer un type
POST
/api/v1/admin/references/vehicle-types
Ajouter un type de véhicule
GET
/api/v1/admin/stats
Statistiques Globales (Admin)
06. Fleet Managers

Administration des entreprises (Réservé ADMIN)
GET
/api/v1/admin/managers/{id}
Détails d'un manager
PUT
/api/v1/admin/managers/{id}
Mettre à jour l'entreprise
GET
/api/v1/admin/managers
Lister tous les managers
07. Drivers

Gestion des chauffeurs (Création & Recrutement)
POST
/api/v1/fleets/{fleetId}/drivers/register
Créer un nouveau Chauffeur
POST
/api/v1/fleets/{fleetId}/drivers/recruit
POST
/api/v1/drivers/{userId}/unassign-vehicle
POST
/api/v1/drivers/{userId}/assign-vehicle
Assigner un véhicule (Smart Swap)
GET
/api/v1/drivers
GET
/api/v1/drivers/{userId}
DELETE
/api/v1/fleets/{fleetId}/drivers/{userId}
08. Vehicles

Gestion unifiée des médias
PUT
/api/v1/vehicles/{vehicleId}/media/vin
Uploader photo VIN
PUT
/api/v1/vehicles/{vehicleId}/media/registration
Uploader photo Carte Grise
PUT
/api/v1/vehicles/{vehicleId}/maintenance-parameters
Mise à jour paramètres maintenance
PUT
/api/v1/vehicles/{vehicleId}/financial-parameters
Mise à jour paramètres financiers
GET
/api/v1/vehicles
Lister les véhicules (Synchronisés)
POST
/api/v1/vehicles
Créer un véhicule
POST
/api/v1/vehicles/{vehicleId}/media/gallery
Ajouter photo galerie
GET
/api/v1/vehicles/{vehicleId}
Détails complets d'un véhicule
DELETE
/api/v1/vehicles/{vehicleId}
Supprimer un véhicule
PATCH
/api/v1/vehicles/{vehicleId}
Mise à jour partielle
GET
/api/v1/vehicles/lookup/{resource}
Récupérer des listes de référence (Proxy)
DELETE
/api/v1/vehicles/{vehicleId}/media/gallery/{imageId}
Supprimer photo galerie
09. Fleets

Gestion des flottes (Sécurisé par Propriétaire)
GET
/api/v1/fleets/{id}
Détails d'une flotte
PUT
/api/v1/fleets/{id}
Mettre à jour une flotte
DELETE
/api/v1/fleets/{id}
Supprimer une flotte
GET
/api/v1/fleets
Lister les flottes
POST
/api/v1/fleets
Créer une flotte
GET
/api/v1/fleets/{id}/stats
Statistiques de la flotte
10. Trips

Gestion des courses et télémétrie
POST
/api/v1/trips/{id}/telemetry
Envoyer un point GPS (Driver)
POST
/api/v1/trips/{id}/end
Terminer une course (Driver)
POST
/api/v1/trips/start
Démarrer une course (Driver)
GET
/api/v1/trips/{id}
Détail d'une course (Manager)
GET
/api/v1/trips/current
Récupérer ma course en cours (Driver)
12. Payments

Intégration Service Paiement
POST
/api/v1/payments/wallet
Initialiser mon wallet
POST
/api/v1/payments/simulate-debit
TEST: Simuler un paiement
POST
/api/v1/payments/recharge
Recharger mon compte
GET
/api/v1/payments/balance
Consulter mon solde
10. Références

Données statiques (Types de véhicules, etc.)
GET
/api/v1/references/vehicle-types
Lister les types de véhicules disponibles
11. Geofencing
GET
/api/v1/geofence/{type}/{id}
Récupérer une géofence (par type + id)
PUT
/api/v1/geofence/{type}/{id}
Modifier une géofence
DELETE
/api/v1/geofence/{type}/{id}
Supprimer une géofence
POST
/api/v1/geofence/zones
Créer une nouvelle géofence
GET
/api/v1/geofence
Récupérer toutes mes géofences
GET
/api/v1/geofence/polygons
Récupérer mes zones polygonales
GET
/api/v1/geofence/circles
Récupérer mes zones circulaires
GET
/api/v1/geofence/alerts
Récupérer toutes mes alertes
Schemas
Financial
Location
Maintenance
Operational
Vehicle
FleetRequest
FleetResponse
VehicleTypeRequest
VehicleTypeEntity
UpdateManagerRequest
UpdateProfileRequest
UserDetail
ChangePasswordRequest
VehicleRequest
TelemetryRequest
LocalTime
Trip
StartTripRequest
WalletExternalResponse
CircleData
GeofenceZoneDTORequest
PolygonData
GeofencePoint
GeofenceZone
DriverRegistrationRequest
Driver
RecruitDriverRequest
VehicleAssignRequest
RegisterRequest
AuthResponse
TokenRefreshRequest
LoginRequest
CreateAdminRequest
Notification
PagedResultNotification
FleetStatsResponse
GlobalStatsResponse
FleetManagerResponse
""