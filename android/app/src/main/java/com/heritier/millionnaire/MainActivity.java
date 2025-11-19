package com.heritier.millionnaire;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.CookieManager;
import android.text.TextUtils;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Bridge;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends BridgeActivity {

    // Base URL du client Web (Next.js) utilisé pour les pages Accueil/Immobilier/Quiz/Pari
    private static final String CLIENT_BASE = "https://client-jeux-millionnaire.vercel.app";

    // Séquence ordonnée des volets pour la navigation par swipe
    // Index 0 → Accueil (/)
    // Index 1 → Immobilier (/immobilier)
    // Index 2 → Quiz (/quiz)
    // Index 3 → Pari (/pari)
    // Pour ajouter un nouveau volet plus tard: ajouter le chemin ici et ajuster la logique next/prev si besoin.
    private static final String[] PAGES = new String[] { "/", "/immobilier", "/quiz", "/pari" };

    private GestureDetector gestureDetector;
    private final AtomicReference<String> lastKnownPath = new AtomicReference<>("/");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // ==========================================
        // MODE FULLSCREEN IMMERSIF + ACTION BAR
        // Supprime toute barre système et action bar
        // ==========================================
        
        // Supprimer l'action bar (barre blanche avec nom appli)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        Window window = getWindow();
        
        // Rendre la barre de statut et de navigation transparentes
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        // Configuration moderne pour Android 11+ (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            if (controller != null) {
                // Masquer la barre de statut ET navigation
                controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
                // Comportement immersif : réaffiche au swipe, puis se cache
                controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            // Pour Android 10 et antérieurs (API < 30)
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
        
        // Enregistrer les plugins avant super.onCreate
        registerPlugin(AdMobPlugin.class);
        registerPlugin(DragLauncherPlugin.class);
        registerPlugin(ImmobilierLauncherPlugin.class);
        super.onCreate(savedInstanceState);
        // Overlay de chargement supprimé - l'app démarre directement

        // ==============================
        // Injection d'authentification
        // ==============================
        // Objectif: si un token a déjà été stocké côté natif (SharedPreferences
        // 'CapacitorStorage'), on le propage dans:
        // - les cookies pour l'API Render (si celle-ci utilise les cookies)
        // - le localStorage (hm-token/HM_TOKEN) pour les appels fetch côté web
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
            String token = prefs.getString("HM_TOKEN", null);
            if (token == null) token = prefs.getString("hm-token", null);
            if (token == null) token = prefs.getString("auth_token", null);

            // Propager en cookie (third-party cookies requis pour fetch vers domaine externe)
            if (!TextUtils.isEmpty(token)) {
                final String apiBase = "https://server-jeux-millionnaire.onrender.com";
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                // Autoriser les cookies tiers pour la WebView principale de Capacitor
                Bridge bridge = getBridge();
                if (bridge != null && bridge.getWebView() != null) {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(bridge.getWebView(), true);
                }
                // Déposer plusieurs clés courantes par sécurité (le serveur utilisera celle attendue)
                cookieManager.setCookie(apiBase, "hm-token=" + token + "; Path=/; SameSite=None; Secure");
                cookieManager.setCookie(apiBase, "token=" + token + "; Path=/; SameSite=None; Secure");
                cookieManager.flush();

                // Propager dans localStorage côté WebView (utilisé par le client Next.js)
                final String js = "try{localStorage.setItem('hm-token', '" + escapeJs(token) + "');localStorage.setItem('HM_TOKEN','" + escapeJs(token) + "');}catch(e){}";
                // Optionnel: session
                final String sessionJson = prefs.getString("hm-session", null);
                final String jsSession = sessionJson != null
                    ? ("try{localStorage.setItem('hm-session', '" + escapeJs(sessionJson) + "');}catch(e){}"): null;

                Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(() -> {
                    if (getBridge() != null && getBridge().getWebView() != null) {
                        getBridge().getWebView().evaluateJavascript(js, null);
                        if (jsSession != null) {
                            getBridge().getWebView().evaluateJavascript(jsSession, null);
                        }
                        // Injecte un hook léger pour suivre le pathname courant côté JS
                        // Ceci met à jour périodiquement lastKnownPath pour accélérer la résolution de la page actuelle lors des swipes
                        final String trackPathJs = "try{window.__hmTrackPathInterval&&clearInterval(window.__hmTrackPathInterval);window.__hmTrackPathInterval=setInterval(function(){try{var p=location.pathname||'/';if(window.AndroidInterface&&window.AndroidInterface.updatePath){window.AndroidInterface.updatePath(p);}else{if(!window.__lastPathSent||window.__lastPathSent!==p){window.__lastPathSent=p;}}}catch(e){}},800);}catch(e){}";
                        getBridge().getWebView().evaluateJavascript(trackPathJs, null);

                        // Optionnel: masquer un éventuel menu flottant web en bas à droite
                        // Décommentez le bloc ci-dessous si le menu est encore présent côté web.
                        // String hideCss = "try{var css='*{touch-action:pan-y} .fixed.bottom-4.right-4, .fixed.bottom-3.right-3, #nav-fab{display:none!important;}';"
                        //     + "var s=document.createElement('style');s.innerHTML=css;document.head.appendChild(s);}catch(e){}";
                        // getBridge().getWebView().evaluateJavascript(hideCss, null);
                    }
                }, 400);
            }
        } catch (Throwable t) {
            // Pas bloquant, on laisse l'app continuer
        }

        // Active la navigation par swipe (gauche/droite) entre les volets
        setupSwipeNavigation();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Forcer le fullscreen au retour de l'app
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Si on a été réveillé avec une intention ciblant un onglet, applique la navigation
        String path = null;
        try { path = getIntent() != null ? getIntent().getStringExtra("targetPath") : null; } catch (Throwable ignored) {}
        if (path != null && path.length() > 0) {
            // Nettoie l’extra pour éviter de re-naviguer en boucle
            getIntent().removeExtra("targetPath");
            navigateToPath(path);
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String path = null;
        try { path = intent != null ? intent.getStringExtra("targetPath") : null; } catch (Throwable ignored) {}
        if (path != null && path.length() > 0) {
            navigateToPath(path);
        }
    }

    // --- Navigation par swipe: gestionnaire de gestes + helpers ---
    private void setupSwipeNavigation() {
        final Bridge bridge = getBridge();
        if (bridge == null || bridge.getWebView() == null) return;

        // Détecteur de gestes pour identifier les flings horizontaux
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;      // distance minimale en px
            private static final int SWIPE_VELOCITY_THRESHOLD = 100; // vitesse minimale

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        // Swipe gauche → page suivante
                        navigateRelative(+1);
                    } else {
                        // Swipe droite → page précédente
                        navigateRelative(-1);
                    }
                    return true;
                }
                return false;
            }
        });

        // Attache l'écouteur de touch au WebView de Capacitor
        bridge.getWebView().setOnTouchListener((v, event) -> gestureDetector != null && gestureDetector.onTouchEvent(event));
    }

    // Calcule l'index courant à partir du pathname et navigue d'un offset relatif (-1/+1)
    private void navigateRelative(int delta) {
        final Bridge bridge = getBridge();
        if (bridge == null || bridge.getWebView() == null) return;

        // Récupère le pathname courant côté JS pour déterminer la page actuelle
        bridge.getWebView().evaluateJavascript("(function(){return location.pathname||'/';})()", value -> {
            String path = cleanJsString(value);
            if (path == null || path.isEmpty()) path = "/";
            lastKnownPath.set(path);
            int idx = indexOfPath(path);
            int next = ((idx + delta) % PAGES.length + PAGES.length) % PAGES.length; // boucle circulaire
            navigateToPath(PAGES[next]);
        });
    }

    // Navigue vers un chemin précis (ex: "/immobilier") en conservant la base client
    private void navigateToPath(String path) {
        final Bridge bridge = getBridge();
        if (bridge == null || bridge.getWebView() == null) return;
        if (path == null || path.length() == 0) path = "/";
        final String url = (path.startsWith("http")) ? path : (CLIENT_BASE + path);
        bridge.getWebView().evaluateJavascript("window.location.assign('" + escapeJs(url) + "')", null);
    }

    // Retourne l'index de PAGES correspondant au pathname courant
    private int indexOfPath(String path) {
        if (path == null) return 0;
        for (int i = 0; i < PAGES.length; i++) {
            if ("/".equals(PAGES[i])) {
                if ("/".equals(path)) return i;
            } else if (path.startsWith(PAGES[i])) {
                return i;
            }
        }
        return 0;
    }

    // Nettoie une chaîne renvoyée par evaluateJavascript (souvent entourée de quotes)
    private static String cleanJsString(String s) {
        if (s == null) return null;
        // Les résultats JS sont souvent comme "\"/immobilier\"" ou ""/immobilier"" selon versions
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\n", "").replace("\\r", "");
    }
    // Helpers simples pour échapper les chaînes insérées dans du JS
    private static String escapeJs(String s) {
        if (s == null) return "";
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}
