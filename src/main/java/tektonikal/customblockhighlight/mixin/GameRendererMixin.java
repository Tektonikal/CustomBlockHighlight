package tektonikal.customblockhighlight.mixin;

//? if >1.8.9 {
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.CustomBlockHighlight;
import tektonikal.customblockhighlight.Renderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void cbh$onFrame(CallbackInfo ci) {
		CustomBlockHighlight.onFrame();
	}

	//? if >=26.3 {
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"))
	//?} elif >=26.1 {
	/*@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
	*///?} elif >=1.21.6 {
	/*@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PerspectiveProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
	*///?} elif >=1.21.2 {
	/*@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/ProjectionType;)V"), index = 0)
	*///?} else
	//@ModifyArg(method = "resetProjectionMatrix", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexSorting;)V"), index = 0)
	private Matrix4f cbh$captureProjection(Matrix4f projection) {
		Renderer.lastProjMat.set(projection);
		return projection;
	}
}
//?} else {
/*import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.CustomBlockHighlight;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.legacy.LegacyClient;
import tektonikal.customblockhighlight.legacy.LevelRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "render(FJ)V", at = @At("HEAD"))
	private void cbh$onFrame(float tickDelta, long startTime, CallbackInfo ci) {
		LegacyClient.onFrame(tickDelta);
		CustomBlockHighlight.onFrame();
		while (LegacyClient.toggleKeyPressed()) CustomBlockHighlight.toggleRendering();
	}

	@Inject(method = "render(IFJ)V", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;popPush(Ljava/lang/String;)V", args = "ldc=hand"))
	private void cbh$renderOutline(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		Renderer.mainLoop(new LevelRenderContext(new PoseStack()));
	}
}
*///?}
