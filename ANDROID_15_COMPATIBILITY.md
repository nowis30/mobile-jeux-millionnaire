# Compatibilité Android 15 (SDK 35) - Résolution des Problèmes Google Play Console

## 📋 Problèmes Résolus

Ce document décrit les modifications apportées pour résoudre les trois problèmes d'expérience utilisateur signalés par Google Play Console pour la version 5 (versionCode 3, versionName 1.0.2).

---

## 🎯 Problème 1: Affichage de Bord à Bord (Edge-to-Edge)

### Description du Problème
> À partir d'Android 15, les applis ciblant le SDK 35 proposeront par défaut l'affichage de bord à bord. Les applis ciblant le SDK 35 doivent gérer les encarts pour s'assurer qu'elles s'affichent correctement sous Android 15 et version ultérieure.

### Solution Implémentée

#### 1. Ajout de la dépendance `androidx.activity`

**Fichier**: `mobile/android/app/build.gradle`

```gradle
implementation "androidx.activity:activity:$androidxActivityVersion"
```

Cette dépendance fournit `EdgeToEdge.enable()` pour activer l'affichage edge-to-edge avec rétrocompatibilité.

#### 2. Activation d'Edge-to-Edge dans MainActivity

**Fichier**: `mobile/android/app/src/main/java/com/heritier/millionnaire/MainActivity.java`

```java
import androidx.activity.EdgeToEdge;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Activer Edge-to-Edge pour compatibilité Android 15+ (SDK 35)
        // Assure la rétrocompatibilité avec les versions antérieures
        EdgeToEdge.enable(this);
        
        registerPlugin(AdMobPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
```

**Avantages**:
- ✅ Compatibilité automatique avec Android 15+
- ✅ Rétrocompatible avec Android 5.1+ (minSdkVersion 22)
- ✅ Gestion automatique des encarts système (status bar, navigation bar)
- ✅ API moderne recommandée par Google

---

## 🎨 Problème 2: APIs et Paramètres Obsolètes pour Edge-to-Edge

### Description du Problème
> Votre appli utilise des API ou des paramètres obsolètes pour l'affichage de bord à bord et des fenêtres sont devenus obsolètes dans Android 15.

### Solution Implémentée

**Fichier**: `mobile/android/app/src/main/res/values/styles.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">

    <style name="AppTheme.NoActionBar" parent="Theme.AppCompat.DayNight.NoActionBar">
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
        <item name="android:background">@null</item>
        <!-- Support Edge-to-Edge pour Android 15+ -->
        <item name="android:windowLayoutInDisplayCutoutMode" tools:targetApi="p">shortEdges</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>

    <style name="AppTheme.NoActionBarLaunch" parent="Theme.SplashScreen">
        <item name="android:background">@drawable/splash</item>
        <!-- Support Edge-to-Edge pour Android 15+ -->
        <item name="android:windowLayoutInDisplayCutoutMode" tools:targetApi="p">shortEdges</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
</resources>
```

**Changements Appliqués**:
1. ✅ Ajout de `xmlns:tools` pour supporter `tools:targetApi`
2. ✅ `windowLayoutInDisplayCutoutMode="shortEdges"` - Support des écrans avec encoche
3. ✅ `statusBarColor="transparent"` - Barre de statut transparente pour edge-to-edge
4. ✅ `navigationBarColor="transparent"` - Barre de navigation transparente

**Note**: Ces attributs utilisent `tools:targetApi="p"` (Android 9.0+) mais sont ignorés gracieusement sur les versions antérieures, assurant la compatibilité.

---

## 📱 Problème 3: Restrictions de Redimensionnement et Orientation (Grands Écrans)

### Description du Problème
> Votre jeu n'est pas compatible avec toutes les configurations d'affichage et utilise des restrictions de redimensionnement et d'orientation qui peuvent entraîner des problèmes de mise en page pour vos utilisateurs

### Solution Implémentée

**Fichier**: `mobile/android/app/src/main/AndroidManifest.xml`

```xml
<activity
    android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|smallestScreenSize|screenLayout|uiMode"
    android:name=".MainActivity"
    android:label="@string/title_activity_main"
    android:theme="@style/AppTheme.NoActionBarLaunch"
    android:launchMode="singleTask"
    android:exported="true"
    android:resizeableActivity="true"
    android:supportsPictureInPicture="false">
```

**Changements Appliqués**:
1. ✅ `android:resizeableActivity="true"` - Active le redimensionnement pour multi-fenêtres, foldables, ChromeOS
2. ✅ `android:supportsPictureInPicture="false"` - Désactive PiP (non applicable pour un jeu)
3. ✅ Pas de restriction `android:screenOrientation` - Support portrait ET paysage
4. ✅ `configChanges` inclut tous les changements de configuration nécessaires

**Compatibilité Grands Écrans**:
- ✅ Tablettes Android
- ✅ ChromeOS (ordinateurs portables)
- ✅ Téléphones pliables (foldables)
- ✅ Mode multi-fenêtres (split-screen)
- ✅ Orientation libre (portrait/paysage)

---

## 🔧 Versions et Dépendances

### Configuration Cible

**Fichier**: `mobile/android/variables.gradle`

```gradle
ext {
    minSdkVersion = 22          // Android 5.1 (Lollipop)
    compileSdkVersion = 35      // Android 15
    targetSdkVersion = 35       // Android 15
    androidxActivityVersion = '1.8.0'
}
```

### Matrice de Compatibilité

| Version Android | API Level | Support |
|----------------|-----------|---------|
| Android 5.1    | 22        | ✅ Minimum supporté |
| Android 6.0    | 23        | ✅ Fully supported |
| Android 7.0    | 24        | ✅ Multi-window support |
| Android 9.0    | 28        | ✅ Display cutout support |
| Android 10     | 29        | ✅ Gesture navigation |
| Android 12     | 31        | ✅ Material You ready |
| Android 13     | 33        | ✅ Themed icons |
| Android 14     | 34        | ✅ Full compatibility |
| **Android 15** | **35**    | ✅ **Edge-to-Edge par défaut** |

---

## 🧪 Tests Recommandés

### Avant Soumission Play Store

1. **Test Edge-to-Edge**
   - [ ] Vérifier que l'app s'affiche correctement sous la status bar
   - [ ] Vérifier que l'app s'affiche correctement au-dessus de la navigation bar
   - [ ] Tester sur un appareil avec encoche (notch)
   - [ ] Tester en mode sombre et clair

2. **Test Grands Écrans**
   - [ ] Tester sur tablette 10"+ (ou émulateur)
   - [ ] Tester rotation portrait → paysage
   - [ ] Tester mode multi-fenêtres (split-screen)
   - [ ] Tester redimensionnement fenêtre (ChromeOS/foldables)

3. **Test Rétrocompatibilité**
   - [ ] Tester sur Android 5.1 (API 22)
   - [ ] Tester sur Android 9.0 (API 28)
   - [ ] Tester sur Android 12 (API 31)
   - [ ] Tester sur Android 14 (API 34)

### Commandes de Test

```bash
# Build Release
cd mobile/android
./gradlew assembleRelease

# Installer sur appareil
adb install -r app/build/outputs/apk/release/app-release.apk

# Vérifier compatibilité Android 15
adb shell dumpsys window | grep "Edge to edge"

# Tester sur différentes densités
adb shell wm density <dpi>
adb shell wm size <width>x<height>
```

---

## 📊 Impact Utilisateur

### Avant les Modifications
- ⚠️ Incompatibilité potentielle avec Android 15
- ⚠️ APIs obsolètes causant des avertissements
- ⚠️ Restrictions sur tablettes et foldables
- ⚠️ Expérience sous-optimale en mode paysage

### Après les Modifications
- ✅ Pleine compatibilité Android 15 (SDK 35)
- ✅ Affichage edge-to-edge moderne
- ✅ Support complet grands écrans (tablettes, ChromeOS, foldables)
- ✅ Rotation libre (portrait/paysage)
- ✅ Multi-fenêtres activé
- ✅ Expérience utilisateur améliorée
- ✅ Conformité Play Store

---

## 🚀 Prochaines Étapes

### Avant Publication

1. **Build APK/AAB Signé**
   ```bash
   cd mobile/android
   ./gradlew bundleRelease
   ```

2. **Vérifier Bundle**
   - Taille < 150 MB
   - Signature valide
   - Version code incrémenté (4+)

3. **Upload Google Play Console**
   - Track: Production
   - Nom version: 1.0.3 (ou supérieur)
   - Notes de version: Mentionner compatibilité Android 15

4. **Configuration Play Console**
   - Captures d'écran tablette (obligatoire maintenant)
   - Description mise à jour mentionnant support grands écrans
   - Catégories d'appareils: Téléphones + Tablettes + ChromeOS

### Améliorations Futures (Optionnel)

1. **WindowInsets Handling**
   - Gérer les insets manuellement dans le WebView Capacitor
   - Padding dynamique basé sur les system bars

2. **Foldables Optimization**
   - Détecter et adapter l'UI pour écrans pliables
   - Support dual-screen (Surface Duo)

3. **ChromeOS Optimization**
   - Support clavier/souris
   - Raccourcis clavier
   - Redimensionnement fenêtre fluide

4. **Picture-in-Picture**
   - Activer PiP si pertinent pour certaines features

---

## 📝 Résumé des Fichiers Modifiés

| Fichier | Changement | Impact |
|---------|-----------|--------|
| `app/build.gradle` | Ajout `androidx.activity:activity` | Dépendance EdgeToEdge |
| `MainActivity.java` | `EdgeToEdge.enable(this)` | Activation edge-to-edge |
| `styles.xml` | Attributs transparence + cutout | Style edge-to-edge |
| `AndroidManifest.xml` | `resizeableActivity="true"` | Support grands écrans |

---

## ✅ Checklist de Validation

- [x] EdgeToEdge.enable() implémenté dans MainActivity
- [x] androidx.activity dépendance ajoutée
- [x] Styles.xml mis à jour avec attributs edge-to-edge
- [x] AndroidManifest.xml: resizeableActivity="true"
- [x] Pas de restrictions d'orientation
- [x] targetSdkVersion = 35 (Android 15)
- [ ] Tests sur Android 15 emulator
- [ ] Tests sur tablette physique
- [ ] Tests redimensionnement multi-fenêtres
- [ ] Build release réussi
- [ ] Upload Google Play Console

---

## 📞 Support

Pour toute question sur ces modifications:
- Documentation Android Edge-to-Edge: https://developer.android.com/develop/ui/views/layout/edge-to-edge
- Guide Grands Écrans: https://developer.android.com/guide/topics/large-screens
- Capacitor Android: https://capacitorjs.com/docs/android

---

**Date de mise à jour**: 13 novembre 2025  
**Version impactée**: 1.0.3+  
**SDK Target**: 35 (Android 15)
