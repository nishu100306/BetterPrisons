package BetterPrisons.modid.enchants;

import BetterPrisons.modid.BetterPrisonsClient;

public class SuperBreakerEnchant extends BaseEnchant {
    // Chat pattern to detect activation (filled in after server testing)
    public String activationPattern = "";

    public SuperBreakerEnchant() {
        super("super_breaker", "Super Breaker");
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
