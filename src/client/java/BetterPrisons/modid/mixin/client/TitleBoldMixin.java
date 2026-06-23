package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Bolds server-sent title / subtitle popups that contain "XP" or "Energy"
 * (e.g. "+1.0 XP", "+18.0 Energy") when the Misc setting is on.
 */
@Mixin(InGameHud.class)
public class TitleBoldMixin {

    @ModifyVariable(method = "setTitle", at = @At("HEAD"), argsOnly = true)
    private Text bp$boldTitle(Text title) {
        return bp$maybeBold(title);
    }

    @ModifyVariable(method = "setSubtitle", at = @At("HEAD"), argsOnly = true)
    private Text bp$boldSubtitle(Text subtitle) {
        return bp$maybeBold(subtitle);
    }

    private static Text bp$maybeBold(Text text) {
        if (text == null) return null;
        if (BetterPrisonsClient.config == null || !BetterPrisonsClient.config.boldXpEnergyTitles) return text;
        String s = text.getString();
        if (s.contains("XP") || s.contains("Energy")) {
            return bp$deepBold(text);
        }
        return text;
    }

    /** Rebuilds the text with bold forced on every component (not just the root). */
    private static Text bp$deepBold(Text text) {
        MutableText result = MutableText.of(text.getContent())
            .setStyle(text.getStyle().withBold(true));
        for (Text sibling : text.getSiblings()) {
            result.append(bp$deepBold(sibling));
        }
        return result;
    }
}
