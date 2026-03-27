package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventsHud extends BaseHud {

    // --- Meteors ---
    private final List<MeteorInfo> activeMeteors = new ArrayList<>();
    private static final long METEOR_TIMEOUT_MS = 20 * 60 * 1000L;
    private static final Pattern COORDS_PATTERN = Pattern.compile("(-?\\d+)x,\\s*(-?\\d+)y,\\s*(-?\\d+)z");

    // --- Merchants ---
    private final List<MerchantInfo> activeMerchants = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    public enum MeteorType {
        NATURAL, SUMMONED
    }

    public enum MerchantType {
        COAL, IRON, LAPIS, REDSTONE, GOLD, DIAMOND, EMERALD, UNKNOWN;

        public static MerchantType fromString(String tierName) {
            switch (tierName.toUpperCase()) {
                case "COAL":     return COAL;
                case "IRON":     return IRON;
                case "LAPIS":    return LAPIS;
                case "REDSTONE": return REDSTONE;
                case "GOLD":     return GOLD;
                case "DIAMOND":  return DIAMOND;
                case "EMERALD":  return EMERALD;
                default:         return UNKNOWN;
            }
        }

        public String getDefaultIconId() {
            switch (this) {
                case COAL:     return "coal";
                case IRON:     return "iron_ingot";
                case LAPIS:    return "lapis_lazuli";
                case REDSTONE: return "redstone";
                case GOLD:     return "gold_ingot";
                case DIAMOND:  return "diamond";
                case EMERALD:  return "emerald";
                default:       return "nether_quartz_ore";
            }
        }

        public String getDisplayName() {
            String name = this.name().charAt(0) + this.name().substring(1).toLowerCase();
            return name + " Ore Merchant";
        }

        public boolean isEnabled(Config config) {
            switch (this) {
                case COAL:     return config.coalMerchantEnabled;
                case IRON:     return config.ironMerchantEnabled;
                case LAPIS:    return config.lapisMerchantEnabled;
                case REDSTONE: return config.redstoneMerchantEnabled;
                case GOLD:     return config.goldMerchantEnabled;
                case DIAMOND:  return config.diamondMerchantEnabled;
                case EMERALD:  return config.emeraldMerchantEnabled;
                default:       return true;
            }
        }

        public int getHeadingColor(Config config) {
            switch (this) {
                case COAL:     return config.coalMerchantHeadingColor;
                case IRON:     return config.ironMerchantHeadingColor;
                case LAPIS:    return config.lapisMerchantHeadingColor;
                case REDSTONE: return config.redstoneMerchantHeadingColor;
                case GOLD:     return config.goldMerchantHeadingColor;
                case DIAMOND:  return config.diamondMerchantHeadingColor;
                case EMERALD:  return config.emeraldMerchantHeadingColor;
                default:       return 0xFFFFFF;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public EventsHud() {
        super("events");
    }

    // -------------------------------------------------------------------------
    // Meteor API
    // -------------------------------------------------------------------------

    /**
     * Called when a two-line meteor announcement is detected.
     * coordsLine is the second line containing the coordinates.
     */
    public void onMeteorFalling(String coordsLine, MeteorType type) {
        Matcher matcher = COORDS_PATTERN.matcher(coordsLine);
        if (matcher.find()) {
            try {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                for (MeteorInfo m : activeMeteors) {
                    if (m.x == x && m.y == y && m.z == z) return;
                }
                activeMeteors.add(new MeteorInfo(x, y, z, System.currentTimeMillis(), createMeteorIcon(), type));
                BetterPrisonsClient.LOGGER.info("Meteor detected at: {}, {}, {} (type: {})", x, y, z, type);
                // Mirror to WaypointManager so it appears on the Waypoints screen
                String name = (type == MeteorType.NATURAL) ? "Natural Meteor" : "Summoned Meteor";
                int color = (type == MeteorType.NATURAL)
                    ? BetterPrisonsClient.config.eventsNaturalHeadingColor
                    : BetterPrisonsClient.config.eventsSummonedHeadingColor;
                String eventKey = (type == MeteorType.NATURAL) ? "METEOR_NATURAL" : "METEOR_SUMMONED";
                BetterPrisonsClient.waypointManager.addEventWaypoint(x, y, z, color, name, eventKey);
            } catch (NumberFormatException e) {
                BetterPrisonsClient.LOGGER.warn("Failed to parse meteor coordinates: {}", coordsLine);
            }
        }
    }

    public void onMeteorCrashed(String coordsLine) {
        Matcher matcher = COORDS_PATTERN.matcher(coordsLine);
        if (matcher.find()) {
            try {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                for (MeteorInfo m : activeMeteors) {
                    if (m.x == x && m.y == y && m.z == z && m.crashTime == null) {
                        m.crashTime = System.currentTimeMillis();
                        BetterPrisonsClient.LOGGER.info("Meteor marked as crashed at: {}, {}, {}", x, y, z);
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                BetterPrisonsClient.LOGGER.warn("Failed to parse meteor crash coordinates: {}", coordsLine);
            }
        }
    }

    private ItemStack createMeteorIcon() {
        ItemStack stack;
        try {
            String itemId = BetterPrisonsClient.config.eventsIconItemId;
            if (!itemId.contains(":")) itemId = "minecraft:" + itemId;
            Identifier identifier = Identifier.tryParse(itemId);
            if (identifier != null) {
                Item item = Registries.ITEM.get(identifier);
                stack = item != null
                    ? new ItemStack(item)
                    : new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "nether_quartz_ore")));
            } else {
                stack = new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "nether_quartz_ore")));
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Failed to create meteor icon: {}", e.getMessage());
            stack = new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "nether_quartz_ore")));
        }
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    // -------------------------------------------------------------------------
    // Merchant API
    // -------------------------------------------------------------------------

    /**
     * Called when a merchant spawn message is detected.
     * tierName is the ore tier word (e.g. "Diamond", "Coal").
     */
    public void onMerchantSpawned(String tierName, int x, int y, int z) {
        MerchantType type = MerchantType.fromString(tierName);
        for (MerchantInfo m : activeMerchants) {
            if (m.x == x && m.y == y && m.z == z) return; // duplicate position
        }
        ItemStack icon = createMerchantIcon(type);
        activeMerchants.add(new MerchantInfo(x, y, z, System.currentTimeMillis(), icon, type));
        BetterPrisonsClient.LOGGER.info("Merchant detected: {} at {}, {}, {}", type, x, y, z);
        // Mirror to WaypointManager so it appears on the Waypoints screen
        String name = type.getDisplayName();
        int color = type.getHeadingColor(BetterPrisonsClient.config);
        String eventKey = "MERCHANT_" + type.name();
        BetterPrisonsClient.waypointManager.addEventWaypoint(x, y, z, color, name, eventKey);
    }

    /**
     * Called when a merchant-slain message is detected.
     * Marks the merchant as slain so it lingers briefly before being removed.
     */
    public void onMerchantSlain(String tierName, int x, int y, int z) {
        for (MerchantInfo m : activeMerchants) {
            if (m.x == x && m.y == y && m.z == z && m.slainTime == null) {
                m.slainTime = System.currentTimeMillis();
                BetterPrisonsClient.LOGGER.info("Merchant marked as slain: {} at {}, {}, {}", tierName, x, y, z);
                return;
            }
        }
        BetterPrisonsClient.LOGGER.warn("Merchant slain but no matching entry found: {} at {}, {}, {}", tierName, x, y, z);
    }

    private ItemStack createMerchantIcon(MerchantType type) {
        try {
            Identifier id = Identifier.of("minecraft", type.getDefaultIconId());
            Item item = Registries.ITEM.get(id);
            if (item != null) return new ItemStack(item);
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Failed to create merchant icon for {}: {}", type, e.getMessage());
        }
        return new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "coal")));
    }

    /** Returns active meteors (used by WaypointRenderer). */
    public List<MeteorInfo> getActiveMeteors() {
        return activeMeteors;
    }

    /** Returns all active merchants (including hidden tiers). */
    public List<MerchantInfo> getActiveMerchants() {
        return activeMerchants;
    }

    /** Returns visible merchants for waypoint rendering — excludes slain merchants. */
    public List<MerchantInfo> getVisibleMerchantsForWaypoints() {
        Config config = BetterPrisonsClient.config;
        if (!config.merchantsEnabled) return new ArrayList<>();
        List<MerchantInfo> result = new ArrayList<>();
        for (MerchantInfo m : activeMerchants) {
            if (m.slainTime == null && m.type.isEnabled(config)) result.add(m);
        }
        return result;
    }

    public void clearMeteors() {
        for (MeteorInfo m : activeMeteors) {
            BetterPrisonsClient.waypointManager.removeEventWaypoint(m.x, m.y, m.z);
        }
        activeMeteors.clear();
    }

    public void clearMerchants() {
        for (MerchantInfo m : activeMerchants) {
            BetterPrisonsClient.waypointManager.removeEventWaypoint(m.x, m.y, m.z);
        }
        activeMerchants.clear();
    }

    /** Returns only the merchants that should currently be rendered. */
    private List<MerchantInfo> getVisibleMerchants() {
        Config config = BetterPrisonsClient.config;
        if (!config.merchantsEnabled) return new ArrayList<>();
        List<MerchantInfo> result = new ArrayList<>();
        for (MerchantInfo m : activeMerchants) {
            if (m.type.isEnabled(config)) result.add(m);
        }
        return result;
    }

    /** Formats coordinates with optional distance suffix "(Xm)" based on player position. */
    private String coordsWithDist(int x, int y, int z, Vec3d playerPos, boolean showDist) {
        String base = String.format("%dx, %dy, %dz", x, y, z);
        if (!showDist || playerPos == null) return base;
        int dist = (int) playerPos.distanceTo(new Vec3d(x + 0.5, y, z + 0.5));
        return base + " (" + dist + "m)";
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        long now = System.currentTimeMillis();
        long crashedDisplayDurationMs = BetterPrisonsClient.config.eventsCrashedDisplayDuration * 1000L;
        long merchantTimeoutMs = BetterPrisonsClient.config.merchantTimeoutMinutes * 60 * 1000L;
        long merchantSlainDisplayMs = BetterPrisonsClient.config.merchantSlainDisplayDuration * 1000L;

        activeMeteors.removeIf(m -> {
            // User deleted from Waypoints screen
            if (!BetterPrisonsClient.waypointManager.hasEventWaypoint(m.x, m.y, m.z)) return true;
            // Natural expiry — also clean up the waypoint entry
            if (now - m.spawnTime > METEOR_TIMEOUT_MS) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(m.x, m.y, m.z);
                return true;
            }
            if (m.crashTime != null && now - m.crashTime > crashedDisplayDurationMs) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(m.x, m.y, m.z);
                return true;
            }
            return false;
        });

        activeMerchants.removeIf(m -> {
            // User deleted from Waypoints screen
            if (!BetterPrisonsClient.waypointManager.hasEventWaypoint(m.x, m.y, m.z)) return true;
            // Natural expiry — also clean up the waypoint entry
            if (m.slainTime != null && now - m.slainTime > merchantSlainDisplayMs) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(m.x, m.y, m.z);
                return true;
            }
            if (m.slainTime == null && now - m.spawnTime > merchantTimeoutMs) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(m.x, m.y, m.z);
                return true;
            }
            return false;
        });
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        this.scale = BetterPrisonsClient.config.eventsHudScale / 100.0f;

        Config config = BetterPrisonsClient.config;
        boolean showTitle = config.showEventsHudTitle;
        Vec3d playerPos = client.player != null
            ? new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ()) : null;
        List<MerchantInfo> visibleMerchants = getVisibleMerchants();
        boolean hasContent = !activeMeteors.isEmpty() || !visibleMerchants.isEmpty();

        if (!enabled || (!showTitle && !hasContent)) return;

        // --- Title dimensions ---
        int titleHeight = 0, titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Events HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int) (client.textRenderer.getWidth(titleText) * scale);
            titleHeight = scaled(12);
        }

        // --- Max width across all entries ---
        int maxTextWidth = titleWidth;
        for (MeteorInfo m : activeMeteors) {
            String heading = m.type == MeteorType.NATURAL ? "Natural Meteor" : "Summoned Meteor";
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(heading)) * scale));
            String coords = coordsWithDist(m.x, m.y, m.z, playerPos, config.meteorShowDistance);
            maxTextWidth = Math.max(maxTextWidth,
                scaled(20) + (int) (client.textRenderer.getWidth(Text.literal(coords)) * scale));
        }
        for (MerchantInfo m : visibleMerchants) {
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(m.type.getDisplayName())) * scale));
            String coords = coordsWithDist(m.x, m.y, m.z, playerPos, config.merchantShowDistance);
            maxTextWidth = Math.max(maxTextWidth,
                scaled(20) + (int) (client.textRenderer.getWidth(Text.literal(coords)) * scale));
        }

        // --- Background & border ---
        int bgWidth = maxTextWidth;
        int contentHeight = (activeMeteors.size() + visibleMerchants.size()) * scaled(32);
        int bgHeight = titleHeight + contentHeight;

        int bgColor     = (config.eventsBgOpacity << 24)     | (config.eventsBgColor & 0xFFFFFF);
        int borderColor = (config.eventsBorderOpacity << 24) | (config.eventsBorderColor & 0xFFFFFF);
        int thickness   = scaled(config.eventsBorderThickness);
        int padding     = scale < 1 ? scaled(4) : 4;

        ctx.fill(x - padding, y - padding, x + bgWidth + padding, y + bgHeight + padding, bgColor);
        ctx.fill(x - padding,           y - padding - thickness, x + bgWidth + padding, y - padding, borderColor);
        ctx.fill(x - padding,           y + bgHeight + padding,  x + bgWidth + padding, y + bgHeight + padding + thickness, borderColor);
        ctx.fill(x - padding - thickness, y - padding - thickness, x - padding, y + bgHeight + padding + thickness, borderColor);
        ctx.fill(x + bgWidth + padding,   y - padding - thickness, x + bgWidth + padding + thickness, y + bgHeight + padding + thickness, borderColor);

        Matrix3x2fStack matrices = ctx.getMatrices();
        int yOffset = 0;

        // --- Title ---
        if (showTitle) {
            Text titleText = Text.literal("Events HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            int titleColor = 0xFF000000 | config.eventsHudTitleColor;
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x / scale, y / scale);
            ctx.drawTextWithShadow(client.textRenderer, titleText, 0, 0, titleColor);
            matrices.popMatrix();
            yOffset += titleHeight;
        }

        int iconSpacing = scaled(20); // 16px icon + 4px gap

        // --- Meteors ---
        int meteorCoordColor = 0xFF000000 | config.eventsTextColor;
        for (MeteorInfo m : activeMeteors) {
            String heading = m.type == MeteorType.NATURAL ? "Natural Meteor" : "Summoned Meteor";
            int headingColor = 0xFF000000 | (m.type == MeteorType.NATURAL
                ? config.eventsNaturalHeadingColor : config.eventsSummonedHeadingColor);

            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x / scale, (y + yOffset) / scale);
            ctx.drawTextWithShadow(client.textRenderer,
                Text.literal(heading).setStyle(Style.EMPTY.withItalic(true)), 0, 0, headingColor);
            matrices.popMatrix();
            yOffset += scaled(12);

            if (m.iconStack != null) {
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x / scale, (y + yOffset) / scale);
                ctx.drawItem(m.iconStack, 0, 0);
                matrices.popMatrix();
            }

            String coords = coordsWithDist(m.x, m.y, m.z, playerPos, config.meteorShowDistance);
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate((x + iconSpacing) / scale, (y + yOffset + scaled(4)) / scale);
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(coords), 0, 0, meteorCoordColor);
            matrices.popMatrix();
            yOffset += scaled(20);
        }

        // --- Merchants ---
        for (MerchantInfo m : visibleMerchants) {
            boolean slain = m.slainTime != null;
            int headingColor = 0xFF000000 | m.type.getHeadingColor(config);
            // Dim slain merchants to 50% alpha so they're visually distinct
            if (slain) headingColor = (headingColor & 0x00FFFFFF) | 0x80000000;

            String displayName = slain
                ? m.type.getDisplayName() + " §c[Slain]"
                : m.type.getDisplayName();

            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x / scale, (y + yOffset) / scale);
            ctx.drawTextWithShadow(client.textRenderer,
                Text.literal(displayName).setStyle(Style.EMPTY.withItalic(true)), 0, 0, headingColor);
            matrices.popMatrix();
            yOffset += scaled(12);

            if (m.iconStack != null) {
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x / scale, (y + yOffset) / scale);
                ctx.drawItem(m.iconStack, 0, 0);
                matrices.popMatrix();
            }

            String coords = coordsWithDist(m.x, m.y, m.z, playerPos, config.merchantShowDistance);
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate((x + iconSpacing) / scale, (y + yOffset + scaled(4)) / scale);
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(coords), 0, 0, headingColor);
            matrices.popMatrix();
            yOffset += scaled(20);
        }
    }

    // -------------------------------------------------------------------------
    // Size helpers
    // -------------------------------------------------------------------------

    @Override
    public int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return scaled(130);

        Config config = BetterPrisonsClient.config;
        List<MerchantInfo> visibleMerchants = getVisibleMerchants();

        int maxTextWidth = 0;
        if (config.showEventsHudTitle) {
            Text titleText = Text.literal("Events HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            maxTextWidth = (int) (client.textRenderer.getWidth(titleText) * scale);
        }
        Vec3d playerPos = client.player != null
            ? new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ()) : null;
        for (MeteorInfo m : activeMeteors) {
            String heading = m.type == MeteorType.NATURAL ? "Natural Meteor" : "Summoned Meteor";
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(heading)) * scale));
            String coords = coordsWithDist(m.x, m.y, m.z, playerPos, config.meteorShowDistance);
            maxTextWidth = Math.max(maxTextWidth,
                scaled(20) + (int) (client.textRenderer.getWidth(Text.literal(coords)) * scale));
        }
        for (MerchantInfo m : visibleMerchants) {
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(m.type.getDisplayName())) * scale));
            String coords = coordsWithDist(m.x, m.y, m.z, playerPos, config.merchantShowDistance);
            maxTextWidth = Math.max(maxTextWidth,
                scaled(20) + (int) (client.textRenderer.getWidth(Text.literal(coords)) * scale));
        }

        int padding = scale < 1 ? scaled(4) : 4;
        return maxTextWidth + (padding * 2);
    }

    @Override
    public int getHeight() {
        Config config = BetterPrisonsClient.config;
        int titleHeight = config.showEventsHudTitle ? scaled(10) : 0;
        int visibleMerchantCount = getVisibleMerchants().size();
        return titleHeight + ((activeMeteors.size() + visibleMerchantCount) * scaled(32));
    }

    // -------------------------------------------------------------------------
    // Data classes
    // -------------------------------------------------------------------------

    public static class MeteorInfo {
        public int x, y, z;
        public long spawnTime;
        public Long crashTime;
        public ItemStack iconStack;
        public MeteorType type;

        public MeteorInfo(int x, int y, int z, long spawnTime, ItemStack iconStack, MeteorType type) {
            this.x = x; this.y = y; this.z = z;
            this.spawnTime = spawnTime;
            this.crashTime = null;
            this.iconStack = iconStack;
            this.type = type;
        }
    }

    public static class MerchantInfo {
        public int x, y, z;
        public long spawnTime;
        public Long slainTime;
        public ItemStack iconStack;
        public MerchantType type;

        public MerchantInfo(int x, int y, int z, long spawnTime, ItemStack iconStack, MerchantType type) {
            this.x = x; this.y = y; this.z = z;
            this.spawnTime = spawnTime;
            this.slainTime = null;
            this.iconStack = iconStack;
            this.type = type;
        }
    }
}
