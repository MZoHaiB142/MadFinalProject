$resDir = "e:\Mad\app\src\main\res"
$files = Get-ChildItem -Path $resDir -Filter *.xml -Recurse
$utf8NoBom = New-Object System.Text.UTF8Encoding $False

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName)
    
    # Negative SDP -> -dp
    $newContent = [regex]::Replace($content, '"@dimen/_minus([0-9]+)sdp"', '"-$1dp"')
    
    # Positive SDP -> dp
    $newContent = [regex]::Replace($newContent, '"@dimen/_([0-9]+)sdp"', '"$1dp"')
    
    # Positive SSP -> sp
    $newContent = [regex]::Replace($newContent, '"@dimen/_([0-9]+)ssp"', '"$1sp"')
    
    if ($content -ne $newContent) {
        Write-Host "Reverting: " $file.FullName
        [System.IO.File]::WriteAllText($file.FullName, $newContent, $utf8NoBom)
    }
}
Write-Host "Revert completed successfully!"
