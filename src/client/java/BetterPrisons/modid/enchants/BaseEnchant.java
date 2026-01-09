package BetterPrisons.modid.enchants;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class BaseEnchant {
    public String id;
    public String displayName;
    public boolean enabled = true;
    public boolean showOnHud = true;

    // Current state
    public boolean isActive = false;
    public long activatedAt = 0;
    public double durationSeconds = 0;
    public Text displayText = null; // Formatted text with color from item lore

    public BaseEnchant(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    // Called every tick - override if enchant needs tick-based detection
    public void tick(MinecraftClient client) {
        // Default: check if active effect has expired
        if (isActive && System.currentTimeMillis() > activatedAt + (long)(durationSeconds * 1000.0)) {
            isActive = false;
        }
    }

    // Called when a chat message is received - override for chat-based detection
    public void onChatMessage(String message) {
        // Override in subclass
    }

    // Call this when the enchant activates
    public void activate(double duration) {
        isActive = true;
        activatedAt = System.currentTimeMillis();
        durationSeconds = duration;
        displayText = null; // Reset to default
    }

    // Call this when the enchant activates with custom display text
    public void activate(double duration, Text displayText) {
        isActive = true;
        activatedAt = System.currentTimeMillis();
        durationSeconds = duration;
        this.displayText = displayText;
    }

    // Get remaining time in seconds (as double for decimal precision)
    public double getRemainingSeconds() {
        if (!isActive) return 0;
        long elapsed = System.currentTimeMillis() - activatedAt;
        return Math.max(0, durationSeconds - (elapsed / 1000.0));
    }
}
