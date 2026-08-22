package tektonikal.customblockhighlight.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.ShapeOutlineFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.Vertexer;

import java.awt.*;
import java.util.List;

@Mixin(ShapeOutlineFeatureRenderer.class)
public class ShapeOutlineFeatureRendererMixin {
//	@WrapMethod(method = "buildGroup")
//	void yeah(FeatureFrameContext context, List<ShapeOutlineFeatureRenderer.Submit> submits, Operation<Void> original) {
//		submits.forEach(submit -> {
			//TODO
//			Renderer.drawEdgeOutline(submit.pose(), submit.shape(), Color.WHITE, Color.BLACK, 1, 0);
//		});
//	}
}
