package konhaiii.faster_hopper.entity.golden_hopper_minecart;

import konhaiii.faster_hopper.block.ModBlocks;
import konhaiii.faster_hopper.block.golden_hopper.GoldenHopperBlockEntity;
import konhaiii.faster_hopper.block.golden_hopper.GoldenHopperScreenHandler;
import konhaiii.faster_hopper.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GoldenHopperMinecartEntity extends StorageMinecartEntity implements Hopper {
	private boolean enabled = true;
	private boolean hopperTicked = false;

	public GoldenHopperMinecartEntity(EntityType<? extends GoldenHopperMinecartEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public BlockState getDefaultContainedBlock() {
		return ModBlocks.GOLDEN_HOPPER.getDefaultState();
	}

	@Override
	public int getDefaultBlockOffset() {
		return 1;
	}

	@Override
	public int size() {
		return 7;
	}

	@Override
	public void onActivatorRail(int x, int y, int z, boolean powered) {
		boolean bl = !powered;
		if (bl != this.isEnabled()) {
			this.setEnabled(bl);
		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public double getHopperX() {
		return this.getX();
	}

	@Override
	public double getHopperY() {
		return this.getY() + 0.5;
	}

	@Override
	public double getHopperZ() {
		return this.getZ();
	}

	@Override
	public boolean canBlockFromAbove() {
		return false;
	}

	@Override
	public void tick() {
		this.hopperTicked = false;
		super.tick();
		this.tickHopper();
	}

	@Override
	protected double moveAlongTrack(BlockPos pos, RailShape shape, double remainingMovement) {
		double d = super.moveAlongTrack(pos, shape, remainingMovement);
		this.tickHopper();
		return d;
	}

	private void tickHopper() {
		if (!this.getWorld().isClient && this.isAlive() && this.isEnabled() && !this.hopperTicked && this.canOperate()) {
			this.hopperTicked = true;
			this.markDirty();
		}
	}

	public boolean canOperate() {
		if (GoldenHopperBlockEntity.extract(this.getWorld(), this)) {
			return true;
		} else {
			for (ItemEntity itemEntity : this.getWorld()
					.getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.25, 0.0, 0.25), EntityPredicates.VALID_ENTITY)) {
				if (GoldenHopperBlockEntity.extract(this, itemEntity)) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	protected Item asItem() {
		return ModItems.GOLDEN_HOPPER_MINECART;
	}

	@Override
	public ItemStack getPickBlockStack() {
		return new ItemStack(ModItems.GOLDEN_HOPPER_MINECART);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("Enabled", this.enabled);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.enabled = !nbt.contains("Enabled") || nbt.getBoolean("Enabled");
	}

	@Override
	public ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new GoldenHopperScreenHandler(syncId, playerInventory, this);
	}
}
