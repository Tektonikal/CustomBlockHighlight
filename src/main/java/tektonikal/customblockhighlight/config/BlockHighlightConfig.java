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
import dev.isxander.yacl3.impl.controller.StringControllerBuilderImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tektonikal.customblockhighlight.OutlineType;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class BlockHighlightConfig {
    public static ConfigClassHandler<BlockHighlightConfig> INSTANCE = ConfigClassHandler.createBuilder(BlockHighlightConfig.class)
            .id(Identifier.fromNamespaceAndPath("custom-block-highlight", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json")).build()).build();
    public static final ValueFormatter<Float> BLOCKS_FORMATTER_TWO_PLACES = val -> Component.nullToEmpty(String.format("%.2f", val).replace(".00", "") + (Math.abs(val) == 1 ? " block" : " blocks"));

    public static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .serializeNulls()
            .registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
            .setPrettyPrinting()
            .create();
    //@formatter:off
    //outline stuff
    @SerialEntry public boolean outlineEnabled = true;
        @SerialEntry public Color lineCol = Color.decode("#000000");
        @SerialEntry public Color lineCol2 = Color.decode("#FFFFFF");
        @SerialEntry public int lineAlpha = 255;
        @SerialEntry public boolean outlineRainbow = true;
        @SerialEntry public OutlineType outlineType = OutlineType.AIR_EXPOSED;
        @SerialEntry public int lineWidth = 3;
        @SerialEntry public float lineExpand = 0;
        @SerialEntry public boolean lineDepthTest = false;

	@SerialEntry public boolean secondary = false;
		@SerialEntry public Color slineCol = Color.decode("#000000");
		@SerialEntry public Color slineCol2 = Color.decode("#FFFFFF");
		@SerialEntry public boolean soutlineRainbow = true;
		@SerialEntry public int slineWidth = 3;
		@SerialEntry public boolean slineDepthTest = false;

	@SerialEntry public boolean tertiary = false;
		@SerialEntry public Color tlineCol = Color.decode("#000000");
		@SerialEntry public Color tlineCol2 = Color.decode("#FFFFFF");
		@SerialEntry public boolean toutlineRainbow = true;
		@SerialEntry public int tlineWidth = 3;
		@SerialEntry public boolean tlineDepthTest = false;

    //fill stuffs
    @SerialEntry public boolean fillEnabled = true;
        @SerialEntry public Color fillCol = Color.decode("#000000");
        @SerialEntry public Color fillCol2 = Color.decode("#FFFFFF");
        @SerialEntry public int fillOpacity = 128;
        @SerialEntry public boolean fillRainbow = false;
        @SerialEntry public OutlineType fillType = OutlineType.AIR_EXPOSED;
        @SerialEntry public float fillExpand = 0.001F;
        @SerialEntry public boolean fillDepthTest = false;
        @SerialEntry public boolean invert = false;
    //extras
    @SerialEntry public boolean doEasing = true;
    @SerialEntry public float easeSpeed = 10F;
    @SerialEntry public boolean fadeIn = true;
    @SerialEntry public boolean fadeOut = true;
    @SerialEntry public float fadeSpeed = 7.5F;
    @SerialEntry public float rainbowSpeed = 5;
    @SerialEntry public int delay = 250;
    @SerialEntry public float saturation = 1;
    @SerialEntry public float brightness = 1;
    @SerialEntry public boolean crystalHelper = true;
    @SerialEntry public Color crystalHelperColor = Color.RED;
    @SerialEntry public boolean connectedBlocks = true;
    //@formatter:on

    public static Screen getConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(INSTANCE, ((defaults, config, builder) -> builder
                .title(Component.literal("Custom Block Highlight"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Outline"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.nullToEmpty("Enabled"))
                                .stateManager(StateManager.createInstant(true, () -> config.outlineEnabled, newVal -> config.outlineEnabled = newVal))
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Color"))
                                .option(Option.<Color>createBuilder()
                                        .name(Component.nullToEmpty(" - Primary"))
                                        .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config.lineCol, newVal -> config.lineCol = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Component.nullToEmpty(" - Secondary"))
                                        .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config.lineCol2, newVal -> config.lineCol2 = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.nullToEmpty(" - Opacity"))
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
                                        .stateManager(StateManager.createInstant(255, () -> config.lineAlpha, newVal -> config.lineAlpha = newVal))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Rainbow"))
                                        .stateManager(StateManager.createInstant(true, () -> config.outlineRainbow, newVal -> config.outlineRainbow = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Miscellaneous"))
                                .option(Option.<OutlineType>createBuilder()
                                        .name(Component.nullToEmpty(" - Mode"))
                                        .description(OptionDescription.of(Component.nullToEmpty("Modes:"),
                                                Component.nullToEmpty(" - All"),
                                                Component.nullToEmpty(" - Edges: Uses model shape."),
                                                Component.nullToEmpty(" - Air Exposed"),
                                                Component.nullToEmpty(" - Concealed Faces"),
                                                Component.nullToEmpty(" - Looked At")
                                                ))
                                        .stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> config.outlineType, newVal -> config.outlineType = newVal))
                                        .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                        
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Depth Test"))
                                        .description(OptionDescription.of(Component.nullToEmpty("Whether parts of the outline are visible through other objects.")))
                                        .stateManager(StateManager.createInstant(false, () -> config.lineDepthTest, newVal -> config.lineDepthTest = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.nullToEmpty(" - Adjust Size By"))
                                        .stateManager(StateManager.createInstant(0F, () -> config.lineExpand, newVal -> config.lineExpand = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.nullToEmpty(" - Line Width"))
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1).formatValue(value -> Component.literal(value + " px")))
                                        .stateManager(StateManager.createInstant(3, () -> config.lineWidth, newVal -> config.lineWidth = newVal))
                                        .build())
                                .build())
                        .build())
		        .category(ConfigCategory.createBuilder()
				        .name(Component.nullToEmpty("Secondary Outline"))
				        .option(Option.<Boolean>createBuilder()
						        .name(Component.nullToEmpty(" - Enabled"))
						        .stateManager(StateManager.createInstant(false, () -> config.secondary, newVal -> config.secondary = newVal))
						        .controller(TickBoxControllerBuilder::create)
						        .build())
				        .group(OptionGroup.createBuilder()
						        .name(Component.nullToEmpty("Color"))
						        .option(Option.<Color>createBuilder()
								        .name(Component.nullToEmpty(" - Primary"))
								        .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config.slineCol, newVal -> config.slineCol = newVal))
								        .controller(ColorControllerBuilder::create)
								        .build())
						        .option(Option.<Color>createBuilder()
								        .name(Component.nullToEmpty(" - Secondary"))
								        .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config.slineCol2, newVal -> config.slineCol2 = newVal))
								        .controller(ColorControllerBuilder::create)
								        .build())
						        .option(Option.<Boolean>createBuilder()
								        .name(Component.nullToEmpty(" - Rainbow"))
								        .stateManager(StateManager.createInstant(true, () -> config.soutlineRainbow, newVal -> config.soutlineRainbow = newVal))
								        .controller(TickBoxControllerBuilder::create)
								        .build())
						        .build())
				        .group(OptionGroup.createBuilder()
						        .name(Component.nullToEmpty("Miscellaneous"))
						        .option(Option.<Boolean>createBuilder()
								        .name(Component.nullToEmpty(" - Depth Test"))
								        .description(OptionDescription.of(Component.nullToEmpty("Whether parts of the outline are visible through other objects.")))
								        .stateManager(StateManager.createInstant(false, () -> config.slineDepthTest, newVal -> config.slineDepthTest = newVal))
								        .controller(TickBoxControllerBuilder::create)
								        .build())
						        .option(Option.<Integer>createBuilder()
								        .name(Component.nullToEmpty(" - Line Width"))
								        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1).formatValue(value -> Component.literal(value + " px")))
								        .stateManager(StateManager.createInstant(3, () -> config.slineWidth, newVal -> config.slineWidth = newVal))
								        .build())
						        .build())
				        .build())
		        .category(ConfigCategory.createBuilder()
				        .name(Component.nullToEmpty("Tertiary Outline"))
				        .option(Option.<Boolean>createBuilder()
						        .name(Component.nullToEmpty(" - Enabled"))
						        .stateManager(StateManager.createInstant(false, () -> config.tertiary, newVal -> config.tertiary = newVal))
						        .controller(TickBoxControllerBuilder::create)
						        .build())
				        .group(OptionGroup.createBuilder()
						        .name(Component.nullToEmpty("Color"))
						        .option(Option.<Color>createBuilder()
								        .name(Component.nullToEmpty(" - Primary"))
								        .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config.tlineCol, newVal -> config.tlineCol = newVal))
								        .controller(ColorControllerBuilder::create)
								        .build())
						        .option(Option.<Color>createBuilder()
								        .name(Component.nullToEmpty(" - Secondary"))
								        .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config.tlineCol2, newVal -> config.tlineCol2 = newVal))
								        .controller(ColorControllerBuilder::create)
								        .build())
						        .option(Option.<Boolean>createBuilder()
								        .name(Component.nullToEmpty(" - Rainbow"))
								        .stateManager(StateManager.createInstant(true, () -> config.toutlineRainbow, newVal -> config.toutlineRainbow = newVal))
								        .controller(TickBoxControllerBuilder::create)
								        .build())
						        .build())
				        .group(OptionGroup.createBuilder()
						        .name(Component.nullToEmpty("Miscellaneous"))
						        .option(Option.<Boolean>createBuilder()
								        .name(Component.nullToEmpty(" - Depth Test"))
								        .description(OptionDescription.of(Component.nullToEmpty("Whether parts of the outline are visible through other objects.")))
								        .stateManager(StateManager.createInstant(false, () -> config.tlineDepthTest, newVal -> config.tlineDepthTest = newVal))
								        .controller(TickBoxControllerBuilder::create)
								        .build())
						        .option(Option.<Integer>createBuilder()
								        .name(Component.nullToEmpty(" - Line Width"))
								        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1).formatValue(value -> Component.literal(value + " px")))
								        .stateManager(StateManager.createInstant(3, () -> config.tlineWidth, newVal -> config.tlineWidth = newVal))
								        .build())
						        .build())
				        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.nullToEmpty("Fill"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.nullToEmpty("Enabled"))
                                .controller(TickBoxControllerBuilder::create)
                                .stateManager(StateManager.createInstant(true, () -> config.fillEnabled, newVal -> config.fillEnabled = newVal))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Color"))
                                .option(Option.<Color>createBuilder()
                                        .name(Component.nullToEmpty(" - Primary"))
                                        .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config.fillCol, newVal -> config.fillCol = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Component.nullToEmpty(" - Secondary"))
                                        .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config.fillCol2, newVal -> config.fillCol2 = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.nullToEmpty(" - Opacity"))
                                        .stateManager(StateManager.createInstant(128, () -> config.fillOpacity, newVal -> config.fillOpacity = newVal))
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Rainbow"))
                                        .stateManager(StateManager.createInstant(false, () -> config.fillRainbow, newVal -> config.fillRainbow = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Miscellaneous"))
                                .option(Option.<OutlineType>createBuilder()
                                        .name(Component.nullToEmpty(" - Mode"))
                                        .description(OptionDescription.of(Component.nullToEmpty("Modes:"),
                                                Component.nullToEmpty(" - All"),
                                                Component.nullToEmpty(" - Edges: Unused for fill."),
                                                Component.nullToEmpty(" - Air Exposed"),
                                                Component.nullToEmpty(" - Concealed Faces"),
                                                Component.nullToEmpty(" - Looked At")
                                        ))
                                        .stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> config.fillType, newVal -> config.fillType = newVal))
                                        .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Depth Test"))
                                        .description(OptionDescription.of(Component.nullToEmpty("Whether parts of the fill are visible through other objects.")))
                                        .stateManager(StateManager.createInstant(false, () -> config.fillDepthTest, newVal -> config.fillDepthTest = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.nullToEmpty(" - Adjust Size By"))
                                        .description(OptionDescription.of(Component.nullToEmpty("By default, this value is set slightly above zero to avoid Z-fighting.")))
                                        .stateManager(StateManager.createInstant(0.001F, () -> config.fillExpand, newVal -> config.fillExpand = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Invert"))
                                        .description(OptionDescription.of(Component.nullToEmpty("Reverses the depth sorting of the fill for a neat effect.")))
                                        .stateManager(StateManager.createInstant(false, () -> config.invert, newVal -> config.invert = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.nullToEmpty("Extras"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Easing"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Enabled"))
                                        .stateManager(StateManager.createInstant(true, () -> config.doEasing, newVal -> config.doEasing = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.nullToEmpty(" - Speed"))
                                        .stateManager(StateManager.createInstant(10F, () -> config.easeSpeed, newVal -> config.easeSpeed = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Fade"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - In"))
                                        .stateManager(StateManager.createInstant(true, () -> config.fadeIn, newVal -> config.fadeIn = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Out"))
                                        .stateManager(StateManager.createInstant(true, () -> config.fadeOut, newVal -> config.fadeOut = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.nullToEmpty(" - Speed"))
                                        .stateManager(StateManager.createInstant(7.5F, () -> config.fadeSpeed, newVal -> config.fadeSpeed = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Rainbow"))
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.nullToEmpty(" - Delay"))
                                        .stateManager(StateManager.createInstant(250, () -> config.delay, newVal -> config.delay = newVal))
                                        .controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(-1000, 1000).step(1).formatValue(value -> Component.literal(value + " ms")))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.nullToEmpty(" - Speed"))
                                        .stateManager(StateManager.createInstant(5F, () -> config.rainbowSpeed, newVal -> config.rainbowSpeed = newVal))
                                        .controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 10F).step(0.1F).formatValue(value -> Component.literal(String.format("%.1fx", value))))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.nullToEmpty(" - Saturation"))
                                        .stateManager(StateManager.createInstant(1F, () -> config.saturation, newVal -> config.saturation = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.nullToEmpty(" - Brightness"))
                                        .stateManager(StateManager.createInstant(1F, () -> config.brightness, newVal -> config.brightness = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Component.literal(String.format("%d", ((int) (value * 100))) + "%")))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Miscellaneous"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Connected Outlines"))
                                        .description(OptionDescription.of(Component.nullToEmpty("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
                                        .stateManager(StateManager.createInstant(true, () -> config.connectedBlocks, newVal -> config.connectedBlocks = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.nullToEmpty(" - Crystal Helper"))
                                        .description(OptionDescription.of(Component.nullToEmpty("highlights the block in the color below when you are looking at an obsidian block that crystals cannot be placed on.")))
                                        .stateManager(StateManager.createInstant(true, () -> config.crystalHelper, newVal -> config.crystalHelper = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Component.nullToEmpty("   - Color"))
                                        .controller(ColorControllerBuilder::create)
                                        .stateManager(StateManager.createInstant(Color.RED, () -> config.crystalHelperColor, color -> config.crystalHelperColor = color))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.nullToEmpty("Config"))
                                .option(ButtonOption.createBuilder()
                                        .name(Component.nullToEmpty(" - Copy To Clipboard"))
                                        .action((yaclScreen, buttonOption) -> {
                                            BlockHighlightConfig.INSTANCE.save();
                                            Minecraft.getInstance().keyboardHandler.setClipboard(BlockHighlightConfig.gson.toJson(INSTANCE.instance()));
                                        })
                                        .text(Component.nullToEmpty("Copy"))
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Component.literal(" - Load From Clipboard"))
                                        .description(OptionDescription.of(Component.nullToEmpty("Loads settings from your clipboard if they're valid. The screen will close, reopen it to see your new values.")))
                                        .text(Component.nullToEmpty("Load"))
                                        .action((yaclScreen, buttonOption) -> {
                                            try {
                                                BlockHighlightConfig yeah = BlockHighlightConfig.gson.fromJson(Minecraft.getInstance().keyboardHandler.getClipboard(), BlockHighlightConfig.class);
                                                if(yeah == null){
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
                                .build())
                        .build())
        )).generateScreen(parent);
    }
}