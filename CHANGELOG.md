# Changelog

## [2.0.0] - 2026-01-28
### Added
- **Refactored Architecture**: Complete restructuring of the project into logical domains (`core`, `config`, `ui`, `discord`, `listeners`) for better scalability and maintenance.
- **Provider Pattern**: Introduced `ActivityProvider` interface and registry in `DiscordRPCManager` to decentralize status logic.
- **New Providers**: Added dedicated providers for Site Map, Scope, and Burp Collaborator activity tracking.
- **Modular UI**: Decomposed the monolithic `BurpcordSettingsTab` into focused components: `SettingsPanel`, `LogPanel`, `AboutPanel`, and `HelpPanel`.
- **Collaborator Support**: Added explicit tracking and Discord status for Burp Collaborator interactions.

### Changed
- **Package Rename**: Migrated from `com.burpcord` to `tech.chron0.burpcord` to adhere to Java naming conventions.
- **Enhanced Javadocs**: Restored and standardized detailed Javadocs across the entire codebase.
- **Version Bump**: Updated project version to 2.0.0.
- **Logging**: Improved console output formatting with clean ASCII art banners and standardized status messages.

## [1.5.2] - 2026-01-27
### Added
- **Rich Presence Features**: Added toggles for showing/hiding specific Burp tools (Proxy, Scanner, Repeater, Intruder, etc.).
- **Scope & Site Map**: Added support for displaying Site Map size and Scope focus in Discord status.
- **Configurable RPC**: Added a global toggle for enabling/disabling Discord RPC without unloading the extension.
- **Settings UI**: clear tooltips and improved layout for configuration options.

### Fixed
- **Performance**: Added caching mechanisms for improved performance with large projects.
- **Connection Handling**: Fixed issues where the RPC connection wouldn't close properly on disconnect.
- **Documentation**: Standardized existing Javadocs and added BApp Store submission criteria.

## [1.0.0] - 2026-01-05
### Added
- **Initial Release**: First stable release of Burpcord.
- **Core Tracking**: Support for Proxy, Scanner, Repeater, and Intruder activity.
- **UI Integration**: Basic settings tab integrated into Burp Suite.
- **Configuration**: Persistence for App ID and basic settings.
- **Discord RPC**: Fundamental implementation of Discord Rich Presence connection.
