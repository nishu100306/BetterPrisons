package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionMixin {

    /**
     * Prevents interaction with all entities when peaceful mining is active
     */
    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        // Check if peaceful mining is active
        if (!isPeacefulMiningActive()) {
            return;
        }

        // Cancel all entity interactions when peaceful mining is active
        cir.setReturnValue(ActionResult.PASS);
    }

    /**
     * Prevents attacking all entities when peaceful mining is active
     */
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity entity, CallbackInfo ci) {
        // Check if peaceful mining is active
        if (!isPeacefulMiningActive()) {
            return;
        }

        // Cancel all entity attacks when peaceful mining is active
        ci.cancel();
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
