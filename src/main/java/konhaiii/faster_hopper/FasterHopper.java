package konhaiii.faster_hopper;

import konhaiii.faster_hopper.block.ModBlocks;
import konhaiii.faster_hopper.config.ModConfigs;
import konhaiii.faster_hopper.entity.ModEntities;
import konhaiii.faster_hopper.item.ModItems;
import konhaiii.faster_hopper.screen.GoldenHopperMenu;
import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FasterHopper implements ModInitializer {
	public static final String MOD_ID = "faster_hopper";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfigs config;
	public static final MenuType<GoldenHopperMenu> GOLDEN_HOPPER_MENU = Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("faster_hopper", "golden_hopper_menu"), new MenuType<>(GoldenHopperMenu::new, FeatureFlags.VANILLA_SET));

	@Override
	public void onInitialize() {
		LOGGER.info("FasterHopper: Initialize");
		config = ModConfigs.loadConfig();
		ModEntities.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
	}
}