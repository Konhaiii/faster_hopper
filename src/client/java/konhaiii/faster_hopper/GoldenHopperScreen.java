package konhaiii.faster_hopper;

import konhaiii.faster_hopper.screen.GoldenHopperMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GoldenHopperScreen extends AbstractContainerScreen<GoldenHopperMenu> {
	private static final Identifier HOPPER_LOCATION = Identifier.fromNamespaceAndPath("faster_hopper", "textures/gui/container/golden_hopper.png");

	public GoldenHopperScreen(GoldenHopperMenu goldenHopperMenu, Inventory inventory, Component component) {
		super(goldenHopperMenu, inventory, component);
		this.imageHeight = 133;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, HOPPER_LOCATION, k, l, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
	}
}