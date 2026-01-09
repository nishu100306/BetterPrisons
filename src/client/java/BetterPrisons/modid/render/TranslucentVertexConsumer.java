package BetterPrisons.modid.render;

import net.minecraft.client.render.VertexConsumer;

/**
 * Wrapper for VertexConsumer that modifies alpha values
 */
public class TranslucentVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float alpha;

    public TranslucentVertexConsumer(VertexConsumer delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        return delegate.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        // Override the alpha value to make it translucent
        return delegate.color(red, green, blue, (int)(this.alpha * 255));
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return delegate.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return delegate.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return delegate.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return delegate.normal(x, y, z);
    }
}
