package tektonikal.customblockhighlight.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PresetsScreen extends Screen {
	private final boolean firstTime;
	private final Screen parent;

	protected PresetsScreen(Component title, boolean firstTime, Screen parent) {
		super(title);
		this.firstTime = firstTime;
		this.parent = parent;
	}

	public static void loadPreset(String name) {
		try {
			Path path = FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json");
			Files.delete(path);
			Files.createFile(path);
			try (var preset = PresetsScreen.class.getResourceAsStream("/assets/presets/" + name + ".json")) {
				if (preset == null) return;
				Files.write(path, preset.readAllBytes());
			}
			BlockHighlightConfig.INSTANCE.load();
		} catch (IOException _) {
		}
	}

	@Override
	protected void init() {
		for (int i = 0; i < 6; i++) { // faggot you should be using enums
			addButton(height / 4 + (height / 8) * i, i);
		}
	}

	public void addButton(int y, int preset) {
		addRenderableWidget(new Button(width / 4, y, width / 2, 18, getPresetTitle(preset), _ -> loadPreset(getPresetName(preset)), _ -> Component.empty()) {
			@Override
			protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
				extractDefaultSprite(graphics);
				extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
			}
		});
	}

	private Component getPresetTitle(int preset) {
		return switch (preset) {
			case 0 -> Component.literal("Give it to me plain! (Vanilla)");
			default -> Component.literal("whar?");
		};
	}
	private String getPresetName(int preset) {
		return switch (preset) {
			case 0 -> "vanilla";
			default -> "vanilla";
		};
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
