# Burpcord Release Notes

## [v2.2.1] - 2026-02-09
### 🐛 Bug Fixes
- **Shadow JAR Packaging**: Fixed `ClassNotFoundException: com.jagrosh.discordipc.IPCListener` caused by the plain `jar` task overwriting the fat shadow JAR during publish. Ensured `shadowJar` always runs last.
- **UTF-8 Encoding**: Fixed unmappable character warning on Windows by setting `compileJava` and `javadoc` encoding to UTF-8.

### 🔧 Improvements
- **Build System**: Made GPG signing conditional (only when keys are available), allowing GitHub Packages publish without GPG configuration.

---

## [v2.2.0] - 2026-02-09
### 🐛 Bug Fixes
- **IPC Handshake NPE**: Fixed `Cannot invoke "JsonObject.getAsJsonObject(String)" because "data" is null` crash on connect when Discord is not fully ready. Added retry-with-backoff (3 attempts, exponential delay) on a background thread.
- **IPC Shutdown**: Upgraded DiscordIPC library which re-adds null support for `sendRichPresence` calls, required for clean IPC shutdown due to recent Discord breakage.

### 🔧 Improvements
- **Dependency Upgrade**: Upgraded DiscordIPC from `0.10.2` → `0.11.2` (new Maven group ID `io.github.cdagaming`), picking up updated Gson (2.13.1), SLF4J (2.0.17), new RPC fields (`status_display_type`, `name`, URL support for images/state/details), and null-safe presence cleanup.
- **API Migration**: Migrated all `setLargeImage`/`setSmallImage` calls to new `setLargeImageWithTooltip`/`setSmallImageWithTooltip` API to match DiscordIPC 0.11.x breaking changes.
- **Connection Resilience**: IPC connection now runs on a dedicated daemon thread (`Burpcord-IPC-Connect`) with exponential backoff retries, preventing extension load failures when Discord is slow to initialize.

---

## [v2.1.0] - 2026-01-28
### 🚀 Major Refactoring & Improvements
- **Component Priority System**: Introduced a robust priority system (`getPriority()`) for `ActivityProvider`s from level 10 to 80, ensuring deterministic and logical component registration order.
- **OOP Registration Logic**: Refactored all listeners (`Proxy`, `Scanner`, `Intruder`, `Repeater`, `WebSocket`) to implement a common `BurpComponent` interface, allowing self-contained registration via `register(MontoyaApi)`.
- **Streamlined Initialization**: Rewrote `BurpcordExtension` to use a clean list-based initialization pattern, significantly reducing boilerplate code and improving maintainability.

### 🐛 Bug Fixes
- **Runtime Crash**: Resolved `ClassNotFoundException` wrapper issues by implementing correct ShadowJar configuration with dependency relocation.
- **RPC Stability**: Fixed an issue where invalid `ActivityType` defaults caused immediate RPC disconnections.
- **UI Sync**: Fixed a race condition where the UI connection status indicator would remain "Disconnected" despite a successful RPC connection.

### 🛠 Technical Changes
- **Project Structure**: Organized listener classes into logical `providers` and `handlers` subpackages for better codebase navigation.
- **Initialization**: Decoupled component instantiation from registration logic using the new priority sorting mechanism.

---

## [v2.0.0] - 2026-01-28
### ✨ New Features
- **Modular UI Architecture**: Decomposed the monolithic `BurpcordSettingsTab` into distinct, maintainable panels:
  - `SettingsPanel`: General configuration options.
  - `LogPanel`: Dedicated scrolling log viewer.
  - `AboutPanel`: Version and author information.
  - `HelpPanel`: Support and documentation links.
- **New Activity Providers**: Added support for tracking:
  - **Site Map**: Tracks project site map size with caching performance optimizations.
  - **Scope**: Monitors target scope configuration status.
  - **Collaborator**: Tracks Burp Collaborator interactions.
- **Provider Pattern**: Introduced the `ActivityProvider` architecture to decentralize status logic from the main RPC manager.

### 🔧 Improvements
- **Package Migration**: Migrated root package from `com.burpcord` to `tech.chron0.burpcord` to adhere to standard Java naming conventions.
- **Enhanced Logging**: Implemented a new ASCII art banner start-up log and improved console output formatting.
- **Documentation**: Restored and standardized Javadocs across the entire codebase.

---

## [v1.5.2] - 2026-01-27
### 🚀 New Features
- **Global RPC Switch**: Added a master toggle to enable/disable Discord RPC entirely without unloading the extension.
- **Rich Presence Toggles**: Added granular control to show/hide specific tools in Discord status.

### 🐛 Bug Fixes
- **Connection Lifecycle**: Fixed issues where the Discord IPC connection would not close cleanly upon extension unload.

---

## [v1.4.0] - 2026-01-27
### ⚡ Performance
- **Site Map Caching**: Added caching mechanisms for large project performance, preventing UI freezes when querying site map size.
- **Documentation**: Updated project documentation to include PortSwigger-provided BApp info.

## [v1.3.2] - 2026-01-27
### 🎨 UI & Logging
- **Enhanced Logs**: Added more context to the custom logging section of the extension tab.
- **Styling**: Improved overall UI styling and log formatting.

## [v1.3.1] - 2026-01-27
### 🐛 Bug Fixes
- **RPC Cleanup**: Fixed issues with sending empty presence updates before disconnection.

## [v1.3.0] - 2026-01-27
### ✨ New Features
- **Expanded Tracking**: Added tracking for Burp Version, Edition, Scope stats, Proxy history, and Site Map size.
- **WebSocket Support**: Added support for monitoring WebSocket traffic activity.
- **Collaborator**: Added initial stats tracking for Collaborator.

## [v1.2.0] - 2026-01-27
### 🚀 Features
- **RPC Toggle**: Added the ability to toggle RPC without unloading the extension.
- **Intruder Tracking**: Added support for tracking Intruder attacks (Fuzzing/Brute-forcing).
- **Custom State**: Allowed users to set custom text for their status state.

## [v1.1.0] - 2026-01-27
### 🛠 Technical
- **GUI Handling**: Implemented usage of Burp's GUI Parent Frame for better dialog modal handling.
- **Threading**: Improved background thread exception handling.
- **License**: Added project LICENSE file.

---

## [v1.0.0] - 2026-01-05
### 🎉 Initial Release
- **Core Functionality**:
  - Integration of Discord Rich Presence into Burp Suite.
  - Support for tracking Proxy, Scanner, Repeater, and Intruder activity.
- **Basic UI**: Integrated settings tab within Burp Suite.
- **Configuration**: Persistence for App ID and basic settings.
- **Foundation**: Basic implementation of the Discord IPC connection.
