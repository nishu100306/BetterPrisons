package BetterPrisons.modid.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({MinecraftClient.class})
public class UseItemWhileMiningMixin {
    @Shadow
    public ClientPlayerInteractionManager interactionManager;

    @Redirect(method = {"doItemUse"}, at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"
            )
    )
    private boolean allowItemUseWhileMining(ClientPlayerInteractionManager interactionManager) {
        return false;
    }
}
