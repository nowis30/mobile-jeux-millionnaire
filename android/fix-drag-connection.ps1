# Script d'amélioration de la connexion au serveur Render pour le jeu drag
$file = "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android\app\src\main\assets\public\drag\main.js"

Write-Host "Lecture du fichier main.js..." -ForegroundColor Yellow
$content = Get-Content $file -Raw -Encoding UTF8

Write-Host "Application des améliorations de connexion..." -ForegroundColor Yellow

# Fix 1: Améliorer getAuthToken pour récupérer le token depuis Android
$oldGetAuthToken = @"
function getAuthToken() {
    try {
        // Priorité : token spécifique drag
        const dragToken = localStorage.getItem('hm-token');
        if (dragToken) return dragToken;
        // Fallback : token global utilisé par le client Next
        const globalToken = localStorage.getItem('HM_TOKEN');
        return globalToken || null;
    } catch {
        return null;
    }
}
"@

$newGetAuthToken = @"
function getAuthToken() {
    try {
        // Priorité 1 : Récupérer le token depuis le bridge Android
        if (typeof window.AndroidDrag !== 'undefined' && typeof window.AndroidDrag.getAuthToken === 'function') {
            const androidToken = window.AndroidDrag.getAuthToken();
            if (androidToken) {
                console.log('[drag] Token récupéré depuis Android');
                // Sauvegarder en localStorage pour utilisation ultérieure
                localStorage.setItem('hm-token', androidToken);
                localStorage.setItem('HM_TOKEN', androidToken);
                return androidToken;
            }
        }
        
        // Priorité 2 : token spécifique drag en localStorage
        const dragToken = localStorage.getItem('hm-token');
        if (dragToken) return dragToken;
        
        // Priorité 3 : token global utilisé par le client Next
        const globalToken = localStorage.getItem('HM_TOKEN');
        return globalToken || null;
    } catch (e) {
        console.error('[drag] Erreur getAuthToken:', e);
        return null;
    }
}
"@

$content = $content.Replace($oldGetAuthToken, $newGetAuthToken)

# Fix 2: Améliorer getStoredSession pour récupérer depuis Android
$oldGetStoredSession = @"
function getStoredSession() {
    try { const raw = localStorage.getItem('hm-session'); return raw ? JSON.parse(raw) : null; } catch { return null; }
}
"@

$newGetStoredSession = @"
function getStoredSession() {
    try {
        // Priorité 1 : Récupérer depuis le bridge Android
        if (typeof window.AndroidDrag !== 'undefined' && typeof window.AndroidDrag.getSessionData === 'function') {
            const androidSession = window.AndroidDrag.getSessionData();
            if (androidSession) {
                try {
                    const parsed = JSON.parse(androidSession);
                    if (parsed && parsed.gameId && parsed.playerId) {
                        console.log('[drag] Session récupérée depuis Android');
                        // Sauvegarder en localStorage
                        localStorage.setItem('hm-session', androidSession);
                        return parsed;
                    }
                } catch (e) {
                    console.error('[drag] Erreur parsing session Android:', e);
                }
            }
        }
        
        // Priorité 2 : localStorage
        const raw = localStorage.getItem('hm-session');
        return raw ? JSON.parse(raw) : null;
    } catch (e) {
        console.error('[drag] Erreur getStoredSession:', e);
        return null;
    }
}
"@

$content = $content.Replace($oldGetStoredSession, $newGetStoredSession)

# Fix 3: Améliorer la gestion des erreurs réseau avec retry et timeout
$oldApiFetchStart = @"
async function apiFetch(path, init = {}, retry = true) {
    const method = (init.method || 'GET').toUpperCase();
    const headers = Object.assign({}, init.headers || {});
"@

$newApiFetchStart = @"
async function apiFetch(path, init = {}, retry = true) {
    const method = (init.method || 'GET').toUpperCase();
    const headers = Object.assign({}, init.headers || {});
    
    // Timeout pour les requêtes lentes (30 secondes)
    const timeoutMs = 30000;
"@

$content = $content.Replace($oldApiFetchStart, $newApiFetchStart)

# Fix 4: Ajouter un timeout et meilleure gestion d'erreur au fetch
$oldFetchCall = @"
    const res = await fetch(url, { credentials: 'include', ...init, headers });
    if (res.status === 401 && retry && getTokenSource() === 'guest') {
"@

$newFetchCall = @"
    // Fetch avec timeout
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    
    try {
        const res = await fetch(url, { 
            credentials: 'include', 
            ...init, 
            headers,
            signal: controller.signal 
        });
        clearTimeout(timeoutId);
        
        if (res.status === 401 && retry && getTokenSource() === 'guest') {
"@

$content = $content.Replace($oldFetchCall, $newFetchCall)

# Fix 5: Ajouter gestion d'erreur de timeout
$oldFetchEnd = @"
    if (!res.ok) throw new Error(`HTTP `${res.status}`);
    if (res.status === 204) return undefined;
    return await res.json();
}
"@

$newFetchEnd = @"
        if (!res.ok) throw new Error(`HTTP `${res.status}`);
        if (res.status === 204) return undefined;
        return await res.json();
    } catch (err) {
        clearTimeout(timeoutId);
        if (err.name === 'AbortError') {
            console.error('[drag] Timeout de la requête:', url);
            throw new Error('Timeout de connexion au serveur');
        }
        throw err;
    }
}
"@

$content = $content.Replace($oldFetchEnd, $newFetchEnd)

# Fix 6: Améliorer ensureSession avec meilleur logging
$oldEnsureSession = @"
async function ensureSession() {
    await ensureGuestToken().catch(() => null);
    let sess = getStoredSession();
    if (sess?.gameId && sess?.playerId) return sess;
    // Auto-join sur la partie globale
    const list = await apiFetch('/api/games');
    const g = list?.games?.[0]; if (!g) throw new Error('Aucune partie');
    const joined = await apiFetch(`/api/games/`${g.id}`/join`, { method:'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify({}) });
    sess = { gameId: g.id, playerId: joined.playerId, nickname: '' };
    setStoredSession(sess);
    return sess;
}
"@

$newEnsureSession = @"
async function ensureSession() {
    try {
        console.log('[drag] Vérification de la session...');
        await ensureGuestToken().catch(() => null);
        
        let sess = getStoredSession();
        if (sess?.gameId && sess?.playerId) {
            console.log('[drag] Session existante trouvée:', sess.gameId);
            return sess;
        }
        
        // Auto-join sur la partie globale
        console.log('[drag] Récupération de la liste des parties...');
        const list = await apiFetch('/api/games');
        const g = list?.games?.[0]; 
        if (!g) throw new Error('Aucune partie disponible');
        
        console.log('[drag] Rejoindre la partie:', g.id);
        const joined = await apiFetch(`/api/games/`${g.id}`/join`, { 
            method:'POST', 
            headers: { 'Content-Type':'application/json' }, 
            body: JSON.stringify({}) 
        });
        
        sess = { gameId: g.id, playerId: joined.playerId, nickname: '' };
        setStoredSession(sess);
        console.log('[drag] Session créée avec succès');
        return sess;
    } catch (err) {
        console.error('[drag] Erreur ensureSession:', err);
        throw err;
    }
}
"@

$content = $content.Replace($oldEnsureSession, $newEnsureSession)

Write-Host "Écriture du fichier amélioré..." -ForegroundColor Yellow
Set-Content $file $content -Encoding UTF8 -NoNewline

Write-Host "`n✅ Améliorations appliquées avec succès!" -ForegroundColor Green
Write-Host "  - Récupération du token depuis Android" -ForegroundColor Cyan
Write-Host "  - Récupération de la session depuis Android" -ForegroundColor Cyan
Write-Host "  - Timeout de 30s pour les requêtes réseau" -ForegroundColor Cyan
Write-Host "  - Meilleur logging des erreurs de connexion" -ForegroundColor Cyan
Write-Host "  - Gestion des erreurs de timeout" -ForegroundColor Cyan
