# 🔧 Corrections de connexion - Jeu Drag Racing

## ✅ Problèmes résolus

### 1. **Connexion au serveur Render instable** 🌐
**Problème** : L'application mobile avait du mal à se connecter au serveur Render.

**Solutions appliquées** :
- ✅ Timeout de 30 secondes pour toutes les requêtes réseau
- ✅ Gestion des erreurs de timeout avec messages clairs
- ✅ Meilleur logging pour diagnostiquer les problèmes de connexion
- ✅ Retry automatique avec gestion d'erreurs améliorée

### 2. **Profil non connecté entre l'app et le jeu drag** 👤
**Problème** : Le jeu drag ne récupérait pas le profil enregistré dans l'application principale.

**Solutions appliquées** :
- ✅ **Bridge Android amélioré** avec deux nouvelles méthodes :
  - `getAuthToken()` : Récupère le token d'authentification depuis l'app
  - `getSessionData()` : Récupère les données de session (gameId, playerId)
- ✅ **JavaScript amélioré** qui vérifie en priorité les données depuis Android
- ✅ Synchronisation automatique du token et de la session en localStorage

## 🔄 Flux de connexion amélioré

### Avant (problématique)
```
Jeu Drag → localStorage (vide) → Création token guest → Profil déconnecté ❌
```

### Après (corrigé)
```
Jeu Drag → Android Bridge → Token de l'app → localStorage → Profil connecté ✅
          ↓
          Session partagée (gameId, playerId)
```

## 📋 Fonctions ajoutées au Bridge Android

### `DragBridge.getAuthToken()`
```java
@JavascriptInterface
public String getAuthToken() {
    // Récupère le token depuis SharedPreferences Capacitor
    // Essaie plusieurs clés : HM_TOKEN, hm-token, auth_token
    return token;
}
```

### `DragBridge.getSessionData()`
```java
@JavascriptInterface
public String getSessionData() {
    // Récupère la session complète : {"gameId": "...", "playerId": "..."}
    return sessionJson;
}
```

## 🎯 Fonctions JavaScript améliorées

### `getAuthToken()`
```javascript
function getAuthToken() {
    // Priorité 1 : Bridge Android
    if (window.AndroidDrag?.getAuthToken) {
        const token = window.AndroidDrag.getAuthToken();
        if (token) {
            localStorage.setItem('hm-token', token); // Sync local
            return token;
        }
    }
    
    // Priorité 2 : localStorage drag
    // Priorité 3 : localStorage global
}
```

### `getStoredSession()`
```javascript
function getStoredSession() {
    // Priorité 1 : Bridge Android
    if (window.AndroidDrag?.getSessionData) {
        const session = window.AndroidDrag.getSessionData();
        if (session) {
            localStorage.setItem('hm-session', session); // Sync local
            return JSON.parse(session);
        }
    }
    
    // Priorité 2 : localStorage
}
```

### `apiFetch()`
```javascript
async function apiFetch(path, init = {}, retry = true) {
    // Timeout de 30 secondes
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000);
    
    try {
        const res = await fetch(url, { 
            signal: controller.signal,
            // ...
        });
        clearTimeout(timeoutId);
        // ...
    } catch (err) {
        if (err.name === 'AbortError') {
            throw new Error('Timeout de connexion au serveur');
        }
        throw err;
    }
}
```

### `ensureSession()`
```javascript
async function ensureSession() {
    try {
        console.log('[drag] Vérification de la session...');
        
        // 1. Récupérer token (depuis Android ou localStorage)
        await ensureGuestToken().catch(() => null);
        
        // 2. Récupérer session (depuis Android ou localStorage)
        let sess = getStoredSession();
        if (sess?.gameId && sess?.playerId) {
            console.log('[drag] Session existante trouvée');
            return sess;
        }
        
        // 3. Créer nouvelle session si nécessaire
        console.log('[drag] Création nouvelle session...');
        // ...
    } catch (err) {
        console.error('[drag] Erreur ensureSession:', err);
        throw err;
    }
}
```

## 🧪 Test de connexion

### Vérifier dans la console Android (Logcat)
```
[DragActivity] Token récupéré: présent/absent
[DragActivity] Session récupérée depuis storage
[DragWebView] Token récupéré depuis Android
[DragWebView] Session récupérée depuis Android
[DragWebView] Session existante trouvée: <gameId>
```

### Vérifier dans la console JavaScript du WebView
```
Chrome DevTools → chrome://inspect → Sélectionner WebView
Console :
  [drag] App mobile Capacitor détecté: API directe https://server-jeux-millionnaire.onrender.com
  [drag] Token récupéré depuis Android
  [drag] Session récupérée depuis Android
  [drag] Vérification de la session...
  [drag] Session existante trouvée: <gameId>
```

## 📱 Installation et test

1. **Installer le nouvel APK** :
   ```
   mobile/android/app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Lancer l'application principale** et se connecter

3. **Ouvrir le jeu drag** depuis le menu

4. **Vérifier** :
   - ✅ Le jeu affiche votre profil (email/nickname)
   - ✅ Le cash est synchronisé avec votre compte
   - ✅ Les courses sont sauvegardées sur votre profil
   - ✅ Pas de message "Connexion requise (guest)"

## 🔍 Diagnostic des problèmes

### Si le profil n'est pas connecté :
```bash
# Activer le logging détaillé
adb logcat | grep -E "DragActivity|DragWebView|drag"
```

### Si timeout de connexion :
- Vérifier la connexion internet
- Render peut prendre 30-60s au démarrage à froid
- Réessayer après quelques secondes

### Si session non trouvée :
- Vérifier que vous êtes bien connecté dans l'app principale
- Relancer l'app principale pour créer la session
- Vérifier les SharedPreferences dans `CapacitorStorage`

## 📊 Améliorations techniques

| Fonctionnalité | Avant | Après |
|---|---|---|
| **Récupération token** | localStorage uniquement | Android Bridge → localStorage |
| **Récupération session** | localStorage uniquement | Android Bridge → localStorage |
| **Timeout réseau** | Aucun | 30 secondes |
| **Gestion erreurs** | Basique | Détaillée avec retry |
| **Logging** | Minimal | Complet (Android + JS) |
| **Connexion profil** | ❌ Déconnecté | ✅ Connecté |
| **Sync données** | ❌ Non synchronisé | ✅ Synchronisé |

## 🎉 Résultat final

- ✅ Le jeu drag se connecte correctement au serveur Render
- ✅ Le profil utilisateur est synchronisé entre l'app et le jeu
- ✅ Les données (cash, stage, upgrades) sont sauvegardées
- ✅ Meilleure gestion des erreurs réseau
- ✅ Timeouts pour éviter les blocages
- ✅ Logging détaillé pour le diagnostic

## 🚀 Prochaines étapes recommandées

1. Tester avec plusieurs comptes utilisateurs
2. Vérifier la persistance après fermeture/réouverture de l'app
3. Tester en conditions réseau dégradées (3G, 4G lent)
4. Monitorer les logs Render pour vérifier les connexions
