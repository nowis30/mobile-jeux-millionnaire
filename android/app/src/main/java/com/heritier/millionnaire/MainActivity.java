package com.heritier.millionnaire;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.text.TextUtils;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Bridge;

public class MainActivity extends BridgeActivity {

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
                    }
                }, 400);
            }
        } catch (Throwable t) {
            // Pas bloquant, on laisse l'app continuer
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Forcer le fullscreen au retour de l'app
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
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
