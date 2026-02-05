# 🤖 Master Prompt : Senior Pair Programmer WebFlux

Tu es mon Senior Pair Programmer expert en Java **Spring Boot WebFlux (Réactif)**.
Nous développons l'API **Fleet Management et Geofencing** (Projet TraEnSys).

### 📋 Ta Méthode de Travail (IMPÉRATIF)
Pour chaque tâche demandée, tu dois obligatoirement suivre ces étapes :

**Étape 1 : Conception fonctionnelle**
- Analyse du besoin, user stories et ajustement du modèle de données.Discuter avec moi de cette conception
- **Attente de ma validation explicite avant d'aller plus loin.**

**Étape 2 : Discussion Technique**
- Avant de coder, explique brièvement comment l'architechture sera gérée pour cette tâche.ne pas hesiter a dire les fichiers qui entrent en jeu,leur role et ce qu'on y ferra.pose moi les questions si a certains endroits tu as des doutes ou si tu as besoin de clarification,pas d'initiatives sans me consulter,pas de code mock,toujours me demander comment faire,car je veux faire une api robuste.c'est une phase de discussion
- **Attente de ma validation explicite avant d'aller plus loin.**

**Étape 3 : Implémentation**
- Fournis le code complet par blocs Markdown copiables.
- Respecte l'architecture hexagonale du projet.
-respecte egalement mes consignes

**Étape 4 : Tests & Validation**
- Instructions pour tester via swagger .

### 🚫 Tes Règles de Conduite
1. **Zéro code non sollicité** : Ne propose aucune solution technique avant l'Étape 3.
2. **Focus** : Réponds uniquement à la question posée, de manière synthétique et précise.
3. **Fichiers complets** : Sauf mention contraire, donne toujours le code complet du fichier pour éviter les erreurs de copier-coller.
4. **Pédagogie** : Si une opération risque de bloquer un thread (ex: JDBC classique, thread sleep), arrête-moi et propose l'alternative non-bloquante.

### 📂 Contexte
Le code source complet est disponible dans le fichier `project_context.txt`.
La roadmap est suivie dans `todo.md`.


### Premiere mission
je viens de merger mon travail ,moi gabriel avec celui de mes colalborateurs,la'pi est fonctionnelle,deployee. maisntenant on doit finaliser.chacun connait deja ses taches donc on va juste mettre dans la todo les taches sous la forme,finaliser le module:
moi en temos que chef du proejt backend,j'ai la plus grosse tache.
-on va d;abord mettre a jour la todo,ensuite on va se oncentrer sur les modules qui me concernent

objectifs:
- reorganiser l'odre des modules du swagger pour plus de clarte et de logique(moi)
-finalisation gestion du geofence: hassana
-finalisation gestion des paiements: raphael
-teste et finalisations des autres modules: gabriel(monitoring,auth,account,superamdnins,admins,managers,drivers,vehicles,flottes,courses) pour chaque module il faudra teste et completer.

ton role est de ma'ccampagner dans ce processus,ta premeire mission est de ma;ider a proposer une todo en md,datee d'ajour'dui selon le modele de l'ancien sausf que ici il n'yaura pas le detail des taches des autres,chacun sait quoi faire,peut etre juste tester et finaliser partout
"
toute fosi tu m'aides d'abord a rfesourdre les soucis que le merge a cause et a lancer l'applicorrectement:" git stash push -m "backup avant checkout raphael"
No local changes to save
gabriel@gabriel-pc:~/Documents/projects/ecole/5GI/projet-synthese/fleet-management/code$ git checkout -b raphael origin/raphael
Branch 'raphael' set up to track remote branch 'raphael' from 'origin'.
Switched to a new branch 'raphael'
gabriel@gabriel-pc:~/Documents/projects/ecole/5GI/projet-synthese/fleet-management/code$ # Passer sur main
git checkout main

# S'assurer que main est à jour
git reset --hard origin/main

# Merger Raphael
git merge raphael
Switched to branch 'main'
Your branch is up to date with 'origin/main'.
HEAD is now at b76d587 resolution des conflits de merge sur le geofence, vehicules et paiements
Auto-merging src/main/java/com/yowyob/fleet/infrastructure/config/SecurityConfig.java
CONFLICT (content): Merge conflict in src/main/java/com/yowyob/fleet/infrastructure/config/SecurityConfig.java
Auto-merging src/main/java/com/yowyob/fleet/infrastructure/config/WebClientConfig.java
CONFLICT (content): Merge conflict in src/main/java/com/yowyob/fleet/infrastructure/config/WebClientConfig.java
Automatic merge failed; fix conflicts and then commit the result.
gabriel@gabriel-pc:~/Documents/projects/ecole/5GI/projet-synthese/fleet-management/code$ git status
On branch main
Your branch is up to date with 'origin/main'.

You have unmerged paths.
  (fix conflicts and run "git commit")
  (use "git merge --abort" to abort the merge)

Changes to be committed:
        modified:   import_context_window.ps1
        new file:   project_context_2.txt
        new file:   project_context_3.txt
        new file:   project_context_4.txt
        new file:   project_context_5.txt
        modified:   src/main/java/com/yowyob/fleet/domain/ports/out/ExternalPaymentPort.java
        new file:   src/main/java/com/yowyob/fleet/infrastructure/adapters/inbound/rest/PaymentController.java
        deleted:    src/main/java/com/yowyob/fleet/infrastructure/adapters/inbound/rest/TestPaymentController.java
        modified:   src/main/java/com/yowyob/fleet/infrastructure/adapters/outbound/external/PaymentApiAdapter.java
        deleted:    src/main/java/com/yowyob/fleet/infrastructure/adapters/outbound/external/client/PaymentApiClient.java
        new file:   src/main/java/com/yowyob/fleet/infrastructure/adapters/outbound/external/dto/TransactionExternalRequest.java
        new file:   src/main/java/com/yowyob/fleet/infrastructure/adapters/outbound/external/dto/TransactionExternalResponse.java

Unmerged paths:
  (use "git add <file>..." to mark resolution)
        both modified:   src/main/java/com/yowyob/fleet/infrastructure/config/SecurityConfig.java
        both modified:   src/main/java/com/yowyob/fleet/infrastructure/config/WebClientConfig.java

gabriel@gabriel-pc:~/Documents/projects/ecole/5GI/projet-synthese/fleet-management/code$ " j'ai fais un premier essaide correction mais dans webclientconig ca ne egrre pas,il y'a le rouge,payment apiclientnot found