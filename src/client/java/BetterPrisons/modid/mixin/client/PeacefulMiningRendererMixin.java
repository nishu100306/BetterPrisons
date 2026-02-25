package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.render.PeacefulMiningState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handles the render-phase overrides for peaceful mining ghost rendering.
 * Targets LivingEntityRenderer because getMixColor and getRenderLayer are
 * declared there (not overridden in PlayerEntityRenderer), ensuring the
 * injections remap and apply correctly.
 *
 * Color math (from LivingEntityRenderer.render() bytecode):
 *   j = hidden ? 0x26FFFFFF : 0xFFFFFFFF
 *   k = ColorHelper.mix(j, getMixColor(state))
 *   submitModel(..., k, ...)
 *
 *   For our targets state.invisible==false → hidden==false → j=0xFFFFFFFF
 *   → mix(0xFFFFFFFF, getMixColor()) == getMixColor()
 *   → returning (alpha<<24)|0x00FFFFFF gives direct alpha control.
 *
 * The instanceof guard prevents side effects on non-player entity renderers.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class PeacefulMiningRendererMixin {

    @Shadow
    public abstract Identifier getTexture(LivingEntityRenderState state);

    // ── Phase 3: custom mix color (configurable alpha) ────────────────────

    @Inject(method = "getMixColor", at = @At("HEAD"), cancellable = true)
    private void onGetMixColor(LivingEntityRenderState state,
                               CallbackInfoReturnable<Integer> cir) {
        if (!(state instanceof PlayerEntityRenderState pState) || !PeacefulMiningState.TARGETS.contains(pState.id)) {
            return;
        }
        int alpha = BetterPrisonsClient.config.peacefulMiningOpacity;
        // Full white tint with configured alpha; passes through ColorHelper.mix unchanged
        // because j=0xFFFFFFFF when the entity is not in the "hidden" code path.
        cir.setReturnValue((alpha << 24) | 0x00FFFFFF);
    }

    // ── Phase 4: force translucent render layer ────────────────────────────

    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private void onGetRenderLayer(LivingEntityRenderState state,
                                  boolean visible, boolean hidden, boolean glowing,
                                  CallbackInfoReturnable<RenderLayer> cir) {
        if (!(state instanceof PlayerEntityRenderState pState) || !PeacefulMiningState.TARGETS.contains(pState.id)) {
            return;
        }
        // itemEntityTranslucentCull is exactly the layer Minecraft uses for ghost entities
        // (invisible==true, bl2==true path in render()). It enables proper alpha blending.
        cir.setReturnValue(RenderLayers.itemEntityTranslucentCull(this.getTexture(state)));
    }
}
