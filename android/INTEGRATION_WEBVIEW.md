# 🎮 Guide d'intégration WebView - Drag Shift Duel

## ✅ État actuel : **COMPLET ET FONCTIONNEL**

Le jeu web est déjà intégré dans une Activity Android native avec WebView plein écran.

---

## 📍 Fichier principal : `DragActivity.java`

**Emplacement :** `mobile/android/app/src/main/java/.../DragActivity.java`

### Fonctionnalités implémentées

✅ **WebView plein écran immersif**
- Mode immersif avec `FLAG_LAYOUT_NO_LIMITS`
- Cache la barre de navigation avec `WindowInsetsController`
- Compatible API 30+

✅ **Configuration WebView optimale**
```java
settings.setJavaScriptEnabled(true);
settings.setDomStorageEnabled(true);
settings.setAllowFileAccessFromFileURLs(true);
settings.setBuiltInZoomControls(false);
```

✅ **Bridge JavaScript ↔ Android**
```javascript
// Appelable depuis le jeu web
window.AndroidDrag.onRaceFinished(playerWon, elapsedMs);
window.AndroidDrag.isAdReady();
window.AndroidDrag.log("message");
```

✅ **Intégration AdMob**
- Annonce interstitielle avec cooldown de 60 secondes
- Bannière publicitaire (désactivable)

---

## 🚀 Comment lancer l'Activity

### Depuis n'importe quelle Activity Java/Kotlin

```java
Intent intent = new Intent(this, DragActivity.class);
startActivity(intent);
```

### Depuis un Fragment

```kotlin
val intent = Intent(requireContext(), DragActivity::class.java)
startActivity(intent)
```

### Avec un bouton

```java
Button btnLaunchDragRace = findViewById(R.id.btnDragRace);
btnLaunchDragRace.setOnClickListener(v -> {
    Intent intent = new Intent(this, DragActivity.class);
    startActivity(intent);
});
```

---

## 📂 Fichiers du jeu web

**Emplacement :** `mobile/android/app/src/main/assets/public/drag/`

```
public/drag/
├── index.html       ← HTML principal (250 lignes)
├── style.css        ← Styles responsives (800 lignes)
├── main.js          ← Logique du jeu (2588 lignes)
├── iframe.html      ← Version iframe
└── standalone.html  ← Version standalone
```

**URL chargée par WebView :**
```
file:///android_asset/public/drag/index.html
```

---

## 📱 Optimisations mobile déjà appliquées

### `index.html`
```html
<meta name="viewport" 
      content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta name="mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-capable" content="yes">
```

### `style.css`

**Boutons tactiles agrandis (2024 update) :**
- Écrans normaux : 160×160px
- Petits écrans (≤520px) : 170×170px
- Position fixe : bottom 32-40px, left/right 24-32px

**Touch optimization :**
```css
.pedal-button, .nitro-button {
    touch-action: none;
    -webkit-user-select: none;
    user-select: none;
}
```

**Responsive breakpoints :**
- `@media (max-width: 768px)` : Tablettes
- `@media (max-width: 640px)` : Petits mobiles
- `@media (max-width: 520px)` : Très petits écrans

---

## 🎮 Structure du jeu web

### Canvas principal
```javascript
<canvas id="trackCanvas" width="960" height="540"></canvas>
```

### Contrôles tactiles
```html
<button id="gasButton" class="pedal-button">ACCÉLÉRER</button>
<button id="nitroButton" class="nitro-button">NITRO</button>
<button id="shiftButton" class="pedal-button shift-mode">SHIFT</button>
```

### HUD (7 cartes stats)
- 🏁 COURSE (numéro)
- 💰 ARGENT (solde)
- ⏱️ TEMPS (chrono)
- 🎯 DERNIER SHIFT (qualité)
- 🏎️ VITESSE MAX
- ⚡ RPM MAX
- 🔄 MODE (sélection)

### Overlays
- **Garage** : Améliorations et réglages
- **Adversaires** : Sélection PvP
- **Écran d'accueil** : Modes de jeu

---

## 🔧 Configuration AdMob

**Fichier :** `DragActivity.java`

```java
// Test IDs (remplacer par vrais IDs en production)
private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
private static final String BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

// Cooldown annonce : 60 secondes
private static final long AD_COOLDOWN_MS = 60000;
```

**Méthodes disponibles :**
```java
loadInterstitialAd()      // Charge une nouvelle annonce
showInterstitialAd()      // Affiche l'annonce (avec cooldown)
isAdReady()               // Vérifie si annonce chargée
```

---

## 🌐 Bridge JavaScript → Android

### Classe interne `DragBridge`

```java
private class DragBridge {
    @JavascriptInterface
    public void onRaceFinished(boolean playerWon, int elapsedMs) {
        // Appelé à la fin de chaque course
        runOnUiThread(() -> {
            if (playerWon) {
                showInterstitialAd();
            }
        });
    }

    @JavascriptInterface
    public boolean isAdReady() {
        return interstitialAd != null;
    }

    @JavascriptInterface
    public void log(String message) {
        android.util.Log.d("DragWebView", message);
    }
}
```

### Utilisation depuis `main.js`

```javascript
// Notifier Android de la fin de course
if (typeof AndroidDrag !== 'undefined') {
    AndroidDrag.onRaceFinished(playerWon, elapsedTimeMs);
}

// Vérifier si pub dispo
if (typeof AndroidDrag !== 'undefined' && AndroidDrag.isAdReady()) {
    // Afficher indication pub dispo
}

// Logger debug
if (typeof AndroidDrag !== 'undefined') {
    AndroidDrag.log("Player speed: " + speed);
}
```

---

## 🎨 Personnalisations possibles

### Agrandir davantage les boutons
```css
/* Dans style.css, breakpoint @media (max-width: 520px) */
.pedal-button, .nitro-button {
    width: 180px;   /* Au lieu de 170px */
    height: 180px;
}
```

### Désactiver zoom complètement
```html
<!-- Dans index.html -->
<meta name="viewport" 
      content="width=device-width, initial-scale=1.0, 
               maximum-scale=1.0, minimum-scale=1.0, 
               user-scalable=no, viewport-fit=cover">
```

### Changer couleurs boutons
```css
/* Dans style.css */
.pedal-button {
    background: linear-gradient(135deg, #FF6B6B, #FF4444); /* Rouge */
}

.nitro-button {
    background: linear-gradient(135deg, #4ECDC4, #44A08D); /* Turquoise */
}
```

---

## 📋 Checklist pré-lancement

- [x] WebView configuré avec JavaScript activé
- [x] Fichiers HTML/CSS/JS dans `assets/public/drag/`
- [x] Viewport meta avec `user-scalable=no`
- [x] Boutons tactiles ≥ 160×160px
- [x] Position fixe avec z-index: 30
- [x] Touch optimization (`touch-action: none`)
- [x] Mode plein écran immersif
- [x] Bridge JavaScript fonctionnel
- [x] AdMob initialisé (Test IDs)
- [x] Responsive breakpoints (768px, 640px, 520px)

---

## 🐛 Debugging

### Activer console WebView

```java
// Dans DragActivity.onCreate()
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    WebView.setWebContentsDebuggingEnabled(true);
}
```

### Inspecter WebView depuis Chrome

1. Connecter appareil en USB
2. Ouvrir Chrome : `chrome://inspect`
3. Sélectionner le WebView de l'app
4. Console JavaScript disponible

### Logs Android

```bash
adb logcat | grep DragWebView
```

---

## 🎯 Modes de jeu disponibles

### RACE_MODES dans `main.js`

```javascript
world: { payout: 1000000 }   // Record mondial - 1M$
pvp: { payout: 500000 }      // PvP - 500k$
ghost: { payout: 50000 }     // IA Fantôme - 50k$
```

### Système d'améliorations

- **Moteur** : Puissance, couple
- **Transmission** : Ratios, vitesse de shift
- **Nitro** : Durée, puissance, charges
- **Pneus** : Adhérence, départ

---

## 📦 Déploiement

### Build debug
```bash
cd mobile/android
./gradlew assembleDebug
```

### Build release (production)
```bash
./gradlew assembleRelease
```

**APK généré :**
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔗 Ressources

- **Jeu web live** : https://nowis30.github.io/drag/
- **Docs WebView** : https://developer.android.com/reference/android/webkit/WebView
- **AdMob** : https://developers.google.com/admob/android
- **JavaScript Bridge** : https://developer.android.com/develop/ui/views/layout/webapps/webview#BindingJavaScript

---

## 💡 Résumé rapide

```java
// C'est aussi simple que ça !
Intent intent = new Intent(this, DragActivity.class);
startActivity(intent);
```

✅ **Le jeu est prêt à l'emploi**  
✅ **Optimisé pour mobile**  
✅ **Intégration AdMob**  
✅ **Bridge JavaScript actif**

**Pas de configuration supplémentaire nécessaire.**
