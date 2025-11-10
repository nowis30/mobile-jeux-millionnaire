/**
 * Plugin AdMob pour Capacitor
 * Permet d'afficher des annonces interstitielles et récompensées dans l'app Android
 */

import { registerPlugin } from '@capacitor/core';

export interface RewardEarned {
  amount: number;
  type: string;
}

export interface AdMobPlugin {
  /**
   * Initialiser le SDK AdMob
   */
  initialize(): Promise<void>;
  
  /**
   * Charger une annonce interstitielle
   */
  loadInterstitial(): Promise<void>;
  
  /**
   * Afficher l'annonce interstitielle chargée
   */
  showInterstitial(): Promise<void>;
  
  /**
   * Vérifier si une annonce interstitielle est prête à être affichée
   */
  isAdReady(): Promise<{ ready: boolean }>;
  
  /**
   * Charger une annonce récompensée
   */
  loadRewardedAd(): Promise<void>;
  
  /**
   * Afficher l'annonce récompensée chargée
   * Retourne la récompense si l'utilisateur a regardé la pub en entier
   */
  showRewardedAd(): Promise<RewardEarned>;
  
  /**
   * Vérifier si une annonce récompensée est prête à être affichée
   */
  isRewardedAdReady(): Promise<{ ready: boolean }>;
}

const AdMob = registerPlugin<AdMobPlugin>('AdMob', {
  web: () => import('./web').then(m => new m.AdMobWeb()),
});

export default AdMob;
