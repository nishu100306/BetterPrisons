package BetterPrisons.modid.enchants;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.devtools.ParticleDebugTracker;
import BetterPrisons.modid.devtools.SoundTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class PowerballEnchant extends BaseEnchant {

    public PowerballEnchant() {
        super("powerball", "Powerball");
    }

    @Override
    public void tick(MinecraftClient client) {
        // Call parent tick to handle expiration
        super.tick(client);

        // Check if wither shoot sound was heard this tick
        if (SoundTracker.wasWitherShootSoundHeard() && client.player != null) {
            ItemStack heldItem = client.player.getMainHandStack();
            // Only activate if holding a pickaxe
            if (heldItem.getItem().getTranslationKey().contains("pickaxe")) {
                onWitherSoundDetected(heldItem);
            }
        }
    }

    /**
     * Called when wither shoot sound is detected
     * Parses the held pickaxe to determine powerball level and activates with appropriate duration
     */
    public void onWitherSoundDetected(ItemStack pickaxe) {
        BetterPrisonsClient.LOGGER.info("Powerball sound detected, checking pickaxe for enchant...");
        if (pickaxe == null || pickaxe.isEmpty()) return;

        // Parse powerball level from lore
        int powerballLevel = getPowerballLevel(pickaxe);
        if (powerballLevel == 0) return; // No powerball enchant

        // Determine duration based on level
        int duration;
        switch (powerballLevel) {
            case 1: duration = 60; break;
            case 2: duration = 50; break;
            case 3: duration = 40; break;
            default: return;
        }

        // Extract formatted text from pickaxe lore
        Text enchantText = ParticleDebugTracker.extractEnchantTextFromHeldItem("Powerball");

        // Activate the enchant with formatted text if found, otherwise use default
        if (enchantText != null) {
            activate(duration, enchantText);
            BetterPrisonsClient.LOGGER.info("Powerball activated with formatted text: {}", enchantText.getString());
        } else {
            activate(duration);
            BetterPrisonsClient.LOGGER.info("Powerball activated with default text");
        }
    }

    /**
     * Parses the powerball level from pickaxe lore
     * Returns 0 if no powerball enchant found, otherwise returns 1, 2, or 3
     */
    private int getPowerballLevel(ItemStack pickaxe) {
        try {
            LoreComponent lore = pickaxe.get(DataComponentTypes.LORE);
            if (lore == null || lore.lines().isEmpty()) return 0;

            // Search for powerball line in lore
            for (Text line : lore.lines()) {
                String lineText = line.getString().toLowerCase().replaceAll("§.", "");

                // Look for "powerball" followed by a level (I, II, III or 1, 2, 3)
                if (lineText.contains("powerball")) {
                    // Check for Roman numerals
                    if (lineText.contains("iii")) return 3;
                    if (lineText.contains("ii")) return 2;
                    if (lineText.contains("i")) return 1;

                    // Check for numbers
                    if (lineText.contains("3")) return 3;
                    if (lineText.contains("2")) return 2;
                    if (lineText.contains("1")) return 1;
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }

        return 0;
    }

    @Override
    public void onChatMessage(String message) {
        // Powerball is detected via sound, not chat
        // This method can remain empty or be used for additional detection methods
    }
}
