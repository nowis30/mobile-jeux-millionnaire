package com.heritier.millionnaire;

import android.app.Activity;
import android.util.Log;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Gestion du consentement via le SDK UMP.
 * Simplification: si le statut n'est pas OBTAINED on considère NPA (non personnalisé).
 */
public class ConsentManager {
    private static final String TAG = "ConsentManager";
    private ConsentInformation consentInformation;
    private boolean npa = false; // true = Non-Personalized Ads

    public interface ReadyCallback {
        void onReady(boolean npa);
    }

    public void request(Activity activity, ReadyCallback callback) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                // .setTagForUnderAgeOfConsent(false) // laisser par défaut
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        consentInformation.requestConsentInfoUpdate(activity, params, () -> {
            Log.d(TAG, "Consent info updated");
            if (consentInformation.isConsentFormAvailable()) {
                loadForm(activity, callback);
            } else {
                mapStatus();
                callback.onReady(npa);
            }
        }, formError -> {
            Log.e(TAG, "Consent info error: " + formError.getMessage());
            mapStatus();
            callback.onReady(npa);
        });
    }

    private void loadForm(Activity activity, ReadyCallback callback) {
        UserMessagingPlatform.loadConsentForm(activity, consentForm -> {
            if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(activity, formError -> {
                    if (formError != null) {
                        Log.w(TAG, "Form dismissed with error: " + formError.getMessage());
                    } else {
                        Log.d(TAG, "Form dismissed");
                    }
                    mapStatus();
                    callback.onReady(npa);
                });
            } else {
                mapStatus();
                callback.onReady(npa);
            }
        }, formError -> {
            Log.e(TAG, "Load form error: " + formError.getMessage());
            mapStatus();
            callback.onReady(npa);
        });
    }

    private void mapStatus() {
        if (consentInformation == null) return;
        int status = consentInformation.getConsentStatus();
        // Si obtenu => personnalisé, sinon => NPA
        npa = status != ConsentInformation.ConsentStatus.OBTAINED;
        Log.d(TAG, "Mapped consent status=" + status + " -> npa=" + npa);
    }

    public boolean isNpa() { return npa; }
}
