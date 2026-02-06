# 📋 Roadmap Finalisation - 05 Février 2026

## 🦁 Cluster Gabriel (Chef de projet Backend)
- [x] **Tâche 1 :** Réorganisation finale Swagger (Config faite).
    - [x] stats publiques pour la landing page
    - [x] mise a jour de checkhealt pour verifier la sante reelle des services
- [x] **Tâche 2 :** Test et finalisation module Monitoring.
    - [x] **2.1 :** stats publiques pour la landing page
    - [x] **2.2 :** mise a jour de checkhealt pour verifier la sante reelle des services
- [x] **Tâche 3 :** Test et finalisation module Auth.
    - [x] **3.1 :** Migration SQL : Déplacer `users` vers le schéma `fleet` et ajouter `is_active` (boolean) et `deleted_at` (timestamp).
    - [x] **3.2 :** Refactoring technique : Adapter l'entité `UserEntity` et son repository pour pointer sur `fleet.users`.
    - [x] **3.3 :** Implémentation du flux `Refresh Token` (Route + Service + Port).
    - [x] **3.4 :** Raffinement du flux `Login` : Intégrer le "Pull" (sync à chaque login) et la vérification du statut `is_active`.
    - [x] **3.5 :** Gestion des erreurs : Créer un mapper d'exceptions pour traduire proprement les codes d'erreur du service distant (401, 409, etc.).
    - [x] **3.6 :** Validation Swagger : Scénario d'inscription, login, accès bloqué (en forçant `is_active: false` en DB) et rafraîchissement de token.

- [-] **Tâche 4 :** Test et finalisation module Account.
    - [ ] **4.1 :** Agrégation du profil (`/me`) : Fusionner les données de l'Auth central avec les données locales (`fleet_managers` ou `drivers`) en un seul objet.
    - [ ] **4.2 :** Mise à jour de l'identité : Implémenter le flux de modification (Nom, Prénom, Phone) avec synchronisation bidirectionnelle (Distant <-> Local).
    - [ ] **4.3 :** Gestion de l'image : Endpoint de mise à jour de la photo de profil (traitement Multipart et propagation au service distant).
    - [ ] **4.4 :** **Soft Delete Métier** : Implémenter l'endpoint de suppression de compte qui active `deleted_at` et `is_active = false` en local.
    - [ ] **4.5 :** **Logique de Libération (Clean-up)** : Créer le service qui, lors d'un Soft Delete, désassigne automatiquement le véhicule si l'utilisateur est un chauffeur (Driver).
    - [ ] **4.6 :** Validation Swagger : Tester la modification de profil, l'upload de photo et vérifier qu'un compte supprimé est immédiatement banni par le `JwtAuthenticationManager`.


- [ ] **Tâche 5 :** Test et finalisation module Super Admin.
- [ ] **Tâche 6 :** Test et finalisation module Admin.
- [ ] **Tâche 7 :** Test et finalisation module Fleet Managers.
- [ ] **Tâche 8 :** Test et finalisation module Drivers.
- [ ] **Tâche 9 :** Test et finalisation module Vehicles (Debug PUT et ressources initiales).
- [ ] **Tâche 10 :** Test et finalisation module Fleets.
- [ ] **Tâche 11 :** Test et finalisation module Trips (Courses).

## 🦅 Cluster Hassana
- [ ] **Tâche 12 :** Finalisation module Geofence.

## 🐢 Cluster Raphaël
- [ ] **Tâche 13 :** Finalisation module Payments.