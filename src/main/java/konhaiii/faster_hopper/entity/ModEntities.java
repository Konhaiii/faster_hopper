package konhaiii.faster_hopper.entity;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.entity.golden_hopper_minecart.GoldenHopperMinecartEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {
	public static final EntityType<GoldenHopperMinecartEntity> GOLDEN_HOPPER_MINECART = register(
			EntityType.Builder.create(GoldenHopperMinecartEntity::new, SpawnGroup.MISC)
					.dropsNothing()
					.dimensions(0.98F, 0.7F)
					.passengerAttachments(0.1875F)
					.maxTrackingRange(8)
	);

	private static <T extends Entity> EntityType<T> register(EntityType.Builder<T> type) {
		return Registry.register(Registries.ENTITY_TYPE, keyOf(), type.build(keyOf()));
	}

	private static RegistryKey<EntityType<?>> keyOf() {
		return RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(FasterHopper.MOD_ID, "golden_hopper_minecart"));
	}

	public static void initialize() {}
}
