# Mobile (Android Capacitor)

Ce dossier contient le projet Android Capacitor qui embarque la version exportée static de l’app Next.js.

## Pré-requis

- Node.js installé (voir README racine pour les versions).
- Android Studio avec SDK 34 et outils de build installés.
- Variables d’environnement `JAVA_HOME` et `ANDROID_HOME` configurées.
- Compte Google Play (pour la publication) si vous préparez une release.

## Flux de build (debug)

1. Construire le front Next.js (depuis `client/`).
   ```powershell
   cd ..\client
   npm run build
   ```
2. Copier la sortie static dans `mobile/dist`.
   ```powershell
   cd ..\mobile
   Remove-Item -Path dist -Recurse -Force -ErrorAction SilentlyContinue
   Copy-Item -Path ..\client\out -Destination dist -Recurse
   ```
3. Synchroniser Capacitor.
   ```powershell
   npx cap sync android
   ```
4. Construire l’APK debug.
   ```powershell
   cd android
   ./gradlew assembleDebug
   ```
   L’APK est généré dans `android/app/build/outputs/apk/debug/app-debug.apk` (applicationId `com.heritier.millionnaire.debug`).

## Signature release

1. Générer un keystore (exécuter depuis `mobile/android`).
   ```powershell
   keytool -genkeypair -v -keystore C:\chemin\hm-release.keystore -alias hm-key -keyalg RSA -keysize 2048 -validity 3650
   ```
2. Renseigner les mots de passe en variables Gradle au moment du build release.
   ```powershell
   ./gradlew assembleRelease `
     -PMYAPP_UPLOAD_STORE_FILE="C:\\chemin\\hm-release.keystore" `
     -PMYAPP_UPLOAD_KEY_ALIAS="hm-key" `
     -PMYAPP_UPLOAD_STORE_PASSWORD="<storePass>" `
     -PMYAPP_UPLOAD_KEY_PASSWORD="<keyPass>"
   ```
   L’AAB/APK signés se trouvent dans `android/app/build/outputs/`.

## AdMob

- Les identifiants de test sont configurés par défaut. Pour la production, mettre à jour `initializeAds` dans `client/lib/ads.ts` et `capacitor.config.ts` avec les IDs AdMob officiels.
- Ajouter un écran de consentement RGPD si l’app est distribuée en Europe.

## Dépannage courant

- **APK refuse de s’installer** : désinstaller la version précédente ou utiliser l’APK debug suffixé `.debug`.
- **Page reste sur l’accueil** : s’assurer qu’on utilise les builds récents (navigation via `next/link` + `router.push`).
- **Pas de son** : vérifier la présence des fichiers audio dans `client/public/audio/` et la permission audio sur l’appareil.
# Application mobile (Capacitor)

Emballage Android (WebView) de l'interface web Héritier Millionnaire.

## Principe

- L'app charge l'URL du client Next.js en production (Vercel) directement dans une WebView Capacitor.
- Aucun code natif spécifique n'est requis pour le MVP.
- Coté API, ajoutez `capacitor://localhost` dans `CLIENT_ORIGIN` (Render) pour autoriser l'origine mobile.

## Pré-requis

- Node.js 18+
- Android Studio + SDK (pour build APK)

## Configuration

- Définir la variable d'environnement `MOBILE_WEB_URL` si vous voulez une URL différente (par défaut: https://client-jeux-millionnaire.vercel.app).
- Côté API (Render):
  - `CLIENT_ORIGIN=https://client-jeux-millionnaire.vercel.app,capacitor://localhost`
  - `JWT_SECRET`, `ADMIN_EMAIL`, etc. comme dans le README racine.

## Installation

Depuis la racine du projet (Windows PowerShell):

```powershell
# 1) Installer les dépendances du module mobile
cd "c:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile"
npm install

# 2) (Optionnel) définir l'URL web utilisée dans la WebView Capacitor
#    Valeur par défaut : https://client-jeux-millionnaire.vercel.app
$env:MOBILE_WEB_URL = "https://client-jeux-millionnaire.vercel.app"

# 3) Synchroniser Capacitor (génère/maj la config native)
npm run sync
```

## Ajouter la plateforme Android (première fois)

Exécuter depuis le dossier `mobile`:

```powershell
cd "c:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile"
npx cap add android
```

## Ouvrir dans Android Studio

```powershell
cd "c:\Users\smori\OneDrive\Documents\application nouvelle\jeux du Millionaire\mobile"
npm run android
```

Dans Android Studio :

- Menu Build → Build Bundle(s) / APK(s) → Build APK(s)
- L’APK se trouve ensuite dans `mobile/android/app/build/outputs/apk/` (debug ou release)

## Notes

- En prod, l'app appelle l'API Render; assurez-vous que `NEXT_PUBLIC_API_BASE` côté client pointe bien vers l'API.
- Pour tester en préproduction, ajustez `MOBILE_WEB_URL` avant la synchronisation.
- iOS est possible avec `@capacitor/ios` mais demande un Mac pour Xcode.

### CORS/API

- Ajoutez `capacitor://localhost` à la variable `CLIENT_ORIGIN` côté API (Render) pour autoriser l'origine mobile.
- Si vous utilisez des cookies (invités, CSRF), vérifiez que le serveur définit `SameSite=None; Secure` et que les requêtes fetch incluent `credentials: 'include'`.
- Alternative avancée: configurer un proxy côté Vercel pour appeler l’API via le même domaine que le client afin d’éviter les cookies tiers.
