package konhaiii.faster_hopper.block.golden_hopper;

import com.mojang.serialization.MapCodec;
import konhaiii.faster_hopper.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class GoldenHopperBlock extends BlockWithEntity {
	public static final MapCodec<GoldenHopperBlock> CODEC = createCodec(GoldenHopperBlock::new);
	public static final EnumProperty<Direction> FACING = Properties.HOPPER_FACING;
	public static final BooleanProperty ENABLED = Properties.ENABLED;
	private static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape MIDDLE_SHAPE = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 10.0, 12.0);
	private static final VoxelShape OUTSIDE_SHAPE = VoxelShapes.union(MIDDLE_SHAPE, TOP_SHAPE);
	private static final VoxelShape INSIDE_SHAPE = createCuboidShape(2.0, 11.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape DEFAULT_SHAPE = VoxelShapes.combineAndSimplify(OUTSIDE_SHAPE, INSIDE_SHAPE, BooleanBiFunction.ONLY_FIRST);
	private static final VoxelShape DOWN_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 4.0, 10.0));
	private static final VoxelShape EAST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(12.0, 4.0, 6.0, 16.0, 8.0, 10.0));
	private static final VoxelShape NORTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0, 4.0, 0.0, 10.0, 8.0, 4.0));
	private static final VoxelShape SOUTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0, 4.0, 12.0, 10.0, 8.0, 16.0));
	private static final VoxelShape WEST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(0.0, 4.0, 6.0, 4.0, 8.0, 10.0));
	private static final VoxelShape DOWN_RAYCAST_SHAPE = INSIDE_SHAPE;
	private static final VoxelShape EAST_RAYCAST_SHAPE = VoxelShapes.union(INSIDE_SHAPE, Block.createCuboidShape(12.0, 8.0, 6.0, 16.0, 10.0, 10.0));
	private static final VoxelShape NORTH_RAYCAST_SHAPE = VoxelShapes.union(INSIDE_SHAPE, Block.createCuboidShape(6.0, 8.0, 0.0, 10.0, 10.0, 4.0));
	private static final VoxelShape SOUTH_RAYCAST_SHAPE = VoxelShapes.union(INSIDE_SHAPE, Block.createCuboidShape(6.0, 8.0, 12.0, 10.0, 10.0, 16.0));
	private static final VoxelShape WEST_RAYCAST_SHAPE = VoxelShapes.union(INSIDE_SHAPE, Block.createCuboidShape(0.0, 8.0, 6.0, 4.0, 10.0, 10.0));

	@Override
	public MapCodec<GoldenHopperBlock> getCodec() {
		return CODEC;
	}

	public GoldenHopperBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.DOWN).with(ENABLED, true));
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(FACING)) {
			case DOWN -> DOWN_SHAPE;
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
			default -> DEFAULT_SHAPE;
		};
	}

	@Override
	protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return switch (state.get(FACING)) {
			case DOWN -> DOWN_RAYCAST_SHAPE;
			case NORTH -> NORTH_RAYCAST_SHAPE;
			case SOUTH -> SOUTH_RAYCAST_SHAPE;
			case WEST -> WEST_RAYCAST_SHAPE;
			case EAST -> EAST_RAYCAST_SHAPE;
			default -> INSIDE_SHAPE;
		};
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction direction = ctx.getSide().getOpposite();
		return this.getDefaultState().with(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction).with(ENABLED, true);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GoldenHopperBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlocks.GOLDEN_HOPPER_BLOCK_ENTITY, GoldenHopperBlockEntity::serverTick);
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.isOf(state.getBlock())) {
			this.updateEnabled(world, pos, state);
		}
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof GoldenHopperBlockEntity goldenHopperBlockEntity) {
			player.openHandledScreen(goldenHopperBlockEntity);
			player.incrementStat(Stats.INSPECT_HOPPER);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
		this.updateEnabled(world, pos, state);
	}

	private void updateEnabled(World world, BlockPos pos, BlockState state) {
		boolean bl = !world.isReceivingRedstonePower(pos);
		if (bl != state.get(ENABLED)) {
			world.setBlockState(pos, state.with(ENABLED, bl), Block.NOTIFY_LISTENERS);
		}
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScatterer.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	protected boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, ENABLED);
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof GoldenHopperBlockEntity) {
			GoldenHopperBlockEntity.onEntityCollided(world, pos, state, entity, (GoldenHopperBlockEntity)blockEntity);
		}
	}

	@Override
	protected boolean canPathfindThrough(BlockState state, NavigationType type) {
		return false;
	}
}
