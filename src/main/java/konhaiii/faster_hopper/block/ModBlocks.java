package konhaiii.faster_hopper.block;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.block.golden_hopper.GoldenHopperBlock;
import konhaiii.faster_hopper.block.golden_hopper.GoldenHopperBlockEntity;
import konhaiii.faster_hopper.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public class ModBlocks {
	public static final Block GOLDEN_HOPPER = register(
			GoldenHopperBlock::new,
			BlockBehaviour.Properties.of().sound(SoundType.METAL).noOcclusion().mapColor(MapColor.GOLD).requiresCorrectToolForDrops().strength(3.0F, 4.8F)
	);

	private static Block register(Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings) {
		ResourceKey<Block> blockKey = keyOfBlock();
		Block block = blockFactory.apply(settings.setId(blockKey));
		ResourceKey<Item> itemKey = keyOfItem();
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	private static ResourceKey<Block> keyOfBlock() {
		return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(FasterHopper.MOD_ID, "golden_hopper"));
	}

	private static ResourceKey<Item> keyOfItem() {
		return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FasterHopper.MOD_ID, "golden_hopper"));
	}

	public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(ModItems.FASTER_HOPPER_CREATIVE_TAB_KEY).register((itemGroup) -> itemGroup.accept(ModBlocks.GOLDEN_HOPPER.asItem()));
	}

	public static final BlockEntityType<GoldenHopperBlockEntity> GOLDEN_HOPPER_BLOCK_ENTITY =
			register(GoldenHopperBlockEntity::new);

	private static <T extends BlockEntity> BlockEntityType<T> register(
			FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory
	) {
		Identifier id = Identifier.fromNamespaceAndPath(FasterHopper.MOD_ID, "golden_hopper_block_entity");
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, new Block[]{ModBlocks.GOLDEN_HOPPER}).build());
	}
}
