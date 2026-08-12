package tektonikal.customblockhighlight.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import tektonikal.customblockhighlight.Blockhighlight;
import tektonikal.customblockhighlight.EvilRenderState;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.Vertexer;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PresetsScreen extends Screen {
	private final boolean firstTime;
	private final Screen parent;

	protected PresetsScreen(boolean firstTime, Screen parent) {
		super(Component.literal("Custom Block Highlight Configuration"));
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
			Blockhighlight.unleashHell();
		} catch (IOException _) {
		}
	}

	@Override
	protected void init() {
		for (Preset preset : Preset.values()) {
			addButton(height / 4 + (height / 8) * preset.ordinal(), preset);
		}
	}

	public void addButton(int y, Preset preset) {
		addRenderableWidget(new Button(width / 4, y, width / 2, 18, preset.meow, _ -> loadPreset(preset.name), _ -> Component.empty()) {
			@Override
			protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
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
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, 0xFFFFFFFF);
		graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(0, 0, width, height, 50, graphics.scissorStack.peek()));
	}

	public enum Preset {
		VANILLA(/*you should tap into Component.translatable tbh*/"vanilla", Component.literal("Give it to me plain!")),
		SWEAT("sweat", Component.literal("PvP sweat")),
		TRANS("trans", Component.literal("Beautiful women!")),
		CLASSIC("classic", Component.literal("Classic CBH experience")),
		FANCY("fancy", Component.literal("Gimme all the bells 'n whistles!"));

		public final String name;
		public final Component meow;

		Preset(String name, Component meow) {
			this.name = name;
			this.meow = meow;
		}
	}
}
