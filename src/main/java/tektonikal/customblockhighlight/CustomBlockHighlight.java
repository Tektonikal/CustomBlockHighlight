package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionEventListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.util.Mth;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.util.Tweener;
//? if >=26.2
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
//? if >=1.21.8
import tektonikal.customblockhighlight.config.screenrenderbullshit.GuiOutlineRenderer;
//? if >=26.1
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
//? if >=1.21.8 && <26.1
//import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

import static tektonikal.customblockhighlight.Renderer.mc;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;

public class CustomBlockHighlight implements ModInitializer {
	public static float xAngle, yAngle;
	public static final Tweener xAngleTweener = new Tweener(() -> xAngle, 20);
	public static final Tweener yAngleTweener = new Tweener(() -> yAngle, 20);

	@Override
	public void onInitialize() {
		BlockHighlightConfig.ACTIVE_INSTANCE = ConfigManager.load();
		clampTwoOptions(o_cutFromCorner, o_cutFromCenter);
		clampTwoOptions(o_scutFromCenter, o_scutFromCorner);
		clampTwoOptions(o_tcutFromCenter, o_tcutFromCenter);
        BlockHighlightConfig.update(o_shapeStyle, o_shapeStyle.stateManager().get());
        BlockHighlightConfig.update(o_sshapeStyle, o_sshapeStyle.stateManager().get());
        BlockHighlightConfig.update(o_tshapeStyle, o_tshapeStyle.stateManager().get());
        BlockHighlightConfig.update(o_globalModToggle, false);
        BlockHighlightConfig.update(o_globalModToggle, o_globalModToggle.stateManager().get());
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> getActiveInstance().drawVanillaOutline);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
		//? if >=26.2
		FeatureRendererRegistry.register(CBHFeatureRenderer.TYPE, CBHFeatureRenderer::new);
		//? if >=26.2 {
		//noinspection UnstableApiUsage
		PictureInPictureRendererRegistryImpl.register(ignored -> new GuiOutlineRenderer());
		//?} elif >=26.1 {
		/*//noinspection UnstableApiUsage
		PictureInPictureRendererRegistryImpl.register(ctx -> new GuiOutlineRenderer(ctx.bufferSource()));
		*///?} elif >=1.21.8 {
		/*SpecialGuiElementRegistry.register(ctx -> new GuiOutlineRenderer(ctx.vertexConsumers()));
		*///?}
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
		if (mc.options.enableVsync().get() || !getActiveInstance().improvedEasing) {
			return (start + (end - start) * (1 - Math.exp(-(1.0F / mc.getFps()) * speed)));
		}
		return (start + (end - start) * (1 - Math.exp(-((double) mc.getFrameTimeNs() / 1000000000) * speed)));
	}

	public static float easeF(double start, double end, float speed) {
		return (float) ease(start, end, speed);
	}
}