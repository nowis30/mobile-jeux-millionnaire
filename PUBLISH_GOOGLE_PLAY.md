# Publication Google Play – App native (Capacitor + AdMob)

Ce document résume le flux pour publier l’app Android native avec publicités AdMob.

## Prérequis
- Keystore présent: `mobile/android/key-android.jks` (alias `nowis`)
- Identifiants AdMob configurés:
  - App ID (AndroidManifest.xml): `ca-app-pub-7443046636998296~8556348720`
  - Interstitial: `ca-app-pub-7443046636998296/9246188831`
  - Rewarded: `ca-app-pub-7443046636998296/3604239096`
- Consentement UMP inclus (RGPD) via `AdMobPlugin.java`
- Page de confidentialité hébergée: `https://app.nowis.store/privacy.html`

## Étapes build AAB (release)
1) Synchroniser Capacitor
```powershell
cd "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile"
npx cap sync
```

2) Générer le bundle release (AAB)
```powershell
cd android
.\gradlew.bat bundleRelease
```

3) Récupérer le fichier
- `mobile/android/app/build/outputs/bundle/release/app-release.aab`
- Option: utiliser un script pour copier dans `releases/` avec un nom horodaté.

## Vérifications Play Console
- Application Id: `com.heritier.millionnaire` (voir `mobile/android/app/build.gradle`)
- versionCode / versionName: ajuster au besoin dans `defaultConfig`
- Déclarer: "Contient de la publicité"
- Lien de suppression de compte (Account deletion web link): https://app.nowis.store/delete-account.html
- Data Safety:
  - Déclarer l’usage du SDK Google Mobile Ads (AdMob) et UMP (consentement)
  - Si personnalisation, déclarer collecte/conservation appropriées
- Politique de confidentialité: URL `https://app.nowis.store/privacy.html`
- Ciblage et contenu: jeu pour public général (non destiné aux enfants <13 ans)

## Tests recommandés
- Installer un APK debug pour vérifier l’intégration pub (IDs de test auto en debug)
- Valider interstitiel: après événements clés (cash-out, transactions…)
- Valider rewarded: via `RewardedAdButton` (cooldown géré)
- Logcat filtres: `AdMobPlugin`, `Ads`

## Checklist soumission
- [ ] AAB release généré et signé
- [ ] Fiche boutique: icône 512×512, feature graphic 1024×500, captures 1080×1920
- [ ] Ads: déclaré dans Play Console
- [ ] Data Safety complétée
- [ ] Privacy Policy en ligne et accessible
- [ ] Test interne → fermé → production

---
Astuce: en debug, le plugin force les Ad Units de test de Google (pas de risque de bannière de prod).
