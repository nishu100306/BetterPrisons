package BetterPrisons.modid;

import BetterPrisons.modid.devtools.DevCommands;
import BetterPrisons.modid.devtools.ParticleDebugTracker;
import BetterPrisons.modid.devtools.SoundDebugListener;
import BetterPrisons.modid.devtools.SoundTracker;
import BetterPrisons.modid.enchants.EnchantParsing;
import BetterPrisons.modid.enchants.EnchantTracker;
import BetterPrisons.modid.hud.CooldownHud;
import BetterPrisons.modid.hud.EnchantHud;
import BetterPrisons.modid.hud.MeteorHud;
import BetterPrisons.modid.hud.SatchelHud;
import BetterPrisons.modid.hud.StatsHud;
import BetterPrisons.modid.hud.SuperBreakerAura;
import BetterPrisons.modid.misc.EasyView;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
    public static MeteorHud meteorHud;

    // Enchant detection system
    public static EnchantParsing enchantParsing;
    public static EnchantTracker enchantTracker;

    // Misc features
    public static EasyView easyView;

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

        // Initialize keybinds
        keybinds = new KeyBindings();

        // Initialize HUDs
        cooldownHud = new CooldownHud();
        cooldownHud.loadFromJson();

        satchelHud = new SatchelHud();
        statsHud = new StatsHud();
        enchantHud = new EnchantHud();
        meteorHud = new MeteorHud();

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

        meteorHud.x = config.meteorHudX;
        meteorHud.y = config.meteorHudY;
        meteorHud.enabled = config.meteorHudEnabled;
        meteorHud.scale = config.meteorHudScale;
        LOGGER.info("MeteorHud: x={}, y={}, enabled={}, scale={}", meteorHud.x, meteorHud.y, meteorHud.enabled, meteorHud.scale);

        // HudRenderer coordinates all HUDs
        hudRenderer = new HudRenderer();

        // Super Breaker aura
        superBreakerAura = new SuperBreakerAura();

        // Enchant system
        enchantParsing = new EnchantParsing();
        enchantTracker = new EnchantTracker();

        // Misc features
        easyView = new EasyView();
        easyView.enabled = config.easyViewEnabled;

        // Debug listeners
        soundDebugListener = new SoundDebugListener();

        // Register callbacks
        HudRenderCallback.EVENT.register((context, counter) -> {
            hudRenderer.render(context, counter);
            superBreakerAura.render(context, MinecraftClient.getInstance());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
            meteorHud.tick();
            easyView.tick(client);

            // Clear debug caches at end of tick
            soundDebugListener.clearTickCache();
            ParticleDebugTracker.logAndClear();
            SoundTracker.clearTickCache();
        });

        // Register dev commands
        ClientCommandRegistrationCallback.EVENT.register(DevCommands::register);

        LOGGER.info("BetterPrisons initialized successfully!");
    }
}
