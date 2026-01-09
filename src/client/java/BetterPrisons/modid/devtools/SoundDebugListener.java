package BetterPrisons.modid.devtools;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.WeightedSoundSet;

import java.util.HashSet;
import java.util.Set;

public class SoundDebugListener implements SoundInstanceListener {

    private final Set<String> loggedSoundsThisTick = new HashSet<>();

    public void clearTickCache() {
        loggedSoundsThisTick.clear();
    }

    @Override
    public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet, float range) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Create unique identifier for this sound instance
        String soundKey = sound.toString();

        // Check for dragon sound and mark it
        if (soundKey.equals("SoundInstance[minecraft:entity.ender_dragon.growl]")) {
            SoundTracker.markDragonSoundHeard();
            BetterPrisonsClient.LOGGER.info("[DRAGON SOUND DETECTED]");
        }

        // Only log specific sound
        if (!soundKey.equals("SoundInstance[minecraft:entity.ender_dragon.growl]")) {
            return;
        }

        // Skip if already logged this tick
        if (loggedSoundsThisTick.contains(soundKey)) {
            return;
        }

        // Get sound position
        double soundX = sound.getX();
        double soundY = sound.getY();
        double soundZ = sound.getZ();

        // Get player position
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();

        // Calculate distance to player
        double distance = Math.sqrt(
            Math.pow(soundX - playerX, 2) +
            Math.pow(soundY - playerY, 2) +
            Math.pow(soundZ - playerZ, 2)
        );

        // Mark as logged for this tick
        loggedSoundsThisTick.add(soundKey);

        // Log the sound
        /*
        BetterPrisonsClient.LOGGER.info("========== SOUND EVENT ==========");
        BetterPrisonsClient.LOGGER.info("Sound: " + sound.toString());
        BetterPrisonsClient.LOGGER.info("Sound ID: " + sound.getId().toString());
        BetterPrisonsClient.LOGGER.info("Sound Path: " + sound.getId().getPath());
        BetterPrisonsClient.LOGGER.info("Sound Namespace: " + sound.getId().getNamespace());
        BetterPrisonsClient.LOGGER.info("Position: x=" + String.format("%.2f", soundX) + ", y=" + String.format("%.2f", soundY) + ", z=" + String.format("%.2f", soundZ));
        BetterPrisonsClient.LOGGER.info("Volume: " + sound.getVolume());
        BetterPrisonsClient.LOGGER.info("Pitch: " + sound.getPitch());
        BetterPrisonsClient.LOGGER.info("Category: " + sound.getCategory().getName());
        BetterPrisonsClient.LOGGER.info("Repeat: " + sound.isRepeatable());
        BetterPrisonsClient.LOGGER.info("Repeat Delay: " + sound.getRepeatDelay());
        BetterPrisonsClient.LOGGER.info("Relative: " + sound.isRelative());
        BetterPrisonsClient.LOGGER.info("Attenuation Type: " + sound.getAttenuationType().toString());
        BetterPrisonsClient.LOGGER.info("Range: " + range);
        BetterPrisonsClient.LOGGER.info("Distance from player: " + String.format("%.2f", distance) + " blocks");
        BetterPrisonsClient.LOGGER.info("=================================");

         */
    }
}
