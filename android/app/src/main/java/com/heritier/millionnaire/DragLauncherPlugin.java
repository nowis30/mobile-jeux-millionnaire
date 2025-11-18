package com.heritier.millionnaire;

import android.content.Intent;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "DragLauncher")
public class DragLauncherPlugin extends Plugin {

    private static final int REQ_DRAG = 10101;
    private PluginCall pendingCall;

    /**
     * Ouvre simplement l'écran natif (sans résultat vers JS). Backward compatible.
     */
    @PluginMethod
    public void open(PluginCall call) {
        try {
            Intent intent = new Intent(getActivity(), DragActivity.class);
            getActivity().startActivity(intent);
            JSObject result = new JSObject();
            result.put("success", true);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Erreur lors de l'ouverture du drag", e);
        }
    }

    /**
     * Lance une course et renvoie le résultat au JS: { win, elapsedMs, reward }
     */
    @PluginMethod
    public void race(PluginCall call) {
        try {
            if (pendingCall != null) {
                call.reject("Une course est déjà en cours");
                return;
            }
            Intent intent = new Intent(getActivity(), DragActivity.class);
            pendingCall = call;
            getActivity().startActivityForResult(intent, REQ_DRAG);
        } catch (Exception e) {
            call.reject("Erreur de lancement de course", e);
        }
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DRAG && pendingCall != null) {
            PluginCall call = pendingCall;
            pendingCall = null;
            if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                boolean win = data.getBooleanExtra("win", false);
                int elapsedMs = data.getIntExtra("elapsedMs", 0);
                int reward = data.getIntExtra("reward", 0);
                JSObject res = new JSObject();
                res.put("win", win);
                res.put("elapsedMs", elapsedMs);
                res.put("reward", reward);
                call.resolve(res);
            } else {
                call.reject("Course annulée");
            }
        }
    }
}
