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

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfigs config;
	public static final MenuType<GoldenHopperMenu> GOLDEN_HOPPER_MENU = Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("faster_hopper", "golden_hopper_menu"), new MenuType<>(GoldenHopperMenu::new, FeatureFlags.VANILLA_SET));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("FasterHopper: Initialize");
		config = ModConfigs.loadConfig();
		ModEntities.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
	}
}