# BetterPrisons — Features & How to Use Them

This guide explains what the BetterPrisons client mod does and how you can customize it to suit your playstyle. It's written for players and server users — no programming knowledge required.

Quick overview
- BetterPrisons is a client-side enhancement pack that adds several on-screen HUDs, helpful inventory overlays, peaceful-mining visuals and targeting, automatic cooldown tracking for commands and abilities, message notifications, and quality-of-life enhancements.
- Everything visible in the HUDs is highly customizable: you can enable or disable features, change colors, adjust opacity and scale, and move elements around on the screen.

How this guide is organized
- HUDs (on-screen overlays)
- EasyView (inventory/hotbar overlays)
- Peaceful Mining (visual & targeting aids)
- Quality-of-Life Features (message notifications, held item scaling, pickaxe drop confirmation)
- Customization & settings (what you can change and where to find them)
- Keyboard shortcuts
- Quick tweaks and examples

---

HUDs (on-screen overlays)
Each HUD is an optional overlay you can turn on or off independently. You can drag HUD elements to reposition them and save their positions in the config screen.

Common HUD features
- Move any HUD by using the HUD editor (available from the mod’s settings screen).
- Scale each HUD independently to fit different resolutions or preferences.
- Tweak background color, border color, border thickness, and opacity for readability.
- Everything can be toggled on and off independently.

- Cooldown HUD
  - Shows active cooldowns for commands and abilities (with timers).
  - Entries can include a small icon and a colored timer so you can glance at remaining time quickly.
  - Useful for keeping track of teleport cooldowns, kit cooldowns, or any server ability you use frequently.
  - Supports unique colored text for each of the different cooldowns
  - Currently supports /Jet, /Feed, /Fix, /Home, /tpa, /tpahere, and a combat timer.
    - Super Breaker and Powerball are supported by the Enchant HUD.

- Satchel HUD
  - Detects satchels in your inventory and displays their current fill and capacity.
  - Works with renamed satchels — detects the satchel type by its Minecraft item when the display name has been changed.
  - Optionally combines multiple satchels of the same type into a single entry with summed totals (toggleable).
  - Shows capacity as a percentage or as raw numbers depending on your preference.
  - Threshold colors highlight nearly-empty or nearly-full satchels so you can spot them easily.
  - Supports unique text colors for each of the different satchel fill levels.

- Stats HUD
  - Shows gameplay stats such as your current XP, XP per hour, Cosmic Energy (or similar resource), and session totals.
  - Displays estimated time until your next level based on your current XP/min rate.
  - Shows a visual indicator when tracking is paused so you always know its current state.
  - You can choose which stats to display and whether numbers are shown compact (e.g., 1.2M) or with commas (1,234,567).
  - Supports unique text colors for each of the different stats displayed.

- Enchant HUD
  - Lists active enchants or temporary effects and shows remaining time for each.
  - Useful to track timed buffs so you know exactly when they will expire.
  - Current Supports Super Breaker, and Powerball.

- Events HUD
  - Detects meteor-related announcements and displays coordinates with a clear icon and text for quick navigation.
  - Each meteor entry shows a heading indicating whether it is naturally spawned or player spawned.
  - Tracks Ore Merchant spawn and death announcements, showing each active merchant's tier, coordinates, and distance.
  - Per-tier toggles let you hide or show individual merchant tiers (Coal, Iron, Lapis, Redstone, Gold, Diamond, Emerald).
  - Supports custom icons and text colors, with distinct heading colors per merchant tier.

- Waypoints
  - Screen-edge direction indicators for every active meteor and merchant: a colored diamond when the target is on screen, or an arrow clamped to the screen edge when off-screen, always pointing the right way.
  - Optional 3D beacon beam pillars drawn in world space at each event location, visible from any distance.
  - Waypoints render correctly regardless of render distance — when the target is beyond the far clip plane the beam floats in the correct horizontal direction at a safe render distance.
  - Opacity, per-type toggles (meteors / merchants), and beacon beam on/off are all configurable.

- Gang Pings
  - Press a keybind (default: G) to broadcast your position, HP, and facing direction to gang chat.
  - Press a keybind (default: H) to broadcast to truce chat instead.
  - Received pings appear as player head icons rendered at the sender's location with optional beacon beams.
  - Player heads use the sender's skin texture and fade with distance (minimum 30% opacity floor).
  - Configurable info text lines below each icon: name, countdown timer, coordinates with distance, HP, and facing direction. Each line is individually toggleable.
  - Per-line text backgrounds for readability, with adjustable text scale (0.5x–2.0x).
  - Icon scaling is configurable: minimum scale, maximum scale, and a toggle to disable distance-based scaling entirely.
  - Sound notification plays when a ping is received in the same world (configurable volume).
  - 3-second client-side cooldown prevents server antispam, with a chat message showing remaining time if triggered too fast.
  - Truce pings share all display settings with gang pings but have their own enable toggle and color.
  - Edge indicators (off by default) show directional arrows when a ping is off-screen.
  - All gang ping settings are in a dedicated config tab with collapsible sections for Icon Settings, Beacon Beams, Sound, and Text Display.

- Super Breaker Aura
  - A centered visual timer that displays as a ring similar to WeakAura from WoW.
  - Colors, opacity, and size are all adjustable.
  - Countdown timer text can be toggled on or off independently.
  - Timer position is adjustable with X and Y offsets so it doesn't overlap the crosshair.

---

EasyView — inventory & hotbar overlays
EasyView adds compact text overlays to items in your inventory and hotbar so you can see important values without opening tooltips.
- Supported overlays include:
  - Cosmic Energy (shows compact amounts with K/M suffixes)
  - Money Notes (shows currency amounts)
  - Gang Points (shows point amounts)
  - Black Scrolls (shows percentage values)
  - Charge Orbs (shows percentage values)
  - Dust (shows dust amounts)
  - Pages (shows page amounts, with optional tier-based coloring)
  - Armor (shows item level from armor names)
  - Weapons (shows item level from sword/axe names)
  - Pickaxes (shows item level from pickaxe names)
- Each overlay type can be toggled on or off independently.
- Colors are configurable for each item type so you can make each overlay stand out.
- Overlays display in your inventory for quick reference without hovering over items.

---

Peaceful Mining — visuals, targeting, and interactions
This feature is aimed at players who mine in crowded areas and want to avoid accidentally targeting or hitting other players.
- When enabled and you are holding a pickaxe, other players near you become translucent so they don't block your view.
- An opacity slider lets you control how "ghost-like" other players appear (0-255).
- A radius setting lets you define how close other players need to be before the effect applies (default: 8 blocks).
- Unlike other mods, peaceful mining in this mod will render block breaking progress even when viewed through other players, so you can still see what you're mining.
- All interaction with other players is disabled with peaceful mining active, preventing accidental right-clicks or hits on other players while you are focused on mining.
- Auto-disable on combat (toggleable): Automatically disables peaceful mining when you enter combat, then re-enables it when combat ends. This ensures you can defend yourself without changing settings manually.

---

Quality-of-Life Features

Message Notifications
- Get audio alerts when you receive private messages so you never miss important communications.
- Choose from 7 different notification sounds: Anvil (default), Bell, XP Orb, Note Block Pling, Enchantment Table, Level Up, or Ender Eye.
- Adjust notification volume from 0-200% (default: 100%).
- Automatically detects private messages in chat and plays your selected sound.
- Can be toggled on or off independently.

Held Item Scaling
- Customize the visual size of items in your hand for better visibility or a cleaner view.
- Independent scaling for:
  - Pickaxes (25-150%, default: 100%)
  - Swords (25-150%, default: 100%)
  - Axes (25-150%, default: 100%)
  - Other items (25-150%, default: 100%)
- Perfect for players who want larger tools for easier viewing or smaller tools for less screen clutter.

Pickaxe Drop Protection
- Prevents accidentally dropping your valuable pickaxe with a double-press confirmation system.
- When enabled, you must press the drop key twice within 3 seconds to drop a pickaxe.
- Shows a confirmation message in chat on the first press: "Press drop again to confirm dropping your pickaxe."
- Option to block pickaxe dropping entirely (off by default) — the drop key is completely ignored for pickaxes.
- Option to block dragging pickaxes out of your inventory (off by default) — prevents dropping via drag-out or Q/Ctrl+Q throw on pickaxe slots.
- Only applies to pickaxe items, all other items drop normally.
- Can be toggled on or off.

---

Customization & settings (where to change things)
- All user-facing settings are available in the in-game configuration screen, accessible via the config keybind (default: I) or Mod Menu.
- The configuration screen is a fully custom UI — no external dependency required.

Layout
- The screen is organized into tabs in a left sidebar, grouped into three sections: HUD Settings, Feature Settings, and Configuration.
- Each tab's content scrolls independently.
- Settings are grouped into collapsible sections so you can collapse categories you're not using to reduce clutter.
- Every widget has a dedicated reset button (X icon) to restore that individual setting to its default without affecting anything else.
- Most settings show a tooltip on hover with a brief description.
- The sidebar tab list scrolls when screen height is too small to show all 13 tabs.
- Text inputs support cursor movement, selection, and scrolling within the field.
- Dropdowns and popups correctly block interaction with elements behind them so there are no accidental clicks through open overlays.

Color picker
- Color fields open a dedicated color picker popup with a full HSV (hue/saturation/value) selector square, a separate hue bar, a live preview strip, and a hex input field.

HUD editor
- The drag-and-drop HUD editor is opened via the button at the bottom of the config screen.
- Right-click any HUD to bring up an inline scale slider (70–150%) with a live preview — previously scale was only adjustable through the config screen itself.
- HUDs are color-coded while editing: green = hovered, yellow = dragging, orange = selected for scaling.
- A Reset Positions button restores all HUDs to their default locations and scales in one click.

Theme customization
- A dedicated Configuration tab exposes 27 theme color settings covering backgrounds, borders, text, accents, scrollbars, sidebar, tooltips, and more.
- Theme changes update the config screen in real time as you edit.

Settings you can change include (but are not limited to):
  - Enable/disable each HUD
  - HUD positions (drag and drop editor)
  - HUD scale per element
  - Background and border colors and opacities
  - HUD title visibility and title text color
  - Whether satchels are combined into single entries
  - Whether satchel values are shown as percent or absolute numbers
  - EasyView toggles per item type and overlay colors
  - Peaceful Mining enable/disable, opacity, effective radius, and auto-disable on combat
  - Cooldown colors and which commands to track
  - Super Breaker aura colors, opacity, and scale
  - Message notification sound selection and volume
  - Held item scaling for pickaxes, swords, axes, and other items
  - Pickaxe drop confirmation, block drop, and block drag-out toggles
  - 27 UI theme color settings
  - And many more customization options!

Config files
- Settings are saved to a configuration file (config/betterprisons/config.json) so your preferences persist between sessions.
- I do not recommend editing the config file directly. The in-game GUI is recommended for ease and safety.

---

Keyboard Shortcuts
- I — Open the BetterPrisons configuration screen
- R — Reset Stats HUD tracking (clears session totals and timers)
- B — Pause/Resume Stats HUD tracking (useful for taking breaks without resetting)
- G — Send a gang ping (broadcasts your position to gang chat)
- H — Send a truce ping (broadcasts your position to truce chat)

---

Special thanks to NotRtzy, viseronic, and other pre-testers for their feedback and suggestions during development!
Contact nishu06 on discord for support or questions about the mod (Ideally by making a ticket in the mod discord (https://discord.gg/gW4sBdNmac).
Also check out the modrinth download at: https://modrinth.com/mod/betterprisons