# PowerShell script to download and copy Spectral executables to the Maven plugin resource directories
# Usage: .\copy-spectral-executables.ps1 [spectral-version]

param(
    [string]$SpectralVersion = "v6.15.0",
    [switch]$Help
)

# Show help if requested
if ($Help) {
    Write-Host "Usage: .\copy-spectral-executables.ps1 [spectral-version]"
    Write-Host ""
    Write-Host "Downloads Spectral executables from GitHub releases and installs them into the Maven plugin."
    Write-Host ""
    Write-Host "Parameters:"
    Write-Host "  -SpectralVersion  The Spectral version to download (default: v6.15.0)"
    Write-Host "                    Must be in format 'vX.Y.Z' (e.g., v6.15.0, v6.11.0)"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\copy-spectral-executables.ps1                    # Downloads v6.15.0"
    Write-Host "  .\copy-spectral-executables.ps1 -SpectralVersion v6.11.0  # Downloads v6.11.0"
    Write-Host "  .\copy-spectral-executables.ps1 v6.15.0           # Downloads v6.15.0"
    Write-Host ""
    Write-Host "Requirements:"
    Write-Host "  - PowerShell 5.0 or later"
    Write-Host "  - Internet connection required"
    exit 0
}

$PluginDir = "spectral-executables"
$TempDir = "$env:TEMP\spectral-downloads"
$BaseUrl = "https://github.com/stoplightio/spectral/releases/download"

# Strip any trailing suffixes like -rc1, -beta, etc. from the version for GitHub release downloads
# This converts versions like "v6.15.0-rc1" to "v6.15.0" for the download URL
$DownloadVersion = $SpectralVersion -replace '-.*$', ''

Write-Host "Original version: $SpectralVersion" -ForegroundColor Green
Write-Host "Download version (suffix stripped): $DownloadVersion" -ForegroundColor Green
Write-Host "Downloading Spectral executables from GitHub release: $DownloadVersion" -ForegroundColor Green
Write-Host "To plugin directory: $PluginDir" -ForegroundColor Green

# Create temporary directory for downloads
if (Test-Path $TempDir) {
    Remove-Item $TempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null

# Function to download and copy executable
function Download-And-Copy-Executable {
    param(
        [string]$Filename,
        [string]$DestDir,
        [string]$ResourcePath
    )
    
    $Url = "$BaseUrl/$DownloadVersion/$Filename"
    $TempFile = Join-Path $TempDir $Filename
    $DestPath = Join-Path "$DestDir\src\main\resources" $ResourcePath
    $DestFolder = Split-Path $DestPath -Parent
    
    Write-Host "Downloading: $Filename" -ForegroundColor Yellow
    
    try {
        # Use Invoke-WebRequest to download the file
        Invoke-WebRequest -Uri $Url -OutFile $TempFile -UseBasicParsing
        
        Write-Host "Copying: $Filename -> $DestDir\src\main\resources\$ResourcePath" -ForegroundColor Cyan
        
        # Create destination directory if it doesn't exist
        if (!(Test-Path $DestFolder)) {
            New-Item -ItemType Directory -Path $DestFolder -Force | Out-Null
        }
        
        # Copy the file
        Copy-Item $TempFile $DestPath -Force
        
        Write-Host "✓ Successfully downloaded and installed $Filename" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "✗ Failed to download $Filename from $Url" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Check if we have internet connectivity
try {
    $TestConnection = Test-NetConnection -ComputerName "github.com" -Port 443 -InformationLevel Quiet
    if (-not $TestConnection) {
        Write-Host "Error: No internet connection detected. Please check your network connection and try again." -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "Warning: Unable to test internet connectivity. Proceeding anyway..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Starting download process..." -ForegroundColor Green

# Download executables with error tracking
$DownloadResults = @()

# Download Windows executable
$DownloadResults += Download-And-Copy-Executable "spectral.exe" "$PluginDir\spectral-win" "spectral\windows\spectral.exe"

# Download Linux executables
$DownloadResults += Download-And-Copy-Executable "spectral-linux-x64" "$PluginDir\spectral-linux-x64" "spectral\linux-x64\spectral"
$DownloadResults += Download-And-Copy-Executable "spectral-linux-arm64" "$PluginDir\spectral-linux-arm64" "spectral\linux-arm64\spectral"

# Download macOS executables
$DownloadResults += Download-And-Copy-Executable "spectral-macos-x64" "$PluginDir\spectral-macos-x64" "spectral\macos-x64\spectral"
$DownloadResults += Download-And-Copy-Executable "spectral-macos-arm64" "$PluginDir\spectral-macos-arm64" "spectral\macos-arm64\spectral"

# Download Alpine executables
$DownloadResults += Download-And-Copy-Executable "spectral-alpine-x64" "$PluginDir\spectral-alpine-x64" "spectral\alpine-x64\spectral"
$DownloadResults += Download-And-Copy-Executable "spectral-alpine-arm64" "$PluginDir\spectral-alpine-arm64" "spectral\alpine-arm64\spectral"

# Clean up temporary directory
Remove-Item $TempDir -Recurse -Force -ErrorAction SilentlyContinue

# Summary
Write-Host ""
$SuccessCount = ($DownloadResults | Where-Object { $_ -eq $true }).Count
$TotalCount = $DownloadResults.Count

if ($SuccessCount -eq $TotalCount) {
    Write-Host "✓ Done downloading and installing all $TotalCount executables." -ForegroundColor Green
} else {
    $FailCount = $TotalCount - $SuccessCount
    Write-Host "⚠ Downloaded $SuccessCount out of $TotalCount executables. $FailCount failed." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Build the plugin: mvn clean install" -ForegroundColor White
Write-Host "2. Test the plugin: cd test-project && mvn spectral:validate" -ForegroundColor White
Write-Host ""
Write-Host "Available executables for Spectral ${SpectralVersion}:" -ForegroundColor Cyan
Write-Host "  - Windows (x64): spectral.exe" -ForegroundColor White
Write-Host "  - Linux (x64): spectral-linux-x64" -ForegroundColor White
Write-Host "  - Linux (ARM64): spectral-linux-arm64" -ForegroundColor White
Write-Host "  - macOS (x64): spectral-macos-x64" -ForegroundColor White
Write-Host "  - macOS (ARM64): spectral-macos-arm64" -ForegroundColor White
Write-Host "  - Alpine (x64): spectral-alpine-x64" -ForegroundColor White
Write-Host "  - Alpine (ARM64): spectral-alpine-arm64" -ForegroundColor White

# Exit with appropriate code
if ($SuccessCount -eq $TotalCount) {
    exit 0
} else {
    exit 1
}