Liste des bugs restants (modules auth et account)

    Crash 500 Persistant sur /picture : Malgré l'enveloppe ByteArrayResource et le MediaType, l'envoi de la photo vers POST /api/users/{id}/picture échoue toujours avec une erreur 500 du serveur Pynfi. Il faut investiguer si une entête spécifique (comme Content-Disposition) est manquante ou si le serveur distant a un problème de buffer.

    Validation du message 403 (Post-Delete) : Vérifier que l'utilisateur reçoit bien le message "Compte désactivé, contactez un admin" lors d'une tentative de ré-inscription sur un compte supprimé (à tester quand le service Auth répondra hors 500).

    Vérification de la libération véhicule : S'assurer que lors du deleteAccount, l'appel driverUseCase.unassignVehicle met bien à jour la table fleet.vehicles en temps réel.

    Incohérence Photo au Register : Confirmer avec l'équipe du service Auth pourquoi le champ photoUri est ignoré lors de l'appel /register alors qu'il est documenté dans leur Swagger.