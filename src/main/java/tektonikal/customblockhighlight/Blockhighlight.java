package tektonikal.customblockhighlight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

public class Blockhighlight implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockHighlightConfig.INSTANCE.load();
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> false);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
	}
}