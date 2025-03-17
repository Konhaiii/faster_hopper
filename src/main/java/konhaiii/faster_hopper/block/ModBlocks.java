package konhaiii.faster_hopper.block;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.block.golden_hopper.GoldenHopperBlock;
import konhaiii.faster_hopper.block.golden_hopper.GoldenHopperBlockEntity;
import konhaiii.faster_hopper.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {

	public static final Block GOLDEN_HOPPER = register(
			"golden_hopper",
			GoldenHopperBlock::new,
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).requiresTool().strength(3.0F, 4.8F).sounds(BlockSoundGroup.METAL).nonOpaque(),
			true
	);

	private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
		// Create a registry key for the block
		RegistryKey<Block> blockKey = keyOfBlock(name);
		// Create the block instance
		Block block = blockFactory.apply(settings.registryKey(blockKey));

		// Sometimes, you may not want to register an item for the block.
		// Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
		if (shouldRegisterItem) {
			// Items need to be registered with a different type of registry key, but the ID
			// can be the same.
			RegistryKey<Item> itemKey = keyOfItem(name);

			BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
			Registry.register(Registries.ITEM, itemKey, blockItem);
		}

		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	private static RegistryKey<Block> keyOfBlock(String name) {
		return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(FasterHopper.MOD_ID, name));
	}

	private static RegistryKey<Item> keyOfItem(String name) {
		return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(FasterHopper.MOD_ID, name));
	}
	public static final BlockEntityType<GoldenHopperBlockEntity> GOLDEN_HOPPER_BLOCK_ENTITY =
			register("counter", GoldenHopperBlockEntity::new, ModBlocks.GOLDEN_HOPPER);

	private static <T extends BlockEntity> BlockEntityType<T> register(
			String name,
			FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
			Block... blocks
	) {
		Identifier id = Identifier.of(FasterHopper.MOD_ID, name);
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
	}

	public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(ModItems.MOD_ITEM_GROUP_KEY).register((itemGroup) -> {
			itemGroup.add(ModBlocks.GOLDEN_HOPPER.asItem());
		});
	}

}