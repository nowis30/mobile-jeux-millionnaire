# 🚀 COMMANDES RAPIDES - Build & Upload

## ⚡ Quick Start (Pour les Impatients)

```powershell
# 1. Aller dans le dossier Android
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"

# 2. Build AAB release
.\gradlew.bat bundleRelease

# 3. Vérifier le fichier
dir app\build\outputs\bundle\release\app-release.aab

# 4. Upload sur Play Console (manuel)
# → https://play.google.com/console
```

---

## 📦 Build AAB (Recommandé)

### Build Release AAB
```powershell
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat clean bundleRelease
```

### Localisation Fichier
```
📁 app\build\outputs\bundle\release\app-release.aab
```

### Vérifier Signature
```powershell
jarsigner -verify -verbose -certs app\build\outputs\bundle\release\app-release.aab
```

**Output Attendu**:
```
jar verified.
```

---

## 📱 Build APK (Test Local)

### Build Release APK
```powershell
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat clean assembleRelease
```

### Localisation Fichier
```
📁 app\build\outputs\apk\release\app-release.apk
```

### Installer sur Appareil
```powershell
# Vérifier appareil connecté
adb devices

# Installer APK
adb install -r app\build\outputs\apk\release\app-release.apk

# Lancer l'app
adb shell am start -n com.heritier.millionnaire/.MainActivity
```

---

## 🔐 Configuration Signature

### Fichier: `mobile/android/gradle.properties`

**Créer si absent**:
```powershell
New-Item -Path "gradle.properties" -ItemType File
```

**Contenu**:
```properties
MYAPP_UPLOAD_STORE_FILE=../../../key-android.jks
MYAPP_UPLOAD_KEY_ALIAS=upload
MYAPP_UPLOAD_STORE_PASSWORD=VOTRE_MOT_DE_PASSE_STORE
MYAPP_UPLOAD_KEY_PASSWORD=VOTRE_MOT_DE_PASSE_KEY
```

⚠️ **IMPORTANT**: Ajouter à `.gitignore`:
```powershell
echo "gradle.properties" >> .gitignore
```

---

## 🧹 Nettoyage

### Clean Standard
```powershell
.\gradlew.bat clean
```

### Clean Complet (Si Problèmes)
```powershell
# Supprimer caches Gradle
Remove-Item -Recurse -Force .gradle, build, app\build -ErrorAction SilentlyContinue

# Rebuild
.\gradlew.bat clean bundleRelease
```

---

## ✅ Vérifications Avant Upload

### 1. Vérifier Version
```powershell
# Ouvrir build.gradle et vérifier
code app\build.gradle
```

**Doit contenir**:
```gradle
versionCode 4
versionName "1.0.3"
targetSdkVersion 35
```

### 2. Vérifier Taille AAB
```powershell
dir app\build\outputs\bundle\release\app-release.aab

# Doit être < 150 MB (généralement 5-15 MB)
```

### 3. Vérifier Signature
```powershell
jarsigner -verify app\build\outputs\bundle\release\app-release.aab
```

**Output**: `jar verified.` ✅

### 4. Analyser AAB (Optionnel)
```powershell
# Installer bundletool
# https://github.com/google/bundletool/releases

bundletool build-apks --bundle=app\build\outputs\bundle\release\app-release.aab --output=test.apks
bundletool get-size total --apks=test.apks
```

---

## 📤 Upload Google Play Console

### 1. Connexion
🔗 https://play.google.com/console

### 2. Navigation
```
Applications → Heritier Millionnaire
→ Release → Production
→ Create new release
```

### 3. Upload AAB
- Drag & Drop: `app-release.aab`
- Ou: Browse files → Sélectionner AAB

### 4. Release Notes

**Français**:
```
Version 1.0.3 - Compatibilité Android 15 et Grands Écrans

Nouveautés:
✅ Support complet Android 15 (API 35)
✅ Affichage moderne edge-to-edge
✅ Compatibilité optimale tablettes et ChromeOS
✅ Support appareils pliables (foldables)
✅ Multi-fenêtres activé
✅ Expérience améliorée sur tous types d'écrans
✅ Corrections de bugs mineurs

Merci de votre confiance!
```

**Anglais** (si applicable):
```
Version 1.0.3 - Android 15 & Large Screens Compatibility

What's New:
✅ Full Android 15 (API 35) support
✅ Modern edge-to-edge display
✅ Optimized for tablets and ChromeOS
✅ Foldable devices support
✅ Multi-window enabled
✅ Enhanced experience on all screen sizes
✅ Minor bug fixes

Thank you for your support!
```

### 5. Review & Publish
```
1. Review release → Verify no warnings
2. Save draft
3. Review → Start rollout to production
4. Confirm
```

---

## 🧪 Tests Avant Production

### Test APK Local
```powershell
# Build et install
.\gradlew.bat assembleRelease
adb install -r app\build\outputs\apk\release\app-release.apk

# Lancer app
adb shell am start -n com.heritier.millionnaire/.MainActivity
```

### Test Rotation
```powershell
# Forcer portrait
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0

# Forcer paysage
adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:1
```

### Test Multi-Window
```
# Sur appareil Android 7+
1. Ouvrir app
2. Bouton carré (Recent apps)
3. Drag l'icône app vers haut
4. Sélectionner 2ème app
```

### Vérifier Edge-to-Edge
```powershell
# Prendre screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Vérifier visuellement:
# - Status bar transparente
# - Navigation bar transparente
# - Contenu sous les barres système
```

---

## 🐛 Troubleshooting Rapide

### Erreur: "Keystore not found"
```powershell
# Vérifier chemin dans gradle.properties
Test-Path "..\..\..key-android.jks"

# Si faux, corriger chemin ou créer keystore
keytool -genkey -v -keystore key-android.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

### Erreur: "SDK 35 not found"
```powershell
# Installer SDK 35 via Android Studio
# Ou via sdkmanager
sdkmanager "platforms;android-35"
```

### Erreur: "Build failed"
```powershell
# Nettoyer et retry
.\gradlew.bat clean --refresh-dependencies
.\gradlew.bat bundleRelease --stacktrace
```

### Warning: "compileSdk 35 not supported by AGP"
```powershell
# Ajouter dans gradle.properties
echo "android.suppressUnsupportedCompileSdk=35" >> gradle.properties
```

---

## 📊 Checklist Upload

### Avant Build
- [ ] versionCode incrémenté (4)
- [ ] versionName mise à jour (1.0.3)
- [ ] gradle.properties configuré
- [ ] Keystore valide

### Build
- [ ] `.\gradlew.bat clean`
- [ ] `.\gradlew.bat bundleRelease`
- [ ] Build SUCCESS
- [ ] AAB créé (< 150 MB)
- [ ] Signature valide

### Tests
- [ ] APK installé sur appareil physique
- [ ] App lance sans crash
- [ ] Rotation fonctionne
- [ ] Pas d'erreurs visuelles

### Upload
- [ ] Connexion Play Console
- [ ] AAB uploadé
- [ ] Release notes ajoutées (FR/EN)
- [ ] Review completed
- [ ] Rollout lancé

### Après Upload
- [ ] Email confirmation Google reçu
- [ ] Vérifier warnings disparus
- [ ] Monitorer crash reports

---

## ⏱️ Temps Estimés

| Étape | Durée |
|-------|-------|
| Clean build | 30 sec |
| Build AAB release | 1-2 min |
| Vérifications | 2-3 min |
| Upload Play Console | 3-5 min |
| **Total** | **~10 min** |

| Validation Google | 24-48h |
|-------------------|--------|

---

## 🎯 Workflow Complet One-Liner

```powershell
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android" ; .\gradlew.bat clean bundleRelease ; jarsigner -verify app\build\outputs\bundle\release\app-release.aab ; echo "✅ AAB prêt pour upload!"
```

---

## 📝 Notes Importantes

### ⚠️ À NE JAMAIS FAIRE
- ❌ Committer `gradle.properties` avec mots de passe
- ❌ Partager keystore publiquement
- ❌ Oublier d'incrémenter versionCode
- ❌ Upload sans tester APK avant

### ✅ Bonnes Pratiques
- ✅ Backup keystore dans lieu sécurisé
- ✅ Tester APK sur appareil physique avant upload
- ✅ Vérifier taille AAB (< 150 MB)
- ✅ Release notes claires et en français

---

## 🚀 Prêt à Publier?

```powershell
# 🎬 ACTION!
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat clean bundleRelease

# Puis upload manuel sur Play Console
# https://play.google.com/console
```

**Bonne chance! 🍀**

---

**Version**: 1.0.3 (build 4)  
**Date**: 13 novembre 2025  
**Status**: ✅ Prêt pour production
