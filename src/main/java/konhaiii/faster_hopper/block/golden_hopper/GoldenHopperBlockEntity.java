package konhaiii.faster_hopper.block.golden_hopper;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.block.ModBlocks;
import konhaiii.faster_hopper.screen.GoldenHopperMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import java.util.List;
import java.util.function.BooleanSupplier;

public class GoldenHopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {
	public static final int MOVE_ITEM_SPEED = FasterHopper.config.goldenHopperCooldownTick;
	public static final int HOPPER_CONTAINER_SIZE = 7;
	private static final int[][] CACHED_SLOTS = new int[54][];
	private static final int NO_COOLDOWN_TIME = -1;
	private static final Component DEFAULT_NAME = Component.translatable("container.golden_hopper");
	private NonNullList<ItemStack> items = NonNullList.withSize(HOPPER_CONTAINER_SIZE, ItemStack.EMPTY);
	private int cooldownTime = -1;
	private long tickedGameTime;
	private Direction facing;

	public GoldenHopperBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModBlocks.GOLDEN_HOPPER_BLOCK_ENTITY, blockPos, blockState);
		this.facing = blockState.getValue(GoldenHopperBlock.FACING);
	}

	@Override
	protected void loadAdditional(ValueInput valueInput) {
		super.loadAdditional(valueInput);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(valueInput)) {
			ContainerHelper.loadAllItems(valueInput, this.items);
		}

		this.cooldownTime = valueInput.getIntOr("TransferCooldown", -1);
	}

	@Override
	protected void saveAdditional(ValueOutput valueOutput) {
		super.saveAdditional(valueOutput);
		if (!this.trySaveLootTable(valueOutput)) {
			ContainerHelper.saveAllItems(valueOutput, this.items);
		}

		valueOutput.putInt("TransferCooldown", this.cooldownTime);
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		this.unpackLootTable(null);
		return ContainerHelper.removeItem(this.getItems(), i, j);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.unpackLootTable(null);
		this.getItems().set(i, itemStack);
		itemStack.limitSize(this.getMaxStackSize(itemStack));
	}

	@Override
	public void setBlockState(BlockState blockState) {
		super.setBlockState(blockState);
		this.facing = blockState.getValue(GoldenHopperBlock.FACING);
	}

	@Override
	protected Component getDefaultName() {
		return DEFAULT_NAME;
	}

	public static void pushItemsTick(Level level, BlockPos blockPos, BlockState blockState, GoldenHopperBlockEntity goldenHopperBlockEntity) {
		goldenHopperBlockEntity.cooldownTime--;
		goldenHopperBlockEntity.tickedGameTime = level.getGameTime();
		if (!goldenHopperBlockEntity.isOnCooldown()) {
			goldenHopperBlockEntity.setCooldown(0);
			tryMoveItems(level, blockPos, blockState, goldenHopperBlockEntity, () -> suckInItems(level, goldenHopperBlockEntity));
		}
	}

	private static boolean tryMoveItems(
			Level level, BlockPos blockPos, BlockState blockState, GoldenHopperBlockEntity goldenHopperBlockEntity, BooleanSupplier booleanSupplier
	) {
		if (level.isClientSide()) {
			return false;
		} else {
			if (!goldenHopperBlockEntity.isOnCooldown() && (Boolean)blockState.getValue(GoldenHopperBlock.ENABLED)) {
				boolean bl = false;
				if (!goldenHopperBlockEntity.isEmpty()) {
					bl = ejectItems(level, blockPos, goldenHopperBlockEntity);
				}

				if (!goldenHopperBlockEntity.inventoryFull()) {
					bl |= booleanSupplier.getAsBoolean();
				}

				if (bl) {
					goldenHopperBlockEntity.setCooldown(MOVE_ITEM_SPEED);
					setChanged(level, blockPos, blockState);
					return true;
				}
			}

			return false;
		}
	}

	private boolean inventoryFull() {
		for (ItemStack itemStack : this.items) {
			if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxStackSize()) {
				return false;
			}
		}

		return true;
	}

	private static boolean ejectItems(Level level, BlockPos blockPos, GoldenHopperBlockEntity hopper) {

		BlockPos targetPos = blockPos.relative(hopper.facing);

		Container container = getContainerAt(level, targetPos);

		if (container != null) {
			Direction direction = hopper.facing.getOpposite();

			if (isFullContainer(container, direction)) {
				return false;
			}

			for (int i = 0; i < hopper.getContainerSize(); i++) {

				ItemStack stack = hopper.getItem(i);

				if (!stack.isEmpty()) {

					int count = stack.getCount();

					ItemStack result = addItem(
							hopper,
							container,
							hopper.removeItem(i, 1),
							direction
					);

					if (result.isEmpty()) {
						container.setChanged();
						return true;
					}

					stack.setCount(count);

					if (count == 1) {
						hopper.setItem(i, stack);
					}
				}
			}

			return false;
		}

		// Fabric Transfer API fallback
		Storage<ItemVariant> storage =
				ItemStorage.SIDED.find(level, targetPos, hopper.facing.getOpposite());

		if (storage == null) return false;

		for (int i = 0; i < hopper.getContainerSize(); i++) {

			ItemStack stack = hopper.getItem(i);

			if (stack.isEmpty()) continue;

			try (Transaction transaction = Transaction.openOuter()) {

				ItemVariant variant = ItemVariant.of(stack);

				long inserted = storage.insert(variant, 1, transaction);

				if (inserted > 0) {

					stack.shrink(1);

					if (stack.isEmpty()) {
						hopper.setItem(i, ItemStack.EMPTY);
					}

					transaction.commit();
					return true;
				}
			}
		}

		return false;
	}

	private static int[] getSlots(Container container, Direction direction) {
		if (container instanceof WorldlyContainer worldlyContainer) {
			return worldlyContainer.getSlotsForFace(direction);
		} else {
			int i = container.getContainerSize();
			if (i < CACHED_SLOTS.length) {
				int[] is = CACHED_SLOTS[i];
				if (is != null) {
					return is;
				} else {
					int[] js = createFlatSlots(i);
					CACHED_SLOTS[i] = js;
					return js;
				}
			} else {
				return createFlatSlots(i);
			}
		}
	}

	private static int[] createFlatSlots(int i) {
		int[] is = new int[i];
		int j = 0;

		while (j < is.length) {
			is[j] = j++;
		}

		return is;
	}

	private static boolean isFullContainer(Container container, Direction direction) {
		int[] is = getSlots(container, direction);

		for (int i : is) {
			ItemStack itemStack = container.getItem(i);
			if (itemStack.getCount() < itemStack.getMaxStackSize()) {
				return false;
			}
		}

		return true;
	}

	public static boolean suckInItems(Level level, Hopper hopper) {

		BlockPos posAbove =
				BlockPos.containing(
						hopper.getLevelX(),
						hopper.getLevelY() + 1,
						hopper.getLevelZ()
				);

		Container container = getContainerAt(level, posAbove);

		if (container != null) {

			Direction direction = Direction.DOWN;

			for (int slot : getSlots(container, direction)) {

				if (tryTakeInItemFromSlot(hopper, container, slot, direction)) {
					return true;
				}
			}

			return false;
		}

		// Fabric Transfer API fallback
		Storage<ItemVariant> storage =
				ItemStorage.SIDED.find(level, posAbove, Direction.DOWN);

		if (storage != null) {

			try (Transaction transaction = Transaction.openOuter()) {

				for (StorageView<ItemVariant> view : storage) {

					if (view.isResourceBlank()) continue;

					ItemVariant resource = view.getResource();

					long extracted = view.extract(resource, 1, transaction);

					if (extracted > 0) {

						ItemStack stack = resource.toStack(1);

						if (addItem(null, (Container) hopper, stack, null).isEmpty()) {

							transaction.commit();
							return true;
						}
					}
				}
			}
		}

		for (ItemEntity itemEntity : getItemsAtAndAbove(level, hopper)) {

			if (addItem(hopper, itemEntity)) {
				return true;
			}
		}

		return false;
	}

	private static boolean tryTakeInItemFromSlot(Hopper hopper, Container container, int i, Direction direction) {
		ItemStack itemStack = container.getItem(i);
		if (!itemStack.isEmpty() && canTakeItemFromContainer(hopper, container, itemStack, i, direction)) {
			int j = itemStack.getCount();
			ItemStack itemStack2 = addItem(container, hopper, container.removeItem(i, 1), null);
			if (itemStack2.isEmpty()) {
				container.setChanged();
				return true;
			}

			itemStack.setCount(j);
			if (j == 1) {
				container.setItem(i, itemStack);
			}
		}

		return false;
	}

	public static boolean addItem(Container container, ItemEntity itemEntity) {
		boolean bl = false;
		ItemStack itemStack = itemEntity.getItem().copy();
		ItemStack itemStack2 = addItem(null, container, itemStack, null);
		if (itemStack2.isEmpty()) {
			bl = true;
			itemEntity.setItem(ItemStack.EMPTY);
			itemEntity.discard();
		} else {
			itemEntity.setItem(itemStack2);
		}

		return bl;
	}

	public static ItemStack addItem(@Nullable Container container, Container container2, ItemStack itemStack, @Nullable Direction direction) {
		if (container2 instanceof WorldlyContainer worldlyContainer && direction != null) {
			int[] is = worldlyContainer.getSlotsForFace(direction);

			for (int i = 0; i < is.length && !itemStack.isEmpty(); i++) {
				itemStack = tryMoveInItem(container, container2, itemStack, is[i], direction);
			}
		} else {
			int j = container2.getContainerSize();

			for (int i = 0; i < j && !itemStack.isEmpty(); i++) {
				itemStack = tryMoveInItem(container, container2, itemStack, i, direction);
			}
		}

		return itemStack;
	}

	private static boolean canPlaceItemInContainer(Container container, ItemStack itemStack, int i, @Nullable Direction direction) {
		return !container.canPlaceItem(i, itemStack)
				? false
				: !(container instanceof WorldlyContainer worldlyContainer && !worldlyContainer.canPlaceItemThroughFace(i, itemStack, direction));
	}

	private static boolean canTakeItemFromContainer(Container container, Container container2, ItemStack itemStack, int i, Direction direction) {
		return !container2.canTakeItem(container, i, itemStack)
				? false
				: !(container2 instanceof WorldlyContainer worldlyContainer && !worldlyContainer.canTakeItemThroughFace(i, itemStack, direction));
	}

	private static ItemStack tryMoveInItem(@Nullable Container container, Container container2, ItemStack itemStack, int i, @Nullable Direction direction) {
		ItemStack itemStack2 = container2.getItem(i);
		if (canPlaceItemInContainer(container2, itemStack, i, direction)) {
			boolean bl = false;
			boolean bl2 = container2.isEmpty();
			if (itemStack2.isEmpty()) {
				container2.setItem(i, itemStack);
				itemStack = ItemStack.EMPTY;
				bl = true;
			} else if (canMergeItems(itemStack2, itemStack)) {
				int j = itemStack.getMaxStackSize() - itemStack2.getCount();
				int k = Math.min(itemStack.getCount(), j);
				itemStack.shrink(k);
				itemStack2.grow(k);
				bl = k > 0;
			}

			if (bl) {
				if (bl2 && container2 instanceof GoldenHopperBlockEntity goldenHopperBlockEntity && !goldenHopperBlockEntity.isOnCustomCooldown()) {
					int k = 0;
					if (container instanceof GoldenHopperBlockEntity goldenHopperBlockEntity2 && goldenHopperBlockEntity.tickedGameTime >= goldenHopperBlockEntity2.tickedGameTime) {
						k = 1;
					}

					goldenHopperBlockEntity.setCooldown(MOVE_ITEM_SPEED - k);
				}

				container2.setChanged();
			}
		}

		return itemStack;
	}

	@Nullable
	private static Container getAttachedContainer(Level level, BlockPos blockPos, GoldenHopperBlockEntity goldenHopperBlockEntity) {
		return getContainerAt(level, blockPos.relative(goldenHopperBlockEntity.facing));
	}

	@Nullable
	private static Container getSourceContainer(Level level, Hopper hopper, BlockPos blockPos, BlockState blockState) {
		return getContainerAt(level, blockPos, blockState, hopper.getLevelX(), hopper.getLevelY() + 1.0, hopper.getLevelZ());
	}

	public static List<ItemEntity> getItemsAtAndAbove(Level level, Hopper hopper) {
		AABB aABB = hopper.getSuckAabb().move(hopper.getLevelX() - 0.5, hopper.getLevelY() - 0.5, hopper.getLevelZ() - 0.5);
		return level.getEntitiesOfClass(ItemEntity.class, aABB, EntitySelector.ENTITY_STILL_ALIVE);
	}

	@Nullable
	public static Container getContainerAt(Level level, BlockPos blockPos) {
		return getContainerAt(level, blockPos, level.getBlockState(blockPos), blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
	}

	@Nullable
	private static Container getContainerAt(Level level, BlockPos blockPos, BlockState blockState, double d, double e, double f) {
		Container container = getBlockContainer(level, blockPos, blockState);
		if (container == null) {
			container = getEntityContainer(level, d, e, f);
		}

		return container;
	}

	@Nullable
	private static Container getBlockContainer(Level level, BlockPos blockPos, BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof WorldlyContainerHolder) {
			return ((WorldlyContainerHolder)block).getContainer(blockState, level, blockPos);
		} else if (blockState.hasBlockEntity() && level.getBlockEntity(blockPos) instanceof Container container) {
			if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
				container = ChestBlock.getContainer((ChestBlock)block, blockState, level, blockPos, true);
			}

			return container;
		} else {
			return null;
		}
	}

	@Nullable
	private static Container getEntityContainer(Level level, double d, double e, double f) {
		List<Entity> list = level.getEntities((Entity)null, new AABB(d - 0.5, e - 0.5, f - 0.5, d + 0.5, e + 0.5, f + 0.5), EntitySelector.CONTAINER_ENTITY_SELECTOR);
		return !list.isEmpty() ? (Container)list.get(level.random.nextInt(list.size())) : null;
	}

	private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack.getCount() <= itemStack.getMaxStackSize() && ItemStack.isSameItemSameComponents(itemStack, itemStack2);
	}

	@Override
	public double getLevelX() {
		return this.worldPosition.getX() + 0.5;
	}

	@Override
	public double getLevelY() {
		return this.worldPosition.getY() + 0.5;
	}

	@Override
	public double getLevelZ() {
		return this.worldPosition.getZ() + 0.5;
	}

	@Override
	public boolean isGridAligned() {
		return true;
	}

	private void setCooldown(int i) {
		this.cooldownTime = i;
	}

	private boolean isOnCooldown() {
		return this.cooldownTime > 0;
	}

	private boolean isOnCustomCooldown() {
		return this.cooldownTime > MOVE_ITEM_SPEED;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> nonNullList) {
		this.items = nonNullList;
	}

	public static void entityInside(Level level, BlockPos blockPos, BlockState blockState, Entity entity, GoldenHopperBlockEntity goldenHopperBlockEntity) {
		if (entity instanceof ItemEntity itemEntity
				&& !itemEntity.getItem().isEmpty()
				&& entity.getBoundingBox().move(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ()).intersects(goldenHopperBlockEntity.getSuckAabb())) {
			tryMoveItems(level, blockPos, blockState, goldenHopperBlockEntity, () -> addItem(goldenHopperBlockEntity, itemEntity));
		}
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new GoldenHopperMenu(i, inventory, this);
	}
}
