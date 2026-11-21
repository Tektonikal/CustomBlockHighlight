package tektonikal.customblockhighlight.mixin;

//? if >=26.2 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.ShapeOutlineFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import tektonikal.customblockhighlight.Renderer;

import java.util.List;

@Mixin(ShapeOutlineFeatureRenderer.class)
public abstract class ShapeOutlineFeatureRendererMixin extends RenderTypeFeatureRenderer<ShapeOutlineFeatureRenderer.Submit> {
	@WrapMethod(method = "buildGroup")
	private void cbh$restyle(FeatureFrameContext context, List<ShapeOutlineFeatureRenderer.Submit> submits, Operation<Void> original) {
		if (!Renderer.restylesShapeOutlines()) {
			original.call(context, submits);
			return;
		}
		for (ShapeOutlineFeatureRenderer.Submit submit : submits) {
			Renderer.drawShapeOutline(submit.pose(), submit.shape(), this::getVertexBuilder);
		}
	}
}
//?}
