package BetterPrisons.modid.api;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.nio.charset.StandardCharsets;

/**
 * A message on the Cosmic API plugin channel ({@code cosmicapi:main}).
 *
 * <p>The wire payload is the raw UTF-8 JSON body (Bukkit plugin channels carry a
 * byte[] payload), so this just reads/writes all remaining bytes as a string.
 */
public record CosmicApiPayload(String json) implements CustomPayload {

    public static final CustomPayload.Id<CosmicApiPayload> ID = CustomPayload.id("cosmicapi:main");

    public static final PacketCodec<PacketByteBuf, CosmicApiPayload> CODEC = PacketCodec.of(
        (value, buf) -> buf.writeBytes(value.json.getBytes(StandardCharsets.UTF_8)),
        buf -> {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return new CosmicApiPayload(new String(bytes, StandardCharsets.UTF_8));
        }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
