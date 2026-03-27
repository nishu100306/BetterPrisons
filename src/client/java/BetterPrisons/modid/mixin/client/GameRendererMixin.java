package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.render.WorldSpaceTransform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Capture the effective FOV for the main world render pass so that
     * WorldSpaceTransform can project waypoints correctly under any zoom mod.
     *
     * Why @ModifyArg on renderWorld at getProjectionMatrix instead of @Inject on getFov:
     *   Zoomify uses MixinExtras @ModifyReturnValue on getFov, which may be ordered
     *   AFTER our @Inject at RETURN. If so, our inject reads the un-zoomed value.
     *   Sprint FOV works because it modifies getFov's body directly — not via
     *   @ModifyReturnValue — so it's visible to any @Inject at RETURN.
     *
     *   By intercepting the fov argument at the renderWorld→getProjectionMatrix call
     *   site, all mixin transformations (including Zoomify's @ModifyReturnValue) have
     *   already been fully applied to the value. Scoping to renderWorld also prevents
     *   this from firing for renderItemInHand's separate projection matrix call.
     */
    @ModifyArg(
        method = "renderWorld",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/render/GameRenderer;getProjectionMatrix(F)Lorg/joml/Matrix4f;"),
        index = 0
    )
    private float captureWorldRenderFov(float fov) {
        WorldSpaceTransform.captureFov(fov);
        return fov;
    }

    /**
     * Intercept crosshair targeting to ignore entity hits and force block raycasts
     * when peaceful mining is active. This allows mining through translucent players.
     * Injects at the tail of updateCrosshairTarget and modifies MinecraftClient.crosshairTarget.
     */
    @Inject(method = "updateCrosshairTarget", at = @At("TAIL"))
    private void onUpdateCrosshairTarget(float tickProgress, CallbackInfo ci) {
        // Check if peaceful mining is active
        if (!isPeacefulMiningActive()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        HitResult result = client.crosshairTarget;

        // If it's already a block hit, don't interfere
        if (result instanceof BlockHitResult) {
            return;
        }

        // If it's an entity hit (or miss), perform our own block-only raycast
        Vec3d eyePos = client.player.getEyePos();
        Vec3d lookVec = client.player.getRotationVec(1.0f);
        double blockInteractionRange = client.player.getBlockInteractionRange();
        Vec3d endPos = eyePos.add(lookVec.multiply(blockInteractionRange));

        // Create raycast context for block-only raycasting
        RaycastContext context = new RaycastContext(
            eyePos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            client.player
        );

        // Perform block raycast and replace the crosshair target
        HitResult blockHit = client.world.raycast(context);
        client.crosshairTarget = blockHit;
    }

    /**
     * Checks if peaceful mining is currently active
     */
    private boolean isPeacefulMiningActive() {
        // Check if peaceful mining is enabled in config
        if (!BetterPrisonsClient.config.peacefulMiningEnabled) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        // Check if the player is holding a pickaxe by checking the item name
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();

        return isPickaxe(mainHand) || isPickaxe(offHand);
    }

    /**
     * Checks if an ItemStack is a pickaxe
     */
    private boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String itemName = stack.getItem().toString().toLowerCase();
        return itemName.contains("pickaxe");
    }
}
