package tektonikal.customblockhighlight.mixin;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.customblockhighlight.CustomBlockHighlight;
import tektonikal.customblockhighlight.config.screenrenderbullshit.EvilRenderState;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;

import static tektonikal.customblockhighlight.CustomBlockHighlight.xAngleTweener;
import static tektonikal.customblockhighlight.CustomBlockHighlight.yAngleTweener;

@Mixin(YACLScreen.class)
public abstract class YACLScreenMixin{
	@Inject(method = "extractBackground", at = @At("HEAD"))
	void yeah(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci){
		//I have a bone to pick with whoever made FAPI screen events.
		CustomBlockHighlight.xAngle = (float) Math.atan((((guiGraphics.guiWidth() / 6F) * 5F) - Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow())) / 40.0F);
		CustomBlockHighlight.yAngle = (float) Math.atan(((guiGraphics.guiHeight() / 2F) - Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow())) / 40.0F);
		xAngleTweener.update(); yAngleTweener.update();
		guiGraphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(0, 0, xAngleTweener.getF(), yAngleTweener.getF(), PresetsScreen.Preset.CLASSIC, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 100F, null));
		guiGraphics.nextStratum();
	}
}
