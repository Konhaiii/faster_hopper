package konhaiii.faster_hopper.screen;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.block.golden_hopper.GoldenHopperScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreens {
	public static final ScreenHandlerType<GoldenHopperScreenHandler> GOLDEN_HOPPER = Registry.register(Registries.SCREEN_HANDLER, FasterHopper.MOD_ID.concat(":").concat("golden_hopper"), new ScreenHandlerType<>(GoldenHopperScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

	public static void initialize() {
	}
}