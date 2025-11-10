package com.heritier.millionnaire;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import java.util.ArrayList;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Enregistrer les plugins avant super.onCreate
        registerPlugin(AdMobPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
