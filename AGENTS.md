# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Burpcord is a Burp Suite extension that integrates Discord Rich Presence functionality. It displays real-time Burp Suite activity (Intercepting, Scanning, Proxying, Repeater) on Discord profiles. The extension is built using Java and the Burp Suite Montoya API.

## Build System

This project uses **Gradle** with Java 21. Key build files:
- `build.gradle` - Main build configuration
- `settings.gradle` - Project settings
- `Makefile` - Windows-specific convenience wrapper for Gradle commands

### Common Commands

**Build the JAR:**
```powershell
.\gradlew.bat jar
```
Or via Makefile:
```powershell
make build
```

The compiled JAR will be located at: `build/libs/Burpcord-1.0.jar`

**Clean build artifacts:**
```powershell
.\gradlew.bat clean
```
Or via Makefile:
```powershell
make clean
```

**Note:** The `gradlew.bat` wrapper script must be used on Windows (not `./gradlew`).

## Architecture

### Extension Entry Point
`BurpcordExtension` (implements `BurpExtension`) is the main entry point:
- Initializes the Discord RPC manager
- Registers handlers for Proxy, Scanner, and Repeater
- Creates the Settings UI tab
- Implements proper cleanup via `ExtensionUnloadingHandler`

### Core Components

**DiscordRPCManager** - Central coordinator managing:
- Discord IPC connection lifecycle using the DiscordIPC library
- Periodic status updates via `ScheduledExecutorService`
- Atomic counters for requests, responses, and vulnerabilities
- Status rotation when multiple activities are active
- Priority-based status determination (Intercept > Scanner > Proxy > Repeater)

**BurpcordConfig** - Configuration persistence layer:
- Uses Burp's `Preferences` API for storage
- Manages Discord App ID, update interval, and feature toggles
- Provides default values when preferences are unset

**Event Handlers:**
- `BurpcordProxyHandler` - Tracks proxy requests/responses and intercept status
- `BurpcordScannerListener` - Monitors active/passive scans and vulnerability counts
- `BurpcordRepeaterListener` - Detects Repeater tool activity via `ToolSource`

**BurpcordSettingsTab** - GUI configuration interface for users

### Threading Model

- Discord RPC updates run on a single-threaded `ScheduledExecutorService`
- Event handlers execute on Burp's threads (must be fast to avoid blocking)
- All shared state uses `AtomicInteger` and `AtomicBoolean` for thread safety
- Scheduler is properly shutdown in `extensionUnloaded()` to prevent resource leaks

### Status Priority Logic

Status updates follow this priority order (highest to lowest):
1. **Intercepting** - Active when proxy traffic is being intercepted (clears after 5s inactivity)
2. **Scanning** - Shows when scan activity occurred in last 60s OR vulnerabilities detected
3. **Proxy** - Displays request/response counts when proxy is active
4. **Repeater** - Shows when Repeater was used in last 60s

When multiple statuses are active, they rotate on each update interval.

## Dependencies

**Compile-only:**
- `montoya-api:2025.12` - Burp Suite extension API (not bundled in JAR)

**Implementation (bundled):**
- `DiscordIPC:0.10.2` - Discord Rich Presence client library
- `gson:2.10.1` - JSON serialization for Discord IPC

The `jar` task creates a fat JAR with all runtime dependencies using `DuplicatesStrategy.EXCLUDE`.

## BApp Store Compliance

When modifying this extension, ensure compliance with BApp Store acceptance criteria (see `docs/BApp-Store-Acceptance-Criteria.md`):

**Critical requirements:**
- All slow operations must run in background threads (never block Swing EDT)
- Use `Extension.registerUnloadingHandler()` to clean up resources (especially the scheduler)
- Prefer Burp's `Http.issueHttpRequest()` over direct HTTP libraries
- Avoid keeping long-term references to `HttpRequestResponse` objects
- GUI elements should use `SwingUtils.suiteFrame()` as parent
- Extension must handle large projects efficiently

**Threading considerations:**
- Wrap background threads in try/catch and log exceptions to extension error stream
- Burp does not catch exceptions in background threads automatically
- The scheduler must be properly terminated in `extensionUnloaded()`

## Discord Integration

The extension requires a Discord Application ID to function. Users can:
- Use the default App ID (`1457789708753965206`)
- Create their own via Discord Developer Portal for custom branding

**Rich Presence assets:**
- Large image key must be named `burp` in Discord Developer Portal
- Start timestamp persists across status updates for accurate session time tracking

## Testing Notes

There is no automated test suite. Manual testing requires:
1. Loading the JAR in Burp Suite via Extensions → Installed → Add
2. Verifying Discord is running and Game Activity is enabled
3. Testing each feature (Intercept, Scanner, Proxy, Repeater) individually
4. Checking the Burpcord tab appears and settings persist
5. Verifying proper cleanup when unloading the extension

## Common Development Scenarios

**Adding a new status type:**
1. Add tracking variables to `DiscordRPCManager` (use `Atomic*` types)
2. Create or modify a listener/handler to update those variables
3. Update `updateStatusFromStats()` to include the new status in priority logic
4. Add configuration toggle in `BurpcordConfig` if user-configurable

**Modifying status priority:**
Edit the conditional logic order in `DiscordRPCManager.updateStatusFromStats()`

**Changing update frequency:**
Users configure this via Settings tab. Default is 5 seconds (`DEFAULT_UPDATE_INTERVAL`).

**Debugging Discord IPC issues:**
Check Burp's extension output/error streams. The `IPCListener` logs connection events.
