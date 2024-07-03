package tektonikal.customblockhighlight.config;

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

    public static GsonConfigInstance<BlockHighlightConfig> INSTANCE = GsonConfigInstance.createBuilder(BlockHighlightConfig.class).setPath(FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json")).build();
    //outline stuff
    @ConfigEntry
    public boolean outlineEnabled = true;
    @ConfigEntry
    public OutlineType type = OutlineType.AIR_EXPOSED;
    @ConfigEntry
    public Color lineCol = Color.decode("#000000");
    @ConfigEntry
    public Color lineCol2 = Color.decode("#FFFFFF");
    @ConfigEntry
    public int width = 2;
    @ConfigEntry
    public int lineAlpha = 128;
    @ConfigEntry
    public float expand = 0;
    @ConfigEntry
    public boolean connected = true;

    //fill stuffs
    @ConfigEntry
    public OutlineType fillType = OutlineType.ALL;
    @ConfigEntry
    public Color fillCol = Color.decode("#000000");
    @ConfigEntry
    public int fillOpacity = 128;
    @ConfigEntry
    public boolean fillEnabled = true;
    @ConfigEntry
    public float fillExpand = 0;
    //easings
    @ConfigEntry
    public boolean doEasing = true;
    @ConfigEntry
    public float easeSpeed = 1F;
    @ConfigEntry
    public boolean outlineRainbow = false;
    @ConfigEntry
    public boolean fillRainbow = false;
    @ConfigEntry
    public int rainbowSpeed = 5;
    @ConfigEntry
    public boolean crystalHelper = true;
//TODO
//    @ConfigEntry
//    public boolean blending;
    @ConfigEntry
    public boolean fillCulling = false;
    @ConfigEntry
    public boolean lineCulling = false;
    @ConfigEntry
    public float fadeSpeed = 8.0F;
    @ConfigEntry
    public boolean fadeOut = true;
    @ConfigEntry
    public boolean fadeIn = true;


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
                                .binding(new Color(0, 0, 0), () -> config.lineCol2, newVal -> config.lineCol2 = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Alpha"))
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1))
                                .binding(255, () -> config.lineAlpha, newVal -> config.lineAlpha = newVal)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Line Width"))
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1))
                                .binding(2, () -> config.width, newVal -> config.width = newVal)
                                .build())
                        .option(Option.createBuilder(OutlineType.class)
                                .name(Text.of("Mode"))
                                .binding(OutlineType.AIR_EXPOSED, () -> config.type, newVal -> config.type = newVal)
                                .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Culling"))
                                .binding(false, () -> config.lineCulling, newVal -> config.lineCulling = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.of("Adjust Size By"))
                                .binding(0F, () -> config.expand, newVal -> config.expand = newVal)
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-2F, 2F).step(0.1F))
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
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Alpha"))
                                .binding(128, () -> config.fillOpacity, newVal -> config.fillOpacity = newVal)
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 255).step(1))
                                .build())
                        .option(Option.createBuilder(OutlineType.class)
                                .name(Text.of("Mode"))
                                .binding(OutlineType.AIR_EXPOSED, () -> config.fillType, newVal -> config.fillType = newVal)
                                .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Culling"))
                                .binding(false, () -> config.fillCulling, newVal -> config.fillCulling = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.of("Adjust Size By"))
                                .binding(0.002F, () -> config.fillExpand, newVal -> config.fillExpand = newVal)
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-2F, 2F).step(0.1F))
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
                                        .binding(3.5F, () -> config.easeSpeed, newVal -> config.easeSpeed = newVal)
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0.01F, 10F).step(0.01F))
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
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0.01F, 10F).step(0.01F))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Rainbow / Chroma"))
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Rainbow Outline"))
                                        .binding(false, () -> config.outlineRainbow, newVal -> config.outlineRainbow = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Rainbow Fill"))
                                        .binding(false, () -> config.fillRainbow, newVal -> config.fillRainbow = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.of("Rainbow Speed"))
                                        .binding(10, () -> config.rainbowSpeed, newVal -> config.rainbowSpeed = newVal)
                                        .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1, 10).step(1))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Miscellaneous"))
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Connected outlines"))
                                        .description(OptionDescription.of(Text.of("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
                                        .binding(true, () -> config.connected, newVal -> config.connected = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.of("Crystal Helper"))
                                        .description(OptionDescription.of(Text.of("highlights the block in red when you are looking at an obsidian block that crystals cannot be placed on.")))
                                        .binding(true, () -> config.crystalHelper, newVal -> config.crystalHelper = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
//                                .option(Option.createBuilder(boolean.class)
//                                        .name(Text.of("Blending"))
//                                        .description(OptionDescription.of(Text.of("Whether to blend overlaid colors.")))
//                                        .controller(TickBoxControllerBuilder::create)
//                                        .binding(true, () -> config.blending, newVal -> config.blending = newVal)
//                                        .build())
                                .build())
                        .build())
        )).generateScreen(parent);
    }
}
