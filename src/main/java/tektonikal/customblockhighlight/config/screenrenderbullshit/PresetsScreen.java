package tektonikal.customblockhighlight.config.screenrenderbullshit;

import net.minecraft.client.Minecraft;
//? if >1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//?}
import net.minecraft.network.chat.Component;
//? if >1.8.9 {
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.model.block.BakedModel;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.item.ItemStack;
import org.joml.AxisAngle4f;
import tektonikal.customblockhighlight.RenderBackend;
*///?}
//? if >=1.21.11 {
import org.jspecify.annotations.NonNull;
//?}
//? if <1.21.8 && >1.8.9 {
/*import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
*///?}
import tektonikal.customblockhighlight.CustomBlockHighlight;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.util.Tweener;


public class PresetsScreen extends Screen {
	private static final float PREVIEW_HALF_SIZE = 100, PREVIEW_SLOT_SWAP_DISTANCE = 250, PREVIEW_HOVER_SLIDE = 100, PREVIEW_TILT_DISTANCE = 40;
	private static final float PREVIEW_MARGIN = 8;
	private static final float HEADING_LINES_HEIGHT = 9 * 2.5F;

	public record PreviewSlot(float x, float y, float half, float scale) {
	}

	public static PreviewSlot previewSlot(int width, int height, float slide, float offset) {
		float paneLeft = width / 32F + width / 2F, paneTop = height / 8F + HEADING_LINES_HEIGHT;
		float half = Math.max(Math.min(width - paneLeft, height - paneTop) / 2 - PREVIEW_MARGIN, 1);
		float unit = half / PREVIEW_HALF_SIZE;
		return new PreviewSlot((paneLeft + width) / 2 - slide * PREVIEW_HOVER_SLIDE * unit, (paneTop + height) / 2 + offset * PREVIEW_SLOT_SWAP_DISTANCE * unit,
				half, unit * (50F + 50F * (1 - slide)));
	}

	private PreviewSlot previewSlot(Preset preset) {
		return previewSlot(width, height, presetVals[preset.ordinal()], preset.ordinal() - tweener.getF());
	}

	//? if >=1.21.8 {
	public static void addPreview(GuiGraphicsExtractor graphics, Preset preset, PreviewSlot s, float xAngle, float yAngle) {
		graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(xAngle, yAngle, preset, (int) (s.x() - s.half()), (int) (s.y() - s.half()), (int) (s.x() + s.half()), (int) (s.y() + s.half()), s.scale()));
	}
	//?}

	private void tiltTowards(double mouseX, double mouseY) {
		PreviewSlot centre = previewSlot(width, height, 0, 0);
		float distance = PREVIEW_TILT_DISTANCE * centre.half() / PREVIEW_HALF_SIZE;
		xAngle = (float) Math.atan((centre.x() - mouseX) / distance);
		yAngle = (float) Math.atan((centre.y() - mouseY) / distance);
	}

	private final boolean firstTime;
	private final Screen parent;

	private Preset hoveredPreset = Preset.VANILLA;
	private final float[] presetVals = new float[Preset.values().length];

	private final Tweener tweener = new Tweener(() -> hoveredPreset.ordinal(), 15);
	private float xAngle, yAngle;
	private final Tweener xAngleTweener = new Tweener(() -> xAngle, 20);
	private final Tweener yAngleTweener = new Tweener(() -> yAngle, 20);

	public PresetsScreen(boolean firstTime, Screen parent) {
		//? if >1.8.9
		super(Component.translatable("cbh.presets.screenTitle"));
		this.firstTime = firstTime;
		this.parent = parent;
	}

	private String heading() {
		return Component.translatable(firstTime ? "cbh.presets.welcome" : "cbh.presets.title").getString();
	}

	private static String previewNote() {
		return Component.translatable("cbh.presets.preview_note").getString();
	}

	public static void loadPreset(Preset preset) {
		BlockHighlightConfig.setActive(ConfigManager.copy(preset.config()));
		ConfigManager.save();
	}

	@Override
	//? if >1.8.9 {
	protected void init() {
	//?} else
	//public void init() {
		for (Preset preset : Preset.values()) {
			if (preset != Preset.CURRENT_CONFIG) {
				addButton(height / 4 + (height / 8) * preset.ordinal(), preset);
			}
		}
	}

	//? if >1.8.9 {
	public void addButton(int y, Preset preset) {
		addRenderableWidget(new Button(width / 32, y, width / 2, 18, preset.meow, button -> {
			loadPreset(preset);
			onClose();
		}, value -> Component.empty()) {
			//? if >=1.21.11 {
			@Override
			protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
				extractDefaultSprite(graphics);
				extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
				if (isMouseOver(mouseX, mouseY) || isFocused()) {
					hoveredPreset = preset;
				}
			}
			//?} else {

			/*@Override
			protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
				boolean hovered = isMouseOver(mouseX, mouseY) || isFocused();
				graphics.fill(getX(), getY(), getX() + width, getY() + height, hovered ? 0x66FFFFFF : 0x66000000);
				graphics.centeredText(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFFFF);
				if (hovered) {
					hoveredPreset = preset;
				}
			}
			*///?}
		});
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreenAndShow(parent);
	}
	//?} else {
	/*public void addButton(int y, Preset preset) {
		buttons.add(new PresetButton(preset, width / 32, y, width / 2, 18));
	}

	private class PresetButton extends ButtonWidget {
		private final Preset preset;

		PresetButton(Preset preset, int x, int y, int width, int height) {
			super(preset.ordinal(), x, y, width, height, preset.meow.getString());
			this.preset = preset;
		}

		@Override
		public void render(Minecraft minecraft, int mouseX, int mouseY) {
			if (!visible) return;
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			fill(x, y, x + width, y + height, hovered ? 0x66FFFFFF : 0x66000000);
			centeredText(minecraft.textRenderer, message, x + width / 2, y + (height - 8) / 2, 0xFFFFFFFF);
			if (hovered) {
				hoveredPreset = preset;
			}
		}
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		if (button instanceof PresetButton presetButton) {
			loadPreset(presetButton.preset);
			minecraft.openScreen(parent);
		}
	}

	@Override
	protected void keyPressed(char chr, int key) {
		// escape
		if (key == 1) {
			minecraft.openScreen(parent);
			return;
		}
		super.keyPressed(chr, key);
	}
	*///?}

	//? if >=26.1 {
	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		for (Preset preset : Preset.values()) {
			if (preset != Preset.CURRENT_CONFIG) {
				PreviewSlot s = previewSlot(preset);
				addPreview(graphics, preset, s, xAngleTweener.getF(), yAngleTweener.getF());
			}
		}
		graphics.centeredText(Minecraft.getInstance().font, heading(), width / 2, height / 8, 0xFFFFFFFF);
		graphics.centeredText(Minecraft.getInstance().font, previewNote(), width / 2, (height / 8) + (int) (font.lineHeight * 1.5), 0x80808080);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		tweener.update();
		xAngleTweener.update();
		yAngleTweener.update();

		tiltTowards(Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow()), Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow()));
		// drawn again on the blurred layer for the glow
		graphics.centeredText(Minecraft.getInstance().font, heading(), width / 2, height / 8, 0xFFFFFFFF);
		graphics.centeredText(Minecraft.getInstance().font, previewNote(), width / 2, (height / 8) + (int) (font.lineHeight * 1.5), 0x80808080);

		for (Preset preset : Preset.values()) {
			if (preset != Preset.CURRENT_CONFIG) {
				presetVals[preset.ordinal()] = (float) CustomBlockHighlight.ease(presetVals[preset.ordinal()], hoveredPreset == preset ? 0 : 1, 15);
				PreviewSlot s = previewSlot(preset);
				addPreview(graphics, preset, s, xAngleTweener.getF(), yAngleTweener.getF());
			}
		}
		graphics.nextStratum();
		super.extractBackground(graphics, mouseX, mouseY, a);
	}
	//?} elif >1.8.9 {
	/*@Override
	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.render(graphics, mouseX, mouseY, a);
		graphics.centeredText(Minecraft.getInstance().font, heading(), width / 2, height / 8, 0xFFFFFFFF);
		graphics.centeredText(Minecraft.getInstance().font, previewNote(), width / 2, (int) (height / 8F + (font.lineHeight * 1.5F)), 0x80808080);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		tweener.update();
		xAngleTweener.update();
		yAngleTweener.update();
		//? if >=1.21.8 {
		tiltTowards(Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow()), Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow()));
		//?} else {
		/^tiltTowards(Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth(),
				Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight());
		^///?}
		super.extractBackground(graphics, mouseX, mouseY, a);
		//? if >=1.21.8
		graphics.nextStratum();
		for (Preset preset : Preset.values()) {
			if (preset != Preset.CURRENT_CONFIG) {
				presetVals[preset.ordinal()] = (float) CustomBlockHighlight.ease(presetVals[preset.ordinal()], hoveredPreset == preset ? 0 : 1, 15);
				PreviewSlot s = previewSlot(preset);
				//? if >=1.21.8
				addPreview(graphics, preset, s, xAngleTweener.getF(), yAngleTweener.getF());
				//? if <1.21.8
				//renderLegacyPreviewCube(graphics, preset, s.x(), s.y(), s.scale(), xAngleTweener.getF(), yAngleTweener.getF());
			}
		}
	}
	*///?} else {
	/*@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		tweener.update();
		xAngleTweener.update();
		yAngleTweener.update();
		tiltTowards(mouseX, mouseY);
		renderBackground();
		for (Preset preset : Preset.values()) {
			if (preset != Preset.CURRENT_CONFIG) {
				presetVals[preset.ordinal()] = (float) CustomBlockHighlight.ease(presetVals[preset.ordinal()], hoveredPreset == preset ? 0 : 1, 15);
				PreviewSlot s = previewSlot(preset);
				renderLegacyPreviewCube(preset, s.x(), s.y(), s.scale(), xAngleTweener.getF(), yAngleTweener.getF());
			}
		}
		GlStateManager.clear(256);
		super.render(mouseX, mouseY, tickDelta);
		centeredText(textRenderer, heading(), width / 2, height / 8, 0xFFFFFFFF);
		centeredText(textRenderer, previewNote(), width / 2, (int) (height / 8F + (textRenderer.fontHeight * 1.5F)), 0x808080);
	}
	*///?}

	//? if <1.21.8 && >1.8.9 {
	/*public static void renderLegacyPreviewCube(GuiGraphicsExtractor graphics, Preset preset, float translateX, float translateY, float scale, float xAngle, float yAngle) {
		if (!shouldRender(preset)) return;
		graphics.pose().pushPose();
		graphics.pose().translate(translateX, translateY, 200);
		graphics.pose().scale(scale, scale, -scale);
		graphics.pose().mulPose(PreviewOutline.rotation(xAngle, yAngle));

		Lighting.setupFor3DItems();
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		graphics.pose().pushPose();
		graphics.pose().translate(-0.5F, -0.5F, -0.5F);
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(preset.block.defaultBlockState(), graphics.pose(), bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		graphics.pose().popPose();

		PreviewOutline.draw(graphics.pose(), bufferSource, preset, (float) Minecraft.getInstance().getWindow().getGuiScale());

		Lighting.setupForFlatItems();
		graphics.pose().popPose();
	}
	*///?} elif =1.8.9 {
	/*public static void renderLegacyPreviewCube(Preset preset, float translateX, float translateY, float scale, float xAngle, float yAngle) {
		if (!shouldRender(preset)) return;
		Minecraft minecraft = Minecraft.getInstance();
		GlStateManager.pushMatrix();
		Lighting.turnOnGui();
		GlStateManager.translatef(translateX, translateY, 200);
		GlStateManager.scalef(scale, scale, -scale);
		AxisAngle4f axisAngle = new AxisAngle4f(PreviewOutline.rotation(xAngle, yAngle));
		GlStateManager.rotatef((float) Math.toDegrees(axisAngle.angle), axisAngle.x, axisAngle.y, axisAngle.z);

		GlStateManager.enableDepthTest();
		GlStateManager.clear(256);

		ItemStack stack = new ItemStack(preset.block, 1, preset.metadata);
		BakedModel model = minecraft.getItemRenderer().getModelShaper().getModel(stack);
		minecraft.getTextureManager().bind(TextureAtlas.BLOCKS_LOCATION);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.scalef(2.0F, 2.0F, 2.0F);
		minecraft.getItemRenderer().renderItem(stack, model);
		GlStateManager.popMatrix();
		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		Lighting.turnOff();

		//TODO: screen-space styles outline the unrotated cube because GL holds the rotation
		PreviewOutline.draw(new PoseStack(), RenderBackend.legacyBuffer, preset, 1);

		GlStateManager.popMatrix();
	}
	*///?}

	public enum Preset {
		//? if >1.8.9 {
		VANILLA("vanilla", Blocks.COBBLESTONE),
		SWEAT("sweat", Blocks.SMITHING_TABLE),
		TRANS("trans", Blocks.AMETHYST_BLOCK),
		CLASSIC("classic", Blocks.OAK_PLANKS),
		FANCY("fancy", Blocks.DARK_OAK_LOG),
		CURRENT_CONFIG("current", Blocks.GRASS_BLOCK),
		//?} else {
		/*VANILLA("vanilla", Blocks.COBBLESTONE, 0),
		SWEAT("sweat", Blocks.CRAFTING_TABLE, 0),
		// purple stained clay
		TRANS("trans", Blocks.STAINED_HARDENED_CLAY, 10),
		CLASSIC("classic", Blocks.PLANKS, 0),
		// dark oak log
		FANCY("fancy", Blocks.LOG2, 1),
		CURRENT_CONFIG("current", Blocks.GRASS, 0),
		*///?}
		;

		public final String name;
		public final Component meow;
		public final Block block;
		//? if =1.8.9
		//public final int metadata;
		private BlockHighlightConfig config;

		//? if >1.8.9 {
		Preset(String name, Block block) {
		//?} else {
		/*Preset(String name, Block block, int metadata) {
			this.metadata = metadata;
		*///?}
			this.name = name;
			this.block = block;
			this.meow = Component.translatable("cbh.presets." + name);
		}

		public BlockHighlightConfig config() {
			if (this == CURRENT_CONFIG) return BlockHighlightConfig.getActiveInstance();
			if (config == null) config = ConfigManager.readBuiltInPreset(name);
			return config;
		}
	}

	public static boolean shouldRender(Preset preset) {
		return (preset == Preset.CURRENT_CONFIG && BlockHighlightConfig.getActiveInstance().enableModRendering) || (preset != null && preset != Preset.CURRENT_CONFIG);
	}
}
