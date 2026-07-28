$resDir = "e:\Mad\app\src\main\res"

$files = Get-ChildItem -Path $resDir\layout -Filter *.xml -Recurse
if (Test-Path $resDir\values) {
    $files += Get-ChildItem -Path $resDir\values -Filter *.xml -Recurse
}

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    
    # Positive DP (>= 2dp)
    $newContent = [regex]::Replace($content, '"([2-9]|[1-9][0-9]+)dp"', '"@dimen/_$1sdp"')
    
    # SP (any sp)
    $newContent = [regex]::Replace($newContent, '"([1-9][0-9]*)sp"', '"@dimen/_$1ssp"')
    
    # Negative DP (-1dp, -2dp, etc.)
    $newContent = [regex]::Replace($newContent, '"-([1-9][0-9]*)dp"', '"@dimen/_minus$1sdp"')
    
    if ($content -ne $newContent) {
        Set-Content -Path $file.FullName -Value $newContent -Encoding UTF8
    }
}
Write-Host "Conversion completed successfully!"
