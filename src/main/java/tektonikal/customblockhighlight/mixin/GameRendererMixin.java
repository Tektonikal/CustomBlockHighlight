package tektonikal.customblockhighlight.mixin;

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
