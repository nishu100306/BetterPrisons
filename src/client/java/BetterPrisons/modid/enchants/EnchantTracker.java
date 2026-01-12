package BetterPrisons.modid.enchants;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class EnchantTracker {
    // Registry of all enchant handlers
    public List<BaseEnchant> enchants = new ArrayList<>();

    public EnchantTracker() {
        // Register all enchant classes
        enchants.add(new SuperBreakerEnchant());
        enchants.add(new PowerballEnchant());
        // Add more enchants here as they're implemented
    }

    public void tick(MinecraftClient client) {
        for (BaseEnchant enchant : enchants) {
            if (enchant.enabled) {
                enchant.tick(client);
            }
        }
    }

    // Called by ChatReceiveMixin or other detection sources
    public void onChatMessage(String message) {
        for (BaseEnchant enchant : enchants) {
            if (enchant.enabled) {
                enchant.onChatMessage(message);
            }
        }
    }

    // Find enchant by ID
    public BaseEnchant getEnchant(String id) {
        for (BaseEnchant e : enchants) {
            if (e.id.equals(id)) return e;
        }
        return null;
    }

    // Get all currently active enchants (for EnchantHud)
    public List<BaseEnchant> getActiveEnchants() {
        List<BaseEnchant> active = new ArrayList<>();
        for (BaseEnchant e : enchants) {
            if (e.isActive) {
                active.add(e);
            }
        }
        return active;
    }
}
