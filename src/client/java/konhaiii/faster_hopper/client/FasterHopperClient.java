package konhaiii.faster_hopper.client;

import konhaiii.faster_hopper.FasterHopper;
import konhaiii.faster_hopper.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class FasterHopperClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(FasterHopper.GOLDEN_HOPPER_MENU, GoldenHopperScreen::new);
		EntityRenderers.register(ModEntities.MINECART_GOLDEN_HOPPER_ENTITY_TYPE, context -> new MinecartRenderer(context, ModelLayers.HOPPER_MINECART));
	}
}
