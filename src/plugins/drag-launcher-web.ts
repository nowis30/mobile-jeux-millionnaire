import { WebPlugin } from '@capacitor/core';
import type { DragLauncherPlugin } from './drag-launcher';

export class DragLauncherWeb extends WebPlugin implements DragLauncherPlugin {
  async open(): Promise<{ success: boolean }> {
    // Sur le web, rediriger vers la page drag
    window.location.href = '/drag';
    return { success: true };
  }
}
