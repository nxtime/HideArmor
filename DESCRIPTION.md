# HideArmor - Advanced Armor Visibility Control

A Hytale plugin that allows players to toggle the visibility of their armor pieces while maintaining full protection benefits. Now featuring a privacy-focused mutual opt-in system for multiplayer interactions and comprehensive admin controls.

## ⚠️ Attention: Version 0.7.0-alpha adds admin features - if faced with any issues please rollback to 0.6.0-alpha

### *Features*

**Hide Individual Armor Pieces** - Toggle visibility for helmet, chestplate, gauntlets, and leggings independently  
**Multiplayer Support** - Hide other players' armor with a **Mutual Opt-In** system (requires permission from both parties)  
**Privacy Control** - Granular "Allow Others" permissions ensure you maintain control over your appearance  
**Persistent State** - All preferences (Self, Hide Others, Allow Others) are automatically saved and restored  
**Native GUI** - Modern, interactive Hytale UI with three distinct configuration sections  
**Performance Optimized** - Lightweight packet interception with intelligent refresh mechanisms  

### *New in 0.7.0 - Admin Features*

**Admin Configuration Menu** - `/hidearmoradmin` for server-wide settings  
**Default Settings** - Set default visibility for new players joining the server  
**Force Overrides** - Globally force-hide specific armor pieces for all players  
**Quick Setup Button** - Automatically configure permissions.json with one click  
**Hot Reload** - `/hidearmoradmin reload` to reload config without server restart  

### *Commands*

#### Player Commands

**/hidearmor** - Open the interactive armor visibility menu  
**/hidearmor {piece}** - Toggle specific self-armor pieces  
**/hidearmor hideothers {piece}** - Toggle visibility of other players' armor  
**/hidearmor allowothers {piece}** - Toggle permissions for others to hide your armor  
**/hidearmor status** - View full configuration status  
**/hidehelmet** - Quick toggle for helmet visibility  

#### Admin Commands

**/hidearmoradmin** - Open the admin configuration menu  
**/hidearmoradmin reload** - Reload configuration from disk  

### *Permission Groups*

#### Quick Setup (Recommended)

Use the **"Setup Permissions"** button in the admin menu (`/hidearmoradmin`) to automatically configure permissions!

#### Manual Setup

Modify your *permissions.json* file, follow the example below:

```json
{
  "users": { ... },
  "groups": {
    "Default": [],
    "OP": ["*"],
    "Adventure": [
      "dev.nxtime.hidearmor.command.hidearmor",
      "dev.nxtime.hidearmor.command.hidehelmet",
      "dev.nxtime.hidearmor.command.hidearmorui"
    ]
  }
}
```

### *Technical Details*

- Uses advanced packet interception for `EntityUpdates` modification  
- 12-bit state storage for comprehensive configuration management  
- Thread-safe state management with debounced disk persistence  
- Centralized logging via `PluginLogger` utility  
- Modular command argument parsing via `CommandUtils`  

### *Roadmap*

- ~~Admin override commands for moderation purpose~~ ✅ Added in v0.7.0
- Per-world configuration options
- Integration with permission plugins

*Built for Hytale Early Access SDK*
