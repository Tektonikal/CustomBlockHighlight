package tektonikal.customblockhighlight.config.screenrenderbullshit;

//? if >=1.21.8 {
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;

//? if >=26.2 {
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.util.LightCoordsUtil;
import tektonikal.customblockhighlight.CBHFeatureRenderer;
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

	//? if >=26.1 {
	private final BlockModelResolver blockModelResolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
	private final BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
	//?}

	//? if >=26.2 {
	@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		if (renderState.preset() == null) return;
		if (!PresetsScreen.shouldRender(renderState.preset())) {
			return;
		}

		Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);

		blockModelResolver.update(blockModelRenderState, renderState.preset().block.defaultBlockState(), BlockDisplayContext.create());

		Quaternionf rotation = PreviewOutline.rotation(renderState.xAngle(), renderState.yAngle());

		PoseStack.Pose linePose = poseStack.last().copy();
		linePose.rotate(rotation);
		poseStack.rotateAround(rotation, 0, 0, 0);
		poseStack.translate(-0.5F, -0.5F, -0.5F);

		blockModelRenderState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
		CBHFeatureRenderer.Submit t = new CBHFeatureRenderer.Submit(linePose, renderState.preset().config());
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

		poseStack.rotateAround(PreviewOutline.rotation(renderState.xAngle(), renderState.yAngle()), 0, 0, 0);

		poseStack.pushPose();
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		//? if >=26.1 {
		blockModelResolver.update(blockModelRenderState, renderState.preset().block.defaultBlockState(), BlockDisplayContext.create());
		FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
		blockModelRenderState.submit(poseStack, featureRenderDispatcher.getSubmitNodeStorage(), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
		featureRenderDispatcher.renderAllFeatures();
		//?} else {
		/^Minecraft.getInstance().getBlockRenderer().renderSingleBlock(renderState.preset().block.defaultBlockState(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		^///?}
		poseStack.popPose();

		PreviewOutline.draw(poseStack, bufferSource, renderState.preset(), 1);
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
