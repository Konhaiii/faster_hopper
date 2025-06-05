package konhaiii.faster_hopper.block;

import konhaiii.faster_hopper.FHBlock;
import konhaiii.faster_hopper.FasterHopper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GoldenHopperBlockEntity extends LootableContainerBlockEntity implements Hopper {
	public static final int TRANSFER_COOLDOWN = FasterHopper.config.goldenHopperCooldownTick;
	public static final int INVENTORY_SIZE = 7;
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
	private int transferCooldown = -1;
	private long lastTickTime;

	public GoldenHopperBlockEntity(BlockPos pos, BlockState state) {
		super(FHBlock.GOLDEN_HOPPER_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		if (!this.deserializeLootTable(nbt)) {
			Inventories.readNbt(nbt, this.inventory);
		}

		this.transferCooldown = nbt.getInt("TransferCooldown");
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if (!this.serializeLootTable(nbt)) {
			Inventories.writeNbt(nbt, this.inventory);
		}

		nbt.putInt("TransferCooldown", this.transferCooldown);
	}

	@Override
	public int size() {
		return this.inventory.size();
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		this.checkLootInteraction(null);
		return Inventories.splitStack(this.getInvStackList(), slot, amount);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.checkLootInteraction(null);
		this.getInvStackList().set(slot, stack);
		if (stack.getCount() > this.getMaxCountPerStack()) {
			stack.setCount(this.getMaxCountPerStack());
		}
	}

	@Override
	protected Text getContainerName() {
		return Text.translatable("container.golden_hopper");
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, GoldenHopperBlockEntity blockEntity) {
		blockEntity.transferCooldown--;
		blockEntity.lastTickTime = world.getTime();
		if (blockEntity.needsCooldown()) {
			blockEntity.setTransferCooldown(0);
			insertAndExtract(world, pos, state, blockEntity, () -> extract(world, blockEntity));
		}
	}

	private static void insertAndExtract(World world, BlockPos pos, BlockState state, GoldenHopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
		if (!world.isClient) {
			if (blockEntity.needsCooldown() && state.get(GoldenHopperBlock.ENABLED)) {
				boolean bl = false;
				if (!blockEntity.isEmpty()) {
					bl = insert(world, pos, state, blockEntity);
				}

				if (!blockEntity.isFull()) {
					bl |= booleanSupplier.getAsBoolean();
				}

				if (bl) {
					blockEntity.setTransferCooldown(TRANSFER_COOLDOWN);
					markDirty(world, pos, state);
				}
			}

		}
	}

	private boolean isFull() {
		for (ItemStack itemStack : this.inventory) {
			if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxCount()) {
				return false;
			}
		}

		return true;
	}

	private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory) {
		Inventory inventory2 = getOutputInventory(world, pos, state);
		if (inventory2 != null) {
			Direction direction = state.get(GoldenHopperBlock.FACING).getOpposite();
			if (!isInventoryFull(inventory2, direction)) {
				for (int i = 0; i < inventory.size(); i++) {
					if (!inventory.getStack(i).isEmpty()) {
						ItemStack itemStack = inventory.getStack(i).copy();
						ItemStack itemStack2 = transfer(inventory, inventory2, inventory.removeStack(i, 1), direction);
						if (itemStack2.isEmpty()) {
							inventory2.markDirty();
							return true;
						}

						inventory.setStack(i, itemStack);
					}
				}

			}
		}
		return false;
	}

	private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
		return inventory instanceof SidedInventory ? IntStream.of(((SidedInventory)inventory).getAvailableSlots(side)) : IntStream.range(0, inventory.size());
	}

	private static boolean isInventoryFull(Inventory inventory, Direction direction) {
		return getAvailableSlots(inventory, direction).allMatch(slot -> {
			ItemStack itemStack = inventory.getStack(slot);
			return itemStack.getCount() >= itemStack.getMaxCount();
		});
	}

	private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
		return getAvailableSlots(inv, facing).allMatch(slot -> inv.getStack(slot).isEmpty());
	}

	public static boolean extract(World world, Hopper hopper) {
		Inventory inventory = getInputInventory(world, hopper);
		if (inventory != null) {
			Direction direction = Direction.DOWN;
			return !isInventoryEmpty(inventory, direction) && getAvailableSlots(inventory, direction).anyMatch(slot -> extract(hopper, inventory, slot, direction));
		} else {
			for (ItemEntity itemEntity : getInputItemEntities(world, hopper)) {
				if (extract(hopper, itemEntity)) {
					return true;
				}
			}

			return false;
		}
	}

	private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
		ItemStack itemStack = inventory.getStack(slot);
		if (!itemStack.isEmpty() && canExtract(hopper, inventory, itemStack, slot, side)) {
			ItemStack itemStack2 = itemStack.copy();
			ItemStack itemStack3 = transfer(inventory, hopper, inventory.removeStack(slot, 1), null);
			if (itemStack3.isEmpty()) {
				inventory.markDirty();
				return true;
			}

			inventory.setStack(slot, itemStack2);
		}

		return false;
	}

	public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
		boolean bl = false;
		ItemStack itemStack = itemEntity.getStack().copy();
		ItemStack itemStack2 = transfer(null, inventory, itemStack, null);
		if (itemStack2.isEmpty()) {
			bl = true;
			itemEntity.discard();
		} else {
			itemEntity.setStack(itemStack2);
		}

		return bl;
	}

	public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
		if (to instanceof SidedInventory sidedInventory && side != null) {
			int[] is = sidedInventory.getAvailableSlots(side);

			for (int i = 0; i < is.length && !stack.isEmpty(); i++) {
				stack = transfer(from, to, stack, is[i], side);
			}
		} else {
			int j = to.size();

			for (int i = 0; i < j && !stack.isEmpty(); i++) {
				stack = transfer(from, to, stack, i, side);
			}
		}

		return stack;
	}

	private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
		return inventory.isValid(slot, stack) && !(inventory instanceof SidedInventory sidedInventory && !sidedInventory.canInsert(slot, stack, side));
	}

	private static boolean canExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing) {
		return fromInventory.canTransferTo(hopperInventory, slot, stack) && !(fromInventory instanceof SidedInventory sidedInventory && !sidedInventory.canExtract(slot, stack, facing));
	}

	private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
		ItemStack itemStack = to.getStack(slot);
		if (canInsert(to, stack, slot, side)) {
			boolean bl = false;
			boolean bl2 = to.isEmpty();
			if (itemStack.isEmpty()) {
				to.setStack(slot, stack);
				stack = ItemStack.EMPTY;
				bl = true;
			} else if (canMergeItems(itemStack, stack)) {
				int i = stack.getMaxCount() - itemStack.getCount();
				int j = Math.min(stack.getCount(), i);
				stack.decrement(j);
				itemStack.increment(j);
				bl = j > 0;
			}

			if (bl) {
				if (bl2 && to instanceof GoldenHopperBlockEntity hopperBlockEntity && !hopperBlockEntity.isDisabled()) {
					int j = 0;
					if (from instanceof GoldenHopperBlockEntity hopperBlockEntity2 && hopperBlockEntity.lastTickTime >= hopperBlockEntity2.lastTickTime) {
						j = 1;
					}

					hopperBlockEntity.setTransferCooldown(TRANSFER_COOLDOWN - j);
				}

				to.markDirty();
			}
		}

		return stack;
	}

	@Nullable
	private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state) {
		Direction direction = state.get(GoldenHopperBlock.FACING);
		return getInventoryAt(world, pos.offset(direction));
	}

	@Nullable
	private static Inventory getInputInventory(World world, Hopper hopper) {
		return getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
	}

	public static List<ItemEntity> getInputItemEntities(World world, Hopper hopper) {
		return hopper.getInputAreaShape()
				.getBoundingBoxes()
				.stream()
				.flatMap(
						box -> world.getEntitiesByClass(
										ItemEntity.class, box.offset(hopper.getHopperX() - 0.5, hopper.getHopperY() - 0.5, hopper.getHopperZ() - 0.5), EntityPredicates.VALID_ENTITY
								)
								.stream()
				)
				.collect(Collectors.toList());
	}

	@Nullable
	public static Inventory getInventoryAt(World world, BlockPos pos) {
		return getInventoryAt(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}

	@Nullable
	private static Inventory getInventoryAt(World world, double x, double y, double z) {
		Inventory inventory = null;
		BlockPos blockPos = BlockPos.ofFloored(x, y, z);
		BlockState blockState = world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof InventoryProvider) {
			inventory = ((InventoryProvider)block).getInventory(blockState, world, blockPos);
		} else if (blockState.hasBlockEntity()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity instanceof Inventory) {
				inventory = (Inventory)blockEntity;
				if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
					inventory = ChestBlock.getInventory((ChestBlock)block, blockState, world, blockPos, true);
				}
			}
		}

		if (inventory == null) {
			List<Entity> list = world.getOtherEntities(null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES);
			if (!list.isEmpty()) {
				inventory = (Inventory)list.get(world.random.nextInt(list.size()));
			}
		}

		return inventory;
	}

	private static boolean canMergeItems(ItemStack first, ItemStack second) {
		return first.getCount() <= first.getMaxCount() && ItemStack.canCombine(first, second);
	}

	@Override
	public double getHopperX() {
		return this.pos.getX() + 0.5;
	}

	@Override
	public double getHopperY() {
		return this.pos.getY() + 0.5;
	}

	@Override
	public double getHopperZ() {
		return this.pos.getZ() + 0.5;
	}

	private void setTransferCooldown(int transferCooldown) {
		this.transferCooldown = transferCooldown;
	}

	private boolean needsCooldown() {
		return this.transferCooldown <= 0;
	}

	private boolean isDisabled() {
		return this.transferCooldown > TRANSFER_COOLDOWN;
	}

	@Override
	protected DefaultedList<ItemStack> getInvStackList() {
		return this.inventory;
	}

	@Override
	protected void setInvStackList(DefaultedList<ItemStack> list) {
		this.inventory = list;
	}

	public static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, GoldenHopperBlockEntity blockEntity) {
		if (entity instanceof ItemEntity
				&& VoxelShapes.matchesAnywhere(
				VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), blockEntity.getInputAreaShape(), BooleanBiFunction.AND
		)) {
			insertAndExtract(world, pos, state, blockEntity, () -> extract(blockEntity, (ItemEntity)entity));
		}
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new GoldenHopperScreenHandler(syncId, playerInventory, this);
	}
}
