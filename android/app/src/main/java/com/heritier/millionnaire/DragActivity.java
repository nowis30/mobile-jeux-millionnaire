package com.heritier.millionnaire;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser AdMob
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob initialisé: " + initializationStatus.toString());
        });
        
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

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Charger le jeu de drag local depuis assets
        webView.loadUrl("file:///android_asset/public/drag/index.html");
        
        // Précharger un interstitiel pour la première course
        loadInterstitialAd();
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
            
            // Afficher un interstitiel (si cooldown respecté)
            runOnUiThread(() -> showInterstitialAd());
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
