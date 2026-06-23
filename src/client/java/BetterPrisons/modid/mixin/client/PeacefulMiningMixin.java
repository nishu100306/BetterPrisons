package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.render.PeacefulMiningState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handles the state-extraction phase of peaceful mining ghost rendering.
 * Targets PlayerEntityRenderer because updateRenderState and shouldRenderFeatures
 * are overridden there with PlayerEntityRenderState — clean remap targets.
 *
 * getMixColor and getRenderLayer live in LivingEntityRenderer and are handled by
 * PeacefulMiningRendererMixin to avoid "Cannot remap" issues.
 */
@Mixin(PlayerEntityRenderer.class)
public class PeacefulMiningMixin {

    // ── Phase 1: mark targets and strip cosmetics ──────────────────────────

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(PlayerLikeEntity entity, PlayerEntityRenderState state,
                                     float tickDelta, CallbackInfo ci) {
        if (isPeacefulMiningTarget(entity)) {
            PeacefulMiningState.TARGETS.add(state.id);
            // Remove all outer skin layers so the ghost silhouette is just the body shape
            state.hatVisible = false;
            state.jacketVisible = false;
            state.leftSleeveVisible = false;
            state.rightSleeveVisible = false;
            state.leftPantsLegVisible = false;
            state.rightPantsLegVisible = false;
            state.capeVisible = false;
        } else {
            PeacefulMiningState.TARGETS.remove(state.id);
        }
    }

    // ── Phase 2: suppress armor and all feature renderers ─────────────────

    @Inject(method = "shouldRenderFeatures", at = @At("HEAD"), cancellable = true)
    private void onShouldRenderFeatures(PlayerEntityRenderState state,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (PeacefulMiningState.TARGETS.contains(state.id)) {
            cir.setReturnValue(false);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private boolean isPeacefulMiningTarget(PlayerLikeEntity entity) {
        if (!BetterPrisonsClient.config.peacefulMiningEnabled) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        // Only ghost other PlayerEntity instances — not ourselves, not mannequins
        if (!(entity instanceof PlayerEntity) || entity.getUuid().equals(client.player.getUuid())) {
            return false;
        }
        // In the prisonbreak world (if enabled), peaceful mining is always active
        // regardless of held item; otherwise require an enabled tool type.
        boolean alwaysActive = BetterPrisonsClient.config.peacefulMiningAlwaysInPrisonbreak && isInPrisonbreak(client);
        if (!alwaysActive) {
            ItemStack main = client.player.getMainHandStack();
            ItemStack off = client.player.getOffHandStack();
            if (!isEnabledTool(main) && !isEnabledTool(off)) {
                return false;
            }
        }
        return Math.sqrt(client.player.squaredDistanceTo(entity)) <= BetterPrisonsClient.config.peacefulMiningDistance;
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
