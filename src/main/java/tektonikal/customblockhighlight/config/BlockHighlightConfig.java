package tektonikal.customblockhighlight.config;

import com.google.gson.JsonSyntaxException;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.OutlineType;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;

import static com.sun.jna.Platform.isWindows;

public class BlockHighlightConfig {
	public static final ValueFormatter<Float> BLOCKS_FORMATTER_TWO_PLACES = val -> Component.nullToEmpty(String.format("%.2f", val).replace(".00", "") + (Math.abs(val) == 1 ? " block" : " blocks"));
	@SuppressWarnings("UnusedAssignment") // required for clinit stuff
	public static BlockHighlightConfig ACTIVE_INSTANCE = new BlockHighlightConfig();

	public static BlockHighlightConfig getActiveInstance() {
		return ACTIVE_INSTANCE;
	}

	public BlockHighlightConfig() {

	}
	//TODO
	public static class RainbowSettings{
		public boolean enabled;
		public int delay;
		public float saturation;
		public float brightness;
		public RainbowSettings(boolean enabled, int delay, float saturation, float brightness) {
			this.enabled = enabled;
			this.delay = delay;
			this.saturation = saturation;
			this.brightness = brightness;
		}
	}

	//@formatter:off
    //outline stuff
    public boolean outlineEnabled = true;
        public Color lineCol = Color.BLACK;
        public Color lineCol2 = Color.WHITE;
        public int lineAlpha = 255;
        public boolean outlineRainbow = true;
        public OutlineType outlineType = OutlineType.AIR_EXPOSED;
        public float lineWidth = 2.5F;
        public float lineExpand = 0;
        public DepthTestMode lineDepthTest = DepthTestMode.ALWAYS_PASS;

	 	public float cutFromCenter = 0.25F;
	 	public float cutFromCorner = 0;
		public float innerThicknessMult = 1;
		public float outerThicknessMult = 1;

 	public boolean secondary = true;
	 	public Color slineCol = Color.BLACK;
	 	public Color slineCol2 = Color.BLACK;
	 	public float slineAlphaMultiplier = 1F;
	 	public boolean soutlineRainbow = false;
	 	public float slineWidth = 5F;
	 	public DepthTestMode slineDepthTest = DepthTestMode.ALWAYS_PASS;
		public float scutFromCenter = 0.25F;
		public float scutFromCorner = 0;
		public float sinnerThicknessMult = 1;
		public float souterThicknessMult = 1;

 	public boolean tertiary = false;
	 	public Color tlineCol = Color.BLACK;
	 	public Color tlineCol2 = Color.WHITE;
	 	public float tlineAlphaMultiplier = 1F;
	 	public boolean toutlineRainbow = false;
	 	public float tlineWidth = 3;
	 	public DepthTestMode tlineDepthTest = DepthTestMode.ALWAYS_PASS;
		public float tcutFromCenter = 0.25F;
		public float tcutFromCorner = 0;
		public float tinnerThicknessMult = 1;
		public float touterThicknessMult = 1;

    //fill stuffs
    public boolean fillEnabled = true;
        public Color fillCol = Color.BLACK;
        public Color fillCol2 = Color.WHITE;
        public int fillOpacity = 128;
        public boolean fillRainbow = false;
        public OutlineType fillType = OutlineType.ALL;
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
	//TODO:
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
	// todo: consider this
//	public static class SomeCategory {
//		 public boolean someThing;
//	}

	//@formatter:on
	@Updatable
	public static Option<Boolean> o_outlineEnabled = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.outlineEnabled, newVal -> ACTIVE_INSTANCE.outlineEnabled = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Color> o_lineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.lineCol, newVal -> ACTIVE_INSTANCE.lineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_lineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> ACTIVE_INSTANCE.lineCol2, newVal -> ACTIVE_INSTANCE.lineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Integer> o_lineAlpha = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Opacity"))
			.controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
			.stateManager(StateManager.createInstant(255, () -> ACTIVE_INSTANCE.lineAlpha, newVal -> ACTIVE_INSTANCE.lineAlpha = newVal))
			.build();
	@Updatable
	public static Option<Boolean> o_outlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.outlineRainbow, newVal -> ACTIVE_INSTANCE.outlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<OutlineType> o_outlineType = Option.<OutlineType>createBuilder()
			.name(Component.nullToEmpty("- Mode"))
			.description(OptionDescription.of(Component.nullToEmpty("Modes:"),
					Component.nullToEmpty("- Air Exposed"),
					Component.nullToEmpty("- All"),
					Component.nullToEmpty("- Concealed Faces"),
					Component.nullToEmpty("- Edges: Uses model shape."),
					Component.nullToEmpty("- Looked At")
			))
			.stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> ACTIVE_INSTANCE.outlineType, newVal -> ACTIVE_INSTANCE.outlineType = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
			.build();
	public static Option<DepthTestMode> o_lineDepthTest = Option.<DepthTestMode>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.literal("Control how this element will appear through walls. Beware of using this with layered lines, visual issues may occur!")))
			.stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> ACTIVE_INSTANCE.lineDepthTest, newVal -> ACTIVE_INSTANCE.lineDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_lineExpand = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Adjust Size By"))
			.stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.lineExpand, newVal -> ACTIVE_INSTANCE.lineExpand = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
			.build();
	public static Option<Float> o_lineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(0.5F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(2.5F, () -> ACTIVE_INSTANCE.lineWidth, newVal -> ACTIVE_INSTANCE.lineWidth = newVal))
			.build();
	public static Option<Float> o_cutFromCorner = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Corner"))
			.stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.cutFromCorner, newVal -> ACTIVE_INSTANCE.cutFromCorner = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_cutFromCenter = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Center"))
			.stateManager(StateManager.createInstant(0.25F, () -> ACTIVE_INSTANCE.cutFromCenter, newVal -> ACTIVE_INSTANCE.cutFromCenter = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_outerThicknessMult = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Outer Thickness Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.outerThicknessMult, newVal -> ACTIVE_INSTANCE.outerThicknessMult = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_innerThicknessMult = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Inner Thickness Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.innerThicknessMult, newVal -> ACTIVE_INSTANCE.innerThicknessMult = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_secondary = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.secondary, newVal -> ACTIVE_INSTANCE.secondary = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Color> o_slineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.slineCol, newVal -> ACTIVE_INSTANCE.slineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_slineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.slineCol2, newVal -> ACTIVE_INSTANCE.slineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Float> o_slineAlphaMultiplier = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Opacity Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.slineAlphaMultiplier, newVal -> ACTIVE_INSTANCE.slineAlphaMultiplier = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_soutlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.soutlineRainbow, newVal -> ACTIVE_INSTANCE.soutlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<DepthTestMode> o_slineDepthTest = Option.<DepthTestMode>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.literal("Control how this element will appear through walls. Beware of using this with layered lines, visual issues may occur!")))
			.stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> ACTIVE_INSTANCE.slineDepthTest, newVal -> ACTIVE_INSTANCE.slineDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_slineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(5F, () -> ACTIVE_INSTANCE.slineWidth, newVal -> ACTIVE_INSTANCE.slineWidth = newVal))
			.build();
	public static Option<Float> o_scutFromCorner = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Corner"))
			.stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.scutFromCorner, newVal -> ACTIVE_INSTANCE.scutFromCorner = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_scutFromCenter = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Center"))
			.stateManager(StateManager.createInstant(0.25F, () -> ACTIVE_INSTANCE.scutFromCenter, newVal -> ACTIVE_INSTANCE.scutFromCenter = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_souterThicknessMult = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Outer Thickness Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.souterThicknessMult, newVal -> ACTIVE_INSTANCE.souterThicknessMult = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_sinnerThicknessMult = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Inner Thickness Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.sinnerThicknessMult, newVal -> ACTIVE_INSTANCE.sinnerThicknessMult = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_tertiary = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.tertiary, newVal -> ACTIVE_INSTANCE.tertiary = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Color> o_tlineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.tlineCol, newVal -> ACTIVE_INSTANCE.tlineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_tlineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> ACTIVE_INSTANCE.tlineCol2, newVal -> ACTIVE_INSTANCE.tlineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Float> o_tlineAlphaMultiplier = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Opacity Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.tlineAlphaMultiplier, newVal -> ACTIVE_INSTANCE.tlineAlphaMultiplier = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_toutlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.toutlineRainbow, newVal -> ACTIVE_INSTANCE.toutlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<DepthTestMode> o_tlineDepthTest = Option.<DepthTestMode>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.literal("Control how this element will appear through walls. Beware of using this with layered lines, visual issues may occur!")))
			.stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> ACTIVE_INSTANCE.tlineDepthTest, newVal -> ACTIVE_INSTANCE.tlineDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_tlineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(3F, () -> ACTIVE_INSTANCE.tlineWidth, newVal -> ACTIVE_INSTANCE.tlineWidth = newVal))
			.build();
	public static Option<Float> o_tcutFromCorner = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Corner"))
			.stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.tcutFromCorner, newVal -> ACTIVE_INSTANCE.tcutFromCorner = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_tcutFromCenter = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Center"))
			.stateManager(StateManager.createInstant(0.25F, () -> ACTIVE_INSTANCE.tcutFromCenter, newVal -> ACTIVE_INSTANCE.tcutFromCenter = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_touterThicknessMult = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Outer Thickness Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.touterThicknessMult, newVal -> ACTIVE_INSTANCE.touterThicknessMult = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_tinnerThicknessMult = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Inner Thickness Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.tinnerThicknessMult, newVal -> ACTIVE_INSTANCE.tinnerThicknessMult = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_fillEnabled = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.controller(TickBoxControllerBuilder::create)
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.fillEnabled, newVal -> ACTIVE_INSTANCE.fillEnabled = newVal))
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Color> o_fillCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> ACTIVE_INSTANCE.fillCol, newVal -> ACTIVE_INSTANCE.fillCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_fillCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> ACTIVE_INSTANCE.fillCol2, newVal -> ACTIVE_INSTANCE.fillCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Integer> o_fillOpacity = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Opacity"))
			.stateManager(StateManager.createInstant(128, () -> ACTIVE_INSTANCE.fillOpacity, newVal -> ACTIVE_INSTANCE.fillOpacity = newVal))
			.controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_fillRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.fillRainbow, newVal -> ACTIVE_INSTANCE.fillRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<OutlineType> o_fillType = Option.<OutlineType>createBuilder()
			.name(Component.nullToEmpty("- Mode"))
			.description(OptionDescription.of(Component.nullToEmpty("Modes:"),
					Component.nullToEmpty("- Air Exposed"),
					Component.nullToEmpty("- All"),
					Component.nullToEmpty("- Concealed Faces"),
					Component.nullToEmpty("- Looked At")
			))
			.stateManager(StateManager.createInstant(OutlineType.ALL, () -> ACTIVE_INSTANCE.fillType, newVal -> ACTIVE_INSTANCE.fillType = newVal))
			.addListener((option, _) -> {
				if (option.pendingValue() == OutlineType.EDGES) {
					option.requestSet(OutlineType.LOOKAT);
				}
			})
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
			.build();
	public static Option<DepthTestMode> o_fillDepthTest = Option.<DepthTestMode>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.literal("Control how this element will appear through walls. Beware of using this with layered lines, visual issues may occur!")))
			.stateManager(StateManager.createInstant(DepthTestMode.HIDDEN_ONLY, () -> ACTIVE_INSTANCE.fillDepthTest, newVal -> ACTIVE_INSTANCE.fillDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_fillExpand = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Adjust Size By"))
			.stateManager(StateManager.createInstant(0F, () -> ACTIVE_INSTANCE.fillExpand, newVal -> ACTIVE_INSTANCE.fillExpand = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
			.build();
	@Updatable
	public static Option<Boolean> o_doEasing = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Enabled"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.doEasing, newVal -> ACTIVE_INSTANCE.doEasing = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Float> o_easeSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(20F, () -> ACTIVE_INSTANCE.easeSpeed, newVal -> ACTIVE_INSTANCE.easeSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 50F).step(0.5F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Boolean> o_fadeIn = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- In"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.fadeIn, newVal -> ACTIVE_INSTANCE.fadeIn = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_fadeInSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Speed"))
			.stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.fadeInSpeed, newVal -> ACTIVE_INSTANCE.fadeInSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Boolean> o_fadeOut = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Out"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.fadeOut, newVal -> ACTIVE_INSTANCE.fadeOut = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_fadeOutSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Speed"))
			.stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.fadeOutSpeed, newVal -> ACTIVE_INSTANCE.fadeOutSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	@Updatable
	public static Option<Boolean> o_scale = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Enabled"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.scale, newVal -> ACTIVE_INSTANCE.scale = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Float> o_scaleSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.scaleSpeed, newVal -> ACTIVE_INSTANCE.scaleSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	@Updatable
	public static Option<Boolean> o_animateLineThickness = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Enabled"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.animateLineThickness, newVal -> ACTIVE_INSTANCE.animateLineThickness = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Float> o_lineThicknessSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(15F, () -> ACTIVE_INSTANCE.lineThicknessAnimationSpeed, newVal -> ACTIVE_INSTANCE.lineThicknessAnimationSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Integer> o_delay = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Delay"))
			.stateManager(StateManager.createInstant(250, () -> ACTIVE_INSTANCE.delay, newVal -> ACTIVE_INSTANCE.delay = newVal))
			.description(OptionDescription.of(Component.literal("How much to delay the rainbow color used for the secondary part of the gradient.")))
			.controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(-1000, 1000).step(1).formatValue(value -> Component.literal(value + " ms")))
			.build();
	public static Option<Float> o_rainbowSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(5F, () -> ACTIVE_INSTANCE.rainbowSpeed, newVal -> ACTIVE_INSTANCE.rainbowSpeed = newVal))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 10F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Float> o_saturation = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Saturation"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.saturation, newVal -> ACTIVE_INSTANCE.saturation = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_brightness = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Brightness"))
			.stateManager(StateManager.createInstant(1F, () -> ACTIVE_INSTANCE.brightness, newVal -> ACTIVE_INSTANCE.brightness = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Boolean> o_connectedBlocks = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Connected Outlines"))
			.description(OptionDescription.of(Component.nullToEmpty("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.connectedBlocks, newVal -> ACTIVE_INSTANCE.connectedBlocks = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_updateWhenUnfocused = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Update When Unfocused"))
			.description(OptionDescription.of(Component.literal("Continues moving the outline box toward its target even when it's not being rendered.")))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.updateWhenUnfocused, newVal -> ACTIVE_INSTANCE.updateWhenUnfocused = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	@Updatable
	public static Option<Boolean> o_crystalHelper = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Crystal Helper"))
			.description(OptionDescription.of(Component.nullToEmpty("highlights the block in the color below when you are looking at an obsidian block that crystals cannot be placed on.")))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.crystalHelper, newVal -> ACTIVE_INSTANCE.crystalHelper = newVal))
			.controller(TickBoxControllerBuilder::create)
			.addListener((option, _) -> ACTIVE_INSTANCE.update(option, option.pendingValue()))
			.build();
	public static Option<Color> o_crystalHelperLineColor = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("  - Line Color"))
			.controller(ColorControllerBuilder::create)
			.stateManager(StateManager.createInstant(Color.RED, () -> ACTIVE_INSTANCE.crystalHelperLineColor, color -> ACTIVE_INSTANCE.crystalHelperLineColor = color))
			.build();
	public static Option<Color> o_crystalHelperFillColor = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("  - Fill Color"))
			.controller(ColorControllerBuilder::create)
			.stateManager(StateManager.createInstant(Color.RED, () -> ACTIVE_INSTANCE.crystalHelperFillColor, color -> ACTIVE_INSTANCE.crystalHelperFillColor = color))
			.build();
	public static Option<Boolean> o_allowEntities = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Select Entities"))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.allowEntities, newVal -> ACTIVE_INSTANCE.allowEntities = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_allowLiquids = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Select Fluids"))
			.description(OptionDescription.of(Component.literal("Makes fluid source blocks valid targets when holding a bucket.")))
			.stateManager(StateManager.createInstant(true, () -> ACTIVE_INSTANCE.allowLiquids, newVal -> ACTIVE_INSTANCE.allowLiquids = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_rotations = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rotations"))
			.description(OptionDescription.of(Component.literal("Rotates the outline based on what side of the block you're looking at. Not a finalized or stable feature, here be dragons!")))
			.stateManager(StateManager.createInstant(false, () -> ACTIVE_INSTANCE.rotations, newVal -> ACTIVE_INSTANCE.rotations = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();

	public Screen getConfigScreen(Screen parent) {
		var layout = YetAnotherConfigLib.createBuilder()
				.title(Component.literal("Custom Block Highlight"))
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("Outline"))
						.option(o_outlineEnabled)
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Color"))
								.option(o_lineCol)
								.option(o_lineCol2)
								.option(o_lineAlpha)
								.option(o_outlineRainbow)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Miscellaneous"))
								.option(o_outlineType)
								.option(o_lineDepthTest)
								.option(o_lineExpand)
								.option(o_lineWidth)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("NAME ME LATER"))
								.option(o_cutFromCorner)
								.option(o_outerThicknessMult)
								.option(o_cutFromCenter)
								.option(o_innerThicknessMult)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Secondary Layer"))
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
								.name(Component.nullToEmpty("Tertiary Layer"))
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
						.name(Component.nullToEmpty("Fill"))
						.option(o_fillEnabled)
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Color"))
								.option(o_fillCol)
								.option(o_fillCol2)
								.option(o_fillOpacity)
								.option(o_fillRainbow)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Miscellaneous"))
								.option(o_fillType)
								.option(o_fillDepthTest)
								.option(o_fillExpand)
								.build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.nullToEmpty("Extras"))
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Easing"))
								.option(o_doEasing)
								.option(o_easeSpeed)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Fade"))
								.option(o_fadeIn)
								.option(o_fadeInSpeed)
								.option(o_fadeOut)
								.option(o_fadeOutSpeed)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Scale"))
								.option(o_scale)
								.option(o_scaleSpeed)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Line Thickness"))
								.option(o_animateLineThickness)
								.option(o_lineThicknessSpeed)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Rainbow"))
								.option(o_delay)
								.option(o_rainbowSpeed)
								.option(o_saturation)
								.option(o_brightness)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Miscellaneous"))
								.option(o_connectedBlocks)
								.option(o_updateWhenUnfocused)
								.option(o_allowEntities)
								.option(o_allowLiquids)
								.option(o_rotations)
								.option(o_crystalHelper)
								.option(o_crystalHelperLineColor)
								.option(o_crystalHelperFillColor)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.nullToEmpty("Config"))
								.option(ButtonOption.createBuilder()
										.name(Component.nullToEmpty("- Copy To Clipboard"))
										.action((_, _) -> {
											ConfigManager.save(); // technically unnecessary
											Minecraft.getInstance().keyboardHandler.setClipboard(ConfigManager.GSON.toJson(this));
										})
										.text(Component.nullToEmpty("Copy"))
										.build())
								.option(ButtonOption.createBuilder()
										.name(Component.literal("- Load From Clipboard"))
										.description(OptionDescription.of(Component.nullToEmpty("Loads settings from your clipboard if they're valid. The screen will close, reopen it to see your new values.")))
										.text(Component.nullToEmpty("Load"))
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
										.name(Component.nullToEmpty("- Presets"))
										.action((screen, _) -> Minecraft.getInstance().setScreenAndShow(new PresetsScreen(false, screen)))
										.text(Component.nullToEmpty("Open"))
										.build())
								.build())
						.build())
				.save(ConfigManager::save)
				.build();
		Screen generatedScreen = layout.generateScreen(parent);
		Path firstOpenPath = FabricLoader.getInstance().getConfigDir().resolve(".cbh_info");
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
						Object correspondingValue = BlockHighlightConfig.class.getField(optionInstanceField.getName().replace("o_", "")).get(this);

						stateManager.set(correspondingValue);
						stateManager.apply();
					} catch (IllegalAccessException | NoSuchFieldException _) {
					}
				});
		return this;
	}

	static {
		ACTIVE_INSTANCE = ConfigManager.load();
	}
}