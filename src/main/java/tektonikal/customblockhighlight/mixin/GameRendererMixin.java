package tektonikal.customblockhighlight.mixin;

//? if >1.8.9 {
//? if >=26.1 {
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?}
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.Renderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
}
//?} else {
/*import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.legacy.LegacyClient;
import tektonikal.customblockhighlight.legacy.LevelRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "render(FJ)V", at = @At("HEAD"))
	private void cbh$onFrame(float tickDelta, long startTime, CallbackInfo ci) {
		LegacyClient.onFrame(tickDelta);
	}

	@Inject(method = "render(IFJ)V", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;popPush(Ljava/lang/String;)V", args = "ldc=hand"))
	private void cbh$renderOutline(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		Renderer.mainLoop(new LevelRenderContext(new PoseStack()));
	}
}
*///?}
