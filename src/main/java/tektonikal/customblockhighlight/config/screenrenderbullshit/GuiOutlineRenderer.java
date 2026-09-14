package tektonikal.customblockhighlight.config.screenrenderbullshit;

//? if >=1.21.8 {
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;

//? if >=26.2 {
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.util.LightCoordsUtil;
import tektonikal.customblockhighlight.CBHFeatureRenderer;

import java.util.List;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
//? if >=26.1 {
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.util.LightCoordsUtil;
//?} else {
/^import net.minecraft.client.renderer.LightTexture;
^///?}
*///?}

public class GuiOutlineRenderer extends PictureInPictureRenderer<EvilRenderState> {
	//? if <26.2 {
	/*public GuiOutlineRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}
	*///?}

	@Override
	public Class<EvilRenderState> getRenderStateClass() {
		return EvilRenderState.class;
	}

	private static Quaternionf previewRotation(EvilRenderState renderState) {
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf xRotation = new Quaternionf().rotateX(renderState.yAngle() * 30.0F * (float) (Math.PI / 180.0));
		xRotation.rotateLocalY(-renderState.xAngle() * 30.0F * (float) (Math.PI / 180.0));
		rotation.mul(xRotation);
		return rotation;
	}

	//? if >=26.2 {
	@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		if (renderState.preset() == null) return;
		if (!PresetsScreen.shouldRender(renderState.preset())) {
			return;
		}

		Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);

		var blockModelResolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
		var blockModelRenderState = new BlockModelRenderState();
		blockModelResolver.update(blockModelRenderState, renderState.preset().block.defaultBlockState(), BlockDisplayContext.create());

		Quaternionf rotation = previewRotation(renderState);

		PoseStack.Pose linePose = poseStack.last().copy();
		//world's worst workaround
		linePose.rotate(rotation);
		poseStack.rotateAround(rotation, 0, 0, 0);
		poseStack.translate(-0.5F, -0.5F, -0.5F);

		blockModelRenderState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        Pair<List<CBHLineRenderInfo>, CBHFillRenderInfo> info = renderState.preset().renderInfo.get();
        CBHFeatureRenderer.Submit t = new CBHFeatureRenderer.Submit(info.first(), linePose, info.right());
		submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, t);
	}
	//?} else {
	
	/*@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack) {
		if (renderState.preset() == null) return;
		if (!PresetsScreen.shouldRender(renderState.preset())) {
			return;
		}

		Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);

		poseStack.rotateAround(previewRotation(renderState), 0, 0, 0);

		poseStack.pushPose();
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		//? if >=26.1 {
		BlockModelResolver blockModelResolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
		BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
		blockModelResolver.update(blockModelRenderState, renderState.preset().block.defaultBlockState(), BlockDisplayContext.create());
		FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
		blockModelRenderState.submit(poseStack, featureRenderDispatcher.getSubmitNodeStorage(), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
		featureRenderDispatcher.renderAllFeatures();
		//?} else {
		/^Minecraft.getInstance().getBlockRenderer().renderSingleBlock(renderState.preset().block.defaultBlockState(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		^///?}
		poseStack.popPose();

		PreviewOutline.draw(poseStack, bufferSource, renderState.preset());
	}
	*///?}

	@Override
	protected float getTranslateY(final int height, final int guiScale) {
		return height / 2F;
	}

	@Override
	protected String getTextureLabel() {
		return "outline";
	}
}
//?}
