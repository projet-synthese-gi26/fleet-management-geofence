# 📋 Roadmap Globale : API Fleet & Geofence

## 🏗️ PHASE 1 : FONDATIONS & INFRASTRUCTURE (Terminé)

#### Jalon 1 : Initialisation & Persistence `Gabriel`
- [x] **Tâche 1.1 :** Nettoyage et Refactoring du template (Renommage packages, suppression de la logique "Product" initiale).
- [x] **Tâche 1.2 :** Implémentation du Schéma de données (Traduction du contrat v1.0 en SQL).
- [x] **Tâche 1.3 :** Mécanisme de Seeding (Données de test).
- [x] **Tâche 1.4 :** Validation Swagger & Health Check.
- [x] **Tâche 1.5 :** Guide d'installation DB locale.
- [x] **Tâche 1.6 :** Intégration **Liquibase** (Multi-schémas `fleet`/`public`, migration JDBC au boot).
- [x] **Tâche 1.7 :** Infrastructure **Docker Locale Complète** (Postgres + Redis + Kafka KRaft).
- [x] **Tâche 1.8 :** Scripts d'automatisation Cross-Platform (`run_local.sh`, `run_local.bat`).

#### Jalon 2 : Authentification & Accès (Base) `Hassana`
- [x] **Tâche 2.1 :** Configuration de la Sécurité Réactive (Spring Security).
- [x] **Tâche 2.2 :** Intégration du Service d'Authentification externe (Adaptateur WebClient).
- [x] **Tâche 2.3 :** Implémentation du **Mode Dégradé (Fake Auth)** pour le développement local.
- [x] **Tâche 2.4 :** Validation Postman basique (Login/Token).

---

## 🛠️ PHASE 2 : REFACTORING & CONSOLIDATION (En cours) `Gabriel`

*Cette phase vise à stabiliser le socle technique, nettoyer le code inutile, et aligner parfaitement notre DB et nos Services avec (1) l'API d'Auth distante et (2) les besoins métier réels.*

#### Jalon 3 : Grand Nettoyage (Clean Code) `Gabriel`
- [x]**Tâche 3.1 :** Supprimer tout le code lié à "Product" (Controller, Service, Entity, Mapper, Repository). C'est du code exemple qui pollue.
- [x] **Tâche 3.2 :** Nettoyer les imports inutilisés et les dépendances mortes dans le `pom.xml`.

#### Jalon 4 : Analyse & Conformité DB `Gabriel`
- [x] **Tâche 4.1 :** Comparer le schéma Liquibase actuel (`fleet.*`) avec les sepcifications et la modelisation initiale et verifier les contracts d'api du frontend
- [x] **Tâche 4.2 :** Mettre à jour les scripts Liquibase  pour combler les manques.

#### Jalon 5 : Auth & User Management Avancé (Proxy Auth) `Gabriel`
*Objectif : Le microservice Fleet doit exposer les fonctionnalités de l'Auth Service de manière transparente.*
- [x] **Tâche 5.1 :** Refactoring `AuthApiClient` : Supporter le `MultipartFile` pour l'upload de photo de profil au `register`.
- [x] **Tâche 5.2 :** Logique "Auto-Role" : Vérifier/Créer les rôles (`FLEET_MANAGER`, `DRIVER`) sur l'Auth Service avant inscription.
- [x] **Tâche 5.3 :** Fixer le `register` : Synchro création Auth Service -> Création données locales (`fleet.drivers`).
- [x] **Tâche 5.4 :** Endpoint `User` : Créer un `UserController` local (Proxy vers Auth Service : Profil, Password).

#### Jalon 6 : Gestion des Acteurs (Managers & Drivers) `Gabriel`
- [ ] **Tâche 6.1 :** Implémenter la gestion des **Fleet Managers**.
- [ ] **Tâche 6.2 :** Refondre la gestion des **Drivers** (User Auth + Permis Local + Assignation Véhicule).

#### Jalon 7 : Flottes & Véhicules (Le Cœur) `Gabriel`
- [ ] **Tâche 7.1 :** CRUD **Flottes** (Lien avec le Fleet Manager connecté).
- [ ] **Tâche 7.2 :** CRUD **Véhicules** (Agrégation Données Locales + API Véhicule Distante).

#### Jalon 8 : Gestion des Trajets (Trips) `Gabriel`
- [ ] **Tâche 8.1 :** Start Trip / End Trip.

---

## 🚀 PHASE 3 : FONCTIONNALITÉS AVANCÉES (À venir)

#### Jalon 9 : Moteur de Geofencing & Alertes (CU10-CU13) `Hassana`
- [ ] **Tâche 9.1 :** Use-Case : Définir et gérer les zones (Geofence Zones).
- [ ] **Tâche 9.2 :** Moteur de détection réactif (Intersection position / zone).
- [ ] **Tâche 9.3 :** Publication des alertes dans Kafka.
- [ ] **Tâche 9.4 :** Validation Postman : Alertes géographiques.

#### Jalon 10 : Intégrations Services Périphériques `Gabriel`
- [ ] **Tâche 10.1 :** Adaptateurs pour Fare Calculator & Payment (avec mode Fake data).
- [ ] **Tâche 10.2 :** Adaptateur pour l'API Media (Gestion des images).
- [ ] **Tâche 10.3 :** Intégration finale du service de Notification.
- [ ] **Tâche 10.4 :** Tests de bout en bout (E2E).