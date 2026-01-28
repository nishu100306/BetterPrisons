package BetterPrisons.modid.ui.custom.screens;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.Config;
import BetterPrisons.modid.hud.BaseHud;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import BetterPrisons.modid.ui.custom.widgets.IntSliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Enhanced HUD editor screen with drag-to-move and scale controls.
 * Right-click on a HUD to adjust its scale (70-150%).
 */
public class EnhancedHudEditorScreen extends Screen {
    private final Screen parent;
    private BaseHud draggedHud = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean isDragging = false;

    private BaseHud selectedHud = null;
    private IntSliderWidget scaleSlider = null;

    public EnhancedHudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Load theme
        Theme.reload(BetterPrisonsClient.config);

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
        // Draw background
        RenderUtils.drawRect(context, 0, 0, width, height, Theme.screenBackground);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, Theme.textPrimary);

        // Draw instructions
        String instructions = "Left-click and drag to move | Right-click to adjust scale";
        context.drawCenteredTextWithShadow(this.textRenderer, instructions, this.width / 2, 25, Theme.textSecondary);

        // Render all HUDs with bounding boxes
        List<BaseHud> huds = BetterPrisonsClient.hudRenderer.getHuds();
        for (BaseHud hud : huds) {
            if (!hud.enabled) continue;

            // Render the actual HUD content
            hud.render(context, this.client);

            // Get actual dimensions from HUD
            int hudWidth = hud.getWidth() > 0 ? hud.getWidth() : 100;
            int hudHeight = hud.getHeight() > 0 ? hud.getHeight() : 50;

            // Determine border color
            int borderColor;
            if (hud == draggedHud) {
                borderColor = 0xFFFFFF00; // Yellow (dragging)
            } else if (hud == selectedHud) {
                borderColor = 0xFFFF8800; // Orange (selected for scaling)
            } else if (isMouseOverHud(mouseX, mouseY, hud)) {
                borderColor = 0x8000FF00; // Green (hovered)
            } else {
                borderColor = 0x40FFFFFF; // Gray (normal)
            }

            // Draw border
            drawBorder(context, hud.x - 2, hud.y - 2, hudWidth + 4, hudHeight + 4, borderColor);

            // Draw scale percentage if selected
            if (hud == selectedHud) {
                int scalePercent = (int) (hud.scale * 100);
                String scaleText = scalePercent + "%";
                int textWidth = textRenderer.getWidth(scaleText);
                context.drawTextWithShadow(textRenderer, scaleText, hud.x + hudWidth - textWidth, hud.y - 15, Theme.textAccent);
            }
        }

        // Render scale slider if a HUD is selected
        if (selectedHud != null && scaleSlider != null) {
            scaleSlider.render(context, mouseX, mouseY, delta);
        }

        // Render buttons
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if clicking on scale slider
        if (scaleSlider != null && scaleSlider.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        List<BaseHud> huds = BetterPrisonsClient.hudRenderer.getHuds();

        if (button == 0) { // Left click - drag to move
            for (BaseHud hud : huds) {
                if (!hud.enabled) continue;

                if (isMouseOverHud(mouseX, mouseY, hud)) {
                    draggedHud = hud;
                    dragOffsetX = (int) (mouseX - hud.x);
                    dragOffsetY = (int) (mouseY - hud.y);
                    isDragging = true;
                    return true;
                }
            }
        } else if (button == 1) { // Right click - show scale slider
            for (BaseHud hud : huds) {
                if (!hud.enabled) continue;

                if (isMouseOverHud(mouseX, mouseY, hud)) {
                    selectedHud = hud;
                    createScaleSlider(hud);
                    return true;
                }
            }

            // Clicked outside, deselect
            selectedHud = null;
            scaleSlider = null;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Handle scale slider dragging
        if (scaleSlider != null && scaleSlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            // Update HUD scale from slider
            if (selectedHud != null) {
                selectedHud.scale = scaleSlider.getValue() / 100.0f;
            }
            return true;
        }

        // Handle HUD dragging
        if (isDragging && draggedHud != null) {
            // Update HUD position
            draggedHud.x = (int) (mouseX - dragOffsetX);
            draggedHud.y = (int) (mouseY - dragOffsetY);

            // Clamp to screen bounds
            int hudWidth = draggedHud.getWidth() > 0 ? draggedHud.getWidth() : 100;
            int hudHeight = draggedHud.getHeight() > 0 ? draggedHud.getHeight() : 50;
            draggedHud.x = Math.max(0, Math.min(draggedHud.x, this.width - hudWidth));
            draggedHud.y = Math.max(0, Math.min(draggedHud.y, this.height - hudHeight));

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Handle scale slider release
        if (scaleSlider != null && scaleSlider.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0 && isDragging) {
            isDragging = false;
            draggedHud = null;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isMouseOverHud(double mouseX, double mouseY, BaseHud hud) {
        int width = hud.getWidth() > 0 ? hud.getWidth() : 100;
        int height = hud.getHeight() > 0 ? hud.getHeight() : 50;
        return mouseX >= hud.x && mouseX <= hud.x + width &&
               mouseY >= hud.y && mouseY <= hud.y + height;
    }

    private void createScaleSlider(BaseHud hud) {
        int currentScale = (int) (hud.scale * 100);
        scaleSlider = new IntSliderWidget("Scale", currentScale, 70, 150, "%");

        // Position slider near the HUD but ensure it's visible
        int sliderX = hud.x;
        int sliderY = hud.y + hud.getHeight() + 10;

        // Adjust if off-screen
        if (sliderX + 300 > width) {
            sliderX = width - 300;
        }
        if (sliderY + 30 > height - 60) {
            sliderY = hud.y - 40;
        }

        scaleSlider.setPosition(sliderX, sliderY);
        scaleSlider.setSize(300, 20);
    }

    private void saveAndClose() {
        // Save positions and scales to config
        Config config = BetterPrisonsClient.config;

        config.cooldownHudX = BetterPrisonsClient.cooldownHud.x;
        config.cooldownHudY = BetterPrisonsClient.cooldownHud.y;
        config.cooldownHudScale = (int) (BetterPrisonsClient.cooldownHud.scale * 100);

        config.satchelHudX = BetterPrisonsClient.satchelHud.x;
        config.satchelHudY = BetterPrisonsClient.satchelHud.y;
        config.satchelHudScale = (int) (BetterPrisonsClient.satchelHud.scale * 100);

        config.statsHudX = BetterPrisonsClient.statsHud.x;
        config.statsHudY = BetterPrisonsClient.statsHud.y;
        config.statsHudScale = (int) (BetterPrisonsClient.statsHud.scale * 100);

        config.enchantHudX = BetterPrisonsClient.enchantHud.x;
        config.enchantHudY = BetterPrisonsClient.enchantHud.y;
        config.enchantHudScale = (int) (BetterPrisonsClient.enchantHud.scale * 100);

        config.meteorHudX = BetterPrisonsClient.meteorHud.x;
        config.meteorHudY = BetterPrisonsClient.meteorHud.y;
        config.meteorHudScale = (int) (BetterPrisonsClient.meteorHud.scale * 100);

        config.save();
        BetterPrisonsClient.LOGGER.info("Saved HUD positions and scales to config");

        // Close and return to parent screen
        this.client.setScreen(parent);
    }

    private void resetPositions() {
        // Reset to default positions and scales
        BetterPrisonsClient.cooldownHud.x = 6;
        BetterPrisonsClient.cooldownHud.y = 7;
        BetterPrisonsClient.cooldownHud.scale = 1.0f;

        BetterPrisonsClient.satchelHud.x = 7;
        BetterPrisonsClient.satchelHud.y = 127;
        BetterPrisonsClient.satchelHud.scale = 1.0f;

        BetterPrisonsClient.statsHud.x = 535;
        BetterPrisonsClient.statsHud.y = 212;
        BetterPrisonsClient.statsHud.scale = 1.0f;

        BetterPrisonsClient.enchantHud.x = 517;
        BetterPrisonsClient.enchantHud.y = 4;
        BetterPrisonsClient.enchantHud.scale = 1.0f;

        BetterPrisonsClient.meteorHud.x = 8;
        BetterPrisonsClient.meteorHud.y = 74;
        BetterPrisonsClient.meteorHud.scale = 1.0f;

        // Clear selection
        selectedHud = null;
        scaleSlider = null;
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
        ctx.fill(x, y, x + width, y + 2, color);
        // Bottom
        ctx.fill(x, y + height - 2, x + width, y + height, color);
        // Left
        ctx.fill(x, y, x + 2, y + height, color);
        // Right
        ctx.fill(x + width - 2, y, x + width, y + height, color);
    }
}
