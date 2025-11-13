# ✅ Correction : Connexion réseau sur Android

**Date :** 13 novembre 2025  
**Problème :** L'application Android ne pouvait pas se connecter au serveur `server-jeux-millionnaire.onrender.com`

## 🔧 Modifications apportées

### 1. Configuration Capacitor (`capacitor.config.ts`)
```typescript
server: {
  cleartext: true,
  androidScheme: 'https'
},
android: {
  allowMixedContent: true
}
```

### 2. AndroidManifest.xml
- Changé `android:usesCleartextTraffic` de `false` à `true`
- Conservé `android:networkSecurityConfig="@xml/network_security_config"`

### 3. Network Security Config (`network_security_config.xml`)
Ajouté :
- Certificats système ET utilisateur (`<certificates src="user" />`)
- Domaines autorisés :
  - `server-jeux-millionnaire.onrender.com` (HTTPS)
  - `nowis.store` (HTTPS)
  - `localhost`, `127.0.0.1`, `10.0.2.2` (HTTP, dev uniquement)

## 📱 Prochaines étapes

1. **Ouvrir Android Studio :**
   ```bash
   npx cap open android
   ```

2. **Sync Gradle :**
   - Dans Android Studio : `File > Sync Project with Gradle Files`

3. **Tester :**
   - Build et lancer l'app sur un appareil/émulateur
   - Vérifier que la connexion au serveur fonctionne
   - Tester les fonctionnalités drag, quiz, immobilier, etc.

4. **Build release APK :**
   ```bash
   cd android
   .\gradlew assembleRelease
   ```
   L'APK sera dans : `android/app/build/outputs/apk/release/app-release.apk`

## 🔍 Debug si problème persiste

Si la connexion ne fonctionne toujours pas :

1. **Vérifier les logs Android :**
   - Dans Android Studio : View > Tool Windows > Logcat
   - Filtrer par "network", "SSL", "fetch", ou "drag"

2. **Tester directement le serveur :**
   ```javascript
   // Dans la console Chrome Remote Debugging
   fetch('https://server-jeux-millionnaire.onrender.com/api/auth/csrf')
     .then(r => r.json())
     .then(console.log)
   ```

3. **Vérifier que le serveur est en ligne :**
   - Ouvrir `https://server-jeux-millionnaire.onrender.com` dans un navigateur

## 📝 Commits créés

- `42e042b` - fix: autoriser connexions réseau vers serveur Render dans Android
- `435dd6b` - chore: mise à jour dist avec nouvel export Next.js

Tous les changements ont été poussés vers GitHub.
