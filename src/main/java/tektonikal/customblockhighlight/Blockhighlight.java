package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.Updatable;

import java.util.Arrays;

public class Blockhighlight implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockHighlightConfig.INSTANCE.load();
		//Classic.
		unleashHell();
		armSecuritySystem();
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, _) -> false);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
	}

	public static void unleashHell() {
		try {
			Arrays.stream(BlockHighlightConfig.class.getDeclaredFields()).filter(field -> field.getName().startsWith("o_") && !field.getName().equals("INSTANCE")).forEach(field -> {
				try {
					((Option) field.get(null)).stateManager().set(BlockHighlightConfig.class.getField(field.getName().replace("o_", "")).get(BlockHighlightConfig.INSTANCE.instance()));
					((Option<?>) field.get(null)).applyValue();
				} catch (IllegalAccessException | NoSuchFieldException _) {
				}
			});
		} catch (SecurityException _) {
		}
	}

	private static void armSecuritySystem() {
		//can't add listeners while options are created for my use-case, since not everything is fully initialized
		Arrays.stream(BlockHighlightConfig.class.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Updatable.class)).forEach(field -> {
			try {
				((Option<Boolean>) field.get(null)).addListener(BlockHighlightConfig::update);
				BlockHighlightConfig.update(((Option<Boolean>) field.get(null)), ((Option<Boolean>) field.get(null)).stateManager().get());
			} catch (Exception _) {
			}
		});
	}

	public static double ease(double start, double end, float speed) {
		return (start + (end - start) * (1 - Math.exp(-(1.0F / Minecraft.getInstance().getFps()) * speed)));
	}
}