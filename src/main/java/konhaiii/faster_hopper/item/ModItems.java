package konhaiii.faster_hopper.item;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;

import java.util.function.Function;

public class ModItems {
	public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
		// Create the item key.
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FasterHopper.MOD_ID, name));

		// Create the item instance.
		T item = itemFactory.apply(settings.setId(itemKey));

		// Register the item.
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		return item;
	}

	public static final Item GOLDEN_HOPPER_MINECART = register("golden_hopper_minecart", (properties) -> new MinecartItem(ModEntities.MINECART_GOLDEN_HOPPER_ENTITY_TYPE, properties), (new Item.Properties()).stacksTo(1));

	public static void initialize() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, FASTER_HOPPER_CREATIVE_TAB_KEY, FASTER_HOPPER_CREATIVE_TAB);
	}

	public static final ResourceKey<CreativeModeTab> FASTER_HOPPER_CREATIVE_TAB_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(FasterHopper.MOD_ID, "creative_tab"));
	public static final CreativeModeTab FASTER_HOPPER_CREATIVE_TAB = FabricItemGroup.builder()
			.icon(() -> new ItemStack(GOLDEN_HOPPER_MINECART))
			.title(Component.translatable("itemGroup.faster_hopper"))
			.displayItems((params, output) -> {
				output.accept(GOLDEN_HOPPER_MINECART);
			})
			.build();
}
