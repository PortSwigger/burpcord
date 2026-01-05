# Burpcord - Discord Rich Presence for Burp Suite

Burpcord is a Burp Suite extension that integrates with Discord Rich Presence. It displays your current Burp Suite activity (Intercepting, Scanning, Proxying, Repeater) on your Discord profile, allowing you to share your security research status with others.

## Features

- **Real-time Status Updates**: Automatically updates your Discord status based on Burp Suite activity.
- **Activity Tracking**:
  - **Intercept**: Shows when you are intercepting traffic.
  - **Scanner**: Displays active/passive scan status and vulnerability counts.
  - **Proxy**: Shows request/response counts when proxying traffic.
  - **Repeater**: Indicates when you are manually testing requests.
- **Priority System**: Intelligently prioritizes statuses (e.g., Intercepting > Scanning > Proxy).
- **Customizable**: Configure App ID, Update Interval, and toggle specific features via the Settings Tab.

## Installation

1.  **Download**: Get the latest `burpcord-all.jar` from the [Releases](https://github.com/jondmarien/burpcord/releases) page.
2.  **Load in Burp Suite**:
    -   Open Burp Suite.
    -   Go to **Extensions** -> **Installed**.
    -   Click **Add**.
    -   Select **Extension type**: Java.
    -   Select the downloaded `.jar` file.
3.  **Verify**: You should see "Burpcord" in the extensions list and a new tab named "Burpcord" in the main UI.

## Configuration

Navigate to the **Burpcord** tab in Burp Suite to configure the extension.

-   **Discord App ID**: The Client ID of your Discord Application. (See setup guide below).
-   **Update Interval**: How frequently (in seconds) the extension updates your status (Default: 5s).
-   **Rich Presence Features**: Toggle which statuses you want to display (Intercept, Scanner, Proxy, Repeater).

**Note:** Changing the App ID or Interval performs a "soft restart" of the RPC connection.

## Setup Guide: Discord Developer Portal

To use Burpcord, you need a Discord Application ID. You can use the default or create your own to customize the name and icons.

1.  Go to the [Discord Developer Portal](https://discord.com/developers/applications).
2.  Click **New Application**.
3.  Name your application (e.g., "Burp Suite Professional"). This name will appear as "Playing **Burp Suite Professional**".
4.  Copy the **Application ID** (Client ID) from the **General Information** page. Paste this into the Burpcord Settings tab.

### Uploading Rich Presence Assets (Images)

To display the Burp Suite logo or other icons:

1.  In your Discord Application, go to **Rich Presence** -> **Art Assets**.
2.  **Add Image(s)**:
    -   Upload a large image (e.g., Burp Suite logo) and name the key `burp`. (The extension is configured to look for an image with the key `burp`).

## Building from Source

Requirements:
-   JDK 17+
-   Gradle

```bash
git clone https://github.com/yourusername/burpcord.git
cd burpcord
./gradlew shadowJar
```

The compiled jar will be in `build/libs/burpcord-all.jar`.

## Troubleshooting

-   **Status not showing?**: Ensure "Game Activity" is enabled in your Discord User Settings -> Activity Privacy.
-   **"Burpcord" tab missing?**: Check the **Extensions** -> **Errors** tab in Burp Suite for any stack traces.

## License

MIT