package tektonikal.customblockhighlight.config.screenrenderbullshit;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.NonNull;
import tektonikal.customblockhighlight.Blockhighlight;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.Tweener;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PresetsScreen extends Screen {
	private final boolean firstTime;
	private final Screen parent;
	private Preset hoveredPreset = Preset.VANILLA;
	Tweener tweener = new Tweener(() -> hoveredPreset.ordinal(), 15);

	public PresetsScreen(boolean firstTime, Screen parent) {
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
		addRenderableWidget(new Button(width / 32, y, width / 2, 18, preset.meow, _ -> loadPreset(preset.name), _ -> Component.empty()) {
			@Override
			protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
				extractDefaultSprite(graphics);
				extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
				if (isMouseOver(mouseX, mouseY)) {
					hoveredPreset = preset;
				}
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
		for (Preset preset : Preset.values()) {
			graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(0, (preset.ordinal() * height) - (tweener.getF() * height), preset, 0, 0, width, height, 100F, null));
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		tweener.update();
		for (Preset preset : Preset.values()) {
			graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(0, (preset.ordinal() * height) - (tweener.getF() * height), preset, 0, 0, width, height, 100F, null));
		}
		graphics.nextStratum();
		super.extractBackground(graphics, mouseX, mouseY, a);
	}

	public enum Preset {
		VANILLA(/*you should tap into Component.translatable tbh*/"vanilla", Component.literal("Give it to me plain!"), Blocks.COBBLESTONE, new CBHLineRenderInfo(Shapes.block().move(-0.5F, -0.5F, -0.5F), Color.BLACK, Color.BLACK, new float[]{102, 102, 102, 102, 102, 102}, 2.5F, DepthTestMode.NORMAL)),
		SWEAT("sweat", Component.literal("PvP sweat"), Blocks.SMITHING_TABLE, new CBHLineRenderInfo(Shapes.block().move(-0.5F, -0.5F, -0.5F), Color.BLACK, Color.BLACK, new float[]{102, 102, 102, 102, 102, 102}, 2.5F, DepthTestMode.NORMAL)),
		TRANS("trans", Component.literal("Beautiful women!"), Blocks.AMETHYST_BLOCK, new CBHLineRenderInfo(Shapes.block().move(-0.5F, -0.5F, -0.5F), Color.BLACK, Color.BLACK, new float[]{102, 102, 102, 102, 102, 102}, 2.5F, DepthTestMode.NORMAL)),
		CLASSIC("classic", Component.literal("Classic CBH experience"), Blocks.OAK_PLANKS, new CBHLineRenderInfo(Shapes.block().move(-0.5F, -0.5F, -0.5F), Color.BLACK, Color.BLACK, new float[]{102, 102, 102, 102, 102, 102}, 2.5F, DepthTestMode.NORMAL)),
		FANCY("fancy", Component.literal("Gimme all the bells 'n whistles!"), Blocks.BREWING_STAND, new CBHLineRenderInfo(Shapes.block().move(-0.5F, -0.5F, -0.5F), Color.BLACK, Color.BLACK, new float[]{102, 102, 102, 102, 102, 102}, 2.5F, DepthTestMode.NORMAL));

		public final String name;
		public final Component meow;
		public final Block block;
		public final CBHLineRenderInfo renderInfo;

		Preset(String name, Component meow, Block block, CBHLineRenderInfo renderInfo) {
			this.name = name;
			this.meow = meow;
			this.block = block;
			this.renderInfo = renderInfo;
		}
	}
}
