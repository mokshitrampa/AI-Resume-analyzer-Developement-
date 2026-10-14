$ErrorActionPreference = 'Stop'
$dest = 'C:\Users\moksh\.jdks'
if (-not (Test-Path $dest)) {
    New-Item -ItemType Directory -Path $dest -Force | Out-Null
}

$jdkZip = Join-Path $dest 'jdk21.zip'
$jdkUrl = 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk'

Write-Host "Downloading Temurin JDK 21..."
& curl.exe -L -o "$jdkZip" "$jdkUrl"

Write-Host "Extracting JDK 21..."
Expand-Archive -Path "$jdkZip" -DestinationPath "$dest" -Force
Remove-Item "$jdkZip" -Force

$mvnZip = Join-Path $dest 'maven.zip'
$mvnUrl = 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip'

Write-Host "Downloading Maven 3.9.6..."
& curl.exe -L -o "$mvnZip" "$mvnUrl"

Write-Host "Extracting Maven 3.9.6..."
Expand-Archive -Path "$mvnZip" -DestinationPath "$dest" -Force
Remove-Item "$mvnZip" -Force

Write-Host "JDK 21 and Maven successfully installed to $dest!"
