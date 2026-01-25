package BetterPrisons.modid.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemUtils {

    /**
     * Extracts the enchantment text from the held item's lore that matches the search string
     * @param searchString The enchantment name to search for (e.g., "Super Breaker")
     * @return The formatted Text with color if found, null otherwise
     */
    public static Text extractEnchantTextFromHeldItem(String searchString) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return null;

        // Get the item in the main hand
        ItemStack heldItem = client.player.getMainHandStack();
        if (heldItem.isEmpty()) return null;

        // Get the lore component from the item
        LoreComponent loreComponent = heldItem.get(DataComponentTypes.LORE);
        if (loreComponent == null) return null;

        // Search for the enchant in the lore lines
        for (Text line : loreComponent.lines()) {
            String lineString = line.getString();
            if (lineString.contains(searchString)) {
                return line; // Return the formatted Text with color
            }
        }

        return null;
    }

    /**
     * Checks if the player is holding a pickaxe
     * @return true if the player is holding a pickaxe, false otherwise
     */
    public static boolean isHoldingPickaxe() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        ItemStack heldItem = client.player.getMainHandStack();
        if (heldItem.isEmpty()) return false;

        String itemName = heldItem.getItem().toString().toLowerCase();
        return itemName.contains("pickaxe");
    }
}
