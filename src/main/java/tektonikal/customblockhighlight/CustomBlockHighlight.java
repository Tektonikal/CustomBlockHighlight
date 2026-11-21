package tektonikal.customblockhighlight;

//? if >1.8.9 {
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else
//import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
//?} else {
/*import tektonikal.customblockhighlight.config.CBHOneConfig;
import net.fabricmc.loader.api.FabricLoader;
import tektonikal.customblockhighlight.legacy.LegacyClient;
*///?}
import net.fabricmc.api.ModInitializer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.util.Tweener;
//? if >=26.2
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
//? if >=1.21.8
import tektonikal.customblockhighlight.config.screenrenderbullshit.GuiOutlineRenderer;
//? if >=26.1
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
//? if >=1.21.8 && <26.1
//import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;

public class CustomBlockHighlight implements ModInitializer {
	public static float xAngle, yAngle;
	public static final Tweener xAngleTweener = new Tweener(() -> xAngle, 20);
	public static final Tweener yAngleTweener = new Tweener(() -> yAngle, 20);
	private static long lastFrameNs = System.nanoTime();
	private static long frameTimeNs = 16_666_666L;
	public static int frame;

	@Override
	public void onInitialize() {
		//? if =1.8.9 {
		/*CBHOneConfig.init();
		if (FabricLoader.getInstance().isModLoaded("osl-keybinds")) LegacyClient.registerToggleKey();
		*///?} else {
		BlockHighlightConfig.applyActiveToOptions();
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> !getActiveInstance().enableModRendering || getActiveInstance().drawVanillaOutline);
		//? if >=26.1 {
		KeyMapping toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("cbh.key.toggle", InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
		//?} elif >=1.21.9 {
		/*KeyMapping toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("cbh.key.toggle", InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
		*///?} else
		//KeyMapping toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("cbh.key.toggle", InputConstants.UNKNOWN.getValue(), "key.categories.misc"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) toggleRendering();
		});
		//? if >=26.2
		LevelRenderEvents.COLLECT_SUBMITS.register(Renderer::collectSubmits);
		LevelRenderEvents.END_MAIN.register(Renderer::mainLoop);
		//? if >=26.2
		FeatureRendererRegistry.register(CBHFeatureRenderer.TYPE, CBHFeatureRenderer::new);
		//? if >=26.2 {
		PictureInPictureRendererRegistry.register(ignored -> new GuiOutlineRenderer());
		//?} elif >=26.1 {
		/*PictureInPictureRendererRegistry.register(ctx -> new GuiOutlineRenderer(ctx.bufferSource()));
		*///?} elif >=1.21.8 {
		/*SpecialGuiElementRegistry.register(ctx -> new GuiOutlineRenderer(ctx.vertexConsumers()));
		*///?}
		//?}
	}

	public static void toggleRendering() {
		getActiveInstance().enableModRendering = !getActiveInstance().enableModRendering;
		BlockHighlightConfig.applyActiveToOptions();
		ConfigManager.save();
	}

	public static double ease(double start, double end, float speed) {
		return start + (end - start) * easeFactor(speed);
	}

	public static void onFrame() {
		frame++;
		long now = System.nanoTime();
		frameTimeNs = now - lastFrameNs;
		lastFrameNs = now;
	}

	public static double easeFactor(float speed) {
		return 1 - Math.exp(-((double) frameTimeNs / 1000000000) * speed);
	}

	public static float easeF(double start, double end, float speed) {
		return (float) ease(start, end, speed);
	}

	public static float fadeIn(double value, float target) {
		BlockHighlightConfig cfg = getActiveInstance();
		return cfg.fadeIn ? easeF(value, target, cfg.fadeInSpeed) : target;
	}

	public static float fadeOut(double value) {
		BlockHighlightConfig cfg = getActiveInstance();
		return cfg.fadeOut ? easeF(value, 0, cfg.fadeOutSpeed) : 0;
	}
}