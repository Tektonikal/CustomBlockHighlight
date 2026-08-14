package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import net.fabricmc.api.ModInitializer;
//? if >=26.2 {
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
//?}
//? if >=26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
//?} else
//import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
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
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hit) -> false);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
		//? if >=26.2 {
		FeatureRendererRegistry.register(CBHFeatureRenderer.TYPE, CBHFeatureRenderer::new);
		PictureInPictureRendererRegistry.register(ctx -> new GuiOutlineRenderer());
		//?} elif >=26.1 {
		/*PictureInPictureRendererRegistry.register(ctx -> new GuiOutlineRenderer(ctx.bufferSource()));
		*///?} else {
		/*SpecialGuiElementRegistry.register(ctx -> new GuiOutlineRenderer(ctx.vertexConsumers()));
		*///?}
	}

	public static void unleashHell() {
		try {
			Arrays.stream(BlockHighlightConfig.class.getDeclaredFields()).filter(field -> field.getName().startsWith("o_") && !field.getName().equals("INSTANCE")).forEach(field -> {
				try {
					//noinspection unchecked, rawtypes
					((Option) field.get(null)).stateManager().set(BlockHighlightConfig.class.getField(field.getName().replace("o_", "")).get(BlockHighlightConfig.config()));
					((Option<?>) field.get(null)).applyValue();
				} catch (IllegalAccessException | NoSuchFieldException ignored) {
				}
			});
		} catch (SecurityException ignored) {
		}
	}

	private static void armSecuritySystem() {
		// can't add listeners while options are created for my use-case, since not everything is fully initialized
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
