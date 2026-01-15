## HideArmor v0.4.0-alpha

A Hytale plugin that provides advanced armor visibility control with a mutual opt-in system for multiplayer servers. Players can hide their own armor, hide other players' armor (with permission), and control who can hide their armor.

### What's New in v0.4.0

- **Hide Other Players' Armor** - Choose which armor pieces to hide on other players
- **Mutual Opt-In System** - Privacy-first: armor only hides when both players agree
- **Allow Others Permissions** - Per-slot control over what others can hide
- **Expanded GUI** - Three clearly labeled sections with golden headers
- **New Commands** - `hideothers` and `allowothers` subcommands
- **Extended Storage** - 12-bit state system (upgraded from 4-bit)
- **Enhanced Status** - View all settings at once with categorized output

### How It Works

For other players' armor to be hidden, **both conditions must be met**:
1. Viewer has "Hide Others' [piece]" enabled
2. Target player has "Allow Others [piece]" enabled

This ensures players maintain full control over their appearance.

### Features

- **Self Armor Control** - Hide your own armor pieces (existing)
- **Hide Others' Armor** - Choose which pieces to hide on others (new)
- **Permission System** - Grant per-slot visibility permissions (new)
- **Per-Slot Granularity** - Independent control for head, chest, hands, legs
- **Interactive GUI** - Three-section interface with checkboxes
- **Persistent State** - Automatic save/restore with backward compatibility
- **Native Hytale UI** - Built on Hytale's custom UI system

### Commands

| Command | Description |
|---------|-------------|
| `/hidearmor` | Opens the armor visibility GUI |
| `/hidearmor status` | Shows all current settings |
| `/hidearmor <piece>` | Toggle your own armor piece |
| `/hidearmor hideothers <piece\|all>` | Toggle hiding on others |
| `/hidearmor allowothers <piece\|all>` | Toggle permission for others |
| `/hidearmorui` | Alternative command to open GUI |
| `/hidehelmet` | Quick toggle for helmet visibility |

Pieces: `head`, `chest`, `hands`, `legs`

### Bug Fixes

- Fixed critical persistence bug preventing new settings from saving
- Fixed `setAll` method overwriting hide-others/allow-others settings
- Fixed all mask comparisons to properly isolate bit ranges
- Fixed UI label syntax errors

### Technical Details

- Upgraded to 12-bit state storage (bits 0-3: self, 4-7: hide others, 8-11: allow others)
- Packet filtering now processes all player entities with UUID resolution
- Uses reflection for Hytale SDK compatibility
- Thread-safe caching for entity UUID lookups
- Backward compatible with v0.3.0 save files

### Migration

Upgrading from v0.3.0 is seamless:
- Existing settings automatically preserved
- New features default to disabled
- No manual intervention required
- Save files auto-upgrade on first save

### Notes

This is an alpha release. The mutual opt-in system requires two or more players to test effectively. See CHANGELOG.md for complete version history.
