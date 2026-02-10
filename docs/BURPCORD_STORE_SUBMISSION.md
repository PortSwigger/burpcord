# Burpcord - BApp Store Submission

## Extension Overview

Burpcord integrates Discord Rich Presence with Burp Suite, displaying your real-time security testing activity on your Discord profile. Share your work status with teammates, friends, or the security community while you hunt for vulnerabilities.

Whether you're intercepting traffic, running scans, fuzzing with Intruder, or testing in Repeater, Burpcord keeps your Discord status updated automatically. It's perfect for security researchers who want to showcase their workflow, streamers demonstrating Burp Suite, or teams who want visibility into each other's testing activity.

---

## Key Features

### Activity Tracking
- **Real-Time Activity Tracking** - Automatically detects and displays your current Burp Suite activity
- **Intercept Status** - Shows when you're actively intercepting proxy traffic with request count
- **Scanner Integration** - Displays vulnerability counts (High/Medium severity findings)
- **Repeater Tracking** - Indicates active manual testing sessions with request count
- **Intruder Monitoring** - Shows ongoing fuzzing attacks with payload count
- **Proxy Statistics** - Displays total proxied requests and responses
- **Session Timer** - Shows how long you've been using Burp Suite

### Advanced Monitoring
- **Site Map Tracking** - Displays mapped endpoint count from Burp's site map
- **Scope Tracking** - Shows unique in-scope target count
- **Collaborator Integration** - Displays OOB interaction hits (Pro only)
- **WebSocket Monitoring** - Tracks WebSocket message counts

### Customization & Configuration
- **Customizable State Text** - Set your own status message (e.g., "Bug Bounty Hunting")
- **RPC Toggle** - Quickly enable/disable Discord presence without unloading the extension
- **Configurable Features** - Toggle individual status types on/off
- **Persistent Settings** - All preferences saved between sessions
- **Built-in Log Viewer** - View extension events and Discord connection status

### Architecture & Performance
- **Modular Architecture** - Completely refactored codebase using the Provider pattern for stability and scalability
- **Optimized Performance** - Smart caching (30s TTL) and priority-based updates ensure zero impact on Burp Suite
- **Robust Connection** - Improved Discord RPC lifecycle management and connection stability
- **BApp Store Compliance** - Full compliance with PortSwigger submission criteria

---

## Usage Instructions

### Installation

1. Download the latest `burpcord-all.jar` from Releases (or build via `./gradlew shadowJar`)
2. In Burp Suite, go to **Extensions** → **Installed**
3. Click **Add** and select the JAR file
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

- Burp Suite (Community or Professional) 2023.1 or later (Montoya API)
- Discord desktop client running locally
- Java 21 or higher

---

## Version History

### [v2.4.1] - 2026-02-10
- **Code Cleanup**: Removed hardcoded version tags from source files — version is now single-sourced

### [v2.4.0] - 2026-02-10
- **RPC Persists After Close**: Fixed critical issue where Discord presence lingered after Burp Suite closed
- **JVM Shutdown Hook**: Safety net ensures presence is always cleared, even during abnormal exits
- **Idempotent Shutdown**: Thread-safe shutdown logic prevents double-execution
- **Code Cleanup**: Removed unused `isConnected` field

### [v2.3.0] - 2026-02-09
- **Default App ID Fix**: Corrected incorrect default Discord Application ID that returned HTTP 404
- **StatusDisplayType NPE**: Fixed crash from uninitialized `statusDisplayType` field in DiscordIPC 0.11.2
- **App ID Validation**: Invalid App IDs now fail fast with a clear error message
- **Connect Timeout**: 10-second timeout per IPC attempt prevents indefinite blocking
- **Retry Logic**: 5 retries with capped exponential backoff (3s → 30s max)

### [v2.2.1] - 2026-02-09
- **Shadow JAR Packaging**: Fixed `ClassNotFoundException` caused by thin JAR overwriting fat JAR
- **UTF-8 Encoding**: Fixed unmappable character warning on Windows
- **Build System**: Conditional GPG signing for GitHub Packages publish

### [v2.2.0] - 2026-02-09
- **IPC Handshake NPE**: Fixed null `data` crash with retry-with-backoff on a background thread
- **Dependency Upgrade**: DiscordIPC `0.10.2` → `0.11.2` with new API and null-safe presence cleanup
- **API Migration**: Migrated to `setLargeImageWithTooltip`/`setSmallImageWithTooltip` API
- **Connection Resilience**: Dedicated daemon thread with exponential backoff retries

### [v2.1.0] - 2026-01-28
- **Major Refactoring**: Introduced component priority system and OOP registration logic
- **Shadow Jar**: Improved build process with dependency relocation to fix runtime crashes
- **RPC Stability**: Fixes for invalid ActivityType and UI sync race conditions
- **Structure**: Organized codebase into providers and handlers subpackages

### [v2.0.0] - 2026-01-28
- **Modular UI**: Decomposed settings tab into Settings, Log, About, and Help panels
- **New Providers**: Site Map, Scope, and Collaborator tracking support
- **Architecture**: Implemented ActivityProvider pattern for decentralized logic
- **Logging**: ASCII art banners and improved console output

### [v1.5.2] - 2026-01-27
- **Global RPC Switch**: Master toggle to enable/disable Discord RPC without unloading
- **Rich Presence Toggles**: Granular control to show/hide specific Burp tools

### [v1.4.0] - 2026-01-27
- **Performance**: Site Map caching for large projects
- **Compliance**: Updates for BApp Store requirements

### [v1.3.2] - 2026-01-27
- **UI & Logging**: Enhanced logs and context for custom logging section

### [v1.3.1] - 2026-01-27
- **Bug Fixes**: Cleanup of RPC presence before disconnection

### [v1.3.0] - 2026-01-27
- **New Features**: WebSocket monitoring, Burp version/edition/scope tracking

### [v1.2.0] - 2026-01-27
- **Features**: Intruder tracking, custom state text, RPC toggle

### [v1.1.0] - 2026-01-27
- **Technical**: Improved GUI threading and modal handling

### [v1.0.0] - 2026-01-05
- **Initial Release**: Basic Discord RPC, core tool tracking (Proxy, Scanner, Repeater, Intruder)

---

## Privacy Note

Burpcord only displays activity type and statistics. It does **not** share target URLs, request contents, or any sensitive testing data with Discord, nor does it store any kind of user or system data.
