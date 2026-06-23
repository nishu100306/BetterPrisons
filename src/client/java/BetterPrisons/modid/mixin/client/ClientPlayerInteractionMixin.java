package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
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
        // Auto-trade: shift-right-click another player to send /trade <username>
        if (BetterPrisonsClient.config.autoTradeEnabled
                && hand == Hand.MAIN_HAND
                && entity instanceof PlayerEntity
                && player.isSneaking()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() != null) {
                String username = ((PlayerEntity) entity).getGameProfile().name();
                client.getNetworkHandler().sendChatCommand("trade " + username);
            }
        }

        // Check if peaceful mining is active
        if (!isPeacefulMiningActive()) {
            return;
        }

        // Cancel all entity interactions when peaceful mining is active
        cir.setReturnValue(ActionResult.PASS);
    }

    /**
     * Prevents attacking all entities when peaceful mining is active.
     * Also resets the combat cooldown when hitting another player.
     */
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity entity, CallbackInfo ci) {
        // Reset combat cooldown when hitting another player
        if (entity instanceof PlayerEntity && BetterPrisonsClient.cooldownHud != null) {
            BetterPrisonsClient.cooldownHud.resetCombatCooldown();
        }

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

        // In the prisonbreak world (if enabled), peaceful mining is always active
        // regardless of held item.
        if (BetterPrisonsClient.config.peacefulMiningAlwaysInPrisonbreak && isInPrisonbreak(client)) {
            return true;
        }

        // Check if the player is holding an enabled tool type
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();

        return isEnabledTool(mainHand) || isEnabledTool(offHand);
    }

    private boolean isInPrisonbreak(MinecraftClient client) {
        return client.world != null
            && "minecraft:prisonbreak".equals(client.world.getRegistryKey().getValue().toString());
    }

    /** True if the stack is a pickaxe or mace and the corresponding config toggle is enabled. */
    private boolean isEnabledTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (BetterPrisonsClient.config.peacefulMiningPickaxe && isPickaxe(stack)) return true;
        if (BetterPrisonsClient.config.peacefulMiningMace && isMace(stack)) return true;
        return false;
    }

    private boolean isPickaxe(ItemStack stack) {
        return stack.getItem().getTranslationKey().toLowerCase().contains("pickaxe");
    }

    private boolean isMace(ItemStack stack) {
        if (stack.getItem() instanceof MaceItem) return true;
        return stack.getItem().getTranslationKey().toLowerCase().contains("mace");
    }
}
