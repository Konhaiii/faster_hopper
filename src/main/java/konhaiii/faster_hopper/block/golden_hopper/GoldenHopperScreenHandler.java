package konhaiii.faster_hopper.block.golden_hopper;

import konhaiii.faster_hopper.screen.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class GoldenHopperScreenHandler extends ScreenHandler {
	public static final int SLOT_COUNT = 7;
	private final Inventory inventory;

	public GoldenHopperScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(SLOT_COUNT));
	}

	public GoldenHopperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(ModScreens.GOLDEN_HOPPER, syncId);
		this.inventory = inventory;
		checkSize(inventory, SLOT_COUNT);
		inventory.onOpen(playerInventory.player);

		for (int i = 0; i < SLOT_COUNT; i++) {
			this.addSlot(new Slot(inventory, i, 26 + i * 18, 20));
		}

		this.addPlayerSlots(playerInventory, 8, 51);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = this.slots.get(slot);
		if (slot2.hasStack()) {
			ItemStack itemStack2 = slot2.getStack();
			itemStack = itemStack2.copy();
			if (slot < this.inventory.size()) {
				if (!this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(itemStack2, 0, this.inventory.size(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot2.setStack(ItemStack.EMPTY);
			} else {
				slot2.markDirty();
			}
		}

		return itemStack;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		this.inventory.onClose(player);
	}
}
