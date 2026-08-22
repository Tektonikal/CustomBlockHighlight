package tektonikal.customblockhighlight.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.io.function.Erase.rethrow;

public class ConfigManager {
	@SuppressWarnings("deprecation")
	public static Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
			.serializeNulls()
			.registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
			.registerTypeAdapter(BlockHighlightConfig.class, (InstanceCreator<BlockHighlightConfig>) _ -> new BlockHighlightConfig())
			.setPrettyPrinting()
			.create();

	private static final Path DEFAULT_PATH = FabricLoader.getInstance().getConfigDir().resolve("blockhighlight.json");

	public static void save() {
		save(BlockHighlightConfig.ACTIVE_INSTANCE);
	}

	public static void save(BlockHighlightConfig instance) {
		String json = GSON.toJson(instance);
		try {
			Files.writeString(DEFAULT_PATH, json);
		} catch (IOException e) {
			throw rethrow(e);
		}
	}

	public static BlockHighlightConfig load() {
		return loadFromFile(DEFAULT_PATH);
	}

	public static BlockHighlightConfig loadPreset(String name) {
		try (var stream = PresetsScreen.class.getResourceAsStream("/assets/presets/" + name + ".json")) {
			if (stream == null) return null;
			return loadFromJsonString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException ignored) {
			return null;
		}
	}

	public static BlockHighlightConfig loadFromFile(Path path) {
		try {
			return loadFromJsonString(Files.readString(path));
		} catch (IOException ignored) {
			return null;
		}
	}

	public static BlockHighlightConfig loadFromJsonString(String json) {
		BlockHighlightConfig config = GSON.fromJson(json, BlockHighlightConfig.class);
		if (config == null) return new BlockHighlightConfig().applyValuesToOptionInstances();
		return config.applyValuesToOptionInstances();
	}

	public static BlockHighlightConfig getPreset(PresetsScreen.Preset preset) {
		try (var stream = PresetsScreen.class.getResourceAsStream("/assets/presets/" + preset.name + ".json")) {
			if (stream == null) return null;
			return GSON.fromJson(new String(stream.readAllBytes(), StandardCharsets.UTF_8), BlockHighlightConfig.class);
		} catch (IOException ignored) {
			return null;
		}
	}
}
