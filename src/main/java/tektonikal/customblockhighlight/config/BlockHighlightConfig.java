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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tektonikal.customblockhighlight.OutlineType;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class BlockHighlightConfig {
    public static ConfigClassHandler<BlockHighlightConfig> INSTANCE = ConfigClassHandler.createBuilder(BlockHighlightConfig.class)
            .id(Identifier.of("custom-block-highlight", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json")).build()).build();
    public static final ValueFormatter<Float> BLOCKS_FORMATTER_TWO_PLACES = val -> Text.of(String.format("%.2f", val).replace(".00", "") + (Math.abs(val) == 1 ? " block" : " blocks"));

    public static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
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
                .title(Text.literal("Custom Block Highlight"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Outline"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Enabled"))
                                .stateManager(StateManager.createInstant(true, () -> config.outlineEnabled, newVal -> config.outlineEnabled = newVal))
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Color"))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.of(" - Primary"))
                                        .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config.lineCol, newVal -> config.lineCol = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.of(" - Secondary"))
                                        .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config.lineCol2, newVal -> config.lineCol2 = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.of(" - Opacity"))
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1).formatValue(value -> Text.of(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
                                        .stateManager(StateManager.createInstant(255, () -> config.lineAlpha, newVal -> config.lineAlpha = newVal))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Rainbow"))
                                        .stateManager(StateManager.createInstant(true, () -> config.outlineRainbow, newVal -> config.outlineRainbow = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Miscellaneous"))
                                .option(Option.<OutlineType>createBuilder()
                                        .name(Text.of(" - Mode"))
                                        .description(OptionDescription.of(Text.of("Modes:"),
                                                Text.of(" - All"),
                                                Text.of(" - Edges: Uses model shape."),
                                                Text.of(" - Air Exposed"),
                                                Text.of(" - Concealed Faces"),
                                                Text.of(" - Looked At")
                                                ))
                                        .stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> config.outlineType, newVal -> config.outlineType = newVal))
                                        .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                        
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Depth Test"))
                                        .description(OptionDescription.of(Text.of("Whether parts of the outline are visible through other objects.")))
                                        .stateManager(StateManager.createInstant(false, () -> config.lineDepthTest, newVal -> config.lineDepthTest = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.of(" - Adjust Size By"))
                                        .stateManager(StateManager.createInstant(0F, () -> config.lineExpand, newVal -> config.lineExpand = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.of(" - Line Width"))
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1).formatValue(value -> Text.of(value + " px")))
                                        .stateManager(StateManager.createInstant(3, () -> config.lineWidth, newVal -> config.lineWidth = newVal))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.of("Fill"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Enabled"))
                                .controller(TickBoxControllerBuilder::create)
                                .stateManager(StateManager.createInstant(true, () -> config.fillEnabled, newVal -> config.fillEnabled = newVal))
                                
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Color"))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.of(" - Primary"))
                                        .stateManager(StateManager.createInstant(new Color(0, 0, 0), () -> config.fillCol, newVal -> config.fillCol = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.of(" - Secondary"))
                                        .stateManager(StateManager.createInstant(new Color(255, 255, 255), () -> config.fillCol2, newVal -> config.fillCol2 = newVal))
                                        .controller(ColorControllerBuilder::create)
                                        
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.of(" - Opacity"))
                                        .stateManager(StateManager.createInstant(128, () -> config.fillOpacity, newVal -> config.fillOpacity = newVal))
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1).formatValue(value -> Text.of(String.format("%d", ((int) (value * 100 / 255F))) + "%")))
                                        
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Rainbow"))
                                        .stateManager(StateManager.createInstant(false, () -> config.fillRainbow, newVal -> config.fillRainbow = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Miscellaneous"))
                                .option(Option.<OutlineType>createBuilder()
                                        .name(Text.of(" - Mode"))
                                        .description(OptionDescription.of(Text.of("Modes:"),
                                                Text.of(" - All"),
                                                Text.of(" - Edges: Unused for fill."),
                                                Text.of(" - Air Exposed"),
                                                Text.of(" - Concealed Faces"),
                                                Text.of(" - Looked At")
                                        ))
                                        .stateManager(StateManager.createInstant(OutlineType.AIR_EXPOSED, () -> config.fillType, newVal -> config.fillType = newVal))
                                        .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                        
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Depth Test"))
                                        .description(OptionDescription.of(Text.of("Whether parts of the fill are visible through other objects.")))
                                        .stateManager(StateManager.createInstant(false, () -> config.fillDepthTest, newVal -> config.fillDepthTest = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.of(" - Adjust Size By"))
                                        .description(OptionDescription.of(Text.of("By default, this value is set slightly above zero to avoid Z-fighting.")))
                                        .stateManager(StateManager.createInstant(0.001F, () -> config.fillExpand, newVal -> config.fillExpand = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F).formatValue(BLOCKS_FORMATTER_TWO_PLACES))
                                        
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Invert"))
                                        .description(OptionDescription.of(Text.of("Reverses the depth sorting of the fill for a neat effect.")))
                                        .stateManager(StateManager.createInstant(false, () -> config.invert, newVal -> config.invert = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.of("Extras"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Easing"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Enabled"))
                                        .stateManager(StateManager.createInstant(true, () -> config.doEasing, newVal -> config.doEasing = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.of(" - Speed"))
                                        .stateManager(StateManager.createInstant(10F, () -> config.easeSpeed, newVal -> config.easeSpeed = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Text.of(String.format("%.1fx", value))))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Fade"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - In"))
                                        .stateManager(StateManager.createInstant(true, () -> config.fadeIn, newVal -> config.fadeIn = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Out"))
                                        .stateManager(StateManager.createInstant(true, () -> config.fadeOut, newVal -> config.fadeOut = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.of(" - Speed"))
                                        .stateManager(StateManager.createInstant(7.5F, () -> config.fadeSpeed, newVal -> config.fadeSpeed = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 25F).step(0.1F).formatValue(value -> Text.of(String.format("%.1fx", value))))
                                        
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Rainbow"))
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.of(" - Delay"))
                                        .stateManager(StateManager.createInstant(250, () -> config.delay, newVal -> config.delay = newVal))
                                        .controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(-1000, 1000).step(1).formatValue(value -> Text.of(value + " ms")))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.of(" - Speed"))
                                        .stateManager(StateManager.createInstant(5F, () -> config.rainbowSpeed, newVal -> config.rainbowSpeed = newVal))
                                        .controller(integerOption -> FloatSliderControllerBuilder.create(integerOption).range(1F, 10F).step(0.1F).formatValue(value -> Text.of(String.format("%.1fx", value))))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.of(" - Saturation"))
                                        .stateManager(StateManager.createInstant(1F, () -> config.saturation, newVal -> config.saturation = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Text.of(String.format("%d", ((int) (value * 100))) + "%")))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.of(" - Brightness"))
                                        .stateManager(StateManager.createInstant(1F, () -> config.brightness, newVal -> config.brightness = newVal))
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).formatValue(value -> Text.of(String.format("%d", ((int) (value * 100))) + "%")))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Miscellaneous"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Connected Outlines"))
                                        .description(OptionDescription.of(Text.of("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
                                        .stateManager(StateManager.createInstant(true, () -> config.connectedBlocks, newVal -> config.connectedBlocks = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of(" - Crystal Helper"))
                                        .description(OptionDescription.of(Text.of("highlights the block in the color below when you are looking at an obsidian block that crystals cannot be placed on.")))
                                        .stateManager(StateManager.createInstant(true, () -> config.crystalHelper, newVal -> config.crystalHelper = newVal))
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.of("   - Color"))
                                        .controller(ColorControllerBuilder::create)
                                        .stateManager(StateManager.createInstant(Color.RED, () -> config.crystalHelperColor, color -> config.crystalHelperColor = color))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Config"))
                                .option(ButtonOption.createBuilder()
                                        .name(Text.of(" - Copy To Clipboard"))
                                        .action((yaclScreen, buttonOption) -> {
                                            BlockHighlightConfig.INSTANCE.save();
                                            MinecraftClient.getInstance().keyboard.setClipboard(BlockHighlightConfig.gson.toJson(INSTANCE.instance()));
                                        })
                                        .text(Text.of("Copy"))
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.literal(" - Load From Clipboard"))
                                        .description(OptionDescription.of(Text.of("Loads settings from your clipboard if they're valid. The screen will close, reopen it to see your new values.")))
                                        .text(Text.of("Load"))
                                        .action((yaclScreen, buttonOption) -> {
                                            try {
                                                BlockHighlightConfig yeah = BlockHighlightConfig.gson.fromJson(MinecraftClient.getInstance().keyboard.getClipboard(), BlockHighlightConfig.class);
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
                                                Files.writeString(path, MinecraftClient.getInstance().keyboard.getClipboard(), StandardCharsets.UTF_8);
                                                BlockHighlightConfig.INSTANCE.load();
                                                MinecraftClient.getInstance().setScreen(parent);
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