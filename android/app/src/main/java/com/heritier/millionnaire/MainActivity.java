package com.heritier.millionnaire;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import java.util.ArrayList;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Activer Edge-to-Edge pour compatibilité Android 15+ (SDK 35)
        // Assure la rétrocompatibilité avec les versions antérieures
        EdgeToEdge.enable(this);

        // Forcer une barre de statut/navigation sombre avec icônes claires pour éviter les halos blancs
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
            insetsController.setAppearanceLightNavigationBars(false);
        }
        
        // Enregistrer les plugins avant super.onCreate
        registerPlugin(AdMobPlugin.class);
        registerPlugin(DragLauncherPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
