import type { CapacitorConfig } from '@capacitor/cli';

const DEFAULT_MOBILE_WEB_URL = 'https://client-jeux-millionnaire.vercel.app';
const configuredWebUrl = (process.env.MOBILE_WEB_URL ?? '').trim();
const mobileWebUrl = configuredWebUrl || DEFAULT_MOBILE_WEB_URL;

let mobileHost = 'client-jeux-millionnaire.vercel.app';
try {
  mobileHost = new URL(mobileWebUrl).hostname || mobileHost;
} catch {
  // Conserver l'hôte de production si une variable invalide est fournie.
}

const config: CapacitorConfig = {
  appId: 'com.heritier.millionnaire',
  appName: 'Héritier Millionnaire',
  webDir: 'dist',

  // Le client Vercel est la source de vérité en production. Cela évite qu'un AAB
  // embarque silencieusement une vieille copie de `dist` lorsque MOBILE_WEB_URL
  // n'a pas été exportée avant `cap sync`.
  server: {
    url: mobileWebUrl,
    cleartext: mobileWebUrl.startsWith('http://'),
    androidScheme: 'https',
    allowNavigation: Array.from(new Set([
      mobileHost,
      'client-jeux-millionnaire.vercel.app',
    ])),
  },

  android: {
    allowMixedContent: false,
  },
  plugins: {
    StatusBar: {
      style: 'light',
      overlays: true,
      backgroundColor: '#00000000',
    },
  },
};

export default config;
