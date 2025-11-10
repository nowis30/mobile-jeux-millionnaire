import { WebPlugin } from '@capacitor/core';
import type { AdMobPlugin, RewardEarned } from './index';

export class AdMobWeb extends WebPlugin implements AdMobPlugin {
  async initialize(): Promise<void> {
    console.log('[AdMob Web] Initialize - No-op on web');
  }

  async loadInterstitial(): Promise<void> {
    console.log('[AdMob Web] Load interstitial - No-op on web');
  }

  async showInterstitial(): Promise<void> {
    console.log('[AdMob Web] Show interstitial - No-op on web');
  }

  async isAdReady(): Promise<{ ready: boolean }> {
    console.log('[AdMob Web] Is ad ready - No-op on web');
    return { ready: false };
  }

  async loadRewardedAd(): Promise<void> {
    console.log('[AdMob Web] Load rewarded ad - No-op on web');
  }

  async showRewardedAd(): Promise<RewardEarned> {
    console.log('[AdMob Web] Show rewarded ad - No-op on web');
    return { amount: 1, type: 'reward' };
  }

  async isRewardedAdReady(): Promise<{ ready: boolean }> {
    console.log('[AdMob Web] Is rewarded ad ready - No-op on web');
    return { ready: false };
  }
}
