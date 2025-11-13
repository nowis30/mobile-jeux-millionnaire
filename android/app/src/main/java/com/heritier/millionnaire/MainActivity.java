package com.heritier.millionnaire;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import com.getcapacitor.BridgeActivity;
import java.util.ArrayList;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Activer Edge-to-Edge pour compatibilité Android 15+ (SDK 35)
        // Assure la rétrocompatibilité avec les versions antérieures
        EdgeToEdge.enable(this);
        
        // Enregistrer les plugins avant super.onCreate
        registerPlugin(AdMobPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
