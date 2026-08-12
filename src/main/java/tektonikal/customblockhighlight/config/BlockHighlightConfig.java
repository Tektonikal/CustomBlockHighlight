package tektonikal.customblockhighlight.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tektonikal.customblockhighlight.Blockhighlight;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.OutlineType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import static com.sun.jna.Platform.isWindows;

public class BlockHighlightConfig {
	@SuppressWarnings("deprecation")
	public static Gson gson = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
			.serializeNulls()
			.registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
			.setPrettyPrinting()
			.create();

	public static ConfigClassHandler<BlockHighlightConfig> INSTANCE;
	public static final ValueFormatter<Float> BLOCKS_FORMATTER_TWO_PLACES = val -> Component.nullToEmpty(String.format("%.2f", val).replace(".00", "") + (Math.abs(val) == 1 ? " block" : " blocks"));

	public static BlockHighlightConfig config() {
		return INSTANCE.instance();
	}

	//@formatter:off
    //outline stuff
    @SerialEntry public boolean outlineEnabled = true;
        @SerialEntry public Color lineCol = Color.BLACK;
        @SerialEntry public Color lineCol2 = Color.WHITE;
        @SerialEntry public int lineAlpha = 255;
        @SerialEntry public boolean outlineRainbow = true;
        @SerialEntry public OutlineType outlineType = OutlineType.AIR_EXPOSED;
        @SerialEntry public float lineWidth = 2.5F;
        @SerialEntry public float lineExpand = 0;
        @SerialEntry public DepthTestMode lineDepthTest = DepthTestMode.ALWAYS_PASS;
		@SerialEntry public float cutFromCenter = 0.25F;
		@SerialEntry public float cutFromCorner = 0;

	@SerialEntry public boolean secondary = true;
		@SerialEntry public Color slineCol = Color.BLACK;
		@SerialEntry public Color slineCol2 = Color.BLACK;
		@SerialEntry public float slineAlphaMultiplier = 1F;
		@SerialEntry public boolean soutlineRainbow = false;
		@SerialEntry public float slineWidth = 5F;
		@SerialEntry public DepthTestMode slineDepthTest = DepthTestMode.ALWAYS_PASS;

	@SerialEntry public boolean tertiary = false;
		@SerialEntry public Color tlineCol = Color.BLACK;
		@SerialEntry public Color tlineCol2 = Color.WHITE;
		@SerialEntry public float tlineAlphaMultiplier = 1F;
		@SerialEntry public boolean toutlineRainbow = false;
		@SerialEntry public float tlineWidth = 3;
		@SerialEntry public DepthTestMode tlineDepthTest = DepthTestMode.ALWAYS_PASS;

    //fill stuffs
    @SerialEntry public boolean fillEnabled = true;
        @SerialEntry public Color fillCol = Color.BLACK;
        @SerialEntry public Color fillCol2 = Color.WHITE;
        @SerialEntry public int fillOpacity = 128;
        @SerialEntry public boolean fillRainbow = false;
        @SerialEntry public OutlineType fillType = OutlineType.ALL;
        @SerialEntry public float fillExpand = 0.001F;
        @SerialEntry public DepthTestMode fillDepthTest = DepthTestMode.HIDDEN_ONLY;
    //extras
    @SerialEntry public boolean doEasing = true;
    @SerialEntry public float easeSpeed = 20F;
    @SerialEntry public boolean fadeIn = true;
	@SerialEntry public float fadeInSpeed = 15F;
    @SerialEntry public boolean fadeOut = true;
    @SerialEntry public float fadeOutSpeed = 15F;
	@SerialEntry public boolean scale = true;
	@SerialEntry public float scaleSpeed = 15F;
    @SerialEntry public float rainbowSpeed = 5;
    @SerialEntry public int delay = 250;
    @SerialEntry public float saturation = 1;
    @SerialEntry public float brightness = 1;
    @SerialEntry public boolean crystalHelper = true;
    @SerialEntry public Color crystalHelperLineColor = Color.RED;
	@SerialEntry public Color crystalHelperFillColor = Color.RED;
    @SerialEntry public boolean connectedBlocks = true;
	@SerialEntry public boolean updateWhenUnfocused = true;
	@SerialEntry public boolean allowEntities = true;
	@SerialEntry public boolean allowLiquids = true;

	static {
		INSTANCE = ConfigClassHandler.createBuilder(BlockHighlightConfig.class)
				.id(Identifier.fromNamespaceAndPath("custom-block-highlight", "config"))
				.serializer(config -> GsonConfigSerializerBuilder.create(config)
						.overrideGsonBuilder(gson)
						.setPath(FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json"))
						.build()
				).build();
		INSTANCE.load();
	}

	//@formatter:on
	@Updatable
	public static Option<Boolean> o_outlineEnabled = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(true, () -> config().outlineEnabled, newVal -> config().outlineEnabled = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_lineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config().lineCol, newVal -> config().lineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_lineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config().lineCol2, newVal -> config().lineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Integer> o_lineAlpha = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Opacity"))
			.controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
			.stateManager(StateManager.createInstant(255, () -> config().lineAlpha, newVal -> config().lineAlpha = newVal))
			.build();
	@Updatable
	public static Option<Boolean> o_outlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(true, () -> config().outlineRainbow, newVal -> config().outlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
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
			.stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> config().outlineType, newVal -> config().outlineType = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
			.build();
	public static Option<DepthTestMode> o_lineDepthTest = Option.<DepthTestMode>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.literal("Control how this element will appear through walls. Beware of using this with layered lines, visual issues may occur!")))
			.stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> config().lineDepthTest, newVal -> config().lineDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_lineExpand = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Adjust Size By"))
			.stateManager(StateManager.createInstant(0F, () -> config().lineExpand, newVal -> config().lineExpand = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
			.build();
	public static Option<Float> o_lineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(0.5F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(2.5F, () -> config().lineWidth, newVal -> config().lineWidth = newVal))
			.build();
	public static Option<Float> o_cutFromCorner = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Corner"))
			.stateManager(StateManager.createInstant(0F, () -> config().cutFromCorner, newVal -> config().cutFromCorner = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_cutFromCenter = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Cut From Center"))
			.stateManager(StateManager.createInstant(0.25F, () -> config().cutFromCenter, newVal -> config().cutFromCenter = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 0.95F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_secondary = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(true, () -> config().secondary, newVal -> config().secondary = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_slineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config().slineCol, newVal -> config().slineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_slineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config().slineCol2, newVal -> config().slineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Float> o_slineAlphaMultiplier = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Opacity Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> config().slineAlphaMultiplier, newVal -> config().slineAlphaMultiplier = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_soutlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(false, () -> config().soutlineRainbow, newVal -> config().soutlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<DepthTestMode> o_slineDepthTest = Option.<DepthTestMode>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.literal("Control how this element will appear through walls. Beware of using this with layered lines, visual issues may occur!")))
			.stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> config().slineDepthTest, newVal -> config().slineDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_slineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(5F, () -> config().slineWidth, newVal -> config().slineWidth = newVal))
			.build();
	@Updatable
	public static Option<Boolean> o_tertiary = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.stateManager(StateManager.createInstant(false, () -> config().tertiary, newVal -> config().tertiary = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_tlineCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config().tlineCol, newVal -> config().tlineCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_tlineCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config().tlineCol2, newVal -> config().tlineCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Float> o_tlineAlphaMultiplier = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Opacity Multiplier"))
			.stateManager(StateManager.createInstant(1F, () -> config().tlineAlphaMultiplier, newVal -> config().tlineAlphaMultiplier = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_toutlineRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(false, () -> config().toutlineRainbow, newVal -> config().toutlineRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<DepthTestMode> o_tlineDepthTest = Option.<DepthTestMode>createBuilder()
			.name(Component.nullToEmpty("- Depth Test"))
			.description(OptionDescription.of(Component.literal("Control how this element will appear through walls. Beware of using this with layered lines, visual issues may occur!")))
			.stateManager(StateManager.createInstant(DepthTestMode.ALWAYS_PASS, () -> config().tlineDepthTest, newVal -> config().tlineDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_tlineWidth = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Line Width"))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 15F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1f", value) + " px")))
			.stateManager(StateManager.createInstant(3F, () -> config().tlineWidth, newVal -> config().tlineWidth = newVal))
			.build();
	@Updatable
	public static Option<Boolean> o_fillEnabled = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("Enabled"))
			.controller(TickBoxControllerBuilder::create)
			.stateManager(StateManager.createInstant(true, () -> config().fillEnabled, newVal -> config().fillEnabled = newVal))
			.build();
	public static Option<Color> o_fillCol = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Primary"))
			.stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config().fillCol, newVal -> config().fillCol = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Color> o_fillCol2 = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("- Secondary"))
			.stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config().fillCol2, newVal -> config().fillCol2 = newVal))
			.controller(ColorControllerBuilder::create)
			.build();
	public static Option<Integer> o_fillOpacity = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Opacity"))
			.stateManager(StateManager.createInstant(128, () -> config().fillOpacity, newVal -> config().fillOpacity = newVal))
			.controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
			.build();
	@Updatable
	public static Option<Boolean> o_fillRainbow = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Rainbow"))
			.stateManager(StateManager.createInstant(false, () -> config().fillRainbow, newVal -> config().fillRainbow = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<OutlineType> o_fillType = Option.<OutlineType>createBuilder()
			.name(Component.nullToEmpty("- Mode"))
			.description(OptionDescription.of(Component.nullToEmpty("Modes:"),
					Component.nullToEmpty("- Air Exposed"),
					Component.nullToEmpty("- All"),
					Component.nullToEmpty("- Concealed Faces"),
					Component.nullToEmpty("- Looked At")
			))
			.stateManager(StateManager.createInstant(OutlineType.ALL, () -> config().fillType, newVal -> config().fillType = newVal))
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
			.stateManager(StateManager.createInstant(DepthTestMode.HIDDEN_ONLY, () -> config().fillDepthTest, newVal -> config().fillDepthTest = newVal))
			.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
			.build();
	public static Option<Float> o_fillExpand = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Adjust Size By"))
			.stateManager(StateManager.createInstant(0F, () -> config().fillExpand, newVal -> config().fillExpand = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
			.build();
	@Updatable
	public static Option<Boolean> o_doEasing = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Enabled"))
			.stateManager(StateManager.createInstant(true, () -> config().doEasing, newVal -> config().doEasing = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_easeSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(20F, () -> config().easeSpeed, newVal -> config().easeSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 50F).step(0.5F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Boolean> o_fadeIn = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- In"))
			.stateManager(StateManager.createInstant(true, () -> config().fadeIn, newVal -> config().fadeIn = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_fadeInSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Speed"))
			.stateManager(StateManager.createInstant(15F, () -> config().fadeInSpeed, newVal -> config().fadeInSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Boolean> o_fadeOut = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Out"))
			.stateManager(StateManager.createInstant(true, () -> config().fadeOut, newVal -> config().fadeOut = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_fadeOutSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("  - Speed"))
			.stateManager(StateManager.createInstant(15F, () -> config().fadeOutSpeed, newVal -> config().fadeOutSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	@Updatable
	public static Option<Boolean> o_scale = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Enabled"))
			.stateManager(StateManager.createInstant(true, () -> config().scale, newVal -> config().scale = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Float> o_scaleSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(15F, () -> config().scaleSpeed, newVal -> config().scaleSpeed = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Integer> o_delay = Option.<Integer>createBuilder()
			.name(Component.nullToEmpty("- Delay"))
			.stateManager(StateManager.createInstant(250, () -> config().delay, newVal -> config().delay = newVal))
			.description(OptionDescription.of(Component.literal("How much to delay the rainbow color used for the secondary part of the gradient.")))
			.controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(-1000, 1000).step(1).formatValue(value -> Component.literal(value + " ms")))
			.build();
	public static Option<Float> o_rainbowSpeed = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Speed"))
			.stateManager(StateManager.createInstant(5F, () -> config().rainbowSpeed, newVal -> config().rainbowSpeed = newVal))
			.controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 10F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
			.build();
	public static Option<Float> o_saturation = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Saturation"))
			.stateManager(StateManager.createInstant(1F, () -> config().saturation, newVal -> config().saturation = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Float> o_brightness = Option.<Float>createBuilder()
			.name(Component.nullToEmpty("- Brightness"))
			.stateManager(StateManager.createInstant(1F, () -> config().brightness, newVal -> config().brightness = newVal))
			.controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
			.build();
	public static Option<Boolean> o_connectedBlocks = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Connected Outlines"))
			.description(OptionDescription.of(Component.nullToEmpty("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
			.stateManager(StateManager.createInstant(true, () -> config().connectedBlocks, newVal -> config().connectedBlocks = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_updateWhenUnfocused = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Update When Unfocused"))
			.description(OptionDescription.of(Component.literal("Continues moving the outline box toward its target even when it's not being rendered.")))
			.stateManager(StateManager.createInstant(true, () -> config().updateWhenUnfocused, newVal -> config().updateWhenUnfocused = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	@Updatable
	public static Option<Boolean> o_crystalHelper = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Crystal Helper"))
			.description(OptionDescription.of(Component.nullToEmpty("highlights the block in the color below when you are looking at an obsidian block that crystals cannot be placed on.")))
			.stateManager(StateManager.createInstant(true, () -> config().crystalHelper, newVal -> config().crystalHelper = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Color> o_crystalHelperLineColor = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("  - Line Color"))
			.controller(ColorControllerBuilder::create)
			.stateManager(StateManager.createInstant(Color.RED, () -> config().crystalHelperLineColor, color -> config().crystalHelperLineColor = color))
			.build();
	public static Option<Color> o_crystalHelperFillColor = Option.<Color>createBuilder()
			.name(Component.nullToEmpty("  - Fill Color"))
			.controller(ColorControllerBuilder::create)
			.stateManager(StateManager.createInstant(Color.RED, () -> config().crystalHelperFillColor, color -> config().crystalHelperFillColor = color))
			.build();
	public static Option<Boolean> o_allowEntities = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Select Entities"))
			.stateManager(StateManager.createInstant(true, () -> config().allowEntities, newVal -> config().allowEntities = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();
	public static Option<Boolean> o_allowLiquids = Option.<Boolean>createBuilder()
			.name(Component.nullToEmpty("- Select Fluids"))
			.description(OptionDescription.of(Component.literal("Makes fluid source blocks valid targets when holding a bucket.")))
			.stateManager(StateManager.createInstant(true, () -> config().allowLiquids, newVal -> config().allowLiquids = newVal))
			.controller(TickBoxControllerBuilder::create)
			.build();

	public static Screen getConfigScreen(Screen parent) {
			Screen generatedScreen = YetAnotherConfigLib.createBuilder()
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
									.option(o_crystalHelper)
									.option(o_crystalHelperLineColor)
									.option(o_crystalHelperFillColor)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.nullToEmpty("Config"))
									.option(ButtonOption.createBuilder()
											.name(Component.nullToEmpty("- Copy To Clipboard"))
											.action((_, _) -> {
												BlockHighlightConfig.INSTANCE.save();
												Minecraft.getInstance().keyboardHandler.setClipboard(BlockHighlightConfig.gson.toJson(config()));
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
												} catch (JsonSyntaxException e) {
													return;
												}
												try {
													Path path = FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json");
													Files.delete(path);
													Files.createFile(path);
													Files.writeString(path, Minecraft.getInstance().keyboardHandler.getClipboard());
													BlockHighlightConfig.INSTANCE.load();
													Blockhighlight.unleashHell();
												} catch (IOException e) {
													throw new RuntimeException(e);
												}
											})
											.build())
									.option(ButtonOption.createBuilder()
											.name(Component.nullToEmpty("- Presets"))
											.action((screen, _) -> Minecraft.getInstance().setScreenAndShow(new PresetsScreen(false, screen)))
											.text(Component.nullToEmpty("Open"))
											.build())
									.build())
							.build()).save(() -> INSTANCE.save())
					.build().generateScreen(parent); // todo?: dont rebuilt the config screen layout every time
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

	public static void update(Option<Boolean> option, Boolean enabled) {
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
		if(option == o_fadeIn){
			o_fadeInSpeed.setAvailable(enabled);
		}
		if(option == o_fadeOut){
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
		}
		if (option == o_tertiary) {
			o_tlineCol.setAvailable(enabled);
			o_tlineCol2.setAvailable(enabled);
			o_tlineAlphaMultiplier.setAvailable(enabled);
			o_toutlineRainbow.setAvailable(enabled);
			o_tlineDepthTest.setAvailable(enabled);
			o_tlineWidth.setAvailable(enabled);
		}
	}
}