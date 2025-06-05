package konhaiii.faster_hopper;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class FasterHopperClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(FHScreen.GOLDEN_HOPPER, GoldenHopperScreen::new);
	}
}