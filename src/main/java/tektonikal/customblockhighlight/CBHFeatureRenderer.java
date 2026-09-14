package tektonikal.customblockhighlight;

//? if >=26.2 {
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
import tektonikal.customblockhighlight.config.screenrenderbullshit.CBHFillRenderInfo;
import tektonikal.customblockhighlight.config.screenrenderbullshit.CBHLineRenderInfo;

import java.awt.*;
import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CBH Outline");
	private static final float UNDO_VIEW_SHRINK = 256.0F / 255.0F;

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
            //fill
			VertexConsumer blegh = switch (submit.fillInfo.mode()){
                case NORMAL -> this.getVertexBuilder(RenderTypes.debugQuads());
                case ALWAYS_PASS -> this.getVertexBuilder(Renderer.fillNoDepth);
                case HIDDEN_ONLY -> this.getVertexBuilder(Renderer.fillConcealed);
            };
            AABB box = Shapes.block().move(-0.5F, -0.5F, -0.5F).bounds();
            PoseStack.Pose pose = submit.pose.copy();
            applyExpansion(pose, box, submit.fillInfo.scaleBlocks(), submit.fillInfo.scalePercent());
            Vertexer.vertexBoxQuads(pose, blegh, box.inflate(0.0001), submit.fillInfo.cols(), submit.fillInfo.alphas());

            //lines
			for (CBHLineRenderInfo info : submit.info.reversed()) {
                AABB box2 = info.shape().bounds();
                PoseStack.Pose pose2 = submit.pose.copy();
                pose2.pose().scaleLocal(UNDO_VIEW_SHRINK);
                applyExpansion(pose2, box2, info.scaleBlocks(), info.scalePercent());
				VertexConsumer builder = switch (info.mode()) {
					case NORMAL -> this.getVertexBuilder(RenderTypes.lines());
					case ALWAYS_PASS -> this.getVertexBuilder(Renderer.linesNoDepth);
					case HIDDEN_ONLY -> this.getVertexBuilder(Renderer.linesConcealed);
				};
				Vertexer.vertexBoxLines(pose2, builder, box2, info.cols(), info.alphas(), info.width(), info.cutFromCenter(), info.cutFromCorner(), info.outerMult(), info.innerMult());
			}
		}
	}

	private static void applyExpansion(PoseStack.Pose pose, AABB box, float scaleBlocks, float scalePercent) {
		AABB scaled = box.inflate(scaleBlocks);
		pose.scale((float) (scaled.getXsize() / box.getXsize()), (float) (scaled.getYsize() / box.getYsize()), (float) (scaled.getZsize() / box.getZsize()));
		pose.scale(scalePercent, scalePercent, scalePercent);
	}

	public record Submit(List<CBHLineRenderInfo> info, PoseStack.Pose pose, CBHFillRenderInfo fillInfo) implements SubmitNode {
		@Override
		public FeatureRendererType<Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}
//?}
