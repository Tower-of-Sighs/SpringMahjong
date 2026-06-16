package com.sighs.springmahjong.block;

import com.sighs.springmahjong.Springmahjong;
import com.sighs.springmahjong.game.TestGameAnnouncer;
import com.sighs.springmahjong.game.model.GameManager;
import com.sighs.springmahjong.game.model.GameSession;
import com.sighs.springmahjong.game.model.GameSettings;
import com.sighs.springmahjong.game.model.Seat;
import com.sighs.springmahjong.network.NetworkBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class TestMahjongTableBlock extends MahjongTableBlock {
    public TestMahjongTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel) {
            setupTestTable(serverLevel, pos);
        }
    }

    private void setupTestTable(ServerLevel level, BlockPos tablePos) {
        placeChair(level, tablePos.south(2), Direction.NORTH);
        placeChair(level, tablePos.north(2), Direction.SOUTH);
        placeChair(level, tablePos.east(2), Direction.WEST);
        placeChair(level, tablePos.west(2), Direction.EAST);

        GameSession session = GameManager.getInstance()
            .getOrCreate(tablePos, GameSettings.RENDER_TEST);

        for (Seat seat : Seat.values()) {
            UUID uuid = UUID.nameUUIDFromBytes(("RenderTestBot:" + tablePos + ":" + seat).getBytes());
            if (session.getPatron(uuid) == null) {
                session.addPatron(uuid, "TestBot-" + seat.name(), true);
            }
            session.sitDown(uuid, seat);
        }

        new NetworkBridge(session, level.getServer()).register(session.eventBus());
        new TestGameAnnouncer(session, level.getServer()).register(session.eventBus());
        level.getServer().getPlayerList().broadcastSystemMessage(
            net.minecraft.network.chat.Component.literal("[测试麻将] 已放置测试桌，自动补椅子并启动四个电脑玩家。"),
            false);
        session.startGame();
    }

    private void placeChair(ServerLevel level, BlockPos pos, Direction facing) {
        if (!level.getBlockState(pos).canBeReplaced()) return;
        BlockState chairState = Springmahjong.MAHJONG_CHAIR.get()
            .defaultBlockState()
            .setValue(MahjongChairBlock.FACING, facing);
        level.setBlock(pos, chairState, 3);
    }
}
