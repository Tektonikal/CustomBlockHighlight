package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.shapes.VoxelShape;
import tektonikal.customblockhighlight.config.screenrenderbullshit.CBHLineRenderInfo;

import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<CBHFeatureRenderer.Submit> TYPE = FeatureRendererType.create("CBH Outline");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (CBHFeatureRenderer.Submit submit : submits) {
			PoseStack.Pose pose = submit.pose();
			VertexConsumer builder = this.getVertexBuilder(submit.renderType());
			Vertexer.vertexBoxLines(pose, builder, submit.shape().bounds(), submit.info().primaryCol(), submit.info().secondaryCol(), submit.info().alphas(), submit.info.width(), 0, 0);
		}
	}

	public record Submit(CBHLineRenderInfo info, PoseStack.Pose pose, VoxelShape shape, RenderType renderType, int color, float width) implements SubmitNode {
		@Override
		public FeatureRendererType<CBHFeatureRenderer.Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}
