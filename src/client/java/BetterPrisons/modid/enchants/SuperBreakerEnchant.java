package BetterPrisons.modid.enchants;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;

public class SuperBreakerEnchant extends BaseEnchant {
    // Chat pattern to detect activation (filled in after server testing)
    public String activationPattern = "";

    public SuperBreakerEnchant() {
        super("super_breaker", "Super Breaker");
    }

    @Override
    public void tick(MinecraftClient client) {
        // If super breaker is active, verify player is still holding a pickaxe with the enchantment
        if (isActive) {
            // Check if player is holding a pickaxe with super breaker
            boolean holdingValidPickaxe = ItemUtils.isHoldingPickaxe() &&
                                          ItemUtils.extractEnchantTextFromHeldItem("Super Breaker") != null;

            if (!holdingValidPickaxe) {
                // Player switched items or no longer holding valid pickaxe - deactivate
                isActive = false;
                return;
            }
        }

        // Call parent tick to handle duration expiration
        super.tick(client);
    }

    @Override
    public void onChatMessage(String message) {
        EnchantParsing parsing = new EnchantParsing();

        // Check if message matches activation pattern
        if (parsing.messageMatches(message, activationPattern)) {
            // Parse duration from message
            int duration = parsing.parseSecondsFromMessage(message, activationPattern);
            if (duration > 0) {
                activate(duration);

                // Optionally start a cooldown on the HUD
                if (showOnHud) {
                    parsing.startCooldown(displayName, duration);
                }
            }
        }
    }

    // Can add more detection methods as needed:
    // - Check for potion effects
    // - Check mining speed changes
    // - Listen for specific sounds
}
