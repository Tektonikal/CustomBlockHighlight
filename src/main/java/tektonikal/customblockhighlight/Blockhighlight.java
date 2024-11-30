package tektonikal.customblockhighlight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

public class Blockhighlight implements ModInitializer {
    @Override
    public void onInitialize() {
        BlockHighlightConfig.INSTANCE.load();
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(Renderer::main);
        BlockTargetCallback.EVENT.register(Renderer::update);
    }
}