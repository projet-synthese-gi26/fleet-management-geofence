# 📋 Roadmap Répartie : API Fleet & Geofence
**Version :** 3.0
**Date de génération :** 27 Janvier 2026
**Statut :** En cours de planification

---

## ⚠️ Règles Générales
*   **Documentation :** Chacun doit lire le `README.md` et le suivre à la lettre avant de travailler pour garantir la productivité. En cas de question, se rapprocher de **Nomo Gabriel**.
*   **Tests :** Chacun doit tester son travail **en local** et vérifier la compatibilité **en production** (staging).
*   **Gestion de code (Git) :**
    *   **Interdiction formelle de push sur `main`**. Il faut impérativement passer par une **Pull Request**.
    *   Seul **Nomo Gabriel** a le droit de valider/merger sur `main`.
    *   **Hassana** travaille en collaboration avec Gabriel sur la branche `master`/`main`.
    *   **Bihai Raphaël** crée une branche `raphael` sur laquelle il travaille. Nous ferons un merge global plus tard.
*   **Base de Données :** Pour toute modification de la BD (schéma), il est impératif de le **signaler dans le groupe** pour éviter de casser le travail des autres.
*   **Délais :** Toutes les tâches doivent être livrées **au plus tard demain matin à 10h00**, lors de la réunion de l'équipe backend.
*   **Configuration :** Toujours mettre à jour le fichier `prod.application.yml` lorsqu'une modification est apportée au `application.yml` local.
*   **Architecture :** Respecter scrupuleusement l'architecture **hexagonale** du projet.

---

## ✅ PHASE 1 : FONDATIONS (Terminé)
*(Rappel pour contexte)*
- [x] Infrastructure (Docker, Liquibase, R2DBC).
- [x] Base de données (Schéma `fleet` & `public`, Seeding).
- [x] Auth & Sécurité (Jwt, Proxy Auth Service, Mode Fake).
- [x] Gestion Fleet Managers (CRUD & Synchro).
- [x] Gestion Flottes (CRUD).
- [x] Gestion Drivers (Implémentation faite, tests restants).

---

## 🏗️ PHASE 2 : PARALLÉLISATION (En cours)

### 🦁 Cluster 1 : Gabriel (Core Business & Orchestration)
*Responsabilité : Logique complexe, agrégation de données, machines à états, validation finale.*

#### Module 1 : Finalisation Auth & Drivers
- [x] **Tâche 1.1 :** Validation Tests en local : Gestion des Drivers (Création, Listing, Retrait).
- [x] **Tâche 1.2 :** Implémentation : Gestion des Admins et Super Admins (1 Super Admin défini en dur dans le code/configuration qui peut créer les autres admins ; il sera configuré dans le `application.yml`).
- [ ] **Tâche 1.3 :** Implémentation : Suppression de compte (Changement de service vers le service `User_Deleted`).
- [ ] **Tâche 1.4 :** Implémentation : Upload/Update de la photo de profil (User, Driver, Fleet Manager, Admin).
- [ ] **Tâche 1.5 :** Test et Implémentation : Gestion des assignations chauffeur-vehicules,flottes-chauffeurs,managers-flottes


#### Module 2 : Gestion Avancée des Véhicules
- [x] **Tâche 2.1 :** Refonte `VehicleService` : Agrégation réactive robuste (Données locales + Données distantes `VehicleApiClient`).au minimum le CRUD
- [x] **Tâche 2.2 :** gestion des medias des vehicles 
- [] **Tâche 2.3 :** CRUD Paramètres Financiers (Relation 1-1).
- [] **Tâche 2.4 :** CRUD Paramètres Maintenance (Relation 1-1).
- [] **Tâche 2.5 :** Logique d'Assignation Croisée (Empêcher conflit : 1 Driver sur 2 véhicules, etc.).
- [] **Tâche 2.6 :** debogage de la methode put des vehicules

#### Module 3 : Gestion des Trajets (Trips) & Télémétrie
- [x] **Tâche 3.1 :** Endpoint `Start Trip` : Initialisation, verrouillage véhicule/driver.
- [x] **Tâche 3.2 :** Endpoint `Telemetry` : Ingestion haute fréquence des points GPS (WebFlux Stream).
- [x] **Tâche 3.3 :** Endpoint `End Trip` : Clôture, calcul distance/durée, libération ressources.
- [x] **Tâche 3.4 :** Identification d'autres endpoints si nécessaire et implémentation.

---

### 🦅 Cluster 2 : Hassana (Geofence & Event-Driven)
*Responsabilité : Moteur spatial, Asynchronisme, Alerting.*

#### Module 4 : Moteur Geofencing
- [ ] **Tâche 4.1 :** CRUD Zones : Comprendre l'API de Kamga, identifier tous les endpoints nécessaires pour notre projet et les intégrer.

#### Module 5 : Notifications & Kafka
- [ ] **Tâche 5.1 :** Analyse du service : Comprendre comment fonctionne le service de notifs (lire la doc et se rapprocher de Tchassi). Consulter également le code d'Igor, qui a déjà intégré ce service avec succès.
- [ ] **Tâche 5.2 :** Configuration Kafka : Configurer le nécessaire pour ce projet et offrir les endpoints pour recevoir les notifications côté client.
- [ ] **Tâche 5.3 :** Workflow Frontend : Discuter avec l'équipe frontend pour identifier **quand** et **à qui** envoyer des notifications en fonction de chaque action. Faire des recherches pour suggérer des améliorations, puis intégrer le tout.

---

### 🐢 Cluster 3 : Raphaël (Support, Admin & Intégrations Satellites)
*Responsabilité : Modules isolés, Dashboarding, Intégrations tiers.*

#### Module 6 : Administration & Statistiques
- [ ] **Tâche 6.1 :** Endpoints Stats Globales (Super Admin) : Nombre total de flottes, véhicules, managers, etc. (Identifier une liste de stats pertinentes).
    *   *Note : Faire un Controller et un Service dédiés à cette tâche.*
- [ ] **Tâche 6.2 :** Endpoints Stats Flotte (Fleet Manager) : Km parcourus par flotte, taux d'occupation des véhicules, nombre de chauffeurs, nombre de km parcourus par chauffeur, etc.

#### Module 7 : Paiements
- [ ] **Tâche 8.1 :** Test et compréhension du service : Se rapprocher de Nathan et comprendre le fonctionnement du service paiement.
- [ ] **Tâche 8.2 :** Intégration : Créer les endpoints de base pour consommer le service dans notre projet.
