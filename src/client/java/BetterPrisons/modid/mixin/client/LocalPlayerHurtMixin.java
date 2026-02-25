package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class LocalPlayerHurtMixin {

    /**
     * Resets the combat cooldown when the local player is hit by another player.
     * EntityDamageS2CPacket carries the attacker entity ID, so we can filter
     * for player-sourced damage specifically.
     */
    @Inject(method = "onEntityDamage", at = @At("HEAD"))
    private void onEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Only care about damage to the local player
        if (packet.entityId() != client.player.getId()) return;

        // Check if the attacker is a player (sourceCauseId holds the attacker's entity ID, 0 = none)
        int causeId = packet.sourceCauseId();
        if (causeId > 0) {
            Entity cause = client.world.getEntityById(causeId);
            if (cause instanceof PlayerEntity && cause != client.player) {
                if (BetterPrisonsClient.cooldownHud != null) {
                    BetterPrisonsClient.cooldownHud.resetCombatCooldown();
                }
            }
        }
    }
}
