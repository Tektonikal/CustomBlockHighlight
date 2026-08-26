package tektonikal.customblockhighlight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.config.screenrenderbullshit.GuiOutlineRenderer;

//           this ↓ should be capitalized.
public class Blockhighlight implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockHighlightConfig.ACTIVE_INSTANCE = ConfigManager.load();
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, _) -> false);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
		FeatureRendererRegistry.register(CBHFeatureRenderer.TYPE, CBHFeatureRenderer::new);
		//noinspection UnstableApiUsage
		PictureInPictureRendererRegistryImpl.register(_ -> new GuiOutlineRenderer());

	}

	public static double ease(double start, double end, float speed) {
		return (start + (end - start) * (1 - Math.exp(-(1.0F / Minecraft.getInstance().getFps()) * speed)));
	}
	public static float easeF(double start, double end, float speed) {
		return (float) ease(start, end, speed);
	}
}