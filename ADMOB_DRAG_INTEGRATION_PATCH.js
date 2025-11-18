/**
 * ===================================================================
 * PATCH POUR INTÉGRATION ADMOB DANS LE JEU DE DRAG
 * ===================================================================
 * 
 * Ce fichier documente les modifications à apporter au fichier main.js
 * du jeu de drag pour intégrer les publicités AdMob sur Android.
 * 
 * FICHIER À MODIFIER:
 * - client/public/drag/main.js
 * - mobile/android/app/src/main/assets/public/drag/main.js
 * 
 * LOCALISATION: Fonction `finishRace(playerWins)` (vers ligne 2106)
 * 
 * ===================================================================
 */

// AJOUTER CE CODE APRÈS LA LIGNE:
// game.result = 'loss';
// }
//
// ET AVANT LA LIGNE:
// // Envoi des résultats au serveur Millionnaire

/*
    // === INTÉGRATION ADMOB ANDROID ===
    // Notifier l'application Android de la fin de la course
    // L'activity Android affichera un interstitiel (si cooldown respecté)
    try {
        if (typeof window !== 'undefined' && window.AndroidDrag && typeof window.AndroidDrag.onRaceFinished === 'function') {
            const elapsedMs = Math.max(1, Math.round(((player.finishTime ?? game.timer) || 0) * 1000));
            window.AndroidDrag.onRaceFinished(finalWin, elapsedMs);
            console.log('[Drag] Notification Android: course terminée, pub en attente...');
        }
    } catch (err) {
        // Pas sur Android ou erreur, continuer normalement
        console.log('[Drag] Pas d\'interface Android détectée (mode web)');
    }
    // === FIN INTÉGRATION ADMOB ===
*/

/**
 * ===================================================================
 * RÉSUMÉ DU CHANGEMENT
 * ===================================================================
 * 
 * Cette modification permet au jeu de drag (HTML/JS) de communiquer
 * avec l'application Android native.
 * 
 * FONCTIONNEMENT:
 * 1. À la fin de chaque course, le JS appelle window.AndroidDrag.onRaceFinished()
 * 2. L'Activity Android (DragActivity.java) reçoit l'appel via @JavascriptInterface
 * 3. L'Activity affiche un interstitiel AdMob (si le cooldown est respecté)
 * 4. Le jeu continue normalement (envoi des résultats au serveur)
 * 
 * COMPATIBILITÉ:
 * - Sur Web: L'interface n'existe pas, le try/catch capture l'erreur, aucun impact
 * - Sur Android: L'interface est injectée par DragActivity, les pubs s'affichent
 * 
 * PARAMÈTRES TRANSMIS:
 * - finalWin (boolean): true si victoire, false si défaite
 * - elapsedMs (number): temps de la course en millisecondes
 * 
 * ===================================================================
 */

// EXEMPLE D'UTILISATION DANS LE CODE:
//
// if (window.AndroidDrag) {
//     window.AndroidDrag.onRaceFinished(true, 5234);  // Victoire en 5.234 secondes
//     window.AndroidDrag.log("Message de debug");     // Log dans logcat Android
//     const ready = window.AndroidDrag.isAdReady();   // Vérifier si pub prête
// }
