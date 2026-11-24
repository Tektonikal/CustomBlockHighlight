package tektonikal.customblockhighlight.config;

import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.loader.api.FabricLoader;
//? if =1.8.9
//import net.minecraft.client.Minecraft;
import tektonikal.customblockhighlight.util.BlendMode;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.FaceMode;
import tektonikal.customblockhighlight.util.OutlineFit;
import tektonikal.customblockhighlight.util.ScreenOutline;
import tektonikal.customblockhighlight.util.ShapeStyle;

import java.awt.*;
import java.util.List;

//? if >1.8.9
import static net.minecraft.util.Util.getMillis;

public class BlockHighlightConfig {
	public record Range(float min, float max, float step) {
	}

	public static final Range EXPAND_BLOCKS = new Range(-2F, 1F, 0.0625F);
	public static final Range EXPAND_PERCENT = new Range(0F, 2F, 0.01F);
	public static final Range PRIMARY_LINE_WIDTH = new Range(0.5F, 15F, 0.1F), LAYER_LINE_WIDTH = new Range(1F, 15F, 0.1F);
	public static final Range PRIMARY_CUT = new Range(0F, 0.99F, 0.01F), LAYER_CUT = new Range(0F, 0.95F, 0.01F);
	public static final float MAX_CUTS = 0.95F;
	public static final Range THICKNESS_MULT = new Range(0F, 2F, 0.05F);
	public static final Range RAINBOW_SPEED = new Range(1F, 10F, 0.1F);
	public static final Range RAINBOW_DELAY = new Range(-1000, 1000, 1);
	public static final Range UNIT = new Range(0F, 1F, 0.01F);
	public static final Range SCREEN_ROTATION = new Range(0F, 360F, 1F);
	public static final Range EASE_SPEED = new Range(5F, 100F, 0.5F);
	public static final Range ANIMATION_SPEED = new Range(5F, 25F, 0.1F);
	public static final Range ROTATION_SPEED = new Range(5F, 50F, 0.1F);

	@SuppressWarnings("UnusedAssignment")
	public static BlockHighlightConfig ACTIVE_INSTANCE = new BlockHighlightConfig();

	public static BlockHighlightConfig getActiveInstance() {
		return ACTIVE_INSTANCE;
	}

	public static void setActive(BlockHighlightConfig config) {
		if (config == null) return;
		config.simpleMode = ACTIVE_INSTANCE.simpleMode;
		ACTIVE_INSTANCE = config;
		applyActiveToOptions();
	}

	public BlockHighlightConfig fixNulls() {
		BlockHighlightConfig defaults = new BlockHighlightConfig();
		if (primary == null) primary = defaults.primary;
		if (secondary == null) secondary = defaults.secondary;
		if (tertiary == null) tertiary = defaults.tertiary;
		if (fillCol == null) fillCol = defaults.fillCol;
		fillCol.fixNulls(defaults.fillCol);
		for (int i = 0; i < 3; i++) {
			LineConfig layer = getLineConfig(i), fallback = defaults.getLineConfig(i);
			if (layer.color == null) layer.color = fallback.color;
			layer.color.fixNulls(fallback.color);
			if (layer.shapeStyle == null) layer.shapeStyle = fallback.shapeStyle;
			if (layer.outlineType == null) layer.outlineType = fallback.outlineType;
			if (layer.lineDepthTest == null) layer.lineDepthTest = fallback.lineDepthTest;
			if (layer.lineBlend == null) layer.lineBlend = BlendMode.NORMAL;
			if (layer.screenOutline == null) layer.screenOutline = new ScreenOutline();
			if (layer.screenOutline.fit == null) layer.screenOutline.fit = OutlineFit.SURROUNDING;
		}
		if (fillShape == null) fillShape = defaults.fillShape;
		if (fillType == null) fillType = defaults.fillType;
		if (fillDepthTest == null) fillDepthTest = defaults.fillDepthTest;
		if (fillBlend == null) fillBlend = BlendMode.NORMAL;
		if (fillScreenOutline == null) fillScreenOutline = new ScreenOutline();
		if (fillScreenOutline.fit == null) fillScreenOutline.fit = OutlineFit.SURROUNDING;
		if (crystalHelperLineColor == null) crystalHelperLineColor = defaults.crystalHelperLineColor;
		if (crystalHelperFillColor == null) crystalHelperFillColor = defaults.crystalHelperFillColor;
		return this;
	}

	public static float limitedCut(float set, float other) {
		return set > 0 && set + other >= MAX_CUTS ? Math.max(MAX_CUTS - set, 0) : other;
	}

	public LineConfig getLineConfig(int layer) {
		return switch (layer) {
			case 0 -> primary;
			case 1 -> secondary;
			case 2 -> tertiary;
			default -> throw new IllegalStateException();
		};
	}

	public static class ColorSetting {
		public Color col1;
		public Color col2;
		public int alpha = 255;
		public RainbowSettings rainbowSettings;
		public Boolean gradient;

		@SuppressWarnings("unused")
		private ColorSetting() {
		}

		void fixNulls(ColorSetting fallback) {
			if (col1 == null) col1 = fallback.col1;
			if (col2 == null) col2 = fallback.col2;
			if (rainbowSettings == null) rainbowSettings = fallback.rainbowSettings;
		}

		public boolean isGradient() {
			return gradient != null ? gradient : !col1.equals(col2) || rainbowSettings.enabled;
		}

		public Pair<Color, Color> getColors(boolean isCrystalObstructed, Color crystalHelperCol) {
			if (isCrystalObstructed) return Pair.of(crystalHelperCol, crystalHelperCol);
			Color first = this.rainbowSettings.enabled ? this.rainbowSettings.getRainbowCol(true) : this.col1;
			if (!isGradient()) return Pair.of(first, first);
			return Pair.of(first, this.rainbowSettings.enabled ? this.rainbowSettings.getRainbowCol(false) : this.col2);
		}

		public ColorSetting(Color col1, Color col2, int alpha, RainbowSettings rainbowSettings) {
			this.col1 = col1;
			this.col2 = col2;
			this.alpha = alpha;
			this.rainbowSettings = rainbowSettings;
		}
	}

	public static class RainbowSettings {
		public boolean enabled;
		public int delay = 250;
		public float saturation = 1;
		public float brightness = 1;
		public float speed = 5;

		@SuppressWarnings("unused")
		private RainbowSettings() {
		}

		public RainbowSettings(boolean enabled, float speed, int delay, float saturation, float brightness) {
			this.enabled = enabled;
			this.speed = speed;
			this.delay = delay;
			this.saturation = saturation;
			this.brightness = brightness;
		}

		public Color getRainbowCol(boolean primaryCol) {
			//? if >1.8.9 {
			double rainbowState = (getMillis() + (primaryCol ? 0 : this.delay)) * (double) this.speed / 50 % 360;
			//?} else
			//double rainbowState = (Minecraft.getTime() + (primaryCol ? 0 : this.delay)) * (double) this.speed / 50 % 360;
			return Color.getHSBColor((float) (rainbowState / 360), this.saturation, this.brightness);
		}
	}

	public static class LineConfig {
		public boolean enabled;
		public ColorSetting color = new ColorSetting(Color.BLACK, Color.WHITE, 255, new RainbowSettings(false, 5, 250, 1, 1));
		public float lineWidth = 2.5F;
		public DepthTestMode lineDepthTest = DepthTestMode.NORMAL;
		public BlendMode lineBlend = BlendMode.NORMAL;
		public float lineExpandBlocks = 0;
		public float lineExpandPercentage = 1;
		public FaceMode outlineType = FaceMode.ALL;
		public ShapeStyle shapeStyle = ShapeStyle.COLLISION_SHAPE;
		public float cutFromCenter = 0.25F;
		public float cutFromCorner = 0;
		public float innerThicknessMult = 1;
		public float outerThicknessMult = 1;
		public ScreenOutline screenOutline = new ScreenOutline();

		@SuppressWarnings("unused")
		private LineConfig() {
		}

		public LineConfig(boolean enabled, ColorSetting color, float lineWidth, DepthTestMode lineDepthTest, float lineExpandBlocks, float lineExpandPercentage, FaceMode outlineType, ShapeStyle shapeStyle, float cutFromCenter, float cutFromCorner, float innerThicknessMult, float outerThicknessMult) {
			this.enabled = enabled;
			this.color = color;
			this.lineWidth = lineWidth;
			this.lineDepthTest = lineDepthTest;
			this.lineExpandBlocks = lineExpandBlocks;
			this.lineExpandPercentage = lineExpandPercentage;
			this.outlineType = outlineType;
			this.shapeStyle = shapeStyle;
			this.cutFromCenter = cutFromCenter;
			this.cutFromCorner = cutFromCorner;
			this.innerThicknessMult = innerThicknessMult;
			this.outerThicknessMult = outerThicknessMult;
		}
	}

	public boolean enableModRendering = true;
	public boolean drawVanillaOutline = false;

	public LineConfig primary = new LineConfig(true, new ColorSetting(Color.BLACK, Color.BLACK, 103, new RainbowSettings(false, 5, 250, 1, 1)), 2.5F, DepthTestMode.NORMAL, 0, 1, FaceMode.ALL, ShapeStyle.COLLISION_SHAPE, 0, 0, 1, 1);
	public LineConfig secondary = extraLayer();
	public LineConfig tertiary = extraLayer();

	private static LineConfig extraLayer() {
		return new LineConfig(false, new ColorSetting(Color.WHITE, Color.BLACK, 255, new RainbowSettings(false, 5, 250, 1, 1)), 5F, DepthTestMode.ALWAYS_PASS, 0, 1, FaceMode.ALL, ShapeStyle.CLASSIC_BOX, 0, 0, 1, 1);
	}

	public List<LineConfig> lineConfigs() {
		return List.of(primary, secondary, tertiary);
	}

	//@formatter:off

	public boolean fillEnabled = false;
		public ColorSetting fillCol = new ColorSetting(Color.WHITE, Color.BLACK, 128, new RainbowSettings(false, 5, 250, 1, 1));
		public FaceMode fillType = FaceMode.ALL;
		public ShapeStyle fillShape = ShapeStyle.CLASSIC_BOX;
		public float fillExpandBlocks = 0F;
	public float fillExpandPercent = 1F;
		public DepthTestMode fillDepthTest = DepthTestMode.HIDDEN_ONLY;
		public BlendMode fillBlend = BlendMode.NORMAL;
		public ScreenOutline fillScreenOutline = new ScreenOutline();
	public boolean doEasing = false;
	public float easeSpeed = 20F;
	public boolean snapWhenFadedOut = false;
	public boolean unifiedQuads = false;
	public boolean simpleMode = false;
	public boolean restyleOtherOutlines = true;
	public boolean fadeIn = false;
 	public float fadeInSpeed = 15F;
	public boolean fadeOut = false;
	public float fadeOutSpeed = 15F;
 	public boolean scale = false;
 	public float scaleSpeed = 15F;
	public boolean scaleOut = false;
	public float scaleOutSpeed = 15F;
	public boolean animateLineThickness = false;
	public float lineThicknessAnimationSpeed = 15F;
	public boolean lineThicknessOut = false;
	public float lineThicknessOutSpeed = 15F;
	public boolean animateLineCuts = false;
	public float lineCutSpeed = 15F;
	public boolean lineCutOut = false;
	public float lineCutOutSpeed = 15F;
	public boolean crystalHelper = false;
	public boolean crystalHelperOnlyWhenHolding = true;
	public Color crystalHelperLineColor = Color.RED;
 	public Color crystalHelperFillColor = Color.RED;
	public boolean connectedBlocks = true;
 	public boolean updateWhenUnfocused = false;
 	public boolean allowEntities = false;
 	public boolean allowLiquids = false;
	public boolean highlightBarriers = false;
	public boolean onlyWhenHoldingAppropriate = false;
	public boolean onlySourceBlocks = false;
	public boolean rotations = false;
	public float rotationSpeed = 20;
	public boolean showWhenNoHud = false;
	public boolean showWhenNoInteraction = false;

	//@formatter:on

	public static void applyActiveToOptions() {
		ACTIVE_INSTANCE.fixNulls();
		//? if >1.8.9 {
		YaclMenu.pushActive();
		if (FabricLoader.getInstance().isModLoaded("oneconfigv1")) CBHOneConfig.refresh();
		//?} else
		//CBHOneConfig.refresh();
	}

	static {
		ACTIVE_INSTANCE = ConfigManager.load();
	}
}
