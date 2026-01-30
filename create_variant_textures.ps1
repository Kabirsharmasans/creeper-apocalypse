# PowerShell script to create colored creeper variants
Add-Type -AssemblyName System.Drawing

$baseTexture = "src\main\resources\assets\creeper-apocalypse\textures\entity\creeper_base.png"
$outputDir = "src\main\resources\assets\creeper-apocalypse\textures\entity"

function Recolor-Texture {
    param(
        [string]$inputPath,
        [string]$outputPath,
        [float]$hueShift,  # 0-360
        [float]$saturation, # 0-2 (1 = no change)
        [float]$brightness  # 0-2 (1 = no change)
    )
    
    $bitmap = [System.Drawing.Bitmap]::new($inputPath)
    
    for ($x = 0; $x -lt $bitmap.Width; $x++) {
        for ($y = 0; $y -lt $bitmap.Height; $y++) {
            $pixel = $bitmap.GetPixel($x, $y)
            
            # Skip transparent pixels
            if ($pixel.A -eq 0) { continue }
            
            # Convert to HSV
            $hue = $pixel.GetHue()
            $sat = $pixel.GetSaturation()
            $val = $pixel.GetBrightness()
            
            # Apply transformations
            $newHue = ($hue + $hueShift) % 360
            $newSat = [Math]::Min(1.0, $sat * $saturation)
            $newVal = [Math]::Min(1.0, $val * $brightness)
            
            # Convert back to RGB
            $c = $newSat * $newVal
            $x2 = $c * (1 - [Math]::Abs(($newHue / 60) % 2 - 1))
            $m = $newVal - $c
            
            $r = $g = $b = 0
            
            if ($newHue -lt 60) {
                $r = $c; $g = $x2; $b = 0
            } elseif ($newHue -lt 120) {
                $r = $x2; $g = $c; $b = 0
            } elseif ($newHue -lt 180) {
                $r = 0; $g = $c; $b = $x2
            } elseif ($newHue -lt 240) {
                $r = 0; $g = $x2; $b = $c
            } elseif ($newHue -lt 300) {
                $r = $x2; $g = 0; $b = $c
            } else {
                $r = $c; $g = 0; $b = $x2
            }
            
            $newR = [byte][Math]::Min(255, ($r + $m) * 255)
            $newG = [byte][Math]::Min(255, ($g + $m) * 255)
            $newB = [byte][Math]::Min(255, ($b + $m) * 255)
            
            $newColor = [System.Drawing.Color]::FromArgb($pixel.A, $newR, $newG, $newB)
            $bitmap.SetPixel($x, $y, $newColor)
        }
    }
    
    $bitmap.Save($outputPath)
    $bitmap.Dispose()
}

# Create variants
Write-Host "Creating Mini Creeper (Bright Green)..."
Recolor-Texture $baseTexture "$outputDir\mini_creeper.png" 90 1.5 1.3

Write-Host "Creating Giant Creeper (Dark Red)..."
Recolor-Texture $baseTexture "$outputDir\giant_creeper.png" 0 1.2 0.8

Write-Host "Creating Spider Creeper (Purple)..."
Recolor-Texture $baseTexture "$outputDir\spider_creeper.png" 270 1.4 1.1

Write-Host "Creating Ninja Creeper (Dark Gray/Black)..."
Recolor-Texture $baseTexture "$outputDir\ninja_creeper.png" 0 0.2 0.5

Write-Host "Creating Rainbow Creeper (Cyan/Magenta)..."
Recolor-Texture $baseTexture "$outputDir\rainbow_creeper.png" 180 1.8 1.4

Write-Host "Creating Bouncy Creeper (Light Blue)..."
Recolor-Texture $baseTexture "$outputDir\bouncy_creeper.png" 200 1.3 1.3

Write-Host "Creating Jockey Creeper (Orange)..."
Recolor-Texture $baseTexture "$outputDir\jockey_creeper.png" 30 1.5 1.2

Write-Host "All textures created successfully!"
