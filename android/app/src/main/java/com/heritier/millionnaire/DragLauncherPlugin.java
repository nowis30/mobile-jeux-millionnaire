package com.heritier.millionnaire;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "DragLauncher")
public class DragLauncherPlugin extends Plugin {

    private static final int REQ_DRAG = 10101;
    private PluginCall pendingCall;
    private static final String TAG = "DragLauncher";

    /**
     * Ouvre simplement l'écran natif (sans résultat vers JS). Backward compatible.
     */
    @PluginMethod
    public void open(PluginCall call) {
        try {
            Log.d(TAG, "open() invoked");
            Intent intent = new Intent(getActivity(), DragActivity.class);
            attachAuthExtras(intent, call);
            getActivity().startActivity(intent);
            JSObject result = new JSObject();
            result.put("success", true);
            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Erreur ouverture drag", e);
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
                Log.w(TAG, "race() rejected: already pending");
                call.reject("Une course est déjà en cours");
                return;
            }
            Log.d(TAG, "race() starting activity");
            Intent intent = new Intent(getActivity(), DragActivity.class);
            attachAuthExtras(intent, call);
            pendingCall = call;
            getActivity().startActivityForResult(intent, REQ_DRAG);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lancement course", e);
            call.reject("Erreur de lancement de course", e);
        }
    }

    private void attachAuthExtras(Intent intent, PluginCall call) {
        if (intent == null || call == null) return;
        String token = call.getString("token");
        String sessionJson = call.getString("sessionJson");
        persistAuthData(token, sessionJson);
        Log.d(TAG, "attachAuthExtras tokenLen=" + (token == null ? 0 : token.length()) + " sessionJsonPresent=" + (!TextUtils.isEmpty(sessionJson)));
        if (!TextUtils.isEmpty(token)) {
            intent.putExtra("authToken", token);
        }
        if (!TextUtils.isEmpty(sessionJson)) {
            intent.putExtra("sessionJson", sessionJson);
        }
    }

    private void persistAuthData(String token, String sessionJson) {
        if (TextUtils.isEmpty(token) && TextUtils.isEmpty(sessionJson)) {
            Log.d(TAG, "persistAuthData: nothing to persist");
            return;
        }
        Context context = getContext();
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (!TextUtils.isEmpty(token)) {
            editor.putString("HM_TOKEN", token);
            editor.putString("hm-token", token);
            editor.putString("auth_token", token);
            Log.d(TAG, "persistAuthData: token persisted (length=" + token.length() + ")");
        }
        if (!TextUtils.isEmpty(sessionJson)) {
            editor.putString("hm-session", sessionJson);
            Log.d(TAG, "persistAuthData: session persisted (chars=" + sessionJson.length() + ")");
        }
        editor.apply();
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
                Log.d(TAG, "race result win=" + win + " elapsedMs=" + elapsedMs + " reward=" + reward);
                JSObject res = new JSObject();
                res.put("win", win);
                res.put("elapsedMs", elapsedMs);
                res.put("reward", reward);
                call.resolve(res);
            } else {
                Log.w(TAG, "race canceled or no data (resultCode=" + resultCode + ")");
                call.reject("Course annulée");
            }
        }
    }

    @Override
    protected void handleOnPause() {
        super.handleOnPause();
        // Réinitialiser le pendingCall si l'activité est mise en pause sans résultat
        if (pendingCall != null) {
            Log.w(TAG, "handleOnPause: pending race interrupted");
            pendingCall.reject("Course interrompue");
            pendingCall = null;
        }
    }
}
