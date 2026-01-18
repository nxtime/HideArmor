# Changelog

All notable changes to HideArmor will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.8.0-alpha] - 2026-01-18

### Added

- **Multi-Language Support** - Built-in translations for 6 languages:
  - English (en_us), Spanish (es_es), Portuguese (pt_br)
  - French (fr_fr), German (de_de), Russian (ru_ru)
- **Language Selector in UI** - Both `/hidearmor` and `/hidearmoradmin` GUIs now have language buttons
- **Server Default Language** - Admins can set a server-wide default language that persists
- **`/hidearmor language <code>`** - Command to change display language (alias: `lang`)

### Changed

- **Three-Column Layout** - Both user and admin UIs now use three-column layout for better organization:
  - Left: Hide My Own Armor
  - Middle: Hide Other Players' Armor
  - Right: Let Others Hide My Armor
- **Message Prefix** - Language change command now uses `[HideArmor]` prefix for consistency
- **Translation Key Fix** - Fixed `allow_gauntlets` → `allow_hands` typo in admin UI

### Removed

- **Chinese Language Button** - Removed from UI due to Hytale's lack of native CJK font support
  - Chinese translation file (`zh_cn.json`) still included for future use
  - Players with Font Fix mod can still use `/hidearmor language zh_cn`

### Technical

- Added `TranslationManager` for centralized localization
- Added `defaultLanguage` field to `HideArmorState` with persistence in `GlobalConfig`
- Language files stored in `resources/lang/` as JSON
- Translation keys follow `category.subcategory.key` naming convention

---

## [0.7.2-alpha] - 2026-01-17

### Added

- **Integration**: Supported "Miners Helmet" mod items (exempt from hiding to preserve light source).
- **Integration**: Added LuckPerms detection - skips `permissions.json` when LP is installed.
- **Integration**: Added Better Modlist support with mod logo.
- **Config**: Added `refreshDelayMs` setting (10-1000ms) to reduce flickering during inventory changes.

### Changed

- **Admin UI Logic** - Simplified event handling to use explicit Button IDs.

### Fixed

- **Permissions Button** - Fixed ID mismatch.
- **Ancient Worlds** - Fixed issues with lag spike on other worlds.
- **Permission Enforcement** - Commands now require proper permissions (works with LuckPerms).

---

## [0.7.0-alpha] - 2026-01-16

### Added

- **Admin Configuration Menu** (`/hidearmoradmin`)
  - Configure default visibility settings for new players
  - Force override system: globally hide armor pieces regardless of player preferences
  - Hot reload command: `/hidearmoradmin reload` to reload config without restart
  - **Quick Setup button**: Automatically adds HideArmor permissions to `permissions.json`
  
- **Utility Classes** (code quality improvements)
  - `CommandUtils` - Centralized command argument parsing
  - `PluginLogger` - Consistent logging with `[HideArmor] [LEVEL]` format
  - `GuiUtils` - GUI opening boilerplate reduction
  - `PermissionUtils` - Automatic permission.json configuration

### Changed

- **Logging System** - Replaced scattered `System.out/err` calls with `PluginLogger`
- **Command Parsing** - Unified argument parsing across all command classes
- **Equipment Refresh** - Force settings now apply immediately to all online players

### Technical

- Added `forcedMask` to `HideArmorState` for global overrides
- Added `trackWorld()` and `refreshAllPlayersEquipment()` to `HideArmorPlugin`
- Persistence now includes `config.defaultMask` and `config.forcedMask` in JSON
- Updated `HideArmorAdminGuiPage` with force override UI bindings

---

## [0.6.0-alpha] - 2026-01-15

### Added

- **Permission-Based Command Access** - Commands now support server permission configuration
- **AbstractPlayerCommand Integration** - All commands extend `AbstractPlayerCommand` for proper permission handling
- **README Permission Documentation** - Detailed instructions for granting command access to non-OP players

### Changed

- **Command Base Class** - Migrated from `CommandBase` to `AbstractPlayerCommand`:
  - `HideArmorCommand`
  - `HideHelmetCommand`
  - `HideArmorUICommand`
  - `HideHelmetDebugCommand`
- **Execute Method Signature** - Updated to `execute(CommandContext, Store<EntityStore>, Ref<EntityStore>, PlayerRef, World)`

### Technical

- Permission format: `dev.nxtime.hidearmor.command.<commandname>`
- Permissions can be granted to player groups (e.g., Adventure) via server configuration
- Commands remain usable by OPs with `*` permission

---

## [0.5.0-alpha] - 2026-01-15

### Added

- **Hytale Settings-Style UI** - Complete visual overhaul matching native Hytale settings panels
- **Two-Column Layout** - "Hide My Armor" and "Hide Others" on left, "Allow Others" on right
- **ColorConfig Utility** - Centralized cozy pastel color palette for all chat messages
- **Reusable UI Component Framework** - Modular `@SettingsRow` and `@SectionHeader` macros

### Changed

- **Chat Message Formatting** - All commands now use consistent `Message.join()` with hex colors:
  - Prefix: `#EAC568` (Muted Gold)
  - Primary Text: `#E0E0E0` (Soft White)
  - Success: `#88CC88` (Sage Green)
  - Error: `#CC6666` (Muted Red)
- **GUI Container** - Wider layout (720x520) without scrollbar
- **Row Styling** - Dark background (`#1a2533`) with right-aligned checkboxes
- **Section Headers** - Uppercase labels with `#96a9be` text color
- Removed "Hide All" toggle options from UI (individual controls only)

### Fixed

- Broken texture references when using custom container approach
- GUI overflow issues with proper container height management

### Technical

- `ColorConfig.java` - New utility class with centralized color constants
- Updated `HideArmorCommand`, `HideHelmetCommand`, `HideArmorTestCommand`, `HideHelmetDebugCommand` to use ColorConfig
- Simplified `HideArmorGuiPage.java` - Removed "All" toggle bindings and handlers
- UI uses `$C.@Container` macro for proper texture path resolution

---

## [0.4.0-alpha] - 2026-01-14

### Added

- **Hide Other Players' Armor** - New system to hide armor on other players
- **Mutual Opt-In Logic** - Both viewer and target must agree for armor to be hidden
- **Allow Others Permissions** - Per-slot permission system for armor visibility
- **Extended GUI** - Added two new sections with 10 new checkboxes total
- **Section Labels** - Golden colored headers for each GUI section
- **New Commands**:
  - `/hidearmor hideothers <piece|all>` - Control hiding on other players
  - `/hidearmor allowothers <piece|all>` - Control permissions for others
- **Enhanced Status Command** - Now shows all three setting categories
- **12-bit State Storage** - Expanded from 4 bits to support new features
- **UUID Resolution System** - Entity tracking for multiplayer armor hiding
- **Reflection-based Compatibility** - Works across Hytale SDK updates
- **Test Mode** (disabled by default) - Single-player testing framework

### Changed

- GUI container height increased from 350px to 675px
- GUI title updated to "Armor Visibility Settings"
- Status command output now categorized into three sections
- Packet receiver now processes all player entities, not just self
- Help text updated with new command options
- MAX_MASK constant increased from 15 to 4095

### Fixed

- **Critical:** Mask clamping bug that prevented settings from persisting (was clamping to 15 instead of 4095)
- **Critical:** `setAll` method was overwriting all bits instead of just self-armor bits
- All mask comparison operations now properly isolate bit ranges (0xF, 0xF0, 0xF00)
- UI file now uses proper `Label` syntax instead of non-existent `$C.@Label`
- GUI checkboxes now correctly check bit ranges for "All" toggles

### Technical

- Added `HideArmorTestCommand` for development testing
- Packet receiver now uses `Object` type for world to avoid dependency issues
- Entity UUID resolution uses reflection for forward compatibility
- Cache system added for networkId to UUID mappings
- Mutual opt-in check runs for every entity in entity updates

### Backward Compatibility

- Existing save files automatically upgrade
- Self-armor functionality unchanged
- No breaking changes to existing commands
- All previous settings preserved on upgrade

---

## [0.3.0-alpha] - 2025-XX-XX

### Added

- Interactive GUI using Hytale's native UI system
- Checkbox-based controls with real-time updates
- `/hidearmorui` command as alternative GUI access
- `HideArmorGuiPage` class for UI page management
- `dev.nxtime_HideArmor_Menu.ui` asset file
- Visual checkboxes for all armor slots
- "Hide All Armor" toggle in GUI

### Changed

- Primary interface switched from commands to GUI
- `/hidearmor` now opens GUI instead of showing help
- GUI-based state updates with instant visual feedback

### Technical

- Integrated with `InteractiveCustomUIPage` system
- Event binding system for checkbox interactions
- UI command builder for dynamic state loading

---

## [0.1.0-alpha] - 2025-XX-XX

### Added

- Initial release
- Self-armor hiding functionality
- Command-line interface
- `/hidehelmet` command for quick helmet toggle
- `/hidearmor` command with full slot control
- Persistence system with `players.json`
- Bitmask state storage (4 bits)
- Packet interception for visual hiding
- Debounced save system (1.5 second delay)
- Equipment refresh on inventory changes

### Technical

- `HideArmorState` class for state management
- `HideArmorPacketReceiver` for packet filtering
- `HideArmorPlugin` main class
- Entity tracker integration
- `ConcurrentHashMap` for thread-safe storage

---

## Version Number Scheme

Format: `MAJOR.MINOR.PATCH-alpha`

- **MAJOR:** Breaking changes or complete rewrites
- **MINOR:** New features, backward compatible
- **PATCH:** Bug fixes and small improvements
- **-alpha:** Pre-release status for Hytale Early Access

---

## Upgrade Path

### 0.4.0 → 0.5.0

- Automatic compatibility
- UI redesign replaces old layout
- "Hide All" toggles removed (use individual controls)
- Chat messages have new color scheme

### 0.3.0 → 0.4.0

- Automatic save file upgrade
- No manual intervention required
- New settings default to disabled (0)
- Existing settings fully preserved

### 0.1.0 → 0.3.0

- Automatic save file compatibility
- Command structure unchanged
- GUI added as primary interface

---

## Future Roadmap

### Potential v0.5.0 Features

- Per-player hide lists (whitelist/blacklist)
- Quick preset configurations
- Admin override commands
- Integration API for other plugins

### Potential v1.0.0 Goals

- Full release when Hytale SDK stabilizes
- Performance optimizations
- Complete test coverage
- Comprehensive documentation

---

[0.5.0-alpha]: https://github.com/nxtime/HideArmor/releases/tag/v0.5.0-alpha
[0.4.0-alpha]: https://github.com/nxtime/HideArmor/releases/tag/v0.4.0-alpha
[0.3.0-alpha]: https://github.com/nxtime/HideArmor/releases/tag/v0.3.0-alpha
[0.1.0-alpha]: https://github.com/nxtime/HideArmor/releases/tag/v0.1.0-alpha
