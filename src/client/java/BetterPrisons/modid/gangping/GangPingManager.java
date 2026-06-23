package BetterPrisons.modid.gangping;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.waypoint.WaypointManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

/**
 * Manages gang ping waypoints — transient waypoints triggered by chat messages.
 * One ping per player; new pings replace old ones. Auto-expire after 15 seconds.
 */
public class GangPingManager {

    private static final long PING_TIMEOUT_MS = 60_000L;

    private static final long SEND_COOLDOWN_MS = 3_000L;

    /** Active pings keyed by player name (one per player). */
    private final Map<String, GangPingInfo> activePings = new LinkedHashMap<>();
    private long lastSendTime = 0;

    // ----------------------------------------------------------------
    // Data class
    // ----------------------------------------------------------------

    public static class GangPingInfo {
        public final String playerName;
        public final int x, y, z;
        public final String world;
        public final float hp, maxHp;
        public final String facing;
        public final long createdAt;
        public final boolean isTruce;

        public GangPingInfo(String playerName, int x, int y, int z, String world,
                            float hp, float maxHp, String facing, boolean isTruce) {
            this.playerName = playerName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
            this.hp = hp;
            this.maxHp = maxHp;
            this.facing = facing;
            this.isTruce = isTruce;
            this.createdAt = System.currentTimeMillis();
        }
    }

    // ----------------------------------------------------------------
    // Send ping
    // ----------------------------------------------------------------

    public void sendPing(MinecraftClient client) {
        sendPingInternal(client, false, null);
    }

    public void sendTrucePing(MinecraftClient client) {
        sendPingInternal(client, true, null);
    }

    /** Sends a gang ping at the given block position instead of the player's position. */
    public void sendPingAtBlock(MinecraftClient client, BlockPos pos) {
        sendPingInternal(client, false, pos);
    }

    private void sendPingInternal(MinecraftClient client, boolean truce, BlockPos overridePos) {
        if (client.player == null || client.world == null || client.getNetworkHandler() == null) return;
        long now = System.currentTimeMillis();
        long remaining = SEND_COOLDOWN_MS - (now - lastSendTime);
        if (remaining > 0) {
            String seconds = String.format("%.1f", remaining / 1000.0);
            client.player.sendMessage(
                Text.literal("\u00a7c[BetterPrisons] Ping on cooldown! Wait " + seconds + "s"), false);
            return;
        }
        lastSendTime = now;

        int x, y, z;
        if (overridePos != null) {
            x = overridePos.getX();
            y = overridePos.getY();
            z = overridePos.getZ();
        } else {
            x = client.player.getBlockPos().getX();
            y = (int) Math.round(client.player.getEyeY());
            z = client.player.getBlockPos().getZ();
        }
        String world = WaypointManager.detectWorldKey();
        float hp = client.player.getHealth();
        float maxHp = client.player.getMaxHealth();

        Direction dir = client.player.getHorizontalFacing();
        String facing = dir.asString().substring(0, 1).toUpperCase() + dir.asString().substring(1);

        String prefix = truce ? "[T!]" : "[!]";
        String msg = String.format("%s %s has pinged at %dx %dy %dz %s | HP %.0f/%.0f | Facing %s",
                prefix, client.player.getGameProfile().name(), x, y, z, world, hp, maxHp, facing);

        client.getNetworkHandler().sendChatCommand(truce ? "g c t" : "g c g");
        client.getNetworkHandler().sendChatMessage(msg);
    }

    // ----------------------------------------------------------------
    // Receive ping
    // ----------------------------------------------------------------

    public void onGangPingReceived(String playerName, int x, int y, int z, String world,
                                    float hp, float maxHp, String facing, boolean isTruce) {
        GangPingInfo info = new GangPingInfo(playerName, x, y, z, world, hp, maxHp, facing, isTruce);
        activePings.put(playerName, info);
        BetterPrisonsClient.LOGGER.info("{} ping from {} at {}, {}, {} ({})",
                isTruce ? "Truce" : "Gang", playerName, x, y, z, world);
    }

    // ----------------------------------------------------------------
    // Tick — expire old pings
    // ----------------------------------------------------------------

    public void tick() {
        long now = System.currentTimeMillis();
        activePings.values().removeIf(p -> now - p.createdAt > PING_TIMEOUT_MS);
    }

    // ----------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------

    public List<GangPingInfo> getActivePings() {
        return new ArrayList<>(activePings.values());
    }

    public void clear() {
        activePings.clear();
    }

    // ----------------------------------------------------------------
    // Distance-based opacity: starts at base, fades to 0 at distance 0
    // ----------------------------------------------------------------

    public static float calculateOpacity(float distance, float baseOpacity) {
        float fadeStart = 10f; // blocks — below this, opacity decreases toward minimum
        float minOpacity = baseOpacity * 0.3f; // 30% of base as floor
        if (distance <= 0) return minOpacity;
        if (distance >= fadeStart) return baseOpacity;
        return minOpacity + (baseOpacity - minOpacity) * (distance / fadeStart);
    }

    // ----------------------------------------------------------------
    // Distance-based scale: peaks at ~75 blocks, decays to 30% beyond
    // ----------------------------------------------------------------

    public static float calculateScale(float distance, float minScale, float maxScale, boolean distanceScaling) {
        if (!distanceScaling) return minScale;
        float peakDistance = 75f;
        if (distance <= peakDistance) {
            return minScale + (maxScale - minScale) * (distance / peakDistance);
        } else {
            return maxScale;
        }
    }
}
