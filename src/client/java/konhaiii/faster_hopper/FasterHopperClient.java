package konhaiii.faster_hopper;

import konhaiii.faster_hopper.entity.ModEntities;
import konhaiii.faster_hopper.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;

import static net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register;

public class FasterHopperClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(ModScreens.GOLDEN_HOPPER, GoldenHopperScreen::new);
		register(ModEntities.GOLDEN_HOPPER_MINECART, context -> new MinecartEntityRenderer(context, EntityModelLayers.HOPPER_MINECART));
	}
}