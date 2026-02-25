package BetterPrisons.modid;

import BetterPrisons.modid.ui.custom.screens.CustomConfigScreen;
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
    }
}
