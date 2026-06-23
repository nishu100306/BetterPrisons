package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SatchelHud extends BaseHud {
    public List<SatchelInfo> foundSatchels = new ArrayList<>();
    private int lastInventoryHash = 0;
    private boolean scanning = false;

    // Default colors for each satchel type (used when custom name strips the original style)
    private static final Map<String, Formatting> SATCHEL_COLORS = new HashMap<>();
    static {
        SATCHEL_COLORS.put("Coal Ore Satchel", Formatting.DARK_GRAY);
        SATCHEL_COLORS.put("Coal Satchel", Formatting.DARK_GRAY);
        SATCHEL_COLORS.put("Iron Ore Satchel", Formatting.GRAY);
        SATCHEL_COLORS.put("Iron Satchel", Formatting.GRAY);
        SATCHEL_COLORS.put("Lapis Ore Satchel", Formatting.BLUE);
        SATCHEL_COLORS.put("Lapis Satchel", Formatting.BLUE);
        SATCHEL_COLORS.put("Redstone Ore Satchel", Formatting.RED);
        SATCHEL_COLORS.put("Redstone Satchel", Formatting.RED);
        SATCHEL_COLORS.put("Gold Ore Satchel", Formatting.GOLD);
        SATCHEL_COLORS.put("Gold Satchel", Formatting.GOLD);
        SATCHEL_COLORS.put("Diamond Ore Satchel", Formatting.AQUA);
        SATCHEL_COLORS.put("Diamond Satchel", Formatting.AQUA);
        SATCHEL_COLORS.put("Emerald Ore Satchel", Formatting.GREEN);
        SATCHEL_COLORS.put("Emerald Satchel", Formatting.GREEN);
    }

    public SatchelHud() {
        super("satchel");
    }

    int count = 0;
    @Override
    public void tick(MinecraftClient client) {
        count++;
        if (!enabled || scanning) return;
        if (client.player == null) return;

        if (count % 5 == 0) {
            rescan(client.player.getInventory());
        }
        /*
        // Only rescan when inventory changes
        int hash = computeInventoryHash(client.player.getInventory());
        if (hash != lastInventoryHash) {
            lastInventoryHash = hash;
            rescan(client.player.getInventory());
        }
        */
    }

    private void rescan(PlayerInventory inv) {
        scanning = true;
        foundSatchels.clear();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (isSatchel(stack)) {
                foundSatchels.add(parseSatchel(stack));
            }
        }
        boolean shouldCombine = BetterPrisonsClient.config.combineSimilarSatchels;
        if (shouldCombine) {
            combineSimilarSatchels();
        }
        scanning = false;
    }

    private void combineSimilarSatchels() {
        try {
            List<SatchelInfo> combined = new ArrayList<>();

            while (!foundSatchels.isEmpty()) {
                SatchelInfo satchel = foundSatchels.remove(0);
                combined.add(satchel);
                for (int j = 0; j < foundSatchels.size(); j++) {
                    SatchelInfo other = foundSatchels.get(j);
                    if (satchel.name.equals(other.name)) {
                        satchel.current += other.current;
                        satchel.max += other.max;
                        foundSatchels.remove(j);
                        j--;
                    }
                }
            }
            this.foundSatchels = combined;
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Error combining similar satchels: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    /**
     * Matches a capacity line like "(0 / 345,600 Ores)" or "(13 / 320 Drops)".
     * The trailing unit word is required so the energy line "(0 / 50,000)" is ignored.
     * group(1) = current, group(2) = max.
     */
    private static final Pattern CAPACITY_PATTERN =
        Pattern.compile("\\((\\d[\\d,]*)\\s*/\\s*(\\d[\\d,]*)\\s*[A-Za-z]+\\)");

    /** Returns the cosmicprisons PublicBukkitValues compound, or null. */
    private NbtCompound getBukkit(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return null;
        NbtCompound bukkit = customData.copyNbt().getCompound("PublicBukkitValues").orElse(null);
        return (bukkit == null || bukkit.isEmpty()) ? null : bukkit;
    }

    private boolean isSatchel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NbtCompound bukkit = getBukkit(stack);
        if (bukkit == null) return false;
        // Detect via NBT — robust to renames and extra lore lines.
        if (bukkit.getString("cosmicprisons:custom_item_id").orElse("").endsWith("_satchel")) return true;
        return !bukkit.getString("cosmicprisons:satchel_ore").orElse("").isEmpty();
    }

    private SatchelInfo parseSatchel(ItemStack stack) {
        SatchelInfo info = new SatchelInfo();
        info.itemStack = stack.copy(); // Store the ItemStack for icon rendering

        NbtCompound bukkit = getBukkit(stack);
        String ore = bukkit != null ? bukkit.getString("cosmicprisons:satchel_ore").orElse("") : "";
        String customId = bukkit != null ? bukkit.getString("cosmicprisons:custom_item_id").orElse("") : "";
        // Renamed satchels carry a custom_display_name; default-named ones don't.
        boolean renamed = bukkit != null && !bukkit.getString("cosmicprisons:custom_display_name").orElse("").isEmpty();

        // Capacity "(X / Y <unit>)" — in the lore for ore satchels, in the display
        // name for drop satchels (clue scroll / contraband / shard).
        long[] cap = parseCapacity(stack);

        // Name resolution:
        //  1. Ore satchels: the "Automatically collects <X> while mining" lore line gives the
        //     exact collected-block name and isn't affected by renames → "<X> Satchel".
        //  2. Otherwise (non-ore, or that line missing): use the exact display name when not
        //     renamed, else reconstruct from the custom_item_id.
        String collected = ore.isEmpty() ? null : parseCollectedBlock(stack);
        if (collected != null) {
            info.name = collected + " Satchel";
        } else {
            info.name = renamed ? prettyName(customId) : stripCapacitySuffix(stack.getName().getString());
        }

        String colorKey;
        if (!ore.isEmpty()) {
            // Ore satchel — current count from NBT. Color is keyed by the base ore type,
            // ignoring any variant prefix.
            boolean refined = bukkit.getBoolean("cosmicprisons:satchel_refined").orElse(false);
            long count = bukkit.getInt("cosmicprisons:satchel_count").orElse(0);
            String oreName = ore.charAt(0) + ore.substring(1).toLowerCase();
            colorKey = refined ? oreName + " Satchel" : oreName + " Ore Satchel";
            info.current = count;
        } else {
            // Other satchel types (e.g. clue scroll satchel) — fill from the capacity line.
            colorKey = info.name;
            info.current = cap != null ? cap[0] : 0;
        }

        info.max = cap != null ? cap[1] : 1;
        if (info.max <= 0) info.max = 1;

        // Style from the known satchel color (display name comes from NBT, not the
        // possibly-renamed custom name). Non-ore satchels use the default style.
        Formatting color = SATCHEL_COLORS.get(colorKey);
        Style style = Style.EMPTY.withBold(true);
        if (color != null) style = style.withColor(color);
        info.displayName = Text.literal(info.name).setStyle(style);

        return info;
    }

    /**
     * Parses {current, max} from a "(X / Y <unit>)" capacity string. Checks the
     * display name first (drop satchels) then the lore (ore satchels). Null if absent.
     */
    private long[] parseCapacity(ItemStack stack) {
        long[] fromName = matchCapacity(stack.getName().getString());
        if (fromName != null) return fromName;

        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                long[] r = matchCapacity(line.getString());
                if (r != null) return r;
            }
        }
        return null;
    }

    private long[] matchCapacity(String s) {
        Matcher m = CAPACITY_PATTERN.matcher(s);
        if (m.find()) {
            try {
                long cur = Long.parseLong(m.group(1).replace(",", ""));
                long max = Long.parseLong(m.group(2).replace(",", ""));
                return new long[]{cur, max};
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Extracts the collected block name from the "Automatically collects <X> while mining"
     * lore line (rename-proof). Returns null if the line isn't present.
     */
    private String parseCollectedBlock(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return null;
        String marker = "Automatically collects ";
        for (Text line : lore.lines()) {
            String s = line.getString();
            int idx = s.indexOf(marker);
            if (idx >= 0) {
                String rest = s.substring(idx + marker.length()).replace("while mining.", "").trim();
                if (!rest.isEmpty()) return rest;
            }
        }
        return null;
    }

    /** Removes the trailing " (X / Y unit)" capacity part from a satchel's display name. */
    private String stripCapacitySuffix(String name) {
        int idx = name.indexOf('(');
        return idx >= 0 ? name.substring(0, idx).trim() : name.trim();
    }

    /** Turns a custom item id like "clue_scroll_satchel" into "Clue Scroll Satchel". */
    private String prettyName(String customId) {
        if (customId == null || customId.isEmpty()) return "Satchel";
        StringBuilder sb = new StringBuilder();
        for (String part : customId.split("_")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.length() == 0 ? "Satchel" : sb.toString();
    }

    private int computeInventoryHash(PlayerInventory inv) {
        // Simple hash to detect changes
        return inv.hashCode();
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        this.scale = BetterPrisonsClient.config.satchelHudScale;
        this.scale = this.scale / 100.0f;

        boolean showTitle = BetterPrisonsClient.config.showSatchelHudTitle;
        boolean hasContent = !foundSatchels.isEmpty();

        // Don't render if HUD is disabled and there's nothing to show
        if (!enabled || (!showTitle && !hasContent)) return;

        // Calculate title dimensions
        int titleHeight = 0;
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Satchel HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
            titleHeight = scaled(12); // Text height + spacing
        }

        // Calculate maximum text width (considering both name and fill text)
        int maxTextWidth = titleWidth;
        if (hasContent) {
            for (SatchelInfo satchel : foundSatchels) {
                // Check name width
                if (satchel.displayName != null) {
                    int nameWidth = client.textRenderer.getWidth(satchel.displayName);
                    nameWidth = (int)(nameWidth * scale); // Apply scaling
                    maxTextWidth = Math.max(maxTextWidth, nameWidth);
                }

                // Check fill text width
                String fillText;
                if (BetterPrisonsClient.config.satchelShowPercentage) {
                    // Show as percentage
                    double percentage = (satchel.current * 100.0) / satchel.max;
                    fillText = String.format("%.1f%%", percentage);
                } else {
                    // Show as actual values
                    fillText = formatNumber(satchel.current) + " / " + formatNumber(satchel.max);
                }
                int fillTextWidth = client.textRenderer.getWidth(Text.literal(fillText));
                fillTextWidth = (int)(fillTextWidth * scale); // Apply scaling
                maxTextWidth = Math.max(maxTextWidth, fillTextWidth);
            }
        }

        // Draw background with custom styling (with scaling applied)
        // Layout: [Icon 16px] [spacing 4px] [text content]
        // Each satchel: 24px height (icon + small padding)
        int bgWidth = hasContent ? (scaled(16 + 4) + maxTextWidth) : maxTextWidth; // icon + spacing + text
        int contentHeight = hasContent ? foundSatchels.size() * scaled(23) : 0;
        int bgHeight = titleHeight + contentHeight;

        // Combine RGB color with opacity to create ARGB
        int bgColor = (BetterPrisonsClient.config.satchelBgOpacity << 24) | (BetterPrisonsClient.config.satchelBgColor & 0xFFFFFF);
        int borderColor = (BetterPrisonsClient.config.satchelBorderOpacity << 24) | (BetterPrisonsClient.config.satchelBorderColor & 0xFFFFFF);
        int thickness = scaled(BetterPrisonsClient.config.satchelBorderThickness);
        int padding = 4;
        if (scale < 1) padding = scaled(padding);

        // Draw background
        ctx.fill(x - padding, y - padding, x + bgWidth + padding, y + bgHeight + padding, bgColor);

        // Draw border outside the background (no overlap)
        // Top border
        ctx.fill(x - padding, y - padding - thickness, x + bgWidth + padding, y - padding, borderColor);
        // Bottom border
        ctx.fill(x - padding, y + bgHeight + padding, x + bgWidth + padding, y + bgHeight + padding + thickness, borderColor);
        // Left border
        ctx.fill(x - padding - thickness, y - padding - thickness, x - padding, y + bgHeight + padding + thickness, borderColor);
        // Right border
        ctx.fill(x + bgWidth + padding, y - padding - thickness, x + bgWidth + padding + thickness, y + bgHeight + padding + thickness, borderColor);

        Matrix3x2fStack matrices = ctx.getMatrices();
        int yOffset = 0;

        // Draw title if enabled
        if (showTitle) {
            Text titleText = Text.literal("Satchel HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            int titleColor = 0xFF000000 | BetterPrisonsClient.config.satchelHudTitleColor;
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x/scale, y/scale);
            ctx.drawTextWithShadow(client.textRenderer, titleText, 0, 0, titleColor);
            matrices.popMatrix();


            yOffset += titleHeight;
        }

        // Draw satchel content
        if (hasContent) {
            int rowHeight = scaled(24);
            int iconSpacing = scaled(20);
            int textLineSpacing = scaled(10);
            int iconYOffset = 0;

            for (SatchelInfo satchel : foundSatchels) {
                // Draw satchel item icon on the left
                if (satchel.itemStack != null) {
                    matrices.pushMatrix();
                    matrices.scale(scale);
                    matrices.translate(x/scale, (y + yOffset + iconYOffset)/scale);
                    ctx.drawItem(satchel.itemStack, 0, 0);
                    matrices.popMatrix();
                }

                // Draw satchel name next to the icon (first line)
                if (satchel.displayName != null) {
                    matrices.pushMatrix();
                    matrices.scale(scale);
                    matrices.translate((x + iconSpacing)/scale, (y + yOffset)/scale);
                    ctx.drawText(client.textRenderer, satchel.displayName, 0, 0, 0xFFFFFFFF, true);
                    matrices.popMatrix();
                }

                // Draw capacity text directly under the name (second line)
                // Calculate percentage for color determination
                double percentage = (satchel.current * 100.0) / satchel.max;

                String fillText;
                if (BetterPrisonsClient.config.satchelShowPercentage) {
                    // Show as percentage
                    fillText = String.format("%.1f%%", percentage);
                } else {
                    // Show as actual values
                    fillText = formatNumber(satchel.current) + " / " + formatNumber(satchel.max);
                }

                // Determine color based on capacity threshold
                int capacityColor;
                if (percentage < 20.0) {
                    capacityColor = 0xFF000000 | BetterPrisonsClient.config.satchelColorUnder20;
                } else if (percentage < 60.0) {
                    capacityColor = 0xFF000000 | BetterPrisonsClient.config.satchelColor20to60;
                } else if (percentage < 95.0) {
                    capacityColor = 0xFF000000 | BetterPrisonsClient.config.satchelColor60to95;
                } else {
                    capacityColor = 0xFF000000 | BetterPrisonsClient.config.satchelColor95Plus;
                }

                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate((x + iconSpacing)/scale, (y + yOffset + textLineSpacing)/scale);
                ctx.drawTextWithShadow(client.textRenderer, Text.literal(fillText), 0, 0, capacityColor);
                matrices.popMatrix();

                yOffset += rowHeight; // Space for next satchel
            }
        }
    }

    @Override
    public int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return scaled(140);

        boolean showTitle = BetterPrisonsClient.config.showSatchelHudTitle;
        boolean hasContent = !foundSatchels.isEmpty();

        // Calculate title width
        int titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Satchel HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int)(client.textRenderer.getWidth(titleText) * scale);
        }

        // Calculate maximum text width
        int maxTextWidth = titleWidth;
        if (hasContent) {
            for (SatchelInfo satchel : foundSatchels) {
                // Check name width
                if (satchel.displayName != null) {
                    int nameWidth = (int)(client.textRenderer.getWidth(satchel.displayName) * scale);
                    maxTextWidth = Math.max(maxTextWidth, nameWidth);
                }

                // Check fill text width
                String fillText;
                if (BetterPrisonsClient.config.satchelShowPercentage) {
                    double percentage = (satchel.current * 100.0) / satchel.max;
                    fillText = String.format("%.1f%%", percentage);
                } else {
                    fillText = formatNumber(satchel.current) + " / " + formatNumber(satchel.max);
                }
                int fillTextWidth = (int)(client.textRenderer.getWidth(Text.literal(fillText)) * scale);
                maxTextWidth = Math.max(maxTextWidth, fillTextWidth);
            }
        }

        int bgWidth = hasContent ? (scaled(16 + 4) + maxTextWidth) : maxTextWidth;

        // Add padding (same logic as render method)
        int padding = 4;
        if (scale < 1) padding = scaled(padding);

        return bgWidth + (padding * 2); // padding on both sides
    }

    @Override
    public int getHeight() {
        int titleHeight = BetterPrisonsClient.config.showSatchelHudTitle ? scaled(10) : 0;
        int contentHeight = foundSatchels.size() * scaled(24);
        return titleHeight + contentHeight;
    }

    private String formatNumber(long num) {
        // Format as "1,234" or "1.2M" etc.
        return String.format("%,d", num);
    }

    public static class SatchelInfo {
        public String name = "";
        public Text displayName; // Stores the trimmed name with original color/formatting
        public ItemStack itemStack;
        public long current = 0;
        public long max = 1;
        public float getFillPercent() { return (float) current / max; }
    }
}
