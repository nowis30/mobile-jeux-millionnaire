package com.heritier.millionnaire;

import android.content.Intent;
import android.util.Log;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "ImmobilierLauncher")
public class ImmobilierLauncherPlugin extends Plugin {
    private static final String TAG = "ImmoLauncher";

    @PluginMethod
    public void open(PluginCall call) {
        try {
            String startUrl = call.getString("startUrl");
            Log.d(TAG, "open() startUrl=" + startUrl);
            // Redirige désormais vers MainActivity et lui indique la page cible
            Intent i = new Intent();
            i.setClassName(getContext(), "com.heritier.millionnaire.MainActivity");
            // On passe une cible au format chemin (ex: "/immobilier")
            String targetPath = "/immobilier";
            if (startUrl != null && startUrl.length() > 0) {
                // si on reçoit une URL absolue, on laisse MainActivity la charger telle quelle
                targetPath = startUrl;
            }
            i.putExtra("targetPath", targetPath);
            getActivity().startActivity(i);
            JSObject res = new JSObject();
            res.put("success", true);
            call.resolve(res);
        } catch (Exception e) {
            call.reject("Erreur ouverture Immobilier", e);
        }
    }
}
