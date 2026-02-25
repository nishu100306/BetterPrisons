package BetterPrisons.modid.render;

import java.util.HashSet;
import java.util.Set;

/**
 * Shared state for the peaceful mining ghost rendering system.
 * Holds the set of entity network IDs that should be rendered as translucent ghosts.
 * Written by PeacefulMiningMixin (PlayerEntityRenderer), read by PeacefulMiningRendererMixin (LivingEntityRenderer).
 */
public class PeacefulMiningState {
    public static final Set<Integer> TARGETS = new HashSet<>();
}
