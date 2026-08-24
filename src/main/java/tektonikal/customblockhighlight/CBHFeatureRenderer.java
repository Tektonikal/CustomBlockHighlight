package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.*;
import tektonikal.customblockhighlight.config.screenrenderbullshit.CBHLineRenderInfo;

import java.awt.*;
import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CBH Outline");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			VertexConsumer blegh = this.getVertexBuilder(RenderTypes.debugQuads());
			Vertexer.vertexBoxQuads(submit.pose, blegh, submit.info.getFirst().shape().bounds().inflate(0.0001), Color.BLUE, Color.RED, new float[]{64, 64, 64, 64, 64, 64});
			submit.pose.pose().scaleLocal(256.0F / 255.0F);
			for (CBHLineRenderInfo info : submit.info.reversed()) {
				VertexConsumer builder = switch (info.mode()) {
					case NORMAL -> this.getVertexBuilder(RenderTypes.lines());
					case ALWAYS_PASS -> this.getVertexBuilder(Renderer.linesNoDepth);
					case HIDDEN_ONLY -> this.getVertexBuilder(Renderer.linesConcealed);
				};
				Vertexer.vertexBoxLines(submit.pose, builder, info.shape().bounds(), info.primaryCol(), info.secondaryCol(), info.alphas(), info.width(), info.cutFromCenter(), info.cutFromCorner(), 1, 1);
			}
		}
	}

	public record Submit(List<CBHLineRenderInfo> info, PoseStack.Pose pose) implements SubmitNode {
		public Submit(CBHLineRenderInfo info, PoseStack.Pose pose) {
			this(List.of(info), pose);
		}

		@Override
		public FeatureRendererType<Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}
