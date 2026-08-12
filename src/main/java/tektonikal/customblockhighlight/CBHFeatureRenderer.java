package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<CBHFeatureRenderer.Submit> TYPE = FeatureRendererType.create("CBH Outline");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		Vector3f normal = new Vector3f();

		for (CBHFeatureRenderer.Submit submit : submits) {
			PoseStack.Pose pose = submit.pose();
			int color = submit.color();
			float width = submit.width();
			VertexConsumer builder = this.getVertexBuilder(submit.renderType());
			submit.shape().forAllEdges((x1, y1, z1, x2, y2, z2) -> {
				normal.set((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
				builder.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color).setNormal(pose, normal).setLineWidth(width);
				builder.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color).setNormal(pose, normal).setLineWidth(width);
			});
		}
	}

	public record Submit(PoseStack.Pose pose, VoxelShape shape, RenderType renderType, int color, float width) implements SubmitNode {
		@Override
		public FeatureRendererType<CBHFeatureRenderer.Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}
