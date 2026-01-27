# Burpcord - Discord Rich Presence for Burp Suite

Burpcord is a Burp Suite extension that integrates with Discord Rich Presence. It displays your current Burp Suite activity on your Discord profile in real-time, including security testing status, scan results, and tool usage statistics.

## Features

### Core Activity Tracking
- **Intercept**: Shows when you are intercepting traffic with request count
- **Scanner**: Displays active/passive scan status and vulnerability counts (High/Medium)
- **Proxy**: Shows accurate request count from proxy history
- **Repeater**: Indicates when you are manually testing requests
- **Intruder**: Tracks active Intruder attacks with request count

### Advanced Features (v1.3)
- **Burp Version Display**: Shows edition and version (e.g., "Burp Suite Professional 2026.1.2")
- **Site Map Stats**: Displays mapped endpoint count
- **Scope Targets**: Shows number of unique hosts in scope
- **Collaborator Tracking**: Counts out-of-band (OOB) interaction hits (Pro only)
- **WebSocket Activity**: Tracks WebSocket message counts

### User Experience
- **Custom State Text**: Personalize the status line shown on your Discord profile
- **Reload RPC Button**: Quickly reconnect to Discord without reloading the extension
- **Built-in Log Viewer**: View extension logs without switching tabs
- **Feature Toggles**: Enable/disable individual features via Settings tab
- **Priority System**: Intelligently rotates between active statuses

## Installation

1. **Download**: Get the latest `Burpcord-X.X.jar` from the [Releases](https://github.com/jondmarien/burpcord/releases) page.
2. **Load in Burp Suite**:
   - Open Burp Suite
   - Go to **Extensions** → **Installed**
   - Click **Add**
   - Select **Extension type**: Java
   - Select the downloaded `.jar` file
3. **Verify**: You should see "Burpcord" in the extensions list and a new tab named "Burpcord" in the main UI.

## Configuration

Navigate to the **Burpcord** tab in Burp Suite to configure the extension.

### General Settings
- **Reload Discord RPC**: Reconnects to Discord (useful if status gets stuck)
- **Discord App ID**: The Client ID of your Discord Application
- **Update Interval**: How frequently (in seconds) the extension updates status (Default: 5s)
- **Custom State Text**: Your personalized status line (e.g., "Hacking :)")

### Tool Activity Features
Toggle which tool activities you want displayed:
- Show Intercept Status
- Show Scanner Status
- Show Proxy Status
- Show Repeater Status
- Show Intruder Status

### Advanced Features (v1.3)
- Show Site Map Stats
- Show Scope Target Count
- Show Collaborator Hits (Pro Only)
- Show WebSocket Activity

### Extension Logs
A built-in log viewer displays connection events, errors, and status updates without needing to check the Extensions tab.

## Setup Guide: Discord Developer Portal

To use Burpcord, you need a Discord Application ID. You can use the default or create your own to customize the name and icons.

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications).
2. Click **New Application**.
3. Name your application (e.g., "Burp Suite Professional"). This name will appear as "Playing **Burp Suite Professional**".
4. Copy the **Application ID** (Client ID) from the **General Information** page. Paste this into the Burpcord Settings tab.

### Uploading Rich Presence Assets (Images)

To display the Burp Suite logo or other icons:

1. In your Discord Application, go to **Rich Presence** → **Art Assets**.
2. **Add Image(s)**:
   - Upload a large image (e.g., Burp Suite logo) and name the key `burp`. (The extension is configured to look for an image with the key `burp`).

## Building from Source

Requirements:
- JDK 21+
- Gradle

```bash
git clone https://github.com/jondmarien/burpcord.git
cd burpcord
./gradlew jar  # Linux/macOS
.\gradlew.bat jar  # Windows
```

The compiled jar will be in `build/libs/Burpcord-1.3.jar`.

## Troubleshooting

- **Status not showing?**: Ensure "Display current activity as a status message" is enabled in Discord User Settings → Activity Privacy.
- **"Burpcord" tab missing?**: Check the **Extensions** → **Errors** tab in Burp Suite for any stack traces.
- **Status stuck after closing Burp?**: This is Discord-side caching; restart Discord to clear it.
- **Check the logs**: Use the built-in log viewer in the Burpcord tab to see connection status and errors.

## License

MIT