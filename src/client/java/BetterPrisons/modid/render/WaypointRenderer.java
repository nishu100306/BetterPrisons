package BetterPrisons.modid.render;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.gangping.GangPingManager;
import BetterPrisons.modid.hud.EventsHud;
import BetterPrisons.modid.waypoint.CustomWaypoint;
import BetterPrisons.modid.waypoint.WaypointManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders 2D screen-edge waypoint indicators for active meteors and merchants.
 *
 * Collision avoidance: all waypoints are collected, sorted nearest-first, then
 * placed one by one. Each new marker is nudged along the Y axis (or Y+X for
 * on-screen markers) until it no longer overlaps any already-placed marker.
 *
 * Indicators:
 *   • On-screen  → item icon centered at projected position
 *   • Off-screen → item icon clamped to left/right screen edge + small arrow badge
 */
public class WaypointRenderer {

    private static final int ICON_HALF    = 8;   // half of the 16×16 item sprite
    private static final int EDGE_MARGIN  = 20;
    private static final int ARROW_OFFSET = 11;  // px from icon center to arrow center
    private static final int ARROW_RADIUS = 5;   // half-size of the arrow triangle
    private static final int LABEL_H      = 7;   // approximate text line height (no shadow)
    private static final int NUDGE_STEP   = 26;  // px per nudge attempt (must exceed box height)
    private static final int MAX_NUDGES   = 8;   // attempts per axis

    // ----------------------------------------------------------------
    // Entry — one waypoint with all data needed for placement + rendering
    // ----------------------------------------------------------------

    enum EntryType { METEOR, MERCHANT, CUSTOM, GANG_PING }

    private static class Entry {
        int wx, wy, wz;
        int rgb;
        ItemStack iconStack;  // null → draw colored square
        float projX, projY;   // raw projected (may be off-screen)
        boolean onScreen;
        double dist;
        String label;
        int labelWidth;
        int ix, iy;           // final placed position after collision avoidance
        EntryType type;
        float onScreenScale  = 1.0f;
        float offScreenScale = 1.0f;
        GangPingManager.GangPingInfo gangPing; // non-null only for GANG_PING entries
        float gangPingAlpha = 1.0f;             // distance-faded alpha for gang pings

        float scale() { return onScreen ? onScreenScale : offScreenScale; }
    }

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private static void render(DrawContext ctx) {
        boolean waypointsEnabled = BetterPrisonsClient.config.waypointsEnabled;
        boolean gangPingsEnabled = BetterPrisonsClient.config.gangPingEnabled || BetterPrisonsClient.config.trucePingEnabled;
        if (!waypointsEnabled && !gangPingsEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen != null) return;

        int screenW = ctx.getScaledWindowWidth();
        int screenH = ctx.getScaledWindowHeight();

        String worldKey = client.world != null
            ? client.world.getRegistryKey().getValue().toString() : "";
        boolean inOverworld = "minecraft:overworld".equals(worldKey);
        boolean inBadlands = "minecraft:badlands".equals(worldKey);

        // --- Collect all waypoints ---
        List<Entry> entries = new ArrayList<>();

        if (waypointsEnabled && inOverworld && BetterPrisonsClient.config.waypointMeteorsEnabled) {
            for (EventsHud.MeteorInfo m : BetterPrisonsClient.eventsHud.getActiveMeteors()) {
                int color = (m.type == EventsHud.MeteorType.NATURAL)
                    ? BetterPrisonsClient.config.eventsNaturalHeadingColor
                    : BetterPrisonsClient.config.eventsSummonedHeadingColor;
                Entry e = buildEntry(client, m.x, m.y, m.z, color, m.iconStack, screenW, screenH, EntryType.METEOR);
                if (e != null && (e.onScreen || BetterPrisonsClient.config.waypointMeteorsEdgeEnabled))
                    entries.add(e);
            }
        }

        if (waypointsEnabled && inOverworld && BetterPrisonsClient.config.waypointMerchantsEnabled) {
            for (EventsHud.MerchantInfo m : BetterPrisonsClient.eventsHud.getVisibleMerchantsForWaypoints()) {
                int color = m.type.getHeadingColor(BetterPrisonsClient.config);
                Entry e = buildEntry(client, m.x, m.y, m.z, color, m.iconStack, screenW, screenH, EntryType.MERCHANT);
                if (e != null && (e.onScreen || BetterPrisonsClient.config.waypointMerchantsEdgeEnabled))
                    entries.add(e);
            }
        }

        if (waypointsEnabled && BetterPrisonsClient.config.waypointCustomEnabled) {
            for (CustomWaypoint wp : BetterPrisonsClient.waypointManager.getEnabled()) {
                // Use name + distance as combined label; no item icon (colored square)
                Entry e = buildEntry(client, wp.x, wp.y, wp.z, wp.color, null, screenW, screenH, EntryType.CUSTOM);
                if (e != null) {
                    // Replace label with name + distance
                    e.label          = wp.name + " " + (int)e.dist + "m";
                    e.labelWidth     = client.textRenderer.getWidth(e.label);
                    e.onScreenScale  = Math.max(0.1f, wp.onScreenScale);
                    e.offScreenScale = Math.max(0.1f, wp.offScreenScale);
                    if (e.onScreen || BetterPrisonsClient.config.waypointCustomEdgeEnabled)
                        entries.add(e);
                }
            }
        }

        if (waypointsEnabled && inBadlands && BetterPrisonsClient.config.waypointBanditRushEnabled) {
            for (EventsHud.BanditRushInfo b : BetterPrisonsClient.eventsHud.getVisibleBanditRushes()) {
                int color = BetterPrisonsClient.config.banditRushHeadingColor;
                Entry e = buildEntry(client, b.x, b.y, b.z, color, b.iconStack, screenW, screenH, EntryType.METEOR);
                if (e != null && (e.onScreen || BetterPrisonsClient.config.waypointBanditRushEdgeEnabled))
                    entries.add(e);
            }
        }

        if (BetterPrisonsClient.config.gangPingEnabled || BetterPrisonsClient.config.trucePingEnabled) {
            String currentWorld = WaypointManager.detectWorldKey();
            float baseAlpha = BetterPrisonsClient.config.gangPingBaseOpacity / 255f;
            for (GangPingManager.GangPingInfo ping : BetterPrisonsClient.gangPingManager.getActivePings()) {
                if (ping.isTruce && !BetterPrisonsClient.config.trucePingEnabled) continue;
                if (!ping.isTruce && !BetterPrisonsClient.config.gangPingEnabled) continue;
                if (!ping.world.equals(currentWorld)) continue;
                int pingColor = ping.isTruce ? BetterPrisonsClient.config.trucePingColor : BetterPrisonsClient.config.gangPingColor;
                Entry e = buildEntry(client, ping.x, ping.y, ping.z,
                        pingColor, null, screenW, screenH, EntryType.GANG_PING);
                if (e != null) {
                    e.label = ping.playerName;
                    e.labelWidth = client.textRenderer.getWidth(e.label);
                    e.gangPing = ping;
                    float scale = GangPingManager.calculateScale((float) e.dist,
                            BetterPrisonsClient.config.gangPingIconMinScale,
                            BetterPrisonsClient.config.gangPingIconMaxScale,
                            BetterPrisonsClient.config.gangPingDistanceScaling);
                    e.onScreenScale = scale;
                    e.offScreenScale = 0.5f;
                    e.gangPingAlpha = GangPingManager.calculateOpacity((float) e.dist, baseAlpha);
                    if (e.gangPingAlpha > 0.01f
                            && (e.onScreen || BetterPrisonsClient.config.gangPingEdgeEnabled))
                        entries.add(e);
                }
            }
        }

        // --- Sort nearest-first so closer waypoints get priority placement ---
        entries.sort((a, b) -> Double.compare(a.dist, b.dist));

        // --- Place with collision avoidance, then render ---
        List<int[]> placed = new ArrayList<>();  // [x1, y1, x2, y2]
        for (Entry e : entries) {
            resolveCollision(e, placed, screenW, screenH);
            placed.add(boundingRect(e.ix, e.iy, e.labelWidth, e.scale()));
            drawEntry(ctx, client, e);
        }
    }

    // ----------------------------------------------------------------
    // Build an Entry from raw waypoint data. Returns null if not ready.
    // ----------------------------------------------------------------

    private static Entry buildEntry(MinecraftClient client,
                                     int wx, int wy, int wz, int rgb, ItemStack iconStack,
                                     int screenW, int screenH, EntryType type) {
        float[] pos = WorldSpaceTransform.worldToScreen(wx + 0.5, wy, wz + 0.5, screenW, screenH);
        if (pos == null) return null;

        double dx = wx + 0.5 - WorldSpaceTransform.getCamX();
        double dy = wy       - WorldSpaceTransform.getCamY();
        double dz = wz + 0.5 - WorldSpaceTransform.getCamZ();

        Entry e = new Entry();
        e.wx = wx; e.wy = wy; e.wz = wz;
        e.rgb = rgb;
        e.iconStack = iconStack;
        e.type = type;
        e.projX = pos[0];
        e.projY = pos[1];
        e.dist  = Math.sqrt(dx * dx + dy * dy + dz * dz);
        e.label = (int) e.dist + "m";
        e.labelWidth = client.textRenderer.getWidth(e.label);

        boolean offLeft   = e.projX < EDGE_MARGIN;
        boolean offRight  = e.projX > screenW - EDGE_MARGIN;
        boolean offTop    = e.projY < EDGE_MARGIN;
        boolean offBottom = e.projY > screenH - EDGE_MARGIN;
        e.onScreen = !offLeft && !offRight && !offTop && !offBottom;

        // Initial clamped position
        float clampedX;
        if (!e.onScreen && !offLeft && !offRight) {
            // Purely above/below → snap to nearest left/right edge
            clampedX = e.projX < screenW / 2f ? EDGE_MARGIN : screenW - EDGE_MARGIN;
        } else {
            clampedX = Math.max(EDGE_MARGIN, Math.min(screenW - EDGE_MARGIN, e.projX));
        }
        float clampedY = Math.max(EDGE_MARGIN, Math.min(screenH - EDGE_MARGIN, e.projY));

        e.ix = (int) clampedX;
        e.iy = (int) clampedY;
        return e;
    }

    // ----------------------------------------------------------------
    // Collision avoidance — nudge e.iy (and optionally e.ix) until clear
    // ----------------------------------------------------------------

    private static void resolveCollision(Entry e, List<int[]> placed, int screenW, int screenH) {
        if (placed.isEmpty()) return;

        int baseX = e.ix;
        int baseY = e.iy;

        // For off-screen (edge) markers, only nudge in Y so they stay on the edge.
        // For on-screen markers, try Y nudges first, then Y+X if needed.
        for (int xi = 0; xi <= (e.onScreen ? MAX_NUDGES : 0); xi++) {
            int xOff = nudgeOffset(xi) * NUDGE_STEP;
            for (int yi = 0; yi < MAX_NUDGES * 2; yi++) {
                int yOff = nudgeOffset(yi) * NUDGE_STEP;
                int tryX = clampX(baseX + xOff, screenW);
                int tryY = clampY(baseY + yOff, screenH);
                if (!overlapsAny(boundingRect(tryX, tryY, e.labelWidth, e.scale()), placed)) {
                    e.ix = tryX;
                    e.iy = tryY;
                    return;
                }
            }
        }
        // All attempts failed — keep original position (rare with ≤16 waypoints)
    }

    /** Converts index → signed offset: 0→0, 1→+1, 2→−1, 3→+2, 4→−2, … */
    private static int nudgeOffset(int i) {
        if (i == 0) return 0;
        return (i % 2 == 1) ? (i + 1) / 2 : -(i / 2);
    }

    private static int clampX(int x, int screenW) {
        return Math.max(EDGE_MARGIN, Math.min(screenW - EDGE_MARGIN, x));
    }

    private static int clampY(int y, int screenH) {
        return Math.max(EDGE_MARGIN, Math.min(screenH - EDGE_MARGIN, y));
    }

    /** Bounding rect for a marker centered at (cx, cy) with the given label pixel width. */
    private static int[] boundingRect(int cx, int cy, int labelWidth, float scale) {
        int scaledHalf = Math.max(1, (int)(ICON_HALF * scale));
        int halfW = Math.max(scaledHalf, labelWidth / 2) + 2;
        return new int[]{
            cx - halfW,
            cy - scaledHalf,
            cx + halfW,
            cy + scaledHalf + 2 + LABEL_H
        };
    }

    private static boolean overlapsAny(int[] r, List<int[]> placed) {
        for (int[] p : placed) {
            if (r[0] < p[2] && r[2] > p[0] && r[1] < p[3] && r[3] > p[1]) return true;
        }
        return false;
    }

    // ----------------------------------------------------------------
    // Render a single placed entry
    // ----------------------------------------------------------------

    private static void drawEntry(DrawContext ctx, MinecraftClient client, Entry e) {
        int   ix    = e.ix;
        int   iy    = e.iy;
        float scale = e.scale();
        int   scaledHalf = Math.max(1, (int)(ICON_HALF * scale));

        // Push matrix, translate to icon center, apply scale
        var ms = ctx.getMatrices();
        ms.pushMatrix();
        ms.translate(ix, iy);
        ms.scale(scale, scale);

        if (e.type == EntryType.GANG_PING && e.gangPing != null) {
            // Gang ping: draw player head skin with distance-based opacity
            drawPlayerHead(ctx, client, e.gangPing, e.gangPingAlpha);
        } else if (e.iconStack != null && !e.iconStack.isEmpty()) {
            // Item sprite for events
            ctx.drawItem(e.iconStack, -ICON_HALF, -ICON_HALF);
        } else {
            // Filled colored square (10×10) with a thin dark border
            int fill = 0xFF000000 | (e.rgb & 0xFFFFFF);
            ctx.fill(-5, -5, 5, 5, fill);
            ctx.fill(-6, -6, 6, -5, 0x80000000);
            ctx.fill(-6,  5, 6,  6, 0x80000000);
            ctx.fill(-6, -5, -5, 5, 0x80000000);
            ctx.fill( 5, -5,  6, 5, 0x80000000);
        }

        ms.popMatrix();

        // Off-screen: arrow badge pointing toward the projected waypoint.
        // Arrow offset scales with the icon so it stays flush to its edge.
        if (!e.onScreen) {
            double arrowAngle  = Math.atan2(e.projY - iy, e.projX - ix);
            int    arrowOffset = Math.max(ARROW_OFFSET, scaledHalf + 3);
            int    argb        = 0xFF000000 | (e.rgb & 0xFFFFFF);
            int    arrowCX     = ix + (int)(arrowOffset * Math.cos(arrowAngle));
            int    arrowCY     = iy + (int)(arrowOffset * Math.sin(arrowAngle));
            drawArrow(ctx, arrowCX, arrowCY, arrowAngle, ARROW_RADIUS, argb);
        }

        // Label just below the (scaled) icon — always drawn at 1:1 size
        int textArgb = 0xFF000000 | (e.rgb & 0xFFFFFF);

        if (e.type == EntryType.GANG_PING && e.gangPing != null) {
            // Collect visible text lines — off-screen only shows name
            List<String> lines = new ArrayList<>();
            if (BetterPrisonsClient.config.gangPingShowName)
                lines.add(e.label);
            if (e.onScreen) {
                if (BetterPrisonsClient.config.gangPingShowTimer) {
                    long elapsed = (System.currentTimeMillis() - e.gangPing.createdAt) / 1000L;
                    lines.add(elapsed + "s ago");
                }
                if (BetterPrisonsClient.config.gangPingShowCoords)
                    lines.add(e.gangPing.x + ", " + e.gangPing.y + ", " + e.gangPing.z + " (" + (int) e.dist + "m)");
                if (BetterPrisonsClient.config.gangPingShowHp)
                    lines.add("HP: " + (int) e.gangPing.hp + "/" + (int) e.gangPing.maxHp);
                if (BetterPrisonsClient.config.gangPingShowFacing)
                    lines.add("Facing: " + e.gangPing.facing);
            }

            if (!lines.isEmpty()) {
                float textScale = Math.max(0.5f, BetterPrisonsClient.config.gangPingTextScale);
                int bgPad = 2;
                int lineSpacing = (int)((LABEL_H + bgPad * 2 + 1) * textScale);
                int bgColor = (BetterPrisonsClient.config.cooldownBgOpacity << 24)
                            | (BetterPrisonsClient.config.cooldownBgColor & 0xFFFFFF);

                int startY = iy + scaledHalf + 2;
                int lineY = startY;
                var textMs = ctx.getMatrices();
                for (String line : lines) {
                    int w = client.textRenderer.getWidth(line);
                    int scaledW = (int)(w * textScale);
                    int scaledH = (int)(LABEL_H * textScale);
                    // Background at screen scale
                    ctx.fill(ix - scaledW / 2 - bgPad, lineY - bgPad,
                             ix + (scaledW + 1) / 2 + bgPad, lineY + scaledH + bgPad, bgColor);
                    // Text with scale
                    textMs.pushMatrix();
                    textMs.translate(ix, lineY);
                    textMs.scale(textScale, textScale);
                    ctx.drawTextWithShadow(client.textRenderer, Text.literal(line),
                        -w / 2, 0, textArgb);
                    textMs.popMatrix();
                    lineY += lineSpacing;
                }
            }
        } else {
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(e.label),
                ix - e.labelWidth / 2, iy + scaledHalf + 2, textArgb);
        }
    }

    /** Draws a player head (face + hat overlay) centered at (0,0) in the current matrix.
     *  Alpha is applied via the ARGB color parameter on PlayerSkinDrawer.draw(). */
    private static void drawPlayerHead(DrawContext ctx, MinecraftClient client,
                                        GangPingManager.GangPingInfo ping, float alpha) {
        int color = BetterPrisonsClient.config.gangPingColor;
        int alphaInt = Math.max(0, Math.min(255, (int)(alpha * 255)));
        int borderArgb = (alphaInt << 24) | (color & 0xFFFFFF);
        // White tint with our alpha for the skin texture
        int skinArgb = (alphaInt << 24) | 0x00FFFFFF;

        SkinTextures skinTextures = null;
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(ping.playerName);
            if (entry != null) {
                skinTextures = entry.getSkinTextures();
            }
        }

        // Draw colored border behind the head to indicate the ping color
        ctx.fill(-9, -9, 9, 9, borderArgb);

        if (skinTextures != null) {
            int size = 16;
            PlayerSkinDrawer.draw(ctx, skinTextures, -size / 2, -size / 2, size, skinArgb);
        } else {
            // Fallback: colored square with first letter of player name
            ctx.fill(-8, -8, 8, 8, borderArgb);
            String initial = ping.playerName.substring(0, 1).toUpperCase();
            int textW = client.textRenderer.getWidth(initial);
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(initial),
                    -textW / 2, -4, 0xFFFFFFFF);
        }
    }

    // ----------------------------------------------------------------
    // Drawing helpers
    // ----------------------------------------------------------------

    private static void drawArrow(DrawContext ctx, int cx, int cy, double angle, int r, int color) {
        int tipX = cx + (int) (r * Math.cos(angle));
        int tipY = cy + (int) (r * Math.sin(angle));

        double perpAngle    = angle + Math.PI / 2;
        int    baseHalfWidth = r - 1;
        int    baseX = cx - (int) ((r / 2.0) * Math.cos(angle));
        int    baseY = cy - (int) ((r / 2.0) * Math.sin(angle));
        int b1x = baseX + (int) (baseHalfWidth * Math.cos(perpAngle));
        int b1y = baseY + (int) (baseHalfWidth * Math.sin(perpAngle));
        int b2x = baseX - (int) (baseHalfWidth * Math.cos(perpAngle));
        int b2y = baseY - (int) (baseHalfWidth * Math.sin(perpAngle));

        fillTriangle(ctx, tipX, tipY, b1x, b1y, b2x, b2y, color);
    }

    private static void fillTriangle(DrawContext ctx,
                                      int x0, int y0, int x1, int y1, int x2, int y2,
                                      int color) {
        int minX = Math.min(x0, Math.min(x1, x2));
        int maxX = Math.max(x0, Math.max(x1, x2));
        int minY = Math.min(y0, Math.min(y1, y2));
        int maxY = Math.max(y0, Math.max(y1, y2));

        int denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
        if (denom == 0) return;

        for (int py = minY; py <= maxY; py++) {
            for (int px = minX; px <= maxX; px++) {
                int w0 = (y1 - y2) * (px - x2) + (x2 - x1) * (py - y2);
                int w1 = (y2 - y0) * (px - x2) + (x0 - x2) * (py - y2);
                int w2 = denom - w0 - w1;
                if (denom > 0 ? (w0 >= 0 && w1 >= 0 && w2 >= 0)
                              : (w0 <= 0 && w1 <= 0 && w2 <= 0)) {
                    ctx.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }
}
