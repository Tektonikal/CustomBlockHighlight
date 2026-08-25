package tektonikal.customblockhighlight.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FramerateLimitTracker.class)
public abstract class InactivityFpsLimiterMixin {

	@Shadow
	public abstract FramerateLimitTracker.FramerateThrottleReason getThrottleReason();

	@Shadow
	private int framerateLimit;

	@ModifyReturnValue(method = "getFramerateLimit", at = @At("RETURN"))
	//TODO: make this only apply to our screens
	int yeah(int original) {
		if (getThrottleReason() == FramerateLimitTracker.FramerateThrottleReason.OUT_OF_LEVEL_MENU) {
			return framerateLimit;
		}
		return original;
	}
}
