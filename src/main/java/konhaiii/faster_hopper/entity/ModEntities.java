package konhaiii.faster_hopper.entity;

import konhaiii.faster_hopper.entity.golden_hopper_minecart.MinecartGoldenHopper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

	public static final EntityType<MinecartGoldenHopper> MINECART_GOLDEN_HOPPER_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Identifier.fromNamespaceAndPath("faster_hopper", "minecart_golden_hopper_entity_type"),
			EntityType.Builder.of(MinecartGoldenHopper::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("faster_hopper", "minecart_golden_hopper")))
	);

	public static void initialize() {}
}
