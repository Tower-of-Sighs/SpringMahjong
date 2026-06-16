package com.sighs.springmahjong.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sighs.springmahjong.Springmahjong;
import com.sighs.springmahjong.block.MahjongTableEntity;
import com.sighs.springmahjong.client.state.ClientGameState;
import com.sighs.springmahjong.game.model.Seat;
import cc.sighs.gb_jMahjong.Tile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Renders all live mahjong pieces through Minecraft's baked block model pipeline.
 * The renderer only places, rotates, and scales instances; model geometry, UVs,
 * lighting layers, and render submission are handled by BlockModelRenderState.
 */
public class MahjongTableRenderer implements BlockEntityRenderer<MahjongTableEntity, MahjongTableRenderer.RenderState> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private static final float TABLE_Y = 1.065f;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float TILE_W = 0.052f;
    private static final float TILE_H = 0.027f;
    private static final float TILE_D = 0.074f;
    private static final float TILE_GAP = 0f;
    private static final float HAND_DIST = 0.68f;
    private static final float WALL_DIST = 0.45f;
    private static final int WALL_STACKS_PER_SIDE = 17;
    private static final int WALL_FULL_TILE_COUNT = WALL_STACKS_PER_SIDE * 4 * 2;
    private static final long TEST_TURN_MS = 3000L;
    private static final float TEST_DRAW_PHASE = 0.45f;
    private static final int TEST_MAX_DISCARDS = 32;

    // Bounds of the supplied Blockbench model in model-space pixels.
    private static final float MODEL_MIN_X = 5.4f / 16f;
    private static final float MODEL_MAX_X = 11.7f / 16f;
    private static final float MODEL_MIN_Y = 0f / 16f;
    private static final float MODEL_MAX_Y = 3f / 16f;
    private static final float MODEL_MIN_Z = 3.42f / 16f;
    private static final float MODEL_MAX_Z = 13.4f / 16f;
    private static final float MODEL_CENTER_X = (MODEL_MIN_X + MODEL_MAX_X) / 2f;
    private static final float MODEL_CENTER_Y = (MODEL_MIN_Y + MODEL_MAX_Y) / 2f;
    private static final float MODEL_CENTER_Z = (MODEL_MIN_Z + MODEL_MAX_Z) / 2f;
    private static final float MODEL_SCALE_X = TILE_W / (MODEL_MAX_X - MODEL_MIN_X);
    private static final float MODEL_SCALE_Y = TILE_H / (MODEL_MAX_Y - MODEL_MIN_Y);
    private static final float MODEL_SCALE_Z = TILE_D / (MODEL_MAX_Z - MODEL_MIN_Z);

    private static final AtomicBoolean LOGGED_TEST_RENDER = new AtomicBoolean(false);

    private static final Map<Seat, List<Tile>> TEST_HANDS = Map.of(
        Seat.SOUTH, tiles(1, 2, 3, 19, 20, 21, 28, 29, 31, 32, 33, 34, 35, 1, 4, 5, 6, 7, 8),
        Seat.EAST, tiles(4, 5, 6, 10, 11, 12, 22, 23, 24, 28, 29, 30, 31, 2, 3, 7, 8, 9),
        Seat.NORTH, tiles(7, 8, 9, 13, 14, 15, 25, 26, 27, 32, 33, 34, 35, 10, 11, 12, 13, 14),
        Seat.WEST, tiles(1, 9, 10, 18, 19, 27, 28, 29, 30, 31, 32, 33, 34, 16, 17, 18, 21, 22)
    );

    public MahjongTableRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(MahjongTableEntity entity, RenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(entity, state, overlay);
        state.renderTestState = entity.getBlockState().is(Springmahjong.TEST_MAHJONG_TABLE.get());
        BlockState tileState = Springmahjong.MAHJONG_TILE.get().defaultBlockState();
        Minecraft.getInstance()
            .getModelManager()
            .getBlockModelSet()
            .get(tileState)
            .update(
            state.tileModel,
            tileState,
            BLOCK_DISPLAY_CONTEXT,
            0L
        );
    }

    @Override
    public void submit(RenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        ClientGameState gameState = ClientGameState.INSTANCE;
        if (state.renderTestState && LOGGED_TEST_RENDER.compareAndSet(false, true)) {
            org.slf4j.LoggerFactory.getLogger(MahjongTableRenderer.class)
                .info("Rendering test mahjong table tiles");
        }

        pose.pushPose();
        pose.translate(0.5f, TABLE_Y, 0.5f);
        if (state.renderTestState) {
            renderTestTiles(state, pose, collector, testStep(), testPhase());
        } else if (gameState.getCurrentSessionId() == null) {
            renderReadyTable(state, pose, collector);
        } else {
            renderAllTiles(state, pose, collector, gameState);
        }
        pose.popPose();
    }

    private void renderReadyTable(RenderState state, PoseStack pose, SubmitNodeCollector collector) {
        renderWallTiles(state, pose, collector, WALL_FULL_TILE_COUNT);
    }

    private void renderAllTiles(RenderState state, PoseStack pose, SubmitNodeCollector collector, ClientGameState gameState) {
        renderHands(state, pose, collector, gameState);
        renderDiscards(state, pose, collector, gameState);
        renderWall(state, pose, collector, gameState);
    }

    private void renderHands(RenderState state, PoseStack pose, SubmitNodeCollector collector, ClientGameState gameState) {
        Seat mySeat = gameState.getMySeat();
        boolean seeAll = gameState.canSeeAllHands();

        for (Seat seat : Seat.values()) {
            List<Tile> tiles = seat == mySeat
                ? gameState.getMyHand()
                : gameState.getOpponentHands().getOrDefault(seat, List.of());
            if (tiles == null || tiles.isEmpty()) continue;

            Vector3f origin = getHandOrigin(seat, tiles.size());
            for (int i = 0; i < tiles.size(); i++) {
                if (seat != mySeat && !seeAll) {
                    renderTile(state, pose, collector, getHandTilePos(seat, origin, i), seat);
                } else {
                    renderTile(state, pose, collector, getHandTilePos(seat, origin, i), seat);
                }
            }
        }
    }

    private void renderDiscards(RenderState state, PoseStack pose, SubmitNodeCollector collector, ClientGameState gameState) {
        for (Map.Entry<Seat, List<Tile>> entry : gameState.getDiscards().entrySet()) {
            Seat seat = entry.getKey();
            List<Tile> discards = entry.getValue();
            for (int i = 0; i < discards.size(); i++) {
                renderTile(state, pose, collector, getDiscardPos(seat, i), seat);
            }
        }
    }

    private void renderWall(RenderState state, PoseStack pose, SubmitNodeCollector collector, ClientGameState gameState) {
        int remaining = gameState.getRemainingWall();
        if (remaining > 0) {
            renderWallTiles(state, pose, collector, Math.min(remaining, WALL_FULL_TILE_COUNT));
        }
    }

    private void renderWallTiles(RenderState state, PoseStack pose, SubmitNodeCollector collector, int tileCount) {
        int remaining = Math.max(0, Math.min(tileCount, WALL_FULL_TILE_COUNT));
        for (Seat seat : Seat.values()) {
            int sideTiles = Math.min(remaining, WALL_STACKS_PER_SIDE * 2);
            renderWallSide(state, pose, collector, seat, sideTiles);
            remaining -= sideTiles;
            if (remaining <= 0) return;
        }
    }

    private void renderWallSide(RenderState state, PoseStack pose, SubmitNodeCollector collector, Seat seat, int tileCount) {
        int stacks = (tileCount + 1) / 2;
        for (int i = 0; i < stacks; i++) {
            Vector3f pos = getWallStackPos(seat, i);
            int tilesInStack = Math.min(2, tileCount - i * 2);
            for (int layer = 0; layer < tilesInStack; layer++) {
                renderTile(state, pose, collector, new Vector3f(pos.x(), layer * TILE_H + 0.003f, pos.z()), seat);
            }
        }
    }

    private void renderTestTiles(RenderState state, PoseStack pose, SubmitNodeCollector collector, int step, float phase) {
        Seat activeSeat = activeTestSeat(step);
        for (Seat seat : Seat.values()) {
            List<Tile> tiles = visibleTestHand(seat, step, phase);
            Vector3f origin = getHandOrigin(seat, tiles.size());
            for (int i = 0; i < tiles.size(); i++) {
                boolean drawnTile = seat == activeSeat && phase < TEST_DRAW_PHASE && i == tiles.size() - 1;
                Vector3f pos = getHandTilePos(seat, origin, i);
                renderTile(state, pose, collector,
                    new Vector3f(
                        pos.x() + (drawnTile ? drawnTileXOffset(seat) : 0f),
                        drawnTile ? 0.025f : 0.001f,
                        pos.z() + (drawnTile ? drawnTileZOffset(seat) : 0f)
                    ),
                    seat);
            }
        }

        List<TestDiscard> discards = visibleTestDiscards(step, phase);
        int[] seatDiscardCounts = new int[Seat.values().length];
        for (TestDiscard discard : discards) {
            Vector3f pos = getDiscardPos(discard.seat(), seatDiscardCounts[discard.seat().index()]++);
            renderTile(state, pose, collector, new Vector3f(pos.x(), 0.004f, pos.z()), discard.seat());
        }
    }

    private void renderTile(RenderState state, PoseStack pose, SubmitNodeCollector collector, Vector3f pos, Seat seat) {
        pose.pushPose();
        pose.translate(pos.x(), pos.y(), pos.z());
        rotateForSeat(pose, seat);
        pose.scale(MODEL_SCALE_X, MODEL_SCALE_Y, MODEL_SCALE_Z);
        pose.translate(-MODEL_CENTER_X, -MODEL_MIN_Y, -MODEL_CENTER_Z);
        state.tileModel.submit(pose, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    private void rotateForSeat(PoseStack pose, Seat seat) {
        switch (seat) {
            case EAST -> pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            case NORTH -> pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
            case WEST -> pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270));
            default -> { }
        }
    }

    private Vector3f getHandOrigin(Seat seat, int tileCount) {
        float totalW = tileCount * (TILE_W + TILE_GAP);
        float sx = -totalW / 2f;
        return switch (seat) {
            case SOUTH -> new Vector3f(sx, 0, HAND_DIST);
            case NORTH -> new Vector3f(-sx, 0, -HAND_DIST);
            case EAST -> new Vector3f(HAND_DIST, 0, -sx);
            case WEST -> new Vector3f(-HAND_DIST, 0, sx);
        };
    }

    private Vector3f getHandTilePos(Seat seat, Vector3f origin, int index) {
        float offset = index * (TILE_W + TILE_GAP);
        return switch (seat) {
            case EAST -> new Vector3f(origin.x(), 0.001f, origin.z() - offset);
            case WEST -> new Vector3f(origin.x(), 0.001f, origin.z() + offset);
            default -> new Vector3f(origin.x() + offset, 0.001f, origin.z());
        };
    }

    private Vector3f getDiscardPos(Seat seat, int index) {
        int columns = 6;
        int row = index / columns;
        int col = index % columns;
        float x = (col - (columns - 1) / 2f) * (TILE_W + TILE_GAP);
        float z = row * (TILE_D + TILE_GAP * 1.5f);
        float base = 0.14f;
        return switch (seat) {
            case EAST -> new Vector3f(base + z, 0.002f, -x);
            case SOUTH -> new Vector3f(x, 0.002f, base + z);
            case WEST -> new Vector3f(-base - z, 0.002f, x);
            case NORTH -> new Vector3f(-x, 0.002f, -base - z);
        };
    }

    private Vector3f getWallStackPos(Seat seat, int index) {
        float step = TILE_W + TILE_GAP;
        float offset = (index - (WALL_STACKS_PER_SIDE - 1) / 2f) * step;
        return switch (seat) {
            case EAST -> new Vector3f(WALL_DIST, 0, -offset);
            case SOUTH -> new Vector3f(offset, 0, WALL_DIST);
            case WEST -> new Vector3f(-WALL_DIST, 0, offset);
            case NORTH -> new Vector3f(-offset, 0, -WALL_DIST);
        };
    }

    private float drawnTileXOffset(Seat seat) {
        return switch (seat) {
            case EAST -> 0.035f;
            case WEST -> -0.035f;
            default -> 0f;
        };
    }

    private float drawnTileZOffset(Seat seat) {
        return switch (seat) {
            case SOUTH -> 0.035f;
            case NORTH -> -0.035f;
            default -> 0f;
        };
    }

    private static List<Tile> tiles(int... ids) {
        List<Tile> result = new java.util.ArrayList<>(ids.length);
        for (int id : ids) result.add(new Tile(id));
        return List.copyOf(result);
    }

    private int testStep() {
        return (int) ((System.currentTimeMillis() / TEST_TURN_MS) % 1024L);
    }

    private float testPhase() {
        return (System.currentTimeMillis() % TEST_TURN_MS) / (float) TEST_TURN_MS;
    }

    private Seat activeTestSeat(int step) {
        return Seat.fromIndex(Math.floorMod(step, Seat.values().length));
    }

    private List<Tile> visibleTestHand(Seat seat, int step, float phase) {
        List<Tile> tiles = TEST_HANDS.getOrDefault(seat, List.of());
        int seatOffset = seat.ordinal();
        int base = 13;
        Seat activeSeat = activeTestSeat(step);
        int visible = base + (seat == activeSeat && phase < TEST_DRAW_PHASE ? 1 : 0);
        int start = Math.floorDiv(step + seatOffset * 3, Seat.values().length);
        List<Tile> result = new java.util.ArrayList<>(visible);
        for (int i = 0; i < visible; i++) {
            result.add(tiles.get((start + i) % tiles.size()));
        }
        return result;
    }

    private List<TestDiscard> visibleTestDiscards(int step, float phase) {
        int count = Math.min(TEST_MAX_DISCARDS, step + (phase >= TEST_DRAW_PHASE ? 1 : 0));
        int first = Math.max(0, count - TEST_MAX_DISCARDS);
        List<TestDiscard> result = new java.util.ArrayList<>(count - first);
        for (int i = first; i < count; i++) {
            Seat seat = activeTestSeat(i);
            List<Tile> hand = TEST_HANDS.getOrDefault(seat, List.of());
            int tileIndex = Math.floorDiv(i, Seat.values().length) + 13;
            result.add(new TestDiscard(seat, hand.get(tileIndex % hand.size())));
        }
        return result;
    }

    private record TestDiscard(Seat seat, Tile tile) {}

    public static class RenderState extends BlockEntityRenderState {
        final BlockModelRenderState tileModel = new BlockModelRenderState();
        boolean renderTestState;
    }
}
