package BetterPrisons.modid.devtools;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.utils.ItemUtils;
import net.minecraft.text.Text;

public class ParticleDebugTracker {
    private static double closestDistance = Double.MAX_VALUE;
    private static ParticleData closestParticle = null;

    public static void considerParticle(String type, String particleString, double x, double y, double z,
                                        double playerX, double playerY, double playerZ, double distance) {
        if (distance > 3.0) return;
        if (distance < closestDistance) {
            closestDistance = distance;
            closestParticle = new ParticleData(type, particleString, x, y, z, playerX, playerY, playerZ, distance);
        }
    }

    public static void logAndClear() {
        if (closestParticle != null) {
            boolean dragonSoundHeard = SoundTracker.wasDragonSoundHeard();

            // Check if particle is within 0.75 blocks AND dragon sound was heard - activate Super Breaker
            if (closestParticle.distance <= 0.75 && dragonSoundHeard) {
                var superBreaker = BetterPrisonsClient.enchantTracker.getEnchant("super_breaker");
                if (superBreaker != null) {
                    // Try to extract enchant text from held pickaxe
                    Text enchantText = ItemUtils.extractEnchantTextFromHeldItem("Super Breaker");

                    if (enchantText != null) {
                        superBreaker.activate(2.5, enchantText); // Activate with formatted text
                    } else {
                        superBreaker.activate(2.5); // Activate with default text
                    }
                    BetterPrisonsClient.LOGGER.info("Super Breaker activated! isActive={}", superBreaker.isActive);
                }
            }
        }

        closestDistance = Double.MAX_VALUE;
        closestParticle = null;
    }

    private static class ParticleData {
        String type;
        String particleString;
        double x, y, z;
        double playerX, playerY, playerZ;
        double distance;

        ParticleData(String type, String particleString, double x, double y, double z,
                    double playerX, double playerY, double playerZ, double distance) {
            this.type = type;
            this.particleString = particleString;
            this.x = x;
            this.y = y;
            this.z = z;
            this.playerX = playerX;
            this.playerY = playerY;
            this.playerZ = playerZ;
            this.distance = distance;
        }
    }
}
