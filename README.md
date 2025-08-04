# QuPath Draggable Labels Extension

A QuPath extension that allows users to drag annotation labels to reposition them for better visualization and organization.

## Features

- Enable/disable draggable labels functionality
- Drag annotation labels to custom positions
- Label positions are persisted in annotation metadata
- Reset all label positions to defaults
- Works across different zoom levels

## Installation

1. Download the extension JAR file
2. Copy it to your QuPath extensions directory
3. Restart QuPath
4. The extension will appear under Extensions > Draggable Labels

## Usage

1. Open an image with annotations that have display names
2. Go to Extensions > Draggable Labels > Enable draggable labels
3. Click and drag any annotation label to reposition it
4. Use "Reset all label positions" to return labels to their default positions
5. Use "Disable draggable labels" to turn off the functionality

## Building

```bash
./gradlew build