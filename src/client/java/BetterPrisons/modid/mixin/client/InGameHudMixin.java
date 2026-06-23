package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client.player == null) {
            return;
        }

        // Don't render if a screen is open (inventory, chest, etc.)
        if (this.client.currentScreen != null) {
            return;
        }

        boolean easyViewActive = BetterPrisonsClient.easyView != null && BetterPrisonsClient.easyView.enabled;
        boolean cooldownActive = BetterPrisonsClient.itemCooldownOverlay != null && BetterPrisonsClient.config.itemCooldownsEnabled;
        boolean clueActive = BetterPrisonsClient.config.clueScrollSortingEnabled;

        if (!easyViewActive && !cooldownActive && !clueActive) return;

        PlayerEntity player = this.client.player;
        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        int hotbarX = scaledWidth / 2 - 91;
        int hotbarY = scaledHeight - 22;

        Matrix3x2fStack matrices = context.getMatrices();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            int slotX = hotbarX + (i * 20) + 3;
            int slotY = hotbarY + 3;

            // EasyView overlay
            if (easyViewActive) {
                BetterPrisons.modid.misc.EasyView.TextWithColor result = BetterPrisonsClient.easyView.processStackForTextWithColor(stack);

                if (result != null && result.text != null && !result.text.isEmpty()) {
                    matrices.pushMatrix();
                    matrices.translate(slotX + 1, slotY + 1);
                    matrices.scale(result.scale, result.scale);

                    net.minecraft.text.Text displayText = net.minecraft.text.Text.literal(result.text)
                            .styled(style -> style.withBold(result.bold));
                    context.drawText(this.client.textRenderer, displayText, 0, 0, result.color, true);

                    matrices.popMatrix();
                }
            }

            // Item cooldown overlay (centered in slot)
            if (cooldownActive) {
                BetterPrisons.modid.misc.ItemCooldownOverlay.CooldownResult cooldown = BetterPrisonsClient.itemCooldownOverlay.getCooldownOverlay(stack);
                if (cooldown != null) {
                    float cdScale = cooldown.scale;

                    net.minecraft.text.Text cdText = net.minecraft.text.Text.literal(cooldown.text)
                            .styled(style -> style.withBold(cooldown.bold));
                    int textWidth = this.client.textRenderer.getWidth(cdText);
                    int textHeight = this.client.textRenderer.fontHeight;

                    matrices.pushMatrix();
                    matrices.translate(slotX + 8, slotY + 8);
                    matrices.scale(cdScale, cdScale);
                    context.drawText(this.client.textRenderer, cdText, -textWidth / 2, -textHeight / 2, cooldown.color, true);
                    matrices.popMatrix();
                }
            }

            // Clue scroll step number (centered in slot)
            if (clueActive) {
                BetterPrisons.modid.chestsearch.ClueScrollOverlay.render(context, slotX, slotY, this.client.textRenderer, stack);
            }
        }
    }
}
