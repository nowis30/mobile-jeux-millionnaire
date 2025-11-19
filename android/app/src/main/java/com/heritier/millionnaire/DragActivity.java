package com.heritier.millionnaire;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.Gravity;
import android.webkit.CookieManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

/**
 * DragActivity - Écran plein écran du mini-jeu Drag Racing
 * 
 * INTÉGRATION ADMOB:
 * - Interstitiel affiché après chaque course (cooldown 60s)
 * - Banner optionnel en bas (désactivé par défaut pour ne pas gêner le gameplay)
 * 
 * CONFIGURATION REQUISE:
 * - Remplacer les IDs de test ci-dessous par vos vrais IDs AdMob
 * - Test Interstitial ID: ca-app-pub-3940256099942544/1033173712
 * - Test Banner ID: ca-app-pub-3940256099942544/6300978111
 * - Production: Remplacer par vos IDs depuis votre compte AdMob
 */
public class DragActivity extends AppCompatActivity {

    private static final String TAG = "DragActivity";
    
    // ========================================
    // ⚠️ CONFIGURATION ADMOB - À PERSONNALISER
    // ========================================
    
    // TODO: Remplacer par votre vrai ID Interstitial depuis AdMob
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7443046636998296/9246188831"; // PROD ID (aligné sur AdMobPlugin)
    
    // TODO: Remplacer par votre vrai ID Banner (optionnel)
    private static final String BANNER_AD_UNIT_ID = "VOTRE_ID_ADMOB_BANNIERE_ICI"; // PROD ID
    
    // Cooldown entre deux interstitiels (en millisecondes)
    private static final long AD_COOLDOWN_MS = 60000; // 60 secondes
    
    // Activer/désactiver la bannière (false par défaut pour ne pas gêner le jeu)
    private static final boolean ENABLE_BANNER = false;
    
    // ========================================
    
    private WebView webView;
    private InterstitialAd interstitialAd;
    private long lastInterstitialTime = 0;
    private boolean isLoadingInterstitial = false;
    private AdView bannerAdView;
    private View authOverlay;

    private static final String API_BASE = "https://server-jeux-millionnaire.onrender.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser AdMob
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob initialisé: " + initializationStatus.toString());
        });

        // Synchroniser immédiatement les données auth fournies par l'activité parent
        syncAuthFromIntent(getIntent());
        
        // ==========================================
        // MODE FULLSCREEN IMMERSIF POUR LE JEU
        // ==========================================
        
        Window window = getWindow();
        
        // Supprime toute barre système
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        // Configuration moderne pour Android 11+ (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            // Pour Android 10 et antérieurs
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        // Créer layout avec WebView + Banner optionnelle
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Créer et configurer WebView
        webView = new WebView(this);
        webView.setPadding(0, 0, 0, 0);
        
        FrameLayout.LayoutParams webViewParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        webView.setLayoutParams(webViewParams);
        rootLayout.addView(webView);

        // Banner publicitaire (optionnel, en bas)
        if (ENABLE_BANNER) {
            bannerAdView = new AdView(this);
            bannerAdView.setAdUnitId(BANNER_AD_UNIT_ID);
            bannerAdView.setAdSize(AdSize.BANNER);
            
            FrameLayout.LayoutParams bannerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            bannerParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
            bannerAdView.setLayoutParams(bannerParams);
            
            rootLayout.addView(bannerAdView);
            
            AdRequest bannerRequest = new AdRequest.Builder().build();
            bannerAdView.loadAd(bannerRequest);
            
            Log.d(TAG, "Bannière publicitaire chargée");
        }

        setContentView(rootLayout);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Interface JavaScript pour communication WebView ↔ Android
        webView.addJavascriptInterface(new DragBridge(), "AndroidDrag");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Injecter token/session dans localStorage côté page
                String token = readTokenFromPrefs();
                String sessionJson = readSessionFromPrefs();
                if (token != null) {
                    String js = "try{localStorage.setItem('hm-token','" + escapeJs(token) + "');localStorage.setItem('HM_TOKEN','" + escapeJs(token) + "');}catch(e){}";
                    view.evaluateJavascript(js, null);
                }
                if (sessionJson != null) {
                    String jsSess = "try{localStorage.setItem('hm-session','" + escapeJs(sessionJson) + "');}catch(e){}";
                    view.evaluateJavascript(jsSess, null);
                }
                // Option: ajouter un wrapper fetch pour attacher Authorization si présent
                if (token != null) {
                    String fetchWrap = "(function(){try{if(window.__hmFetchWrapped) return; window.__hmFetchWrapped=true; const _f=window.fetch; window.fetch=function(i,init){init=init||{}; init.headers=init.headers||{}; try{if(!init.headers['Authorization']){init.headers['Authorization']='Bearer " + escapeJs(token) + "';}}catch(e){}; init.credentials = init.credentials || 'include'; return _f(i,init);};}catch(e){}})();";
                    view.evaluateJavascript(fetchWrap, null);
                }
            }
        });
        // Debug WebView (devtools)
        try { WebView.setWebContentsDebuggingEnabled(true); } catch (Throwable ignored) {}
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "[WV] " + consoleMessage.message() + " @" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                return super.onConsoleMessage(consoleMessage);
            }
        });

        // Autoriser cookies tiers et déposer le cookie d'API si token présent
        try {
            CookieManager cm = CookieManager.getInstance();
            cm.setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            String token = readTokenFromPrefs();
            if (token != null && token.length() > 0) {
                cm.setCookie(API_BASE, "hm-token=" + token + "; Path=/; SameSite=None; Secure");
                cm.setCookie(API_BASE, "token=" + token + "; Path=/; SameSite=None; Secure");
                cm.flush();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Cookie setup failed: " + t.getMessage());
        }

        // Charger le jeu de drag local depuis assets
        webView.loadUrl("file:///android_asset/public/drag/index.html");
        
        // Ajouter un overlay si l'auth est absente/incorrecte
        ensureAuthOrPrompt(rootLayout);

        // Précharger un interstitiel pour la première course
        loadInterstitialAd();
    }

    private void syncAuthFromIntent(Intent intent) {
        if (intent == null) return;
        String token = intent.getStringExtra("authToken");
        String sessionJson = intent.getStringExtra("sessionJson");
        if (TextUtils.isEmpty(token) && TextUtils.isEmpty(sessionJson)) {
            Log.d(TAG, "Pas de données auth à synchroniser depuis l'intent");
            return;
        }
        try {
            SharedPreferences prefs = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (!TextUtils.isEmpty(token)) {
                editor.putString("HM_TOKEN", token);
                editor.putString("hm-token", token);
                editor.putString("auth_token", token);
                Log.d(TAG, "Token synchronisé depuis intent");
            }
            if (!TextUtils.isEmpty(sessionJson)) {
                editor.putString("hm-session", sessionJson);
                Log.d(TAG, "Session synchronisée depuis intent");
            }
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur sync auth intent: " + e.getMessage());
        }
    }

    private String readTokenFromPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
            String token = prefs.getString("HM_TOKEN", null);
            if (token == null) token = prefs.getString("hm-token", null);
            if (token == null) token = prefs.getString("auth_token", null);
            return token;
        } catch (Exception e) {
            return null;
        }
    }

    private String readSessionFromPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
            return prefs.getString("hm-session", null);
        } catch (Exception e) { return null; }
    }

    private void ensureAuthOrPrompt(FrameLayout rootLayout) {
        String token = readTokenFromPrefs();
        if (token != null && token.length() > 10) {
            // Auth ok → rien à afficher
            return;
        }
        // Construire un overlay simple avec message + actions
        LinearLayout overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setBackgroundColor(0xCC000000);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        overlay.setLayoutParams(lp);
        overlay.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Connexion requise");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);

        TextView msg = new TextView(this);
        msg.setText("Aucun utilisateur connecté ou session expirée.\nVous pouvez retourner à l'accueil ou vous reconnecter.");
        msg.setTextColor(0xFFDDDDDD);
        msg.setTextSize(14);
        msg.setGravity(Gravity.CENTER);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);

        Button backBtn = new Button(this);
        backBtn.setText("Retour accueil");
        backBtn.setOnClickListener(v -> finish());

        Button loginBtn = new Button(this);
        loginBtn.setText("Se reconnecter");
        loginBtn.setOnClickListener(v -> {
            try {
                // Ouvrir l'activité principale (web) pour login
                Intent i = new Intent(DragActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            } catch (Exception e) {
                finish();
            }
        });

        int pad = (int) (getResources().getDisplayMetrics().density * 16);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, pad, pad, pad);
        container.setGravity(Gravity.CENTER);

        container.addView(title);
        View spacer = new View(this); spacer.setMinimumHeight(pad/2); container.addView(spacer);
        container.addView(msg);
        View spacer2 = new View(this); spacer2.setMinimumHeight(pad); container.addView(spacer2);
        buttons.addView(backBtn);
        View spacer3 = new View(this); spacer3.setMinimumWidth(pad); buttons.addView(spacer3);
        buttons.addView(loginBtn);
        container.addView(buttons);

        overlay.addView(container);
        rootLayout.addView(overlay);
        authOverlay = overlay;
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Interface JavaScript exposée au jeu de drag
     * Permet au JavaScript d'appeler des méthodes Android
     */
    private class DragBridge {
        
        /**
         * Appelée par le JavaScript à la fin d'une course
         * Usage depuis le jeu: window.AndroidDrag.onRaceFinished(win, elapsedMs)
         */
        @JavascriptInterface
        public void onRaceFinished(boolean playerWon, int elapsedMs) {
            Log.d(TAG, "Course terminée: " + (playerWon ? "VICTOIRE" : "DÉFAITE") + " en " + elapsedMs + "ms");
            
            // Pub interstitielle désactivée après course pour meilleure expérience utilisateur
            // runOnUiThread(() -> showInterstitialAd());
        }
        
        /**
         * Permet au JavaScript de vérifier si une pub est prête
         * Usage: window.AndroidDrag.isAdReady()
         */
        @JavascriptInterface
        public boolean isAdReady() {
            return interstitialAd != null;
        }
        
        /**
         * Log depuis le JavaScript
         * Usage: window.AndroidDrag.log("Message de debug")
         */
        @JavascriptInterface
        public void log(String message) {
            Log.d(TAG, "[JS] " + message);
        }
        
        /**
         * Récupère le token d'authentification depuis SharedPreferences
         * Usage: window.AndroidDrag.getAuthToken()
         */
        @JavascriptInterface
        public String getAuthToken() {
            try {
                android.content.SharedPreferences prefs = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
                // Essayer différentes clés possibles
                String token = prefs.getString("HM_TOKEN", null);
                if (token == null) {
                    token = prefs.getString("hm-token", null);
                }
                if (token == null) {
                    token = prefs.getString("auth_token", null);
                }
                Log.d(TAG, "Token récupéré: " + (token != null ? "présent" : "absent"));
                return token;
            } catch (Exception e) {
                Log.e(TAG, "Erreur récupération token: " + e.getMessage());
                return null;
            }
        }
        
        /**
         * Récupère les données de session (gameId, playerId, etc.)
         * Usage: window.AndroidDrag.getSessionData()
         * Retourne JSON string: {"gameId": "...", "playerId": "...", "nickname": "..."}
         */
        @JavascriptInterface
        public String getSessionData() {
            try {
                android.content.SharedPreferences prefs = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
                String sessionJson = prefs.getString("hm-session", null);
                if (sessionJson != null) {
                    Log.d(TAG, "Session récupérée depuis storage");
                    return sessionJson;
                }
                Log.d(TAG, "Aucune session trouvée");
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Erreur récupération session: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Charge un nouvel interstitiel
     * Appelée automatiquement après l'affichage du précédent
     */
    private void loadInterstitialAd() {
        if (isLoadingInterstitial) {
            Log.d(TAG, "Chargement d'interstitiel déjà en cours, skip");
            return;
        }
        
        isLoadingInterstitial = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, INTERSTITIAL_AD_UNIT_ID, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    DragActivity.this.interstitialAd = interstitialAd;
                    isLoadingInterstitial = false;
                    Log.d(TAG, "Interstitiel chargé avec succès");
                    
                    // Configurer les callbacks
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitiel fermé par l'utilisateur");
                            DragActivity.this.interstitialAd = null;
                            // Précharger le prochain
                            loadInterstitialAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Échec affichage interstitiel: " + adError.getMessage());
                            DragActivity.this.interstitialAd = null;
                            // Réessayer de charger
                            loadInterstitialAd();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitiel affiché à l'écran");
                            lastInterstitialTime = System.currentTimeMillis();
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.e(TAG, "Échec chargement interstitiel: " + loadAdError.getMessage());
                    interstitialAd = null;
                    isLoadingInterstitial = false;
                    // Réessayer dans 10 secondes
                    webView.postDelayed(() -> loadInterstitialAd(), 10000);
                }
            });
    }

    /**
     * Affiche l'interstitiel si les conditions sont remplies:
     * - Un interstitiel est chargé
     * - Le cooldown est respecté (60s par défaut)
     */
    private void showInterstitialAd() {
        long now = System.currentTimeMillis();
        long timeSinceLastAd = now - lastInterstitialTime;
        
        if (interstitialAd != null) {
            if (timeSinceLastAd >= AD_COOLDOWN_MS) {
                interstitialAd.show(this);
                Log.d(TAG, "Affichage de l'interstitiel");
            } else {
                long remaining = (AD_COOLDOWN_MS - timeSinceLastAd) / 1000;
                Log.d(TAG, "Cooldown actif, prochain interstitiel dans " + remaining + "s");
                // Précharger pour la prochaine fois
                if (interstitialAd == null && !isLoadingInterstitial) {
                    loadInterstitialAd();
                }
            }
        } else {
            Log.d(TAG, "Aucun interstitiel prêt, chargement en cours...");
            if (!isLoadingInterstitial) {
                loadInterstitialAd();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Reprendre la bannière si activée
        if (bannerAdView != null) {
            bannerAdView.resume();
        }
        
        // Rétablir l'immersion si l'utilisateur navigue ailleurs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
    
    @Override
    protected void onPause() {
        if (bannerAdView != null) {
            bannerAdView.pause();
        }
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }
}
