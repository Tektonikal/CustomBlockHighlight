package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.gui.YACLScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.config.screenrenderbullshit.EvilRenderState;
import tektonikal.customblockhighlight.config.screenrenderbullshit.GuiOutlineRenderer;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
import tektonikal.customblockhighlight.util.Tweener;

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
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, _) -> getActiveInstance().drawVanillaOutline);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
		FeatureRendererRegistry.register(CBHFeatureRenderer.TYPE, CBHFeatureRenderer::new);
		//noinspection UnstableApiUsage
		PictureInPictureRendererRegistryImpl.register(_ -> new GuiOutlineRenderer());
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if(screen instanceof YACLScreen yaclScreen && yaclScreen.config.title().equals(Component.translatable("cbh.config.title"))){
				ScreenEvents.afterExtract(screen).register((screen1, graphics, mouseX, mouseY, tickProgress) -> {
					graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(0, 0, xAngleTweener.getF(), yAngleTweener.getF(), PresetsScreen.Preset.CLASSIC, 0, 0, scaledWidth, scaledHeight, 100F, null));
				});
			}
		});
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