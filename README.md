# Sanctuary Block (mod autonome)

Mini-mod Fabric pour Minecraft 1.21.1 qui ajoute **uniquement** le Bloc Sanctuaire, réécrit à
partir de zéro (mais avec le comportement et les textures d'origine) pour fonctionner sans
Cobblemon ni aucune des autres fonctionnalités de "Legendary Monuments".

## Ce que fait le bloc

- Bloc de 2 blocs de haut, sans collision (on peut marcher à travers).
- Clic droit en survie : active/désactive la protection (son + particules + message).
- Clic droit en créatif : ouvre un écran de configuration avec :
  - Actif / Inactif
  - Protection contre les explosions
  - Blocage du placement de blocs
  - Blocage du cassage de blocs
  - Blocage du spawn de mobs
  - Rayon horizontal (1–100) et rayon vertical (1–100)
- La configuration est sauvegardée dans le monde (NBT) et survit au rechargement.
- Disponible dans l'onglet créatif "Functional Blocks". Pas de recette (comme l'original,
  c'est un bloc réservé aux admins/créatif).

## ⚠️ Important : je n'ai pas pu compiler ce projet moi-même

Je n'ai accès qu'à GitHub, npm et PyPI dans mon environnement — pas aux dépôts Maven de
Fabric/Mojang nécessaires pour télécharger Minecraft, les mappings Yarn et Fabric API. Le code
a donc été écrit avec soin à partir du bytecode décompilé du bloc original, mais **il n'a pas
été testé par une compilation réelle**.

## Comment obtenir le .jar SANS RIEN INSTALLER (recommandé si tu ne sais pas coder)

Ce projet contient un fichier `.github/workflows/build.yml` qui demande à GitHub de compiler
le mod pour toi, gratuitement, dans le cloud. Tu n'as besoin d'installer ni Java, ni Gradle,
ni aucun logiciel.

1. Va sur [github.com](https://github.com) et crée un compte gratuit si tu n'en as pas déjà un.
2. Clique sur le **+** en haut à droite → **New repository**. Donne-lui un nom (ex:
   `sanctuary-block-mod`), laisse-le en "Public", puis clique **Create repository**.
3. Sur la page du nouveau dépôt, clique sur **"uploading an existing file"** (ou **Add file →
   Upload files**).
4. Dézippe `sanctuary-block-mod.zip` sur ton ordinateur, puis **glisse-dépose tout le contenu**
   du dossier (pas le dossier lui-même, son contenu : `build.gradle`, `README.md`, `src/`,
   `.github/`, etc.) dans la zone d'upload de GitHub.

   ⚠️ **Le dossier `.github/` est un dossier caché** (à cause du point devant son nom) — sur
   Windows, active "Éléments masqués" dans l'onglet Affichage de l'Explorateur de fichiers ;
   sur Mac, appuie sur `Cmd + Maj + .` dans le Finder pour le faire apparaître. Sans ce dossier,
   la compilation automatique ne se déclenchera pas.
5. Clique **Commit changes** en bas de la page.
6. Va dans l'onglet **Actions** en haut du dépôt. Une compilation ("Build Sanctuary Block mod")
   devrait démarrer automatiquement (petit rond jaune qui tourne). Attends 2-5 minutes qu'elle
   devienne verte ✅ (si elle devient rouge ❌, clique dessus, copie-moi le message d'erreur et
   je corrigerai le code).
7. Une fois verte, clique sur cette exécution, descends jusqu'à **Artifacts**, et télécharge
   **sanctuaryblock-mod** — c'est un zip contenant le `.jar` compilé.
8. Place ce `.jar` dans le dossier `mods/` de ton instance Fabric 1.21.1 (avec **Fabric API**
   installé également).

## Compiler en local (si tu préfères, ou si tu as déjà de l'expérience)

**Prérequis :** JDK 21, une connexion internet (pour télécharger Minecraft/Fabric/Yarn/Loom).

1. Ouvre un terminal dans ce dossier.
2. Génère le wrapper Gradle (une seule fois, si tu as Gradle installé globalement) :
   ```
   gradle wrapper --gradle-version 8.8
   ```
   Sinon, ouvre simplement le dossier avec IntelliJ IDEA (avec le plugin Fabric/Gradle) qui
   gère ça automatiquement.
3. Compile :
   ```
   ./gradlew build
   ```
   (ou `gradlew.bat build` sous Windows)
4. Le fichier `.jar` final se trouve dans `build/libs/sanctuaryblock-1.0.0.jar`.
5. Place-le dans le dossier `mods/` de ton instance Fabric (1.21.1) qui a **Fabric API**
   installé (Fabric Loader + Fabric API requis, aucune autre dépendance).

## Points à vérifier / bugs potentiels

Comme le code n'a pas été compilé/testé en jeu, voici les points les plus susceptibles de
nécessiter un petit ajustement si `./gradlew build` remonte des erreurs :

- Les noms exacts de certaines méthodes Yarn (`onUse`, `onBreak`, `canPlaceAt`,
  `getOutlineShape`, etc.) peuvent varier légèrement selon le build exact des mappings Yarn
  1.21.1 utilisé — si le compilateur signale une méthode introuvable, l'IDE (IntelliJ) proposera
  généralement le bon nom via auto-complétion.
- `SanctuaryMobSpawnMixin` cible `ServerWorld#spawnEntity` : si cette signature a changé,
  ajuste le point d'injection en conséquence.
- Le rendu du bloc utilise `RenderLayer.getCutout()` et `RenderLayer.getTranslucent()` — si le
  bloc s'affiche mal en jeu (trop transparent ou avec des artefacts), essaie de ne garder qu'un
  seul des deux appels dans `SanctuaryBlockModClient`.

## Licence / origine

Le bloc, son comportement, son modèle 3D et ses textures proviennent du mod **"Cobblemon:
Legendary Monuments"** par JorgaoMC, distribué sous licence **Mozilla Public License 2.0**.
Crédit modèle/texture original : *SpookyThunder / AstraSilverhorn*. Ce mod n'est ni affilié ni
approuvé par l'auteur original — c'est une extraction/réimplémentation minimaliste à usage
personnel d'une seule fonctionnalité du mod.
