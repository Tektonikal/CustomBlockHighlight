package tektonikal.customblockhighlight.mixin;

//? if >=26.3 {
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.RenderBackend;

@Mixin(LevelRenderer.class)
public class OitDepthMixin {
	@Inject(method = "executeOit", at = @At("HEAD"))
	private void cbh$copyOpaqueDepth(CallbackInfo ci) {
		RenderBackend.copyOpaqueDepth();
	}
}
//?}
