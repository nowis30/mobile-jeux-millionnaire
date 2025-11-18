package com.heritier.millionnaire;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Random;

/**
 * Activité native simple pour une course de drag.
 * - 2 voitures (joueur vs IA)
 * - Compteur 3-2-1-GO puis tap pour partir
 * - Animation simple sur l'axe X
 * - Pas de pub, retour direct après la course
 *
 * RÉSULTAT: setResult(RESULT_OK) avec extras: win:boolean, elapsedMs:int, reward:int
 */
public class DragRaceNativeActivity extends AppCompatActivity {
    private static final String TAG = "DragRaceNative";

    private ImageView carPlayer;
    private ImageView carOpponent;
    private View track;
    private Button startBtn;
    private TextView statusTxt;

    private boolean raceStarted = false;
    private boolean raceFinished = false;
    private long goTimestamp = 0L;

    private final Random rnd = new Random();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_race_native);

        // Fullscreen immersif
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
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

        carPlayer = findViewById(R.id.car_player);
        carOpponent = findViewById(R.id.car_opponent);
        track = findViewById(R.id.track);
        startBtn = findViewById(R.id.btn_start);
        statusTxt = findViewById(R.id.status_text);

        statusTxt.setText("Appuyez sur DÉMARRER");

        startBtn.setOnClickListener(v -> {
            if (raceStarted || raceFinished) return;
            startBtn.setEnabled(false);
            startBtn.setVisibility(View.GONE);
            startCountdown();
        });

        // Tap sur la piste = tentative de départ du joueur
        track.setOnClickListener(v -> onPlayerTap());
    }

    private void startCountdown() {
        statusTxt.setText("3");
        track.postDelayed(() -> {
            statusTxt.setText("2");
            track.postDelayed(() -> {
                statusTxt.setText("1");
                track.postDelayed(() -> {
                    statusTxt.setText("GO!");
                    goTimestamp = System.currentTimeMillis();
                    raceStarted = true;
                }, 1000);
            }, 1000);
        }, 1000);
    }

    private void onPlayerTap() {
        if (!raceStarted || raceFinished) {
            // Tap avant le GO ou après la fin = ignoré
            if (!raceStarted && !raceFinished) {
                statusTxt.setText("⚠️ Faux départ!");
                track.postDelayed(() -> {
                    endRace(false, 0);
                }, 1500);
                raceFinished = true;
            }
            return;
        }

        raceFinished = true;
        long now = System.currentTimeMillis();
        int reaction = (int) Math.max(1, now - goTimestamp); // ms

        // Paramètres simples: vitesse voiture = distance/durée
        int trackWidth = track.getWidth();
        int travel = Math.max(1, trackWidth - carPlayer.getWidth() - dp(16));

        int playerDuration = Math.max(600, 1600 - reaction); // meilleure réaction => plus rapide
        int opponentDuration = 900 + rnd.nextInt(700); // 0.9s à 1.6s

        // Animer les voitures
        animateCar(carPlayer, travel, playerDuration);
        animateCar(carOpponent, travel, opponentDuration);

        boolean win = playerDuration <= opponentDuration;
        int elapsedMs = playerDuration;
        
        // Afficher résultat avec délai
        track.postDelayed(() -> {
            statusTxt.setText(win ? "🏆 VICTOIRE!" : "😢 Défaite");
            track.postDelayed(() -> {
                endRace(win, elapsedMs);
            }, 2000);
        }, Math.max(playerDuration, opponentDuration));
    }

    private void animateCar(View car, int distancePx, int durationMs) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(car, "translationX", 0f, distancePx);
        anim.setDuration(durationMs);
        anim.start();
    }

    private void endRace(boolean win, int elapsedMs) {
        // Récompense locale simple (à intégrer côté serveur via plugin si besoin)
        int reward = win ? 5000 : 1000;
        
        // Retour immédiat, pas de pub
        finishWithResult(win, elapsedMs, reward);
    }

    private void finishWithResult(boolean win, int elapsedMs, int reward) {
        getIntent().putExtra("win", win);
        getIntent().putExtra("elapsedMs", elapsedMs);
        getIntent().putExtra("reward", reward);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
