package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.hud.EventsHud;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatReceiveMixin {
    @Shadow @Final private static Logger LOGGER;
    private String previousMessage = "";
    // Pattern to match private messages: [any] [username -> me] message
    private static final Pattern PM_PATTERN = Pattern.compile("\\[.*?]\\s*\\[.+?\\s*->\\s*me].*");
    // Merchant message patterns (single-line)
    private static final Pattern MERCHANT_SPAWN_PATTERN = Pattern.compile(
        "\\(!\\) A (\\w+) Ore Merchant traveled to (-?\\d+)x, (-?\\d+)y, (-?\\d+)z");
    private static final Pattern MERCHANT_SLAIN_PATTERN = Pattern.compile(
        "\\(!\\) A (\\w+) Ore Merchant has been slain by .+ at (-?\\d+)x, (-?\\d+)y, (-?\\d+)z");
    private static final Pattern GANG_PING_PATTERN = Pattern.compile(
        "\\[!]\\s+(\\S+)\\s+has pinged at\\s+(-?\\d+)x\\s+(-?\\d+)y\\s+(-?\\d+)z\\s+(\\S+)\\s+\\|\\s+HP:\\s+([\\d.]+)/([\\d.]+)\\s+\\|\\s+Facing:\\s+(\\w+)");
    private static final Pattern TRUCE_PING_PATTERN = Pattern.compile(
        "\\[T!]\\s+(\\S+)\\s+has pinged at\\s+(-?\\d+)x\\s+(-?\\d+)y\\s+(-?\\d+)z\\s+(\\S+)\\s+\\|\\s+HP:\\s+([\\d.]+)/([\\d.]+)\\s+\\|\\s+Facing:\\s+(\\w+)");
    private static final Pattern BANDIT_RUSH_PATTERN = Pattern.compile(
        "(\\w+) BANDIT RUSH has spawned at (-?\\d+), (-?\\d+), (-?\\d+)");
    private static final Pattern BANDIT_RUSH_WON_PATTERN = Pattern.compile(
        "won the\\s+(\\w+)\\s+BANDIT RUSH\\s+at\\s+(-?\\d+),\\s*(-?\\d+),\\s*(-?\\d+)");

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onReceiveMessage(Text message, CallbackInfo ci) {
        String text = message.getString();
        BetterPrisonsClient.enchantTracker.onChatMessage(text);
        BetterPrisonsClient.cooldownHud.onChatReceived(text);

        // Check for private messages
        checkPrivateMessage(text);

        // Check for merchant spawn
        Matcher merchantSpawnMatcher = MERCHANT_SPAWN_PATTERN.matcher(text);
        if (merchantSpawnMatcher.find()) {
            try {
                String tierName = merchantSpawnMatcher.group(1);
                int x = Integer.parseInt(merchantSpawnMatcher.group(2));
                int y = Integer.parseInt(merchantSpawnMatcher.group(3));
                int z = Integer.parseInt(merchantSpawnMatcher.group(4));
                BetterPrisonsClient.eventsHud.onMerchantSpawned(tierName, x, y, z);
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to parse merchant spawn coordinates: {}", text);
            }
        }

        // Check for merchant slain
        Matcher merchantSlainMatcher = MERCHANT_SLAIN_PATTERN.matcher(text);
        if (merchantSlainMatcher.find()) {
            try {
                String tierName = merchantSlainMatcher.group(1);
                int x = Integer.parseInt(merchantSlainMatcher.group(2));
                int y = Integer.parseInt(merchantSlainMatcher.group(3));
                int z = Integer.parseInt(merchantSlainMatcher.group(4));
                BetterPrisonsClient.eventsHud.onMerchantSlain(tierName, x, y, z);
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to parse merchant slain coordinates: {}", text);
            }
        }

        // Check for meteor falling (coordinates in current message, announcement in previous)
        if (previousMessage.startsWith("(!) A meteor is falling from the sky at:")) {
            BetterPrisonsClient.eventsHud.onMeteorFalling(text, EventsHud.MeteorType.NATURAL);
        } else if (previousMessage.startsWith("(!) A meteor summoned by") && previousMessage.contains("is falling from the sky at:")) {
            BetterPrisonsClient.eventsHud.onMeteorFalling(text, EventsHud.MeteorType.SUMMONED);
        }

        // Check for meteor crashed (coordinates in current message, announcement in previous)
        if (previousMessage.contains("(!) A meteor has crashed at:")) {
            BetterPrisonsClient.eventsHud.onMeteorCrashed(text);
        }

        // Strip §-codes for bandit rush matching (they can appear inline in coordinates)
        String strippedText = text.replaceAll("§.", "");

        // Check for bandit rush spawn
        Matcher banditRushMatcher = BANDIT_RUSH_PATTERN.matcher(strippedText);
        if (banditRushMatcher.find()) {
            try {
                String tier = banditRushMatcher.group(1);
                int bx = Integer.parseInt(banditRushMatcher.group(2));
                int by = Integer.parseInt(banditRushMatcher.group(3));
                int bz = Integer.parseInt(banditRushMatcher.group(4));
                BetterPrisonsClient.eventsHud.onBanditRushSpawned(tier, bx, by, bz);
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to parse bandit rush coordinates: {}", strippedText);
            }
        }

        // Check for bandit rush won
        if (strippedText.contains("BANDIT RUSH") && strippedText.contains("won the")) {
            Matcher banditRushWonMatcher = BANDIT_RUSH_WON_PATTERN.matcher(strippedText);
            if (banditRushWonMatcher.find()) {
                try {
                    String wonTier = banditRushWonMatcher.group(1);
                    int bx = Integer.parseInt(banditRushWonMatcher.group(2));
                    int bz = Integer.parseInt(banditRushWonMatcher.group(4));
                    BetterPrisonsClient.eventsHud.onBanditRushWon(wonTier, bx, bz);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Failed to parse bandit rush won coordinates: {}", strippedText);
                }
            }
        }

        // Check for gang ping or truce ping
        if (text.contains("has pinged at")) {
            boolean isTruce = false;
            Matcher pingMatcher = null;

            boolean showNonGang = BetterPrisonsClient.config.gangPingShowNonGang;
            boolean fromGangChat = text.contains("[GC]");
            boolean fromTruceChat = text.contains("[TC]");

            // Truce pings: accept from [TC] or [GC], or any chat if showNonGang is enabled
            if (BetterPrisonsClient.config.trucePingEnabled && text.contains("[T!]")
                    && (fromTruceChat || fromGangChat || showNonGang)) {
                pingMatcher = TRUCE_PING_PATTERN.matcher(text);
                if (pingMatcher.find()) isTruce = true;
                else pingMatcher = null;
            }
            // Gang pings: accept from [GC], or any chat if showNonGang is enabled
            if (pingMatcher == null && BetterPrisonsClient.config.gangPingEnabled && text.contains("[!]")
                    && (fromGangChat || showNonGang)) {
                pingMatcher = GANG_PING_PATTERN.matcher(text);
                if (!pingMatcher.find()) pingMatcher = null;
            }

            if (pingMatcher != null) {
                try {
                    String playerName = pingMatcher.group(1);
                    int px = Integer.parseInt(pingMatcher.group(2));
                    int py = Integer.parseInt(pingMatcher.group(3));
                    int pz = Integer.parseInt(pingMatcher.group(4));
                    String world = pingMatcher.group(5);
                    float hp = Float.parseFloat(pingMatcher.group(6));
                    float maxHp = Float.parseFloat(pingMatcher.group(7));
                    String facing = pingMatcher.group(8);
                    BetterPrisonsClient.gangPingManager.onGangPingReceived(
                            playerName, px, py, pz, world, hp, maxHp, facing, isTruce);

                    // Play notification sound (only if ping is in current world)
                    if (BetterPrisonsClient.config.gangPingSoundEnabled
                            && world.equals(BetterPrisons.modid.waypoint.WaypointManager.detectWorldKey())) {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) {
                            float volume = BetterPrisonsClient.config.gangPingSoundVolume / 100.0f;
                            client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), volume, 2.0f);
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warn("Failed to parse {} ping: {}", isTruce ? "truce" : "gang", text);
                }
            }
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
