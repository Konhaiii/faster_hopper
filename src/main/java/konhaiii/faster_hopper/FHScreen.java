package konhaiii.faster_hopper;

import konhaiii.faster_hopper.block.GoldenHopperScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class FHScreen {
	public static final ScreenHandlerType<GoldenHopperScreenHandler> GOLDEN_HOPPER = Registry.register(Registries.SCREEN_HANDLER, FasterHopper.MOD_ID.concat(":").concat("golden_hopper"), new ScreenHandlerType<>(GoldenHopperScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

	public static void initialize() {
	}
}
