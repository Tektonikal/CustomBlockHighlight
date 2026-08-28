package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionEventListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.config.screenrenderbullshit.GuiOutlineRenderer;

import static tektonikal.customblockhighlight.Renderer.mc;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.o_cutFromCenter;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.o_cutFromCorner;

//           this ↓ should be capitalized.
public class Blockhighlight implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockHighlightConfig.ACTIVE_INSTANCE = ConfigManager.load();
        clampTwoOptions(o_cutFromCorner, o_cutFromCenter);
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, _) -> false);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
		FeatureRendererRegistry.register(CBHFeatureRenderer.TYPE, CBHFeatureRenderer::new);
		//noinspection UnstableApiUsage
		PictureInPictureRendererRegistryImpl.register(_ -> new GuiOutlineRenderer());

	}

    public void clampTwoOptions(Option<Float> first, Option<Float> second) {
        first.addEventListener((option, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                if (option.pendingValue() + second.pendingValue() >= 0.95) {
                        second.requestSet(Mth.clamp(second.pendingValue(), 0, 0.95F - option.pendingValue()));
                }
            }
        });
        second.addEventListener((option, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                if (option.pendingValue() + first.pendingValue() >= 0.95) {
                        first.requestSet(Mth.clamp(first.pendingValue(), 0, 0.95F - option.pendingValue()));
                }
            }
        });
    }

    public static double ease(double start, double end, float speed) {
		return (start + (end - start) * (1 - Math.exp(-((double) mc.getFrameTimeNs() / 1000000000) * speed)));
	}
	public static float easeF(double start, double end, float speed) {
		return (float) ease(start, end, speed);
	}
}