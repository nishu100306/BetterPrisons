package BetterPrisons.modid.devtools;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.JsonLoader;
import BetterPrisons.modid.hud.EventsHud;
import BetterPrisons.modid.ui.custom.screens.WaypointsScreen;
import BetterPrisons.modid.waypoint.CustomWaypoint;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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

        // /bpevents — test commands for EventsHud
        dispatcher.register(ClientCommandManager.literal("bpevents")
            .then(ClientCommandManager.literal("clear")
                .executes(ctx -> {
                    BetterPrisonsClient.eventsHud.clearMeteors();
                    BetterPrisonsClient.eventsHud.clearMerchants();
                    ctx.getSource().sendFeedback(Text.literal("§aCleared all meteors and merchants."));
                    return 1;
                }))
            .then(ClientCommandManager.literal("meteor")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                    .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                        // /bpevents meteor add <x> <y> <z>  — defaults to natural
                        .executes(ctx -> addMeteor(ctx, EventsHud.MeteorType.NATURAL))
                        .then(ClientCommandManager.argument("type", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("natural");
                                builder.suggest("summoned");
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String type = StringArgumentType.getString(ctx, "type");
                                EventsHud.MeteorType meteorType = type.equalsIgnoreCase("summoned")
                                    ? EventsHud.MeteorType.SUMMONED : EventsHud.MeteorType.NATURAL;
                                return addMeteor(ctx, meteorType);
                            }))))))
                .then(ClientCommandManager.literal("crash")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                    .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            int x = IntegerArgumentType.getInteger(ctx, "x");
                            int y = IntegerArgumentType.getInteger(ctx, "y");
                            int z = IntegerArgumentType.getInteger(ctx, "z");
                            String coordsLine = x + "x, " + y + "y, " + z + "z";
                            BetterPrisonsClient.eventsHud.onMeteorCrashed(coordsLine);
                            ctx.getSource().sendFeedback(Text.literal("§aMeteor crashed at " + coordsLine));
                            return 1;
                        })))))
                .then(ClientCommandManager.literal("clear")
                    .executes(ctx -> {
                        BetterPrisonsClient.eventsHud.clearMeteors();
                        ctx.getSource().sendFeedback(Text.literal("§aCleared all meteors."));
                        return 1;
                    })))
            .then(ClientCommandManager.literal("merchant")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("tier", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (EventsHud.MerchantType t : EventsHud.MerchantType.values()) {
                                if (t != EventsHud.MerchantType.UNKNOWN)
                                    builder.suggest(t.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                        .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                        .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                            .executes(ctx -> {
                                String tier = StringArgumentType.getString(ctx, "tier");
                                int x = IntegerArgumentType.getInteger(ctx, "x");
                                int y = IntegerArgumentType.getInteger(ctx, "y");
                                int z = IntegerArgumentType.getInteger(ctx, "z");
                                BetterPrisonsClient.eventsHud.onMerchantSpawned(tier, x, y, z);
                                ctx.getSource().sendFeedback(Text.literal(
                                    "§aAdded " + tier + " merchant at " + x + ", " + y + ", " + z));
                                return 1;
                            }))))))
                .then(ClientCommandManager.literal("kill")
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                    .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            int x = IntegerArgumentType.getInteger(ctx, "x");
                            int y = IntegerArgumentType.getInteger(ctx, "y");
                            int z = IntegerArgumentType.getInteger(ctx, "z");
                            BetterPrisonsClient.eventsHud.onMerchantSlain("unknown", x, y, z);
                            ctx.getSource().sendFeedback(Text.literal(
                                "§aMerchant killed at " + x + ", " + y + ", " + z));
                            return 1;
                        })))))
                .then(ClientCommandManager.literal("clear")
                    .executes(ctx -> {
                        BetterPrisonsClient.eventsHud.clearMerchants();
                        ctx.getSource().sendFeedback(Text.literal("§aCleared all merchants."));
                        return 1;
                    }))));

        // /bpworld — print current world key
        dispatcher.register(ClientCommandManager.literal("bpworld")
            .executes(ctx -> {
                String world = BetterPrisons.modid.waypoint.WaypointManager.detectWorldKey();
                ctx.getSource().sendFeedback(Text.literal("§7Current world: §f" + world));
                return 1;
            }));

        // /bpwaypoints — open the waypoints management screen
        dispatcher.register(ClientCommandManager.literal("bpwaypoints")
            .executes(ctx -> {
                ctx.getSource().getClient().setScreen(new WaypointsScreen());
                return 1;
            }));

        // /bpwaypoint — CRUD commands
        dispatcher.register(ClientCommandManager.literal("bpwaypoint")
            .then(ClientCommandManager.literal("add")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                    // /bpwaypoint add <name> <x> <y> <z>  — white by default
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name");
                        int x = IntegerArgumentType.getInteger(ctx, "x");
                        int y = IntegerArgumentType.getInteger(ctx, "y");
                        int z = IntegerArgumentType.getInteger(ctx, "z");
                        BetterPrisonsClient.waypointManager.add(new CustomWaypoint(name, x, y, z, 0xFFFFFF));
                        ctx.getSource().sendFeedback(Text.literal("§aWaypoint '" + name + "' added at " + x + ", " + y + ", " + z));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("color", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            int x = IntegerArgumentType.getInteger(ctx, "x");
                            int y = IntegerArgumentType.getInteger(ctx, "y");
                            int z = IntegerArgumentType.getInteger(ctx, "z");
                            int color;
                            try {
                                color = (int) Long.parseLong(
                                    StringArgumentType.getString(ctx, "color").replace("#",""), 16) & 0xFFFFFF;
                            } catch (NumberFormatException e) { color = 0xFFFFFF; }
                            BetterPrisonsClient.waypointManager.add(new CustomWaypoint(name, x, y, z, color));
                            ctx.getSource().sendFeedback(Text.literal("§aWaypoint '" + name + "' added."));
                            return 1;
                        })))))))
            .then(ClientCommandManager.literal("here")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> {
                        var player = ctx.getSource().getClient().player;
                        if (player == null) return 0;
                        String name = StringArgumentType.getString(ctx, "name");
                        int x = (int) player.getX(), y = (int) player.getY(), z = (int) player.getZ();
                        BetterPrisonsClient.waypointManager.add(new CustomWaypoint(name, x, y, z, 0xFFFFFF));
                        ctx.getSource().sendFeedback(Text.literal("§aWaypoint '" + name + "' added at your position."));
                        return 1;
                    })))
            .then(ClientCommandManager.literal("remove")
                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "name");
                        var wps = BetterPrisonsClient.waypointManager.getAll();
                        for (int i = 0; i < wps.size(); i++) {
                            if (wps.get(i).name.equalsIgnoreCase(name)) {
                                BetterPrisonsClient.waypointManager.remove(i);
                                ctx.getSource().sendFeedback(Text.literal("§aRemoved waypoint '" + name + "'."));
                                return 1;
                            }
                        }
                        ctx.getSource().sendFeedback(Text.literal("§cNo waypoint named '" + name + "'."));
                        return 0;
                    })))
            .then(ClientCommandManager.literal("list")
                .executes(ctx -> {
                    var wps = BetterPrisonsClient.waypointManager.getAll();
                    if (wps.isEmpty()) {
                        ctx.getSource().sendFeedback(Text.literal("§7No waypoints."));
                        return 1;
                    }
                    ctx.getSource().sendFeedback(Text.literal("§eWaypoints:"));
                    for (CustomWaypoint wp : wps) {
                        String status = wp.enabled ? "§a[ON]" : "§c[OFF]";
                        ctx.getSource().sendFeedback(Text.literal(
                            status + " §f" + wp.name + " §7" + wp.x + ", " + wp.y + ", " + wp.z));
                    }
                    return 1;
                }))
            .then(ClientCommandManager.literal("clear")
                .executes(ctx -> {
                    int count = BetterPrisonsClient.waypointManager.getAll().size();
                    while (!BetterPrisonsClient.waypointManager.getAll().isEmpty()) {
                        BetterPrisonsClient.waypointManager.remove(0);
                    }
                    ctx.getSource().sendFeedback(Text.literal("§aCleared " + count + " waypoints."));
                    return 1;
                })));
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

    private static int addMeteor(CommandContext<FabricClientCommandSource> ctx, EventsHud.MeteorType type) {
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        String coordsLine = x + "x, " + y + "y, " + z + "z";
        BetterPrisonsClient.eventsHud.onMeteorFalling(coordsLine, type);
        ctx.getSource().sendFeedback(Text.literal(
            "§aAdded " + type.name().toLowerCase() + " meteor at " + x + ", " + y + ", " + z));
        return 1;
    }
}
