package com.sighs.springmahjong.block;

import com.mojang.serialization.MapCodec;
import com.sighs.springmahjong.entity.SeatEntity;
import com.sighs.springmahjong.game.model.GameManager;
import com.sighs.springmahjong.game.model.GameSession;
import com.sighs.springmahjong.game.model.Seat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Single chair block with horizontal facing.
 * Placed in front of each side of the mahjong table.
 */
public class MahjongChairBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SOUTH_SHAPE = makeShape(
        modelBox(1, 8, 1, 15, 12, 15),
        modelBox(2, 12, 2, 14, 13, 14),
        modelBox(2, 0, 2, 4, 10, 4),
        modelBox(12, 0, 2, 14, 10, 4),
        modelBox(12, 0, 12, 14, 10, 14),
        modelBox(2, 0, 12, 4, 10, 14)
    );
    private static final VoxelShape NORTH_SHAPE = rotateShape(SOUTH_SHAPE, Direction.NORTH);
    private static final VoxelShape EAST_SHAPE = rotateShape(SOUTH_SHAPE, Direction.EAST);
    private static final VoxelShape WEST_SHAPE = rotateShape(SOUTH_SHAPE, Direction.WEST);

    public MahjongChairBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(MahjongChairBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level,
                                   BlockPos pos, CollisionContext context) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                           BlockPos pos, CollisionContext context) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                                BlockPos pos, Player player,
                                                BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos tablePos = findTableBlock(level, pos);
        if (tablePos == null) return InteractionResult.FAIL;

        GameSession session = GameManager.getInstance().getOrCreate(tablePos);
        if (session.isTerminated()) {
            GameManager.getInstance().removeSession(tablePos);
            session = GameManager.getInstance().getOrCreate(tablePos);
        }

        if (session.getPatron(player.getUUID()) == null) {
            session.addPatron(player.getUUID(), player.getName().getString(), false);
        }

        // Map chair facing to seat position
        Seat seat = facingToSeat(state.getValue(FACING));
        if (session.isSeatAvailable(seat)) {
            session.sitDown(player.getUUID(), seat);
            SeatEntity.createAndMount(level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    /** Convert chair facing direction to table seat position. */
    public static Seat facingToSeat(Direction facing) {
        return switch (facing) {
            case NORTH -> Seat.SOUTH;  // Chair faces north = player sits on south side
            case EAST -> Seat.WEST;
            case SOUTH -> Seat.NORTH;
            case WEST -> Seat.EAST;
            default -> Seat.EAST;
        };
    }

    private BlockPos findTableBlock(Level level, BlockPos chairPos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = chairPos.offset(dx, 0, dz);
                if (level.getBlockState(p).getBlock() instanceof MahjongTableBlock) {
                    return p;
                }
            }
        }
        return null;
    }

    private static VoxelShape shapeForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> SOUTH_SHAPE;
        };
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

    private static VoxelShape rotateShape(VoxelShape shape, Direction facing) {
        VoxelShape[] rotated = new VoxelShape[]{Shapes.empty()};
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            VoxelShape part = switch (facing) {
                case NORTH -> unitBox(1 - x2, y1, 1 - z2, 1 - x1, y2, 1 - z1);
                case EAST -> unitBox(1 - z2, y1, x1, 1 - z1, y2, x2);
                case WEST -> unitBox(z1, y1, 1 - x2, z2, y2, 1 - x1);
                default -> unitBox(x1, y1, z1, x2, y2, z2);
            };
            rotated[0] = Shapes.or(rotated[0], part);
        });
        return rotated[0];
    }

    private static VoxelShape unitBox(double x1, double y1, double z1,
                                      double x2, double y2, double z2) {
        return Shapes.box(
            Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
            Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
        );
    }
}
