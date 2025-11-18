# Script de correction du jeu drag racing
$file = "c:\Users\smori\application nouvelle\jeux du Millionaire\mobile\android\app\src\main\assets\public\drag\main.js"

Write-Host "Lecture du fichier..." -ForegroundColor Yellow
$content = Get-Content $file -Raw -Encoding UTF8

Write-Host "Application des correctifs..." -ForegroundColor Yellow

# Fix 1: Améliorer la détection Capacitor
$oldCapacitor = "const isCapacitor = protocol === 'capacitor:' || protocol === 'ionic:' || protocol === 'file:';"
$newCapacitor = @"
const isCapacitor = protocol === 'capacitor:' || protocol === 'ionic:' || protocol === 'file:' || 
                           (typeof window.Capacitor !== 'undefined') || 
                           (typeof window.cordova !== 'undefined');
"@
$content = $content.Replace($oldCapacitor, $newCapacitor)

# Fix 2: Améliorer le message de log
$content = $content.Replace(
    "try { console.info('[drag] App mobile: API directe', API_BASE);",
    "try { console.info('[drag] App mobile Capacitor détecté: API directe', API_BASE);"
)

# Fix 3: Ajouter la gestion du bouton Rejouer
$oldButton = @"
startButton.addEventListener('click', () => {
    if (game.state === 'countdown') {
        return;
    }
    // Exiger un mode
"@

$newButton = @"
startButton.addEventListener('click', () => {
    if (game.state === 'countdown') {
        return;
    }
    
    // Si la course est terminée, réinitialiser l'état pour permettre de rejouer
    if (game.state === 'finished') {
        game.state = 'idle';
        startButton.textContent = 'LANCER LA COURSE';
        startButton.disabled = false;
        // Remettre le bouton en mode normal pour permettre une nouvelle sélection
        return;
    }
    
    // Exiger un mode
"@
$content = $content.Replace($oldButton, $newButton)

Write-Host "Écriture du fichier corrigé..." -ForegroundColor Yellow
Set-Content $file $content -Encoding UTF8 -NoNewline

Write-Host "`n✅ Corrections appliquées avec succès!" -ForegroundColor Green
Write-Host "  - Détection Capacitor améliorée (connexion API)" -ForegroundColor Cyan
Write-Host "  - Bouton Rejouer fonctionnel" -ForegroundColor Cyan
