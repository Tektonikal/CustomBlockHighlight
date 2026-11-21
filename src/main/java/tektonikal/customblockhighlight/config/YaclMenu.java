package tektonikal.customblockhighlight.config;

//? if >1.8.9 {
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.ColorSetting;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.LineConfig;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.RainbowSettings;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.Range;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
import tektonikal.customblockhighlight.util.BlendMode;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.FaceMode;
import tektonikal.customblockhighlight.util.OutlineFit;
import tektonikal.customblockhighlight.util.OutlineSides;
import tektonikal.customblockhighlight.util.ScreenOutline;
import tektonikal.customblockhighlight.util.ShapeStyle;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;

@SuppressWarnings("NoTranslation")
public final class YaclMenu {
	private YaclMenu() {
	}

	public static final ValueFormatter<Float> BLOCKS_FORMATTER_THREE_PLACES = val ->
			Component.translatable(Math.abs(val) == 1 ? "cbh.config.unit.block" : "cbh.config.unit.blocks",
					String.format(Locale.ROOT, "%.3f", val).replace(".000", ""));

	static <T> StateManager<T> createInstant(@NotNull T def, @NotNull Supplier<@NotNull T> getter, @NotNull Consumer<@NotNull T> setter) {
		return new EvilInstantStateManager<>(Binding.generic(def, getter, setter));
	}

	private static final ValueFormatter<Float> PERCENT = value -> Component.literal(String.format(Locale.ROOT, "%d", Math.round(value * 100)) + "%");
	private static final ValueFormatter<Float> TIMES = value -> Component.literal(String.format(Locale.ROOT, "%.1fx", value));

	private static final String DASH = "- ", SUB_DASH = "  - ";

	private static Component named(String indent, String key) {
		return Component.literal(indent).append(Component.translatable(key));
	}

	public static final class ColorOptions {
		public final Option<Color> col, col2;
		public final Option<Integer> alpha, rainbowDelay;
		public final Option<Boolean> rainbow;
		public final Option<Float> rainbowSpeed, saturation, brightness;

		ColorOptions(ColorSetting def, Supplier<ColorSetting> active, int minAlpha) {
			col = Option.<Color>createBuilder()
					.name(named(DASH, "cbh.config.primary"))
					.stateManager(createInstant(def.col1, () -> active.get().col1, newVal -> {
						ColorSetting setting = active.get();
						if (newVal.equals(setting.col1)) return;
						setting.col1 = newVal;
						setting.gradient = null;
					}))
					.controller(ColorControllerBuilder::create)
					.build();
			col2 = Option.<Color>createBuilder()
					.name(named(DASH, "cbh.config.secondary"))
					.stateManager(createInstant(def.col2, () -> active.get().col2, newVal -> {
						ColorSetting setting = active.get();
						if (newVal.equals(setting.col2)) return;
						setting.col2 = newVal;
						setting.gradient = null;
					}))
					.controller(ColorControllerBuilder::create)
					.build();
			alpha = Option.<Integer>createBuilder()
					.name(named(DASH, "cbh.config.opacity"))
					.controller(option -> IntegerSliderControllerBuilder.create(option).range(minAlpha, 255).step(1).formatValue(value -> Component.literal(String.format(Locale.ROOT, "%d", Math.round(value * 100 / 255F)) + "%")))
					.stateManager(createInstant(def.alpha, () -> active.get().alpha, newVal -> active.get().alpha = newVal))
					.build();
			rainbow = Option.<Boolean>createBuilder()
					.name(named(DASH, "cbh.config.rainbow"))
					.stateManager(createInstant(def.rainbowSettings.enabled, () -> active.get().rainbowSettings.enabled, newVal -> {
						ColorSetting setting = active.get();
						if (newVal == setting.rainbowSettings.enabled) return;
						setting.rainbowSettings.enabled = newVal;
						setting.gradient = null;
					}))
					.controller(TickBoxControllerBuilder::create)
					.addListener((option, event) -> refreshAvailability())
					.build();
			rainbowSpeed = Option.<Float>createBuilder()
					.name(named(SUB_DASH, "cbh.config.speed"))
					.stateManager(createInstant(def.rainbowSettings.speed, () -> active.get().rainbowSettings.speed, newVal -> active.get().rainbowSettings.speed = newVal))
					.controller(option -> slider(option, RAINBOW_SPEED).formatValue(TIMES))
					.build();
			rainbowDelay = Option.<Integer>createBuilder()
					.name(named(SUB_DASH, "cbh.config.delay"))
					.stateManager(createInstant(def.rainbowSettings.delay, () -> active.get().rainbowSettings.delay, newVal -> active.get().rainbowSettings.delay = newVal))
					.description(OptionDescription.of(Component.translatable("cbh.config.delay.description")))
					.controller(option -> IntegerSliderControllerBuilder.create(option).range((int) RAINBOW_DELAY.min(), (int) RAINBOW_DELAY.max()).step(1).formatValue(value -> Component.translatable("cbh.config.unit.ms", value)))
					.build();
			saturation = Option.<Float>createBuilder()
					.name(named(SUB_DASH, "cbh.config.saturation"))
					.stateManager(createInstant(def.rainbowSettings.saturation, () -> active.get().rainbowSettings.saturation, newVal -> active.get().rainbowSettings.saturation = newVal))
					.controller(option -> slider(option, UNIT).formatValue(PERCENT))
					.build();
			brightness = Option.<Float>createBuilder()
					.name(named(SUB_DASH, "cbh.config.brightness"))
					.stateManager(createInstant(def.rainbowSettings.brightness, () -> active.get().rainbowSettings.brightness, newVal -> active.get().rainbowSettings.brightness = newVal))
					.controller(option -> slider(option, UNIT).formatValue(PERCENT))
					.build();
		}

		List<Option<?>> all() {
			return List.of(col, col2, alpha, rainbow, rainbowSpeed, rainbowDelay, saturation, brightness);
		}

		OptionGroup group() {
			return OptionGroup.createBuilder().name(Component.translatable("cbh.config.color")).options(all()).build();
		}
	}

	public static final class ScreenOptions {
		public final Option<OutlineSides> sides;
		public final Option<OutlineFit> fit;
		public final Option<Boolean> enabled;
		public final Option<Boolean> keepOnScreen;
		public final Option<Float> rotation;

		ScreenOptions(ScreenOutline def, Supplier<ScreenOutline> active, boolean keepOnScreenOption) {
			enabled = Option.<Boolean>createBuilder()
					.name(Component.translatable("cbh.config.screen_space_enabled"))
					.description(OptionDescription.of(Component.translatable("cbh.config.screen_space_enabled.description")))
					.stateManager(createInstant(def.enabled, () -> active.get().enabled, newVal -> active.get().enabled = newVal))
					.controller(TickBoxControllerBuilder::create)
					.addListener((option, event) -> refreshAvailability())
					.build();
			sides = Option.<OutlineSides>createBuilder()
					.name(Component.translatable("cbh.config.screen_sides"))
					.description(OptionDescription.of(Component.translatable("cbh.config.screen_sides.description")))
					.stateManager(createInstant(OutlineSides.of(def.sides), () -> OutlineSides.of(active.get().sides), newVal -> active.get().sides = newVal.count))
					.controller(option -> EnumControllerBuilder.create(option).enumClass(OutlineSides.class))
					.addListener((option, event) -> refreshAvailability())
					.build();
			fit = Option.<OutlineFit>createBuilder()
					.name(Component.translatable("cbh.config.screen_fit"))
					.description(OptionDescription.of(Component.translatable("cbh.config.screen_fit.description")))
					.stateManager(createInstant(def.fit, () -> active.get().fit, newVal -> active.get().fit = newVal))
					.controller(option -> EnumControllerBuilder.create(option).enumClass(OutlineFit.class))
					.build();
			rotation = Option.<Float>createBuilder()
					.name(Component.translatable("cbh.config.screen_rotation"))
					.stateManager(createInstant(def.rotation, () -> active.get().rotation, newVal -> active.get().rotation = newVal))
					.controller(option -> slider(option, SCREEN_ROTATION).formatValue(value -> Component.literal(String.format(Locale.ROOT, "%.0f°", value))))
					.build();
			keepOnScreen = !keepOnScreenOption ? null : Option.<Boolean>createBuilder()
					.name(Component.translatable("cbh.config.keep_on_screen"))
					.description(OptionDescription.of(Component.translatable("cbh.config.keep_on_screen.description")))
					.stateManager(createInstant(def.keepOnScreen, () -> active.get().keepOnScreen, newVal -> active.get().keepOnScreen = newVal))
					.controller(TickBoxControllerBuilder::create)
					.build();
		}

		List<Option<?>> all() {
			return keepOnScreen == null ? List.of(enabled, sides, fit, rotation) : List.of(enabled, sides, fit, rotation, keepOnScreen);
		}

		OptionGroup group() {
			return OptionGroup.createBuilder().name(Component.translatable("cbh.config.screen_space")).options(all()).build();
		}

		void availableFor(boolean drawn, ScreenOutline outline) {
			available(drawn, enabled);
			available(drawn && outline.enabled, sides);
			if (keepOnScreen != null) available(drawn && outline.enabled, keepOnScreen);
			available(drawn && outline.enabled && !outline.exact(), fit, rotation);
		}
	}

	public static final class LineOptions {
		public final Option<Boolean> enabled;
		public final ColorOptions color;
		public final Option<Float> expand, expandPercent, width, cutFromCorner, cutFromCenter, outerThicknessMult, innerThicknessMult;
		public final Option<FaceMode> faceMode;
		public final Option<DepthTestMode> depthTest;
		public final Option<BlendMode> blend;
		public final Option<ShapeStyle> shapeStyle;
		public final ScreenOptions screen;

		LineOptions(Function<BlockHighlightConfig, LineConfig> layer, String enabledDescription, Range widthRange, Range cutRange) {
			LineConfig def = layer.apply(new BlockHighlightConfig());
			Supplier<LineConfig> active = () -> layer.apply(ACTIVE_INSTANCE);
			enabled = Option.<Boolean>createBuilder()
					.name(Component.translatable("cbh.config.enabled"))
					.description(OptionDescription.of(Component.translatable(enabledDescription)))
					.stateManager(createInstant(def.enabled, () -> active.get().enabled, newVal -> active.get().enabled = newVal))
					.controller(TickBoxControllerBuilder::create)
					.addListener((option, event) -> refreshAvailability())
					.build();
			color = new ColorOptions(def.color, () -> active.get().color, 0);
			screen = new ScreenOptions(def.screenOutline, () -> active.get().screenOutline, true);
			faceMode = Option.<FaceMode>createBuilder()
					.name(named(SUB_DASH, "cbh.config.mode"))
					.description(OptionDescription.of(Component.translatable("cbh.config.mode.description.1"),
							Component.translatable("cbh.config.mode.description.2"),
							Component.translatable("cbh.config.mode.description.3"),
							Component.translatable("cbh.config.mode.description.4"),
							Component.translatable("cbh.config.mode.description.5")
					))
					.stateManager(createInstant(def.outlineType, () -> active.get().outlineType, newVal -> active.get().outlineType = newVal))
					.controller(option -> EnumControllerBuilder.create(option).enumClass(FaceMode.class))
					.build();
			depthTest = Option.<DepthTestMode>createBuilder()
					.name(named(DASH, "cbh.config.depthTest"))
					.description(OptionDescription.of(Component.translatable("cbh.config.depthTest.description")))
					.stateManager(createInstant(def.lineDepthTest, () -> active.get().lineDepthTest, newVal -> active.get().lineDepthTest = newVal))
					.controller(option -> EnumControllerBuilder.create(option).enumClass(DepthTestMode.class))
					.build();
			blend = Option.<BlendMode>createBuilder()
					.name(named(DASH, "cbh.config.blendMode"))
					.description(OptionDescription.of(Component.translatable("cbh.config.blendMode.description")))
					.stateManager(createInstant(def.lineBlend, () -> active.get().lineBlend, newVal -> active.get().lineBlend = newVal))
					.controller(option -> EnumControllerBuilder.create(option).enumClass(BlendMode.class))
					.build();
			expand = Option.<Float>createBuilder()
					.name(named(DASH, "cbh.config.expand_blocks"))
					.stateManager(createInstant(def.lineExpandBlocks, () -> active.get().lineExpandBlocks, newVal -> active.get().lineExpandBlocks = newVal))
					.controller(option -> slider(option, EXPAND_BLOCKS).formatValue(BLOCKS_FORMATTER_THREE_PLACES))
					.build();
			expandPercent = Option.<Float>createBuilder()
					.name(named(DASH, "cbh.config.expand_percent"))
					.stateManager(createInstant(def.lineExpandPercentage, () -> active.get().lineExpandPercentage, newVal -> active.get().lineExpandPercentage = newVal))
					.controller(option -> slider(option, EXPAND_PERCENT).formatValue(PERCENT))
					.build();
			shapeStyle = Option.<ShapeStyle>createBuilder()
					.name(named(DASH, "cbh.enum.shape_style"))
					.description(OptionDescription.of(Component.translatable("cbh.enum.shape_style.description")))
					.stateManager(createInstant(def.shapeStyle, () -> active.get().shapeStyle, newVal -> active.get().shapeStyle = newVal))
					.controller(option -> EnumControllerBuilder.create(option).enumClass(ShapeStyle.class))
					.addListener((option, event) -> refreshAvailability())
					.build();
			width = Option.<Float>createBuilder()
					.name(named(DASH, "cbh.config.lineWidth"))
					.controller(option -> slider(option, widthRange).formatValue(value -> Component.translatable("cbh.config.unit.px", String.format(Locale.ROOT, "%.1f", value))))
					.stateManager(createInstant(def.lineWidth, () -> active.get().lineWidth, newVal -> active.get().lineWidth = newVal))
					.build();
			cutFromCorner = Option.<Float>createBuilder()
					.name(named(DASH, "cbh.config.cutFromCorner"))
					.stateManager(createInstant(def.cutFromCorner, () -> active.get().cutFromCorner, newVal -> active.get().cutFromCorner = newVal))
					.controller(option -> slider(option, cutRange).formatValue(PERCENT))
					.build();
			cutFromCenter = Option.<Float>createBuilder()
					.name(named(DASH, "cbh.config.cutFromCenter"))
					.stateManager(createInstant(def.cutFromCenter, () -> active.get().cutFromCenter, newVal -> active.get().cutFromCenter = newVal))
					.controller(option -> slider(option, cutRange).formatValue(PERCENT))
					.build();
			outerThicknessMult = Option.<Float>createBuilder()
					.name(named(SUB_DASH, "cbh.config.outer_thickness_multiplier"))
					.description(OptionDescription.of(Component.translatable("cbh.config.thickness_multiplier_desc")))
					.stateManager(createInstant(def.outerThicknessMult, () -> active.get().outerThicknessMult, newVal -> active.get().outerThicknessMult = newVal))
					.controller(option -> slider(option, THICKNESS_MULT).formatValue(PERCENT))
					.build();
			innerThicknessMult = Option.<Float>createBuilder()
					.name(named(SUB_DASH, "cbh.config.inner_thickness_multiplier"))
					.description(OptionDescription.of(Component.translatable("cbh.config.thickness_multiplier_desc")))
					.stateManager(createInstant(def.innerThicknessMult, () -> active.get().innerThicknessMult, newVal -> active.get().innerThicknessMult = newVal))
					.controller(option -> slider(option, THICKNESS_MULT).formatValue(PERCENT))
					.build();
			clampCuts(cutFromCorner, cutFromCenter);
			clampCuts(cutFromCenter, cutFromCorner);
		}

		private static boolean clampingCuts;

		private static void clampCuts(Option<Float> changed, Option<Float> other) {
			changed.addEventListener((option, event) -> {
				if (event != OptionEventListener.Event.STATE_CHANGE || pushingValues || clampingCuts) return;
				float limited = limitedCut(option.pendingValue(), other.pendingValue());
				if (limited == other.pendingValue()) return;
				clampingCuts = true;
				try {
					other.requestSet(limited);
				} finally {
					clampingCuts = false;
				}
			});
		}

		List<Option<?>> all() {
			List<Option<?>> all = new ArrayList<>(List.of(enabled, expand, expandPercent, width, cutFromCorner, cutFromCenter, outerThicknessMult, innerThicknessMult, faceMode, depthTest, blend, shapeStyle));
			all.addAll(color.all());
			all.addAll(screen.all());
			return all;
		}
	}

	private static final BlockHighlightConfig DEFAULTS = new BlockHighlightConfig();

	private static <T> StateManager<T> instant(Function<BlockHighlightConfig, T> getter, BiConsumer<BlockHighlightConfig, T> setter) {
		return createInstant(getter.apply(DEFAULTS), () -> getter.apply(ACTIVE_INSTANCE), value -> setter.accept(ACTIVE_INSTANCE, value));
	}

	private static Option<Boolean> toggle(Component name, String description, boolean refreshes, Function<BlockHighlightConfig, Boolean> getter, BiConsumer<BlockHighlightConfig, Boolean> setter) {
		Option.Builder<Boolean> builder = Option.<Boolean>createBuilder()
				.name(name)
				.stateManager(instant(getter, setter))
				.controller(TickBoxControllerBuilder::create);
		if (description != null) builder.description(OptionDescription.of(Component.translatable(description)));
		if (refreshes) builder.addListener((option, event) -> refreshAvailability());
		return builder.build();
	}

	private static FloatSliderControllerBuilder slider(Option<Float> option, Range range) {
		return FloatSliderControllerBuilder.create(option).range(range.min(), range.max()).step(range.step());
	}

	private static Option<Float> speed(Range range,Function<BlockHighlightConfig, Float> getter, BiConsumer<BlockHighlightConfig, Float> setter) {
		return Option.<Float>createBuilder()
				.name(named(SUB_DASH, "cbh.config.speed"))
				.stateManager(instant(getter, setter))
				.controller(option -> slider(option, range).formatValue(TIMES))
				.build();
	}

	static final class Options {
		public final LineOptions PRIMARY_OPTIONS = new LineOptions(c -> c.primary, "cbh.config.main_outline_toggle_desc", PRIMARY_LINE_WIDTH, PRIMARY_CUT);
		public final LineOptions SECONDARY_OPTIONS = new LineOptions(c -> c.secondary, "cbh.config.non_primary_layer_toggle_desc", LAYER_LINE_WIDTH, LAYER_CUT);
		public final LineOptions TERTIARY_OPTIONS = new LineOptions(c -> c.tertiary, "cbh.config.non_primary_layer_toggle_desc", LAYER_LINE_WIDTH, LAYER_CUT);
		public final List<LineOptions> LINE_OPTIONS = List.of(PRIMARY_OPTIONS, SECONDARY_OPTIONS, TERTIARY_OPTIONS);
		public final Option<Boolean> o_fillEnabled = toggle(Component.translatable("cbh.config.enabled"), null, true, c -> c.fillEnabled, (c, v) -> c.fillEnabled = v);
		public final ColorOptions FILL_COLOR_OPTIONS = new ColorOptions(DEFAULTS.fillCol, () -> ACTIVE_INSTANCE.fillCol, 1);
		public final ScreenOptions FILL_SCREEN_OPTIONS = new ScreenOptions(DEFAULTS.fillScreenOutline, () -> ACTIVE_INSTANCE.fillScreenOutline, false);

		public final Option<FaceMode> o_fillType = Option.<FaceMode>createBuilder()
				.name(named(DASH, "cbh.config.mode"))
				.description(OptionDescription.of(Component.translatable("cbh.config.mode.description.1"),
						Component.translatable("cbh.config.mode.description.2"),
						Component.translatable("cbh.config.mode.description.3"),
						Component.translatable("cbh.config.mode.description.4"),
						Component.translatable("cbh.config.mode.description.5")
				))
				.stateManager(instant(c -> c.fillType, (c, v) -> c.fillType = v))
				.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(FaceMode.class))
				.build();
		public final Option<ShapeStyle> o_fillShape = Option.<ShapeStyle>createBuilder()
				.name(named(DASH, "cbh.enum.shape_style"))
				.description(OptionDescription.of(Component.translatable("cbh.config.fill_shape.description")))
				.stateManager(instant(c -> c.fillShape, (c, v) -> c.fillShape = v))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(ShapeStyle.class))
				.addListener((option, event) -> refreshAvailability())
				.build();
		public final Option<Boolean> o_unifiedQuads = toggle(named(SUB_DASH, "cbh.config.unified_quads"), "cbh.config.unified_quads.description", false, c -> c.unifiedQuads, (c, v) -> c.unifiedQuads = v);
		public final Option<DepthTestMode> o_fillDepthTest = Option.<DepthTestMode>createBuilder()
				.name(named(DASH, "cbh.config.depthTest"))
				.description(OptionDescription.of(Component.translatable("cbh.config.depthTest.description")))
				.stateManager(instant(c -> c.fillDepthTest, (c, v) -> c.fillDepthTest = v))
				.controller(outlineTypeOption -> EnumControllerBuilder.create(outlineTypeOption).enumClass(DepthTestMode.class))
				.build();
		public final Option<BlendMode> o_fillBlend = Option.<BlendMode>createBuilder()
				.name(named(DASH, "cbh.config.blendMode"))
				.description(OptionDescription.of(Component.translatable("cbh.config.blendMode.description")))
				.stateManager(instant(c -> c.fillBlend, (c, v) -> c.fillBlend = v))
				.controller(blendOption -> EnumControllerBuilder.create(blendOption).enumClass(BlendMode.class))
				.build();
		public final Option<Float> o_fillExpandBlocks = Option.<Float>createBuilder()
				.name(named(DASH, "cbh.config.expand_blocks"))
				.stateManager(instant(c -> c.fillExpandBlocks, (c, v) -> c.fillExpandBlocks = v))
				.controller(floatOption -> slider(floatOption, EXPAND_BLOCKS).formatValue(BLOCKS_FORMATTER_THREE_PLACES))
				.build();
		public final Option<Float> o_fillExpandPercent = Option.<Float>createBuilder()
				.name(named(DASH, "cbh.config.expand_percent"))
				.stateManager(instant(c -> c.fillExpandPercent, (c, v) -> c.fillExpandPercent = v))
				.controller(floatOption -> slider(floatOption, EXPAND_PERCENT).formatValue(PERCENT))
				.build();
		public final Option<Boolean> o_doEasing = toggle(named(DASH, "cbh.config.enabled"), "cbh.config.easing.description", true, c -> c.doEasing, (c, v) -> c.doEasing = v);
		public final Option<Float> o_easeSpeed = speed(EASE_SPEED, c -> c.easeSpeed, (c, v) -> c.easeSpeed = v);
		public final Option<Boolean> o_snapWhenFadedOut = toggle(named(DASH, "cbh.config.snap_when_faded_out"), "cbh.config.snap_when_faded_out.description", false, c -> c.snapWhenFadedOut, (c, v) -> c.snapWhenFadedOut = v);
		public final Option<Boolean> o_restyleOtherOutlines = toggle(named(DASH, "cbh.config.restyle_other_outlines"), "cbh.config.restyle_other_outlines.description", false, c -> c.restyleOtherOutlines, (c, v) -> c.restyleOtherOutlines = v);
		public final Option<Boolean> o_fadeIn = toggle(named(DASH, "cbh.config.in"), null, true, c -> c.fadeIn, (c, v) -> c.fadeIn = v);
		public final Option<Float> o_fadeInSpeed = speed(ANIMATION_SPEED, c -> c.fadeInSpeed, (c, v) -> c.fadeInSpeed = v);
		public final Option<Boolean> o_fadeOut = toggle(named(DASH, "cbh.config.out"), null, true, c -> c.fadeOut, (c, v) -> c.fadeOut = v);
		public final Option<Float> o_fadeOutSpeed = speed(ANIMATION_SPEED, c -> c.fadeOutSpeed, (c, v) -> c.fadeOutSpeed = v);
		public final Option<Boolean> o_scale = toggle(named(DASH, "cbh.config.in"), null, true, c -> c.scale, (c, v) -> c.scale = v);
		public final Option<Float> o_scaleSpeed = speed(ANIMATION_SPEED, c -> c.scaleSpeed, (c, v) -> c.scaleSpeed = v);
		public final Option<Boolean> o_scaleOut = toggle(named(DASH, "cbh.config.out"), null, true, c -> c.scaleOut, (c, v) -> c.scaleOut = v);
		public final Option<Float> o_scaleOutSpeed = speed(ANIMATION_SPEED, c -> c.scaleOutSpeed, (c, v) -> c.scaleOutSpeed = v);
		public final Option<Boolean> o_animateLineThickness = toggle(named(DASH, "cbh.config.in"), null, true, c -> c.animateLineThickness, (c, v) -> c.animateLineThickness = v);
		public final Option<Float> o_lineThicknessSpeed = speed(ANIMATION_SPEED, c -> c.lineThicknessAnimationSpeed, (c, v) -> c.lineThicknessAnimationSpeed = v);
		public final Option<Boolean> o_lineThicknessOut = toggle(named(DASH, "cbh.config.out"), null, true, c -> c.lineThicknessOut, (c, v) -> c.lineThicknessOut = v);
		public final Option<Float> o_lineThicknessOutSpeed = speed(ANIMATION_SPEED, c -> c.lineThicknessOutSpeed, (c, v) -> c.lineThicknessOutSpeed = v);
		public final Option<Boolean> o_animateLineCuts = toggle(named(DASH, "cbh.config.in"), null, true, c -> c.animateLineCuts, (c, v) -> c.animateLineCuts = v);
		public final Option<Float> o_lineCutSpeed = speed(ANIMATION_SPEED, c -> c.lineCutSpeed, (c, v) -> c.lineCutSpeed = v);
		public final Option<Boolean> o_lineCutOut = toggle(named(DASH, "cbh.config.out"), null, true, c -> c.lineCutOut, (c, v) -> c.lineCutOut = v);
		public final Option<Float> o_lineCutOutSpeed = speed(ANIMATION_SPEED, c -> c.lineCutOutSpeed, (c, v) -> c.lineCutOutSpeed = v);
		public final Option<Boolean> o_connectedBlocks = toggle(named(DASH, "cbh.config.connected_outlines"), "cbh.config.connected_outlines.description", false, c -> c.connectedBlocks, (c, v) -> c.connectedBlocks = v);
		public final Option<Boolean> o_updateWhenUnfocused = toggle(named(DASH, "cbh.config.update_when_unfocused"), "cbh.config.update_when_unfocused.description", false, c -> c.updateWhenUnfocused, (c, v) -> c.updateWhenUnfocused = v);
		public final Option<Boolean> o_crystalHelper = toggle(named(DASH, "cbh.config.enabled"), "cbh.config.crystal_helper.description", true, c -> c.crystalHelper, (c, v) -> c.crystalHelper = v);
		public final Option<Boolean> o_crystalHelperOnlyWhenHolding = toggle(named(SUB_DASH, "cbh.config.crystal_helper.only_when_holding"), "cbh.config.crystal_helper.only_when_holding.description", false, c -> c.crystalHelperOnlyWhenHolding, (c, v) -> c.crystalHelperOnlyWhenHolding = v);
		public final Option<Color> o_crystalHelperLineColor = Option.<Color>createBuilder()
				.name(named(SUB_DASH, "cbh.config.crystal_helper.line_color"))
				.controller(ColorControllerBuilder::create)
				.stateManager(instant(c -> c.crystalHelperLineColor, (c, v) -> c.crystalHelperLineColor = v))
				.build();
		public final Option<Color> o_crystalHelperFillColor = Option.<Color>createBuilder()
				.name(named(SUB_DASH, "cbh.config.crystal_helper.fill_color"))
				.controller(ColorControllerBuilder::create)
				.stateManager(instant(c -> c.crystalHelperFillColor, (c, v) -> c.crystalHelperFillColor = v))
				.build();
		public final Option<Boolean> o_allowEntities = toggle(named(DASH, "cbh.config.select_entities"), null, false, c -> c.allowEntities, (c, v) -> c.allowEntities = v);
		public final Option<Boolean> o_allowLiquids = toggle(named(DASH, "cbh.config.select_fluids"), "cbh.config.select_fluids.description", true, c -> c.allowLiquids, (c, v) -> c.allowLiquids = v);
		public final Option<Boolean> o_highlightBarriers = toggle(named(DASH, "cbh.config.highlight_barriers"), "cbh.config.highlight_barriers.description", false, c -> c.highlightBarriers, (c, v) -> c.highlightBarriers = v);
		public final Option<Boolean> o_whenHoldingAppropriate = toggle(named(SUB_DASH, "cbh.config.holding_appropriate_item"), "cbh.config.holding_appropriate_item_desc", false, c -> c.onlyWhenHoldingAppropriate, (c, v) -> c.onlyWhenHoldingAppropriate = v);
		public final Option<Boolean> o_onlySourceBlocks = toggle(named(SUB_DASH, "cbh.config.only_source_blocks"), null, false, c -> c.onlySourceBlocks, (c, v) -> c.onlySourceBlocks = v);
		public final Option<Boolean> o_rotations = toggle(named(DASH, "cbh.config.rotations"), "cbh.config.rotations.description", true, c -> c.rotations, (c, v) -> c.rotations = v);
		public final Option<Float> o_rotationSpeed = speed(ROTATION_SPEED, c -> c.rotationSpeed, (c, v) -> c.rotationSpeed = v);
		public final Option<Boolean> o_showWhenNoHud = toggle(named(DASH, "cbh.config.show_when_no_hud"), null, false, c -> c.showWhenNoHud, (c, v) -> c.showWhenNoHud = v);
		public final Option<Boolean> o_showWhenNoInteraction = toggle(named(DASH, "cbh.config.show_when_no_interaction"), "cbh.config.show_when_no_interaction.desc", false, c -> c.showWhenNoInteraction, (c, v) -> c.showWhenNoInteraction = v);
		public final Option<Boolean> o_globalModToggle = toggle(named(DASH, "cbh.config.global_toggle"), null, true, c -> c.enableModRendering, (c, v) -> c.enableModRendering = v);
		public final Option<Boolean> o_vanillaOutline = toggle(named(DASH, "cbh.config.show_vanilla_outline"), null, false, c -> c.drawVanillaOutline, (c, v) -> c.drawVanillaOutline = v);

		YetAnotherConfigLib layout() {
			return YetAnotherConfigLib.createBuilder()
					.title(Component.translatable("cbh.config.title"))
					.category(ConfigCategory.createBuilder()
							.name(Component.translatable("cbh.config.outline"))
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.general"))
									.option(o_globalModToggle)
									.option(o_vanillaOutline)
									.build())
							.groups(lineGroups("cbh.config.outline", PRIMARY_OPTIONS))
							.groups(lineGroups("cbh.config.secondary_layer", SECONDARY_OPTIONS))
							.groups(lineGroups("cbh.config.tertiary_layer", TERTIARY_OPTIONS))
							.build())
					.category(ConfigCategory.createBuilder()
							.name(Component.translatable("cbh.config.fill"))
							.option(o_fillEnabled)
							.group(FILL_COLOR_OPTIONS.group())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.scaling"))
									.option(o_fillExpandBlocks)
									.option(o_fillExpandPercent)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.misc"))
									.option(o_fillShape)
									.option(o_fillType)
									.option(o_fillDepthTest)
									.option(o_fillBlend)
									.build())
							.group(FILL_SCREEN_OPTIONS.group())
							.build())
					.category(ConfigCategory.createBuilder()
							.name(Component.translatable("cbh.config.visuals"))
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.easing"))
									.option(o_doEasing)
									.option(o_easeSpeed)
									.option(o_unifiedQuads)
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
									.option(o_scaleOut)
									.option(o_scaleOutSpeed)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.line_thickness"))
									.option(o_animateLineThickness)
									.option(o_lineThicknessSpeed)
									.option(o_lineThicknessOut)
									.option(o_lineThicknessOutSpeed)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.line_cuts"))
									.option(o_animateLineCuts)
									.option(o_lineCutSpeed)
									.option(o_lineCutOut)
									.option(o_lineCutOutSpeed)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.rotation"))
									.option(o_rotations)
									.option(o_rotationSpeed)
									.build())
							.build())
					.category(ConfigCategory.createBuilder()
							.name(Component.translatable("cbh.config.behaviour"))
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.render_conditions"))
									.option(o_allowEntities)
									.option(o_allowLiquids)
									.option(o_onlySourceBlocks)
									.option(o_whenHoldingAppropriate)
									.option(o_highlightBarriers)
									.option(o_showWhenNoHud)
									.option(o_showWhenNoInteraction)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.movement"))
									.option(o_updateWhenUnfocused)
									.option(o_snapWhenFadedOut)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.crystal_helper"))
									.option(o_crystalHelper)
									.option(o_crystalHelperOnlyWhenHolding)
									.option(o_crystalHelperLineColor)
									.option(o_crystalHelperFillColor)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config.misc"))
									.option(o_connectedBlocks)
									//? if >=26.2
									.option(o_restyleOtherOutlines)
									.build())
							.group(OptionGroup.createBuilder()
									.name(Component.translatable("cbh.config"))
									.option(ButtonOption.createBuilder()
											.name(named(DASH, "cbh.config.copy_to_clipboard"))
											.action((screen, option) -> {
												ConfigManager.save();
												Minecraft.getInstance().keyboardHandler.setClipboard(ConfigManager.GSON.toJson(ACTIVE_INSTANCE));
											})
											.text(Component.translatable("cbh.config.copy"))
											.build())
									.option(ButtonOption.createBuilder()
											.name(named(DASH, "cbh.config.load_from_clipboard"))
											.description(OptionDescription.of(Component.translatable("cbh.config.load_from_clipboard.description")))
											.text(Component.translatable("cbh.config.load"))
											.action((screen, option) -> ConfigManager.loadFromClipboardWithNotice(Minecraft.getInstance().keyboardHandler.getClipboard()))
											.build())
									.option(ButtonOption.createBuilder()
											.name(named(DASH, "cbh.config.presets"))
											.action((screen, option) -> Minecraft.getInstance().setScreenAndShow(new PresetsScreen(false, screen)))
											.text(Component.translatable("cbh.config.open"))
											.build())
									.build())
							.build())
					.save(ConfigManager::save)
					.build();
		}

		void refresh() {
			BlockHighlightConfig c = ACTIVE_INSTANCE;
			boolean global = c.enableModRendering;
			available(global, PRIMARY_OPTIONS.enabled, o_fillEnabled, o_doEasing, o_fadeIn, o_fadeOut, o_scale, o_scaleOut, o_animateLineThickness, o_lineThicknessOut, o_animateLineCuts, o_lineCutOut,
					o_crystalHelper, o_allowLiquids, o_rotations, o_allowEntities, o_showWhenNoInteraction, o_showWhenNoHud,
					o_connectedBlocks, o_updateWhenUnfocused, o_restyleOtherOutlines, o_highlightBarriers);
			available(global && c.allowLiquids, o_whenHoldingAppropriate, o_onlySourceBlocks);

			boolean primary = global && c.primary.enabled;
			lineLayer(primary, c.primary, PRIMARY_OPTIONS);
			available(primary, SECONDARY_OPTIONS.enabled, TERTIARY_OPTIONS.enabled);
			lineLayer(primary && c.secondary.enabled, c.secondary, SECONDARY_OPTIONS);
			lineLayer(primary && c.tertiary.enabled, c.tertiary, TERTIARY_OPTIONS);

			boolean fill = global && c.fillEnabled;
			available(fill, o_fillType, o_fillShape, o_fillDepthTest, o_fillBlend, o_fillExpandBlocks, o_fillExpandPercent);
			colors(fill, c.fillCol, FILL_COLOR_OPTIONS);
			FILL_SCREEN_OPTIONS.availableFor(fill, c.fillScreenOutline);

			available(global && c.doEasing, o_easeSpeed, o_snapWhenFadedOut, o_unifiedQuads);
			available(global && c.fadeIn, o_fadeInSpeed);
			available(global && c.fadeOut, o_fadeOutSpeed);
			available(global && c.scale, o_scaleSpeed);
			available(global && c.scaleOut, o_scaleOutSpeed);
			available(global && c.animateLineThickness, o_lineThicknessSpeed);
			available(global && c.lineThicknessOut, o_lineThicknessOutSpeed);
			available(global && c.animateLineCuts, o_lineCutSpeed);
			available(global && c.lineCutOut, o_lineCutOutSpeed);
			available(global && c.crystalHelper, o_crystalHelperOnlyWhenHolding, o_crystalHelperFillColor, o_crystalHelperLineColor);
			available(global && c.rotations, o_rotationSpeed);
		}

		void push() {
			Arrays.stream(Options.class.getDeclaredFields())
					.filter(field -> field.getName().startsWith("o_"))
					.forEach(optionInstanceField -> {
						try {
							pushBindingValue((Option<?>) optionInstanceField.get(this));
						} catch (IllegalAccessException ignored) {
						}
					});
			LINE_OPTIONS.forEach(layer -> layer.all().forEach(YaclMenu::pushBindingValue));
			FILL_COLOR_OPTIONS.all().forEach(YaclMenu::pushBindingValue);
			FILL_SCREEN_OPTIONS.all().forEach(YaclMenu::pushBindingValue);
		}
	}

	static Options options;

	public static boolean isOwnConfig(YetAnotherConfigLib config) {
		return config.title().equals(Component.translatable("cbh.config.title"));
	}

	public static Screen getConfigScreen(Screen parent) {
		ACTIVE_INSTANCE.fixNulls();
		options = null;
		options = new Options();
		refreshAvailability();
		Screen generatedScreen = options.layout().generateScreen(parent);
		Path firstOpenPath = FabricLoader.getInstance().getConfigDir().resolve(".cbh_info");
		if (Files.notExists(firstOpenPath)) {
			try {
				Files.createFile(firstOpenPath);
				Files.setAttribute(firstOpenPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
			} catch (IOException | UnsupportedOperationException e) {
			}
			return new PresetsScreen(true, generatedScreen);
		} else {
			return generatedScreen;
		}
	}

	private static List<OptionGroup> lineGroups(String layerName, LineOptions o) {
		return List.of(
				OptionGroup.createBuilder()
						.name(Component.translatable(layerName))
						.option(o.enabled)
						.build(),
				o.color.group(),
				OptionGroup.createBuilder()
						.name(Component.translatable("cbh.config.misc"))
						.option(o.shapeStyle)
						.option(o.faceMode)
						.option(o.depthTest)
						.option(o.blend)
						.option(o.width)
						.build(),
				o.screen.group(),
				OptionGroup.createBuilder()
						.name(Component.translatable("cbh.config.scaling"))
						.option(o.expand)
						.option(o.expandPercent)
						.build(),
				OptionGroup.createBuilder()
						.name(Component.translatable("cbh.config.subdiv"))
						.option(o.cutFromCorner)
						.option(o.outerThicknessMult)
						.option(o.cutFromCenter)
						.option(o.innerThicknessMult)
						.build());
	}

	static boolean refreshingAvailability;

	public static void refreshAvailability() {
		if (options == null || refreshingAvailability) return;
		refreshingAvailability = true;
		try {
			options.refresh();
		} finally {
			refreshingAvailability = false;
		}
	}

	private static void lineLayer(boolean enabled, LineConfig layer, LineOptions o) {
		available(enabled, o.depthTest, o.blend, o.expand, o.expandPercent, o.width, o.cutFromCenter, o.cutFromCorner, o.innerThicknessMult, o.outerThicknessMult, o.shapeStyle, o.faceMode);
		o.screen.availableFor(enabled, layer.screenOutline);
		colors(enabled, layer.color, o.color);
	}

	private static void colors(boolean enabled, ColorSetting setting, ColorOptions o) {
		RainbowSettings rainbow = setting.rainbowSettings;
		available(enabled, o.alpha, o.rainbow);
		available(enabled && !rainbow.enabled, o.col, o.col2);
		available(enabled && rainbow.enabled, o.rainbowSpeed, o.rainbowDelay, o.saturation, o.brightness);
	}

	private static void available(boolean available, Option<?>... options) {
		for (Option<?> option : options) {
			if (option.available() != available) option.setAvailable(available);
		}
	}

	@SuppressWarnings("unchecked")
	private static void pushBindingValue(Option<?> option) {
		EvilInstantStateManager<Object> stateManager = (EvilInstantStateManager<Object>) option.stateManager();
		stateManager.set(stateManager.getBinding().getValue());
		stateManager.apply();
	}

	static boolean pushingValues;

	static void pushActive() {
		if (options == null) return;
		pushingValues = true;
		try {
			options.push();
		} finally {
			pushingValues = false;
		}
		refreshAvailability();
	}
}
//?}
