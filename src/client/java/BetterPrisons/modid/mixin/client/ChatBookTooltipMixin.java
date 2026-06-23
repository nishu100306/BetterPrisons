package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.misc.EnchantBookTooltip;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * CosmicPrisons sends enchant-book chat hovers as SHOW_TEXT (pre-rendered text, no item),
 * so the normal item-tooltip callback can't touch them. This redirects the SHOW_TEXT
 * value lookup in {@code drawHoverEvent} and appends the upgrade-cost lines when the
 * hovered text looks like an enchant book.
 */
@Mixin(DrawContext.class)
public class ChatBookTooltipMixin {

    @Redirect(method = "drawHoverEvent", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/text/HoverEvent$ShowText;value()Lnet/minecraft/text/Text;"))
    private Text bp$appendBookCost(HoverEvent.ShowText showText) {
        return EnchantBookTooltip.appendChatHoverCost(showText.value());
    }
}
