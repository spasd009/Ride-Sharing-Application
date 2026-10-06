$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$previousUser = $env:DATABASE_USER
$previousPassword = $env:DATABASE_PASSWORD
$secret = $null
$secretPointer = [IntPtr]::Zero
try {
    if (-not $env:DATABASE_USER) { $env:DATABASE_USER = Read-Host "MySQL application username" }
    if (-not $env:DATABASE_PASSWORD) {
        $secret = Read-Host "MySQL application password (hidden)" -AsSecureString
        $secretPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
        $env:DATABASE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($secretPointer)
    }
    if (Test-Path "ride-sharing-1.0.0.jar") { & java -jar "ride-sharing-1.0.0.jar" }
    else { & mvn spring-boot:run }
} finally {
    $env:DATABASE_USER = $previousUser
    $env:DATABASE_PASSWORD = $previousPassword
    if ($secretPointer -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($secretPointer) }
    if ($secret) { $secret.Dispose() }
}
