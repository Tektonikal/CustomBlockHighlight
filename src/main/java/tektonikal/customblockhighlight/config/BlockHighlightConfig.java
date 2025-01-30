package tektonikal.customblockhighlight.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import tektonikal.customblockhighlight.OutlineType;

import java.awt.*;


public class BlockHighlightConfig {

    public static GsonConfigInstance<BlockHighlightConfig> INSTANCE = GsonConfigInstance.createBuilder(BlockHighlightConfig.class).setPath(FabricLoader.getInstance().getConfigDir().resolve("blockhighlight-new.json")).build();
    public static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .serializeNulls()
            .registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
            .setPrettyPrinting()
            .create();
    //@formatter:off
    //outline stuff
    @ConfigEntry public boolean outlineEnabled = true;
        @ConfigEntry public Color lineCol = Color.decode("#000000");
        @ConfigEntry public Color lineCol2 = Color.decode("#FFFFFF");
        @ConfigEntry public int lineAlpha = 255;
        @ConfigEntry public boolean outlineRainbow = true;
        @ConfigEntry public OutlineType outlineType = OutlineType.AIR_EXPOSED;
        @ConfigEntry public int lineWidth = 3;
        @ConfigEntry public float lineExpand = 0;
        @ConfigEntry public boolean lineDepthTest = false;

    //fill stuffs
    @ConfigEntry public boolean fillEnabled = true;
        @ConfigEntry public Color fillCol = Color.decode("#000000");
        @ConfigEntry public Color fillCol2 = Color.decode("#FFFFFF");
        @ConfigEntry public int fillOpacity = 128;
        @ConfigEntry public boolean fillRainbow = false;
        @ConfigEntry public OutlineType fillType = OutlineType.AIR_EXPOSED;
        @ConfigEntry public float fillExpand = 0.001F;
        @ConfigEntry public boolean fillDepthTest = false;
        @ConfigEntry public boolean invert = false;
    //extras
    @ConfigEntry public boolean doEasing = true;
    @ConfigEntry public float easeSpeed = 10F;
    @ConfigEntry public boolean fadeIn = true;
    @ConfigEntry public boolean fadeOut = true;
    @ConfigEntry public float fadeSpeed = 8.0F;
    @ConfigEntry public int rainbowSpeed = 5;
    @ConfigEntry public int delay = 250;
    @ConfigEntry public boolean crystalHelper = true;
    @ConfigEntry public boolean connectedBlocks = true;
    //@formatter:on

    public static Screen getConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(INSTANCE, ((defaults, config, builder) -> builder
                .title(Text.literal("Custom Block Highlight"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Outline"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Enabled"))
                                .binding(true, () -> config.outlineEnabled, newVal -> config.outlineEnabled = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Color.class)
                                .name(Text.of("Color"))
                                .binding(new Color(0, 0, 0), () -> config.lineCol, newVal -> config.lineCol = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Color.class)
                                .name(Text.of("Color 2"))
                                .binding(new Color(255, 255, 255), () -> config.lineCol2, newVal -> config.lineCol2 = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Alpha"))
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1))
                                .binding(255, () -> config.lineAlpha, newVal -> config.lineAlpha = newVal)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Rainbow Outline"))
                                .binding(true, () -> config.outlineRainbow, newVal -> config.outlineRainbow = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(OutlineType.class)
                                .name(Text.of("Mode"))
                                .binding(OutlineType.AIR_EXPOSED, () -> config.outlineType, newVal -> config.outlineType = newVal)
                                .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Line Width"))
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1))
                                .binding(3, () -> config.lineWidth, newVal -> config.lineWidth = newVal)
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.of("Adjust Size By"))
                                .binding(0F, () -> config.lineExpand, newVal -> config.lineExpand = newVal)
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Depth Test"))
                                .binding(false, () -> config.lineDepthTest, newVal -> config.lineDepthTest = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.of("Fill"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Enabled"))
                                .controller(TickBoxControllerBuilder::create)
                                .binding(true, () -> config.fillEnabled, newVal -> config.fillEnabled = newVal)
                                .build())
                        .option(Option.createBuilder(Color.class)
                                .name(Text.of("Color"))
                                .binding(new Color(0, 0, 0), () -> config.fillCol, newVal -> config.fillCol = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Color.class)
                                .name(Text.of("Color 2"))
                                .binding(new Color(255, 255, 255), () -> config.fillCol2, newVal -> config.fillCol2 = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Alpha"))
                                .binding(128, () -> config.fillOpacity, newVal -> config.fillOpacity = newVal)
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Rainbow Fill"))
                                .binding(false, () -> config.fillRainbow, newVal -> config.fillRainbow = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(OutlineType.class)
                                .name(Text.of("Mode"))
                                .binding(OutlineType.AIR_EXPOSED, () -> config.fillType, newVal -> config.fillType = newVal)
                                .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.of("Adjust Size By"))
                                .binding(0.001F, () -> config.fillExpand, newVal -> config.fillExpand = newVal)
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-1F, 1F).step(0.05F))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Depth Test"))
                                .binding(false, () -> config.fillDepthTest, newVal -> config.fillDepthTest = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Invert"))
                                .binding(false, () -> config.invert, newVal -> config.invert = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.of("Extras"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Easing"))
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Ease movement"))
                                        .binding(true, () -> config.doEasing, newVal -> config.doEasing = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(float.class)
                                        .name(Text.of("Ease speed"))
                                        .description(OptionDescription.of(Text.of("How fast to animate the block. FPS independent !!")))
                                        .binding(10F, () -> config.easeSpeed, newVal -> config.easeSpeed = newVal)
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 15F).step(0.1F))
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Fade in"))
                                        .binding(true, () -> config.fadeIn, newVal -> config.fadeIn = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Fade out"))
                                        .binding(true, () -> config.fadeOut, newVal -> config.fadeOut = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(float.class)
                                        .name(Text.of("Fade speed"))
                                        .binding(8.0F, () -> config.fadeSpeed, newVal -> config.fadeSpeed = newVal)
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 15F).step(0.1F))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Rainbow / Chroma"))
                                .option(Option.createBuilder(int.class)
                                        .name(Text.of("Rainbow Speed"))
                                        .binding(5, () -> config.rainbowSpeed, newVal -> config.rainbowSpeed = newVal)
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1))
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.of("Rainbow Delay"))
                                        .binding(250, () -> config.delay, newVal -> config.delay = newVal)
                                        .controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(0, 750).step(1))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Miscellaneous"))
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Connected Outlines"))
                                        .description(OptionDescription.of(Text.of("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
                                        .binding(true, () -> config.connectedBlocks, newVal -> config.connectedBlocks = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Crystal Helper"))
                                        .description(OptionDescription.of(Text.of("highlights the block in red when you are looking at an obsidian block that crystals cannot be placed on.")))
                                        .binding(true, () -> config.crystalHelper, newVal -> config.crystalHelper = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
        )).generateScreen(parent);
    }
}