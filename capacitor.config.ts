import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.heritier.millionnaire',
  appName: 'Héritier Millionnaire',
  webDir: 'dist',
  server: {
    // Permet à l'app d'accéder aux URLs externes
    cleartext: true,
    androidScheme: 'https'
  },
  android: {
    allowMixedContent: true
  },
  plugins: {
    StatusBar: {
      style: 'dark',
      backgroundColor: '#0f172a'
    }
  }
};

export default config;
