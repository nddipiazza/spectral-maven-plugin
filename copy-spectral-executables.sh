#!/bin/bash

# Script to download and copy Spectral executables to the Maven plugin resource directories
# Usage: ./copy-spectral-executables.sh [spectral-version]

# Show help if requested
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "Usage: $0 [spectral-version]"
    echo ""
    echo "Downloads Spectral executables from GitHub releases and installs them into the Maven plugin."
    echo ""
    echo "Arguments:"
    echo "  spectral-version  The Spectral version to download (default: v6.15.0)"
    echo "                    Must be in format 'vX.Y.Z' (e.g., v6.15.0, v6.11.0)"
    echo ""
    echo "Examples:"
    echo "  $0                # Downloads v6.15.0"
    echo "  $0 v6.11.0        # Downloads v6.11.0"
    echo "  $0 v6.15.0        # Downloads v6.15.0"
    echo ""
    echo "Requirements:"
    echo "  - curl must be installed"
    echo "  - Internet connection required"
    exit 0
fi

SPECTRAL_VERSION=${1:-"v6.15.0"}
PLUGIN_DIR="spectral-executables"
TEMP_DIR="/tmp/spectral-downloads"
BASE_URL="https://github.com/stoplightio/spectral/releases/download"

echo "Downloading Spectral executables version: $SPECTRAL_VERSION"
echo "To plugin directory: $PLUGIN_DIR"

# Create temporary directory for downloads
mkdir -p "$TEMP_DIR"

# Function to download and copy executable
download_and_copy_executable() {
    local filename="$1"
    local dest_dir="$2"
    local dest_file="$3"
    local url="$BASE_URL/$SPECTRAL_VERSION/$filename"
    local temp_file="$TEMP_DIR/$filename"
    
    echo "Downloading: $filename"
    if curl $CURL_ADDITIONAL_ARGS -L -o "$temp_file" "$url" --fail --silent --show-error; then
        echo "Copying: $filename -> $dest_dir/src/main/resources/$dest_file"
        mkdir -p "$dest_dir/src/main/resources"
        cp "$temp_file" "$dest_dir/src/main/resources/$dest_file"
        chmod +x "$dest_dir/src/main/resources/$dest_file"
        echo "✓ Successfully downloaded and installed $filename"
    else
        echo "✗ Failed to download $filename from $url"
        return 1
    fi
}

# Check if curl is available
if ! command -v curl &> /dev/null; then
    echo "Error: curl is required but not installed. Please install curl and try again."
    exit 1
fi

# Download Windows executable
download_and_copy_executable "spectral.exe" "$PLUGIN_DIR/spectral-win" "spectral.exe"

# Download Linux executables
download_and_copy_executable "spectral-linux-x64" "$PLUGIN_DIR/spectral-linux-x64" "spectral"
download_and_copy_executable "spectral-linux-arm64" "$PLUGIN_DIR/spectral-linux-arm64" "spectral"

# Download macOS executables
download_and_copy_executable "spectral-macos-x64" "$PLUGIN_DIR/spectral-macos-x64" "spectral"
download_and_copy_executable "spectral-macos-arm64" "$PLUGIN_DIR/spectral-macos-arm64" "spectral"

# Download Alpine executables
download_and_copy_executable "spectral-alpine-x64" "$PLUGIN_DIR/spectral-alpine-x64" "spectral"
download_and_copy_executable "spectral-alpine-arm64" "$PLUGIN_DIR/spectral-alpine-arm64" "spectral"

# Clean up temporary directory
rm -rf "$TEMP_DIR"

echo ""
echo "✓ Done downloading and installing executables."
echo ""
echo "Next steps:"
echo "1. Build the plugin: mvn clean install"
echo "2. Test the plugin: cd test-project && mvn spectral:validate"
echo ""
echo "Available executables for Spectral $SPECTRAL_VERSION:"
echo "  - Windows (x64): spectral.exe"
echo "  - Linux (x64): spectral-linux-x64"
echo "  - Linux (ARM64): spectral-linux-arm64"
echo "  - macOS (x64): spectral-macos-x64"
echo "  - macOS (ARM64): spectral-macos-arm64"
echo "  - Alpine (x64): spectral-alpine-x64"
echo "  - Alpine (ARM64): spectral-alpine-arm64"
