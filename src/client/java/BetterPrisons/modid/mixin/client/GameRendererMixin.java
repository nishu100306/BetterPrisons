package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Intercept crosshair targeting to ignore entity hits and force block raycasts
     * when peaceful mining is active. This allows mining through translucent players.
     */
    @Inject(method = "findCrosshairTarget", at = @At("RETURN"), cancellable = true)
    private void onFindCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        // Check if peaceful mining is active
        if (!isPeacefulMiningActive()) {
            return;
        }

        HitResult result = cir.getReturnValue();

        // If it's already a block hit, don't interfere
        if (result instanceof BlockHitResult) {
            return;
        }

        // If it's an entity hit (or miss), perform our own block-only raycast
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        // Get player's eye position and look direction
        Vec3d eyePos = client.player.getEyePos();
        Vec3d lookVec = client.player.getRotationVec(1.0f);
        Vec3d endPos = eyePos.add(lookVec.multiply(blockInteractionRange));

        // Create raycast context for block-only raycasting
        RaycastContext context = new RaycastContext(
            eyePos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            client.player
        );

        // Perform block raycast and replace the return value
        HitResult blockHit = client.world.raycast(context);
        cir.setReturnValue(blockHit);
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
