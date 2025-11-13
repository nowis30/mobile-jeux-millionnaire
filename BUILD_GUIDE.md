# Guide de Build - Version 1.0.3 (Android 15 Compatible)

## 🚀 Build APK/AAB pour Google Play Console

### Prérequis
- ✅ JDK 21 installé
- ✅ Android SDK 35 installé
- ✅ Fichier keystore configuré (`gradle.properties`)
- ✅ Modifications Android 15 appliquées

---

## 📦 Option 1: Build AAB (Recommandé pour Play Store)

### 1. Build Release AAB
```bash
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat bundleRelease
```

### 2. Localisation du fichier
```
mobile/android/app/build/outputs/bundle/release/app-release.aab
```

### 3. Vérification
```bash
# Taille fichier
dir app\build\outputs\bundle\release\app-release.aab

# Signature
jarsigner -verify -verbose -certs app\build\outputs\bundle\release\app-release.aab
```

---

## 📱 Option 2: Build APK (Test local)

### 1. Build Release APK
```bash
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat assembleRelease
```

### 2. Localisation du fichier
```
mobile/android/app/build/outputs/apk/release/app-release.apk
```

### 3. Installation sur appareil
```bash
# Connecter appareil en USB avec debug activé
adb devices

# Installer APK
adb install -r app\build\outputs\apk\release\app-release.apk
```

---

## 🧹 Nettoyage Avant Build

### Clean Build
```bash
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat clean
```

### Full Clean (si problèmes)
```bash
# Supprimer .gradle et build
Remove-Item -Recurse -Force .gradle, app\build, build

# Rebuild complet
.\gradlew.bat clean bundleRelease
```

---

## 🔐 Configuration Signature (gradle.properties)

Créer/vérifier `mobile/android/gradle.properties`:

```properties
MYAPP_UPLOAD_STORE_FILE=../../../key-android.jks
MYAPP_UPLOAD_KEY_ALIAS=upload
MYAPP_UPLOAD_STORE_PASSWORD=YOUR_STORE_PASSWORD
MYAPP_UPLOAD_KEY_PASSWORD=YOUR_KEY_PASSWORD
```

⚠️ **Important**: Ne JAMAIS committer ce fichier dans git !

---

## ✅ Checklist Avant Upload Play Store

### Build
- [ ] versionCode = 4
- [ ] versionName = "1.0.3"
- [ ] targetSdkVersion = 35
- [ ] Build réussi sans erreurs
- [ ] AAB signé correctement

### Code
- [ ] EdgeToEdge.enable() présent
- [ ] androidx.activity dépendance ajoutée
- [ ] styles.xml mis à jour
- [ ] resizeableActivity="true" dans manifest

### Tests
- [ ] APK installé et lancé sur appareil physique
- [ ] Test rotation portrait/paysage
- [ ] Test affichage status bar/navigation bar
- [ ] Pas de crash au lancement
- [ ] AdMob fonctionne (si applicable)

---

## 📤 Upload Google Play Console

### 1. Connexion
https://play.google.com/console

### 2. Navigation
1. Sélectionner l'application "Heritier Millionnaire"
2. Release → Production
3. Create new release

### 3. Upload AAB
1. Drag & drop `app-release.aab`
2. Release name: `1.0.3`
3. Release notes (français):

```
Version 1.0.3 - Compatibilité Android 15

✅ Support complet Android 15 (API 35)
✅ Affichage edge-to-edge moderne
✅ Compatibilité améliorée tablettes et grands écrans
✅ Support multi-fenêtres et appareils pliables
✅ Optimisations de performance
✅ Corrections de bugs mineurs
```

### 4. Review
1. Vérifier que les 3 warnings Play Console ont disparu
2. Enregistrer le brouillon
3. Review → Start rollout to production

---

## 🧪 Tests Post-Upload

### Test Internal/Closed Track (Optionnel)
```bash
# Upload sur track interne d'abord
Release → Internal testing → Create release
```

### Validation Google
- ⏳ Durée: 24-48h généralement
- 📧 Email de confirmation
- ⚠️ Possibles demandes de modifications

---

## 🐛 Troubleshooting

### Erreur: "compileSdk 35 not supported"
**Solution**: Ignorer warning ou ajouter dans `gradle.properties`:
```properties
android.suppressUnsupportedCompileSdk=35
```

### Erreur: "Java 8 deprecated"
**Solution**: Temporaire - ignorer. Future: mise à jour Java 17.

### Build échoue: "Plugin not found"
**Solution**:
```bash
.\gradlew.bat --refresh-dependencies
```

### Signature invalide
**Solution**: Vérifier chemins dans `gradle.properties`:
```bash
# Tester chemin keystore
Test-Path "../../../key-android.jks"
```

---

## 📊 Tailles Attendues

| Fichier | Taille Typique | Max Play Store |
|---------|----------------|----------------|
| AAB | 5-15 MB | 150 MB |
| APK Universal | 8-20 MB | 150 MB |
| APK arm64-v8a | 6-12 MB | - |

---

## 🔄 Workflow Complet

```bash
# 1. Clean
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat clean

# 2. Build AAB
.\gradlew.bat bundleRelease

# 3. Vérifier signature
jarsigner -verify -verbose -certs app\build\outputs\bundle\release\app-release.aab

# 4. Copier AAB vers dossier upload
Copy-Item app\build\outputs\bundle\release\app-release.aab -Destination ..\..\releases\

# 5. Upload sur Play Console (manuel)
# https://play.google.com/console
```

---

## 📝 Notes de Version

### Changements Techniques (Internal)
- Implementation EdgeToEdge.enable() pour Android 15
- Migration styles.xml vers APIs modernes
- Activation resizeableActivity pour multi-window
- Suppression restrictions orientation

### Impact Utilisateur (Public)
- Expérience edge-to-edge moderne
- Meilleure compatibilité tablettes
- Support rotation libre
- Performance améliorée

---

## 🎯 Prochaines Versions

### 1.0.4 (Future)
- Migration Java 8 → Java 17
- Optimisations WebView Capacitor
- Support Picture-in-Picture (optionnel)

### 1.1.0 (Future)
- Nouvelles features gameplay
- Optimisations foldables
- Support ChromeOS clavier/souris

---

**Date**: 13 novembre 2025  
**Version**: 1.0.3  
**Build**: 4  
**Target SDK**: 35 (Android 15)
