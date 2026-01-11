package BetterPrisons.modid.devtools;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.JsonLoader;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Development commands for testing and debugging
 */
public class DevCommands {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        // Register /bpitem command to inspect held item
        dispatcher.register(ClientCommandManager.literal("bpitem")
                .executes(DevCommands::inspectHeldItem));

        // Register /bpscoreboard command to dump scoreboard
        dispatcher.register(ClientCommandManager.literal("bpscoreboard")
                .executes(DevCommands::dumpScoreboard));

        // Register /bpblock command to get block coordinates
        dispatcher.register(ClientCommandManager.literal("bpblock")
                .executes(DevCommands::getBlockLookingAt));

        dispatcher.register(ClientCommandManager.literal("bploadcmd")
                .executes(DevCommands::createDefaultCommands));
    }

    private static int inspectHeldItem(CommandContext<FabricClientCommandSource> context) {
        var client = context.getSource().getClient();
        var player = client.player;

        if (player == null) {
            BetterPrisonsClient.LOGGER.info("[DevTools] No player found");
            return 0;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            BetterPrisonsClient.LOGGER.info("[DevTools] No item in main hand");
            context.getSource().sendFeedback(Text.literal("§cNo item in main hand"));
            return 0;
        }

        // Log item information
        BetterPrisonsClient.LOGGER.info("========== ITEM INSPECTION ==========");
        BetterPrisonsClient.LOGGER.info("Item: " + stack.getItem().toString());
        BetterPrisonsClient.LOGGER.info("Registry Name: " + stack.getItem().getTranslationKey());
        BetterPrisonsClient.LOGGER.info("Display Name: " + stack.getName().getString());
        BetterPrisonsClient.LOGGER.info("Count: " + stack.getCount());

        // Log lore
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            BetterPrisonsClient.LOGGER.info("Lore:");
            int lineNum = 1;
            for (Text line : lore.lines()) {
                String lineText = line.getString();
                BetterPrisonsClient.LOGGER.info("  [" + lineNum + "] " + lineText);
                lineNum++;
            }
        } else {
            BetterPrisonsClient.LOGGER.info("Lore: (none)");
        }

        // Log custom name if present
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) {
            BetterPrisonsClient.LOGGER.info("Custom Name: " + customName.getString());
        }

        // Log NBT/component data
        BetterPrisonsClient.LOGGER.info("Components: " + stack.getComponents().toString());
        BetterPrisonsClient.LOGGER.info("====================================");

        // Send feedback to player
        context.getSource().sendFeedback(Text.literal("§aItem details logged to console. Check latest.log"));

        return 1;
    }

    private static int dumpScoreboard(CommandContext<FabricClientCommandSource> context) {
        var client = context.getSource().getClient();

        if (client.world == null) {
            BetterPrisonsClient.LOGGER.info("[DevTools] No world loaded");
            return 0;
        }

        var scoreboard = client.world.getScoreboard();

        BetterPrisonsClient.LOGGER.info("========== SCOREBOARD DUMP ==========");

        // Dump sidebar specifically (most important)
        BetterPrisonsClient.LOGGER.info("\n--- SIDEBAR ---");
        var sidebarObjective = scoreboard.getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (sidebarObjective != null) {
            BetterPrisonsClient.LOGGER.info("Objective: " + sidebarObjective.getName());
            BetterPrisonsClient.LOGGER.info("Display Name: " + sidebarObjective.getDisplayName().getString());
            BetterPrisonsClient.LOGGER.info("Criterion: " + sidebarObjective.getRenderType().getName());

            var sidebarEntries = scoreboard.getScoreboardEntries(sidebarObjective);
            if (!sidebarEntries.isEmpty()) {
                BetterPrisonsClient.LOGGER.info("Entries:");
                int lineNum = 1;
                for (var entry : sidebarEntries) {
                    String owner = entry.owner();
                    int value = entry.value();

                    // Get the full display text
                    Text displayText;

                    // First, check if there's a direct display override
                    Text directDisplay = entry.display();
                    if (directDisplay != null) {
                        displayText = directDisplay;

                    } else {
                        // No direct display - check for team formatting
                        var team = scoreboard.getScoreHolderTeam(owner);
                        if (team != null) {
                            // Apply team prefix + owner + suffix + color
                            displayText = Team.decorateName(team, Text.literal(owner));
                        } else {
                            // No team, just use the raw owner
                            displayText = Text.literal(owner);
                        }
                    }
                    BetterPrisonsClient.LOGGER.info("displayText: ", displayText.getString());

                    String fullText = displayText.getString();
                    String stripped = fullText.replaceAll("§.", "");
                    BetterPrisonsClient.LOGGER.info("  [" + lineNum + "] DISPLAY: \"" + fullText + "\" | STRIPPED: \"" + stripped + "\" | OWNER: \"" + owner + "\" | VALUE: " + value);
                    lineNum++;
                }
            } else {
                BetterPrisonsClient.LOGGER.info("Entries: (none)");
            }
        } else {
            BetterPrisonsClient.LOGGER.info("(no sidebar objective)");
        }

        BetterPrisonsClient.LOGGER.info("\n====================================");

        context.getSource().sendFeedback(Text.literal("§aScoreboard dumped to console. Check latest.log"));

        return 1;
    }

    private static int getBlockLookingAt(CommandContext<FabricClientCommandSource> context) {
        var client = context.getSource().getClient();
        var player = client.player;

        if (player == null) {
            BetterPrisonsClient.LOGGER.info("[DevTools] No player found");
            return 0;
        }

        // Get the crosshair target (what the player is looking at)
        HitResult hitResult = client.crosshairTarget;

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            BetterPrisonsClient.LOGGER.info("[DevTools] Not looking at a block");
            context.getSource().sendFeedback(Text.literal("§cNot looking at a block"));
            return 0;
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos blockPos = blockHit.getBlockPos();

        // Log block information
        BetterPrisonsClient.LOGGER.info("========== BLOCK COORDINATES ==========");
        BetterPrisonsClient.LOGGER.info("Block Position: x=" + blockPos.getX() + ", y=" + blockPos.getY() + ", z=" + blockPos.getZ());
        BetterPrisonsClient.LOGGER.info("Block: " + client.world.getBlockState(blockPos).getBlock().toString());
        BetterPrisonsClient.LOGGER.info("=======================================");

        // Send feedback to player
        String message = "§aBlock: §f" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
        context.getSource().sendFeedback(Text.literal(message));

        return 1;
    }

    private static int createDefaultCommands(CommandContext<FabricClientCommandSource> context) {
        JsonLoader.createDefaultCommands();
        context.getSource().sendFeedback(Text.literal("§aDefault commands configuration created/overwritten."));
        return 1;
    }
}
