
Add-Type -AssemblyName System.Drawing

$basePath = "$PSScriptRoot\src\main\resources\assets\creeper-apocalypse\textures\entity\creeper_base.png"
$outPath = "$PSScriptRoot\src\main\resources\assets\creeper-apocalypse\textures\entity\creeper_armor_purple.png"

if (-not (Test-Path $basePath)) {
    Write-Host "Error: Base texture not found at $basePath"
    exit
}

$bitmap = [System.Drawing.Bitmap]::new($basePath)
$newBitmap = [System.Drawing.Bitmap]::new($bitmap.Width, $bitmap.Height)

for ($x = 0; $x -lt $bitmap.Width; $x++) {
    for ($y = 0; $y -lt $bitmap.Height; $y++) {
        $pixel = $bitmap.GetPixel($x, $y)
        
        if ($pixel.A -eq 0) { 
            $newBitmap.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
            continue 
        }

        # Create an "energy" effect
        # We want high brightness pixels to be opaque purple
        # Low brightness pixels to be transparent
        $bright = $pixel.GetBrightness()
        
        # Color: Purple (180, 50, 255)
        # Alpha: Based on brightness (so it looks like energy)
        $alpha = [int]([Math]::Min(255, $bright * 200))
        
        # Add some diagonal striping to mimic energy field if possible? 
        # Simple math pattern: ($x + $y) % 8
        if (($x + $y) % 4 -eq 0) {
            $alpha = [int]($alpha * 1.5)
            if ($alpha -gt 255) { $alpha = 255 }
        }

        $color = [System.Drawing.Color]::FromArgb($alpha, 180, 40, 255)
        $newBitmap.SetPixel($x, $y, $color)
    }
}

$newBitmap.Save($outPath)
$bitmap.Dispose()
$newBitmap.Dispose()

Write-Host "Created purple armor texture at $outPath"
