# HideArmor v0.4.0-alpha Release Notes

## Mutual Opt-In System for Multiplayer Armor Visibility

This release introduces a major new feature: the ability to hide other players' armor with a privacy-first mutual opt-in system.

---

## What's New

### Hide Other Players' Armor
Players can now choose to hide armor pieces on other players, not just their own. This feature requires explicit permission from both parties.

### Mutual Opt-In System
For armor to be hidden, **both conditions must be met**:
1. The viewer must enable "Hide Others' [piece]"
2. The target player must enable "Allow Others [piece]"

This ensures players maintain full control over their appearance and privacy.

### Expanded GUI
The interface now features three distinct sections:
- **Hide My Own Armor** - Control your own armor visibility (existing)
- **Hide Other Players' Armor** - Choose which pieces to hide on others (new)
- **Let Others Hide My Armor** - Grant permissions per armor slot (new)

Each section is clearly labeled with golden headers and visual separators.

### New Commands
```bash
/hidearmor hideothers <piece>       # Toggle hiding on others
/hidearmor hideothers all           # Toggle all pieces on others
/hidearmor allowothers <piece>      # Toggle permission for others
/hidearmor allowothers all          # Toggle all permissions
```

The status command now displays all three categories of settings.

---

## Technical Improvements

### Extended Persistence System
- Upgraded from 4-bit to 12-bit state storage
- Bits 0-3: Self armor (existing)
- Bits 4-7: Hide others' armor (new)
- Bits 8-11: Allow others permissions (new)
- Backward compatible with existing save files

### Enhanced Packet Filtering
- Now processes both self and other players' entities
- Implements UUID resolution with caching for performance
- Applies mutual opt-in logic server-side
- Uses reflection for compatibility with Hytale SDK

### Bug Fixes
- Fixed mask clamping that prevented new settings from saving
- Fixed `setAll` method that was overwriting hide-others/allow-others settings
- Fixed all mask comparison operations to properly isolate bit ranges
- Corrected UI file to use proper Label syntax

---

## Use Cases

### Roleplay Servers
Players can coordinate armor visibility for immersive storytelling without affecting actual equipment.

### Screenshot Sessions
Hide specific armor pieces on others (with their permission) for cleaner screenshots and cinematics.

### PvP Practice
Reduce visual clutter during training sessions while maintaining full combat mechanics.

### Content Creation
Streamers and video creators can customize what armor appears on screen while recording.

---

## Migration from v0.3.0

### Automatic Migration
Existing settings are automatically preserved. The new bits default to 0 (disabled), so existing functionality remains unchanged.

### What You Need to Know
- All previous settings continue to work exactly as before
- No manual migration required
- Save files are automatically upgraded on first save
- New features are opt-in and do not affect existing behavior

---

## Known Limitations

### Requires Two Players
The mutual opt-in system requires both players to be online and have configured their settings. Single-player testing requires the test mode (currently disabled in production).

### Visual Only
This plugin only affects client-side rendering. Armor stats, durability, and combat calculations are completely unaffected.

### Entity Resolution
The system uses reflection-based UUID resolution for compatibility. In rare cases with very high player counts, there may be minimal performance impact during initial caching.

---

## Testing Recommendations

### Basic Functionality
1. Open `/hidearmor` GUI
2. Verify all three sections display correctly
3. Toggle settings in each section
4. Close and reopen GUI to verify persistence

### Mutual Opt-In Testing
**With two players:**
1. Player A enables "Hide Others' Helmet"
2. Verify Player B's helmet is still visible
3. Player B enables "Allow Helmet"
4. Verify Player B's helmet is now hidden for Player A
5. Player B disables "Allow Helmet"
6. Verify Player B's helmet becomes visible again

### Persistence Testing
1. Configure various settings
2. Use `/hidearmor status` to verify
3. Restart server
4. Verify settings restored correctly

---

## Upgrade Instructions

1. Stop your server
2. Backup your `plugins/HideArmor/players.json` file (optional but recommended)
3. Replace the old `.jar` with the new v0.4.0 `.jar`
4. Start your server
5. Verify with `/hidearmor` that the new GUI sections appear

---

## Support

**Issues:** Report bugs on the GitHub issues page
**Questions:** See the updated README.md for detailed documentation
**Compatibility:** Tested with Hytale Early Access SDK

---

## Credits

Thanks to the Hytale modding community for feedback and testing.

Special thanks to PabloBora for the original HideHelmet inspiration.

---

## Coming Soon

Future versions may include:
- Per-player permission lists (hide specific players regardless of their settings)
- Quick presets for common configurations
- Integration with other cosmetic plugins
- Admin override commands for moderation purposes

---

Released: January 14, 2026
