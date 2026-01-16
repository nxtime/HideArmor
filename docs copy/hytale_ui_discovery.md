# Hytale UI System - Discovery Notes

This document summarizes the UI patterns, assets, and styles discovered from analyzing Hytale's native UI files.

---

## Asset Locations

| Category | Path |
|----------|------|
| **Common UI Assets** | `Client/Data/Game/Interface/Common/` |
| **Button Textures** | `Client/Data/Game/Interface/Common/Buttons/` |
| **Settings UI** | `Client/Data/Game/Interface/Common/Settings/` |
| **Main Common.ui** | [Common.ui](file:///C:/Users/marcos/AppData/Roaming/Hytale/install/release/package/game/build-1/Client/Data/Game/Interface/Common.ui) |
| **Container Templates** | [Container.ui](file:///C:/Users/marcos/AppData/Roaming/Hytale/install/release/package/game/build-1/Client/Data/Game/Interface/Common/Container.ui) |
| **Settings Templates** | [Settings.ui](file:///C:/Users/marcos/AppData/Roaming/Hytale/install/release/package/game/build-1/Client/Data/Game/Interface/Common/Settings/Settings.ui) |

---

## Scrollbar Styles

Found in [Common.ui](file:///C:/Users/marcos/AppData/Roaming/Hytale/install/release/package/game/build-1/Client/Data/Game/Interface/Common.ui) (lines 30-54):

```ui
@DefaultScrollbarStyle = ScrollbarStyle(
  Spacing: 6,
  Size: 6,
  Background: (TexturePath: "Common/Scrollbar.png", Border: 3),
  Handle: (TexturePath: "Common/ScrollbarHandle.png", Border: 3),
  HoveredHandle: (TexturePath: "Common/ScrollbarHandleHovered.png", Border: 3),
  DraggedHandle: (TexturePath: "Common/ScrollbarHandleDragged.png", Border: 3)
);

@TranslucentScrollbarStyle = ScrollbarStyle(
  Spacing: 6,
  Size: 6,
  OnlyVisibleWhenHovered: true,
  Handle: (TexturePath: "Common/ScrollbarHandle.png", Border: 3)
);
```

**Usage:** Apply to a Group with `ScrollbarStyle: $Common.@DefaultScrollbarStyle;`

---

## Button Assets

Located in `Common/Buttons/`:

| Button Type | Normal | Hovered | Pressed |
|-------------|--------|---------|---------|
| **Primary** | `Primary@2x.png` | `Primary_Hovered@2x.png` | `Primary_Pressed@2x.png` |
| **Secondary** | `Secondary@2x.png` | `Secondary_Hovered@2x.png` | `Secondary_Pressed@2x.png` |
| **Tertiary** | `Tertiary@2x.png` | `Tertiary_Hovered@2x.png` | `Tertiary_Pressed@2x.png` |
| **Destructive** | `Destructive@2x.png` | `Destructive_Hovered@2x.png` | `Destructive_Pressed@2x.png` |
| **Disabled** | `Disabled@2x.png` | - | - |

**Button Style Pattern:**

```ui
@PrimaryButtonStyle = ButtonStyle(
  Default: (Background: PatchStyle(TexturePath: "Common/Buttons/Primary.png", Border: 12)),
  Hovered: (Background: PatchStyle(TexturePath: "Common/Buttons/Primary_Hovered.png", Border: 12)),
  Pressed: (Background: PatchStyle(TexturePath: "Common/Buttons/Primary_Pressed.png", Border: 12)),
  Disabled: (Background: PatchStyle(TexturePath: "Common/Buttons/Disabled.png", Border: 12)),
  Sounds: @ButtonSounds
);
```

---

## Container Components

From [Container.ui](file:///C:/Users/marcos/AppData/Roaming/Hytale/install/release/package/game/build-1/Client/Data/Game/Interface/Common/Container.ui):

| Asset | Description |
|-------|-------------|
| `ContainerPatch@2x.png` | Main container background (NineSlice, Border: 23) |
| `ContainerHeader@2x.png` | Decorated header with runes |
| `ContainerHeaderNoRunes@2x.png` | Clean header |
| `ContainerCloseButton*.png` | Close button states |
| `ContainerDecorationTop/Bottom@2x.png` | Fancy decorations |
| `ContainerVerticalSeparator@2x.png` | Column divider |

**Container Macro:**

```ui
@Container = Group {
  Group #Title {
    Background: (TexturePath: "ContainerHeaderNoRunes.png", HorizontalBorder: 35);
  }
  Group #Content {
    LayoutMode: Top;
    Background: (TexturePath: "ContainerPatch.png", Border: 23);
  }
};
```

---

## Input Controls

### Checkbox

```ui
@DefaultCheckBoxStyle = CheckBoxStyle(
  Unchecked: (DefaultBackground: (Color: #00000000)),
  Checked: (DefaultBackground: (TexturePath: "Common/Checkmark.png"))
);
```

### Slider

```ui
@DefaultSliderStyle = SliderStyle(
  Background: (TexturePath: "Common/SliderBackground.png", Border: 2),
  Handle: "Common/SliderHandle.png",
  HandleWidth: 16,
  HandleHeight: 16
);
```

### Input Box

```ui
@InputBoxBackground = PatchStyle(TexturePath: "Common/InputBox.png", Border: 16);
@InputBoxHoveredBackground = PatchStyle(TexturePath: "Common/InputBoxHovered.png", Border: 16);
@InputBoxSelectedBackground = PatchStyle(TexturePath: "Common/InputBoxSelected.png", Border: 16);
```

---

## Tab Navigation

From Container.ui (lines 43-93):

```ui
@TopTabStyle = TabStyleState(
  Background: "Tab.png",
  Overlay: "TabOverlay.png",
  IconAnchor: (Width: 44, Height: 44),
  LabelStyle: (FontSize: 19)
);

@TopTabsStyle = TabNavigationStyle(
  TabStyle: (
    Default: @TopTabStyle,
    Hovered: (...@TopTabStyle, Anchor: (..., Bottom: -5)),
    Pressed: (...@TopTabStyle, Anchor: (..., Bottom: -8))
  ),
  SelectedTabStyle: (
    Default: (...@TopTabStyle, Overlay: "TabSelectedOverlay.png")
  )
);
```

---

## Dropdown Boxes

```ui
@DefaultDropdownBoxStyle = DropdownBoxStyle(
  DefaultBackground: (TexturePath: "Common/Dropdown.png", Border: 16),
  HoveredBackground: (TexturePath: "Common/DropdownHovered.png", Border: 16),
  DefaultArrowTexturePath: "Common/DefaultDropdownCaret.png",
  PanelBackground: (TexturePath: "Common/DropdownBox.png", Border: 16),
  PanelScrollbarStyle: @DefaultScrollbarStyle,
  EntryHeight: 31
);
```

---

## Color Constants (from Common.ui)

```ui
@DisabledColor = #797b7c;
@PrimaryButtonLabelStyle.TextColor = #bfcdd5;
@SecondaryButtonLabelStyle.TextColor = #bdcbd3;
```

---

## Key UI Properties

| Property | Values | Description |
|----------|--------|-------------|
| `LayoutMode` | `Top`, `Left`, `Right`, `Center`, `Stack`, `Middle` | Layout direction |
| `FlexWeight` | Number | Flex sizing in layout |
| `Background` | PatchStyle or Color | Element background |
| `Border` | Number | NineSlice border size |
| `Anchor` | `(Width, Height, Top, Left, Right, Bottom)` | Positioning |
| `Padding` | `(Full, Horizontal, Vertical, Top, Left, Right, Bottom)` | Inner spacing |
| `ScrollbarStyle` | ScrollbarStyle | Enable scrolling |

---

## Sound References

Located in `Sounds.ui`:

- `@ButtonsLight` - Standard button sounds
- `@ButtonsDestructive` - Delete/danger button sounds  
- `@Tick` / `@Untick` - Checkbox sounds
- `@SliderRelease` - Slider sounds
- `@DropdownBox` - Dropdown sounds
