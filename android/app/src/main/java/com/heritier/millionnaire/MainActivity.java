package com.heritier.millionnaire;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;
import java.util.ArrayList;
import android.widget.TextView;

public class MainActivity extends BridgeActivity {

    private View loadingOverlay;
    private TextView loadingSubtitle;

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
        setupLoadingOverlay();
        attachLoadingCallbacks();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Forcer le fullscreen au retour de l'app
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void setupLoadingOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        loadingOverlay = inflater.inflate(R.layout.loading_overlay, null);
        loadingSubtitle = loadingOverlay.findViewById(R.id.loadingSubtitle);
        addContentView(loadingOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void attachLoadingCallbacks() {
        WebView webView = getBridge().getWebView();
        webView.setWebViewClient(new BridgeWebViewClient(getBridge()) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showLoading("Connexion au serveur Render…");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideLoading();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                showLoading("Toujours en attente du serveur Render…");
            }
        });
    }

    private void showLoading(String message) {
        runOnUiThread(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.VISIBLE);
                if (loadingSubtitle != null) {
                    loadingSubtitle.setText(message);
                }
            }
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
            }
        });
    }
}
