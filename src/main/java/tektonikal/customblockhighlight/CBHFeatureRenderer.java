package tektonikal.customblockhighlight;

//? if >=26.2 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PreviewOutline;
import tektonikal.customblockhighlight.util.BlendMode;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.util.List;

public class CBHFeatureRenderer extends RenderTypeFeatureRenderer<CBHFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CBH Outline");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			if (submit == WORLD) {
				Renderer.drawWorld(this::getVertexBuilder);
				continue;
			}
			PoseStack stack = new PoseStack();
			stack.last().set(submit.pose());
			PreviewOutline.draw(stack, submit.config(), 1, new PreviewOutline.Layers() {
				@Override
				public VertexConsumer begin(DepthTestMode mode, boolean lines, boolean screen, BlendMode blend) {
					if (lines) return getVertexBuilder(RenderBackend.previewLineType(mode, blend));
					return getVertexBuilder(screen ? RenderBackend.screenRenderType(mode, blend) : RenderBackend.worldRenderType(mode, false, blend));
				}

				@Override
				public void end(DepthTestMode mode, boolean lines, BlendMode blend, float width) {
				}
			});
		}
	}

	static void applyExpansion(PoseStack.Pose pose, AABB box, float scaleBlocks, float scalePercent) {
		Vector3f scale = Vertexer.expansionScale(box, scaleBlocks, scalePercent);
		pose.scale(scale.x, scale.y, scale.z);
	}

	public static final Submit WORLD = new Submit(null, null);

	public record Submit(PoseStack.Pose pose, BlockHighlightConfig config) implements SubmitNode {
		@Override
		public FeatureRendererType<Submit> featureType() {
			return CBHFeatureRenderer.TYPE;
		}
	}
}
//?}
