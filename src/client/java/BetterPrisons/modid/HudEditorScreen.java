package BetterPrisons.modid;

import BetterPrisons.modid.hud.BaseHud;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class HudEditorScreen extends Screen {
    private final Screen parent;
    private BaseHud draggedHud = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean isDragging = false;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Add "Done" button at bottom center
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            saveAndClose();
        }).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());

        // Add "Reset Positions" button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset Positions"), button -> {
            resetPositions();
        }).dimensions(this.width / 2 - 100, this.height - 55, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background once
        super.render(context, mouseX, mouseY, delta);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        // Draw instructions
        String instructions = "Click and drag HUD elements to reposition them";
        context.drawCenteredTextWithShadow(this.textRenderer, instructions, this.width / 2, 25, 0xAAAAAA);

        // Render all HUDs with bounding boxes
        List<BaseHud> huds = BetterPrisonsClient.hudRenderer.getHuds();
        for (BaseHud hud : huds) {
            if (!hud.enabled) continue;

            // Render the actual HUD content
            hud.render(context, this.client);

            // Get actual dimensions from HUD
            int width = hud.getWidth() > 0 ? hud.getWidth() : 100;
            int height = hud.getHeight() > 0 ? hud.getHeight() : 50;

            // Draw border only (no background, no title)
            // Yellow if being dragged, semi-transparent green otherwise
            int borderColor = (hud == draggedHud) ? 0xFFFFFF00 : 0x8000FF00;
            drawBorder(context, hud.x - 2, hud.y - 2, width + 4, height + 4, borderColor);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button == 0) { // Left click
            List<BaseHud> huds = BetterPrisonsClient.hudRenderer.getHuds();
            for (BaseHud hud : huds) {
                if (!hud.enabled) continue;

                int width = hud.getWidth() > 0 ? hud.getWidth() : 100;
                int height = hud.getHeight() > 0 ? hud.getHeight() : 50;

                // Check if click is within HUD bounds
                if (mouseX >= hud.x && mouseX <= hud.x + width &&
                    mouseY >= hud.y && mouseY <= hud.y + height) {

                    draggedHud = hud;
                    dragOffsetX = (int)(mouseX - hud.x);
                    dragOffsetY = (int)(mouseY - hud.y);
                    isDragging = true;
                    return true;
                }
            }
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (isDragging && draggedHud != null) {
            // Update HUD position
            draggedHud.x = (int)(mouseX - dragOffsetX);
            draggedHud.y = (int)(mouseY - dragOffsetY);

            // Clamp to screen bounds using actual HUD dimensions
            int hudWidth = draggedHud.getWidth() > 0 ? draggedHud.getWidth() : 100;
            int hudHeight = draggedHud.getHeight() > 0 ? draggedHud.getHeight() : 50;
            draggedHud.x = Math.max(0, Math.min(draggedHud.x, this.width - hudWidth));
            draggedHud.y = Math.max(0, Math.min(draggedHud.y, this.height - hudHeight));

            return true;
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && isDragging) {
            isDragging = false;
            draggedHud = null;
            return true;
        }

        return super.mouseReleased(click);
    }

    private void saveAndClose() {
        // Save positions to config
        Config config = BetterPrisonsClient.config;

        config.cooldownHudX = BetterPrisonsClient.cooldownHud.x;
        config.cooldownHudY = BetterPrisonsClient.cooldownHud.y;
        config.satchelHudX = BetterPrisonsClient.satchelHud.x;
        config.satchelHudY = BetterPrisonsClient.satchelHud.y;
        config.statsHudX = BetterPrisonsClient.statsHud.x;
        config.statsHudY = BetterPrisonsClient.statsHud.y;
        config.enchantHudX = BetterPrisonsClient.enchantHud.x;
        config.enchantHudY = BetterPrisonsClient.enchantHud.y;
        config.eventsHudX = BetterPrisonsClient.eventsHud.x;
        config.eventsHudY = BetterPrisonsClient.eventsHud.y;

        config.save();
        BetterPrisonsClient.LOGGER.info("Saved HUD positions to config");

        // Close and return to parent screen
        this.client.setScreen(parent);
    }

    private void resetPositions() {
        // Reset to default positions
        BetterPrisonsClient.cooldownHud.x = 10;
        BetterPrisonsClient.cooldownHud.y = 10;
        BetterPrisonsClient.satchelHud.x = 10;
        BetterPrisonsClient.satchelHud.y = 100;
        BetterPrisonsClient.statsHud.x = 10;
        BetterPrisonsClient.statsHud.y = 200;
        BetterPrisonsClient.enchantHud.x = 10;
        BetterPrisonsClient.enchantHud.y = 300;
    }

    @Override
    public void close() {
        saveAndClose();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void drawBorder(DrawContext ctx, int x, int y, int width, int height, int color) {
        // Top
        ctx.fill(x, y, x + width, y + 1, color);
        // Bottom
        ctx.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        ctx.fill(x, y, x + 1, y + height, color);
        // Right
        ctx.fill(x + width - 1, y, x + width, y + height, color);
    }
}
