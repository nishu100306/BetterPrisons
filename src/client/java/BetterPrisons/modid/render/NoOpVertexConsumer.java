package BetterPrisons.modid.render;

import net.minecraft.client.render.VertexConsumer;

/**
 * A no-op VertexConsumer that discards all rendering calls.
 * Used to prevent armor and capes from rendering on translucent players.
 */
public class NoOpVertexConsumer implements VertexConsumer {
    public static final NoOpVertexConsumer INSTANCE = new NoOpVertexConsumer();

    private NoOpVertexConsumer() {}

    public static NoOpVertexConsumer create() {
        return new NoOpVertexConsumer();
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer color(int argb) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer lineWidth(float width) {
        return this;
    }
}
