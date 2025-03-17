package konhaiii.faster_hopper.item;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.block.ModBlocks;
import konhaiii.faster_hopper.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {

	public static final RegistryKey<ItemGroup> MOD_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(FasterHopper.MOD_ID, "item_group"));
	public static final ItemGroup MOD_ITEM_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModBlocks.GOLDEN_HOPPER))
			.displayName(Text.translatable("itemGroup.faster_hopper"))
			.build();
	public static final Item GOLDEN_HOPPER_MINECART = register("golden_hopper_minecart", settings -> new MinecartItem(ModEntities.GOLDEN_HOPPER_MINECART, settings), new Item.Settings());
	public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
		// Create the item key.
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(FasterHopper.MOD_ID, name));

		// Create the item instance.
		Item item = itemFactory.apply(settings.registryKey(itemKey));

		// Register the item.
		Registry.register(Registries.ITEM, itemKey, item);

		return item;
	}
	public static void initialize() {
		Registry.register(Registries.ITEM_GROUP, MOD_ITEM_GROUP_KEY, MOD_ITEM_GROUP);
		ItemGroupEvents.modifyEntriesEvent(MOD_ITEM_GROUP_KEY)
				.register((itemGroup) -> itemGroup.add(ModItems.GOLDEN_HOPPER_MINECART));
	}
}
