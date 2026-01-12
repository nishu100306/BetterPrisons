package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin {
    @Shadow
    @Final
    private int rows;


    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BetterPrisonsClient.easyView == null || !BetterPrisonsClient.easyView.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        GenericContainerScreen screen = (GenericContainerScreen) (Object) this;
        GenericContainerScreenHandler screenHandler = screen.getScreenHandler();

        int backgroundHeight = 114 + this.rows * 18;
        int xSlot = (screen.width - 176) / 2;
        int ySlot = (screen.height - backgroundHeight) / 2;

        Matrix3x2fStack matrices = context.getMatrices();

        for (Slot slot : screenHandler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            // Process all items on-the-fly
            BetterPrisons.modid.misc.EasyView.TextWithColor result = BetterPrisonsClient.easyView.processStackForTextWithColor(stack);

            if (result != null && result.text != null && !result.text.isEmpty()) {
                int slotX = slot.x;
                int slotY = slot.y;

                matrices.pushMatrix();
                matrices.translate(xSlot + slotX + 1, ySlot + slotY + 1);
                matrices.scale(0.5f, 0.5f);

                context.drawText(client.textRenderer, result.text, 0, 0, result.color, true);

                matrices.popMatrix();
            }
        }
    }
}
