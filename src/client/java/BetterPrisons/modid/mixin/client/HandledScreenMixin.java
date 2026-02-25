package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BetterPrisonsClient.easyView == null || !BetterPrisonsClient.easyView.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        InventoryScreen screen = (InventoryScreen) (Object) this;
        PlayerScreenHandler handler = screen.getScreenHandler();

        int xSlot = (screen.width - 176) / 2;
        int ySlot = (screen.height - 166) / 2;

        Matrix3x2fStack matrices = context.getMatrices();

        // Render overlays for all inventory slots
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            // Process the item directly to get text and color
            BetterPrisons.modid.misc.EasyView.TextWithColor result = BetterPrisonsClient.easyView.processStackForTextWithColor(stack);

            if (result != null && result.text != null && !result.text.isEmpty()) {
                int slotX = slot.x;
                int slotY = slot.y;

                matrices.pushMatrix();
                matrices.translate(xSlot + slotX + 1, ySlot + slotY + 1);
                matrices.scale(result.scale, result.scale);

                net.minecraft.text.Text displayText = net.minecraft.text.Text.literal(result.text)
                        .styled(style -> style.withBold(result.bold));
                context.drawText(client.textRenderer, displayText, 0, 0, result.color, true);

                matrices.popMatrix();
            }
        }
    }
}
