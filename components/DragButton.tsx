import React from 'react';
import DragLauncher from '../src/plugins/drag-launcher';

/**
 * Composant bouton pour ouvrir le mini-jeu de drag racing.
 * 
 * Utilise le plugin Capacitor DragLauncher pour:
 * - Android/iOS: ouvrir DragActivity (écran natif avec WebView local)
 * - Web: rediriger vers /drag
 */
export function DragButton() {
  const handleClick = async () => {
    try {
      const result = await DragLauncher.open();
      console.log('Drag ouvert:', result);
    } catch (error) {
      console.error('Erreur ouverture drag:', error);
      alert('Impossible d\'ouvrir le jeu de drag. Veuillez réessayer.');
    }
  };

  return (
    <button
      onClick={handleClick}
      className="bg-gradient-to-r from-red-600 to-orange-600 hover:from-red-700 hover:to-orange-700 text-white font-bold py-4 px-8 rounded-xl shadow-lg transform transition hover:scale-105 active:scale-95"
    >
      🏎️ Mini-jeu Drag Racing
    </button>
  );
}

export default DragButton;
