# AGENTS.md

This file provides guidance to AI agents and WARP (warp.dev) when working with code in this repository.

## Documentation References

- See `@docs/portswigger/CLAUDE.md` for official Montoya API guidance
- See `@docs/portswigger/bapp-store-requirements.md` for BApp Store submission criteria
- See `@docs/portswigger/development-best-practices.md` for development guidelines
- See `@docs/portswigger/montoya-api-examples.md` for API code patterns
- See `@docs/portswigger/resources.md` for external links

## Project Overview

Burpcord is a Burp Suite extension that integrates Discord Rich Presence functionality. It displays real-time Burp Suite activity on Discord profiles, including security testing status, scan results, tool usage, and advanced metrics via the Montoya API. The extension is built using Java 21 and the Burp Suite Montoya API.

**Current Version:** 2.5.2

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

The compiled JAR will be located at: `build/libs/Burpcord-X.X.jar`

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
- Registers handlers for Proxy, Scanner, Repeater, Intruder, and WebSockets
- Creates the Settings UI tab
- Implements proper cleanup via `ExtensionUnloadingHandler` and a JVM shutdown hook

### Core Components

**DiscordRPCManager** - Central coordinator managing:

- Discord IPC connection lifecycle using the DiscordIPC library
- Periodic status updates via `ScheduledExecutorService`
- Atomic counters for requests, responses, and vulnerabilities
- Idempotent shutdown with `AtomicBoolean` guard and explicit presence clearing
- Status rotation when multiple activities are active
- Priority-based status determination
- Montoya API integrations for proxy history, site map, scope, and Collaborator
- WebSocket message tracking

**BurpcordConfig** - Configuration persistence layer:

- Uses Burp's `Preferences` API for storage
- Manages Discord App ID, update interval, and feature toggles
- Provides default values when preferences are unset
- v1.3 features: site map, scope, collaborator, websockets toggles

**Event Handlers:**

- `BurpcordProxyHandler` - Tracks proxy requests/responses and intercept status
- `BurpcordScannerListener` - Monitors active/passive scans and vulnerability counts
- `BurpcordRepeaterListener` - Detects Repeater tool activity via `ToolSource`
- `BurpcordIntruderListener` - Tracks Intruder attack requests via `HttpHandler`
- `BurpcordWebSocketListener` - Monitors WebSocket messages via `WebSocketCreatedHandler`

**BurpcordSettingsTab** - GUI configuration interface:

- Reload RPC button for quick reconnection
- App ID and interval configuration
- Custom state text field
- Feature toggles for all status types
- Built-in log viewer with timestamps

### Threading Model

- Discord RPC updates run on a single-threaded `ScheduledExecutorService`
- Event handlers execute on Burp's threads (must be fast to avoid blocking)
- All shared state uses `AtomicInteger` and `AtomicBoolean` for thread safety
- Scheduler is properly terminated in `extensionUnloaded()` to prevent resource leaks
- JVM shutdown hook as safety net; `AtomicBoolean` guard prevents double-shutdown
- UI logging uses `SwingUtilities.invokeLater()` for thread-safe updates

### Status Priority Logic

Status updates follow this priority order (highest to lowest):

1. **Intercepting** - Active when proxy traffic is being intercepted (clears after 5s inactivity)
2. **Scanning** - Shows when scan activity occurred in last 60s OR vulnerabilities detected
3. **Proxy** - Displays accurate request count from Montoya API
4. **Repeater** - Shows when Repeater was used in last 60s
5. **Intruder** - Shows when Intruder attack is active (last 60s)
6. **Site Map** - Displays mapped endpoint count
7. **Scope** - Shows unique target count in scope
8. **Collaborator** - Shows OOB interaction hits (Pro only)
9. **WebSocket** - Shows message count when WebSocket activity detected

When multiple statuses are active, they rotate on each update interval.

## Dependencies

**Compile-only:**

- `montoya-api:2025.12` - Burp Suite extension API (not bundled in JAR)

**Implementation (bundled):**

- `DiscordIPC:0.11.3` - Discord Rich Presence client library
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
3. Testing each feature (Intercept, Scanner, Proxy, Repeater, Intruder) individually
4. Testing v1.3 features (Site Map, Scope, Collaborator, WebSockets)
5. Checking the Burpcord tab appears and settings persist
6. Verifying proper cleanup when unloading the extension
7. Testing the log viewer displays connection events

## Common Development Scenarios

**Adding a new status type:**

1. Add tracking variables to `DiscordRPCManager` (use `Atomic*` types)
2. Create or modify a listener/handler to update those variables
3. Update `updateStatusFromStats()` to include the new status in priority logic
4. Add configuration toggle in `BurpcordConfig` if user-configurable
5. Add checkbox to `BurpcordSettingsTab`

**Modifying status priority:**
Edit the conditional logic order in `DiscordRPCManager.updateStatusFromStats()`

**Changing update frequency:**
Users configure this via Settings tab. Default is 5 seconds (`DEFAULT_UPDATE_INTERVAL`).

**Debugging Discord IPC issues:**

1. Check the built-in log viewer in the Burpcord tab
2. Check Burp's extension output/error streams
3. The `IPCListener` logs connection events

**Adding log entries:**
Call `BurpcordSettingsTab.log("message")` from anywhere in the codebase.

## File Structure

```tree
src/main/java/com/burpcord/
├── BurpcordExtension.java      # Entry point, registers all handlers
├── BurpcordConfig.java         # Configuration persistence
├── DiscordRPCManager.java      # Core RPC logic and Montoya integrations
├── BurpcordSettingsTab.java    # GUI tab with settings and log viewer
├── BurpcordProxyHandler.java   # Proxy request/response tracking
├── BurpcordScannerListener.java # Scanner activity monitoring
├── BurpcordRepeaterListener.java # Repeater activity detection
├── BurpcordIntruderListener.java # Intruder attack tracking
└── BurpcordWebSocketListener.java # WebSocket message tracking
```
