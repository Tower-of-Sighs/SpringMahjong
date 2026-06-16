package com.sighs.springmahjong.client;

import cc.sighs.gb_jMahjong.Tile;
import com.sighs.springmahjong.ai.AiController;
import com.sighs.springmahjong.game.event.GameEvent;
import com.sighs.springmahjong.game.model.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Disabled: remove comment to re-enable auto test
//@EventBusSubscriber(value = Dist.CLIENT)
public class GameTestRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger("MahjongTest");
    private static boolean started = false;
    private static GameSession session;
    private static final long firstLoadTime = System.currentTimeMillis();
    private static int gameCount = 0;
    private static boolean scheduleNext = false;
    private static final int MAX_GAMES = 5;
    private static final int[] fanTotals = new int[MAX_GAMES];
    private static final Map<Seat, Integer>[] finalScores = new HashMap[MAX_GAMES];

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return;
        if (!started) {
            if (System.currentTimeMillis() < firstLoadTime + 5000) return;
            started = true;
            runNextGame();
        } else if (scheduleNext) {
            scheduleNext = false;
            runNextGame();
        }
    }

    private static void runNextGame() {
        if (gameCount >= MAX_GAMES) {
            LOGGER.info("========== ALL {} GAMES COMPLETE ==========", MAX_GAMES);
            LOGGER.info("=== SUMMARY ===");
            for (int i = 0; i < MAX_GAMES; i++) {
                LOGGER.info("Game {}: final scores {}. Max fan: {}", i + 1, finalScores[i], fanTotals[i]);
            }
            // Calculate average stats
            double avgFan = Arrays.stream(fanTotals).average().orElse(0);
            LOGGER.info("Average max fan: {:.1f}", avgFan);
            LOGGER.info("Stopping client...");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().stop());
                }
            }, 1000);
            return;
        }

        int gameNum = gameCount + 1;
        LOGGER.info("");
        LOGGER.info("========== GAME {} / {} ==========", gameNum, MAX_GAMES);
        session = new GameSession(GameSettings.DEBUG);

        // Summary-only logging for 5-game test
        session.eventBus().register(GameEvent.SettlementEvent.class, e -> {
            LOGGER.info("[{}][WIN] {} wins! Fan: {} (selfDraw={})", gameNum, e.winnerSeat(), e.totalFan(), e.isSelfDraw());
            fanTotals[gameCount] = e.totalFan();
        });

        session.eventBus().register(GameEvent.GameEndEvent.class, e -> {
            Map<Seat, Integer> scores = new HashMap<>(e.finalScores());
            finalScores[gameCount] = scores;
            LOGGER.info("[{}][END] Final: EAST={} SOUTH={} WEST={} NORTH={}",
                gameNum, scores.get(Seat.EAST), scores.get(Seat.SOUTH),
                scores.get(Seat.WEST), scores.get(Seat.NORTH));
            gameCount++;
            scheduleNext = true; // Start next game on next client tick (fresh stack)
        });

        // Add 4 bots
        UUID botE = UUID.nameUUIDFromBytes("Bot:EAST".getBytes());
        UUID botS = UUID.nameUUIDFromBytes("Bot:SOUTH".getBytes());
        UUID botW = UUID.nameUUIDFromBytes("Bot:WEST".getBytes());
        UUID botN = UUID.nameUUIDFromBytes("Bot:NORTH".getBytes());

        session.addPatron(botE, "Bot-东", true);
        session.addPatron(botS, "Bot-南", true);
        session.addPatron(botW, "Bot-西", true);
        session.addPatron(botN, "Bot-北", true);

        session.sitDown(botE, Seat.EAST);
        session.sitDown(botS, Seat.SOUTH);
        session.sitDown(botW, Seat.WEST);
        session.sitDown(botN, Seat.NORTH);

        AiController ai = new AiController(session);
        ai.register(session.eventBus());

        LOGGER.info("[{}] Starting...", gameNum);
        session.startGame();
    }
}
