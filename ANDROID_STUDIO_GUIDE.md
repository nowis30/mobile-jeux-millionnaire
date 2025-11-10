# Guide Android Studio (Windows)

Ce guide vous accompagne pas à pas pour ouvrir, exécuter et construire l’APK Android de l’app mobile (Capacitor) Héritier Millionnaire.

## 1) Préparatifs

- Installez Android Studio (version récente) depuis developer.android.com/studio
- Ouvrez Android Studio une fois installé, et laissez-le télécharger les composants SDK recommandés.
- Assurez-vous que vous avez déjà synchronisé le projet Capacitor côté JS:
  - Dans PowerShell, placez-vous dans `mobile/` et exécutez `npm install`, puis `npm run sync`.
  - La plateforme Android doit être ajoutée au moins une fois avec `npx cap add android`.

## 2) Ouvrir le projet Android

- Depuis `mobile/`, lancez `npm run android` (ouvre Android Studio sur le sous-projet `mobile/android`).
- Si vous ouvrez manuellement : Android Studio → Open → sélectionnez le dossier `mobile/android`.
- Au premier lancement, Android Studio fera un « Gradle Sync ». Laissez-le terminer.

### Problèmes fréquents lors du Sync

- SDK non trouvé: Android Studio → File → Settings → Appearance & Behavior → System Settings → Android SDK → installez les SDK Platforms (API 34/35) et SDK Tools requis.
- JDK: Android Studio embarque un JDK; sinon vérifiez File → Settings → Build Tools → Gradle → Gradle JDK (choisissez l’Embedded JDK ou JDK 17).
- Proxy/réseau: vérifiez que les dépôts Maven sont accessibles (pas de VPN bloquant).

## 3) Lancer l’app en debug (émulateur ou appareil)

- Émulateur: Android Studio → Device Manager → Create Device → choisissez un Pixel + une image système (API 34/35) → Download/Next/Finish.
- Appareil réel: Activez le "Débogage USB" (Options développeur) et connectez l’appareil via USB (acceptez l’empreinte).
- Dans la barre du haut, choisissez l’appareil (émulateur ou téléphone), puis cliquez sur ▶ (Run). Android Studio construit et installe l’app.

Astuce si vous ne voyez pas “Rebuild Project”
- Selon la version d’Android Studio, le menu peut ne pas afficher « Rebuild Project ».
- Utilisez alors l’une de ces alternatives équivalentes:
  1) Build → Clean Project, puis Build → Build Bundle(s) / APK(s) → Build APK(s)
  2) Onglet Gradle (barre droite) → app → Tasks → build → assembleDebug (ou assembleRelease)
  3) Bouton « Sync Now » (bannière bleue) pour resynchroniser, puis bouton ▶ Run
  4) Build → Make Project (équivalent à une compilation complète du module)

## 4) Construire un APK debug rapidement

- Menu: Build → Build Bundle(s) / APK(s) → Build APK(s)
- Une fois terminé: cliquez sur "locate" pour ouvrir le dossier de sortie.
- Chemin attendu: `mobile/android/app/build/outputs/apk/` (debug ou release).

## 5) Générer un APK signé (release)

1. Créer/choisir un keystore:
   - Build → Generate Signed Bundle / APK
   - Choisir "APK" → Next
   - Key store path → Create new → définissez un chemin sécurisé (ex: `C:/Users/vous/keystores/hm-release.keystore`)
   - Renseignez Key alias + mot de passe → Notez ces informations (à conserver en lieu sûr).
2. Sélectionner "release" → Next → Finish
3. Une fois la construction terminée, récupérez l’APK release dans `app/build/outputs/apk/release/`.

Conseils:
- Conservez le keystore et ses mots de passe. Sans eux, vous ne pourrez plus mettre à jour l’app sur Play Store.
- Vous pouvez aussi produire un Android App Bundle (AAB) pour Play Store (préféré par Google).

## 6) Publication (aperçu rapide)

- Créez un compte Google Play Console (payant, unique) si ce n’est pas déjà fait.
- Créez une application, remplissez la fiche magasin (icônes, captures, description, politique confidentialité, classification contenu, etc.).
- Téléversez un AAB ou APK signé sur une piste (interne/fermée/ouverte), puis soumettez pour révision.

## 7) Particularités Capacitor

- L’app charge l’URL du client web (voir `capacitor.config.ts`, variable `MOBILE_WEB_URL`).
- Côté API (Render), autorisez l’origine `capacitor://localhost` dans `CLIENT_ORIGIN`.
- Si vous utilisez des cookies (invités/CSRF), assurez-vous que les attributs de cookie conviennent (SameSite=None; Secure) et que les fetch incluent `credentials: 'include'`.

## 8) Dépannage rapide

- Écran blanc au lancement: vérifiez l’URL configurée et l’accessibilité réseau de l’API/du client web.
- Erreur CORS/auth: ajustez `CLIENT_ORIGIN` côté API et/ou la politique de cookies.
- Build Gradle échoue: effacez `.gradle`/`build`, puis File → Sync Project with Gradle Files; vérifiez versions du Android Gradle Plugin.
- Emulateur lent: activez la virtualisation matérielle (BIOS), utilisez une image x86_64/ARM correspondante.

---

Besoin d’aide pas à pas (partage d’écran)? On peut faire la première génération d’APK ensemble: j’aurai besoin que vous ouvriez Android Studio, installiez les SDK, puis on suit les étapes ci-dessus.

## 9) Dépannage connexion ("le jeu ne se connecte plus")

Quand l’app s’ouvre mais n’arrive plus à parler à l’API, suivez ces vérifications rapides:

1) URL chargée par l’app
- Le fichier `capacitor.config.ts` définit `server.url` via la variable d’env `MOBILE_WEB_URL` (sinon valeur par défaut).
- Si vous changez d’URL, exportez la variable puis synchronisez:

  ```powershell
  # Dans le dossier mobile/
  $env:MOBILE_WEB_URL = "https://ton-client-web.example.com"
  npm run sync
  ```

  Cela met à jour `android/app/src/main/assets/capacitor.config.json` utilisé par l’app.

2) CORS et cookies côté API
- L’API doit autoriser exactement l’origine du client web (ex: `https://client-jeux-millionnaire.vercel.app`).
- Si l’app est servie à distance (server.url https), l’origine N’EST PAS `capacitor://localhost` mais l’URL https du client.
- Pour une auth par cookies cross-site, vérifiez:
  - Set-Cookie avec `SameSite=None; Secure`
  - Réponses CORS avec `Access-Control-Allow-Credentials: true` et `Access-Control-Allow-Origin` sur l’URL exacte (pas `*`).
  - Les `fetch` côté client utilisent `credentials: 'include'`.

3) HTTP vs HTTPS
- Si votre API est en HTTP (non recommandé), Android 9+ bloque le clair par défaut.
- Solutions:
  - Passer l’API en HTTPS (préféré), ou
  - Temporairement activer le clair: dans `capacitor.config.ts`, `server.cleartext: true` et dans le Manifest `<application android:usesCleartextTraffic="true" />`. Pensez à revenir en HTTPS avant production.

4) OneDrive et fichiers verrouillés
- Si le projet est dans `OneDrive\Documents`, OneDrive peut verrouiller des fichiers pendant le build.
- En cas d’erreurs `Unable to delete directory`:
  - Fermez Android Studio, exécutez `gradlew --stop`, supprimez les dossiers `android/build`, `android/app/build` et `node_modules/@capacitor/android/capacitor/build`, puis relancez le build.
  - Idéal: déplacer le projet hors OneDrive (ex: `C:\dev\jeux-millionnaire`).

5) Inspecter le WebView (logs réseau)
- Branchez l’appareil en USB, ouvrez Chrome sur le PC: `chrome://inspect/#devices`.
- Cliquez sur le WebView de l’app → onglet Network: vérifiez les requêtes (codes, CORS, cookies) et la Console (erreurs TLS/DNS).

Astuce: pour des changements de config (URL, cleartext), refaites toujours `npm run sync` avant de reconstruire.

## 10) (Option) Signature release par Gradle (sans assistant)

Vous pouvez signer la release en ligne de commande sans ouvrir l’assistant, grâce à la config conditionnelle ajoutée dans `app/build.gradle`.

1) Créez un keystore si besoin (Android Studio → Generate Signed Bundle/APK → Create new) et notez:
   - Chemin du keystore (ex: `C:/Users/vous/keystores/hm-release.keystore`)
   - Key alias (ex: `hm-key`)
   - Store password et Key password

2) Construisez en renseignant les propriétés en ligne (PowerShell, dossier `mobile/android`):

```powershell
.\u0067radlew.bat assembleRelease -PMYAPP_UPLOAD_STORE_FILE="C:\Users\vous\keystores\hm-release.keystore" `
  -PMYAPP_UPLOAD_KEY_ALIAS="hm-key" -PMYAPP_UPLOAD_STORE_PASSWORD="<storePass>" -PMYAPP_UPLOAD_KEY_PASSWORD="<keyPass>"
```

3) Récupérez la sortie signée:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab` (utilisez `bundleRelease` à la place d’`assembleRelease`)

Note: Les mots de passe ne doivent pas être commités. Préférez les passer avec `-P...` en ligne de commande ou via des variables d’environnement sécurisées.

## Annexe A) Nettoyage rapide PowerShell (verrous OneDrive)

Si vous voyez dans Android Studio des erreurs du type « Unable to delete directory … .transforms … » (souvent liées à OneDrive qui verrouille des fichiers), vous pouvez nettoyer rapidement depuis PowerShell, puis relancer la compilation:

```powershell
# 1) Arrêter Gradle pour libérer les verrous
& "C:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile\android\gradlew.bat" --stop

# 2) Supprimer les dossiers de build susceptibles d'être verrouillés
Remove-Item -LiteralPath 'C:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile\android\app\build' -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath 'C:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile\android\build' -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath 'C:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile\android\.gradle' -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath 'C:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile\node_modules\@capacitor\android\capacitor\build' -Recurse -Force -ErrorAction SilentlyContinue

# 3) Recompiler un APK debug propre (option: -x lint pour aller plus vite)
& "C:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile\android\gradlew.bat" assembleDebug -x lint

# L'APK généré se trouve typiquement ici:
# C:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile\android\app\build\outputs\apk\debug\app-debug.apk
```

Astuce: pour éviter la récidive, déplacez idéalement le projet hors d’un dossier OneDrive (ex: `C:\dev\jeux-millionnaire`) ou excluez ce dossier de la synchronisation OneDrive.
