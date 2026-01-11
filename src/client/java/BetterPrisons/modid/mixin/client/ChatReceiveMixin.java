package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatReceiveMixin {
    private String previousMessage = "";

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onReceiveMessage(Text message, CallbackInfo ci) {
        String text = message.getString();
        BetterPrisonsClient.enchantTracker.onChatMessage(text);
        BetterPrisonsClient.cooldownHud.onChatReceived(text);

        // Check for meteor falling (coordinates in current message, announcement in previous)
        if (previousMessage.contains("(!) A meteor is falling from the sky at:")) {
            BetterPrisonsClient.meteorHud.onMeteorFalling(text);
        }

        // Check for meteor crashed (coordinates in current message, announcement in previous)
        if (previousMessage.contains("(!) A meteor has crashed at:")) {
            BetterPrisonsClient.meteorHud.onMeteorCrashed(text);
        }

        // Store current message for next iteration
        previousMessage = text;
    }
}
