package BetterPrisons.modid.misc;

import BetterPrisons.modid.BetterPrisonsClient;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.util.Identifier;

/**
 * Bundles a built-in resource pack (the PrisonBreak ore texture pack) inside the
 * mod jar and auto-applies it while the player is in the {@code minecraft:prisonbreak}
 * world, removing it on leave.
 *
 * <p>Enabling/disabling a resource pack requires a full {@code reloadResources()},
 * which causes a brief reload hitch on each world transition.
 */
public final class PrisonbreakTexturePack {

    private static final Identifier PACK_ID = Identifier.of("betterprisons", "prisonbreak");

    /** Cached resolved profile id (e.g. "betterprisons/prisonbreak"). */
    private static String resolvedProfileId = null;
    /** Whether the pack is currently applied by us. */
    private static boolean applied = false;

    private PrisonbreakTexturePack() {}

    /** Registers the bundled pack so the game can load it from inside the jar. */
    public static void register() {
        ModContainer mod = FabricLoader.getInstance().getModContainer(BetterPrisonsClient.MOD_ID).orElse(null);
        if (mod == null) {
            BetterPrisonsClient.LOGGER.warn("Could not find mod container; PrisonBreak texture pack not registered");
            return;
        }
        ResourceManagerHelper.registerBuiltinResourcePack(PACK_ID, mod, ResourcePackActivationType.NORMAL);
    }

    /**
     * Called each client tick with whether the player is currently in the
     * prisonbreak world. Enables/disables the pack only on state change.
     */
    public static void update(boolean inPrisonbreak) {
        boolean want = inPrisonbreak && BetterPrisonsClient.config.prisonbreakTexturePackEnabled;
        if (want == applied) return;
        setApplied(want);
    }

    private static void setApplied(boolean enable) {
        MinecraftClient client = MinecraftClient.getInstance();
        ResourcePackManager rpm = client.getResourcePackManager();
        String id = resolveProfileId(rpm);
        if (id == null) {
            BetterPrisonsClient.LOGGER.warn("PrisonBreak texture pack profile not found");
            return;
        }

        boolean changed = enable ? rpm.enable(id) : rpm.disable(id);
        applied = enable;
        if (changed) {
            BetterPrisonsClient.LOGGER.info("PrisonBreak texture pack {}", enable ? "enabled" : "disabled");
            client.reloadResources();
        }
    }

    private static String resolveProfileId(ResourcePackManager rpm) {
        if (resolvedProfileId != null && rpm.hasProfile(resolvedProfileId)) return resolvedProfileId;
        for (String id : rpm.getIds()) {
            if (id.contains("prisonbreak")) {
                resolvedProfileId = id;
                return id;
            }
        }
        return null;
    }
}
