import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.heritier.millionnaire',
  appName: 'Héritier Millionnaire',
  webDir: 'dist',
  android: {
    allowMixedContent: false
  }
};

export default config;
