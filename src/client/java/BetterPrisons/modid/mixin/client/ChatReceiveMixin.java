package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatReceiveMixin {
    @Shadow @Final private static Logger LOGGER;
    private String previousMessage = "";
    // Pattern to match private messages: [any] [username -> me] message
    private static final Pattern PM_PATTERN = Pattern.compile("\\[.*?]\\s*\\[.+?\\s*->\\s*me].*");

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onReceiveMessage(Text message, CallbackInfo ci) {
        String text = message.getString();
        BetterPrisonsClient.enchantTracker.onChatMessage(text);
        BetterPrisonsClient.cooldownHud.onChatReceived(text);

        // Check for private messages
        checkPrivateMessage(text);

        // Check for meteor falling (coordinates in current message, announcement in previous)
        if (previousMessage.startsWith("(!) A meteor is falling from the sky at:") || (previousMessage.startsWith("(!) A meteor summoned by") && previousMessage.contains("is falling from the sky at:"))) {
            BetterPrisonsClient.meteorHud.onMeteorFalling(text);
        }

        // Check for meteor crashed (coordinates in current message, announcement in previous)
        if (previousMessage.contains("(!) A meteor has crashed at:")) {
            BetterPrisonsClient.meteorHud.onMeteorCrashed(text);
        }

        // Store current message for next iteration
        previousMessage = text;
    }

    private void checkPrivateMessage(String text) {
        // Check if message notifications are enabled
        if (!BetterPrisonsClient.config.messageNotifsEnabled) {
            return;
        }

        // Check if message matches private message pattern
        if (PM_PATTERN.matcher(text).matches()) {
            LOGGER.info("Private message detected: " + text);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                return;
            }

            // Play the configured sound with configured volume
            String soundType = BetterPrisonsClient.config.messageNotifsSound;
            float volume = BetterPrisonsClient.config.messageNotifsVolume / 100.0f;

            switch (soundType) {
                case "bell":
                    client.player.playSound(SoundEvents.BLOCK_BELL_USE, volume, 1.0f);
                    break;
                case "xp_orb":
                    client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 1.0f);
                    break;
                case "note_pling":
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), volume, 1.0f);
                    break;
                case "enchant":
                    client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, volume, 1.0f);
                    break;
                case "level_up":
                    client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, volume, 1.0f);
                    break;
                case "ender_eye":
                    client.player.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, volume, 1.0f);
                    break;
                default:
                    // Default to anvil
                    client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, volume, 1.0f);
                    break;
            }

            LOGGER.info("Private message detected, played sound: {} at volume: {}", soundType, volume);
        }
    }
}
