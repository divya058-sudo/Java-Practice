$jarName = "mysql-connector-j-8.0.33.jar"
$downloadUrl = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"

if (!(Test-Path $jarName)) {
    Write-Host "MySQL JDBC Driver not found. Downloading $jarName from Maven Central..." -ForegroundColor Cyan
    try {
        Invoke-WebRequest -Uri $downloadUrl -OutFile $jarName
        Write-Host "Download complete!" -ForegroundColor Green
    } catch {
        Write-Error "Failed to download JDBC driver: $_"
        Exit 1
    }
}

Write-Host "Compiling Java source files..." -ForegroundColor Cyan
javac *.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Starting Chat Application...`n" -ForegroundColor Green
    java -cp ".;$jarName" Main
} else {
    Write-Error "Compilation failed."
}
