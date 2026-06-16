package com.sighs.springmahjong.block;

import com.sighs.springmahjong.Springmahjong;
import com.sighs.springmahjong.game.model.GameSession;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Block entity for the mahjong table.
 * Holds a reference to the active GameSession and persists game state to disk.
 */
public class MahjongTableEntity extends BlockEntity {
    private UUID tableId = UUID.randomUUID();
    private @Nullable GameSession session;

    // (BlockPos, BlockState) constructor for BlockEntitySupplier
    public MahjongTableEntity(BlockPos pos, BlockState state) {
        super(Springmahjong.TABLE_ENTITY.get(), pos, state);
    }

    // Full constructor for manual creation
    public MahjongTableEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public UUID getTableId() { return tableId; }

    public @Nullable GameSession getSession() { return session; }

    public void linkSession(GameSession session) {
        this.session = session;
        setChanged();
    }

    public void unlinkSession() {
        this.session = null;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("TableIdMSB", tableId.getMostSignificantBits());
        output.putLong("TableIdLSB", tableId.getLeastSignificantBits());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long msb = input.getLongOr("TableIdMSB", 0L);
        long lsb = input.getLongOr("TableIdLSB", 0L);
        this.tableId = new UUID(msb, lsb);
    }
}
