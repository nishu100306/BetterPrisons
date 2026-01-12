# BetterPrisons — Features & How to Use Them

This guide explains what the BetterPrisons client mod does and how you can customize it to suit your playstyle. It's written for players and server users — no programming knowledge required.

Quick overview
- BetterPrisons is a client-side enhancement pack that adds several on-screen HUDs, helpful inventory overlays, peaceful-mining visuals and targeting, and automatic cooldown tracking for commands and abilities.
- Everything visible in the HUDs is highly customizable: you can enable or disable features, change colors, adjust opacity and scale, and move elements around on the screen.

How this guide is organized
- HUDs (on-screen overlays)
- EasyView (inventory/hotbar overlays)
- Peaceful Mining (visual & targeting aids)
- Cooldown tracking (commands & chat-triggered abilities)
- Customization & settings (what you can change and where to find them)
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
- Supported overlays include energy, money notes, gang points, black scrolls, and charge orbs.
- Each overlay type can be toggled on or off independently.
- Colors are configurable for each item type so you can make each overlay stand out.
- Overlays show both on the hotbar (in-game) and inside inventory/container screens.

---

Peaceful Mining — visuals, targeting, and interactions
This feature is aimed at players who mine in crowded areas and want to avoid accidentally targeting or hitting other players.
- When enabled and you are holding a pickaxe, other players near you become translucent so they don't block your view.
- An opacity slider lets you control how "ghost-like" other players appear.
- A radius setting lets you define how close other players need to be before the effect applies.
- Unlike other mods, peaceful mining in this mod will render block breaking progress even when viewed through other players, so you can still see what you're mining.
- all interaction with other players is disabled with peaceful mining active, preventing accidental right-clicks or hits on other players while you are focused on mining.

---

Customization & settings (where to change things)
- All user-facing settings are available in the in-game configuration screen accessible from they config keybind or Mod Menu.
- Settings you can change include (but are not limited to):
  - Enable/disable each HUD
  - HUD positions (drag and drop editor)
  - HUD scale per element
  - Background and border colors and opacities
  - HUD Title visibility and title text color
  - Whether satchels are combined into single entries
  - Whether satchel values are shown as percent or absolute numbers
  - EasyView toggles per item type and overlay colors
  - Peaceful Mining enable/disable, opacity, and effective radius
  - Cooldown colors and which commands to track
  - Super Breaker aura colors, opacity, and scale
  - And many, many more that I have forgotten to list here!

Config files
- Settings are saved to a configuration file so your preferences persist between sessions. I do not recommend editing the config file directly. The in-game GUI is recommended for ease and safety.

---

Special thanks to NotRtzy, viseronic, and other pre-testers for their feedback and suggestions during development!
Contact nishu06 on discord for support or questions about the mod (Ideally by making a ticket in the mod discord [link to be inserted here).
