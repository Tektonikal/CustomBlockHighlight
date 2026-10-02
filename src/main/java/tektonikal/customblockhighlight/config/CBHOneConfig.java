package tektonikal.customblockhighlight.config;

//? if =1.8.9 {
/*import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Properties;
import org.polyfrost.oneconfig.api.config.v1.Property;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.Visualizer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.ColorSetting;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.LineConfig;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.FaceMode;
import tektonikal.customblockhighlight.util.ShapeStyle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static tektonikal.customblockhighlight.config.BlockHighlightConfig.getActiveInstance;

public class CBHOneConfig extends Config {
	private static final BlockHighlightConfig DEFAULTS = new BlockHighlightConfig();
	private static CBHOneConfig instance;

	// assigned in makeTree, which OneConfig may call from the super constructor before field initializers run
	private List<Property<?>> properties;
	private Tree tree;
	private Tree target;
	private String category;
	private String subcategory;

	private CBHOneConfig() {
		super("custom-block-highlight.json", "assets/icon.png", "Custom Block Highlight", Category.VISUALS);
	}

	public static void init() {
		if (instance == null) instance = new CBHOneConfig();
	}

	public static void refresh() {
		if (instance == null || instance.properties == null) return;
		for (Property<?> property : instance.properties) property.revaluateDisplay();
	}

	public static void openPresets() {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.openScreen(new PresetsScreen(false, minecraft.screen));
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
		tree.addMetadata("open_original_screen", (Runnable) CBHOneConfig::openPresets);
		buildOptions();
		return tree;
	}

	private void buildOptions() {
		BooleanSupplier global = () -> getActiveInstance().enableModRendering;

		group("Outline", "General");
		toggle("globalModToggle", "Enable Mod Rendering", null, c -> c.enableModRendering, (c, v) -> c.enableModRendering = v, () -> true);
		toggle("vanillaOutline", "Show Vanilla Outline", null, c -> c.drawVanillaOutline, (c, v) -> c.drawVanillaOutline = v, () -> true);
		lineLayer("", "Primary Layer", c -> c.primary, "Completely toggle all line rendering.", global, 0.5F, 0.99F);

		BooleanSupplier outline = () -> global.getAsBoolean() && getActiveInstance().primary.enabled;
		lineLayer("s", "Secondary Layer", c -> c.secondary, "Enable another line layer. Each layer renders under the preceding ones.", outline, 1F, 0.95F);
		lineLayer("t", "Tertiary Layer", c -> c.tertiary, "Enable another line layer. Each layer renders under the preceding ones.", outline, 1F, 0.95F);

		group("Fill", "Fill");
		toggle("fillEnabled", "Enabled", null, c -> c.fillEnabled, (c, v) -> c.fillEnabled = v, global);
		BooleanSupplier fill = () -> global.getAsBoolean() && getActiveInstance().fillEnabled;
		subcategory = "Color";
		colorSetting("fill", c -> c.fillCol, fill, 1);
		subcategory = "Scaling";
		slider("fillExpandBlocks", "Adjust Size By (Blocks)", null, c -> c.fillExpandBlocks, (c, v) -> c.fillExpandBlocks = v, -2F, 1F, 0.0625F, fill);
		slider("fillExpandPercent", "Adjust Size By (Percentage)", null, c -> c.fillExpandPercent, (c, v) -> c.fillExpandPercent = v, 0F, 2F, 0.01F, fill);
		subcategory = "Miscellaneous";
		dropdown("fillType", "Face Mode", FACE_MODE_DESCRIPTION, FaceMode.class, c -> c.fillType, (c, v) -> c.fillType = v, fill);
		dropdown("fillDepthTest", "Depth Test", DEPTH_TEST_DESCRIPTION, DepthTestMode.class, c -> c.fillDepthTest, (c, v) -> c.fillDepthTest = v, fill);

		group("Extras", "Easing");
		toggle("doEasing", "Enabled", "Controls how fast the outline moves between targets, as well as individual lines from the model. Disable to turn off all animation.", c -> c.doEasing, (c, v) -> c.doEasing = v, global);
		BooleanSupplier easing = () -> global.getAsBoolean() && getActiveInstance().doEasing;
		slider("easeSpeed", "Speed", null, c -> c.easeSpeed, (c, v) -> c.easeSpeed = v, 5F, 100F, 0.5F, easing);
		toggle("improvedEasing", "Improved Easing", "Calculates animations based on frame timings rather than FPS for more consistent animations, especially during rapid changes in FPS. Meant for people who play on uncapped FPS!", c -> c.improvedEasing, (c, v) -> c.improvedEasing = v, easing);

		subcategory = "Fading Animations";
		toggle("fadeIn", "In", null, c -> c.fadeIn, (c, v) -> c.fadeIn = v, global);
		slider("fadeInSpeed", "Speed", null, c -> c.fadeInSpeed, (c, v) -> c.fadeInSpeed = v, 5F, 25F, 0.1F, () -> global.getAsBoolean() && getActiveInstance().fadeIn);
		toggle("fadeOut", "Out", null, c -> c.fadeOut, (c, v) -> c.fadeOut = v, global);
		slider("fadeOutSpeed", "Speed", null, c -> c.fadeOutSpeed, (c, v) -> c.fadeOutSpeed = v, 5F, 25F, 0.1F, () -> global.getAsBoolean() && getActiveInstance().fadeOut);

		subcategory = "Scaling Animation";
		toggle("scale", "Enabled", null, c -> c.scale, (c, v) -> c.scale = v, global);
		slider("scaleSpeed", "Speed", null, c -> c.scaleSpeed, (c, v) -> c.scaleSpeed = v, 5F, 25F, 0.1F, () -> global.getAsBoolean() && getActiveInstance().scale);

		subcategory = "Line Thickness Animation";
		toggle("animateLineThickness", "Enabled", null, c -> c.animateLineThickness, (c, v) -> c.animateLineThickness = v, global);
		slider("lineThicknessSpeed", "Speed", null, c -> c.lineThicknessAnimationSpeed, (c, v) -> c.lineThicknessAnimationSpeed = v, 5F, 25F, 0.1F, () -> global.getAsBoolean() && getActiveInstance().animateLineThickness);

		subcategory = "Render Conditions";
		toggle("allowEntities", "Select Entities", null, c -> c.allowEntities, (c, v) -> c.allowEntities = v, global);
		toggle("allowLiquids", "Select Fluids", "Makes fluid blocks valid targets.", c -> c.allowLiquids, (c, v) -> c.allowLiquids = v, global);
		BooleanSupplier liquids = () -> global.getAsBoolean() && getActiveInstance().allowLiquids;
		toggle("onlySourceBlocks", "Only Source Blocks", null, c -> c.onlySourceBlocks, (c, v) -> c.onlySourceBlocks = v, liquids);
		toggle("whenHoldingAppropriate", "Only When Holding Appropriate Item", "Boats, buckets, and lily pads.", c -> c.onlyWhenHoldingAppropriate, (c, v) -> c.onlyWhenHoldingAppropriate = v, liquids);
		toggle("showWhenNoHud", "Show In Hidden HUD", null, c -> c.showWhenNoHud, (c, v) -> c.showWhenNoHud = v, global);
		toggle("showWhenNoInteraction", "Show When Unable To Interact", "Stops rendering of the selection when in adventure or spectator mode.", c -> c.showWhenNoInteraction, (c, v) -> c.showWhenNoInteraction = v, global);

		subcategory = "Miscellaneous";
		toggle("connectedBlocks", "Connected Outlines", "Connects double blocks like chests. Applies to both the fill and outline.", c -> c.connectedBlocks, (c, v) -> c.connectedBlocks = v, global);
		toggle("updateWhenUnfocused", "Update When Unfocused", "Continues moving the outline box toward its target even when it's not being rendered.", c -> c.updateWhenUnfocused, (c, v) -> c.updateWhenUnfocused = v, global);
		toggle("rotations", "Rotations (Beta)", "Rotates the outline based on what side of the block you're looking at. Not a finalized or stable feature, here be dragons!", c -> c.rotations, (c, v) -> c.rotations = v, global);
		slider("rotationSpeed", "Speed", null, c -> c.rotationSpeed, (c, v) -> c.rotationSpeed = v, 5F, 50F, 0.1F, () -> global.getAsBoolean() && getActiveInstance().rotations);

		subcategory = "Config";
		button("copy", "Copy To Clipboard", null, "Copy", () -> {
			ConfigManager.save(); // technically unnecessary
			Screen.setClipboard(ConfigManager.GSON.toJson(getActiveInstance()));
		});
		button("load", "Load From Clipboard", "Loads settings from your clipboard if they're valid.", "Load", () -> {
			try {
				BlockHighlightConfig yeah = ConfigManager.GSON.fromJson(Screen.getClipboard(), BlockHighlightConfig.class);
				if (yeah == null) {
					return;
				}
				BlockHighlightConfig.ACTIVE_INSTANCE = yeah.applyValuesToOptionInstances();
				ConfigManager.save();
			} catch (JsonSyntaxException ignored) {
			}
		});
		button("presets", "Presets", null, "Open", CBHOneConfig::openPresets);
	}

	private void lineLayer(String prefix, String layerName, Function<BlockHighlightConfig, LineConfig> layer, String toggleDescription, BooleanSupplier parent, float minWidth, float maxCut) {
		group("Outline", layerName);
		toggle(prefix + "outlineEnabled", "Enabled", toggleDescription, c -> layer.apply(c).enabled, (c, v) -> layer.apply(c).enabled = v, parent);
		BooleanSupplier enabled = () -> parent.getAsBoolean() && layer.apply(getActiveInstance()).enabled;

		beginAccordion(prefix + "lineColorGroup", "Color");
		colorSetting(prefix + "line", c -> layer.apply(c).color, enabled, 0);

		beginAccordion(prefix + "lineShapeGroup", "Shape");
		dropdown(prefix + "shapeStyle", "Shape Style", "Dictates how the outline shape will be computed.", ShapeStyle.class, c -> layer.apply(c).shapeStyle, (c, v) -> layer.apply(c).shapeStyle = v, enabled);
		dropdown(prefix + "outlineType", "Face Mode", FACE_MODE_DESCRIPTION, FaceMode.class, c -> layer.apply(c).outlineType, (c, v) -> layer.apply(c).outlineType = v,
				() -> enabled.getAsBoolean() && layer.apply(getActiveInstance()).shapeStyle == ShapeStyle.CLASSIC_BOX);
		dropdown(prefix + "lineDepthTest", "Depth Test", DEPTH_TEST_DESCRIPTION, DepthTestMode.class, c -> layer.apply(c).lineDepthTest, (c, v) -> layer.apply(c).lineDepthTest = v, enabled);
		slider(prefix + "lineWidth", "Line Width", null, c -> layer.apply(c).lineWidth, (c, v) -> layer.apply(c).lineWidth = v, minWidth, 15F, 0.1F, enabled);

		beginAccordion(prefix + "lineScalingGroup", "Scaling");
		slider(prefix + "lineExpand", "Adjust Size By (Blocks)", null, c -> layer.apply(c).lineExpandBlocks, (c, v) -> layer.apply(c).lineExpandBlocks = v, -2F, 1F, 0.0625F, enabled);
		slider(prefix + "lineExpandPercent", "Adjust Size By (Percentage)", null, c -> layer.apply(c).lineExpandPercentage, (c, v) -> layer.apply(c).lineExpandPercentage = v, 0F, 2F, 0.01F, enabled);

		beginAccordion(prefix + "lineSubdivisionGroup", "Subdivision");
		slider(prefix + "cutFromCorner", "Cut From Corner", null, c -> layer.apply(c).cutFromCorner, (c, v) -> {
			LineConfig line = layer.apply(c);
			line.cutFromCorner = v;
			if (v + line.cutFromCenter >= 0.95F) line.cutFromCenter = Math.clamp(0.95F - v, 0F, 1F);
		}, 0F, maxCut, 0.01F, enabled);
		slider(prefix + "outerThicknessMult", "Outer Thickness Multiplier", null, c -> layer.apply(c).outerThicknessMult, (c, v) -> layer.apply(c).outerThicknessMult = v, 0F, 2F, 0.05F, enabled);
		slider(prefix + "cutFromCenter", "Cut From Center", null, c -> layer.apply(c).cutFromCenter, (c, v) -> {
			LineConfig line = layer.apply(c);
			line.cutFromCenter = v;
			if (v + line.cutFromCorner >= 0.95F) line.cutFromCorner = Math.clamp(0.95F - v, 0F, 1F);
		}, 0F, maxCut, 0.01F, enabled);
		slider(prefix + "innerThicknessMult", "Inner Thickness Multiplier", null, c -> layer.apply(c).innerThicknessMult, (c, v) -> layer.apply(c).innerThicknessMult = v, 0F, 2F, 0.05F, enabled);
		endAccordion();
	}

	private void colorSetting(String prefix, Function<BlockHighlightConfig, ColorSetting> setting, BooleanSupplier enabled, int minAlpha) {
		BooleanSupplier rainbow = () -> enabled.getAsBoolean() && setting.apply(getActiveInstance()).rainbowSettings.enabled;
		BooleanSupplier staticColors = () -> enabled.getAsBoolean() && !setting.apply(getActiveInstance()).rainbowSettings.enabled;
		color(prefix + "Col", "Primary", c -> setting.apply(c).col1, (c, v) -> setting.apply(c).col1 = v, staticColors);
		color(prefix + "Col2", "Secondary", c -> setting.apply(c).col2, (c, v) -> setting.apply(c).col2 = v, staticColors);
		intSlider(prefix + "Alpha", "Opacity", null, c -> setting.apply(c).alpha, (c, v) -> setting.apply(c).alpha = v, minAlpha, 255, enabled);
		toggle(prefix + "Rainbow", "Rainbow", null, c -> setting.apply(c).rainbowSettings.enabled, (c, v) -> setting.apply(c).rainbowSettings.enabled = v, enabled);
		slider(prefix + "RainbowSpeed", "Speed", null, c -> setting.apply(c).rainbowSettings.speed, (c, v) -> setting.apply(c).rainbowSettings.speed = v, 1F, 10F, 0.1F, rainbow);
		intSlider(prefix + "RainbowDelay", "Delay", "How much to delay the rainbow color used for the secondary part of the gradient.", c -> setting.apply(c).rainbowSettings.delay, (c, v) -> setting.apply(c).rainbowSettings.delay = v, -1000, 1000, rainbow);
		slider(prefix + "Saturation", "Saturation", null, c -> setting.apply(c).rainbowSettings.saturation, (c, v) -> setting.apply(c).rainbowSettings.saturation = v, 0F, 1F, 0.01F, rainbow);
		slider(prefix + "Brightness", "Brightness", null, c -> setting.apply(c).rainbowSettings.brightness, (c, v) -> setting.apply(c).rainbowSettings.brightness = v, 0F, 1F, 0.01F, rainbow);
	}

	private static final String FACE_MODE_DESCRIPTION = "Dictates what faces are visible when looking at a block. Only works with the Bounds mode.";
	private static final String DEPTH_TEST_DESCRIPTION = "Dictates how this element will draw against other terrain. Beware of using this with layered lines, visual issues may occur!";

	private void group(String category, String subcategory) {
		this.category = category;
		this.subcategory = subcategory;
	}

	private void beginAccordion(String id, String title) {
		Tree accordion = new Tree(id, title, null, null);
		accordion.addMetadata("category", category);
		accordion.addMetadata("subcategory", subcategory);
		accordion.addMetadata("collapsed", true);
		tree.put(accordion);
		target = accordion;
	}

	private void endAccordion() {
		target = tree;
	}

	private void toggle(String id, String title, String description, Function<BlockHighlightConfig, Boolean> getter, BiConsumer<BlockHighlightConfig, Boolean> setter, BooleanSupplier enabled) {
		Property<Boolean> property = property(id, title, description, getter, setter, Boolean.class, enabled);
		property.addMetadata("visualizer", Visualizer.SwitchVisualizer.class);
	}

	private void slider(String id, String title, String description, Function<BlockHighlightConfig, Float> getter, BiConsumer<BlockHighlightConfig, Float> setter, float min, float max, float step, BooleanSupplier enabled) {
		Property<Float> property = property(id, title, description, getter, (c, v) -> setter.accept(c, Math.clamp(v, min, max)), Float.class, enabled);
		property.addMetadata("visualizer", Visualizer.SliderVisualizer.class);
		property.addMetadata("min", min);
		property.addMetadata("max", max);
		property.addMetadata("step", step);
	}

	private void intSlider(String id, String title, String description, Function<BlockHighlightConfig, Integer> getter, BiConsumer<BlockHighlightConfig, Integer> setter, int min, int max, BooleanSupplier enabled) {
		Property<Integer> property = property(id, title, description, getter, (c, v) -> setter.accept(c, Math.clamp(v, min, max)), Integer.class, enabled);
		property.addMetadata("visualizer", Visualizer.SliderVisualizer.class);
		property.addMetadata("min", (float) min);
		property.addMetadata("max", (float) max);
		property.addMetadata("step", 1F);
	}

	private void color(String id, String title, Function<BlockHighlightConfig, Color> getter, BiConsumer<BlockHighlightConfig, Color> setter, BooleanSupplier enabled) {
		Property<Color> property = property(id, title, null, getter, setter, Color.class, enabled);
		property.addMetadata("visualizer", Visualizer.ColorVisualizer.class);
		property.addMetadata("noAlpha", true);
	}

	private <E extends Enum<E>> void dropdown(String id, String title, String description, Class<E> type, Function<BlockHighlightConfig, E> getter, BiConsumer<BlockHighlightConfig, E> setter, BooleanSupplier enabled) {
		Property<E> property = property(id, title, description, getter, setter, type, enabled);
		property.addMetadata("visualizer", Visualizer.DropdownVisualizer.class);
		List<E> values = Arrays.asList(type.getEnumConstants());
		property.addMetadata("options", values.stream().map(Enum::toString).toList());
		property.addMetadata("optionValues", values);
		property.addMetadata("optionLabels", values.stream().map(CBHOneConfig::label).toList());
	}

	private void button(String id, String title, String description, String text, Runnable action) {
		Property<Void> property = Properties.dummy(id, title, description);
		property.addMetadata("visualizer", Visualizer.ButtonVisualizer.class);
		property.addMetadata("text", text);
		property.addMetadata("runnable", action);
		property.addMetadata("category", category);
		property.addMetadata("subcategory", subcategory);
		target.put(property);
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
		property.addDisplayCondition(() -> enabled.getAsBoolean() ? Property.Display.SHOWN : Property.Display.DISABLED);
		properties.add(property);
		target.put(property);
		return property;
	}

	private static String label(Enum<?> value) {
		if (value instanceof FaceMode mode) {
			return switch (mode) {
				case ALL -> "All";
				case AIR_EXPOSED -> "Air Exposed Faces";
				case CONCEALED -> "Concealed Faces";
				case LOOKAT -> "Looked At Face";
			};
		}
		if (value instanceof DepthTestMode mode) {
			return switch (mode) {
				case NORMAL -> "Normal";
				case ALWAYS_PASS -> "Always Pass";
				case HIDDEN_ONLY -> "Only Concealed";
			};
		}
		if (value instanceof ShapeStyle style) {
			return switch (style) {
				case CLASSIC_BOX -> "Classic (Bounds)";
				case COLLISION_SHAPE -> "Collision Shape";
				case MODEL_SHAPE -> "Model Shape (Beta)";
			};
		}
		return value.name();
	}
}
*///?}
