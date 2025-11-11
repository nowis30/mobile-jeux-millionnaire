# Data Safety – Modèle de réponses (Google Play)

Ce document t'aide à remplir le formulaire "Sécurité des données" pour l'app native `com.heritier.millionnaire` utilisant AdMob + UMP.

## 1. Liste des SDK
- Google Mobile Ads SDK (AdMob)
- User Messaging Platform (UMP) – consentement RGPD
- Capacitor (framework d'accès natif)

## 2. Catégories de données déclarées
| Catégorie | Exemple interne | Collectée | Partagée | Finalités | Justification |
|-----------|-----------------|----------|----------|-----------|---------------|
| Identifiants | Advertising ID (AAID) | Oui | Oui (réseaux publicitaires) | Publicité / mesure | Requis par AdMob pour servir/limiter la fréquence des annonces |
| Diagnostics | Logs de crash, erreurs chargement d'annonces | Oui | Non | Analyse / stabilité | Améliorer qualité, débogage |
| Interactions publicitaires | Impressions, clics, complétion rewarded | Oui | Oui | Publicité / mesure | Fournir reporting agrégé AdMob |
| Infos appareil non personnelles | Type OS, version API Android | Oui | Oui (AdMob) | Publicité / compatibilité | Adapter formats / ciblage technique |

Si tu limites la personnalisation (NPA) via UMP pour certains utilisateurs, l’Advertising ID est toujours transmis mais usage de ciblage personnalisé réduit.

## 3. Finalités à sélectionner
- Publicité ou marketing
- Analyse
- Fonctionnement de l'application (si Google te le propose pour données techniques minimales)

## 4. Collecte
Les données ci-dessus sont collectées automatiquement à l'ouverture et durant l'usage de fonctionnalités publicitaires.

## 5. Partage
Partage avec Google (AdMob) et partenaires publicitaires pour diffusion d'annonces + mesure.

## 6. Protection
| Aspect | Réponse |
|--------|---------|
| Chiffrement en transit | Oui (HTTPS) |
| Possibilité de suppression | Advertising ID réinitialisable par l'utilisateur Android |
| Vente de données | Non |
| Application destinée aux enfants | Non |
| Données nécessaires au fonctionnement | Oui (diagnostics techniques minimaux) |

## 7. Minimisation / Consentement
- UMP affiche la demande de consentement en UE / régions concernées.
- Si statut != OBTAINED => diffusion NPA (non-personalized ads) dans le plugin (flag `npa`).

## 8. Aucune donnée sensible
Pas de:
- Localisation précise
- Contacts
- SMS / appels
- Santé / finance personnelle réelle (les données financières sont fictives de jeu)

## 9. Vérification
Avant de soumettre:
1. UMP se lance sur un appareil en région UE (tester via VPN si besoin).
2. Logs montrent mapping consent => `npa=true/false`.
3. Rewarded ads fonctionnent après consentement (ou en mode NPA).

## 10. Mise à jour future
Si ajout d’Analytics (Firebase) ou achats in-app => mettre à jour ce fichier et re-déclarer sur Play.

---
Dernière mise à jour: 11/11/2025
