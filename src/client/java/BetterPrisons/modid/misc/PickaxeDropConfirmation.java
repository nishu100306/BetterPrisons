package BetterPrisons.modid.misc;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class PickaxeDropConfirmation {
    private long lastDropAttemptTime = 0;
    private static final long CONFIRMATION_WINDOW_MS = 3000; // 3 seconds

    /**
     * Checks if an item can be dropped, or if confirmation is needed
     * @param stack The ItemStack being dropped
     * @return true if the item should be dropped, false if it should be blocked (waiting for confirmation)
     */
    public boolean canDrop(ItemStack stack) {
        // Feature disabled
        if (!BetterPrisonsClient.config.pickaxeDropConfirmationEnabled) {
            return true;
        }

        // Not a pickaxe - allow drop
        if (!isPickaxe(stack)) {
            return true;
        }

        long now = System.currentTimeMillis();
        long timeSinceLastAttempt = now - lastDropAttemptTime;

        // Check if we're within the confirmation window
        if (timeSinceLastAttempt < CONFIRMATION_WINDOW_MS) {
            // Confirmed - allow drop and reset
            lastDropAttemptTime = 0;
            return true;
        } else {
            // First attempt or expired - show confirmation message
            lastDropAttemptTime = now;
            showConfirmationMessage();
            return false;
        }
    }

    /**
     * Shows the confirmation message in chat
     */
    private void showConfirmationMessage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§e§l[!] §6Are you sure you want to drop your pickaxe? Press drop again to confirm."),
                false
            );
        }
    }

    /**
     * Checks if an ItemStack is a pickaxe
     */
    private boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String itemName = stack.getItem().toString().toLowerCase();
        return itemName.contains("pickaxe");
    }

    /**
     * Resets the confirmation state (called on tick to handle window expiration)
     */
    public void tick() {
        long now = System.currentTimeMillis();
        long timeSinceLastAttempt = now - lastDropAttemptTime;

        // Reset if the confirmation window has expired
        if (lastDropAttemptTime > 0 && timeSinceLastAttempt >= CONFIRMATION_WINDOW_MS) {
            lastDropAttemptTime = 0;
        }
    }
}
