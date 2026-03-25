package konhaiii.faster_hopper;

import konhaiii.faster_hopper.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;

import static net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register;

public class FasterHopperClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(FasterHopper.GOLDEN_HOPPER_MENU, GoldenHopperScreen::new);
		register(ModEntities.MINECART_GOLDEN_HOPPER_ENTITY_TYPE, context -> new MinecartRenderer(context, ModelLayers.HOPPER_MINECART));
	}
}
