package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemScaleMixin {


    @Inject(method = "renderFirstPersonItem",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V"))
    private void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
        // Apply scaling based on which hand is being rendered
        float scale = getItemScale(item);

        if (scale != 1.0f) {
            matrices.scale(scale, scale, scale);
        }
    }

    /**
     * Determines the scale factor for the given item
     */
    private float getItemScale(ItemStack stack) {
        if (stack.isEmpty()) {
            return 1.0f;
        }

        String itemName = stack.getItem().toString().toLowerCase();

        // Check item type and return appropriate scale
        if (itemName.contains("pickaxe")) {
            return BetterPrisonsClient.config.heldItemPickaxeScale / 100.0f;
        } else if (itemName.contains("sword")) {
            return BetterPrisonsClient.config.heldItemSwordScale / 100.0f;
        } else if (itemName.contains("axe")) {
            return BetterPrisonsClient.config.heldItemAxeScale / 100.0f;
        } else {
            return BetterPrisonsClient.config.heldItemOtherScale / 100.0f;
        }
    }
}
