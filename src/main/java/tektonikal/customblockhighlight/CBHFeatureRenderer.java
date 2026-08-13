//? if >=26.2 {
package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<CBHFeatureRenderer.Submit> TYPE = FeatureRendererType.create("CBH Outline");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (CBHFeatureRenderer.Submit submit : submits) {
			Vertexer.vertexShapeEdges(submit.pose(), this.getVertexBuilder(submit.renderType()), submit.shape(), submit.color(), submit.width());
		}
	}

	public record Submit(PoseStack.Pose pose, VoxelShape shape, RenderType renderType, int color, float width) implements SubmitNode {
		@Override
		public FeatureRendererType<CBHFeatureRenderer.Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}

//?}
