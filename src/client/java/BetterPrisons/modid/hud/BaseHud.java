package BetterPrisons.modid.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class BaseHud {
    public String id;
    public boolean enabled = true;

    // Position (loaded from config)
    public int x = 10;
    public int y = 10;

    // Scale (loaded from config)
    public float scale = 1.0f;

    public BaseHud(String id) {
        this.id = id;
    }

    // Called every tick - subclass handles its own data updates
    public void tick() {}
    public void tick(MinecraftClient client) {}

    // Each HUD implements its own rendering
    public abstract void render(DrawContext context, MinecraftClient client);

    // Optional: get height for stacking HUDs
    public int getHeight() { return 0; }

    // Helper methods for scaling
    // Use these in render() method to scale widths, heights, offsets, etc.
    // Example: ctx.fill(x, y, x + scaled(100), y + scaled(50), color)
    protected int scaled(int value) {
        return (int)(value * scale);
    }

    protected float scaled(float value) {
        return value * scale;
    }
}
