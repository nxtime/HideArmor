# HideArmor - Advanced Armor Visibility Control

A Hytale plugin that allows players to toggle the visibility of their armor pieces while maintaining full protection benefits. Now featuring a privacy-focused mutual opt-in system for multiplayer interactions.

### *Features*

**Hide Individual Armor Pieces** - Toggle visibility for helmet, chestplate, gauntlets, and leggings independently  
**Multiplayer Support** - Hide other players' armor with a **Mutual Opt-In** system (requires permission from both parties)  
**Privacy Control** - Granular "Allow Others" permissions ensure you maintain control over your appearance  
**Persistent State** - All preferences (Self, Hide Others, Allow Others) are automatically saved and restored  
**Native GUI** - Modern, interactive Hytale UI with three distinct configuration sections  
**Performance Optimized** - Lightweight packet interception with intelligent refresh mechanisms  

### *Commands*

**/hidearmor** - Open the interactive armor visibility menu  
**/hidearmor {piece}** - Toggle specific self-armor pieces  
**/hidearmor hideothers {piece}** - Toggle visibility of other players' armor  
**/hidearmor allowothers {piece}** - Toggle permissions for others to hide your armor  
**/hidearmor status** - View full configuration status  
**/hidehelmet** - Quick toggle for helmet visibility  

### *Technical Details*

Uses advanced packet interception for `EntityUpdates` modification  
12-bit state storage for comprehensive configuration management  
Thread-safe state management with debounced disk persistence  
Built for Hytale Early Access SDK  
