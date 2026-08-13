//? if >=26.2 {
package tektonikal.customblockhighlight.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.Renderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"))
	private void midnigtcontrols$captureMatrices(DeltaTracker deltaTracker, CallbackInfo ci, @Local(ordinal = 0) Matrix4f projectionMatrix, @Local CameraRenderState camState) {
		Renderer.lastProjMat.set(projectionMatrix);
		Renderer.lastModMat.set(RenderSystem.getModelViewMatrixCopy());
		Renderer.lastWorldSpaceMatrix.set(camState.viewRotationMatrix);
	}
}

//?}
