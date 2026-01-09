package BetterPrisons.modid.render;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

/**
 * Wrapper for VertexConsumerProvider that applies translucency and hides armor/capes
 */
public class TranslucentVertexConsumerProvider implements VertexConsumerProvider {
    private final VertexConsumerProvider delegate;

    public TranslucentVertexConsumerProvider(VertexConsumerProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        // Check if this is an armor or cape layer and skip rendering
        String layerName = layer.toString().toLowerCase();
        if (layerName.contains("armor") || layerName.contains("cape") || layerName.contains("elytra")) {
            // Return a no-op vertex consumer that discards all rendering
            return NoOpVertexConsumer.create();
            //return NoOpVertexConsumer.INSTANCE;
        }

        // Get opacity from config (0-255) and convert to 0.0-1.0 range
        float alpha = BetterPrisonsClient.config.peacefulMiningOpacity / 255.0f;

        VertexConsumer buffer = delegate.getBuffer(layer);
        return new TranslucentVertexConsumer(buffer, alpha);
    }
}
