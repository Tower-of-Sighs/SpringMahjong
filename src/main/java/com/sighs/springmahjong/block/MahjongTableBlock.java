package com.sighs.springmahjong.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MahjongTableBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = makeShape(
        modelBox(-7, 13, -7, 0, 16, -4),
        modelBox(-7, 13, -4, -5, 16, 0),
        modelBox(-5, 13, -4, 0, 15.7, 0),
        modelBox(-7, 13, 0, -4, 16, 23),
        modelBox(-4, 13, 0, 0, 16, 1),
        modelBox(-4, 13, 1, 0, 15.7, 21),
        modelBox(-4, 13, 21, 0, 16, 23),
        modelBox(0, 13, -7, 16, 16, -4),
        modelBox(0, 13, -4, 15, 15.7, 0),
        modelBox(15, 13, -4, 16, 16, 0),
        modelBox(0, 13, 0, 16, 16, 16),
        modelBox(16, 13, -7, 20, 16, -5),
        modelBox(16, 13, -5, 20, 15.7, 15),
        modelBox(16, 13, 15, 20, 16, 16),
        modelBox(20, 13, -7, 23, 16, 16),
        modelBox(0, 13, 16, 1, 16, 20),
        modelBox(1, 13, 16, 21, 15.7, 20),
        modelBox(21, 13, 16, 23, 16, 20),
        modelBox(0, 13, 20, 23, 16, 23),
        modelBox(-10, 13, -10, 26, 17, 26),
        modelBox(23, 17, 23, -7, 13, -7)
    );

    public MahjongTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(MahjongTableBlock::new);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level,
                                   BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                           BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MahjongTableEntity(pos, state);
    }

    private static VoxelShape makeShape(VoxelShape... shapes) {
        return Shapes.or(Shapes.empty(), shapes);
    }

    private static VoxelShape modelBox(double x1, double y1, double z1,
                                       double x2, double y2, double z2) {
        return Block.box(
            Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
            Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
        );
    }
}
