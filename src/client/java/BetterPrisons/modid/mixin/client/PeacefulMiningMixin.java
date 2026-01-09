package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.render.TranslucentVertexConsumerProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class PeacefulMiningMixin {

    /**
     * Modify the VertexConsumerProvider to wrap it for rendering other players
     */
    @ModifyVariable(
        method = "renderEntity",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private VertexConsumerProvider wrapVertexConsumerProvider(VertexConsumerProvider original, Entity entity) {
        if (!isPeacefulMiningActive()) {
            return original;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return original;
        }

        // Check if the entity is a player (but not ourselves)
        if (entity instanceof PlayerEntity player && !player.getUuid().equals(client.player.getUuid())) {
            // Wrap with translucent vertex consumer provider
            if (!inPeacefulMiningDistance(entity)) {
                return original;
            }
            return new TranslucentVertexConsumerProvider(original);
        }

        return original;
    }

    /**
     * Disable depth writing before rendering translucent players
     * This makes them truly ghost-like - other entities and effects render over them
     */
    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void beforeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!isPeacefulMiningActive()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Check if the entity is a player (but not ourselves)
        if (entity instanceof PlayerEntity player && !player.getUuid().equals(client.player.getUuid())) {
            // Disable depth writing so other things render over this translucent player
            if (!inPeacefulMiningDistance(entity)) {
                return;
            }
            GL11.glDepthMask(false);
        }
    }

    /**
     * Restore depth writing after rendering translucent players
     */
    @Inject(method = "renderEntity", at = @At("RETURN"))
    private void afterRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!isPeacefulMiningActive()) {
            return;
        }
        if (!inPeacefulMiningDistance(entity)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Check if the entity is a player (but not ourselves)
        if (entity instanceof PlayerEntity player && !player.getUuid().equals(client.player.getUuid())) {
            // Restore depth writing for normal rendering
            GL11.glDepthMask(true);
        }
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

    private boolean inPeacefulMiningDistance(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        double distanceSq = client.player.squaredDistanceTo(entity);
        double dist = Math.sqrt(distanceSq);
        return dist <= BetterPrisonsClient.config.peacefulMiningDistance;
    }
}
