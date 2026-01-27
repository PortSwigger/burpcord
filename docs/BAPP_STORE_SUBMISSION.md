# Burpcord - BApp Store Submission

## Extension Overview

Burpcord integrates Discord Rich Presence with Burp Suite, displaying your real-time security testing activity on your Discord profile. Share your work status with teammates, friends, or the security community while you hunt for vulnerabilities.

Whether you're intercepting traffic, running scans, fuzzing with Intruder, or testing in Repeater, Burpcord keeps your Discord status updated automatically. It's perfect for security researchers who want to showcase their workflow, streamers demonstrating Burp Suite, or teams who want visibility into each other's testing activity.

---

## Key Features

- **Real-Time Activity Tracking** - Automatically detects and displays your current Burp Suite activity
- **Intercept Status** - Shows when you're actively intercepting proxy traffic with request count
- **Scanner Integration** - Displays vulnerability counts (High/Medium severity findings)
- **Repeater Tracking** - Indicates active manual testing sessions with request count
- **Intruder Monitoring** - Shows ongoing fuzzing attacks with payload count
- **Proxy Statistics** - Displays total proxied requests and responses
- **Session Timer** - Shows how long you've been using Burp Suite
- **Customizable State Text** - Set your own status message (e.g., "Bug Bounty Hunting")
- **RPC Toggle** - Quickly enable/disable Discord presence without unloading the extension
- **Configurable Features** - Toggle individual status types on/off
- **Persistent Settings** - All preferences saved between sessions

---

## Usage Instructions

### Installation

1. Download the Burpcord JAR file
2. In Burp Suite, go to **Extensions** → **Installed**
3. Click **Add** and select the downloaded JAR file
4. Ensure Discord is running on your computer
5. Burpcord will automatically connect and display your status

### Configuration

1. Navigate to the **Burpcord** tab in Burp Suite
2. Configure your preferences:
   - **Enable Discord Rich Presence**: Master toggle to enable/disable the extension
   - **Discord App ID**: Use the default or enter your own Discord application ID
   - **Update Interval**: How often the status updates (1-60 seconds)
   - **Custom State Text**: Your personalized status message
3. Toggle individual features:
   - Show Intercept Status
   - Show Scanner Status
   - Show Proxy Status
   - Show Repeater Status
   - Show Intruder Status
4. Click **Save All Settings** to apply changes

### Discord App Setup (Optional)

If you want custom branding for your presence:

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Upload assets (large/small images) under **Rich Presence** → **Art Assets**
4. Copy the **Application ID** and paste it in Burpcord settings

### Troubleshooting

- **Status not showing**: Ensure Discord is running and Activity Status is enabled in Discord settings
- **Connection failed**: Restart Discord, then reload the extension
- **Wrong images**: Verify your Discord App ID is correct and assets are uploaded

---

## Requirements

- Burp Suite (Community or Professional)
- Discord desktop client running locally
- Java 21 or higher

---

## Privacy Note

Burpcord only displays activity type and statistics. It does **not** share target URLs, request contents, or any sensitive testing data with Discord.
