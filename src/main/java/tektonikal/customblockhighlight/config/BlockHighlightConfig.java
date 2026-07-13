package tektonikal.customblockhighlight.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tektonikal.customblockhighlight.OutlineType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import static com.sun.jna.Platform.isWindows;


public class BlockHighlightConfig {
	public static ConfigClassHandler<BlockHighlightConfig> INSTANCE = ConfigClassHandler.createBuilder(BlockHighlightConfig.class)
			.id(Identifier.fromNamespaceAndPath("custom-block-highlight", "config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json")).build()).build();
	public static final ValueFormatter<Float> BLOCKS_FORMATTER_TWO_PLACES = val -> Component.nullToEmpty(String.format("%.2f", val).replace(".00", "") + (Math.abs(val) == 1 ? " block" : " blocks"));

	@SuppressWarnings("deprecation")
	public static Gson gson = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
			.serializeNulls()
			.registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
			.setPrettyPrinting()
			.create();
	//@formatter:off
    //outline stuff
    @SerialEntry public boolean outlineEnabled = true;
        @SerialEntry public Color lineCol = Color.BLACK;
        @SerialEntry public Color lineCol2 = Color.WHITE;
        @SerialEntry public int lineAlpha = 255;
        @SerialEntry public boolean outlineRainbow = true;
        @SerialEntry public OutlineType outlineType = OutlineType.AIR_EXPOSED;
        @SerialEntry public float lineWidth = 3;
        @SerialEntry public float lineExpand = 0;
        @SerialEntry public boolean lineDepthTest = false;
		@SerialEntry public float cutFromCenter = 0;
		@SerialEntry public float cutFromCorner = 0;

	@SerialEntry public boolean secondary = false;
		@SerialEntry public Color slineCol = Color.BLACK;
		@SerialEntry public Color slineCol2 = Color.WHITE;
		@SerialEntry public float slineAlphaMultiplier = 1F;
		@SerialEntry public boolean soutlineRainbow = true;
		@SerialEntry public float slineWidth = 3;
		@SerialEntry public boolean slineDepthTest = false;

	@SerialEntry public boolean tertiary = false;
		@SerialEntry public Color tlineCol = Color.BLACK;
		@SerialEntry public Color tlineCol2 = Color.WHITE;
		@SerialEntry public float tlineAlphaMultiplier = 1F;
		@SerialEntry public boolean toutlineRainbow = true;
		@SerialEntry public float tlineWidth = 3;
		@SerialEntry public boolean tlineDepthTest = false;

    //fill stuffs
    @SerialEntry public boolean fillEnabled = true;
        @SerialEntry public Color fillCol = Color.BLACK;
        @SerialEntry public Color fillCol2 = Color.WHITE;
        @SerialEntry public int fillOpacity = 128;
        @SerialEntry public boolean fillRainbow = false;
        @SerialEntry public OutlineType fillType = OutlineType.AIR_EXPOSED;
        @SerialEntry public float fillExpand = 0.001F;
        @SerialEntry public boolean fillDepthTest = false;
    //extras
    @SerialEntry public boolean doEasing = true;
    @SerialEntry public float easeSpeed = 20F;
    @SerialEntry public boolean fadeIn = true;
    @SerialEntry public boolean fadeOut = true;
    @SerialEntry public float fadeSpeed = 15F;
	@SerialEntry public boolean scale = true;
	@SerialEntry public float scaleSpeed = 15F;
    @SerialEntry public float rainbowSpeed = 5;
    @SerialEntry public int delay = 250;
    @SerialEntry public float saturation = 1;
    @SerialEntry public float brightness = 1;
    @SerialEntry public boolean crystalHelper = true;
    @SerialEntry public Color crystalHelperColor = Color.RED;
    @SerialEntry public boolean connectedBlocks = true;
	@SerialEntry public boolean updateWhenUnfocused = true;
	//@formatter:on
	public static Option<Boolean> o_outlineEnabled = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().outlineEnabled, newVal -> BlockHighlightConfig.INSTANCE.instance().outlineEnabled = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_lineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> BlockHighlightConfig.INSTANCE.instance().lineCol, newVal -> BlockHighlightConfig.INSTANCE.instance().lineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_lineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> BlockHighlightConfig.INSTANCE.instance().lineCol2, newVal -> BlockHighlightConfig.INSTANCE.instance().lineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Integer> o_lineAlpha = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Opacity"))
			.controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
			.stateManager(StateManager.createInstant(255, () -> BlockHighlightConfig.INSTANCE.instance().lineAlpha, newVal -> BlockHighlightConfig.INSTANCE.instance().lineAlpha = newVal))
			.build();
	public static Option<Boolean> o_outlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().outlineRainbow, newVal -> BlockHighlightConfig.INSTANCE.instance().outlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<OutlineType> o_outlineType = Option.<OutlineType>createBuilder()
			.name(Component.nullToEmpty("- Mode"))
			.description(OptionDescription.of(Component.nullToEmpty("Modes:"),
					Component.nullToEmpty("- All"),
					Component.nullToEmpty("- Edges: Uses model shape."),
					Component.nullToEmpty("- Air Exposed"),
					Component.nullToEmpty("- Concealed Faces"),
					Component.nullToEmpty("- Looked At")
			))
			.stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> BlockHighlightConfig.INSTANCE.instance().outlineType, newVal -> BlockHighlightConfig.INSTANCE.instance().outlineType = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
			.build();
	public static Option<Boolean> o_lineDepthTest = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.nullToEmpty("Whether parts of the outline are visible through other objects.")))
			.stateManager(StateManager.createInstant(false, () -> BlockHighlightConfig.INSTANCE.instance().lineDepthTest, newVal -> BlockHighlightConfig.INSTANCE.instance().lineDepthTest = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_lineExpand = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Adjust Size By"))
			.stateManager(StateManager.createInstant(0F, () -> BlockHighlightConfig.INSTANCE.instance().lineExpand, newVal -> BlockHighlightConfig.INSTANCE.instance().lineExpand = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
			.build();
	public static Option<Float> o_lineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(0.5F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(3F, () -> BlockHighlightConfig.INSTANCE.instance().lineWidth, newVal -> BlockHighlightConfig.INSTANCE.instance().lineWidth = newVal))
			.build();
	public static Option<Float> o_cutFromCorner = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Corner"))
			.stateManager(StateManager.createInstant(0F, () -> BlockHighlightConfig.INSTANCE.instance().cutFromCorner, newVal -> BlockHighlightConfig.INSTANCE.instance().cutFromCorner = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_cutFromCenter = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Center"))
			.stateManager(StateManager.createInstant(0F, () -> BlockHighlightConfig.INSTANCE.instance().cutFromCenter, newVal -> BlockHighlightConfig.INSTANCE.instance().cutFromCenter = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Boolean> o_secondary = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(false, () -> BlockHighlightConfig.INSTANCE.instance().secondary, newVal -> BlockHighlightConfig.INSTANCE.instance().secondary = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_slineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> BlockHighlightConfig.INSTANCE.instance().slineCol, newVal -> BlockHighlightConfig.INSTANCE.instance().slineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_slineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> BlockHighlightConfig.INSTANCE.instance().slineCol2, newVal -> BlockHighlightConfig.INSTANCE.instance().slineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Float> o_slineAlphaMultiplier = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Opacity Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> BlockHighlightConfig.INSTANCE.instance().slineAlphaMultiplier, newVal -> BlockHighlightConfig.INSTANCE.instance().slineAlphaMultiplier = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Boolean> o_soutlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().soutlineRainbow, newVal -> BlockHighlightConfig.INSTANCE.instance().soutlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_slineDepthTest = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.nullToEmpty("Whether parts of the outline are visible through other objects.")))
			.stateManager(StateManager.createInstant(false, () -> BlockHighlightConfig.INSTANCE.instance().slineDepthTest, newVal -> BlockHighlightConfig.INSTANCE.instance().slineDepthTest = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_slineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(3F, () -> BlockHighlightConfig.INSTANCE.instance().slineWidth, newVal -> BlockHighlightConfig.INSTANCE.instance().slineWidth = newVal))
			.build();
	public static Option<Boolean> o_tertiary = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(false, () -> BlockHighlightConfig.INSTANCE.instance().tertiary, newVal -> BlockHighlightConfig.INSTANCE.instance().tertiary = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_tlineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> BlockHighlightConfig.INSTANCE.instance().tlineCol, newVal -> BlockHighlightConfig.INSTANCE.instance().tlineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_tlineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> BlockHighlightConfig.INSTANCE.instance().tlineCol2, newVal -> BlockHighlightConfig.INSTANCE.instance().tlineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Float> o_tlineAlphaMultiplier = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Opacity Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> BlockHighlightConfig.INSTANCE.instance().tlineAlphaMultiplier, newVal -> BlockHighlightConfig.INSTANCE.instance().tlineAlphaMultiplier = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Boolean> o_toutlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().toutlineRainbow, newVal -> BlockHighlightConfig.INSTANCE.instance().toutlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_tlineDepthTest = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.nullToEmpty("Whether parts of the outline are visible through other objects.")))
			.stateManager(StateManager.createInstant(false, () -> BlockHighlightConfig.INSTANCE.instance().tlineDepthTest, newVal -> BlockHighlightConfig.INSTANCE.instance().tlineDepthTest = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_tlineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(3F, () -> BlockHighlightConfig.INSTANCE.instance().tlineWidth, newVal -> BlockHighlightConfig.INSTANCE.instance().tlineWidth = newVal))
			.build();
	public static Option<Boolean> o_fillEnabled = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.controller(TickBoxControllerBuilder::create)
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().fillEnabled, newVal -> BlockHighlightConfig.INSTANCE.instance().fillEnabled = newVal))
			.build();
	public static Option<Color> o_fillCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> BlockHighlightConfig.INSTANCE.instance().fillCol, newVal -> BlockHighlightConfig.INSTANCE.instance().fillCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_fillCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> BlockHighlightConfig.INSTANCE.instance().fillCol2, newVal -> BlockHighlightConfig.INSTANCE.instance().fillCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Integer> o_fillOpacity = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Opacity"))
			.stateManager(StateManager.createInstant(128, () -> BlockHighlightConfig.INSTANCE.instance().fillOpacity, newVal -> BlockHighlightConfig.INSTANCE.instance().fillOpacity = newVal))
			.controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
			.build();
	public static Option<Boolean> o_fillRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(false, () -> BlockHighlightConfig.INSTANCE.instance().fillRainbow, newVal -> BlockHighlightConfig.INSTANCE.instance().fillRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<OutlineType> o_fillType = Option.<OutlineType>createBuilder()
			.name(Component.nullToEmpty("- Mode"))
			.description(OptionDescription.of(Component.nullToEmpty("Modes:"),
					Component.nullToEmpty("- All"),
					Component.nullToEmpty("- Edges: Unused for fill."),
					Component.nullToEmpty("- Air Exposed"),
					Component.nullToEmpty("- Concealed Faces"),
					Component.nullToEmpty("- Looked At")
			))
			.stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> BlockHighlightConfig.INSTANCE.instance().fillType, newVal -> BlockHighlightConfig.INSTANCE.instance().fillType = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
			.build();
	public static Option<Boolean> o_fillDepthTest = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.nullToEmpty("Whether parts of the fill are visible through other objects.")))
			.stateManager(StateManager.createInstant(false, () -> BlockHighlightConfig.INSTANCE.instance().fillDepthTest, newVal -> BlockHighlightConfig.INSTANCE.instance().fillDepthTest = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_fillExpand = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Adjust Size By"))
			.description(OptionDescription.of(Component.nullToEmpty("By default, this value is set slightly above zero to avoid Z-fighting.")))
			.stateManager(StateManager.createInstant(0.001F, () -> BlockHighlightConfig.INSTANCE.instance().fillExpand, newVal -> BlockHighlightConfig.INSTANCE.instance().fillExpand = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
			.build();
	public static Option<Boolean> o_doEasing = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Enabled"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().doEasing, newVal -> BlockHighlightConfig.INSTANCE.instance().doEasing = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_easeSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(20F, () -> BlockHighlightConfig.INSTANCE.instance().easeSpeed, newVal -> BlockHighlightConfig.INSTANCE.instance().easeSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 50F).step(0.5F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Boolean> o_fadeIn = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- In"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().fadeIn, newVal -> BlockHighlightConfig.INSTANCE.instance().fadeIn = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_fadeOut = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Out"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().fadeOut, newVal -> BlockHighlightConfig.INSTANCE.instance().fadeOut = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_fadeSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(15F, () -> BlockHighlightConfig.INSTANCE.instance().fadeSpeed, newVal -> BlockHighlightConfig.INSTANCE.instance().fadeSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Boolean> o_scale = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Enabled"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().scale, newVal -> BlockHighlightConfig.INSTANCE.instance().scale = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	;
	public static Option<Float> o_scaleSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(15F, () -> BlockHighlightConfig.INSTANCE.instance().scaleSpeed, newVal -> BlockHighlightConfig.INSTANCE.instance().scaleSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Integer> o_delay = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Delay"))
			.stateManager(StateManager.createInstant(250, () -> BlockHighlightConfig.INSTANCE.instance().delay, newVal -> BlockHighlightConfig.INSTANCE.instance().delay = newVal))
			.controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(-1000, 1000).step(1).formatValue(value -> Component.literal(value + " ms")))
			.build();
	public static Option<Float> o_rainbowSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(5F, () -> BlockHighlightConfig.INSTANCE.instance().rainbowSpeed, newVal -> BlockHighlightConfig.INSTANCE.instance().rainbowSpeed = newVal))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 10F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Float> o_saturation = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Saturation"))
			.stateManager(StateManager.createInstant(1F, () -> BlockHighlightConfig.INSTANCE.instance().saturation, newVal -> BlockHighlightConfig.INSTANCE.instance().saturation = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_brightness = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Brightness"))
			.stateManager(StateManager.createInstant(1F, () -> BlockHighlightConfig.INSTANCE.instance().brightness, newVal -> BlockHighlightConfig.INSTANCE.instance().brightness = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Boolean> o_connectedBlocks = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Connected Outlines"))
			.description(OptionDescription.of(Component.nullToEmpty("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().connectedBlocks, newVal -> BlockHighlightConfig.INSTANCE.instance().connectedBlocks = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_updateWhenUnfocused = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Update When Unfocused"))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().updateWhenUnfocused, newVal -> BlockHighlightConfig.INSTANCE.instance().updateWhenUnfocused = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_crystalHelper = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Crystal Helper"))
			.description(OptionDescription.of(Component.nullToEmpty("highlights the block in the color below when you are looking at an obsidian block that crystals cannot be placed on.")))
			.stateManager(StateManager.createInstant(true, () -> BlockHighlightConfig.INSTANCE.instance().crystalHelper, newVal -> BlockHighlightConfig.INSTANCE.instance().crystalHelper = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_crystalHelperColor = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("  - Color"))
			.controller(ColorControllerBuilder::create)
			.stateManager(StateManager.createInstant(Color.RED, () -> BlockHighlightConfig.INSTANCE.instance().crystalHelperColor, color -> BlockHighlightConfig.INSTANCE.instance().crystalHelperColor = color))
			.build();

	public static Screen getConfigScreen(Screen parent) {
		Path firstOpenPath = FabricLoader.getInstance().getConfigDir().resolve(".cbh_info");
		File f = firstOpenPath.toFile();
		if (!f.exists()) {
			// presets screen
			try {
				Files.createFile(firstOpenPath);
				if (isWindows()) {
					Files.setAttribute(firstOpenPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
				}
			} catch (IOException e) {
				//NOP
			}
			return new PresetsScreen(Component.literal("Custom Block Highlight Configuration"), true);
		} else {
			return YetAnotherConfigLib.createBuilder()
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
									.option(o_cutFromCorner)
									.option(o_cutFromCenter)
									.build())
							//TODO: better formatting or something for these
							.group(OptionGroup.createBuilder()
									.name(Component.nullToEmpty("Secondary Layer"))
									.option(o_secondary)
									.option(o_slineCol)
									.option(o_slineCol2)
									.option(o_slineAlphaMultiplier)
									.option(o_soutlineRainbow)
									.option(o_slineDepthTest)
									.option(o_slineWidth)
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
									.option(o_fadeOut)
									.option(o_fadeSpeed)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.nullToEmpty("Scale"))
									.option(o_scale)
									.option(o_scaleSpeed)
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
									.option(o_crystalHelper)
									.option(o_crystalHelperColor)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.nullToEmpty("Config"))
									.option(ButtonOption.createBuilder()
											.name(Component.nullToEmpty("- Copy To Clipboard"))
											.action((_, _) -> {
												BlockHighlightConfig.INSTANCE.save();
												Minecraft.getInstance().keyboardHandler.setClipboard(BlockHighlightConfig.gson.toJson(INSTANCE.instance()));
											})
											.text(Component.nullToEmpty("Copy"))
											.build())
									.option(ButtonOption.createBuilder()
											.name(Component.literal("- Load From Clipboard"))
											.description(OptionDescription.of(Component.nullToEmpty("Loads settings from your clipboard if they're valid. The screen will close, reopen it to see your new values.")))
											.text(Component.nullToEmpty("Load"))
											.action((_, _) -> {
												try {
													BlockHighlightConfig yeah = BlockHighlightConfig.gson.fromJson(Minecraft.getInstance().keyboardHandler.getClipboard(), BlockHighlightConfig.class);
													if (yeah == null) {
														return;
													}
												} catch (Exception e) {
													return;
												}
												try {
													Path path = FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json");
													Files.delete(path);
													Files.createFile(path);
													Files.writeString(path, Minecraft.getInstance().keyboardHandler.getClipboard(), StandardCharsets.UTF_8);
													BlockHighlightConfig.INSTANCE.load();
													Minecraft.getInstance().setScreenAndShow(parent);
												} catch (IOException e) {
													throw new RuntimeException(e);
												}
											})
											.build())
									.option(ButtonOption.createBuilder()
											.name(Component.nullToEmpty("- Presets"))
											.action((_, _) -> {
												Minecraft.getInstance().setScreenAndShow(new PresetsScreen(Component.literal("Custom Block Highlight Configuration"), false));
											})
											.text(Component.nullToEmpty("Open"))
											.build())
									.build())
							.build())
					.build().generateScreen(parent);
		}
	}
}