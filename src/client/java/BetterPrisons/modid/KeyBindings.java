package BetterPrisons.modid;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public KeyBinding resetStatsKey;
    public KeyBinding configKey;

    public KeyBindings() {
        resetStatsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.reset_stats",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.betterprisons"
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.betterprisons.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.betterprisons"
        ));
    }

    public void tick(MinecraftClient client) {
        if (resetStatsKey.wasPressed()) {
            BetterPrisonsClient.statsHud.resetTracking();
        }
        if (configKey.wasPressed()) {
            client.setScreen(BetterPrisonsClient.config.createConfigScreen(client.currentScreen));
        }
    }
}
