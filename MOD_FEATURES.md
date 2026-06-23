# BetterPrisons — Features & How to Use Them

This guide explains what the BetterPrisons client mod does and how you can customize it to suit your playstyle. It's written for players and server users — no programming knowledge required.

Quick overview
- BetterPrisons is a client-side enhancement pack that adds several on-screen HUDs, helpful inventory overlays, peaceful-mining visuals and targeting, automatic cooldown tracking for commands and abilities, message notifications, and quality-of-life enhancements.
- Everything visible in the HUDs is highly customizable: you can enable or disable features, change colors, adjust opacity and scale, and move elements around on the screen.

How this guide is organized
- HUDs (on-screen overlays)
- EasyView (inventory/hotbar overlays)
- Item Tooltips (extra info added to vanilla tooltips)
- Tools & Search (energy calculator, chest search, clue scroll sorting)
- Peaceful Mining (visual & targeting aids)
- Quality-of-Life Features (message notifications, held item scaling, pickaxe drop confirmation, auto trade, powerball alerts, bold XP/energy popups, PrisonBreak texture pack)
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
  - Currently supports /Jet, /Feed, /Fix, /Home, /tpa, /tpahere, /dangle, /adangle, /near, /pulse, and a combat timer.
    - `/near` is a 45-second cooldown (compass icon); `/pulse` is a 5-minute cooldown (redstone-torch icon).
    - Super Breaker and Powerball are supported by the Enchant HUD.
  - `/adangle` is optimistically applied on use and automatically cancels if the server reports it failed (e.g. "items already dangling").

- Satchel HUD
  - Detects satchels in your inventory and displays their current fill and capacity, read from each satchel's underlying data (so it stays correct even when the satchel is renamed).
  - Covers every ore-satchel variant — regular, Deepslate, and Block-of variants are each named and tracked separately.
  - Also tracks the non-ore "drop" satchels: Shard, Contraband, and Clue Scroll satchels.
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
  - Powerball Ready Alert: when your Powerball cooldown finishes, a customizable title pops up on screen with an optional sound cue so you know the moment it's available again.

- Events HUD
  - Detects meteor-related announcements and displays coordinates with a clear icon and text for quick navigation.
  - Each meteor entry shows a heading indicating whether it is naturally spawned or player spawned.
  - Live countdown to landing — natural meteors show a 7-minute countdown from the spawn message; summoned meteors show a 1-minute countdown. Once the timer elapses the entry shows `(Imminent)`, and once the crash message is received it changes to `[Crashed]`. An entry that stays imminent for over a minute (e.g. you logged off and missed the crash) is cleared automatically.
  - Tracks Meteorite Shower events with their own landing countdown, waypoint, and beacon beam — fully configurable (timeout, colors, icon, beam opacity, sound).
  - Tracks Ore Merchant spawn and death announcements, showing each active merchant's tier, coordinates, and distance.
  - Per-tier toggles let you hide or show individual merchant tiers (Coal, Iron, Lapis, Redstone, Gold, Diamond, Emerald).
  - Tracks Bandit Rush events in the Badlands world — only displays rushes that spawned in your current badlands sub-world (Chain, Gold, Iron, or Diamond region). Fully configurable timeout, color, icon, beam opacity, and sound notification.
  - Supports custom icons and text colors, with distinct heading colors per merchant tier.

- Waypoints
  - Screen-edge direction indicators for every active meteor, merchant, bandit rush, and meteorite shower: a colored diamond when the target is on screen, or an arrow clamped to the screen edge when off-screen, always pointing the right way.
  - Optional 3D beacon beam pillars drawn in world space at each event location, visible from any distance.
  - Waypoints render correctly regardless of render distance — when the target is beyond the far clip plane the beam floats in the correct horizontal direction at a safe render distance.
  - Opacity, per-type toggles, and beacon beam on/off are all configurable.

- Gang Pings
  - Press a keybind (default: G) to broadcast your position, HP, and facing direction to gang chat.
  - Press a keybind (default: H) to broadcast to truce chat instead.
  - A third keybind (unbound by default) sends a gang ping at the block you're currently looking at — handy for pointing out a spot to gangmates without having to walk to it. Raycasts up to 200 blocks.
  - Pings are only displayed if the message comes from the appropriate chat channel (`[GC]` for gang, `[TC]` or `[GC]` for truce). Optional "Show Pings Not From Your Gang/Truce" toggle relaxes this if desired.
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
  - Prestige Tokens (shows token level)
  - XP Bottles (shows XP amount, with optional tier-based coloring)
  - Armor (shows item level from armor names)
  - Weapons (shows item level from sword/axe names)
  - Pickaxes (shows item level from pickaxe names)
- Each overlay type can be toggled on or off independently.
- Colors are configurable for each item type so you can make each overlay stand out.
- Overlays display in your inventory for quick reference without hovering over items.

---

Item Tooltips
BetterPrisons adds extra tooltip lines to certain Cosmic Prisons items. All BP-added lines are prefixed with `[BP]` so they're easy to spot.

- Enchant Book Costs
  - Hovering an enchant book — either in your inventory or one linked in chat — shows the energy cost to upgrade it to each higher level, plus a running total.
  - The formula is tier-aware: COMMON / UNCOMMON / RARE / ULTIMATE use `original × level²`, while LEGENDARY adds an extra ×3 multiplier starting at level 3.
  - Configurable text color and a toggle in the Misc tab.

- Gang Point Expiry
  - Cosmic Prisons displays gang point note expiry in EDT, which is awkward for players outside that timezone.
  - BetterPrisons adds two lines: a live countdown to expiry (e.g. `Expires in: 1d 18h 42m`) and the exact moment in your system's local timezone.
  - Configurable text color and a toggle in the Misc tab.

---

Tools & Search

Energy Calculator
- Calculates the total Cosmic Energy needed to upgrade a pickaxe or satchel between two levels (supports levels up to 750).
- Available two ways: a GUI in the Tools tab (pick the type and levels with dropdowns/sliders) or the `/calc` command for pickaxes, ore satchels, and refined satchels.

Chest Search & Book Search
- Open any chest or container to get a search bar plus a filter-rule sidebar that highlights matching items.
- Filter rules each have their own highlight color and an Any/All (OR/AND) match mode, so you can stack conditions.
- Enchant books can be filtered by success rate, destroy rate, or energy cost — not just name.
- Can be toggled in the Misc tab.

Clue Scroll Sorting
- Shows a clue scroll's current step number large and centered on the item, both in containers and in your hotbar, so a chest full of scrolls can be sorted at a glance.
- Configurable number color. An optional tooltip flags any clue step type the mod doesn't recognize yet so it can be reported.
- Can be toggled in the Misc tab.

---

Peaceful Mining — visuals, targeting, and interactions
This feature is aimed at players who mine in crowded areas and want to avoid accidentally targeting or hitting other players.
- When enabled and you are holding a pickaxe or mace, other players near you become translucent so they don't block your view. Pickaxe and mace each have an independent toggle.
- In the PrisonBreak world it stays active regardless of what you're holding (toggleable), since you're mining there constantly.
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
- Automatically detects private messages in chat (current server DM format) and plays your selected sound.
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

Auto Trade
- Shift-right-click another player to automatically send `/trade <username>` — no need to type their name.
- Enabled by default; toggle in the Misc tab.

Powerball Ready Alert
- When your Powerball cooldown ends, a customizable title pops up on screen with an optional sound cue.
- Title text, color, and visibility are configurable independently from the sound cue.
- Pick from 7 alert sounds with adjustable volume (0–200%).

Bold XP/Energy Popups
- Optionally bold the on-screen `+XP` and `+Energy` title popups the server shows while mining, so they stand out more.
- Off by default; toggle in the Misc tab.

PrisonBreak Texture Pack
- A bundled ore texture pack that automatically applies when you enter the PrisonBreak world and removes itself when you leave.
- Toggle in the Misc tab. (Switching the pack on/off triggers a brief resource reload, same as toggling any resource pack.)

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
- The sidebar tab list scrolls when screen height is too small to show all the tabs.
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
  - Meteorite shower and bandit rush event settings
  - Chest search, clue scroll sorting, auto trade, bold XP/energy popups, and PrisonBreak texture pack toggles
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
- (unbound by default) — Send a gang ping at the block you're looking at. Bind it in Minecraft's Controls menu under the BetterPrisons category.

---

Special thanks to NotRtzy, viseronic, and other pre-testers for their feedback and suggestions during development!
Contact nishu06 on discord for support or questions about the mod (Ideally by making a ticket in the mod discord (https://discord.gg/gW4sBdNmac).
Also check out the modrinth download at: https://modrinth.com/mod/betterprisons