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
import net.minecraft.sound.SoundEvents;
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
    private static final long NATURAL_METEOR_DURATION_MS = 7 * 60 * 1000L; // 10 minutes from spawn to landing
    private static final long SUMMONED_METEOR_DURATION_MS = 60 * 1000L;      // 1 minute
    // How long an event may stay "(Imminent)" (landing time passed, no crash message
    // received — e.g. the player logged off and missed the crash) before it's removed.
    private static final long IMMINENT_GRACE_MS = 60 * 1000L; // 1 minute
    private static final Pattern COORDS_PATTERN = Pattern.compile("(-?\\d+)x,\\s*(-?\\d+)y,\\s*(-?\\d+)z");

    // --- Merchants ---
    private final List<MerchantInfo> activeMerchants = new ArrayList<>();

    // --- Bandit Rushes ---
    private final List<BanditRushInfo> activeBanditRushes = new ArrayList<>();

    // --- Meteorite Showers ---
    private final List<MeteoriteShowerInfo> activeMeteoriteShowers = new ArrayList<>();
    private static final long METEORITE_SHOWER_WARNING_MS = 60 * 1000L; // "will crash in 1 minute"
    private static final long METEORITE_SHOWER_TIMEOUT_MS = 15 * 60 * 1000L; // safety expiry
    // Coords + optional zone: "220x, 108y, -794z (Iron Zone)"
    private static final Pattern SHOWER_COORDS_PATTERN =
        Pattern.compile("(-?\\d+)x,\\s*(-?\\d+)y,\\s*(-?\\d+)z(?:\\s*\\(([^)]+)\\))?");

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

    /**
     * The four badlands sub-worlds within minecraft:badlands, identified by coordinate bounds.
     * Each region is defined by two opposite corners (x1, z1) to (x2, z2).
     */
    public enum BadlandsRegion {
        CHAIN(1073, -127, 1295, 95),
        GOLD(641, -127, 863, 95),
        IRON(641, 289, 863, 511),
        DIAMOND(1073, 289, 1295, 511);

        public final int minX, minZ, maxX, maxZ;

        BadlandsRegion(int x1, int z1, int x2, int z2) {
            this.minX = Math.min(x1, x2);
            this.minZ = Math.min(z1, z2);
            this.maxX = Math.max(x1, x2);
            this.maxZ = Math.max(z1, z2);
        }

        public boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }

        public static BadlandsRegion fromCoords(int x, int z) {
            for (BadlandsRegion r : values()) {
                if (r.contains(x, z)) return r;
            }
            return null;
        }

        public static BadlandsRegion getPlayerRegion() {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return null;
            return fromCoords((int) client.player.getX(), (int) client.player.getZ());
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
                long now = System.currentTimeMillis();
                long duration = (type == MeteorType.NATURAL) ? NATURAL_METEOR_DURATION_MS : SUMMONED_METEOR_DURATION_MS;
                activeMeteors.add(new MeteorInfo(x, y, z, now, now + duration, createMeteorIcon(), type));
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

                // No matching falling meteor (e.g. the warning was missed) — register
                // the crash directly so the mineable location still shows.
                MeteorType type = coordsLine.contains("Summoned by") ? MeteorType.SUMMONED : MeteorType.NATURAL;
                long now = System.currentTimeMillis();
                MeteorInfo crashed = new MeteorInfo(x, y, z, now, now, createMeteorIcon(), type);
                crashed.crashTime = now;
                activeMeteors.add(crashed);
                BetterPrisonsClient.LOGGER.info("Meteor crash registered (no prior falling) at: {}, {}, {} (type: {})", x, y, z, type);

                String name = (type == MeteorType.NATURAL) ? "Natural Meteor" : "Summoned Meteor";
                int color = (type == MeteorType.NATURAL)
                    ? BetterPrisonsClient.config.eventsNaturalHeadingColor
                    : BetterPrisonsClient.config.eventsSummonedHeadingColor;
                String eventKey = (type == MeteorType.NATURAL) ? "METEOR_NATURAL" : "METEOR_SUMMONED";
                BetterPrisonsClient.waypointManager.addEventWaypoint(x, y, z, color, name, eventKey);
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
        // Match on x/z only — the slain Y often differs from spawn Y (merchant moves around)
        for (MerchantInfo m : activeMerchants) {
            if (m.x == x && m.z == z && m.slainTime == null) {
                m.slainTime = System.currentTimeMillis();
                BetterPrisonsClient.LOGGER.info("Merchant marked as slain: {} at {}, {}, {} (spawn Y was {})",
                    tierName, x, y, z, m.y);
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

    // -------------------------------------------------------------------------
    // Bandit Rush API
    // -------------------------------------------------------------------------

    /**
     * Called when a bandit rush spawn message is detected.
     * Only registers the event if the player is in the same badlands sub-world.
     */
    public void onBanditRushSpawned(String tier, int x, int y, int z) {
        if (!BetterPrisonsClient.config.banditRushEnabled) return;

        BadlandsRegion rushRegion = BadlandsRegion.fromCoords(x, z);
        BadlandsRegion playerRegion = BadlandsRegion.getPlayerRegion();
        if (rushRegion == null || playerRegion == null || rushRegion != playerRegion) {
            BetterPrisonsClient.LOGGER.info("Bandit rush at {}, {}, {} ignored (different badlands region)", x, y, z);
            return;
        }

        for (BanditRushInfo b : activeBanditRushes) {
            if (b.x == x && b.y == y && b.z == z) return; // duplicate
        }

        ItemStack icon = createBanditRushIcon();
        activeBanditRushes.add(new BanditRushInfo(x, y, z, System.currentTimeMillis(), icon, tier));
        BetterPrisonsClient.LOGGER.info("Bandit rush detected: {} at {}, {}, {}", tier, x, y, z);

        int color = BetterPrisonsClient.config.banditRushHeadingColor;
        String name = tier + " Bandit Rush";
        BetterPrisonsClient.waypointManager.addEventWaypoint(x, y, z, color, name, "BANDIT_RUSH_" + tier.toUpperCase());

        // Play notification sound
        if (BetterPrisonsClient.config.banditRushSoundEnabled) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                float volume = BetterPrisonsClient.config.banditRushSoundVolume / 100.0f;
                String soundType = BetterPrisonsClient.config.banditRushSound;
                switch (soundType) {
                    case "bell":
                        client.player.playSound(SoundEvents.BLOCK_BELL_USE, volume, 1.0f);
                        break;
                    case "xp_orb":
                        client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 1.0f);
                        break;
                    case "note_pling":
                        client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), volume, 1.0f);
                        break;
                    case "enchant":
                        client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, volume, 1.0f);
                        break;
                    case "level_up":
                        client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, volume, 1.0f);
                        break;
                    case "ender_eye":
                        client.player.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, volume, 1.0f);
                        break;
                    default:
                        client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, volume, 1.0f);
                        break;
                }
            }
        }
    }

    public List<BanditRushInfo> getActiveBanditRushes() {
        return activeBanditRushes;
    }

    public List<BanditRushInfo> getVisibleBanditRushes() {
        if (!BetterPrisonsClient.config.banditRushEnabled) return new ArrayList<>();
        return new ArrayList<>(activeBanditRushes);
    }

    /**
     * Called when a bandit rush is won. Removes any active rush of the same tier
     * in the same badlands sub-world as the won coordinates.
     */
    public void onBanditRushWon(String tier, int x, int z) {
        BadlandsRegion wonRegion = BadlandsRegion.fromCoords(x, z);
        activeBanditRushes.removeIf(b -> {
            if (b.tier.equalsIgnoreCase(tier)) {
                BadlandsRegion rushRegion = BadlandsRegion.fromCoords(b.x, b.z);
                if (wonRegion != null && wonRegion == rushRegion) {
                    BetterPrisonsClient.waypointManager.removeEventWaypoint(b.x, b.y, b.z);
                    BetterPrisonsClient.LOGGER.info("Bandit rush {} won in {} region — removed from {}, {}, {}",
                        tier, wonRegion, b.x, b.y, b.z);
                    return true;
                }
            }
            return false;
        });
    }

    public void clearBanditRushes() {
        for (BanditRushInfo b : activeBanditRushes) {
            BetterPrisonsClient.waypointManager.removeEventWaypoint(b.x, b.y, b.z);
        }
        activeBanditRushes.clear();
    }

    private ItemStack createBanditRushIcon() {
        try {
            String itemId = BetterPrisonsClient.config.banditRushIconItemId;
            if (!itemId.contains(":")) itemId = "minecraft:" + itemId;
            Identifier identifier = Identifier.tryParse(itemId);
            if (identifier != null) {
                Item item = Registries.ITEM.get(identifier);
                if (item != null) return new ItemStack(item);
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Failed to create bandit rush icon: {}", e.getMessage());
        }
        return new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "iron_sword")));
    }

    // -------------------------------------------------------------------------
    // Meteorite Shower API
    // -------------------------------------------------------------------------

    /**
     * Called when a meteorite shower announcement is detected.
     * coordsLine is the line containing "Mine the comets at <coords> (<zone>) for".
     * @param crashed true if the shower has already crashed (mineable now),
     *                false if it's the "will crash in 1 minute" warning.
     */
    public void onMeteoriteShower(String coordsLine, boolean crashed) {
        if (!BetterPrisonsClient.config.meteoriteShowerEnabled) return;
        Matcher matcher = SHOWER_COORDS_PATTERN.matcher(coordsLine);
        if (!matcher.find()) return;
        try {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            String zone = matcher.group(4); // may be null

            long now = System.currentTimeMillis();

            // If we already track this shower, just update its state.
            for (MeteoriteShowerInfo s : activeMeteoriteShowers) {
                if (s.x == x && s.y == y && s.z == z) {
                    if (crashed && s.crashTime == null) {
                        s.crashTime = now;
                        BetterPrisonsClient.LOGGER.info("Meteorite shower crashed at {}, {}, {}", x, y, z);
                    }
                    return;
                }
            }

            MeteoriteShowerInfo info = new MeteoriteShowerInfo(
                x, y, z, now, now + METEORITE_SHOWER_WARNING_MS, createMeteoriteShowerIcon(), zone);
            if (crashed) info.crashTime = now;
            activeMeteoriteShowers.add(info);
            BetterPrisonsClient.LOGGER.info("Meteorite shower detected at {}, {}, {} (zone: {}, crashed: {})",
                x, y, z, zone, crashed);

            int color = BetterPrisonsClient.config.meteoriteShowerHeadingColor;
            BetterPrisonsClient.waypointManager.addEventWaypoint(x, y, z, color, "Meteorite Shower", "METEORITE_SHOWER");

            playMeteoriteShowerSound();
        } catch (NumberFormatException e) {
            BetterPrisonsClient.LOGGER.warn("Failed to parse meteorite shower coordinates: {}", coordsLine);
        }
    }

    private void playMeteoriteShowerSound() {
        if (!BetterPrisonsClient.config.meteoriteShowerSoundEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        float volume = BetterPrisonsClient.config.meteoriteShowerSoundVolume / 100.0f;
        switch (BetterPrisonsClient.config.meteoriteShowerSound) {
            case "bell":      client.player.playSound(SoundEvents.BLOCK_BELL_USE, volume, 1.0f); break;
            case "xp_orb":    client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 1.0f); break;
            case "note_pling":client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), volume, 1.0f); break;
            case "enchant":   client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, volume, 1.0f); break;
            case "level_up":  client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, volume, 1.0f); break;
            case "ender_eye": client.player.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, volume, 1.0f); break;
            default:          client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, volume, 1.0f); break;
        }
    }

    public List<MeteoriteShowerInfo> getVisibleMeteoriteShowers() {
        if (!BetterPrisonsClient.config.meteoriteShowerEnabled) return new ArrayList<>();
        return new ArrayList<>(activeMeteoriteShowers);
    }

    public void clearMeteoriteShowers() {
        for (MeteoriteShowerInfo s : activeMeteoriteShowers) {
            BetterPrisonsClient.waypointManager.removeEventWaypoint(s.x, s.y, s.z);
        }
        activeMeteoriteShowers.clear();
    }

    private ItemStack createMeteoriteShowerIcon() {
        try {
            String itemId = BetterPrisonsClient.config.meteoriteShowerIconItemId;
            if (!itemId.contains(":")) itemId = "minecraft:" + itemId;
            Identifier identifier = Identifier.tryParse(itemId);
            if (identifier != null) {
                Item item = Registries.ITEM.get(identifier);
                if (item != null) return new ItemStack(item);
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("Failed to create meteorite shower icon: {}", e.getMessage());
        }
        return new ItemStack(Registries.ITEM.get(Identifier.of("minecraft", "magma_block")));
    }

    /** Builds the meteorite shower heading with countdown / mineable state. */
    private String buildShowerHeading(MeteoriteShowerInfo s, long now) {
        String name = "Meteorite Shower";
        if (s.crashTime != null) return name + " [Mineable]";
        long remaining = s.landingTime - now;
        if (remaining <= 0) return name + " (Imminent)";
        long totalSecs = remaining / 1000;
        return String.format("%s (%d:%02d)", name, totalSecs / 60, totalSecs % 60);
    }

    /** Builds the meteorite shower coordinate line. */
    private String buildShowerCoords(MeteoriteShowerInfo s, Vec3d playerPos) {
        return coordsWithDist(s.x, s.y, s.z, playerPos,
            BetterPrisonsClient.config.meteoriteShowerShowDistance);
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

    /** Builds the meteor heading line with countdown timer or crashed indicator. */
    private String buildMeteorHeading(MeteorInfo m, long now) {
        String name = m.type == MeteorType.NATURAL ? "Natural Meteor" : "Summoned Meteor";
        if (m.crashTime != null) return name + " [Crashed]";
        long remaining = m.landingTime - now;
        if (remaining <= 0) return name + " (Imminent)";
        long totalSecs = remaining / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        return String.format("%s (%d:%02d)", name, mins, secs);
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
            // Stuck "(Imminent)" — crash message never arrived (player logged off / missed it)
            if (m.crashTime == null && now - m.landingTime > IMMINENT_GRACE_MS) {
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

        long banditRushDurationMs = BetterPrisonsClient.config.banditRushTimeoutSeconds * 1000L;
        activeBanditRushes.removeIf(b -> {
            if (!BetterPrisonsClient.waypointManager.hasEventWaypoint(b.x, b.y, b.z)) return true;
            if (now - b.spawnTime > banditRushDurationMs) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(b.x, b.y, b.z);
                return true;
            }
            return false;
        });

        long showerMineableMs = BetterPrisonsClient.config.meteoriteShowerTimeoutSeconds * 1000L;
        activeMeteoriteShowers.removeIf(s -> {
            if (!BetterPrisonsClient.waypointManager.hasEventWaypoint(s.x, s.y, s.z)) return true;
            // Remove once it has been mineable for the configured duration
            if (s.crashTime != null && now - s.crashTime > showerMineableMs) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(s.x, s.y, s.z);
                return true;
            }
            // Stuck "(Imminent)" — crash message never arrived (player logged off / missed it)
            if (s.crashTime == null && now - s.landingTime > IMMINENT_GRACE_MS) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(s.x, s.y, s.z);
                return true;
            }
            // Safety expiry in case the "has crashed" message never arrives
            if (now - s.spawnTime > METEORITE_SHOWER_TIMEOUT_MS) {
                BetterPrisonsClient.waypointManager.removeEventWaypoint(s.x, s.y, s.z);
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
        List<BanditRushInfo> visibleBanditRushes = getVisibleBanditRushes();
        List<MeteoriteShowerInfo> visibleShowers = getVisibleMeteoriteShowers();
        boolean hasContent = !activeMeteors.isEmpty() || !visibleMerchants.isEmpty()
            || !visibleBanditRushes.isEmpty() || !visibleShowers.isEmpty();

        if (!enabled || (!showTitle && !hasContent)) return;

        // --- Title dimensions ---
        int titleHeight = 0, titleWidth = 0;
        if (showTitle) {
            Text titleText = Text.literal("Events HUD").setStyle(Style.EMPTY.withUnderline(true).withBold(true));
            titleWidth = (int) (client.textRenderer.getWidth(titleText) * scale);
            titleHeight = scaled(12);
        }

        long renderNow = System.currentTimeMillis();

        // --- Max width across all entries ---
        int maxTextWidth = titleWidth;
        for (MeteorInfo m : activeMeteors) {
            String heading = buildMeteorHeading(m, renderNow);
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
        for (BanditRushInfo b : visibleBanditRushes) {
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(b.getDisplayName())) * scale));
            String coords = coordsWithDist(b.x, b.y, b.z, playerPos, config.banditRushShowDistance);
            maxTextWidth = Math.max(maxTextWidth,
                scaled(20) + (int) (client.textRenderer.getWidth(Text.literal(coords)) * scale));
        }
        for (MeteoriteShowerInfo s : visibleShowers) {
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(buildShowerHeading(s, renderNow))) * scale));
            String coords = buildShowerCoords(s, playerPos);
            maxTextWidth = Math.max(maxTextWidth,
                scaled(20) + (int) (client.textRenderer.getWidth(Text.literal(coords)) * scale));
        }

        // --- Background & border ---
        int bgWidth = maxTextWidth;
        int contentHeight = (activeMeteors.size() + visibleMerchants.size()
            + visibleBanditRushes.size() + visibleShowers.size()) * scaled(32);
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
            String heading = buildMeteorHeading(m, renderNow);
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

        // --- Bandit Rushes ---
        int rushCoordColor = 0xFF000000 | config.banditRushTextColor;
        for (BanditRushInfo b : visibleBanditRushes) {
            int rushHeadingColor = 0xFF000000 | config.banditRushHeadingColor;
            String displayName = b.getDisplayName();

            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x / scale, (y + yOffset) / scale);
            ctx.drawTextWithShadow(client.textRenderer,
                Text.literal(displayName).setStyle(Style.EMPTY.withItalic(true)), 0, 0, rushHeadingColor);
            matrices.popMatrix();
            yOffset += scaled(12);

            if (b.iconStack != null) {
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x / scale, (y + yOffset) / scale);
                ctx.drawItem(b.iconStack, 0, 0);
                matrices.popMatrix();
            }

            String rushCoords = coordsWithDist(b.x, b.y, b.z, playerPos, config.banditRushShowDistance);
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate((x + iconSpacing) / scale, (y + yOffset + scaled(4)) / scale);
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(rushCoords), 0, 0, rushCoordColor);
            matrices.popMatrix();
            yOffset += scaled(20);
        }

        // --- Meteorite Showers ---
        int showerCoordColor = 0xFF000000 | config.meteoriteShowerTextColor;
        for (MeteoriteShowerInfo s : visibleShowers) {
            int showerHeadingColor = 0xFF000000 | config.meteoriteShowerHeadingColor;
            String displayName = buildShowerHeading(s, renderNow);

            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate(x / scale, (y + yOffset) / scale);
            ctx.drawTextWithShadow(client.textRenderer,
                Text.literal(displayName).setStyle(Style.EMPTY.withItalic(true)), 0, 0, showerHeadingColor);
            matrices.popMatrix();
            yOffset += scaled(12);

            if (s.iconStack != null) {
                matrices.pushMatrix();
                matrices.scale(scale);
                matrices.translate(x / scale, (y + yOffset) / scale);
                ctx.drawItem(s.iconStack, 0, 0);
                matrices.popMatrix();
            }

            String showerCoords = buildShowerCoords(s, playerPos);
            matrices.pushMatrix();
            matrices.scale(scale);
            matrices.translate((x + iconSpacing) / scale, (y + yOffset + scaled(4)) / scale);
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(showerCoords), 0, 0, showerCoordColor);
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
        long widthNow = System.currentTimeMillis();
        for (MeteorInfo m : activeMeteors) {
            String heading = buildMeteorHeading(m, widthNow);
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
        for (BanditRushInfo b : getVisibleBanditRushes()) {
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(b.getDisplayName())) * scale));
            String coords = coordsWithDist(b.x, b.y, b.z, playerPos, config.banditRushShowDistance);
            maxTextWidth = Math.max(maxTextWidth,
                scaled(20) + (int) (client.textRenderer.getWidth(Text.literal(coords)) * scale));
        }
        for (MeteoriteShowerInfo s : getVisibleMeteoriteShowers()) {
            maxTextWidth = Math.max(maxTextWidth,
                (int) (client.textRenderer.getWidth(Text.literal(buildShowerHeading(s, widthNow))) * scale));
            String coords = buildShowerCoords(s, playerPos);
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
        int visibleBanditRushCount = getVisibleBanditRushes().size();
        int visibleShowerCount = getVisibleMeteoriteShowers().size();
        return titleHeight + ((activeMeteors.size() + visibleMerchantCount
            + visibleBanditRushCount + visibleShowerCount) * scaled(32));
    }

    // -------------------------------------------------------------------------
    // Data classes
    // -------------------------------------------------------------------------

    public static class MeteorInfo {
        public int x, y, z;
        public long spawnTime;
        public long landingTime; // estimated time of impact
        public Long crashTime;
        public ItemStack iconStack;
        public MeteorType type;

        public MeteorInfo(int x, int y, int z, long spawnTime, long landingTime, ItemStack iconStack, MeteorType type) {
            this.x = x; this.y = y; this.z = z;
            this.spawnTime = spawnTime;
            this.landingTime = landingTime;
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

    public static class BanditRushInfo {
        public int x, y, z;
        public long spawnTime;
        public ItemStack iconStack;
        public String tier; // e.g. "DIAMOND", "IRON"

        public BanditRushInfo(int x, int y, int z, long spawnTime, ItemStack iconStack, String tier) {
            this.x = x; this.y = y; this.z = z;
            this.spawnTime = spawnTime;
            this.iconStack = iconStack;
            this.tier = tier;
        }

        public String getDisplayName() {
            String name = tier.charAt(0) + tier.substring(1).toLowerCase();
            return name + " Bandit Rush";
        }
    }

    public static class MeteoriteShowerInfo {
        public int x, y, z;
        public long spawnTime;
        public long landingTime; // when it will crash (spawnTime + 1 min for the warning)
        public Long crashTime;   // set once it has crashed (mineable)
        public ItemStack iconStack;
        public String zone;      // e.g. "Iron Zone" — may be null

        public MeteoriteShowerInfo(int x, int y, int z, long spawnTime, long landingTime, ItemStack iconStack, String zone) {
            this.x = x; this.y = y; this.z = z;
            this.spawnTime = spawnTime;
            this.landingTime = landingTime;
            this.crashTime = null;
            this.iconStack = iconStack;
            this.zone = zone;
        }
    }
}
