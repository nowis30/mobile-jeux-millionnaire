package com.heritier.millionnaire;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.content.pm.ApplicationInfo;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import androidx.annotation.NonNull;

@CapacitorPlugin(name = "AdMob")
public class AdMobPlugin extends Plugin {
    private static final String TAG = "AdMobPlugin";
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private boolean isAdLoading = false;
    private boolean isRewardedAdLoading = false;
    private boolean npa = false; // Non-Personalized Ads flag
    private ConsentInformation consentInformation;
    
    // IDs de production
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7443046636998296/9246188831";
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-7443046636998296/3604239096";

    // IDs de test Google (ne diffusent que des pubs de test)
    private static final String INTERSTITIAL_TEST_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String REWARDED_TEST_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    private boolean isDebuggable() {
        Activity activity = getActivity();
        if (activity == null) return false;
        ApplicationInfo appInfo = activity.getApplicationInfo();
        return (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private String getInterstitialUnitId() {
        // En debug, toujours utiliser les ad units de test pour éviter les blocages
        return isDebuggable() ? INTERSTITIAL_TEST_UNIT_ID : INTERSTITIAL_AD_UNIT_ID;
    }

    private String getRewardedUnitId() {
        return isDebuggable() ? REWARDED_TEST_UNIT_ID : REWARDED_AD_UNIT_ID;
    }
    
    @PluginMethod
    public void initialize(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }
        
        activity.runOnUiThread(() -> {
            MobileAds.initialize(activity, initializationStatus -> {
                Log.d(TAG, "AdMob initialized");
                call.resolve();
            });
        });
    }

    /**
     * Demander le consentement via UMP et renseigner le flag NPA.
     * Résout avec { npa: boolean }
     */
    @PluginMethod
    public void requestConsent(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }

        activity.runOnUiThread(() -> {
            ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
            consentInformation = UserMessagingPlatform.getConsentInformation(activity);
            consentInformation.requestConsentInfoUpdate(activity, params,
                () -> {
                    if (consentInformation.isConsentFormAvailable()) {
                        UserMessagingPlatform.loadConsentForm(activity, consentForm -> {
                            if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                                consentForm.show(activity, formError -> {
                                    mapConsentToNpa();
                                    resolveNpa(call);
                                });
                            } else {
                                mapConsentToNpa();
                                resolveNpa(call);
                            }
                        }, formError -> {
                            Log.e(TAG, "Load form error: " + formError.getMessage());
                            mapConsentToNpa();
                            resolveNpa(call);
                        });
                    } else {
                        mapConsentToNpa();
                        resolveNpa(call);
                    }
                },
                formError -> {
                    Log.e(TAG, "Consent info error: " + formError.getMessage());
                    mapConsentToNpa();
                    resolveNpa(call);
                }
            );
        });
    }

    private void mapConsentToNpa() {
        if (consentInformation == null) { npa = true; return; }
        int status = consentInformation.getConsentStatus();
        // Si status obtenu => personnalisé (npa=false), sinon par défaut npa=true
        npa = status != ConsentInformation.ConsentStatus.OBTAINED;
        Log.d(TAG, "Consent status=" + status + " => npa=" + npa);
    }

    private void resolveNpa(PluginCall call) {
        com.getcapacitor.JSObject obj = new com.getcapacitor.JSObject();
        obj.put("npa", npa);
        call.resolve(obj);
    }
    
    @PluginMethod
    public void loadInterstitial(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }
        
        if (isAdLoading) {
            call.reject("Ad is already loading");
            return;
        }
        
        activity.runOnUiThread(() -> {
            isAdLoading = true;
            Bundle extras = new Bundle();
            if (npa) { extras.putString("npa", "1"); }
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            
            InterstitialAd.load(
                activity,
                getInterstitialUnitId(),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        isAdLoading = false;
                        Log.d(TAG, "Interstitial ad loaded");
                        call.resolve();
                    }
                    
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null;
                        isAdLoading = false;
                        Log.e(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage());
                        call.reject("Failed to load ad: " + loadAdError.getMessage());
                    }
                }
            );
        });
    }
    
    @PluginMethod
    public void showInterstitial(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }
        
        activity.runOnUiThread(() -> {
            if (interstitialAd != null) {
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        interstitialAd = null;
                        Log.d(TAG, "Interstitial ad dismissed");
                        notifyListeners("adDismissed", null);
                        call.resolve();
                    }
                    
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        interstitialAd = null;
                        Log.e(TAG, "Failed to show interstitial ad: " + adError.getMessage());
                        call.reject("Failed to show ad: " + adError.getMessage());
                    }
                    
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad showed");
                        notifyListeners("adShowed", null);
                    }
                });
                
                interstitialAd.show(activity);
            } else {
                call.reject("No ad loaded");
            }
        });
    }
    
    @PluginMethod
    public void isAdReady(PluginCall call) {
        boolean ready = interstitialAd != null;
        call.resolve(new com.getcapacitor.JSObject().put("ready", ready));
    }
    
    @PluginMethod
    public void loadRewardedAd(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }
        
        if (isRewardedAdLoading) {
            call.reject("Rewarded ad is already loading");
            return;
        }
        
        activity.runOnUiThread(() -> {
            isRewardedAdLoading = true;
            Bundle extras = new Bundle();
            if (npa) { extras.putString("npa", "1"); }
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            
            RewardedAd.load(
                activity,
                getRewardedUnitId(),
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        isRewardedAdLoading = false;
                        Log.d(TAG, "Rewarded ad loaded");
                        call.resolve();
                    }
                    
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;
                        isRewardedAdLoading = false;
                        Log.e(TAG, "Failed to load rewarded ad: " + loadAdError.getMessage());
                        call.reject("Failed to load rewarded ad: " + loadAdError.getMessage());
                    }
                }
            );
        });
    }
    
    @PluginMethod
    public void showRewardedAd(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }
        
        activity.runOnUiThread(() -> {
            if (rewardedAd != null) {
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        Log.d(TAG, "Rewarded ad dismissed");
                        notifyListeners("rewardedAdDismissed", null);
                    }
                    
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        rewardedAd = null;
                        Log.e(TAG, "Failed to show rewarded ad: " + adError.getMessage());
                        call.reject("Failed to show rewarded ad: " + adError.getMessage());
                    }
                    
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad showed");
                        notifyListeners("rewardedAdShowed", null);
                    }
                });
                
                rewardedAd.show(activity, rewardItem -> {
                    // L'utilisateur a gagné la récompense
                    int amount = rewardItem.getAmount();
                    String type = rewardItem.getType();
                    Log.d(TAG, "User earned reward: " + amount + " " + type);
                    
                    com.getcapacitor.JSObject reward = new com.getcapacitor.JSObject();
                    reward.put("amount", amount);
                    reward.put("type", type);
                    notifyListeners("rewardEarned", reward);
                    call.resolve(reward);
                });
            } else {
                call.reject("No rewarded ad loaded");
            }
        });
    }
    
    @PluginMethod
    public void isRewardedAdReady(PluginCall call) {
        boolean ready = rewardedAd != null;
        call.resolve(new com.getcapacitor.JSObject().put("ready", ready));
    }
}
