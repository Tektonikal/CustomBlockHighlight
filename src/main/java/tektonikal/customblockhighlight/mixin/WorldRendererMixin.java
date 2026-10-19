package tektonikal.customblockhighlight.mixin;

//? if =1.8.9 {
/*import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void cbh$hideVanillaOutline(PlayerEntity camera, HitResult hit, int mode, float tickDelta, CallbackInfo ci) {
		if (!BlockHighlightConfig.getActiveInstance().drawVanillaOutline) ci.cancel();
	}
}
*///?}
