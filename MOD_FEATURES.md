# BetterPrisons — Features & How to Use Them

### Disclaimer: This mod requires Cloth Config API as  dependency. download it here: https://modrinth.com/mod/cloth-config?version=1.21.8
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
  - Currently supports /Jet, /Feed, /Fix, /Home, and a combat timer.
    - Super Breaker and Powerball are supported by the Enchant HUD.

- Satchel HUD
  - Detects satchels in your inventory and displays their current fill and capacity.
  - Optionally combines multiple satchels of the same type into a single entry with summed totals (toggleable).
  - Shows capacity as a percentage or as raw numbers depending on your preference.
  - Threshold colors highlight nearly-empty or nearly-full satchels so you can spot them easily.
  - Supports unique text colors for each of the different satchel fill levels.

- Stats HUD
  - Shows gameplay stats such as your current XP, XP per hour, Cosmic Energy (or similar resource), and session totals.
  - You can choose which stats to display and whether numbers are shown compact (e.g., 1.2M) or with commas (1,234,567).
  - Supports unique text colors for each of the different stats displayed.

- Enchant HUD
  - Lists active enchants or temporary effects and shows remaining time for each.
  - Useful to track timed buffs so you know exactly when they will expire.
  - Current Supports Super Breaker, and Powerball.

- Meteor HUD
  - Detects meteor-related announcements and displays coordinates with a clear icon and text for quick navigation.
  - Supports custom icons and text colors.

- Super Breaker Aura
  - A centered visual timer that displays as a ring similar to WeakAura from WoW.
  - Colors, opacity, and size are all adjustable.

---

EasyView — inventory & hotbar overlays
EasyView adds compact text overlays to items in your inventory and hotbar so you can see important values without opening tooltips.
- Supported overlays include:
  - Cosmic Energy (shows compact amounts with K/M suffixes)
  - Money Notes (shows currency amounts)
  - Gang Points (shows point amounts)
  - Black Scrolls (shows percentage values)
  - Charge Orbs (shows percentage values)
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

Pickaxe Drop Confirmation
- Prevents accidentally dropping your valuable pickaxe with a double-press confirmation system.
- When enabled, you must press the drop key twice within 3 seconds to drop a pickaxe.
- Shows a confirmation message in chat on the first press: "Press drop again to confirm dropping your pickaxe."
- Only applies to pickaxe items, all other items drop normally.
- Can be toggled on or off.

---

Customization & settings (where to change things)
- All user-facing settings are available in the in-game configuration screen accessible from the config keybind (default: I) or Mod Menu.
- Settings you can change include (but are not limited to):
  - Enable/disable each HUD
  - HUD positions (drag and drop editor)
  - HUD scale per element
  - Background and border colors and opacities
  - HUD Title visibility and title text color
  - Whether satchels are combined into single entries
  - Whether satchel values are shown as percent or absolute numbers
  - EasyView toggles per item type and overlay colors
  - Peaceful Mining enable/disable, opacity, effective radius, and auto-disable on combat
  - Cooldown colors and which commands to track
  - Super Breaker aura colors, opacity, and scale
  - Message notification sound selection and volume
  - Held item scaling for pickaxes, swords, axes, and other items
  - Pickaxe drop confirmation toggle
  - And many more customization options!

Config files
- Settings are saved to a configuration file (config/betterprisons/config.json) so your preferences persist between sessions.
- I do not recommend editing the config file directly. The in-game GUI is recommended for ease and safety.

---

Keyboard Shortcuts
- I — Open the BetterPrisons configuration screen
- R — Reset Stats HUD tracking (clears session totals and timers)
- B — Pause/Resume Stats HUD tracking (useful for taking breaks without resetting)

---

Special thanks to NotRtzy, viseronic, and other pre-testers for their feedback and suggestions during development!
Contact nishu06 on discord for support or questions about the mod (Ideally by making a ticket in the mod discord (https://discord.gg/gW4sBdNmac).
