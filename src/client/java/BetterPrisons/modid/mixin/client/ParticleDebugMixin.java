package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.devtools.ParticleDebugTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class ParticleDebugMixin {

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"))
    private void addParticle(Particle particle, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Get particle string representation
        String particleString = particle.toString();

        double x = particle.getBoundingBox().getCenter().x;
        double y = particle.getBoundingBox().getCenter().y;
        double z = particle.getBoundingBox().getCenter().z;

        // Calculate horizontal distance to player (x and z only)
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        double distance = Math.sqrt(
            Math.pow(x - playerX, 2) +
            Math.pow(z - playerZ, 2)
        );

        // Log ALL particles within 1 block to see what we're getting
        if (distance <= 1.0) {
            /*
            BetterPrisonsClient.LOGGER.info("[PARTICLE] type={}, distance={}",
                particleString, String.format("%.2f", distance));

             */
        }

        // Only process FlameParticle (refmap handles obfuscation in production)
        if (!(particle instanceof FlameParticle || particle instanceof SpellParticle)) {
            return;
        }

        if (distance <= 1.0) {

            //BetterPrisonsClient.LOGGER.info("[{} DETECTED] distance={}", particle.getClass(),String.format("%.2f", distance));



            // Store this particle if it's the closest so far this tick
            ParticleDebugTracker.considerParticle(
                particle.getType().toString(),
                particleString,
                x, y, z,
                playerX, playerY, playerZ,
                distance
            );
        }
    }
}
