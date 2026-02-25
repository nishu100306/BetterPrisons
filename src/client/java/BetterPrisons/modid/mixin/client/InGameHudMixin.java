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
        if (BetterPrisonsClient.easyView == null || !BetterPrisonsClient.easyView.enabled) {
            return;
        }

        if (this.client.player == null) {
            return;
        }

        // Don't render if a screen is open (inventory, chest, etc.)
        if (this.client.currentScreen != null) {
            return;
        }

        PlayerEntity player = this.client.player;
        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        // Hotbar is centered at bottom of screen
        // Each slot is 20 pixels wide (16px item + 2px padding on each side)
        // There are 9 slots, so total width is 182 pixels (9 * 20 + 2 for border)
        int hotbarX = scaledWidth / 2 - 91; // Center of screen minus half hotbar width
        int hotbarY = scaledHeight - 22; // 22 pixels from bottom

        Matrix3x2fStack matrices = context.getMatrices();

        // Render overlays for hotbar items (inventory slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            BetterPrisons.modid.misc.EasyView.TextWithColor result = BetterPrisonsClient.easyView.processStackForTextWithColor(stack);

            if (result != null && result.text != null && !result.text.isEmpty()) {
                // Calculate slot position
                // Each slot starts at hotbarX + (i * 20) + 3 (border + padding)
                int slotX = hotbarX + (i * 20) + 3;
                int slotY = hotbarY + 3;

                matrices.pushMatrix();
                matrices.translate(slotX + 1, slotY + 1);
                matrices.scale(result.scale, result.scale);

                net.minecraft.text.Text displayText = net.minecraft.text.Text.literal(result.text)
                        .styled(style -> style.withBold(result.bold));
                context.drawText(this.client.textRenderer, displayText, 0, 0, result.color, true);

                matrices.popMatrix();
            }
        }
    }
}
