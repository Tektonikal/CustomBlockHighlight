package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.util.Arrays;

public class Blockhighlight implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockHighlightConfig.INSTANCE.load();
		unleashHell();
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, _) -> false);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
	}
	public static void unleashHell() {
		try {
			Arrays.stream(BlockHighlightConfig.class.getDeclaredFields()).filter(field -> field.getName().startsWith("o_") && !field.getName().equals("INSTANCE")).forEach(field -> {
				try {
					((Option) field.get(null)).requestSet(BlockHighlightConfig.class.getField(field.getName().replace("o_", "")).get(BlockHighlightConfig.INSTANCE.instance()));
				} catch (IllegalAccessException | NoSuchFieldException _) {
				}
			});
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}