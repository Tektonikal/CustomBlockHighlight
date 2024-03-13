package tektonikal.blockhighlight.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import tektonikal.blockhighlight.Easing;
import tektonikal.blockhighlight.OutlineType;

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
    public Color fillCol1 = Color.decode("#000000");
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
    public Easing easing = Easing.easeInOutCirc;
    @ConfigEntry
    public float easeSpeed = 0.4F;
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
                            .binding(new Color(0,0,0), () -> config.lineCol, newVal -> config.lineCol = newVal)
                            .controller(ColorControllerBuilder::create)
                            .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Alpha"))
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(0, 255).step(1))
                                .binding(255, () -> config.lineAlpha, newVal -> config.lineAlpha = newVal)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Line width"))
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1,5).step(1))
                                .binding(2, () -> config.width, newVal -> config.width = newVal)
                                .build())
                        .option(Option.createBuilder(OutlineType.class)
                                .name(Text.of("Mode"))
                                .binding(OutlineType.AIR_EXPOSED, () -> config.type, newVal -> config.type = newVal)
                                .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.of("Adjust size by"))
                                .binding(0F, () -> config.expand, newVal -> config.expand = newVal)
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-2F, 2F).step(0.1F))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Connected outlines"))
                                .description(OptionDescription.of(Text.of("This applies to both the fill and outline. Maybe I'll change it later, who knows?")))
                                .binding(true, () -> config.connected, newVal -> config.connected = newVal)
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
                                .binding(new Color(0,0,0), () -> config.fillCol1, newVal -> config.fillCol1 = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.of("Alpha"))
                                .binding(128, () ->config.fillOpacity, newVal -> config.fillOpacity = newVal)
                                .controller(integerOption -> IntegerSliderControllerBuilder.create(integerOption).range(1,255).step(1))
                                .build())
                        .option(Option.createBuilder(OutlineType.class)
                                .name(Text.of("Mode"))
                                .binding(OutlineType.CONCEALED, () -> config.fillType, newVal -> config.fillType = newVal)
                                .controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(OutlineType.class))
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.of("Adjust size by"))
                                .binding(0F, () -> config.fillExpand, newVal -> config.fillExpand = newVal)
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-2F, 2F).step(0.1F))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.of("Easing"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.of("Ease movement"))
                                .binding(true, () -> config.doEasing, newVal -> config.doEasing = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.of("Ease speed"))
                                .description(OptionDescription.of(Text.of("How fast to animate the block. Due to jank the speed of the animation depends on your game's current FPS, so it recommended to cap your FPS and mess around with this setting if it looks weird.")))
                                .binding(0.25F, () -> config.easeSpeed, newVal -> config.easeSpeed = newVal)
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0.01F, 0.99F).step(0.01F))
                                .build())
                        .option(Option.createBuilder(Easing.class)
                                .name(Text.of("Easing mode"))
                                .binding(Easing.easeInOutExpo, () -> config.easing, newVal -> config.easing = newVal)
                                .controller(easingOption -> EnumControllerBuilder.create(easingOption).enumClass(Easing.class))
                                .build())
                        .build())
        )).generateScreen(parent);
    }
}
