package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import tektonikal.customblockhighlight.config.screenrenderbullshit.CBHFillRenderInfo;
import tektonikal.customblockhighlight.config.screenrenderbullshit.CBHLineRenderInfo;

import java.awt.*;
import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CBH Outline");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
            //fill
			VertexConsumer blegh = switch (submit.fillInfo.mode()){
                case NORMAL -> this.getVertexBuilder(RenderTypes.debugQuads());
                case ALWAYS_PASS -> this.getVertexBuilder(Renderer.fillNoDepth);
                case HIDDEN_ONLY -> this.getVertexBuilder(Renderer.fillConcealed);
            };
            PoseStack.Pose pose = submit.pose.copy();
            AABB scaled = Shapes.block().move(-0.5F, -0.5F, -0.5F).bounds().inflate(submit.fillInfo.scaleBlocks());
            AABB box = Shapes.block().move(-0.5F, -0.5F, -0.5F).bounds();
            Vector3f boxDim = new Vector3f((float) (scaled.getXsize() / box.getXsize()), (float) (scaled.getYsize() / box.getYsize()), (float) (scaled.getZsize() / box.getZsize()));
            pose.scale(boxDim.x, boxDim.y, boxDim.z);
            pose.scale(submit.fillInfo.scalePercent(), submit.fillInfo.scalePercent(), submit.fillInfo.scalePercent());
            Vertexer.vertexBoxQuads(pose, blegh, Shapes.block().move(-0.5F, -0.5F, -0.5F).bounds().inflate(0.0001 + submit.fillInfo.scaleBlocks()), submit.fillInfo.cols(), submit.fillInfo.alphas());

            //lines
			submit.pose.pose().scaleLocal(256.0F / 255.0F);

			for (CBHLineRenderInfo info : submit.info.reversed()) {
                PoseStack.Pose pose2 = submit.pose.copy();
                AABB scaled2 = info.shape().bounds().inflate(info.scaleBlocks());
                AABB box2 = info.shape().bounds();
                Vector3f boxDim2 = new Vector3f((float) (scaled2.getXsize() / box2.getXsize()), (float) (scaled2.getYsize() / box2.getYsize()), (float) (scaled2.getZsize() / box2.getZsize()));
                pose2.scale(boxDim2.x, boxDim2.y, boxDim2.z);
                pose2.scale(info.scalePercent(), info.scalePercent(), info.scalePercent());
				VertexConsumer builder = switch (info.mode()) {
					case NORMAL -> this.getVertexBuilder(RenderTypes.lines());
					case ALWAYS_PASS -> this.getVertexBuilder(Renderer.linesNoDepth);
					case HIDDEN_ONLY -> this.getVertexBuilder(Renderer.linesConcealed);
				};
				Vertexer.vertexBoxLines(pose2, builder, info.shape().bounds(), info.cols(), info.alphas(), info.width(), info.cutFromCenter(), info.cutFromCorner(), info.outerMult(), info.innerMult());
			}
		}
	}

	public record Submit(List<CBHLineRenderInfo> info, PoseStack.Pose pose, CBHFillRenderInfo fillInfo) implements SubmitNode {
		@Override
		public FeatureRendererType<Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}
