package BetterPrisons.modid.render;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.gangping.GangPingManager;
import BetterPrisons.modid.hud.EventsHud;
import BetterPrisons.modid.waypoint.CustomWaypoint;
import BetterPrisons.modid.waypoint.WaypointManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renders a translucent vertical beacon beam in 3D world space at each active
 * meteor / merchant location. The beam uses a custom RenderPipeline with
 * DepthTestFunction.NO_DEPTH_TEST so it renders visible through all blocks.
 */
public class BeaconBeamRenderer {

    private static final float BEAM_HALF_WIDTH  = 0.15f;
    // Distance scale factor: angular half-width = atan(w/dist); this keeps
    // the beam at ~5 screen pixels wide at typical FOV across all distances.
    private static final float BEAM_DIST_SCALE = 0.005f;
    private static final int   BEAM_HEIGHT     = 250; // blocks above the waypoint Y

    /** POSITION_COLOR pipeline with depth test disabled and culling off — renders through all blocks. */
    private static final RenderPipeline BEACON_NO_DEPTH_PIPELINE =
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("betterprisons", "pipeline/beacon_beam_no_depth"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build();

    private static final RenderLayer BEACON_NO_DEPTH_LAYER = RenderLayer.of(
        "betterprisons_beacon_no_depth",
        RenderSetup.builder(BEACON_NO_DEPTH_PIPELINE).translucent().build()
    );

    /**
     * POSITION_COLOR pipeline with standard depth test and culling off — respects block occlusion
     * but uses the same fog-free shader as the no-depth layer so the beam stays visible at any
     * distance. Culling disabled so all faces render regardless of camera angle.
     */
    private static final RenderPipeline BEACON_DEPTH_PIPELINE =
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("betterprisons", "pipeline/beacon_beam_depth"))
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withCull(false)
            .build();

    private static final RenderLayer BEACON_DEPTH_LAYER = RenderLayer.of(
        "betterprisons_beacon_depth",
        RenderSetup.builder(BEACON_DEPTH_PIPELINE).translucent().build()
    );

    public static void register() {
        WorldRenderEvents.END_MAIN.register(BeaconBeamRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        if (!BetterPrisonsClient.config.waypointsEnabled) return;
        if (!BetterPrisonsClient.config.beaconBeamsEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        MatrixStack matrices = ctx.matrices();
        if (matrices == null) return;

        OrderedRenderCommandQueue commandQueue = ctx.commandQueue();
        if (commandQueue == null) return;

        Vec3d camera = client.gameRenderer.getCamera().getCameraPos();

        boolean inOverworld = client.world != null
            && "minecraft:overworld".equals(client.world.getRegistryKey().getValue().toString());

        if (inOverworld && BetterPrisonsClient.config.waypointMeteorsEnabled) {
            int opacity = BetterPrisonsClient.config.meteorBeamOpacity;
            List<EventsHud.MeteorInfo> meteors = BetterPrisonsClient.eventsHud.getActiveMeteors();
            for (EventsHud.MeteorInfo m : meteors) {
                int color = (m.type == EventsHud.MeteorType.NATURAL)
                    ? BetterPrisonsClient.config.eventsNaturalHeadingColor
                    : BetterPrisonsClient.config.eventsSummonedHeadingColor;
                submitBeam(matrices, commandQueue, client, camera, m.x, m.y, m.z, color, opacity);
            }
        }

        if (inOverworld && BetterPrisonsClient.config.waypointMerchantsEnabled) {
            int opacity = BetterPrisonsClient.config.merchantBeamOpacity;
            List<EventsHud.MerchantInfo> merchants =
                BetterPrisonsClient.eventsHud.getVisibleMerchantsForWaypoints();
            for (EventsHud.MerchantInfo m : merchants) {
                int color = m.type.getHeadingColor(BetterPrisonsClient.config);
                submitBeam(matrices, commandQueue, client, camera, m.x, m.y, m.z, color, opacity);
            }
        }

        if (BetterPrisonsClient.config.waypointCustomEnabled) {
            for (CustomWaypoint wp : BetterPrisonsClient.waypointManager.getEnabled()) {
                submitBeam(matrices, commandQueue, client, camera, wp.x, wp.y, wp.z, wp.color, wp.opacity);
            }
        }

        if (BetterPrisonsClient.config.gangPingBeamEnabled
                && (BetterPrisonsClient.config.gangPingEnabled || BetterPrisonsClient.config.trucePingEnabled)) {
            String currentWorld = WaypointManager.detectWorldKey();
            int opacity = BetterPrisonsClient.config.gangPingBeamOpacity;
            for (GangPingManager.GangPingInfo ping : BetterPrisonsClient.gangPingManager.getActivePings()) {
                if (ping.isTruce && !BetterPrisonsClient.config.trucePingEnabled) continue;
                if (!ping.isTruce && !BetterPrisonsClient.config.gangPingEnabled) continue;
                if (!ping.world.equals(currentWorld)) continue;
                int color = ping.isTruce ? BetterPrisonsClient.config.trucePingColor : BetterPrisonsClient.config.gangPingColor;
                submitBeam(matrices, commandQueue, client, camera,
                        ping.x, ping.y, ping.z, color, opacity);
            }
        }
    }

    private static void submitBeam(MatrixStack matrices, OrderedRenderCommandQueue commandQueue,
                                    MinecraftClient client, Vec3d camera,
                                    int wx, int wy, int wz, int rgb, int opacityInt) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float a = Math.max(0, Math.min(255, opacityInt)) / 255f;

        double dx = wx + 0.5 - camera.x;
        double dz = wz + 0.5 - camera.z;
        // Ignore event Y — always anchor beam at world Y=0 so it spans the full
        // vertical range (Y=0 to Y=512) and is visible from any altitude.
        double dy = -camera.y;

        double horizDist = Math.sqrt(dx * dx + dz * dz);
        // effectiveHorizDist tracks the distance at which the beam will actually render
        // (may be less than horizDist when the through-walls cap moves it closer).
        double effectiveHorizDist = horizDist;

        boolean throughWalls = BetterPrisonsClient.config.beaconBeamThroughWalls;

        if (throughWalls) {
            // When rendering through walls, cap to stay inside the fog-free zone so the
            // beam is always fully visible. Fog starts at ~(renderDistance-2)*16 blocks.
            int renderChunks = client.options.getViewDistance().getValue();
            double maxHorizDist = Math.max(16.0, (renderChunks - 4) * 16.0);
            if (horizDist > maxHorizDist) {
                double scale = maxHorizDist / horizDist;
                dx *= scale;
                dz *= scale;
                effectiveHorizDist = maxHorizDist;
            }
        }
        // When NOT through walls, do NOT cap. Beyond render distance there is no loaded
        // terrain geometry, so the depth test passes and the beam is naturally visible.
        // Capping would move the beam into loaded terrain which would occlude it.

        // Scale beam width with effective render distance so the beam stays ~5px wide
        // on screen regardless of how far away it is.
        float beamHalfWidth = Math.max(BEAM_HALF_WIDTH,
            (float) effectiveHorizDist * BEAM_DIST_SCALE);

        final double fdx = dx, fdy = dy, fdz = dz;
        final float  fw  = beamHalfWidth;
        matrices.push();
        matrices.translate(fdx, fdy, fdz);

        RenderLayer layer = throughWalls ? BEACON_NO_DEPTH_LAYER : BEACON_DEPTH_LAYER;
        commandQueue.submitCustom(matrices, layer, (entry, consumer) ->
            drawBeamVertices(entry.getPositionMatrix(), consumer, r, g, b, a, fw));

        matrices.pop();
    }

    private static void drawBeamVertices(Matrix4f mat, VertexConsumer consumer,
                                          float r, float g, float b, float a, float w) {
        float h = BEAM_HEIGHT;

        // North face
        consumer.vertex(mat,  w, 0, -w).color(r, g, b, a);
        consumer.vertex(mat,  w, h, -w).color(r, g, b, a);
        consumer.vertex(mat, -w, h, -w).color(r, g, b, a);
        consumer.vertex(mat, -w, 0, -w).color(r, g, b, a);

        // South face
        consumer.vertex(mat, -w, 0, w).color(r, g, b, a);
        consumer.vertex(mat, -w, h, w).color(r, g, b, a);
        consumer.vertex(mat,  w, h, w).color(r, g, b, a);
        consumer.vertex(mat,  w, 0, w).color(r, g, b, a);

        // West face
        consumer.vertex(mat, -w, 0, -w).color(r, g, b, a);
        consumer.vertex(mat, -w, h, -w).color(r, g, b, a);
        consumer.vertex(mat, -w, h,  w).color(r, g, b, a);
        consumer.vertex(mat, -w, 0,  w).color(r, g, b, a);

        // East face
        consumer.vertex(mat,  w, 0,  w).color(r, g, b, a);
        consumer.vertex(mat,  w, h,  w).color(r, g, b, a);
        consumer.vertex(mat,  w, h, -w).color(r, g, b, a);
        consumer.vertex(mat,  w, 0, -w).color(r, g, b, a);
    }
}
