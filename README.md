# Mon Petit Pokédex

Mon Petit Pokédex est une application Android qui permet aux utilisateurs de parcourir une liste de Pokémon, de les capturer et de les relâcher, ainsi que de consulter des informations détaillées sur chaque Pokémon.

## Fonctionnalités

1. Authentification des Utilisateurs: Les utilisateurs peuvent s'inscrire, se connecter et se déconnecter de manière sécurisée en utilisant l'authentification Firebase.
1. Liste des Pokémon: Affiche une liste de Pokémon récupérée depuis l'API PokéAPI. Chaque élément Pokémon affiche son nom, son sprite et son statut de capture.
1. Capture/Relâche de Pokémon: Les utilisateurs peuvent capturer ou relâcher des Pokémon en cliquant dessus dans la liste.
1. Détails des Pokémon: Affiche des informations détaillées sur un Pokémon sélectionné, y compris son nom, son type, ses capacités et ses statistiques.
1. Synchronisation en Arrière-plan: Met automatiquement à jour la liste des Pokémon lorsque l'application est reprise pour garantir la fraîcheur des données.
1. Design Réactif: L'application est conçue pour fonctionner sur différentes tailles d'écran et orientations.

## Technologies Utilisées

. Kotlin
. Authentification Firebase
. Firestore Firebase
. Retrofit pour les appels d'API
. RecyclerView pour l'affichage des listes
. Glide pour le chargement et la mise en cache des images

## Prise en Main

. Clonez le dépôt sur votre machine locale :

```bash
git clone https://github.com/votreutilisateur/mon-petit-pokedex.git
```

1. Ouvrez le projet dans Android Studio.

1. Configurez l'authentification Firebase et Firestore pour votre projet. Remplacez le fichier google-services.json par votre propre fichier de configuration Firebase.

1. Exécutez l'application sur un émulateur ou un appareil physique.

