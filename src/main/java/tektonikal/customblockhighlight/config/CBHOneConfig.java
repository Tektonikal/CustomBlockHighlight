package tektonikal.customblockhighlight.config;

import net.minecraft.client.Minecraft;
//? if =1.8.9
//import net.minecraft.client.gui.screen.Screen;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.ConfigManager.ProfileChangeListener;
import org.polyfrost.oneconfig.api.config.v1.Node;
import org.polyfrost.oneconfig.api.config.v1.Properties;
import org.polyfrost.oneconfig.api.config.v1.Property;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.Visualizer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.ColorSetting;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.LineConfig;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.RainbowSettings;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
import tektonikal.customblockhighlight.util.BlendMode;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.FaceMode;
import tektonikal.customblockhighlight.util.OutlineFit;
import tektonikal.customblockhighlight.util.OutlineSides;
import tektonikal.customblockhighlight.util.ScreenOutline;
import tektonikal.customblockhighlight.util.ShapeStyle;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static org.polyfrost.oneconfig.api.config.v1.ConfigManager.active;
import static org.polyfrost.oneconfig.api.config.v1.ConfigManager.addProfileChangeListener;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.getActiveInstance;

public class CBHOneConfig extends Config {
	private static final BlockHighlightConfig DEFAULTS = new BlockHighlightConfig();
	private static CBHOneConfig instance;

	private List<Property<?>> properties;
	private Tree tree;
	private Tree target;
	private String category;
	private String subcategory;
	private Node last;

	private CBHOneConfig() {
		super("custom-block-highlight.json", "assets/customblockhighlight/icon.png", "Custom Block Highlight", Category.VISUALS);
	}

	public static void init() {
		if (instance != null) return;
		instance = new CBHOneConfig();
		addProfileChangeListener(new ProfileChangeListener() {
			@Override
			public void onProfileCreated(String profile) {
				BlockHighlightConfig.ACTIVE_INSTANCE = new BlockHighlightConfig();
			}

			@Override
			public void onProfileChanged(String profile) {
				BlockHighlightConfig.ACTIVE_INSTANCE = ConfigManager.load();
				refresh();
			}
		});
	}

	static Path profileFolder() {
		return active().getFolder();
	}

	public static void refresh() {
		if (instance == null || instance.properties == null) return;
		for (Property<?> property : instance.properties) property.revaluateDisplay();
	}

	public static void openPresets() {
		//? if >1.8.9 {
		Minecraft.getInstance().setScreenAndShow(new PresetsScreen(false, Minecraft.getInstance().gui.screen()));
		//?} else {
		/*Minecraft minecraft = Minecraft.getInstance();
		minecraft.openScreen(new PresetsScreen(false, minecraft.screen));
		*///?}
	}

	private static void setClipboard(String text) {
		//? if >1.8.9 {
		Minecraft.getInstance().keyboardHandler.setClipboard(text);
		//?} else
		//Screen.setClipboard(text);
	}

	private static String getClipboard() {
		//? if >1.8.9 {
		return Minecraft.getInstance().keyboardHandler.getClipboard();
		//?} else
		//return Screen.getClipboard();
	}

	@Override
	protected Tree makeTree() {
		Tree tree = super.makeTree();
		if (tree == null) return null;
		this.tree = tree;
		this.target = tree;
		this.properties = new ArrayList<>();
		tree.addMetadata("custom_save", (Runnable) ConfigManager::save);
		tree.addMetadata("no_cache", true);
		tree.addMetadata("aliases", new ArrayList<>(Arrays.asList("cbh", "block outline", "block highlight", "block overlay", "selection box")));
		//? if =1.8.9
		//tree.addMetadata("open_original_screen", (Runnable) CBHOneConfig::openPresets);
		buildOptions();
		return tree;
	}

	private void buildOptions() {
		BooleanSupplier global = () -> getActiveInstance().enableModRendering;

		group("cbh.config.outline", "cbh.config.general");
		toggle("globalModToggle", "cbh.config.global_toggle", null, c -> c.enableModRendering, (c, v) -> c.enableModRendering = v, () -> true)
				.addMetadata("masterToggle", true);
		aliases("toggle", "disable", "on", "off");
		simple();
		toggle("vanillaOutline", "cbh.config.show_vanilla_outline", null, c -> c.drawVanillaOutline, (c, v) -> c.drawVanillaOutline = v, () -> true);
		aliases("default outline", "black outline");
		toggle("simpleMode", "cbh.config.simple_mode", "cbh.config.simple_mode.description", c -> c.simpleMode, (c, v) -> c.simpleMode = v, () -> true);
		aliases("basic", "advanced", "beginner");
		simple();
		lineLayer("", "cbh.config.primary_layer", c -> c.primary, "cbh.config.main_outline_toggle_desc", global, BlockHighlightConfig.PRIMARY_LINE_WIDTH, BlockHighlightConfig.PRIMARY_CUT);

		BooleanSupplier outline = () -> global.getAsBoolean() && getActiveInstance().primary.enabled;
		lineLayer("s", "cbh.config.secondary_layer", c -> c.secondary, "cbh.config.non_primary_layer_toggle_desc", outline, BlockHighlightConfig.LAYER_LINE_WIDTH, BlockHighlightConfig.LAYER_CUT);
		lineLayer("t", "cbh.config.tertiary_layer", c -> c.tertiary, "cbh.config.non_primary_layer_toggle_desc", outline, BlockHighlightConfig.LAYER_LINE_WIDTH, BlockHighlightConfig.LAYER_CUT);

		group("cbh.config.fill", "cbh.config.fill");
		toggle("fillEnabled", "cbh.config.enabled", null, c -> c.fillEnabled, (c, v) -> c.fillEnabled = v, global);
		simple();
		BooleanSupplier fill = () -> global.getAsBoolean() && getActiveInstance().fillEnabled;
		beginAccordion("fillColorGroup", "cbh.config.color");
		aliases("colour", "rgb", "rainbow", "chroma", "gradient", "opacity", "transparency");
		colorSetting("fill", c -> c.fillCol, fill, 1);
		beginAccordion("fillScalingGroup", "cbh.config.scaling");
		aliases("size", "grow", "shrink", "padding", "inflate");
		slider("fillExpandBlocks", "cbh.config.expand_blocks", null, c -> c.fillExpandBlocks, (c, v) -> c.fillExpandBlocks = v, BlockHighlightConfig.EXPAND_BLOCKS, fill);
		slider("fillExpandPercent", "cbh.config.expand_percent", null, c -> c.fillExpandPercent, (c, v) -> c.fillExpandPercent = v, BlockHighlightConfig.EXPAND_PERCENT, fill);
		beginAccordion("fillMiscGroup", "cbh.config.misc");
		dropdown("fillShape", "cbh.enum.shape_style", "cbh.config.fill_shape.description", ShapeStyle.class, ShapeStyle::key, c -> c.fillShape, (c, v) -> c.fillShape = v, fill);
		aliases("hitbox", "collision", "model");
		simple();
		dropdown("fillType", "cbh.config.mode", "cbh.config.mode.description.1", FaceMode.class, FaceMode::key, c -> c.fillType, (c, v) -> c.fillType = v, fill);
		aliases("faces", "sides");
		dropdown("fillDepthTest", "cbh.config.depthTest", "cbh.config.depthTest.description", DepthTestMode.class, DepthTestMode::key, c -> c.fillDepthTest, (c, v) -> c.fillDepthTest = v, fill);
		aliases("see through", "xray", "through walls");
		dropdown("fillBlend", "cbh.config.blendMode", "cbh.config.blendMode.description", BlendMode.class, BlendMode::key, c -> c.fillBlend, (c, v) -> c.fillBlend = v, fill);
		aliases("additive", "multiply", "screen", "invert", "subtract", "glow");
		screenOutline("fill", c -> c.fillScreenOutline, fill, false);

		group("cbh.config.visuals", "cbh.config.animations");
		beginAccordion("easingGroup", "cbh.config.easing");
		headToggle("doEasing", "cbh.config.easing", "cbh.config.easing.description", c -> c.doEasing, (c, v) -> c.doEasing = v, global);
		simple();
		aliases("smooth", "animation", "lerp", "interpolation");
		slider("easeSpeed", "cbh.config.speed", null, c -> c.easeSpeed, (c, v) -> c.easeSpeed = v, BlockHighlightConfig.EASE_SPEED, global);
		simple();
		toggle("unifiedQuads", "cbh.config.unified_quads", "cbh.config.unified_quads.description", c -> c.unifiedQuads, (c, v) -> c.unifiedQuads = v, global);
		aliases("faces", "together", "fill");

		beginAccordion("fadeGroup", "cbh.config.fade");
		aliases("fade", "opacity animation");
		toggle("fadeIn", "cbh.config.in", null, c -> c.fadeIn, (c, v) -> c.fadeIn = v, global);
		slider("fadeInSpeed", "cbh.config.speed", null, c -> c.fadeInSpeed, (c, v) -> c.fadeInSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().fadeIn);
		toggle("fadeOut", "cbh.config.out", null, c -> c.fadeOut, (c, v) -> c.fadeOut = v, global);
		slider("fadeOutSpeed", "cbh.config.speed", null, c -> c.fadeOutSpeed, (c, v) -> c.fadeOutSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().fadeOut);

		beginAccordion("scaleGroup", "cbh.config.scale");
		aliases("grow", "pop");
		toggle("scale", "cbh.config.in", null, c -> c.scale, (c, v) -> c.scale = v, global);
		slider("scaleSpeed", "cbh.config.speed", null, c -> c.scaleSpeed, (c, v) -> c.scaleSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().scale);
		toggle("scaleOut", "cbh.config.out", null, c -> c.scaleOut, (c, v) -> c.scaleOut = v, global);
		slider("scaleOutSpeed", "cbh.config.speed", null, c -> c.scaleOutSpeed, (c, v) -> c.scaleOutSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().scaleOut);

		beginAccordion("lineThicknessGroup", "cbh.config.line_thickness");
		aliases("width animation");
		toggle("animateLineThickness", "cbh.config.in", null, c -> c.animateLineThickness, (c, v) -> c.animateLineThickness = v, global);
		slider("lineThicknessSpeed", "cbh.config.speed", null, c -> c.lineThicknessAnimationSpeed, (c, v) -> c.lineThicknessAnimationSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().animateLineThickness);
		toggle("lineThicknessOut", "cbh.config.out", null, c -> c.lineThicknessOut, (c, v) -> c.lineThicknessOut = v, global);
		slider("lineThicknessOutSpeed", "cbh.config.speed", null, c -> c.lineThicknessOutSpeed, (c, v) -> c.lineThicknessOutSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().lineThicknessOut);

		beginAccordion("lineCutGroup", "cbh.config.line_cuts");
		aliases("cut", "gaps", "subdivision animation");
		toggle("animateLineCuts", "cbh.config.in", null, c -> c.animateLineCuts, (c, v) -> c.animateLineCuts = v, global);
		slider("lineCutSpeed", "cbh.config.speed", null, c -> c.lineCutSpeed, (c, v) -> c.lineCutSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().animateLineCuts);
		toggle("lineCutOut", "cbh.config.out", null, c -> c.lineCutOut, (c, v) -> c.lineCutOut = v, global);
		slider("lineCutOutSpeed", "cbh.config.speed", null, c -> c.lineCutOutSpeed, (c, v) -> c.lineCutOutSpeed = v, BlockHighlightConfig.ANIMATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().lineCutOut);

		beginAccordion("rotationGroup", "cbh.config.rotation");
		toggle("rotations", "cbh.config.rotations", "cbh.config.rotations.description", c -> c.rotations, (c, v) -> c.rotations = v, global);
		aliases("spin", "tilt");
		slider("rotationSpeed", "cbh.config.speed", null, c -> c.rotationSpeed, (c, v) -> c.rotationSpeed = v, BlockHighlightConfig.ROTATION_SPEED, () -> global.getAsBoolean() && getActiveInstance().rotations);

		group("cbh.config.behaviour", "cbh.config.general");
		beginAccordion("renderConditionsGroup", "cbh.config.render_conditions");
		toggle("allowEntities", "cbh.config.select_entities", null, c -> c.allowEntities, (c, v) -> c.allowEntities = v, global);
		aliases("mobs", "players");
		toggle("allowLiquids", "cbh.config.select_fluids", "cbh.config.select_fluids.description", c -> c.allowLiquids, (c, v) -> c.allowLiquids = v, global);
		aliases("water", "lava");
		toggle("onlySourceBlocks", "cbh.config.only_source_blocks", null, c -> c.onlySourceBlocks, (c, v) -> c.onlySourceBlocks = v, () -> global.getAsBoolean() && getActiveInstance().allowLiquids);
		toggle("whenHoldingAppropriate", "cbh.config.holding_appropriate_item", "cbh.config.holding_appropriate_item_desc", c -> c.onlyWhenHoldingAppropriate, (c, v) -> c.onlyWhenHoldingAppropriate = v, () -> global.getAsBoolean() && getActiveInstance().allowLiquids);
		toggle("highlightBarriers", "cbh.config.highlight_barriers", "cbh.config.highlight_barriers.description", c -> c.highlightBarriers, (c, v) -> c.highlightBarriers = v, global);
		toggle("showWhenNoHud", "cbh.config.show_when_no_hud", null, c -> c.showWhenNoHud, (c, v) -> c.showWhenNoHud = v, global);
		aliases("f1", "hide gui");
		toggle("showWhenNoInteraction", "cbh.config.show_when_no_interaction", "cbh.config.show_when_no_interaction.desc", c -> c.showWhenNoInteraction, (c, v) -> c.showWhenNoInteraction = v, global);
		aliases("adventure", "spectator");

		beginAccordion("movementGroup", "cbh.config.movement");
		toggle("updateWhenUnfocused", "cbh.config.update_when_unfocused", "cbh.config.update_when_unfocused.description", c -> c.updateWhenUnfocused, (c, v) -> c.updateWhenUnfocused = v, global);
		aliases("background");
		toggle("snapWhenFadedOut", "cbh.config.snap_when_faded_out", "cbh.config.snap_when_faded_out.description", c -> c.snapWhenFadedOut, (c, v) -> c.snapWhenFadedOut = v, () -> global.getAsBoolean() && getActiveInstance().doEasing);
		aliases("teleport", "instant");

		//? if >1.8.9 {
		beginAccordion("crystalHelperGroup", "cbh.config.crystal_helper");
		headToggle("crystalHelper", "cbh.config.crystal_helper", "cbh.config.crystal_helper.description", c -> c.crystalHelper, (c, v) -> c.crystalHelper = v, global);
		aliases("end crystal", "obsidian", "pvp");
		toggle("crystalHelperOnlyWhenHolding", "cbh.config.crystal_helper.only_when_holding", "cbh.config.crystal_helper.only_when_holding.description", c -> c.crystalHelperOnlyWhenHolding, (c, v) -> c.crystalHelperOnlyWhenHolding = v, global);
		plainColor("crystalHelperLineColor", "cbh.config.crystal_helper.line_color", c -> c.crystalHelperLineColor, (c, v) -> c.crystalHelperLineColor = v, global);
		plainColor("crystalHelperFillColor", "cbh.config.crystal_helper.fill_color", c -> c.crystalHelperFillColor, (c, v) -> c.crystalHelperFillColor = v, global);
		//?}

		beginAccordion("miscGroup", "cbh.config.misc");
		toggle("connectedBlocks", "cbh.config.connected_outlines", "cbh.config.connected_outlines.description", c -> c.connectedBlocks, (c, v) -> c.connectedBlocks = v, global);
		aliases("chest", "double chest", "merge");
		//? if >=26.2 {
		toggle("restyleOtherOutlines", "cbh.config.restyle_other_outlines", "cbh.config.restyle_other_outlines.description", c -> c.restyleOtherOutlines, (c, v) -> c.restyleOtherOutlines = v, global);
		aliases("other mods", "compat");
		//?}

		beginAccordion("configGroup", "cbh.config");
		simple();
		button("copy", "cbh.config.copy_to_clipboard", null, "cbh.config.copy", () -> {
			ConfigManager.save();
			setClipboard(ConfigManager.GSON.toJson(getActiveInstance()));
		});
		aliases("export", "share");
		button("load", "cbh.config.load_from_clipboard", "cbh.config.load_from_clipboard.description", "cbh.config.load", () -> ConfigManager.loadFromClipboardWithNotice(getClipboard()));
		aliases("import", "paste");
		button("presets", "cbh.config.presets", null, "cbh.config.open", CBHOneConfig::openPresets);
		aliases("templates");

		properties.forEach(Property::revaluateDisplay);
	}

	private void lineLayer(String prefix, String layerName, Function<BlockHighlightConfig, LineConfig> layer, String toggleDescription, BooleanSupplier parent, BlockHighlightConfig.Range widthRange, BlockHighlightConfig.Range cutRange) {
		group("cbh.config.outline", layerName);
		toggle(prefix + "outlineEnabled", "cbh.config.enabled", toggleDescription, c -> layer.apply(c).enabled, (c, v) -> layer.apply(c).enabled = v, parent);
		simple();
		BooleanSupplier enabled = () -> parent.getAsBoolean() && layer.apply(getActiveInstance()).enabled;

		beginAccordion(prefix + "lineColorGroup", "cbh.config.color");
		aliases("colour", "rgb", "rainbow", "chroma", "gradient", "opacity", "transparency");
		colorSetting(prefix + "line", c -> layer.apply(c).color, enabled, 0);

		beginAccordion(prefix + "lineShapeGroup", "cbh.config.shape");
		dropdown(prefix + "shapeStyle", "cbh.enum.shape_style", "cbh.enum.shape_style.description", ShapeStyle.class, ShapeStyle::key, c -> layer.apply(c).shapeStyle, (c, v) -> layer.apply(c).shapeStyle = v, enabled);
		aliases("hitbox", "collision", "model");
		simple();
		dropdown(prefix + "outlineType", "cbh.config.mode", "cbh.config.mode.description.1", FaceMode.class, FaceMode::key, c -> layer.apply(c).outlineType, (c, v) -> layer.apply(c).outlineType = v, enabled);
		aliases("faces", "sides");
		dropdown(prefix + "lineDepthTest", "cbh.config.depthTest", "cbh.config.depthTest.description", DepthTestMode.class, DepthTestMode::key, c -> layer.apply(c).lineDepthTest, (c, v) -> layer.apply(c).lineDepthTest = v, enabled);
		aliases("see through", "xray", "through walls");
		dropdown(prefix + "lineBlend", "cbh.config.blendMode", "cbh.config.blendMode.description", BlendMode.class, BlendMode::key, c -> layer.apply(c).lineBlend, (c, v) -> layer.apply(c).lineBlend = v, enabled);
		aliases("additive", "multiply", "screen", "invert", "subtract", "glow");
		slider(prefix + "lineWidth", "cbh.config.lineWidth", null, c -> layer.apply(c).lineWidth, (c, v) -> layer.apply(c).lineWidth = v, widthRange, enabled);
		aliases("thickness");
		simple();
		screenOutline(prefix + "line", c -> layer.apply(c).screenOutline, enabled, true);

		beginAccordion(prefix + "lineScalingGroup", "cbh.config.scaling");
		aliases("size", "grow", "shrink", "padding", "inflate");
		slider(prefix + "lineExpand", "cbh.config.expand_blocks", null, c -> layer.apply(c).lineExpandBlocks, (c, v) -> layer.apply(c).lineExpandBlocks = v, BlockHighlightConfig.EXPAND_BLOCKS, enabled);
		slider(prefix + "lineExpandPercent", "cbh.config.expand_percent", null, c -> layer.apply(c).lineExpandPercentage, (c, v) -> layer.apply(c).lineExpandPercentage = v, BlockHighlightConfig.EXPAND_PERCENT, enabled);

		beginAccordion(prefix + "lineSubdivisionGroup", "cbh.config.subdiv");
		aliases("corners", "gaps", "dashed", "split");
		slider(prefix + "cutFromCorner", "cbh.config.cutFromCorner", null, c -> layer.apply(c).cutFromCorner, (c, v) -> {
			LineConfig line = layer.apply(c);
			line.cutFromCorner = v;
			line.cutFromCenter = BlockHighlightConfig.limitedCut(v, line.cutFromCenter);
		}, cutRange, enabled);
		slider(prefix + "outerThicknessMult", "cbh.config.outer_thickness_multiplier", "cbh.config.thickness_multiplier_desc", c -> layer.apply(c).outerThicknessMult, (c, v) -> layer.apply(c).outerThicknessMult = v, BlockHighlightConfig.THICKNESS_MULT, enabled);
		slider(prefix + "cutFromCenter", "cbh.config.cutFromCenter", null, c -> layer.apply(c).cutFromCenter, (c, v) -> {
			LineConfig line = layer.apply(c);
			line.cutFromCenter = v;
			line.cutFromCorner = BlockHighlightConfig.limitedCut(v, line.cutFromCorner);
		}, cutRange, enabled);
		slider(prefix + "innerThicknessMult", "cbh.config.inner_thickness_multiplier", "cbh.config.thickness_multiplier_desc", c -> layer.apply(c).innerThicknessMult, (c, v) -> layer.apply(c).innerThicknessMult = v, BlockHighlightConfig.THICKNESS_MULT, enabled);
		endAccordion();
	}

	private void screenOutline(String prefix, Function<BlockHighlightConfig, ScreenOutline> outline, BooleanSupplier drawn, boolean keepOnScreenOption) {
		beginAccordion(prefix + "ScreenGroup", "cbh.config.screen_space");
		headToggle(prefix + "ScreenSpace", "cbh.config.screen_space", "cbh.config.screen_space_enabled.description", c -> outline.apply(c).enabled, (c, v) -> outline.apply(c).enabled = v, drawn);
		aliases("screen space", "flat", "2d");
		BooleanSupplier polygon = () -> drawn.getAsBoolean() && !outline.apply(getActiveInstance()).exact();
		dropdown(prefix + "ScreenSides", "cbh.config.screen_sides", "cbh.config.screen_sides.description", OutlineSides.class, OutlineSides::key, c -> OutlineSides.of(outline.apply(c).sides), (c, v) -> outline.apply(c).sides = v.count, drawn);
		aliases("polygon", "circle", "triangle", "square", "exact", "sides");
		dropdown(prefix + "ScreenFit", "cbh.config.screen_fit", "cbh.config.screen_fit.description", OutlineFit.class, OutlineFit::key, c -> outline.apply(c).fit, (c, v) -> outline.apply(c).fit = v, polygon);
		slider(prefix + "ScreenRotation", "cbh.config.screen_rotation", null, c -> outline.apply(c).rotation, (c, v) -> outline.apply(c).rotation = v, BlockHighlightConfig.SCREEN_ROTATION, polygon);
		if (keepOnScreenOption) {
			toggle(prefix + "KeepOnScreen", "cbh.config.keep_on_screen", "cbh.config.keep_on_screen.description", c -> outline.apply(c).keepOnScreen, (c, v) -> outline.apply(c).keepOnScreen = v, drawn);
			aliases("clamp", "off screen", "edge");
		}
		endAccordion();
	}

	private void colorSetting(String prefix, Function<BlockHighlightConfig, ColorSetting> setting, BooleanSupplier enabled, int minAlpha) {
		BooleanSupplier rainbow = () -> setting.apply(getActiveInstance()).rainbowSettings.enabled;
		color(prefix + "Col", "cbh.config.color", setting, minAlpha, enabled);
		simple();
		intSlider(prefix + "RainbowDelay", "cbh.config.delay", "cbh.config.delay.description", c ->setting.apply(c).rainbowSettings.delay, (c, v) -> setting.apply(c).rainbowSettings.delay = v, (int) BlockHighlightConfig.RAINBOW_DELAY.min(), (int) BlockHighlightConfig.RAINBOW_DELAY.max(), enabled);
		child(rainbow);
		slider(prefix + "Saturation","cbh.config.saturation", null, c -> setting.apply(c).rainbowSettings.saturation, (c, v) -> setting.apply(c).rainbowSettings.saturation = v, BlockHighlightConfig.UNIT, enabled);
		child(rainbow);
		slider(prefix + "Brightness","cbh.config.brightness", null, c -> setting.apply(c).rainbowSettings.brightness, (c, v) -> setting.apply(c).rainbowSettings.brightness = v, BlockHighlightConfig.UNIT, enabled);
		child(rainbow);
	}

	//chroma speed in oneconfig is cycles per 10 seconds
	private static final float CHROMA_SPEED_SCALE = 1.8F;

	static PolyColor toPolyColor(ColorSetting setting) {
		RainbowSettings rainbow = setting.rainbowSettings;
		int alpha = setting.alpha << 24;
		return new PolyColor(alpha | (setting.col1.getRGB() & 0xFFFFFF), rainbow.enabled, rainbow.speed / CHROMA_SPEED_SCALE)
				.withGradient(setting.isGradient(), alpha | (setting.col2.getRGB() & 0xFFFFFF));
	}

	static void fromPolyColor(ColorSetting setting, PolyColor value, int minAlpha) {
		int argb = value.getRawArgb();
		RainbowSettings rainbow = setting.rainbowSettings;
		setting.col1 = new Color(argb & 0xFFFFFF);
		rainbow.enabled = value.getChroma();
		setting.gradient = value.getGradientEnabled();
		if (value.getGradientEnabled()) setting.col2 = new Color(value.getGradientArgb() & 0xFFFFFF);
		setting.alpha = Math.clamp(argb >>> 24, minAlpha, 255);
		rainbow.speed = Math.clamp(value.getChromaSpeed() * CHROMA_SPEED_SCALE, BlockHighlightConfig.RAINBOW_SPEED.min(), BlockHighlightConfig.RAINBOW_SPEED.max());
	}


	private void group(String category, String subcategory) {
		this.category = category;
		this.subcategory = subcategory;
		endAccordion();
	}

	private void beginAccordion(String id, String title) {
		Tree accordion = new Tree(id, title, null, null);
		accordion.addMetadata("category", category);
		accordion.addMetadata("subcategory", subcategory);
		accordion.addMetadata("collapsed", false);
		tree.put(accordion);
		target = accordion;
		last = accordion;
	}

	private void simple() {
		last.addMetadata("simple", true);
	}

	private static boolean hiddenBySimpleMode(Property<?> property, Tree parent) {
		return getActiveInstance().simpleMode && property.getMetadata("simple") == null && parent.getMetadata("simple") == null;
	}

	private void child(BooleanSupplier parentOn) {
		((Property<?>) last).addDisplayCondition(() -> parentOn.getAsBoolean() ? Property.Display.SHOWN : Property.Display.HIDDEN);
	}

	private void aliases(String... aliases) {
		last.addMetadata("aliases", new ArrayList<>(Arrays.asList(aliases)));
	}

	private void endAccordion() {
		target = tree;
	}

	private void headToggle(String id, String title, String description, Function<BlockHighlightConfig, Boolean> getter, BiConsumer<BlockHighlightConfig, Boolean> setter, BooleanSupplier enabled) {
		property(id, title, description, getter, setter, Boolean.class, enabled);
	}

	private void plainColor(String id, String title, Function<BlockHighlightConfig, Color> getter, BiConsumer<BlockHighlightConfig, Color> setter, BooleanSupplier enabled) {
		Property<PolyColor> property = property(id, title, null, c -> new PolyColor(getter.apply(c).getRGB() | 0xFF000000), (c, v) -> setter.accept(c, new Color(v.getRawArgb() & 0xFFFFFF)), PolyColor.class, enabled);
		property.addMetadata("visualizer", Visualizer.ColorVisualizer.class);
	}

	private Property<Boolean> toggle(String id, String title, String description, Function<BlockHighlightConfig, Boolean> getter, BiConsumer<BlockHighlightConfig, Boolean> setter, BooleanSupplier enabled) {
		Property<Boolean> property = property(id, title, description, getter, setter, Boolean.class, enabled);
		property.addMetadata("visualizer", Visualizer.SwitchVisualizer.class);
		return property;
	}

	private void slider(String id, String title, String description, Function<BlockHighlightConfig, Float> getter, BiConsumer<BlockHighlightConfig, Float> setter, BlockHighlightConfig.Range range, BooleanSupplier enabled) {
		Property<Float> property = property(id, title, description, getter, (c, v) -> setter.accept(c, Math.clamp(v, range.min(), range.max())), Float.class, enabled);
		property.addMetadata("visualizer", Visualizer.SliderVisualizer.class);
		property.addMetadata("min", range.min());
		property.addMetadata("max", range.max());
		property.addMetadata("step", range.step());
	}

	private void intSlider(String id, String title, String description, Function<BlockHighlightConfig, Integer> getter, BiConsumer<BlockHighlightConfig, Integer> setter, int min, int max, BooleanSupplier enabled) {
		Property<Integer> property = property(id, title, description, getter, (c, v) -> setter.accept(c, Math.clamp(v, min, max)), Integer.class, enabled);
		property.addMetadata("visualizer", Visualizer.SliderVisualizer.class);
		property.addMetadata("min", (float) min);
		property.addMetadata("max", (float) max);
		property.addMetadata("step", 1F);
	}

	private void color(String id, String title, Function<BlockHighlightConfig, ColorSetting> setting, int minAlpha, BooleanSupplier enabled) {
		Property<PolyColor> property = property(id, title, null, c -> toPolyColor(setting.apply(c)), (c, v) -> fromPolyColor(setting.apply(c), v, minAlpha), PolyColor.class, enabled);
		property.addMetadata("visualizer", Visualizer.ColorVisualizer.class);
		property.addMetadata("gradient", true);
	}

	private <E extends Enum<E>> void dropdown(String id, String title, String description, Class<E> type, Function<E, String> key, Function<BlockHighlightConfig, E> getter, BiConsumer<BlockHighlightConfig, E> setter, BooleanSupplier enabled) {
		Property<E> property = property(id, title, description, getter, setter, type, enabled);
		property.addMetadata("visualizer", Visualizer.DropdownVisualizer.class);
		List<E> values = Arrays.asList(type.getEnumConstants());
		property.addMetadata("options", values.stream().map(key).toList());
		property.addMetadata("optionValues", values);
	}

	private void button(String id, String title, String description, String text, Runnable action) {
		Property<Void> property = Properties.dummy(id, title, description);
		property.addMetadata("visualizer", Visualizer.ButtonVisualizer.class);
		property.addMetadata("text", text);
		property.addMetadata("runnable", action);
		property.addMetadata("category", category);
		property.addMetadata("subcategory", subcategory);
		Tree parent = target;
		property.addDisplayCondition(() -> hiddenBySimpleMode(property, parent) ? Property.Display.HIDDEN : Property.Display.SHOWN);
		properties.add(property);
		target.put(property);
		last = property;
	}

	private <T> Property<T> property(String id, String title, String description, Function<BlockHighlightConfig, T> getter, BiConsumer<BlockHighlightConfig, T> setter, Class<T> type, BooleanSupplier enabled) {
		Property<T> property = Properties.functional(
				() -> getter.apply(getActiveInstance()),
				value -> {
					if (value == null) return;
					setter.accept(getActiveInstance(), value);
					refresh();
				},
				id, title, description, type
		);
		property.addMetadata("default", getter.apply(DEFAULTS));
		property.addMetadata("category", category);
		property.addMetadata("subcategory", subcategory);
		Tree parent = target;
		property.addDisplayCondition(() -> {
			if (hiddenBySimpleMode(property, parent)) return Property.Display.HIDDEN;
			return enabled.getAsBoolean() ? Property.Display.SHOWN : Property.Display.DISABLED;
		});
		properties.add(property);
		target.put(property);
		last = property;
		return property;
	}
}
