package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.Updatable;

import java.util.Arrays;

//           this ↓ should be capitalized.
public class Blockhighlight implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockHighlightConfig.INSTANCE.load();
		armSecuritySystem();
		unleashHell();
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((yeah, yah) -> false);
		WorldRenderEvents.END_MAIN.register(Renderer::mainLoop);
	}
	public static void unleashHell() {
		try {
			Arrays.stream(BlockHighlightConfig.class.getDeclaredFields()).filter(field -> field.getName().startsWith("o_") && !field.getName().equals("INSTANCE")).forEach(field -> {
				try {
					((Option) field.get(null)).stateManager().set(BlockHighlightConfig.class.getField(field.getName().replace("o_", "")).get(BlockHighlightConfig.INSTANCE.instance()));
					((Option<?>) field.get(null)).applyValue();
				} catch (IllegalAccessException | NoSuchFieldException ignored) {
				}
			});
		} catch (SecurityException ignored) {
		}
	}

	private static void armSecuritySystem() {
		//can't add listeners while options are created for my use-case, since not everything is fully initialized
		// actually you're just stupid
		Arrays.stream(BlockHighlightConfig.class.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Updatable.class))
				.forEach(field -> {
					try {
						//noinspection unchecked
						Option<Boolean> option = (Option<Boolean>) field.get(null);
						//noinspection deprecation yacl sucks yo
			            option.addListener(BlockHighlightConfig::update);
			            BlockHighlightConfig.update(option, option.stateManager().get());
					} catch (IllegalAccessException ignored) {
					}
				});
	}

	public static double ease(double start, double end, float speed) {
		return (start + (end - start) * (1 - Math.exp(-(1.0F / Minecraft.getInstance().getFps()) * speed)));
	}
}