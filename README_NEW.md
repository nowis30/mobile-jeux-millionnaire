# 📱 Application Mobile - Héritier Millionnaire

Application Android native basée sur Capacitor pour le jeu Héritier Millionnaire.

---

## 📚 Documentation

### 🚀 Quick Start
- **[COMMANDES_BUILD.md](COMMANDES_BUILD.md)** - Commandes rapides pour build & upload
- **[BUILD_GUIDE.md](BUILD_GUIDE.md)** - Guide complet de build AAB/APK

### 📖 Documentation Technique
- **[ANDROID_15_COMPATIBILITY.md](ANDROID_15_COMPATIBILITY.md)** - Compatibilité Android 15 (SDK 35)
- **[RESOLUTION_COMPLETE.md](RESOLUTION_COMPLETE.md)** - Résolution problèmes Play Console
- **[CHANGEMENT_ANDROID_15.md](CHANGEMENT_ANDROID_15.md)** - Résumé des changements

---

## ⚡ Démarrage Rapide

### Build AAB Release (1 commande)
```powershell
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android"
.\gradlew.bat clean bundleRelease
```

### Fichier Généré
```
📁 android/app/build/outputs/bundle/release/app-release.aab
```

---

## 🎯 Version Actuelle

| Info | Valeur |
|------|--------|
| **Version Name** | 1.0.3 |
| **Version Code** | 4 |
| **Target SDK** | 35 (Android 15) |
| **Min SDK** | 22 (Android 5.1) |
| **Package** | com.heritier.millionnaire |

---

## ✅ Nouveautés Version 1.0.3

### Problèmes Google Play Console Résolus
- ✅ **Edge-to-Edge**: Support complet Android 15 avec `EdgeToEdge.enable()`
- ✅ **APIs Modernes**: Migration vers APIs window/display modernes
- ✅ **Grands Écrans**: Support tablettes, ChromeOS, foldables
- ✅ **Multi-Window**: Redimensionnement et split-screen activés

### Compatibilité Étendue
- ✅ Android 15 (SDK 35) supporté
- ✅ Tablettes full support
- ✅ ChromeOS compatible
- ✅ Appareils pliables (foldables)
- ✅ Mode multi-fenêtres
- ✅ Rotation libre (portrait/paysage)

---

## 🔧 Principe Technique

### Architecture
- **Frontend**: Next.js 14 (static export)
- **WebView**: Capacitor Android
- **Backend**: Fastify API (Render.com)
- **Base de données**: PostgreSQL (Render)
- **Ads**: Google AdMob avec UMP (GDPR)

### Flux de Build
1. Build Next.js client → static export
2. Copy export vers `mobile/dist`
3. Capacitor sync → Android project
4. Gradle build → APK/AAB

---

## 🛠️ Prérequis

### Logiciels Requis
- ✅ Node.js 18+ LTS
- ✅ JDK 21 (OpenJDK ou Oracle)
- ✅ Android Studio Hedgehog (2023.1.1) ou +
- ✅ Android SDK 35
- ✅ Gradle 8.5+

### Variables d'Environnement
```powershell
# JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# ANDROID_HOME
$env:ANDROID_HOME = "C:\Users\<user>\AppData\Local\Android\Sdk"

# PATH
$env:PATH += ";$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools"
```

---

## 📦 Installation

### 1. Installer Dépendances
```powershell
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile"
npm install
```

### 2. Synchroniser Capacitor
```powershell
npx cap sync android
```

### 3. Ouvrir dans Android Studio
```powershell
npx cap open android
```

---

## 🏗️ Build

### Build Debug (Développement)
```powershell
cd android
.\gradlew.bat assembleDebug

# APK: android/app/build/outputs/apk/debug/app-debug.apk
# Package ID: com.heritier.millionnaire.debug
```

### Build Release (Production)
```powershell
cd android
.\gradlew.bat bundleRelease

# AAB: android/app/build/outputs/bundle/release/app-release.aab
# Package ID: com.heritier.millionnaire
```

**Note**: Nécessite keystore configuré dans `gradle.properties`

---

## 🔐 Signature Release

### Générer Keystore (Première fois)
```powershell
cd mobile
keytool -genkey -v -keystore key-android.jks `
  -alias upload `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000
```

### Configurer `gradle.properties`
```properties
MYAPP_UPLOAD_STORE_FILE=../../../key-android.jks
MYAPP_UPLOAD_KEY_ALIAS=upload
MYAPP_UPLOAD_STORE_PASSWORD=<mot_de_passe_store>
MYAPP_UPLOAD_KEY_PASSWORD=<mot_de_passe_key>
```

⚠️ **IMPORTANT**: Ne jamais committer ce fichier !

---

## 📱 AdMob

### Configuration
- **App ID**: `ca-app-pub-7443046636998296~8556348720`
- **Plugin**: Custom `AdMobPlugin.java`
- **UMP**: Consentement GDPR activé

---

## 🌐 API & CORS

### Configuration Serveur (Render)
```env
CLIENT_ORIGIN=https://client-jeux-millionnaire.vercel.app,capacitor://localhost
```

---

## 🧪 Tests

### Test Local APK
```powershell
# Connecter appareil Android
adb devices

# Installer APK
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# Lancer app
adb shell am start -n com.heritier.millionnaire.debug/.MainActivity
```

---

## 📤 Upload Play Console

Voir **[COMMANDES_BUILD.md](COMMANDES_BUILD.md)** pour les étapes détaillées.

---

## 📊 Structure Projet

```
mobile/
├── android/                    # Projet Android natif
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/           # Code Java (MainActivity, AdMob)
│   │   │   ├── res/            # Resources (icons, styles)
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle        # Config app
│   ├── build.gradle            # Config projet
│   └── variables.gradle        # Versions SDK
├── dist/                       # Next.js static export
├── ANDROID_15_COMPATIBILITY.md # Doc Android 15
├── BUILD_GUIDE.md              # Guide build
├── COMMANDES_BUILD.md          # Commandes rapides
├── capacitor.config.ts         # Config Capacitor
└── package.json                # Dépendances Node
```

---

## 🎯 Versions

| Version | Code | Date | Changements |
|---------|------|------|-------------|
| 1.0.3 | 4 | 2025-11-13 | Android 15 compatibility, grands écrans |
| 1.0.2 | 3 | 2025-11-xx | AdMob integration, bug fixes |

---

**Dernière mise à jour**: 13 novembre 2025  
**Version stable**: 1.0.3 (build 4)  
**Status**: ✅ Production Ready
