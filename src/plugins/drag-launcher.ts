import { registerPlugin } from '@capacitor/core';

export interface DragLauncherPlugin {
  /**
   * Ouvre l'écran du mini-jeu de drag racing intégré à l'application.
   * 
   * @returns {Promise<{ success: boolean }>} Promise résolue quand l'écran est ouvert
   */
  open(): Promise<{ success: boolean }>;
}

const DragLauncher = registerPlugin<DragLauncherPlugin>('DragLauncher', {
  web: () => import('./drag-launcher-web').then(m => new m.DragLauncherWeb()),
});

export default DragLauncher;
