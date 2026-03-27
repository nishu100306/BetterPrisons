package BetterPrisons.modid;

import BetterPrisons.modid.ui.custom.screens.CustomConfigScreen;
import BetterPrisons.modid.ui.custom.screens.WaypointsScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public KeyBinding resetStatsKey;
    public KeyBinding configKey;
    public KeyBinding pauseKey;
    public KeyBinding waypointsKey;
    public KeyBinding gangPingKey;
    public KeyBinding trucePingKey;

    public KeyBindings() {
        KeyBinding.Category bpCategory = KeyBinding.Category.create(Identifier.of("betterprisons", "betterprisons"));

        resetStatsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.reset_stats",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                bpCategory
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                bpCategory
        ));
        pauseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.pause",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                bpCategory
        ));
        waypointsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.waypoints",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                bpCategory
        ));
        gangPingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.gang_ping",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                bpCategory
        ));
        trucePingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.truce_ping",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                bpCategory
        ));
    }

    public void tick(MinecraftClient client) {
        if (resetStatsKey.wasPressed()) {
            BetterPrisonsClient.statsHud.resetTracking();
        }
        if (configKey.wasPressed()) {
            client.setScreen(new CustomConfigScreen(client.currentScreen));
        }
        if (pauseKey.wasPressed()) {
            BetterPrisonsClient.statsHud.togglePause();
            BetterPrisonsClient.LOGGER.info("Toggled Stats HUD pause state to " + BetterPrisonsClient.statsHud.paused);
        }
        if (waypointsKey.wasPressed()) {
            client.setScreen(new WaypointsScreen());
        }
        if (gangPingKey.wasPressed() && BetterPrisonsClient.config.gangPingEnabled) {
            BetterPrisonsClient.gangPingManager.sendPing(client);
        }
        if (trucePingKey.wasPressed() && BetterPrisonsClient.config.trucePingEnabled) {
            BetterPrisonsClient.gangPingManager.sendTrucePing(client);
        }
    }
}
