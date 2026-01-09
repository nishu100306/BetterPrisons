package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ChatScreen.class)
public class ChatMixin {
    // Intercept outgoing commands
    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void sendMessage(String message, boolean overlay, CallbackInfo ci) {
        if (message.toString().startsWith("/")) {
            BetterPrisonsClient.cooldownHud.onCommandSent(message.toString());
        }
    }
}
