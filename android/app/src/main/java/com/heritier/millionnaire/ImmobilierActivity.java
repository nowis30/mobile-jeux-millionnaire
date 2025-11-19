package com.heritier.millionnaire;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class ImmobilierActivity extends AppCompatActivity {
    private static final String TAG = "ImmobilierActivity";
    private static final String API_BASE = "https://server-jeux-millionnaire.onrender.com";
    private static final String CLIENT_BASE = "https://client-jeux-millionnaire.vercel.app";
    private static final String DEFAULT_URL = CLIENT_BASE + "/immobilier";

    private WebView webView;
    private View authOverlay;
    private boolean triedImmobilierFallback = false;
    private boolean triedMenuFallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveFullscreen();

        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        root.addView(webView);
        setContentView(root);

        setupWebView(webView);
        setupCookies();
        setupDebugging();

        String startUrl = getIntent() != null ? getIntent().getStringExtra("startUrl") : null;
        String target = DEFAULT_URL;
        if (!TextUtils.isEmpty(startUrl)) {
            if (startUrl.startsWith("http://") || startUrl.startsWith("https://")) {
                target = startUrl;
            } else {
                if (startUrl.startsWith("/")) target = CLIENT_BASE + startUrl;
                else target = CLIENT_BASE + "/" + startUrl;
            }
        }
        webView.loadUrl(target);

        ensureAuthOrPrompt(root);
    }

    private void enableImmersiveFullscreen() {
        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            WindowInsetsControllerCompat c = WindowCompat.getInsetsController(window, window.getDecorView());
            if (c != null) {
                c.hide(WindowInsetsCompat.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView wv) {
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setMediaPlaybackRequiresUserGesture(false);

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String tok = readToken();
                if (!TextUtils.isEmpty(tok)) {
                    String js = "try{localStorage.setItem('hm-token','" + escapeJs(tok) + "');localStorage.setItem('HM_TOKEN','" + escapeJs(tok) + "');}catch(e){}";
                    view.evaluateJavascript(js, null);
                    String wrap = "(function(){try{if(window.__hmFetchWrapped)return;window.__hmFetchWrapped=true;const _f=window.fetch;window.fetch=function(i,init){init=init||{};init.headers=init.headers||{};try{if(!init.headers['Authorization']){init.headers['Authorization']='Bearer " + escapeJs(tok) + "';}}catch(e){};init.credentials=init.credentials||'include';return _f(i,init);};}catch(e){}})();";
                    view.evaluateJavascript(wrap, null);
                }
                String sess = readSession();
                if (!TextUtils.isEmpty(sess)) {
                    String jsS = "try{localStorage.setItem('hm-session','" + escapeJs(sess) + "');}catch(e){}";
                    view.evaluateJavascript(jsS, null);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                try {
                    // Only act on main frame 404s
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (request != null && !request.isForMainFrame()) return;
                    }
                    int status = errorResponse != null ? errorResponse.getStatusCode() : -1;
                    if (status == 404) {
                        Log.w(TAG, "HTTP 404 détecté, tentative de fallback...");
                        // 1) Tenter /immobilier si pas déjà fait
                        if (!triedImmobilierFallback) {
                            triedImmobilierFallback = true;
                            String url = CLIENT_BASE + "/immobilier";
                            Log.w(TAG, "Fallback vers: " + url);
                            view.loadUrl(url);
                            return;
                        }
                        // 2) Puis tenter /immobilier/menu
                        if (!triedMenuFallback) {
                            triedMenuFallback = true;
                            String url = CLIENT_BASE + "/immobilier/menu";
                            Log.w(TAG, "Fallback vers: " + url);
                            view.loadUrl(url);
                            return;
                        }
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "onReceivedHttpError handling failed: " + t.getMessage());
                }
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                try {
                    Log.w(TAG, "WebView error (" + errorCode + "): " + description + " url=" + failingUrl);
                    // Ancienne API de fallback générique
                    if (!triedImmobilierFallback) {
                        triedImmobilierFallback = true;
                        String url = CLIENT_BASE + "/immobilier";
                        Log.w(TAG, "Fallback (legacy) vers: " + url);
                        view.loadUrl(url);
                        return;
                    }
                    if (!triedMenuFallback) {
                        triedMenuFallback = true;
                        String url = CLIENT_BASE + "/immobilier/menu";
                        Log.w(TAG, "Fallback (legacy) vers: " + url);
                        view.loadUrl(url);
                        return;
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "onReceivedError handling failed: " + t.getMessage());
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
    }

    private void setupCookies() {
        try {
            CookieManager cm = CookieManager.getInstance();
            cm.setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            String token = readToken();
            if (!TextUtils.isEmpty(token)) {
                cm.setCookie(API_BASE, "hm-token=" + token + "; Path=/; SameSite=None; Secure");
                cm.setCookie(API_BASE, "token=" + token + "; Path=/; SameSite=None; Secure");
                cm.flush();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Cookie setup failed: " + t.getMessage());
        }
    }

    private void setupDebugging() {
        try { WebView.setWebContentsDebuggingEnabled(true); } catch (Throwable ignored) {}
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "[WV] " + consoleMessage.message() +
                        " @" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                return super.onConsoleMessage(consoleMessage);
            }
        });
    }

    private String readToken() {
        try {
            android.content.SharedPreferences p = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
            String t = p.getString("HM_TOKEN", null);
            if (t == null) t = p.getString("hm-token", null);
            if (t == null) t = p.getString("auth_token", null);
            return t;
        } catch (Exception e) { return null; }
    }

    private String readSession() {
        try {
            android.content.SharedPreferences p = getSharedPreferences("CapacitorStorage", MODE_PRIVATE);
            return p.getString("hm-session", null);
        } catch (Exception e) { return null; }
    }

    private void ensureAuthOrPrompt(FrameLayout root) {
        String token = readToken();
        if (!TextUtils.isEmpty(token) && token.length() > 10) return;

        LinearLayout overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setBackgroundColor(0xCC000000);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        overlay.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Connexion requise");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22f);
        title.setGravity(Gravity.CENTER);

        TextView msg = new TextView(this);
        msg.setText("Impossible d'ouvrir l'Immobilier sans session.\nRetournez à l'accueil ou reconnectez-vous.");
        msg.setTextColor(0xFFDDDDDD);
        msg.setTextSize(14f);
        msg.setGravity(Gravity.CENTER);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        Button back = new Button(this);
        back.setText("Retour accueil");
        back.setOnClickListener(v -> finish());

        Button login = new Button(this);
        login.setText("Se reconnecter");
        login.setOnClickListener(v -> {
            try { startActivity(new Intent(ImmobilierActivity.this, MainActivity.class)); } catch (Exception ignored) {}
            finish();
        });

        int pad = (int)(getResources().getDisplayMetrics().density * 16);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(pad, pad, pad, pad);
        col.setGravity(Gravity.CENTER);

        View spacer1 = new View(this); spacer1.setMinimumHeight(pad / 2);
        View spacer2 = new View(this); spacer2.setMinimumHeight(pad);
        View spacer3 = new View(this); spacer3.setMinimumWidth(pad);

        col.addView(title);
        col.addView(spacer1);
        col.addView(msg);
        col.addView(spacer2);
        row.addView(back);
        row.addView(spacer3);
        row.addView(login);
        col.addView(row);

        overlay.addView(col);
        root.addView(overlay);
        authOverlay = overlay;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

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
