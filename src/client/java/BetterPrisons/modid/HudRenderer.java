package BetterPrisons.modid;

import BetterPrisons.modid.hud.BaseHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {
    // List of all HUDs to render
    private List<BaseHud> huds = new ArrayList<>();

    public HudRenderer() {
        // Add all HUDs
        huds.add(BetterPrisonsClient.cooldownHud);
        huds.add(BetterPrisonsClient.satchelHud);
        huds.add(BetterPrisonsClient.statsHud);
        huds.add(BetterPrisonsClient.enchantHud);
        huds.add(BetterPrisonsClient.meteorHud);
    }

    public void render(DrawContext context, RenderTickCounter counter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Render all enabled HUDs
        // Note: Scaling is available via hud.scale field
        // Individual HUDs need to use scaled() helper methods for proper scaling
        for (BaseHud hud : huds) {
            if (hud.enabled) {
                hud.render(context, client);
            }
        }
    }

    // Get all HUDs for HudEditorScreen
    public List<BaseHud> getHuds() {
        return huds;
    }
}
