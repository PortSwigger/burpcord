# Burpcord - Discord Rich Presence for Burp Suite

[![GitHub Package](https://img.shields.io/badge/GitHub-Packages-blue)](https://github.com/jondmarien/Burpcord/packages)
[![Version](https://img.shields.io/badge/v2.1.0-blue.svg)](https://github.com/jondmarien/Burpcord/releases/tag/v2.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Burpcord v2.1.0** is a complete rewrite of the original Burp Suite extension that integrates with Discord Rich Presence. It displays your active security testing workflow on your Discord profile in real-time, built with a robust, modular architecture.

## 🚀 Features

### Core Activity Tracking (Real-Time)
- **Intercept**: Shows when you are actively intercepting traffic (Requests captured).
- **Scanner**: Displays active/passive scan status, issue counts, and severity (High/Medium).
- **Proxy**: Tracks request history count directly from the HTTP Proxy.
- **Repeater**: Indicates manual testing sessions.
- **Intruder**: Displays active active attacks and request counts.

### Advanced Metrics
- **Burp Version**: Displays edition & version (e.g., "Burp Suite Professional 2026.1.2").
- **Project Stats**:
    - **Site Map**: Tracks mapped endpoints.
    - **Scope**: Counts unique in-scope targets.
    - **WebSockets**: Monitors active WebSocket message flow.
- **Burp Collaborator**: (Pro Only) Real-time tracking of Out-of-Band (OOB) interactions.

### User Experience
- **Smart Status System**:
  - **Priority Queue**: Intelligently rotates status based on what you are *actually* doing (e.g., Intercept > Scanning > Idle).
  - **Custom States**: Set your own status message (e.g., "Bug Bounty Hunting").
- **Robust Connection**:
  - **Auto-Reconnect**: Built-in "Reload RPC" button to fix connection issues instantly.
  - **Status Indicator**: Visual feedback in the UI showing connection state (Connected/Disconnected).
- **Embedded Logging**:
  - **Dual Logging**: Logs to both the specialized **Burpcord Log Panel** and Burp's native **Output** tab.
  - **Verbose Debugging**: Detailed connection events and error traces.

## 🛠️ Installation

1. **Download**: Get the latest `Burpcord-2.1.0.jar` from the [Releases](https://github.com/jondmarien/burpcord/releases) page.
2. **Load in Burp Suite**:
   - Go to **Extensions** → **Installed**.
   - Click **Add**.
   - Select **Extension type**: Java.
   - Select the downloaded `.jar` file.
3. **Verify**: You will see a "Burpcord" tab appear. The status bar at the top should turn **Green** ("Connected to Discord").

## ⚙️ Configuration

Navigate to the **Burpcord** tab to customize your experience:

### General
- **Discord App ID**: Your unique Discord Application ID (Defaults to the official Burpcord ID).
- **Update Interval**: How often the status refreshes (Default: 30s).
- **Custom State Text**: Your personal tagline.
- **Reload RPC**: Use this button if your status gets stuck or Discord restarts.

### Feature Toggles
Enable or disable specific tracking modules:
- [x] Show Intercept
- [x] Show Scan Status
- [x] Show Proxy History
- [x] Show Repeater
- [x] Show Intruder
- [x] Show Site Map / Scope
- [x] Show Collaborator Hits (Pro)
- [x] Show WebSockets

## 🏗️ Building from Source

**Prerequisites**:
- JDK 21+
- Gradle 8.0+

Burpcord v2.1.0 uses the **ShadowJar** plugin to bundle dependencies and prevent runtime conflicts with Burp Suite.

```bash
git clone https://github.com/jondmarien/burpcord.git
cd burpcord

# Build the Fat JAR
./gradlew clean shadowJar
```

The compiled artifact will be located at:
`build/libs/Burpcord-2.1.0.jar`

## 🔧 Troubleshooting

- **"Disconnected" Status**: Click the **Reload Discord RPC** button in the top right.
- **No Status on Discord**:
  - Check **User Settings** → **Activity Privacy** → "Display current activity as a status message".
  - Ensure no other RPC apps are conflicting.
- **Logs**: Check the **Burpcord** tab's **Log Panel** or Burp's **Extensions** → **Output** tab for errors.

## 📝 License

MIT
