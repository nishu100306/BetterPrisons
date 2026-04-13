package BetterPrisons.modid.misc;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemCooldownOverlay {

    // Matches lore lines like " 1m duration", " 30s duration", " 1m 30s duration"
    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\s*(?:(\\d+)m)?\\s*(?:(\\d+)s)?\\s+duration$");

    // Matches "Time Left: 6m 57s" or "Time to Unlock: 8m 0s"
    private static final Pattern TIME_LEFT_PATTERN = Pattern.compile("Time Left:\\s*(?:(\\d+)m)?\\s*(?:(\\d+)s)?");

    public static class CooldownResult {
        public final String text;
        public final int color;
        public final float scale;
        public final boolean bold;

        public CooldownResult(String text, int color, float scale, boolean bold) {
            this.text = text;
            this.color = color;
            this.scale = scale;
            this.bold = bold;
        }
    }

    public boolean isPet(ItemStack stack) {
        if (stack.isEmpty()) return false;
        try {
            String name = stack.getName().getString();
            return name.contains(" Pet [LVL ");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTrinket(ItemStack stack) {
        if (stack.isEmpty()) return false;
        try {
            String name = stack.getName().getString();
            return name.contains(" Trinket (");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBanditBox(ItemStack stack) {
        if (stack.isEmpty()) return false;
        try {
            String name = stack.getName().getString();
            return name.startsWith("Bandit Box:");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse the active duration in seconds from a pet's lore.
     * Looks for lines like " 1m duration" or " 30s duration".
     * Returns 0 if no duration found.
     */
    private int parseDurationFromLore(ItemStack stack) {
        try {
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore == null) return 0;

            for (Text line : lore.lines()) {
                String text = line.getString();
                Matcher matcher = DURATION_PATTERN.matcher(text);
                if (matcher.matches()) {
                    int totalSeconds = 0;
                    if (matcher.group(1) != null) {
                        totalSeconds += Integer.parseInt(matcher.group(1)) * 60;
                    }
                    if (matcher.group(2) != null) {
                        totalSeconds += Integer.parseInt(matcher.group(2));
                    }
                    return totalSeconds;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }

    /**
     * Parse "Time Left: Xm Ys" from lore and return seconds remaining.
     * Returns -1 if not found.
     */
    private int parseTimeLeftFromLore(ItemStack stack) {
        try {
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore == null) return -1;

            for (Text line : lore.lines()) {
                String text = line.getString();
                Matcher matcher = TIME_LEFT_PATTERN.matcher(text);
                if (matcher.find()) {
                    int totalSeconds = 0;
                    if (matcher.group(1) != null) {
                        totalSeconds += Integer.parseInt(matcher.group(1)) * 60;
                    }
                    if (matcher.group(2) != null) {
                        totalSeconds += Integer.parseInt(matcher.group(2));
                    }
                    return totalSeconds;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return -1;
    }

    /**
     * Get the pet_last_use_ms timestamp from the item's custom data.
     * Path: custom_data -> PublicBukkitValues -> cosmicprisons:pet_last_use_ms
     * Returns 0 if not found.
     */
    private long getLastUseMs(ItemStack stack) {
        try {
            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (customData == null) return 0;

            NbtCompound nbt = customData.copyNbt();
            NbtCompound bukkit = nbt.getCompound("PublicBukkitValues").orElse(null);
            if (bukkit == null || bukkit.isEmpty()) return 0;

            return bukkit.getLong("cosmicprisons:pet_last_use_ms").orElse(0L);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get the trinket_last_use_ms timestamp from the item's custom data.
     */
    private long getTrinketLastUseMs(ItemStack stack) {
        try {
            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (customData == null) return 0;

            NbtCompound nbt = customData.copyNbt();
            NbtCompound bukkit = nbt.getCompound("PublicBukkitValues").orElse(null);
            if (bukkit == null || bukkit.isEmpty()) return 0;

            return bukkit.getLong("cosmicprisons:trinket_last_use_ms").orElse(0L);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Check if an item has an active cooldown/timer and return the overlay text (mm:ss).
     * Returns null if no timer should be shown.
     */
    public CooldownResult getCooldownOverlay(ItemStack stack) {
        if (!BetterPrisonsClient.config.itemCooldownsEnabled) return null;
        if (stack.isEmpty()) return null;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return null;

        // Pet cooldown
        if (isPet(stack)) {
            if (!BetterPrisonsClient.config.itemCooldownsPetEnabled) return null;
            return getPetCooldownOverlay(stack);
        }

        // Trinket cooldown
        if (isTrinket(stack)) {
            if (!BetterPrisonsClient.config.itemCooldownsTrinketEnabled) return null;
            return getTrinketCooldownOverlay(stack);
        }

        // Bandit box timer
        if (isBanditBox(stack)) {
            if (!BetterPrisonsClient.config.itemCooldownsBanditBoxEnabled) return null;
            return getBanditBoxOverlay(stack);
        }

        return null;
    }

    private CooldownResult getPetCooldownOverlay(ItemStack stack) {
        // Get total cooldown from use_cooldown component
        UseCooldownComponent cooldownComponent = stack.get(DataComponentTypes.USE_COOLDOWN);
        if (cooldownComponent == null) return null;

        float totalCooldownSeconds = cooldownComponent.seconds();

        // Use pet_last_use_ms from item NBT — persists across world switches and death
        long lastUseMs = getLastUseMs(stack);
        if (lastUseMs <= 0) return null;

        long now = System.currentTimeMillis();
        long cooldownExpiresMs = lastUseMs + (long) (totalCooldownSeconds * 1000);
        float remainingCooldown = (cooldownExpiresMs - now) / 1000.0f;

        if (remainingCooldown <= 0) return null; // Cooldown finished

        // Check if pet has an active duration
        int durationSeconds = parseDurationFromLore(stack);
        if (durationSeconds > 0) {
            float elapsed = (now - lastUseMs) / 1000.0f;
            float remainingDuration = durationSeconds - elapsed;

            if (remainingDuration > 0) {
                // Pet effect is still active — show green duration timer
                int minutes = (int) (remainingDuration / 60);
                int seconds = (int) (remainingDuration % 60);
                String text = String.format("%d:%02d", minutes, seconds);

                return new CooldownResult(text,
                        0xFF000000 | BetterPrisonsClient.config.itemCooldownsPetActiveColor,
                        0.5f,
                        BetterPrisonsClient.config.itemCooldownsPetBold);
            }
        }

        // Pet effect expired (or no duration) — show cooldown timer
        int minutes = (int) (remainingCooldown / 60);
        int seconds = (int) (remainingCooldown % 60);
        String text = String.format("%d:%02d", minutes, seconds);

        return new CooldownResult(text,
                0xFF000000 | BetterPrisonsClient.config.itemCooldownsPetCooldownColor,
                0.5f,
                BetterPrisonsClient.config.itemCooldownsPetBold);
    }

    private CooldownResult getTrinketCooldownOverlay(ItemStack stack) {
        UseCooldownComponent cooldownComponent = stack.get(DataComponentTypes.USE_COOLDOWN);
        if (cooldownComponent == null) return null;

        float totalCooldownSeconds = cooldownComponent.seconds();

        long lastUseMs = getTrinketLastUseMs(stack);
        if (lastUseMs <= 0) return null;

        long now = System.currentTimeMillis();
        long cooldownExpiresMs = lastUseMs + (long) (totalCooldownSeconds * 1000);
        float remainingCooldown = (cooldownExpiresMs - now) / 1000.0f;

        if (remainingCooldown <= 0) return null;

        int minutes = (int) (remainingCooldown / 60);
        int seconds = (int) (remainingCooldown % 60);
        String text = String.format("%d:%02d", minutes, seconds);

        return new CooldownResult(text,
                0xFF000000 | BetterPrisonsClient.config.itemCooldownsTrinketColor,
                0.5f,
                BetterPrisonsClient.config.itemCooldownsTrinketBold);
    }

    private CooldownResult getBanditBoxOverlay(ItemStack stack) {
        try {
            String name = stack.getName().getString();

            if (name.contains("(Unlocking")) {
                // Actively unlocking — parse remaining time from lore
                int timeLeft = parseTimeLeftFromLore(stack);
                if (timeLeft <= 0) return null;

                int minutes = timeLeft / 60;
                int seconds = timeLeft % 60;
                String text = String.format("%d:%02d", minutes, seconds);

                return new CooldownResult(text,
                        0xFF000000 | BetterPrisonsClient.config.itemCooldownsBanditBoxColor,
                        0.5f,
                        BetterPrisonsClient.config.itemCooldownsBanditBoxBold);
            }

            // Locked or Unlocked — no timer needed
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
