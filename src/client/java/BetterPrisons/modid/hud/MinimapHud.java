package BetterPrisons.modid.hud;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.hud.EventsHud.MerchantInfo;
import BetterPrisons.modid.hud.EventsHud.MeteorInfo;
import BetterPrisons.modid.waypoint.CustomWaypoint;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Minimap HUD — renders a top-down terrain overview around the player.
 *
 * Terrain is sampled from loaded chunk data (surface heightmap + MapColor)
 * and cached every REBUILD_TICKS ticks.  Entity and waypoint dots are
 * re-projected each frame from live world positions.
 *
 * Two orientation modes:
 *   • North-up (rotating=false): north always points to the top of the map.
 *     The player arrow rotates to show the player's heading.
 *   • Rotating  (rotating=true) : the player's heading always points up.
 *     A small "N" indicator marks north on the map edge.
 */
public class MinimapHud extends BaseHud {

    // ── config (read from Config each render) ─────────────────────────────
    public int     mapSize        = 128;   // pixel diameter of the map widget
    public int     pixelsPerBlock = 1;     // zoom: 1px = 1 block, 2px = 1 block …
    public boolean circleShape    = true;
    public boolean rotating       = false; // false = north-up
    public boolean showWaypoints  = true;
    public boolean showCoords     = true;
    public int     borderColor    = 0xFFFFFF;
    public int     borderOpacity  = 220;
    public int     borderThickness = 2;

    // ── block colour cache ────────────────────────────────────────────────
    // Indexed [(dz + cacheRadius) * cacheSize + (dx + cacheRadius)]
    // Values are packed ARGB.
    private int[] colorCache;
    private int   cacheRadius;   // blocks in each direction
    private int   cacheSize;     // cacheRadius*2 + 1
    private int   cacheCenterX, cacheCenterZ;

    private int tickTimer = 0;
    private static final int REBUILD_TICKS = 10; // ≈ 0.5 s at 20 TPS

    // ── static colour constants ───────────────────────────────────────────
    private static final int COL_UNLOADED   = 0xFF111111;
    private static final int COL_PLAYER     = 0xFFFFFF00; // yellow
    private static final int COL_PLAYER_OUT = 0xFF000000; // arrow outline
    private static final int COL_METEOR     = 0xFFFF6600;
    private static final int COL_MERCHANT   = 0xFF00FF88;

    // ─────────────────────────────────────────────────────────────────────
    public MinimapHud() {
        super("minimap");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Tick – rebuild terrain cache
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void tick(MinecraftClient client) {
        if (!enabled || client.world == null || client.player == null) return;
        if (++tickTimer < REBUILD_TICKS) return;
        tickTimer = 0;

        int px = MathHelper.floor(client.player.getX());
        int pz = MathHelper.floor(client.player.getZ());

        // In rotating mode the visible area is a rotated square; pad for diagonal.
        int halfPixels = mapSize / 2;
        int blockRadius = rotating
                ? (int) Math.ceil(halfPixels * Math.sqrt(2.0) / pixelsPerBlock) + 2
                : halfPixels / pixelsPerBlock + 2;

        rebuildCache(client.world, px, pz, blockRadius);
    }

    private void rebuildCache(ClientWorld world, int cx, int cz, int radius) {
        int size = radius * 2 + 1;
        if (colorCache == null || cacheRadius != radius) {
            colorCache = new int[size * size];
            cacheRadius = radius;
            cacheSize   = size;
        }
        cacheCenterX = cx;
        cacheCenterZ = cz;

        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                colorCache[(dz + radius) * size + (dx + radius)] =
                        sampleBlock(world, cx + dx, cz + dz);
            }
        }
    }

    private int sampleBlock(ClientWorld world, int wx, int wz) {
        WorldChunk chunk = world.getChunkManager().getWorldChunk(wx >> 4, wz >> 4, false);
        if (chunk == null || chunk.isEmpty()) return COL_UNLOADED;

        int lx = wx & 15, lz = wz & 15;
        int surfaceY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lx, lz);
        if (surfaceY <= world.getBottomY()) return COL_UNLOADED;

        BlockPos pos = new BlockPos(wx, surfaceY - 1, wz);
        MapColor mc = chunk.getBlockState(pos).getMapColor(world, pos);
        if (mc == MapColor.CLEAR) {
            pos = pos.down();
            mc = chunk.getBlockState(pos).getMapColor(world, pos);
        }

        int brightness = MapColor.Brightness.NORMAL.brightness;
        int rgb = mc.color;
        int r = ((rgb >> 16) & 0xFF) * brightness / 255;
        int g = ((rgb >>  8) & 0xFF) * brightness / 255;
        int b =  (rgb        & 0xFF) * brightness / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Render
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, MinecraftClient client) {
        if (!enabled || client.player == null || client.world == null) return;

        // Re-read scale from config each frame (same pattern as EventsHud / CooldownHud).
        this.scale = BetterPrisonsClient.config.minimapScale / 100.0f;

        int size   = scaled(mapSize);
        int radius = size / 2;
        int cx     = x + radius;
        int cy     = y + radius;
        float rSq  = (float) radius * radius;

        // ── Background ────────────────────────────────────────────────────
        int bgArgb = 0xB4000000; // ~70 % opaque black
        if (circleShape) {
            drawFilledCircle(ctx, cx, cy, radius, bgArgb);
        } else {
            ctx.fill(x, y, x + size, y + size, bgArgb);
        }

        // ── Terrain ───────────────────────────────────────────────────────
        if (colorCache != null) {
            renderTerrain(ctx, client, cx, cy, size, radius, rSq);
        }

        // ── Waypoints ─────────────────────────────────────────────────────
        if (showWaypoints && BetterPrisonsClient.config.waypointsEnabled) {
            renderWaypoints(ctx, client, cx, cy, radius);
        }

        // ── Player arrow ──────────────────────────────────────────────────
        renderPlayerArrow(ctx, client, cx, cy);

        // ── North indicator (rotating mode only) ──────────────────────────
        if (rotating) {
            renderNorthDot(ctx, client, cx, cy, radius);
        }

        // ── Border ────────────────────────────────────────────────────────
        int borderArgb = ((borderOpacity & 0xFF) << 24) | (borderColor & 0xFFFFFF);
        if (circleShape) {
            drawCircleBorder(ctx, cx, cy, radius, borderThickness, borderArgb);
        } else {
            int t = borderThickness;
            ctx.fill(x,            y,            x + size,     y + t,         borderArgb);
            ctx.fill(x,            y + size - t, x + size,     y + size,      borderArgb);
            ctx.fill(x,            y + t,        x + t,        y + size - t,  borderArgb);
            ctx.fill(x + size - t, y + t,        x + size,     y + size - t,  borderArgb);
        }

        // ── Coordinates ───────────────────────────────────────────────────
        if (showCoords) {
            int bx = MathHelper.floor(client.player.getX());
            int by = MathHelper.floor(client.player.getY());
            int bz = MathHelper.floor(client.player.getZ());
            String coords = bx + ", " + by + ", " + bz;
            int tw = client.textRenderer.getWidth(coords);
            int tx = cx - tw / 2;
            int ty = y + size + 2;
            ctx.fill(tx - 1, ty - 1, tx + tw + 1, ty + 9, 0x88000000);
            ctx.drawTextWithShadow(client.textRenderer, coords, tx, ty, 0xFFFFFF);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Terrain rendering
    // ─────────────────────────────────────────────────────────────────────

    private void renderTerrain(DrawContext ctx, MinecraftClient client,
                                int cx, int cy, int size, int radius, float rSq) {
        int playerX = MathHelper.floor(client.player.getX());
        int playerZ = MathHelper.floor(client.player.getZ());

        float yawRad = rotating ? (float) Math.toRadians(client.player.getYaw()) : 0f;
        float cos = (float) Math.cos(yawRad);
        float sin = (float) Math.sin(yawRad);

        int ppb = Math.max(1, pixelsPerBlock);

        for (int py = 0; py < size; py += ppb) {
            for (int px = 0; px < size; px += ppb) {
                // Centre of this cell in screen-offset coords
                float sdx = (px + ppb * 0.5f) - radius;
                float sdz = (py + ppb * 0.5f) - radius;

                if (circleShape && sdx * sdx + sdz * sdz > rSq) continue;

                int blockDx, blockDz;
                if (rotating) {
                    blockDx = Math.round((sdx * cos + sdz * sin) / ppb);
                    blockDz = Math.round((sdx * sin - sdz * cos) / ppb);
                } else {
                    blockDx = Math.round(sdx / ppb);
                    blockDz = Math.round(sdz / ppb);
                }

                int worldX  = playerX + blockDx;
                int worldZ  = playerZ + blockDz;
                int cacheDx = worldX - cacheCenterX;
                int cacheDz = worldZ - cacheCenterZ;

                int color = COL_UNLOADED;
                if (Math.abs(cacheDx) <= cacheRadius && Math.abs(cacheDz) <= cacheRadius) {
                    color = colorCache[(cacheDz + cacheRadius) * cacheSize + (cacheDx + cacheRadius)];
                }

                ctx.fill(x + px, y + py, x + px + ppb, y + py + ppb, color);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Dot overlays
    // ─────────────────────────────────────────────────────────────────────

    private void renderWaypoints(DrawContext ctx, MinecraftClient client,
                                  int cx, int cy, int radius) {
        int px = MathHelper.floor(client.player.getX());
        int pz = MathHelper.floor(client.player.getZ());
        float yaw = rotating ? (float) Math.toRadians(client.player.getYaw()) : 0f;

        if (BetterPrisonsClient.config.waypointMeteorsEnabled) {
            for (MeteorInfo m : BetterPrisonsClient.eventsHud.getActiveMeteors()) {
                String lbl = m.x + ", " + m.y + ", " + m.z;
                plotDot(ctx, client, m.x, m.z, px, pz, cx, cy, radius, yaw, COL_METEOR, lbl);
            }
        }

        if (BetterPrisonsClient.config.waypointMerchantsEnabled) {
            for (MerchantInfo m : BetterPrisonsClient.eventsHud.getActiveMerchants()) {
                if (m.slainTime != null) continue;
                plotDot(ctx, client, m.x, m.z, px, pz, cx, cy, radius, yaw, COL_MERCHANT, null);
            }
        }

        if (BetterPrisonsClient.config.waypointCustomEnabled) {
            for (CustomWaypoint wp : BetterPrisonsClient.waypointManager.getEnabled()) {
                int col = 0xFF000000 | (wp.color & 0xFFFFFF);
                plotDot(ctx, client, wp.x, wp.z, px, pz, cx, cy, radius, yaw, col, wp.name);
            }
        }
    }

    /**
     * Maps a world (wx, wz) coordinate to a screen dot on the minimap.
     * Points outside the map radius are clamped to the edge.
     */
    private void plotDot(DrawContext ctx, MinecraftClient client,
                          int wx, int wz,
                          int playerX, int playerZ,
                          int cx, int cy,
                          int radius, float yawRad,
                          int color, String label) {
        float wdx = wx - playerX;
        float wdz = wz - playerZ;

        float sdx, sdz;
        if (rotating) {
            float cos = (float) Math.cos(yawRad);
            float sin = (float) Math.sin(yawRad);
            // Inverse of terrain sample transform:
            //   blockDx = (sdx*cos + sdz*sin) / ppb
            //   blockDz = (sdx*sin - sdz*cos) / ppb
            // Solving → sdx = wdx*cos + wdz*sin;  sdz = wdx*sin - wdz*cos
            sdx = wdx * cos + wdz * sin;
            sdz = wdx * sin - wdz * cos;
        } else {
            sdx = wdx;
            sdz = wdz;
        }

        // Apply zoom
        sdx *= pixelsPerBlock;
        sdz *= pixelsPerBlock;

        // Clamp to edge (leaving a small margin so the dot is fully visible)
        float edge = radius - 3f;
        float dSq  = sdx * sdx + sdz * sdz;
        if (dSq > edge * edge) {
            float len = (float) Math.sqrt(dSq);
            sdx = sdx / len * edge;
            sdz = sdz / len * edge;
        }

        int dotX = cx + Math.round(sdx);
        int dotY = cy + Math.round(sdz);

        // 3×3 outline + 2×2 coloured fill
        ctx.fill(dotX - 2, dotY - 2, dotX + 3, dotY + 3, COL_PLAYER_OUT);
        ctx.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, color);

        // Label – only when the point is within ~60 % of the radius (not crowded)
        if (label != null && dSq < (radius * 0.6f * pixelsPerBlock) * (radius * 0.6f * pixelsPerBlock)) {
            ctx.drawTextWithShadow(client.textRenderer, label, dotX + 3, dotY - 4, 0xFFFFFF);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Player arrow
    // ─────────────────────────────────────────────────────────────────────

    private void renderPlayerArrow(DrawContext ctx, MinecraftClient client, int cx, int cy) {
        float yawRad = (float) Math.toRadians(client.player.getYaw());

        float fwdX, fwdY;
        if (rotating) {
            fwdX =  0f;  // player always points up in rotating mode
            fwdY = -1f;
        } else {
            // In Minecraft: forward world vector = (-sin(yaw), cos(yaw)) in (X, Z).
            // Screen X = world X, Screen Y = world Z.
            fwdX = -(float) Math.sin(yawRad);
            fwdY =  (float) Math.cos(yawRad);
        }

        // Right-perpendicular of forward
        float perpX =  fwdY;
        float perpY = -fwdX;

        // Triangle: tip (7px ahead) + two base corners (3px behind, 4px to each side)
        int tipX  = cx + Math.round(fwdX  *  7);
        int tipY  = cy + Math.round(fwdY  *  7);
        int blX   = cx + Math.round(-fwdX *  3 + perpX * 4);
        int blY   = cy + Math.round(-fwdY *  3 + perpY * 4);
        int brX   = cx + Math.round(-fwdX *  3 - perpX * 4);
        int brY   = cy + Math.round(-fwdY *  3 - perpY * 4);

        // Filled triangle via scan-line (simple rasterise of the 3 edges)
        fillTriangle(ctx, tipX, tipY, blX, blY, brX, brY, COL_PLAYER_OUT, COL_PLAYER);

        // Centre dot (white core)
        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);
    }

    /** Draws a small north indicator dot at the top edge of the map in rotating mode. */
    private void renderNorthDot(DrawContext ctx, MinecraftClient client, int cx, int cy, int radius) {
        float yawRad = (float) Math.toRadians(client.player.getYaw());
        float cos = (float) Math.cos(yawRad);
        float sin = (float) Math.sin(yawRad);

        // North = (worldDx=0, worldDz=-1).  Map north to screen.
        // sdx = 0*cos + (-1)*sin = -sin;  sdz = 0*sin - (-1)*cos = cos
        float sdx = -sin;
        float sdz =  cos;

        // Place it at the map edge
        float len = (float) Math.sqrt(sdx * sdx + sdz * sdz);
        int nx = cx + Math.round(sdx / len * (radius - 4));
        int ny = cy + Math.round(sdz / len * (radius - 4));

        ctx.fill(nx - 2, ny - 2, nx + 3, ny + 3, 0xFF000000);
        ctx.fill(nx - 1, ny - 1, nx + 2, ny + 2, 0xFFFF0000); // red N dot
        ctx.drawTextWithShadow(client.textRenderer, "N", nx - 2, ny - 9, 0xFFFF0000);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Triangle fill (Bresenham edge + scan-line)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Rasterises a filled triangle using a scan-line approach.
     * The outline colour is drawn first, fill colour on top.
     */
    private void fillTriangle(DrawContext ctx,
                               int x0, int y0, int x1, int y1, int x2, int y2,
                               int outline, int fill) {
        // Sort vertices by Y (top to bottom)
        if (y1 < y0) { int tx=x0,ty=y0; x0=x1;y0=y1;x1=tx;y1=ty; }
        if (y2 < y0) { int tx=x0,ty=y0; x0=x2;y0=y2;x2=tx;y2=ty; }
        if (y2 < y1) { int tx=x1,ty=y1; x1=x2;y1=y2;x2=tx;y2=ty; }

        // Draw 3 outline edges
        drawLine(ctx, x0, y0, x1, y1, outline);
        drawLine(ctx, x1, y1, x2, y2, outline);
        drawLine(ctx, x0, y0, x2, y2, outline);

        // Scan-line fill
        for (int sy = y0; sy <= y2; sy++) {
            float t02 = (y2 == y0) ? 1f : (float)(sy - y0) / (y2 - y0);
            int xa = x0 + Math.round(t02 * (x2 - x0));

            int xb;
            if (sy <= y1) {
                float t01 = (y1 == y0) ? 1f : (float)(sy - y0) / (y1 - y0);
                xb = x0 + Math.round(t01 * (x1 - x0));
            } else {
                float t12 = (y2 == y1) ? 1f : (float)(sy - y1) / (y2 - y1);
                xb = x1 + Math.round(t12 * (x2 - x1));
            }

            int lo = Math.min(xa, xb);
            int hi = Math.max(xa, xb);
            ctx.fill(lo, sy, hi + 1, sy + 1, fill);
        }
    }

    /** Bresenham line draw. */
    private void drawLine(DrawContext ctx, int x0, int y0, int x1, int y1, int color) {
        int dx =  Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            ctx.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Circle helpers
    // ─────────────────────────────────────────────────────────────────────

    private void drawFilledCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int) Math.sqrt((double)(r * r - dy * dy));
            ctx.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, color);
        }
    }

    private void drawCircleBorder(DrawContext ctx, int cx, int cy, int r, int t, int color) {
        int inner = r - t;
        for (int dy = -r; dy <= r; dy++) {
            int outerHW = (int) Math.sqrt((double)(r * r - dy * dy));
            int innerHW = (dy * dy <= inner * inner)
                    ? (int) Math.sqrt((double)(inner * inner - dy * dy))
                    : 0;
            ctx.fill(cx - outerHW, cy + dy, cx - innerHW,     cy + dy + 1, color);
            ctx.fill(cx + innerHW, cy + dy, cx + outerHW + 1, cy + dy + 1, color);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // BaseHud bounds (for HudEditorScreen drag handles)
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public int getWidth() {
        return scaled(mapSize);
    }

    @Override
    public int getHeight() {
        int extra = showCoords ? 11 : 0;
        return scaled(mapSize) + extra;
    }
}
