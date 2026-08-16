package tektonikal.customblockhighlight.config.screenrenderbullshit;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.feature.ShapeOutlineFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Quaternionf;
import tektonikal.customblockhighlight.CBHFeatureRenderer;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public class GuiOutlineRenderer extends PictureInPictureRenderer<EvilRenderState> {

	@Override
	public Class<EvilRenderState> getRenderStateClass() {
		return EvilRenderState.class;
	}

	@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {

		var blockModelResolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
		var blockModelRenderState = new BlockModelRenderState();
		blockModelResolver.update(blockModelRenderState, Blocks.CONCRETE.green().defaultBlockState(), BlockDisplayContext.create());

		blockModelRenderState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
		var info = new CBHLineRenderInfo(Renderer.easeBox, Color.RED, Color.BLUE, new float[]{255, 255, 255, 255, 255, 255}, 1, DepthTestMode.ALWAYS_PASS);
		CBHFeatureRenderer.Submit t = new CBHFeatureRenderer.Submit(info, poseStack.last().copy(), Shapes.block(), RenderTypes.lines(), -1, 1);
		submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, t);
	}

	@Override
	protected float getTranslateY(final int height, final int guiScale) {
		return 0;
	}


	@Override
	protected String getTextureLabel() {
		return "outline";
	}
}
