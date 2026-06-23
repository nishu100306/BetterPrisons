package BetterPrisons.modid.chestsearch;

import BetterPrisons.modid.BetterPrisonsClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clue Scroll Sorting — a special case of the container search overlay. Instead
 * of tinting a slot, this draws the clue's current (furthest) step number large
 * and centered, filling the item slot, so chests of clue scrolls can be sorted
 * at a glance.
 *
 * The step number is derived from the active clue's {@code type} in the
 * {@code cosmicprisons:clue_scroll_data} JSON.
 */
public final class ClueScrollOverlay {

    /**
     * Clue step NBT type → displayed step number. The enum strings are NOT
     * derivable from the human-readable descriptions, so each must be confirmed
     * from a real clue scroll's NBT. Unmapped types render "?" and log a warning
     * with the type name so they can be added here.
     */
    private static final Map<String, Integer> TYPE_TO_STEP = new HashMap<>();
    static {
        // --- Tinker (steps 1–4) ---
        TYPE_TO_STEP.put("TINKER_PICKAXE", 1);             // Tinker a level+ pickaxe
        TYPE_TO_STEP.put("TINKER_GEAR", 2);                // Tinker a level+ piece of gear
        TYPE_TO_STEP.put("TINKER_ENCHANT_PICKAXE", 3);     // Tinker an Enchantment orb (orb → pickaxe)
        TYPE_TO_STEP.put("TINKER_ENCHANT_GEAR", 4);        // Tinker an Enchantment Book (confirmed)

        // --- Mining / meteor (steps 5–9) ---
        TYPE_TO_STEP.put("MINE_ORE_REFINED", 5);           // Mine an Ore
        TYPE_TO_STEP.put("MINE_ORE_REGULAR", 5);           // Mine an Ore (variant)
        TYPE_TO_STEP.put("MINE_METEORITE_ORE", 6);         // Mine Ore or better from a Meteorite
        TYPE_TO_STEP.put("MINE_METEOR_BLOCK", 7);          // Mine a Meteor block
        TYPE_TO_STEP.put("MINE_METEOR_CONTRABAND", 8);     // Mine a Meteor block (contraband variant)
        TYPE_TO_STEP.put("DISCOVER_CONTRABAND", 8);        // Mine a Contraband from a Meteor
        TYPE_TO_STEP.put("DISCOVER_SHARD", 9);             // Discover shards from mining

        // --- Open / market (steps 10–15) ---
        TYPE_TO_STEP.put("ITEM_USE_SHARD", 10);            // Open Shards
        TYPE_TO_STEP.put("ITEM_USE_CONTRABAND", 11);       // Open a Contraband
        TYPE_TO_STEP.put("ITEM_USE_MYSTERY_ENCHANT_GEAR", 12); // Reveal a Mystery Enchant Book or better
        TYPE_TO_STEP.put("AH_BUY", 13);                    // Buy a single item from /market (confirmed)
        TYPE_TO_STEP.put("AH_SELL", 14);                   // Sell a single item on /market
        TYPE_TO_STEP.put("JACKPOT_BUY", 15);               // Buy Jackpot Tickets
        TYPE_TO_STEP.put("CF_WIN", 16);                    // Win from /coinflip
        TYPE_TO_STEP.put("CF_LOSE", 16);                   // /coinflip (lose variant)

        // --- Wormhole / enchanter (steps 17–24) ---
        TYPE_TO_STEP.put("ENCHANTER_REPAIR_GEAR", 17);     // Repair gear in the Wormhole
        TYPE_TO_STEP.put("ENCHANT_DUST", 18);              // Use Enchant Dust in the Wormhole
        TYPE_TO_STEP.put("ENCHANTER_ENCHANT_PICKAXE", 19); // Successfully enchant a pickaxe
        TYPE_TO_STEP.put("ENCHANTER_LEVELUP_PICKAXE", 19); // (same step, observed in NBT)
        TYPE_TO_STEP.put("ENCHANTER_FAIL_PICKAXE", 20);    // Fail a pickaxe enchant
        TYPE_TO_STEP.put("TELEPORT_KOTH", 21);             // Teleport to a Koth
        TYPE_TO_STEP.put("LOCATION_VISIT_ZONE", 22);       // Go to the zone/mine
        TYPE_TO_STEP.put("ENCHANTER_ENCHANT_GEAR", 23);    // Successfully enchant gear
        TYPE_TO_STEP.put("ENCHANTER_FAIL_GEAR", 24);       // Fail a gear enchant
        TYPE_TO_STEP.put("ENCHANTER_DESTROY_GEAR", 24);    // (guess) destroyed-on-fail gear

        // --- Item use (steps 25–32) ---
        TYPE_TO_STEP.put("ITEM_USE_BLACKSCROLL", 25);      // Use a Black Scroll
        TYPE_TO_STEP.put("ITEM_USE_WHITESCROLL", 26);      // Apply a White Scroll
        TYPE_TO_STEP.put("ITEM_USE_ABSORBER", 27);         // Use an Absorber
        TYPE_TO_STEP.put("ITEM_USE_ENCHANT_PAGE", 28);     // Use an Enchantment Page
        TYPE_TO_STEP.put("ITEM_USE_CHARGEORB", 29);        // Apply a Charge Orb
        TYPE_TO_STEP.put("ITEM_USE_RANDOMIZATION", 30);    // Use a Randomization Scroll
        TYPE_TO_STEP.put("ITEM_USE_LORECRYSTAL", 31);      // Apply an Item Lore Crystal
        TYPE_TO_STEP.put("ITEM_USE_BOOSTER_XP", 32);       // Use an XP Booster

        // --- Misc (steps 33–38) ---
        TYPE_TO_STEP.put("ITEM_PICKAXE_PRESTIGE", 33);     // Prestige a pickaxe with a Prestige Token
        TYPE_TO_STEP.put("ENTITY_ATTACK_BANDIT", 34);      // Attack an Ore Bandit
        TYPE_TO_STEP.put("ENTITY_KILL_BANDIT", 34);        // Kill an Ore Bandit (variant)
        TYPE_TO_STEP.put("ENTITY_ATTACK_ESCAPEE_BANDIT", 35); // Attack an Escapee Bandit
        TYPE_TO_STEP.put("ENTITY_KILL_ESCAPEE_BANDIT", 35);   // Kill an Escapee Bandit (variant)
        TYPE_TO_STEP.put("TRADE_GIVE", 36);                // Trade items with another player
        TYPE_TO_STEP.put("TRADE_RECEIVE", 36);             // Trade items (receive variant)
        TYPE_TO_STEP.put("SHOP_BUY_COOKIE", 38);           // Buy a Cookie from the shop
    }

    /** Unmapped types already logged this session — avoids per-frame log spam. */
    private static final Set<String> LOGGED_UNMAPPED = new HashSet<>();

    private ClueScrollOverlay() {}

    /** Returns the mapped step number for a clue type, or null if unmapped. */
    public static Integer getStep(String type) {
        return TYPE_TO_STEP.get(type);
    }

    /** Draws the current clue step number on a clue scroll slot, if applicable. */
    public static void render(DrawContext ctx, int slotX, int slotY, TextRenderer font, ItemStack stack) {
        ClueData data = parse(stack);
        if (data == null) return;

        // Log any unmapped step types across ALL clues (done or not), once each.
        for (String type : data.allTypes) {
            if (!TYPE_TO_STEP.containsKey(type) && LOGGED_UNMAPPED.add(type)) {
                BetterPrisonsClient.LOGGER.warn("Unmapped clue scroll step type: {}", type);
            }
        }

        if (data.currentType == null) return;
        Integer step = TYPE_TO_STEP.get(data.currentType);
        String label = step != null ? Integer.toString(step) : "?";
        drawBigCentered(ctx, slotX, slotY, font, label);
    }

    /**
     * Adds a tooltip warning if the clue scroll has any unmapped step types, asking
     * the player to report them so the mapping can be completed.
     */
    public static void appendTooltip(ItemStack stack, List<Text> lines) {
        if (!BetterPrisonsClient.config.clueScrollUnmappedTooltipEnabled) return;
        ClueData data = parse(stack);
        if (data == null) return;

        List<String> unmapped = new ArrayList<>();
        for (String type : data.allTypes) {
            if (!TYPE_TO_STEP.containsKey(type) && !unmapped.contains(type)) {
                unmapped.add(type);
            }
        }
        if (unmapped.isEmpty()) return;

        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555)).withItalic(false);
        lines.add(Text.literal("[BP] Unmapped clue step! Please report to nishu06 on Discord:").setStyle(style));
        for (String type : unmapped) {
            lines.add(Text.literal("[BP]  - " + type).setStyle(style));
        }
    }

    private static void drawBigCentered(DrawContext ctx, int slotX, int slotY, TextRenderer font, String label) {
        int rawW = font.getWidth(label);
        int rawH = font.fontHeight;
        // Fixed scale sized for the widest 2-digit case, so 1- and 2-digit numbers
        // render at the same size (and 2-digit numbers still fit the 16px slot).
        float scale = 14f / Math.max(font.getWidth("00"), rawH);

        int color = 0xFF000000 | (BetterPrisonsClient.config.clueScrollNumberColor & 0xFFFFFF);

        Matrix3x2fStack matrices = ctx.getMatrices();
        matrices.pushMatrix();
        matrices.translate(slotX + 8f, slotY + 8f);
        matrices.scale(scale, scale);
        Text text = Text.literal(label).styled(s -> s.withBold(true));
        ctx.drawText(font, text, -rawW / 2, -rawH / 2, color, true);
        matrices.popMatrix();
    }

    /** Parsed clue scroll data: the current (furthest) clue type + every clue type. */
    private static final class ClueData {
        final String currentType;
        final List<String> allTypes;
        ClueData(String currentType, List<String> allTypes) {
            this.currentType = currentType;
            this.allTypes = allTypes;
        }
    }

    /** Parses a clue scroll's data, or returns null if the stack isn't a clue scroll. */
    private static ClueData parse(ItemStack stack) {
        try {
            if (stack == null || stack.isEmpty()) return null;
            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (customData == null) return null;
            NbtCompound bukkit = customData.copyNbt().getCompound("PublicBukkitValues").orElse(null);
            if (bukkit == null || bukkit.isEmpty()) return null;
            if (!"clue_scroll".equals(bukkit.getString("cosmicprisons:custom_item_id").orElse(""))) return null;

            String json = bukkit.getString("cosmicprisons:clue_scroll_data").orElse("");
            if (json.isEmpty()) return null;

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray clues = root.getAsJsonArray("clues");
            if (clues == null || clues.isEmpty()) return null;

            int idx = root.has("currentClueIndex") ? root.get("currentClueIndex").getAsInt() : 0;
            if (idx < 0 || idx >= clues.size()) idx = clues.size() - 1;

            List<String> allTypes = new ArrayList<>();
            for (int i = 0; i < clues.size(); i++) {
                JsonObject c = clues.get(i).getAsJsonObject();
                if (c.has("type")) allTypes.add(c.get("type").getAsString());
            }
            String currentType = clues.get(idx).getAsJsonObject().get("type").getAsString();
            return new ClueData(currentType, allTypes);
        } catch (Exception e) {
            return null;
        }
    }
}
