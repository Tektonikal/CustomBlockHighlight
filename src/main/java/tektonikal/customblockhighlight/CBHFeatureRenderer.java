package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tektonikal.customblockhighlight.config.screenrenderbullshit.CBHLineRenderInfo;

import java.awt.*;
import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CBH Outline");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {

			//TODO:
			VertexConsumer blegh = this.getVertexBuilder(RenderTypes.debugQuads());
			Vertexer.vertexBoxQuads(submit.pose, blegh, Shapes.block().move(-0.5F, -0.5F, -0.5F).bounds().inflate(0.0001), Color.BLUE, Color.RED, new float[]{64, 64, 64, 64, 64, 64});
			VertexConsumer builder = switch (submit.info.mode()){
				case NORMAL -> this.getVertexBuilder(RenderTypes.lines());
				case ALWAYS_PASS -> this.getVertexBuilder(RenderType.create("lines_no_depth",
						RenderSetup.builder(Renderer.LINE_NO_DEPTH)
								.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
								.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
								.createRenderSetup()));
				case HIDDEN_ONLY -> this.getVertexBuilder(RenderType.create("lines_concealed",
						RenderSetup.builder(Renderer.LINES_CONCEALED_ONLY)
								.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
								.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
								.createRenderSetup()));
			};
			submit.pose.pose().scaleLocal(256.0F / 255.0F);
			Vertexer.vertexBoxLines(submit.pose, builder, submit.info.shape().bounds(), submit.info().primaryCol(), submit.info().secondaryCol(), submit.info().alphas(), submit.info.width(), submit.info.cutFromCenter(), submit.info.cutFromCorner());
		}
	}

	public record Submit(CBHLineRenderInfo info, PoseStack.Pose pose) implements SubmitNode {
		@Override
		public FeatureRendererType<Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}
