package BetterPrisons.modid.api;

import BetterPrisons.modid.BetterPrisonsClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client-side integration with the Cosmic Mods server API (the {@code cosmicapi:main}
 * plugin channel). On joining a server it sends a {@code client_hello} with this app's
 * public clientId and the scopes/hooks it wants, and stores the effective access the
 * server grants back.
 *
 * <p>NOTE — two values below are app-specific and not derivable from the protocol docs:
 *   {@link #CLIENT_ID} (your approved app's public client id) and the requested
 *   {@link #REQUESTED_SCOPES} / {@link #REQUESTED_HOOKS} (what BetterPrisons wants to use).
 */
public final class CosmicApi {

    private static final int PROTOCOL_VERSION = 1;
    private static final String MOD_ID = "betterprisons";

    // BetterPrisons' approved public client id (from the Cosmic dashboard).
    private static final String CLIENT_ID = "client_mqo5z17at3zeg17bx1";

    // TODO: fill with the scopes/hooks BetterPrisons should request (depends on intended use).
    private static final List<String> REQUESTED_SCOPES = List.of();
    private static final List<String> REQUESTED_HOOKS = List.of();

    private static final Gson GSON = new Gson();

    // Effective access granted by the server for this connection.
    public static volatile String sessionId = null;
    public static volatile Set<String> allowedScopes = Set.of();
    public static volatile Set<String> allowedHooks = Set.of();

    private CosmicApi() {}

    public static void register() {
        // Register the payload type both directions.
        PayloadTypeRegistry.playC2S().register(CosmicApiPayload.ID, CosmicApiPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CosmicApiPayload.ID, CosmicApiPayload.CODEC);

        // Receive server messages (handshake reply, hook events, ...).
        ClientPlayNetworking.registerGlobalReceiver(CosmicApiPayload.ID,
            (payload, context) -> context.client().execute(() -> handleMessage(payload.json())));

        // Send the client_hello once we join a server.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sessionId = null;
            allowedScopes = Set.of();
            allowedHooks = Set.of();
            sendHello();
        });
    }

    private static void sendHello() {
        if (!BetterPrisonsClient.config.cosmicApiEnabled) return;
        if (CLIENT_ID.startsWith("REPLACE")) {
            BetterPrisonsClient.LOGGER.info("[CosmicApi] client id not configured — skipping handshake");
            return;
        }
        if (!ClientPlayNetworking.canSend(CosmicApiPayload.ID)) {
            BetterPrisonsClient.LOGGER.info("[CosmicApi] server does not advertise cosmicapi:main — not a Cosmic API server");
            return;
        }

        JsonObject hello = new JsonObject();
        hello.addProperty("v", PROTOCOL_VERSION);
        hello.addProperty("kind", "client_hello");
        hello.addProperty("clientId", CLIENT_ID);
        hello.addProperty("modId", MOD_ID);
        hello.addProperty("installId", installId());
        hello.addProperty("modVersion", modVersion());
        hello.addProperty("minecraftVersion", FabricLoader.getInstance()
            .getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown"));
        hello.add("requestedScopes", GSON.toJsonTree(REQUESTED_SCOPES));
        hello.add("requestedHooks", GSON.toJsonTree(REQUESTED_HOOKS));

        ClientPlayNetworking.send(new CosmicApiPayload(GSON.toJson(hello)));
        BetterPrisonsClient.LOGGER.info("[CosmicApi] sent client_hello (modId={}, scopes={}, hooks={})",
            MOD_ID, REQUESTED_SCOPES, REQUESTED_HOOKS);
    }

    private static void handleMessage(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            // Handshake reply carries a sessionId + effective access.
            if (obj.has("sessionId")) {
                sessionId = obj.get("sessionId").getAsString();
                allowedScopes = toSet(obj.getAsJsonArray("allowedScopes"));
                allowedHooks = toSet(obj.getAsJsonArray("allowedHooks"));
                BetterPrisonsClient.LOGGER.info("[CosmicApi] session={} allowedScopes={} allowedHooks={}",
                    sessionId, allowedScopes, allowedHooks);
                if (obj.has("deniedScopes") && obj.getAsJsonArray("deniedScopes").size() > 0) {
                    BetterPrisonsClient.LOGGER.info("[CosmicApi] deniedScopes={}", obj.getAsJsonArray("deniedScopes"));
                }
            } else {
                // Hook events / other messages — handled once those formats are wired up.
                BetterPrisonsClient.LOGGER.info("[CosmicApi] message: {}", json);
            }
        } catch (Exception e) {
            BetterPrisonsClient.LOGGER.warn("[CosmicApi] failed to parse message: {}", json);
        }
    }

    private static Set<String> toSet(JsonArray arr) {
        Set<String> out = new HashSet<>();
        if (arr != null) {
            for (JsonElement el : arr) out.add(el.getAsString());
        }
        return out;
    }

    /** A stable per-install id, generated once and persisted in the config. */
    private static String installId() {
        String id = BetterPrisonsClient.config.cosmicApiInstallId;
        if (id == null || id.isEmpty()) {
            id = "ins_" + java.util.UUID.randomUUID();
            BetterPrisonsClient.config.cosmicApiInstallId = id;
            BetterPrisonsClient.config.save();
        }
        return id;
    }

    private static String modVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }
}
