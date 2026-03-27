package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
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

    // Whitelist of valid satchel names
    private static final List<String> VALID_SATCHELS = List.of(
        "Coal Ore Satchel",
        "Coal Satchel",
        "Iron Ore Satchel",
        "Iron Satchel",
        "Lapis Ore Satchel",
        "Lapis Satchel",
        "Redstone Ore Satchel",
        "Redstone Satchel",
        "Gold Ore Satchel",
        "Gold Satchel",
        "Diamond Ore Satchel",
        "Diamond Satchel",
        "Emerald Ore Satchel",
        "Emerald Satchel"
    );

    // Map Minecraft items to satchel type names (for renamed satchels)
    private static final Map<Item, String> ITEM_TO_SATCHEL = new HashMap<>();
    static {
        ITEM_TO_SATCHEL.put(Items.COAL_ORE, "Coal Ore Satchel");
        ITEM_TO_SATCHEL.put(Items.COAL, "Coal Satchel");
        ITEM_TO_SATCHEL.put(Items.IRON_ORE, "Iron Ore Satchel");
        ITEM_TO_SATCHEL.put(Items.IRON_INGOT, "Iron Satchel");
        ITEM_TO_SATCHEL.put(Items.LAPIS_ORE, "Lapis Ore Satchel");
        ITEM_TO_SATCHEL.put(Items.LAPIS_LAZULI, "Lapis Satchel");
        ITEM_TO_SATCHEL.put(Items.REDSTONE_ORE, "Redstone Ore Satchel");
        ITEM_TO_SATCHEL.put(Items.REDSTONE, "Redstone Satchel");
        ITEM_TO_SATCHEL.put(Items.GOLD_ORE, "Gold Ore Satchel");
        ITEM_TO_SATCHEL.put(Items.GOLD_INGOT, "Gold Satchel");
        ITEM_TO_SATCHEL.put(Items.DIAMOND_ORE, "Diamond Ore Satchel");
        ITEM_TO_SATCHEL.put(Items.DIAMOND, "Diamond Satchel");
        ITEM_TO_SATCHEL.put(Items.EMERALD_ORE, "Emerald Ore Satchel");
        ITEM_TO_SATCHEL.put(Items.EMERALD, "Emerald Satchel");
    }

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

    private boolean isSatchel(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // Get the display name and parse it (remove capacity part)
        String displayName = stack.getName().getString();
        String satchelName = displayName.split("\\(")[0].trim();

        // Check if the parsed name is in the whitelist
        for (String validName : VALID_SATCHELS) {
            if (satchelName.startsWith(validName)) {
                return true;
            }
        }

        // Fallback: check if item type matches a known satchel item AND name contains capacity pattern
        if (ITEM_TO_SATCHEL.containsKey(stack.getItem()) && displayName.matches(".*\\(\\d[\\d,]*\\s*/\\s*\\d[\\d,]*\\).*")) {
            return true;
        }

        return false;
    }

    /**
     * Parses the style from the first sibling in the toString() representation of a Text object.
     * Example input: "empty[siblings=[literal{Redstone Ore Satchel }[style={color=red,bold,!italic}], ...]]"
     * Returns a Style object with the parsed formatting.
     */
    private Style parseStyleFromToString(String textToString) {
        Style style = Style.EMPTY;

        try {
            // Pattern to match the first [style={...}] block
            Pattern stylePattern = Pattern.compile("\\[style=\\{([^}]+)\\}\\]");
            Matcher matcher = stylePattern.matcher(textToString);

            if (matcher.find()) {
                String styleString = matcher.group(1);

                // Split by comma to get individual style attributes
                String[] attributes = styleString.split(",");

                for (String attr : attributes) {
                    attr = attr.trim();

                    // Handle color
                    if (attr.startsWith("color=")) {
                        String colorName = attr.substring(6); // Remove "color="
                        Formatting color = parseColor(colorName);
                        if (color != null) {
                            style = style.withColor(color);
                        }
                    }
                    // Handle bold
                    else if (attr.equals("bold")) {
                        style = style.withBold(true);
                    } else if (attr.equals("!bold")) {
                        style = style.withBold(false);
                    }
                    // Handle italic
                    else if (attr.equals("italic")) {
                        style = style.withItalic(true);
                    } else if (attr.equals("!italic")) {
                        style = style.withItalic(false);
                    }
                    // Handle underline
                    else if (attr.equals("underlined")) {
                        style = style.withUnderline(true);
                    } else if (attr.equals("!underlined")) {
                        style = style.withUnderline(false);
                    }
                    // Handle strikethrough
                    else if (attr.equals("strikethrough")) {
                        style = style.withStrikethrough(true);
                    } else if (attr.equals("!strikethrough")) {
                        style = style.withStrikethrough(false);
                    }
                    // Handle obfuscated
                    else if (attr.equals("obfuscated")) {
                        style = style.withObfuscated(true);
                    } else if (attr.equals("!obfuscated")) {
                        style = style.withObfuscated(false);
                    }
                }
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Error parsing style from toString: " + e.getMessage());
        }

        return style;
    }

    /**
     * Parses a color name to a Formatting enum value.
     */
    private Formatting parseColor(String colorName) {
        try {
            // Try to match Minecraft formatting colors
            switch (colorName.toLowerCase()) {
                case "black": return Formatting.BLACK;
                case "dark_blue": return Formatting.DARK_BLUE;
                case "dark_green": return Formatting.DARK_GREEN;
                case "dark_aqua": return Formatting.DARK_AQUA;
                case "dark_red": return Formatting.DARK_RED;
                case "dark_purple": return Formatting.DARK_PURPLE;
                case "gold": return Formatting.GOLD;
                case "gray": return Formatting.GRAY;
                case "dark_gray": return Formatting.DARK_GRAY;
                case "blue": return Formatting.BLUE;
                case "green": return Formatting.GREEN;
                case "aqua": return Formatting.AQUA;
                case "red": return Formatting.RED;
                case "light_purple": return Formatting.LIGHT_PURPLE;
                case "yellow": return Formatting.YELLOW;
                case "white": return Formatting.WHITE;
                default:
                    BetterPrisonsClient.LOGGER.warn("Unknown color: " + colorName);
                    return null;
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Error parsing color: " + colorName);
            return null;
        }
    }

    private SatchelInfo parseSatchel(ItemStack stack) {
        SatchelInfo info = new SatchelInfo();

        // Get the full display name with color/formatting
        Text fullName = stack.getName();
        String fullNameString = fullName.getString();

        // Parse the trimmed name (remove capacity part)
        String trimmedName = fullNameString.split("\\(")[0].trim();

        // Check if the name matches a known satchel type
        String matchedType = null;
        for (String validName : VALID_SATCHELS) {
            if (trimmedName.startsWith(validName)) {
                matchedType = validName;
                break;
            }
        }

        // If name doesn't match, fall back to item-based detection
        if (matchedType == null) {
            matchedType = ITEM_TO_SATCHEL.get(stack.getItem());
        }

        // If name matched whitelist, use parsed style from the Text; otherwise use fallback color
        boolean nameMatched = matchedType != null && trimmedName.startsWith(matchedType);
        info.name = matchedType != null ? matchedType : trimmedName;

        try {
            if (nameMatched) {
                // Standard name — parse style from the custom name Text
                String textToString = fullName.toString();
                Style parsedStyle = parseStyleFromToString(textToString);
                info.displayName = Text.literal(info.name).setStyle(parsedStyle);
            } else {
                // Renamed satchel — use known color for the satchel type
                Formatting color = SATCHEL_COLORS.get(info.name);
                Style fallbackStyle = Style.EMPTY.withBold(true);
                if (color != null) {
                    fallbackStyle = fallbackStyle.withColor(color);
                }
                info.displayName = Text.literal(info.name).setStyle(fallbackStyle);
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Failed to parse satchel style, using default: " + e.getMessage());
            info.displayName = Text.literal(info.name);
        }

        info.itemStack = stack.copy(); // Store the ItemStack for icon rendering

        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {

            try {
                List<Text> lines = lore.lines();
                String line = lines.get(9).getString();
                info.current = Integer.parseInt(line.split("/")[0].replace("\\(", "")
                        .strip().replaceAll(",", ""));
                info.max = Integer.parseInt(line.split("/")[1].replace("\\)", "")
                        .strip().replaceAll(",", ""));
            } catch (NumberFormatException e) {
                BetterPrisonsClient.LOGGER.warn("Unable to read satchel values");
            }
        }

        // Ensure max is at least 1 to avoid division by zero
        if (info.max <= 0) {
            info.max = 1;
        }

        return info;
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
