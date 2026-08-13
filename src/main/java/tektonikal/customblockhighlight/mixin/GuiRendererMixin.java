//? if <26.1 {
/*package tektonikal.customblockhighlight.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tektonikal.customblockhighlight.GuiOutlineRenderer;

import java.util.ArrayList;
import java.util.List;

//fabric api has no picture-in-picture renderer registry on 1.21.11, so sneak ours into the vanilla list
@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, index = 5)
	private List<PictureInPictureRenderer<?>> customblockhighlight$addOutlineRenderer(List<PictureInPictureRenderer<?>> renderers, @Local(argsOnly = true) MultiBufferSource.BufferSource bufferSource) {
		List<PictureInPictureRenderer<?>> yeah = new ArrayList<>(renderers);
		yeah.add(new GuiOutlineRenderer(bufferSource));
		return yeah;
	}
}
*///?}
