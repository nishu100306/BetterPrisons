package BetterPrisons.modid.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/**
 * Snaps camera state each frame at WorldRenderEvents.END_MAIN so that the 2D
 * waypoint overlay can project using values that are consistent with what was
 * actually rendered that frame.
 *
 * FOV capture (zoom-mod safe):
 *   GameRendererMixin injects into getFov(Camera, float, boolean) at RETURN and
 *   calls captureFov() only when changingFov=true (the main world render call).
 *   Hand rendering uses changingFov=false — Zoomify un-zooms that call — so it
 *   is ignored and cannot overwrite the correct zoomed value.
 */
public class WorldSpaceTransform {

    private static double camX, camY, camZ;
    private static double sinYaw, cosYaw, sinPitch, cosPitch;
    private static float  fovDeg = 0;
    private static boolean ready = false;

    public static void register() {
        WorldRenderEvents.END_MAIN.register(WorldSpaceTransform::capture);
    }

    private static void capture(WorldRenderContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Camera camera = client.gameRenderer.getCamera();

        double yawRad   = Math.toRadians(camera.getYaw());
        double pitchRad = Math.toRadians(camera.getPitch());
        sinYaw   = Math.sin(yawRad);
        cosYaw   = Math.cos(yawRad);
        sinPitch = Math.sin(pitchRad);
        cosPitch = Math.cos(pitchRad);

        Vec3d cam = camera.getCameraPos();
        camX = cam.x;
        camY = cam.y;
        camZ = cam.z;

        // fovDeg is written by captureFov() from GameRendererMixin each frame.
        // Fall back to the base option value only before the first frame.
        if (fovDeg <= 0) {
            fovDeg = client.options.getFov().getValue();
        }
        ready = true;
    }

    /**
     * Called by GameRendererMixin when getFov(changingFov=true) returns.
     * That is the main world-render FOV, already modified by zoom mods.
     */
    public static void captureFov(float fov) {
        if (fov > 0) {
            fovDeg = fov;
        }
    }

    /**
     * Projects a world position to GUI-scaled screen coordinates.
     *
     * @return float[]{screenX, screenY, camFwd}
     *         camFwd > 0  → in front of camera (may still be off-screen)
     *         camFwd <= 0 → behind camera (coords pushed far off-screen in the
     *                       correct lateral direction so clamped arrows point right)
     *         null → not yet captured (first frame only)
     */
    public static float[] worldToScreen(double wx, double wy, double wz,
                                         int screenW, int screenH) {
        if (!ready) return null;

        double dx = wx - camX;
        double dy = wy - camY;
        double dz = wz - camZ;

        double camRight = -cosYaw * dx                          - sinYaw * dz;
        double camUp    = -sinPitch * sinYaw * dx + cosPitch * dy + sinPitch * cosYaw * dz;
        double camFwd   = -sinYaw * cosPitch * dx - sinPitch * dy + cosYaw * cosPitch * dz;

        double focal = (screenH / 2.0) / Math.tan(Math.toRadians(fovDeg / 2.0));
        int cx = screenW / 2;
        int cy = screenH / 2;

        float projX, projY;
        if (camFwd > 1e-4) {
            projX = (float)(cx + (camRight / camFwd) * focal);
            projY = (float)(cy - (camUp    / camFwd) * focal);
        } else {
            double lateralMag = Math.sqrt(camRight * camRight + camUp * camUp);
            if (lateralMag < 1e-6) {
                projX = cx + screenW * 4f;
                projY = cy;
            } else {
                projX = (float)(cx + (camRight / lateralMag) * screenW * 4);
                projY = (float)(cy - (camUp    / lateralMag) * screenH * 4);
            }
        }

        return new float[]{projX, projY, (float) camFwd};
    }

    public static double getCamX() { return camX; }
    public static double getCamY() { return camY; }
    public static double getCamZ() { return camZ; }
}
