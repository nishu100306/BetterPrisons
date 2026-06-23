package BetterPrisons.modid;

import BetterPrisons.modid.devtools.DevCommands;
import BetterPrisons.modid.devtools.ParticleDebugTracker;
import BetterPrisons.modid.devtools.SoundDebugListener;
import BetterPrisons.modid.devtools.SoundTracker;
import BetterPrisons.modid.gangping.GangPingManager;
import BetterPrisons.modid.enchants.EnchantParsing;
import BetterPrisons.modid.enchants.EnchantTracker;
import BetterPrisons.modid.hud.CooldownHud;
import BetterPrisons.modid.hud.EnchantHud;
import BetterPrisons.modid.hud.EventsHud;
import BetterPrisons.modid.hud.MinimapHud;
import BetterPrisons.modid.hud.SatchelHud;
import BetterPrisons.modid.hud.StatsHud;
import BetterPrisons.modid.hud.SuperBreakerAura;
import BetterPrisons.modid.misc.EasyView;
import BetterPrisons.modid.misc.EnchantBookTooltip;
import BetterPrisons.modid.api.CosmicApi;
import BetterPrisons.modid.chestsearch.ClueScrollOverlay;
import BetterPrisons.modid.misc.GangPointTooltip;
import BetterPrisons.modid.misc.ItemCooldownOverlay;
import BetterPrisons.modid.misc.PickaxeDropConfirmation;
import BetterPrisons.modid.misc.PrisonbreakTexturePack;
import BetterPrisons.modid.render.BeaconBeamRenderer;
import BetterPrisons.modid.waypoint.WaypointManager;
import BetterPrisons.modid.render.WaypointRenderer;
import BetterPrisons.modid.render.WorldSpaceTransform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterPrisonsClient implements ClientModInitializer {
    public static final String MOD_ID = "betterprisons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Core systems
    public static Config config;
    public static KeyBindings keybinds;
    public static HudRenderer hudRenderer;
    public static SuperBreakerAura superBreakerAura;

    // HUD elements (each handles its own data + rendering)
    public static CooldownHud cooldownHud;
    public static SatchelHud satchelHud;
    public static StatsHud statsHud;
    public static EnchantHud enchantHud;
    public static EventsHud eventsHud;
    public static MinimapHud minimapHud;

    // Enchant detection system
    public static EnchantParsing enchantParsing;
    public static EnchantTracker enchantTracker;

    // Misc features
    public static EasyView easyView;
    public static ItemCooldownOverlay itemCooldownOverlay;
    public static PickaxeDropConfirmation pickaxeDropConfirmation;

    // Custom waypoints
    public static WaypointManager waypointManager;

    // Gang pings
    public static GangPingManager gangPingManager;

    // Debug listeners
    public static SoundDebugListener soundDebugListener;
    public static boolean soundDebugEnabled = true;
    private static boolean soundListenerRegistered = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing BetterPrisons...");

        // Initialize config
        config = new Config();
        config.load();

        // Register the bundled PrisonBreak texture pack
        PrisonbreakTexturePack.register();

        // Cosmic Mods server API channel + handshake.
        // Left unused for now — the server side isn't fully set up yet. Re-enable when ready.
        // CosmicApi.register();

        // Initialize waypoint manager
        waypointManager = new WaypointManager();
        waypointManager.load();

        // Initialize gang ping manager
        gangPingManager = new GangPingManager();

        // Initialize keybinds
        keybinds = new KeyBindings();

        // Initialize HUDs
        cooldownHud = new CooldownHud();
        cooldownHud.loadFromJson();

        satchelHud = new SatchelHud();
        statsHud = new StatsHud();
        enchantHud = new EnchantHud();
        eventsHud = new EventsHud();

        // Load positions and settings from config
        cooldownHud.x = config.cooldownHudX;
        cooldownHud.y = config.cooldownHudY;
        cooldownHud.enabled = config.cooldownHudEnabled;
        cooldownHud.scale = config.cooldownHudScale;
        LOGGER.info("CooldownHud: x={}, y={}, enabled={}, scale={}", cooldownHud.x, cooldownHud.y, cooldownHud.enabled, cooldownHud.scale);

        satchelHud.x = config.satchelHudX;
        satchelHud.y = config.satchelHudY;
        satchelHud.enabled = config.satchelHudEnabled;
        satchelHud.scale = config.satchelHudScale;
        LOGGER.info("SatchelHud: x={}, y={}, enabled={}, scale={}", satchelHud.x, satchelHud.y, satchelHud.enabled, satchelHud.scale);

        statsHud.x = config.statsHudX;
        statsHud.y = config.statsHudY;
        statsHud.enabled = config.statsHudEnabled;
        statsHud.scale = config.statsHudScale;
        LOGGER.info("StatsHud: x={}, y={}, enabled={}, scale={}", statsHud.x, statsHud.y, statsHud.enabled, statsHud.scale);

        enchantHud.x = config.enchantHudX;
        enchantHud.y = config.enchantHudY;
        enchantHud.enabled = config.enchantHudEnabled;
        enchantHud.scale = config.enchantHudScale;
        LOGGER.info("EnchantHud: x={}, y={}, enabled={}, scale={}", enchantHud.x, enchantHud.y, enchantHud.enabled, enchantHud.scale);

        eventsHud.x = config.eventsHudX;
        eventsHud.y = config.eventsHudY;
        eventsHud.enabled = config.eventsHudEnabled;
        eventsHud.scale = config.eventsHudScale;
        LOGGER.info("EventsHud: x={}, y={}, enabled={}, scale={}", eventsHud.x, eventsHud.y, eventsHud.enabled, eventsHud.scale);

        minimapHud = new MinimapHud();
        minimapHud.x                = config.minimapX;
        minimapHud.y                = config.minimapY;
        minimapHud.enabled          = config.minimapEnabled;
        minimapHud.scale            = config.minimapScale / 100.0f;
        minimapHud.mapSize          = config.minimapSize;
        minimapHud.pixelsPerBlock   = config.minimapPixelsPerBlock;
        minimapHud.circleShape      = config.minimapCircleShape;
        minimapHud.rotating         = config.minimapRotating;
        minimapHud.showWaypoints    = config.minimapShowWaypoints;
        minimapHud.showCoords       = config.minimapShowCoords;
        minimapHud.borderColor      = config.minimapBorderColor;
        minimapHud.borderOpacity    = config.minimapBorderOpacity;
        minimapHud.borderThickness  = config.minimapBorderThickness;
        LOGGER.info("MinimapHud: x={}, y={}, enabled={}", minimapHud.x, minimapHud.y, minimapHud.enabled);

        // HudRenderer coordinates all HUDs
        hudRenderer = new HudRenderer();

        // Capture view/projection matrices each frame for accurate 2D projection
        WorldSpaceTransform.register();

        // Register waypoint overlay (screen-edge direction indicators)
        WaypointRenderer.register();

        // Register 3D beacon beam renderer (vertical pillars at event coords)
        BeaconBeamRenderer.register();

        // Super Breaker aura
        superBreakerAura = new SuperBreakerAura();

        // Enchant system
        enchantParsing = new EnchantParsing();
        enchantTracker = new EnchantTracker();

        // Misc features
        easyView = new EasyView();
        easyView.enabled = config.easyViewEnabled;
        itemCooldownOverlay = new ItemCooldownOverlay();
        pickaxeDropConfirmation = new PickaxeDropConfirmation();

        // Debug listeners
        soundDebugListener = new SoundDebugListener();

        // Register callbacks
        HudRenderCallback.EVENT.register((context, counter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            // Respect F1 (hide HUD) — skip all BP HUDs when vanilla HUD is hidden
            if (mc.options.hudHidden) return;
            hudRenderer.render(context, counter);
            superBreakerAura.render(context, mc);
        });

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            EnchantBookTooltip.append(stack, lines);
            GangPointTooltip.append(stack, lines);
            ClueScrollOverlay.appendTooltip(stack, lines);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Keep waypoint manager aware of which world the player is in
            if (client.world != null) {
                String worldKey = WaypointManager.detectWorldKey();
                if (!worldKey.equals(waypointManager.getCurrentWorld())) {
                    // First world join in this session — clear stale event waypoints
                    if ("unknown".equals(waypointManager.getCurrentWorld())) {
                        waypointManager.clearAllEventWaypoints();
                    }
                    waypointManager.setCurrentWorld(worldKey);
                }
            }

            // Auto-apply / remove the bundled PrisonBreak texture pack by world
            boolean inPrisonbreak = client.world != null
                && "minecraft:prisonbreak".equals(client.world.getRegistryKey().getValue().toString());
            PrisonbreakTexturePack.update(inPrisonbreak);

            // Register sound listener once when sound manager is available
            if (soundDebugEnabled && !soundListenerRegistered && client.getSoundManager() != null) {
                client.getSoundManager().registerListener(soundDebugListener);
                soundListenerRegistered = true;
            }

            keybinds.tick(client);
            cooldownHud.tick();
            satchelHud.tick(client);
            statsHud.tick(client);
            enchantTracker.tick(client);
            enchantHud.tick();
            eventsHud.tick();
            gangPingManager.tick();
            minimapHud.tick(client);
            easyView.tick(client);
            pickaxeDropConfirmation.tick();

            // Clear debug caches at end of tick
            soundDebugListener.clearTickCache();
            ParticleDebugTracker.logAndClear();
            SoundTracker.clearTickCache();
        });

        // Register dev commands
        ClientCommandRegistrationCallback.EVENT.register(DevCommands::register);

        LOGGER.info("BetterPrisons initialized successfully!");
    }

    /** Re-applies config-backed fields to live HUD objects. Call after config.save(). */
    public static void applyConfig() {
        cooldownHud.enabled = config.cooldownHudEnabled;
        satchelHud.enabled = config.satchelHudEnabled;
        statsHud.enabled = config.statsHudEnabled;
        enchantHud.enabled = config.enchantHudEnabled;
        eventsHud.enabled = config.eventsHudEnabled;
        easyView.enabled = config.easyViewEnabled;

        minimapHud.enabled         = config.minimapEnabled;
        minimapHud.mapSize         = config.minimapSize;
        minimapHud.pixelsPerBlock  = config.minimapPixelsPerBlock;
        minimapHud.circleShape     = config.minimapCircleShape;
        minimapHud.rotating        = config.minimapRotating;
        minimapHud.showWaypoints   = config.minimapShowWaypoints;
        minimapHud.showCoords      = config.minimapShowCoords;
        minimapHud.borderColor     = config.minimapBorderColor;
        minimapHud.borderOpacity   = config.minimapBorderOpacity;
        minimapHud.borderThickness = config.minimapBorderThickness;
    }
}
