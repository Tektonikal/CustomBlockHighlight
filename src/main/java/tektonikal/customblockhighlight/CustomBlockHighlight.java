package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionEventListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.minecraft.util.Mth;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.config.screenrenderbullshit.GuiOutlineRenderer;

import static tektonikal.customblockhighlight.Renderer.mc;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;

//           this ↓ should be capitalized.
public class CustomBlockHighlight implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockHighlightConfig.ACTIVE_INSTANCE = ConfigManager.load();
		clampTwoOptions(o_cutFromCorner, o_cutFromCenter);
		clampTwoOptions(o_scutFromCenter, o_scutFromCorner);
		clampTwoOptions(o_tcutFromCenter, o_tcutFromCenter);
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, _) -> false);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
		FeatureRendererRegistry.register(CBHFeatureRenderer.TYPE, CBHFeatureRenderer::new);
		//noinspection UnstableApiUsage
		PictureInPictureRendererRegistryImpl.register(_ -> new GuiOutlineRenderer());

	}

	public void clampTwoOptions(Option<Float> first, Option<Float> second) {
		yah(second, first);
		yah(first, second);
	}

	public void yah(Option<Float> first, Option<Float> second) {
		second.addEventListener((option, event) -> {
			if (event == OptionEventListener.Event.STATE_CHANGE) {
				if (option.pendingValue() + first.pendingValue() >= 0.95) {
					first.requestSet(Mth.clamp(first.pendingValue(), 0, Math.clamp(0.95F - option.pendingValue(), 0, 1)));
				}
			}
		});
	}

	public static double ease(double start, double end, float speed) {
		//TODO: vsync lied to me
		if (mc.options.enableVsync().get()) {
			return (start + (end - start) * (1 - Math.exp(-(1.0F / mc.getFps()) * speed)));
		}
		return (start + (end - start) * (1 - Math.exp(-((double) mc.getFrameTimeNs() / 1000000000) * speed)));
	}

	public static float easeF(double start, double end, float speed) {
		return (float) ease(start, end, speed);
	}
}