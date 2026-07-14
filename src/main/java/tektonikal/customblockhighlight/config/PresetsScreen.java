package tektonikal.customblockhighlight.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import tektonikal.customblockhighlight.Blockhighlight;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PresetsScreen extends Screen {
	private final boolean firstTime;
	private final Screen parent;

	protected PresetsScreen(Component title, boolean firstTime, Screen parent) {
		super(title);
		this.firstTime = firstTime;
		this.parent = parent;
	}

	@Override
	protected void init() {
		addRenderableWidget(new Button(width / 4, height / 2, width / 2, 16, Component.literal("Give it to me plain! I'll take it from there."), button -> {
			try {
				Path path = FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json");
				Files.delete(path);
				Files.createFile(path);
				Path p = FabricLoader.getInstance().getModContainer("custom-block-highlight").get().getRootPaths().getFirst().resolve("assets/presets/vanilla.json");
				Files.writeString(path, Files.readString(p), StandardCharsets.UTF_8);
				BlockHighlightConfig.INSTANCE.load();
				Blockhighlight.unleashHell();
			} catch (Exception _) {
			}
		}, _ -> Component.empty()) {

			@Override
			protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
				extractDefaultSprite(graphics);
				extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
			}
		});
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreenAndShow(parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, -1);
	}
}
