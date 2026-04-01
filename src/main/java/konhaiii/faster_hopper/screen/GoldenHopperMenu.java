package konhaiii.faster_hopper.screen;

import konhaiii.faster_hopper.FasterHopper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class GoldenHopperMenu extends AbstractContainerMenu {
	private final Container golden_hopper;
	public static final int SLOT_COUNT = 7;

	public GoldenHopperMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(SLOT_COUNT));
	}

	public GoldenHopperMenu(int i, Inventory inventory, Container container) {
		super(FasterHopper.GOLDEN_HOPPER_MENU, i);
		this.golden_hopper = container;
		checkContainerSize(container, SLOT_COUNT);
		container.startOpen(inventory.player);

		for (int j = 0; j < SLOT_COUNT; j++) {
			this.addSlot(new Slot(container, j, 26 + j * 18, 20));
		}

		this.addStandardInventorySlots(inventory, 8, 51);
	}

	@Override
	public boolean stillValid(@NonNull Player player) {
		return this.golden_hopper.stillValid(player);
	}

	@Override
	public @NonNull ItemStack quickMoveStack(@NonNull Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i < this.golden_hopper.getContainerSize()) {
				if (!this.moveItemStackTo(itemStack2, this.golden_hopper.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, this.golden_hopper.getContainerSize(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemStack;
	}

	@Override
	public void removed(@NonNull Player player) {
		super.removed(player);
		this.golden_hopper.stopOpen(player);
	}
}
