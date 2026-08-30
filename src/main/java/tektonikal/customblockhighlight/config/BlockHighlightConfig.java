package tektonikal.customblockhighlight.config;

import com.google.gson.JsonSyntaxException;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.impl.ProvidesBindingForDeprecation;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.FaceMode;
import tektonikal.customblockhighlight.util.ShapeStyle;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.sun.jna.Platform.isWindows;
import static net.minecraft.util.Util.getMillis;

@SuppressWarnings("NoTranslation") // temporary fix until fletching table finds a solution
public class BlockHighlightConfig {
    public static final ValueFormatter<Float> BLOCKS_FORMATTER_TWO_PLACES = val -> Component.translatable(String.format("%.2f", val).replace(".00", "") + (Math.abs(val) == 1 ? " block" : " blocks"));
    @SuppressWarnings("UnusedAssignment") // required for clinit stuff
    public static BlockHighlightConfig ACTIVE_INSTANCE = new BlockHighlightConfig();

    public static BlockHighlightConfig getActiveInstance() {
        return ACTIVE_INSTANCE;
    }

    public BlockHighlightConfig() {
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
		public Color col1 = Color.BLACK;
		public Color col2 = Color.WHITE;
		public int alpha = 255;
		public RainbowSettings rainbowSettings = new RainbowSettings(false, 5, 250, 1, 1);

		public Pair<Color, Color> getColors(boolean isCrystalObstructed, Color crystalHelperCol) {
			return Pair.of(
					isCrystalObstructed ? crystalHelperCol : this.rainbowSettings.enabled ? this.rainbowSettings.getRainbowCol(true) : this.col1,
					isCrystalObstructed ? crystalHelperCol : this.rainbowSettings.enabled ? this.rainbowSettings.getRainbowCol(false) : this.col2
			);
		}
	}

	public static class RainbowSettings {
        public boolean enabled;
        public int delay;
        public float saturation;
        public float brightness;
		public float speed;

        public RainbowSettings(boolean enabled, float speed, int delay, float saturation, float brightness) {
            this.enabled = enabled;
	        this.speed = speed;
	        this.delay = delay;
            this.saturation = saturation;
            this.brightness = brightness;
        }
		public Color getRainbowCol(boolean primaryCol) {
			float rainbowState = Mth.ceil((getMillis() + (primaryCol ? 0 : this.delay))) * this.speed / 50;
			rainbowState %= 360;
			return Color.getHSBColor(rainbowState / 360, this.saturation, this.brightness);
		}
    }

	public static class LineConfig {
		public boolean enabled;
		public ColorSetting color = new ColorSetting();
		public float lineWidth = 5F;
		public DepthTestMode lineDepthTest = DepthTestMode.ALWAYS_PASS;
		public float lineExpand = 0;
		public FaceMode outlineType = FaceMode.AIR_EXPOSED;
		public ShapeStyle shapeStyle = ShapeStyle.CLASSIC_BOX;
		public float cutFromCenter = 0.25F;
		public float cutFromCorner = 0;
		public float innerThicknessMult = 1;
		public float outerThicknessMult = 1;

		public LineConfig(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public LineConfig primary = new LineConfig(true);
	public LineConfig secondary = new LineConfig(true);
	public LineConfig tertiary = new LineConfig(false);

	public List<LineConfig> lineConfigs() {
		return List.of(primary, secondary, tertiary);
	}
	public List<LineConfig> reversedLineConfigs() {
		return List.of(tertiary, secondary, primary);
	}

    //@formatter:off
    //outline stuff

    //fill stuffs
    public boolean fillEnabled = true;
		public ColorSetting fillCol = new ColorSetting();
        public FaceMode fillType = FaceMode.ALL;
        public float fillExpand = 0.001F;
        public DepthTestMode fillDepthTest = DepthTestMode.HIDDEN_ONLY;
    //extras
    public boolean doEasing = true;
    public float easeSpeed = 20F;
    public boolean fadeIn = true;
 	public float fadeInSpeed = 15F;
    public boolean fadeOut = true;
    public float fadeOutSpeed = 15F;
 	public boolean scale = true;
 	public float scaleSpeed = 15F;
	public boolean animateLineThickness = true;
	public float lineThicknessAnimationSpeed = 15F;
	public boolean animateLineCuts = true;
	public float lineCutAnimationSpeed = 15F;
    public float rainbowSpeed = 5;
    public int delay = 250;
    public float saturation = 1;
    public float brightness = 1;
    public boolean crystalHelper = true;
    public Color crystalHelperLineColor = Color.RED;
 	public Color crystalHelperFillColor = Color.RED;
    public boolean connectedBlocks = true;
 	public boolean updateWhenUnfocused = true;
 	public boolean allowEntities = true;
 	public boolean allowLiquids = true;
	public boolean rotations = false;
	public boolean showWhenNoHud = false;
	public boolean showWhenNoInteraction = false;


	//@formatter:on
    @Updatable
    public static Option<Boolean> o_outlineEnabled = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.enabled"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.primary.enabled, newVal -> ACTIVE_INSTANCE.primary.enabled = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Color> o_lineCol = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.primary"))
            .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.primary.color.col1, newVal -> ACTIVE_INSTANCE.primary.color.col1 = newVal))
            .controller(ColorControllerBuilder::create)
            .build();
	public static final Option<Color> o_lineCol2 = Option.<Color>createBuilder()
			.name(Component.translatable("cbh.config.secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> ACTIVE_INSTANCE.primary.color.col2, newVal -> ACTIVE_INSTANCE.primary.color.col2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
    public static Option<Integer> o_lineAlpha = Option.<Integer>createBuilder()
            .name(Component.translatable("cbh.config.opacity"))
            .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
            .stateManager(StateManager.createInstant(255, () -> ACTIVE_INSTANCE.primary.color.alpha, newVal -> ACTIVE_INSTANCE.primary.color.alpha = newVal))
            .build();
    @Updatable
    public static Option<Boolean> o_outlineRainbow = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.rainbow"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.primary.color.rainbowSettings.enabled, newVal -> ACTIVE_INSTANCE.primary.color.rainbowSettings.enabled = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<FaceMode> o_outlineType = Option.<FaceMode>createBuilder()
            .name(Component.translatable("cbh.config.mode"))
            .description(OptionDescription.of(Component.translatable("cbh.config.mode.description.1"),
                    Component.translatable("cbh.config.mode.description.2"),
                    Component.translatable("cbh.config.mode.description.3"),
                    Component.translatable("cbh.config.mode.description.4"),
                    Component.translatable("cbh.config.mode.description.5"),
                    Component.translatable("cbh.config.mode.description.6")
            ))
            .stateManager(StateManager.createInstant(FaceMode.AIR_EXPOSED, () -> ACTIVE_INSTANCE.primary.outlineType, newVal -> ACTIVE_INSTANCE.primary.outlineType = newVal))
            .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(FaceMode.class))
            .build();
    public static Option<DepthTestMode> o_lineDepthTest = Option.<DepthTestMode>createBuilder()
            .name(Component.translatable("cbh.config.depthTest"))
            .description(OptionDescription.of(Component.translatable("cbh.config.depthTest.description")))
            .stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> ACTIVE_INSTANCE.primary.lineDepthTest, newVal -> ACTIVE_INSTANCE.primary.lineDepthTest = newVal))
            .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
            .build();
    public static Option<Float> o_lineExpand = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.expand"))
            .stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.primary.lineExpand, newVal -> ACTIVE_INSTANCE.primary.lineExpand = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
            .build();
    public static Option<Float> o_lineWidth = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.lineWidth"))
            .controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(0.5F, 15F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1f", value) + " px")))
            .stateManager(StateManager.createInstant(2.5F, () -> ACTIVE_INSTANCE.primary.lineWidth, newVal -> ACTIVE_INSTANCE.primary.lineWidth = newVal))
            .build();
    public static Option<Float> o_cutFromCorner = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.cutFromCorner"))
            .stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.primary.cutFromCorner, newVal -> ACTIVE_INSTANCE.primary.cutFromCorner = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.99F).step(0.01F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_cutFromCenter = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.cutFromCenter"))
            .stateManager(StateManager.createInstant(0.25F, () -> ACTIVE_INSTANCE.primary.cutFromCenter, newVal -> ACTIVE_INSTANCE.primary.cutFromCenter = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.99F).step(0.01F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_outerThicknessMult = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.outer_thickness_multiplier"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.primary.outerThicknessMult, newVal -> ACTIVE_INSTANCE.primary.outerThicknessMult = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_innerThicknessMult = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.inner_thickness_multiplier"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.primary.innerThicknessMult, newVal -> ACTIVE_INSTANCE.primary.innerThicknessMult = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    @Updatable
    public static Option<Boolean> o_secondary = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.enabled"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.secondary.enabled, newVal -> ACTIVE_INSTANCE.secondary.enabled = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Color> o_slineCol = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.primary"))
            .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.secondary.color.col1, newVal -> ACTIVE_INSTANCE.secondary.color.col1 = newVal))
            .controller(ColorControllerBuilder::create)
            .build();
    public static Option<Color> o_slineCol2 = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.secondary"))
            .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.secondary.color.col2, newVal -> ACTIVE_INSTANCE.secondary.color.col2 = newVal))
            .controller(ColorControllerBuilder::create)
            .build();
    public static Option<Integer> o_slineAlphaMultiplier = Option.<Integer>createBuilder()
            .name(Component.translatable("cbh.config.alphaMultiplier"))
            .stateManager(StateManager.createInstant(1, () -> ACTIVE_INSTANCE.secondary.color.alpha, newVal -> ACTIVE_INSTANCE.secondary.color.alpha = newVal))
            .controller(intOption -> IntegerSliderControllerBuilder.create(intOption).range(0, 255).step(1))
            .build();
    @Updatable
    public static Option<Boolean> o_soutlineRainbow = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.rainbow"))
            .stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.secondary.color.rainbowSettings.enabled, newVal -> ACTIVE_INSTANCE.secondary.color.rainbowSettings.enabled = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<DepthTestMode> o_slineDepthTest = Option.<DepthTestMode>createBuilder()
            .name(Component.translatable("cbh.config.depthTest"))
            .description(OptionDescription.of(Component.translatable("cbh.config.depthTest.description")))
            .stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> ACTIVE_INSTANCE.secondary.lineDepthTest, newVal -> ACTIVE_INSTANCE.secondary.lineDepthTest = newVal))
            .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
            .build();
    public static Option<Float> o_slineWidth = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.lineWidth"))
            .controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1f", value) + " px")))
            .stateManager(StateManager.createInstant(5F, () -> ACTIVE_INSTANCE.secondary.lineWidth, newVal -> ACTIVE_INSTANCE.secondary.lineWidth = newVal))
            .build();
    public static Option<Float> o_scutFromCorner = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.cutFromCorner"))
            .stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.secondary.cutFromCorner, newVal -> ACTIVE_INSTANCE.secondary.cutFromCorner = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_scutFromCenter = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.cutFromCenter"))
            .stateManager(StateManager.createInstant(0.25F, () -> ACTIVE_INSTANCE.secondary.cutFromCenter, newVal -> ACTIVE_INSTANCE.secondary.cutFromCenter = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_souterThicknessMult = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.outer_thickness_multiplier"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.secondary.outerThicknessMult, newVal -> ACTIVE_INSTANCE.secondary.outerThicknessMult = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_sinnerThicknessMult = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.inner_thickness_multiplier"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.secondary.innerThicknessMult, newVal -> ACTIVE_INSTANCE.secondary.innerThicknessMult = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    @Updatable
    public static Option<Boolean> o_tertiary = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.enabled"))
            .stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.tertiary.enabled, newVal -> ACTIVE_INSTANCE.tertiary.enabled = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Color> o_tlineCol = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.primary"))
            .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.tertiary.color.col1, newVal -> ACTIVE_INSTANCE.tertiary.color.col1 = newVal))
            .controller(ColorControllerBuilder::create)
            .build();
    public static Option<Color> o_tlineCol2 = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.secondary"))
            .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> ACTIVE_INSTANCE.tertiary.color.col2, newVal -> ACTIVE_INSTANCE.tertiary.color.col2 = newVal))
            .controller(ColorControllerBuilder::create)
            .build();
	public static Option<Integer> o_tlineAlphaMultiplier = Option.<Integer>createBuilder()
			.name(Component.translatable("cbh.config.alphaMultiplier"))
			.stateManager(StateManager.createInstant(1, () -> ACTIVE_INSTANCE.tertiary.color.alpha, newVal -> ACTIVE_INSTANCE.tertiary.color.alpha = newVal))
			.controller(intOption -> IntegerSliderControllerBuilder.create(intOption).range(0, 255).step(1))
			.build();
    @Updatable
    public static Option<Boolean> o_toutlineRainbow = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.rainbow"))
            .stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.tertiary.color.rainbowSettings.enabled, newVal -> ACTIVE_INSTANCE.tertiary.color.rainbowSettings.enabled = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<DepthTestMode> o_tlineDepthTest = Option.<DepthTestMode>createBuilder()
            .name(Component.translatable("cbh.config.depthTest"))
            .description(OptionDescription.of(Component.translatable("cbh.config.depthTest.description")))
            .stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> ACTIVE_INSTANCE.tertiary.lineDepthTest, newVal -> ACTIVE_INSTANCE.tertiary.lineDepthTest = newVal))
            .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
            .build();
    public static Option<Float> o_tlineWidth = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.lineWidth"))
            .controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1f", value) + " px")))
            .stateManager(StateManager.createInstant(3F, () -> ACTIVE_INSTANCE.tertiary.lineWidth, newVal -> ACTIVE_INSTANCE.tertiary.lineWidth = newVal))
            .build();
    public static Option<Float> o_tcutFromCorner = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.cutFromCorner"))
            .stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.tertiary.cutFromCorner, newVal -> ACTIVE_INSTANCE.tertiary.cutFromCorner = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_tcutFromCenter = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.cutFromCenter"))
            .stateManager(StateManager.createInstant(0.25F, () -> ACTIVE_INSTANCE.tertiary.cutFromCenter, newVal -> ACTIVE_INSTANCE.tertiary.cutFromCenter = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_touterThicknessMult = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.outer_thickness_multiplier"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.tertiary.outerThicknessMult, newVal -> ACTIVE_INSTANCE.tertiary.outerThicknessMult = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_tinnerThicknessMult = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.inner_thickness_multiplier"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.tertiary.innerThicknessMult, newVal -> ACTIVE_INSTANCE.tertiary.innerThicknessMult = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    @Updatable
    public static Option<Boolean> o_fillEnabled = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.enabled"))
            .controller(TickBoxControllerBuilder::create)
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.fillEnabled, newVal -> ACTIVE_INSTANCE.fillEnabled = newVal))
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Color> o_fillCol = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.primary"))
            .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.fillCol.col1, newVal -> ACTIVE_INSTANCE.fillCol.col1 = newVal))
            .controller(ColorControllerBuilder::create)
            .build();
    public static Option<Color> o_fillCol2 = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.secondary"))
            .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> ACTIVE_INSTANCE.fillCol.col2, newVal -> ACTIVE_INSTANCE.fillCol.col2 = newVal))
            .controller(ColorControllerBuilder::create)
            .build();
    public static Option<Integer> o_fillOpacity = Option.<Integer>createBuilder()
            .name(Component.translatable("cbh.config.opacity"))
            .stateManager(StateManager.createInstant(128, () -> ACTIVE_INSTANCE.fillCol.alpha, newVal -> ACTIVE_INSTANCE.fillCol.alpha = newVal))
            .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
            .build();
    @Updatable
    public static Option<Boolean> o_fillRainbow = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.rainbow"))
            .stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.fillCol.rainbowSettings.enabled, newVal -> ACTIVE_INSTANCE.fillCol.rainbowSettings.enabled = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<FaceMode> o_fillType = Option.<FaceMode>createBuilder()
            .name(Component.translatable("cbh.config.mode"))
            .description(OptionDescription.of(Component.translatable("cbh.config.mode.description.1"),
                    Component.translatable("cbh.config.mode.description.2"),
                    Component.translatable("cbh.config.mode.description.3"),
                    Component.translatable("cbh.config.mode.description.4"),
                    Component.translatable("cbh.config.mode.description.5"),
                    Component.translatable("cbh.config.mode.description.6")
            ))
            .stateManager(StateManager.createInstant(FaceMode.ALL, () -> ACTIVE_INSTANCE.fillType, newVal -> ACTIVE_INSTANCE.fillType = newVal))
            .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(FaceMode.class))
            .build();
    public static Option<DepthTestMode> o_fillDepthTest = Option.<DepthTestMode>createBuilder()
            .name(Component.translatable("cbh.config.depthTest"))
            .description(OptionDescription.of(Component.translatable("cbh.config.depthTest.description")))
            .stateManager(StateManager.createInstant(DepthTestMode.HIDDEN_ONLY, () -> ACTIVE_INSTANCE.fillDepthTest, newVal -> ACTIVE_INSTANCE.fillDepthTest = newVal))
            .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
            .build();
    public static Option<Float> o_fillExpand = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.expand"))
            .stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.fillExpand, newVal -> ACTIVE_INSTANCE.fillExpand = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
            .build();
    @Updatable
    public static Option<Boolean> o_doEasing = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.enabled"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.doEasing, newVal -> ACTIVE_INSTANCE.doEasing = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Float> o_easeSpeed = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.speed"))
            .stateManager(StateManager.createInstant(20F, () -> ACTIVE_INSTANCE.easeSpeed, newVal -> ACTIVE_INSTANCE.easeSpeed = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 50F).step(0.5F).formatValue(value -> Component.translatable(String.format("%.1fx", value))))
            .build();
    public static Option<Boolean> o_fadeIn = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.in"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.fadeIn, newVal -> ACTIVE_INSTANCE.fadeIn = newVal))
            .controller(TickBoxControllerBuilder::create)
            .build();
    public static Option<Float> o_fadeInSpeed = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.speed"))
            .stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.fadeInSpeed, newVal -> ACTIVE_INSTANCE.fadeInSpeed = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1fx", value))))
            .build();
    public static Option<Boolean> o_fadeOut = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.out"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.fadeOut, newVal -> ACTIVE_INSTANCE.fadeOut = newVal))
            .controller(TickBoxControllerBuilder::create)
            .build();
    public static Option<Float> o_fadeOutSpeed = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.speed"))
            .stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.fadeOutSpeed, newVal -> ACTIVE_INSTANCE.fadeOutSpeed = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1fx", value))))
            .build();
    @Updatable
    public static Option<Boolean> o_scale = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.enabled"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.scale, newVal -> ACTIVE_INSTANCE.scale = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Float> o_scaleSpeed = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.speed"))
            .stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.scaleSpeed, newVal -> ACTIVE_INSTANCE.scaleSpeed = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1fx", value))))
            .build();
    @Updatable
    public static Option<Boolean> o_animateLineThickness = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.enabled"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.animateLineThickness, newVal -> ACTIVE_INSTANCE.animateLineThickness = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Float> o_lineThicknessSpeed = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.speed"))
            .stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.lineThicknessAnimationSpeed, newVal -> ACTIVE_INSTANCE.lineThicknessAnimationSpeed = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1fx", value))))
            .build();
    public static Option<Integer> o_delay = Option.<Integer>createBuilder()
            .name(Component.translatable("cbh.config.delay"))
            .stateManager(StateManager.createInstant(250, () -> ACTIVE_INSTANCE.delay, newVal -> ACTIVE_INSTANCE.delay = newVal))
            .description(OptionDescription.of(Component.translatable("cbh.config.delay.description")))
            .controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(-1000, 1000).step(1).formatValue(value -> Component.translatable(value + " ms")))
            .build();
    public static Option<Float> o_rainbowSpeed = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.speed"))
            .stateManager(StateManager.createInstant(5F, () -> ACTIVE_INSTANCE.rainbowSpeed, newVal -> ACTIVE_INSTANCE.rainbowSpeed = newVal))
            .controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 10F).step(0.1F).formatValue(value -> Component.translatable(String.format("%.1fx", value))))
            .build();
    public static Option<Float> o_saturation = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.saturation"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.saturation, newVal -> ACTIVE_INSTANCE.saturation = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Float> o_brightness = Option.<Float>createBuilder()
            .name(Component.translatable("cbh.config.brightness"))
            .stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.brightness, newVal -> ACTIVE_INSTANCE.brightness = newVal))
            .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.translatable(String.format("%d", ((int) (value * 100))) + "%")))
            .build();
    public static Option<Boolean> o_connectedBlocks = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.connected_outlines"))
            .description(OptionDescription.of(Component.translatable("cbh.config.connected_outlines.description")))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.connectedBlocks, newVal -> ACTIVE_INSTANCE.connectedBlocks = newVal))
            .controller(TickBoxControllerBuilder::create)
            .build();
    public static Option<Boolean> o_updateWhenUnfocused = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.update_when_unfocused"))
            .description(OptionDescription.of(Component.translatable("cbh.config.update_when_unfocused")))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.updateWhenUnfocused, newVal -> ACTIVE_INSTANCE.updateWhenUnfocused = newVal))
            .controller(TickBoxControllerBuilder::create)
            .build();
    @Updatable
    public static Option<Boolean> o_crystalHelper = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.crystal_helper"))
            .description(OptionDescription.of(Component.translatable("cbh.config.crystal_helper.description")))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.crystalHelper, newVal -> ACTIVE_INSTANCE.crystalHelper = newVal))
            .controller(TickBoxControllerBuilder::create)
            .addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
            .build();
    public static Option<Color> o_crystalHelperLineColor = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.crystal_helper.fill_color"))
            .controller(ColorControllerBuilder::create)
            .stateManager(StateManager.createInstant(Color.RED, () -> ACTIVE_INSTANCE.crystalHelperLineColor, color -> ACTIVE_INSTANCE.crystalHelperLineColor = color))
            .build();
    public static Option<Color> o_crystalHelperFillColor = Option.<Color>createBuilder()
            .name(Component.translatable("cbh.config.crystal_helper.fill_color"))
            .controller(ColorControllerBuilder::create)
            .stateManager(StateManager.createInstant(Color.RED, () -> ACTIVE_INSTANCE.crystalHelperFillColor, color -> ACTIVE_INSTANCE.crystalHelperFillColor = color))
            .build();
    public static Option<Boolean> o_allowEntities = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.select_entities"))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.allowEntities, newVal -> ACTIVE_INSTANCE.allowEntities = newVal))
            .controller(TickBoxControllerBuilder::create)
            .build();
    public static Option<Boolean> o_allowLiquids = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.select_fluids"))
            .description(OptionDescription.of(Component.translatable("cbh.config.select_fluids.description")))
            .stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.allowLiquids, newVal -> ACTIVE_INSTANCE.allowLiquids = newVal))
            .controller(TickBoxControllerBuilder::create)
            .build();
    public static Option<Boolean> o_rotations = Option.<Boolean>createBuilder()
            .name(Component.translatable("cbh.config.rotations"))
            .description(OptionDescription.of(Component.translatable("cbh.config.rotations.description")))
            .stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.rotations, newVal -> ACTIVE_INSTANCE.rotations = newVal))
            .controller(TickBoxControllerBuilder::create)
            .build();
	public static Option<Boolean> o_showWhenNoHud = Option.<Boolean>createBuilder()
			.name(Component.translatable("cbh.config.show_when_no_hud"))
			.stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.showWhenNoHud, newVal -> ACTIVE_INSTANCE.showWhenNoHud = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_showWhenNoInteraction = Option.<Boolean>createBuilder()
			.name(Component.translatable("cbh.config.show_when_no_interaction"))
			.stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.showWhenNoInteraction, newVal -> ACTIVE_INSTANCE.showWhenNoInteraction = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();

    public Screen getConfigScreen(Screen parent) {
        var layout = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("cbh.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("cbh.config.outline"))
                        .option(o_outlineEnabled)
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.color"))
                                .option(o_lineCol)
                                .option(o_lineCol2)
                                .option(o_lineAlpha)
                                .option(o_outlineRainbow)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.misc"))
                                .option(o_outlineType)
                                .option(o_lineDepthTest)
                                .option(o_lineExpand)
                                .option(o_lineWidth)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.subdiv"))
                                .option(o_cutFromCorner)
                                .option(o_outerThicknessMult)
                                .option(o_cutFromCenter)
                                .option(o_innerThicknessMult)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.secondary_layer"))
                                .option(o_secondary)
                                .option(o_slineCol)
                                .option(o_slineCol2)
                                .option(o_slineAlphaMultiplier)
                                .option(o_soutlineRainbow)
                                .option(o_slineDepthTest)
                                .option(o_slineWidth)
                                .option(o_scutFromCorner)
                                .option(o_souterThicknessMult)
                                .option(o_scutFromCenter)
                                .option(o_sinnerThicknessMult)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.tertiary_layer"))
                                .option(o_tertiary)
                                .option(o_tlineCol)
                                .option(o_tlineCol2)
                                .option(o_tlineAlphaMultiplier)
                                .option(o_toutlineRainbow)
                                .option(o_tlineDepthTest)
                                .option(o_tlineWidth)
                                .option(o_tcutFromCorner)
                                .option(o_touterThicknessMult)
                                .option(o_tcutFromCenter)
                                .option(o_tinnerThicknessMult)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("cbh.config.fill"))
                        .option(o_fillEnabled)
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.color"))
                                .option(o_fillCol)
                                .option(o_fillCol2)
                                .option(o_fillOpacity)
                                .option(o_fillRainbow)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.misc"))
                                .option(o_fillType)
                                .option(o_fillDepthTest)
                                .option(o_fillExpand)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("cbh.config.extras"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.easing"))
                                .option(o_doEasing)
                                .option(o_easeSpeed)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.fade"))
                                .option(o_fadeIn)
                                .option(o_fadeInSpeed)
                                .option(o_fadeOut)
                                .option(o_fadeOutSpeed)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.scale"))
                                .option(o_scale)
                                .option(o_scaleSpeed)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.line_thickness"))
                                .option(o_animateLineThickness)
                                .option(o_lineThicknessSpeed)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.rainbow_"))
                                .option(o_delay)
                                .option(o_rainbowSpeed)
                                .option(o_saturation)
                                .option(o_brightness)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config.misc"))
                                .option(o_connectedBlocks)
                                .option(o_updateWhenUnfocused)
                                .option(o_allowEntities)
                                .option(o_allowLiquids)
                                .option(o_rotations)
                                .option(o_crystalHelper)
                                .option(o_crystalHelperLineColor)
                                .option(o_crystalHelperFillColor)
		                        .option(o_showWhenNoHud)
		                        .option(o_showWhenNoInteraction)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("cbh.config"))
                                .option(ButtonOption.createBuilder()
                                        .name(Component.translatable("cbh.config.copy_to_clipboard"))
                                        .action((_, _) -> {
                                            ConfigManager.save(); // technically unnecessary
                                            Minecraft.getInstance().keyboardHandler.setClipboard(ConfigManager.GSON.toJson(this));
                                        })
                                        .text(Component.translatable("cbh.config.copy"))
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Component.translatable("cbh.config.load_from_clipboard"))
                                        .description(OptionDescription.of(Component.translatable("cbh.config.load_from_clipboard.description")))
                                        .text(Component.translatable("cbh.config.load"))
                                        .action((_, _) -> {
                                            try {
                                                BlockHighlightConfig yeah = ConfigManager.GSON.fromJson(Minecraft.getInstance().keyboardHandler.getClipboard(), BlockHighlightConfig.class);
                                                if (yeah == null) {
                                                    return;
                                                }
                                                BlockHighlightConfig.ACTIVE_INSTANCE = yeah.applyValuesToOptionInstances();
                                            } catch (JsonSyntaxException ignored) {
                                            }
                                        })
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Component.translatable("cbh.config.presets"))
                                        .action((screen, _) -> Minecraft.getInstance().setScreenAndShow(new PresetsScreen(false, screen)))
                                        .text(Component.translatable("cbh.config.open"))
                                        .build())
                                .build())
                        .build())
                .save(ConfigManager::save)
                .build();
        Screen generatedScreen = layout.generateScreen(parent);
        Path firstOpenPath = FabricLoader.getInstance().getConfigDir().resolve(".cbh_info"); // im ngl probably have a hidden config option
        if (Files.notExists(firstOpenPath)) {
            // presets screen
            try {
                Files.createFile(firstOpenPath);
                if (isWindows()) {
                    Files.setAttribute(firstOpenPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                }
            } catch (IOException e) {
                //NOP
            }
            return new PresetsScreen(true, generatedScreen);
        } else {
            return generatedScreen;
        }
    }

    public void update(Option<Boolean> option, Boolean enabled) {
        if (option == o_outlineEnabled) {
            o_lineCol.setAvailable(enabled);
            o_lineCol2.setAvailable(enabled);
            o_lineAlpha.setAvailable(enabled);
            o_outlineRainbow.setAvailable(enabled);
            o_outlineType.setAvailable(enabled);
            o_lineDepthTest.setAvailable(enabled);
            o_lineExpand.setAvailable(enabled);
            o_lineWidth.setAvailable(enabled);
            o_cutFromCenter.setAvailable(enabled);
            o_cutFromCorner.setAvailable(enabled);
            o_innerThicknessMult.setAvailable(enabled);
            o_outerThicknessMult.setAvailable(enabled);

            o_secondary.setAvailable(enabled);
            o_tertiary.setAvailable(enabled);
        }
        if (option == o_fillEnabled) {
            o_fillCol.setAvailable(enabled);
            o_fillCol2.setAvailable(enabled);
            o_fillOpacity.setAvailable(enabled);
            o_fillRainbow.setAvailable(enabled);
            o_fillType.setAvailable(enabled);
            o_fillDepthTest.setAvailable(enabled);
            o_fillExpand.setAvailable(enabled);
        }
        if (option == o_fadeIn) {
            o_fadeInSpeed.setAvailable(enabled);
        }
        if (option == o_fadeOut) {
            o_fadeOutSpeed.setAvailable(enabled);
        }
        if (option == o_outlineRainbow) {
            o_lineCol.setAvailable(!enabled && o_outlineEnabled.stateManager().get());
            o_lineCol2.setAvailable(!enabled && o_outlineEnabled.stateManager().get());
        }
        if (option == o_fillRainbow) {
            o_fillCol.setAvailable(!enabled && o_outlineEnabled.stateManager().get());
            o_fillCol2.setAvailable(!enabled && o_outlineEnabled.stateManager().get());
        }
        if (option == o_doEasing) {
            o_easeSpeed.setAvailable(enabled);
        }
        if (option == o_scale) {
            o_scaleSpeed.setAvailable(enabled);
        }
        if (option == o_animateLineThickness) {
            o_lineThicknessSpeed.setAvailable(enabled);
        }
        if (option == o_crystalHelper) {
            o_crystalHelperFillColor.setAvailable(enabled);
            o_crystalHelperLineColor.setAvailable(enabled);
        }
        if (option == o_secondary) {
            o_slineCol.setAvailable(enabled);
            o_slineCol2.setAvailable(enabled);
            o_slineAlphaMultiplier.setAvailable(enabled);
            o_soutlineRainbow.setAvailable(enabled);
            o_slineDepthTest.setAvailable(enabled);
            o_slineWidth.setAvailable(enabled);
            o_scutFromCenter.setAvailable(enabled);
            o_scutFromCorner.setAvailable(enabled);
        }
        if (option == o_tertiary) {
            o_tlineCol.setAvailable(enabled);
            o_tlineCol2.setAvailable(enabled);
            o_tlineAlphaMultiplier.setAvailable(enabled);
            o_toutlineRainbow.setAvailable(enabled);
            o_tlineDepthTest.setAvailable(enabled);
            o_tlineWidth.setAvailable(enabled);
            o_tcutFromCenter.setAvailable(enabled);
            o_tcutFromCorner.setAvailable(enabled);
        }
    }

    @SuppressWarnings("unchecked")
    public BlockHighlightConfig applyValuesToOptionInstances() {
        Arrays.stream(BlockHighlightConfig.class.getDeclaredFields())
                .filter(field -> field.getName().startsWith("o_"))
                .forEach(optionInstanceField -> {
                    try {
                        Option<Object> option = (Option<Object>) optionInstanceField.get(this);
                        if (option == null)
                            return;
                        StateManager<Object> stateManager = option.stateManager();
						Object correspondingValue = ((ProvidesBindingForDeprecation<Object>) stateManager).getBinding().getValue();
                        stateManager.set(correspondingValue);
                        stateManager.apply();
                    } catch (IllegalAccessException _) {
                    }
                });
        return this;
    }

    static {
        ACTIVE_INSTANCE = ConfigManager.load();
    }
}