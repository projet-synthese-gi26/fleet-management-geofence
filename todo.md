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

- [x] **Tâche 4 :** Test et finalisation module Account.
    - [x] **Tâche 4.1 :** **Agrégation & Synchro Profile** : Garantir un `pullSync` systématique sur le `GET /me` et enrichir l'objet avec les données métier complètes (Manager/Driver).
    - [x] **Tâche 4.2 :** **Mise à jour Identité** : Implémenter le `PUT /account` avec propagation immédiate des modifications vers le cache local `fleet.users`.
    - [x] **Tâche 4.3 :** **Soft Delete & Nettoyage** : Implémenter la suppression logique (is_active/deleted_at) et créer le service de libération automatique du véhicule pour les Chauffeurs.
    - [x] **Tâche 4.4 :** **Tests & Debug Médias** : Valider l'upload de photo (`POST /picture`) et le changement de mot de passe avec le service distant.
    - [x] **Tâche 4.5 :** **Blindage des Erreurs** : Mapper les codes d'erreur spécifiques du service Auth (401, 403, 409) vers nos `DomainException` pour des retours API clairs.



- [x] **Tâche 5 :** Test et finalisation module Super Admin.
    - [x] **Tâche 5.1 :** Création du Port & Service pour le filtrage spécifique des utilisateurs ayant le rôle `FLEET_ADMIN`.
    - [x] **Tâche 5.2 :** Implémentation du "Toggle Status" (Activer/Désactiver) modifiant la colonne `is_active` dans `fleet.users`.
    - [x] **Tâche 5.3 :** Implémentation de la suppression logique (`deleted_at`) pour les comptes Admin.
    - [x] **Tâche 5.4 :** Sécurisation et mise à jour du `AdminUserController` (Accès restreint au Super Admin).

- [x] **Tâche 6 :** Test et finalisation module Admin.
    **6.1 Gestion des Managers**
    - [x] **Tâche 6.1.1 :** Extension du service Manager pour inclure la désactivation de compte (`is_active = false`).
    - [x] **Tâche 6.1.2 :** Ajout de la suppression logique des managers.
    - [x] **Tâche 6.1.3 :** Listing global des managers avec état du compte (Actif/Suspendu).

    **6.2 Gestion des Ressources (Nouveau Tag)**
    - [x] **Tâche 6.2.1 :** Finalisation du CRUD complet pour `VehicleType` (Create, Read, Update, Delete).
    - [x] **Tâche 6.2.2 :** Réorganisation Swagger : Création du tag **"05. Admin | Gestion des Ressources"** et déplacement des endpoints de référence.
    - [x] **Tâche 6.2.3 :** Validation des contraintes (Interdire la suppression d'un type utilisé par un véhicule).

- [ ] **Tâche 7 :** Test et finalisation module Fleet Managers.
- [ ] **Tâche 8 :** Test et finalisation module Drivers.
- [ ] **Tâche 9 :** Test et finalisation module Vehicles (Debug PUT et ressources initiales).
- [ ] **Tâche 10 :** Test et finalisation module Fleets.
- [ ] **Tâche 11 :** Test et finalisation module Trips (Courses).

## 🦅 Cluster Hassana
- [ ] **Tâche 12 :** Finalisation module Geofence.

## 🐢 Cluster Raphaël
- [ ] **Tâche 13 :** Finalisation module Payments.