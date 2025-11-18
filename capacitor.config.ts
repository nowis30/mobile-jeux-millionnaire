import type { CapacitorConfig } from '@capacitor/cli';

const mobileWebUrl = (process.env.MOBILE_WEB_URL ?? '').trim();

const config: CapacitorConfig = {
  appId: 'com.heritier.millionnaire',
  appName: 'Héritier Millionnaire',
  webDir: 'dist',
  server: mobileWebUrl ? {
    // URL du client Next.js distant si MOBILE_WEB_URL est défini
    url: mobileWebUrl,
    cleartext: true,
    androidScheme: 'https'
  } : {
    // Utilise le build local (dist) par défaut
    cleartext: true,
    androidScheme: 'https'
  },
  android: {
    allowMixedContent: true
  },
  plugins: {
    StatusBar: {
      style: 'light',
      overlays: true,
      backgroundColor: '#00000000'
    }
  }
};

export default config;
