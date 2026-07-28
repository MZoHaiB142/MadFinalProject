$resDir = "e:\Mad\app\src\main\res"
$files = Get-ChildItem -Path $resDir -Filter *.xml -Recurse
$utf8NoBom = New-Object System.Text.UTF8Encoding $False
foreach ($file in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    if ($bytes.Length -gt 3 -and $bytes[0] -eq 239 -and $bytes[1] -eq 187 -and $bytes[2] -eq 191) {
        Write-Host "Removing BOM from: " $file.FullName
        $content = [System.IO.File]::ReadAllText($file.FullName)
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
    }
}
Write-Host "Done"
