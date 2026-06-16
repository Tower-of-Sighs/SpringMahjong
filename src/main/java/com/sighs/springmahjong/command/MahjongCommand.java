package com.sighs.springmahjong.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sighs.springmahjong.ai.AiController;
import com.sighs.springmahjong.game.model.GameManager;
import com.sighs.springmahjong.game.model.GameSession;
import com.sighs.springmahjong.game.model.Seat;
import com.sighs.springmahjong.network.NetworkBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission.HasCommandLevel;
import net.minecraft.server.permissions.PermissionLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class MahjongCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("mahjong")

            .then(Commands.literal("create")
                .executes(MahjongCommand::createGame))

            .then(Commands.literal("join")
                .executes(MahjongCommand::joinGame))

            .then(Commands.literal("start")
                .requires(src -> src.permissions().hasPermission(
                    new HasCommandLevel(PermissionLevel.GAMEMASTERS)))
                .executes(MahjongCommand::startGame))

            .then(Commands.literal("leave")
                .executes(MahjongCommand::leaveGame))

            .then(Commands.literal("addbot")
                .requires(src -> src.permissions().hasPermission(
                    new HasCommandLevel(PermissionLevel.GAMEMASTERS)))
                .then(Commands.literal("east").executes(c -> addBot(c, Seat.EAST)))
                .then(Commands.literal("south").executes(c -> addBot(c, Seat.SOUTH)))
                .then(Commands.literal("west").executes(c -> addBot(c, Seat.WEST)))
                .then(Commands.literal("north").executes(c -> addBot(c, Seat.NORTH))))

            .then(Commands.literal("debug")
                .requires(src -> src.permissions().hasPermission(
                    new HasCommandLevel(PermissionLevel.ADMINS)))
                .then(Commands.literal("hand")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(MahjongCommand::debugHand))));

        event.getDispatcher().register(root);
    }

    // ==================== Commands ====================

    private static int createGame(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) { src.sendFailure(Component.literal("Player only command")); return 0; }

        GameSession session = GameManager.getInstance().getOrCreate(player.blockPosition());
        if (session.getPatron(player.getUUID()) == null) {
            session.addPatron(player.getUUID(), player.getName().getString(), false);
        }

        MinecraftServer server = src.getServer();
        NetworkBridge bridge = new NetworkBridge(session, server);
        bridge.register(session.eventBus());
        AiController ai = new AiController(session);
        ai.register(session.eventBus());

        src.sendSuccess(() -> Component.literal("Mahjong table created! Use /mahjong join to sit down."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int joinGame(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) { src.sendFailure(Component.literal("Player only command")); return 0; }

        GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
        if (session == null) {
            session = GameManager.getInstance().getOrCreate(player.blockPosition());
        }

        if (session.getPatron(player.getUUID()) == null) {
            session.addPatron(player.getUUID(), player.getName().getString(), false);
            MinecraftServer server = src.getServer();
            NetworkBridge bridge = new NetworkBridge(session, server);
            bridge.register(session.eventBus());
        }

        for (Seat seat : Seat.values()) {
            if (session.isSeatAvailable(seat)) {
                session.sitDown(player.getUUID(), seat);
                src.sendSuccess(() -> Component.literal("You sat at " + seat.name()), true);
                return Command.SINGLE_SUCCESS;
            }
        }

        src.sendFailure(Component.literal("No available seats."));
        return 0;
    }

    private static int startGame(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) { src.sendFailure(Component.literal("Player only command")); return 0; }

        GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
        if (session == null) {
            src.sendFailure(Component.literal("You are not at a mahjong table."));
            return 0;
        }

        if (session.seatedPatrons().size() < 4) {
            session.fillEmptySeatsWithBots();
            src.sendSuccess(() -> Component.literal("Filling empty seats with bots..."), true);
        }

        session.startGame();
        src.sendSuccess(() -> Component.literal("Game started!"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int leaveGame(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) { src.sendFailure(Component.literal("Player only command")); return 0; }

        GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
        if (session != null) {
            session.standUp(player.getUUID());
            session.removePatron(player.getUUID());
            src.sendSuccess(() -> Component.literal("You left the mahjong table."), true);
        } else {
            src.sendFailure(Component.literal("You are not at a mahjong table."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addBot(CommandContext<CommandSourceStack> ctx, Seat seat) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) { src.sendFailure(Component.literal("Player only command")); return 0; }

        GameSession session = GameManager.getInstance().findByPlayer(player.getUUID());
        if (session == null) {
            src.sendFailure(Component.literal("No mahjong session found."));
            return 0;
        }

        UUID botUuid = UUID.nameUUIDFromBytes(("Bot:" + seat.name()).getBytes());
        if (session.getPatron(botUuid) != null || !session.isSeatAvailable(seat)) {
            src.sendFailure(Component.literal("Seat " + seat.name() + " is already occupied."));
            return 0;
        }

        session.addPatron(botUuid, "Bot-" + seat.name(), true);
        session.sitDown(botUuid, seat);

        MinecraftServer server = src.getServer();
        NetworkBridge bridge = new NetworkBridge(session, server);
        bridge.register(session.eventBus());
        AiController ai = new AiController(session);
        ai.register(session.eventBus());

        src.sendSuccess(() -> Component.literal("Bot-" + seat.name() + " added to seat " + seat.name()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int debugHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            GameSession session = GameManager.getInstance().findByPlayer(target.getUUID());
            if (session == null || session.currentTable() == null) {
                src.sendFailure(Component.literal("Player is not in an active game."));
                return 0;
            }

            session.seatedPatrons().forEach((seat, patron) -> {
                if (patron.uuid().equals(target.getUUID())) {
                    String handStr = session.currentTable().getPlayer(seat).hand().handtilesToString();
                    src.sendSuccess(() -> Component.literal(seat + " hand: " + handStr), false);
                }
            });
        } catch (Exception e) {
            src.sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
