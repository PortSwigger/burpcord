# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Burpcord is a Python Discord Rich Presence (RPC) integration for Burp Suite. It displays your Burp Suite activity (e.g., vulnerability scanning) as your Discord status, allowing you to share what you're working on with other Discord users.

## Architecture

The project is minimal with a single main script:
- `api.py`: The core module that initializes a Discord RPC connection using the `pypresence` library. It establishes a connection with Discord and periodically updates the RPC state to show that the user is scanning for vulnerabilities.

The application is designed to run as a Burp Suite extension (via Jython) or as a standalone Python script, displaying persistent status updates to Discord.

## Setup and Dependencies

**Required Python Package:**
- `pypresence`: For Discord Rich Presence communication

To install dependencies:
```
pip install pypresence
```

## Common Commands

**Run the script:**
```
python api.py
```

**Build executable (using PyInstaller):**
```
pyinstaller --onefile api.py
```
The executable will be generated in the `dist/` directory.

## Burp Suite Integration

The original design intended to integrate with Burp Suite via Jython:
1. Download Jython standalone JAR
2. Run the installer (install.bat) and configure the installation folder
3. In Burp Suite: Extensions → Extension Settings → Python Environment → Select jython.jar
4. Add the extension by pointing to this script

**Note:** This approach is deprecated. Modern Burp Suite extensions use native Python support or the Burp Extension API directly.

## Development Notes

- The RPC connection displays a hardcoded Discord client ID (`993442275533271050`)
- Status updates occur every 15 seconds while the script is running
- The script shows "Scanning for Vulnerabilities" as the default state with Burp Suite imagery

## Python Version

The project targets Python 3.10+ (as evidenced by the cached .pyc files and modern syntax).
