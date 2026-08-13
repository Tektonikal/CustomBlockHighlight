package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
//? if >=26.2 {
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.renderer.SubmitNodeCollector;
//?} else
//import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

public class GuiOutlineRenderer extends PictureInPictureRenderer<EvilRenderState> {
	private static final VoxelShape OUTLINE = Shapes.block().move(-0.5F, -0.5F, -0.5F);
	private static final int COLOR = -1;
	private static final float WIDTH = 5;

	//? if <26.2 {
	/*public GuiOutlineRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}
	*///?}

	@Override
	public Class<EvilRenderState> getRenderStateClass() {
		return EvilRenderState.class;
	}

	//? if >=26.2 {
	@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		prepareMatrices(renderState, poseStack);
		CBHFeatureRenderer.Submit t = new CBHFeatureRenderer.Submit(poseStack.last(), OUTLINE, RenderTypes.lines(), COLOR, WIDTH);
		submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, t);
	}
	//?} else {
	/*@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack) {
		prepareMatrices(renderState, poseStack);
		Vertexer.vertexShapeEdges(poseStack.last(), bufferSource.getBuffer(RenderTypes.lines()), OUTLINE, COLOR, WIDTH);
		bufferSource.endBatch();
	}
	*///?}

	private static void prepareMatrices(EvilRenderState renderState, PoseStack poseStack) {
		poseStack.translate(0, -renderState.y1() / 2F / renderState.scale(), 0.0D);
		poseStack.rotateAround(new Quaternionf().rotateYXZ((float) (Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow()) / renderState.x1() * 4), 10, 0), 0, 0, 0);
	}

	@Override
	protected String getTextureLabel() {
		return "outline";
	}
}
