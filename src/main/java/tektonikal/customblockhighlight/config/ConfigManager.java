package tektonikal.customblockhighlight.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
//? if >1.8.9 {
import net.minecraft.client.gui.components.toasts.SystemToast;
//?} else
//import org.polyfrost.oneconfig.api.notifications.v1.Notifications;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
import tektonikal.customblockhighlight.util.ShapeStyle;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
	private ConfigManager() {
	}

	private static final Logger LOGGER = LogManager.getLogger("CustomBlockHighlight");

	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
			.serializeNulls()
			.registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
			.setPrettyPrinting()
			.create();

	private static final Path DEFAULT_PATH = FabricLoader.getInstance().getConfigDir().resolve("custom-block-highlight.json");

	public static void save() {
		try {
			Files.writeString(DEFAULT_PATH, GSON.toJson(BlockHighlightConfig.ACTIVE_INSTANCE));
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}

	public static BlockHighlightConfig load() {
		return loadFromFile(DEFAULT_PATH);
	}

	public static BlockHighlightConfig copy(BlockHighlightConfig config) {
		return fromJson(GSON.toJsonTree(config).getAsJsonObject());
	}

	public static BlockHighlightConfig loadFromFile(Path path) {
		try {
			if (Files.notExists(path)) Files.writeString(path, GSON.toJson(new BlockHighlightConfig()));
			BlockHighlightConfig config = fromJson(GSON.fromJson(Files.readString(path), JsonObject.class));
			if (config != null) return config;
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Failed to read config {}, using defaults", path, e);
		}
		return new BlockHighlightConfig();
	}

	public static boolean loadFromClipboard(String text) {
		BlockHighlightConfig config;
		try {
			config = fromJson(GSON.fromJson(text, JsonObject.class));
		} catch (RuntimeException e) {
			return false;
		}
		if (config == null) return false;
		BlockHighlightConfig.setActive(config);
		save();
		return true;
	}

	//? if >1.8.9
	private static final SystemToast.SystemToastId CLIPBOARD_TOAST = new SystemToast.SystemToastId();

	public static void loadFromClipboardWithNotice(String text) {
		boolean loaded = loadFromClipboard(text);
		Component title = Component.translatable("cbh.config.title");
		Component message = Component.translatable(loaded ? "cbh.config.load_from_clipboard.loaded" : "cbh.config.load_from_clipboard.failed");
		//? if >=26.2 {
		SystemToast.addOrUpdate(Minecraft.getInstance().gui.toastManager(), CLIPBOARD_TOAST, title, message);
		//?} elif >=1.21.2 {
		/*SystemToast.addOrUpdate(Minecraft.getInstance().getToastManager(), CLIPBOARD_TOAST, title, message);
		*///?} elif >1.8.9 {
		/*SystemToast.addOrUpdate(Minecraft.getInstance().getToasts(), CLIPBOARD_TOAST, title, message);
		*///?} else {
		/*
		// no toasts before 1.12. use oneconfig
		if (loaded) Notifications.success(title.getString(), message.getString());
		else Notifications.error(title.getString(), message.getString());
		*///?}
	}

	private static BlockHighlightConfig fromJson(JsonObject json) {
		if (json == null) return null;
		BlockHighlightConfig config = GSON.fromJson(json, BlockHighlightConfig.class);
		if (!json.has("fillShape")) config.fillShape = ShapeStyle.CLASSIC_BOX;
		if (!json.has("crystalHelperOnlyWhenHolding")) config.crystalHelperOnlyWhenHolding = false;
		if (!json.has("scaleOutSpeed")) config.scaleOutSpeed = config.scaleSpeed;
		if (!json.has("lineThicknessOutSpeed")) config.lineThicknessOutSpeed = config.lineThicknessAnimationSpeed;
		if (!json.has("lineCutOutSpeed")) config.lineCutOutSpeed = config.lineCutSpeed;
		if (!json.has("scaleOut")) config.scaleOut = config.scale;
		if (!json.has("lineThicknessOut")) config.lineThicknessOut = config.animateLineThickness;
		if (!json.has("lineCutOut")) config.lineCutOut = config.animateLineCuts;
		return config.fixNulls();
	}

	public static BlockHighlightConfig readBuiltInPreset(String name) {
		try (var stream = PresetsScreen.class.getResourceAsStream("/assets/customblockhighlight/presets/" + name + ".json")) {
			if (stream == null) throw new IOException("missing bundled preset " + name);
			BlockHighlightConfig config = fromJson(GSON.fromJson(new String(stream.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class));
			if (config != null) return config;
		} catch (IOException | JsonParseException e) {
			LOGGER.error("Failed to read preset {}", name, e);
		}
		return new BlockHighlightConfig();
	}
}
