package konhaiii.faster_hopper;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FasterHopper implements ModInitializer {
	public static final String MOD_ID = "faster_hopper";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static FHConfig config;

	@Override
	public void onInitialize() {
		config = FHConfig.loadConfig();
		FHScreen.initialize();
		FHBlock.initialize();
		LOGGER.info("Initialization completed.");
	}
}