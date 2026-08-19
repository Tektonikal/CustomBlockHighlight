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
import net.minecraft.client.renderer.feature.FeatureRendererType;
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
import tektonikal.customblockhighlight.Vertexer;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public class GuiOutlineRenderer extends PictureInPictureRenderer<EvilRenderState> {

	@Override
	public Class<EvilRenderState> getRenderStateClass() {
		return EvilRenderState.class;
	}

	@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		if(renderState.preset() == null){
			return;
		}
		var blockModelResolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
		var blockModelRenderState = new BlockModelRenderState();
			blockModelResolver.update(blockModelRenderState, renderState.preset().block.defaultBlockState(), BlockDisplayContext.create());
		poseStack.translate(0, renderState.y() / renderState.scale(), 0.0D);
		poseStack.translate(renderState.x1() / 3F / renderState.scale(), 0, 0.0D);
		float centerX = (renderState.x1() / 6F) * 5F;
		float centerY = renderState.y1() / 2F;
		float xAngle = (float) Math.atan((centerX - Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow())) / 40.0F);
		float yAngle = (float) Math.atan((centerY - Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow())) / 40.0F);
		//sometimes it just decides to flip around ? and i don't know why?
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
		xRotation.rotateLocalY(-xAngle * 20.0F * (float) (Math.PI / 180.0));
		rotation.mul(xRotation);
		var info = new CBHLineRenderInfo(renderState.preset().renderInfo.shape(), renderState.preset().renderInfo.primaryCol(), renderState.preset().renderInfo.secondaryCol(), renderState.preset().renderInfo.alphas(), renderState.preset().renderInfo.width(), renderState.preset().renderInfo.mode());
		PoseStack.Pose linePose = poseStack.last().copy();
		//world's worst workaround
		linePose.pose().scaleLocal(256.0F / 255.0F);
		linePose.rotate(rotation);
		poseStack.rotateAround(rotation, 0, 0, 0);
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		blockModelRenderState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
		CBHFeatureRenderer.Submit t = new CBHFeatureRenderer.Submit(info, linePose);
		submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, t);
	}

	@Override
	protected float getTranslateY(final int height, final int guiScale) {
		return height / 2F;
	}


	@Override
	protected String getTextureLabel() {
		return "outline";
	}
}
