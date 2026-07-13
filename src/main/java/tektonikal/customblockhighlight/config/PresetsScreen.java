package tektonikal.customblockhighlight.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PresetsScreen extends Screen {
	private final boolean firstTime;
	protected PresetsScreen(Component title, boolean firstTime) {
		super(title);
		this.firstTime = firstTime;
	}
	@Override
	protected void init() {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, -1);
	}
}
