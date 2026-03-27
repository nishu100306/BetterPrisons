package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class InventoryDropMixin {

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSlotClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (!BetterPrisonsClient.config.pickaxeDropConfirmationEnabled
                || !BetterPrisonsClient.config.pickaxeDropDragBlockEnabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Clicking outside inventory with cursor item (slot is null or slotId == -999)
        if (slotId == -999 && (actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_CRAFT)) {
            ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
            if (!cursorStack.isEmpty() && isPickaxe(cursorStack)) {
                client.player.sendMessage(
                        Text.literal("\u00a7c\u00a7l[!] \u00a7cPickaxe dragging out of inventory is disabled."), false);
                ci.cancel();
                return;
            }
        }

        // Q/Ctrl+Q throw while hovering a slot
        if (actionType == SlotActionType.THROW && slot != null) {
            ItemStack slotStack = slot.getStack();
            if (!slotStack.isEmpty() && isPickaxe(slotStack)) {
                client.player.sendMessage(
                        Text.literal("\u00a7c\u00a7l[!] \u00a7cPickaxe dropping is disabled."), false);
                ci.cancel();
            }
        }
    }

    private static boolean isPickaxe(ItemStack stack) {
        return stack.getItem().toString().toLowerCase().contains("pickaxe");
    }
}
