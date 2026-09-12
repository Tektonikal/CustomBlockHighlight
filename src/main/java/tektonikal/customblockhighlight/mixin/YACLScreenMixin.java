package tektonikal.customblockhighlight.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.tab.TabExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tektonikal.customblockhighlight.CustomBlockHighlight;
import tektonikal.customblockhighlight.config.screenrenderbullshit.PresetsScreen;
//? if >=1.21.8
import tektonikal.customblockhighlight.config.screenrenderbullshit.EvilRenderState;

import static tektonikal.customblockhighlight.CustomBlockHighlight.xAngleTweener;
import static tektonikal.customblockhighlight.CustomBlockHighlight.yAngleTweener;

@Mixin(YACLScreen.class)
public abstract class YACLScreenMixin extends Screen {
	@Shadow
	@Final
	public YetAnotherConfigLib config;

	@Shadow
	@Final
	public TabManager tabManager;

	protected YACLScreenMixin(Component title) {
		super(title);
	}

	@WrapMethod(method = "extractBackground")
	void yeah(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		if (!config.title().equals(Component.translatable("cbh.config.title"))) {
			original.call(guiGraphics, mouseX, mouseY, partialTick);
		}
		if (tabManager.getCurrentTab() instanceof TabExt tab) {
			tab.renderBackground(guiGraphics);
		}
		//I have a bone to pick with whoever made FAPI screen events.
		//? if >=1.21.8 {
		CustomBlockHighlight.xAngle = (float) Math.atan((((guiGraphics.guiWidth() / 6F) * 5F) - Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow())) / 40.0F);
		CustomBlockHighlight.yAngle = (float) Math.atan(((guiGraphics.guiHeight() / 2F) - Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow())) / 40.0F);
		//?} else {
		/*CustomBlockHighlight.xAngle = (float) Math.atan((((guiGraphics.guiWidth() / 6F) * 5F) - Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth()) / 40.0F);
		CustomBlockHighlight.yAngle = (float) Math.atan(((guiGraphics.guiHeight() / 2F) - Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight()) / 40.0F);
		*///?}
		xAngleTweener.update();
		yAngleTweener.update();
		//? if >=1.21.8 {
		if (PresetsScreen.shouldRender(PresetsScreen.Preset.CURRENT_CONFIG)) {
			int centerX = (int) ((guiGraphics.guiWidth() / 6F) * 5F);
			int centerY = guiGraphics.guiHeight() / 2;
			guiGraphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(xAngleTweener.getF(), yAngleTweener.getF(), PresetsScreen.Preset.CURRENT_CONFIG, centerX - 100, centerY - 100, centerX + 100, centerY + 100, 75, null));
		}
		guiGraphics.nextStratum();
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		//?} else {
		/*super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		if (PresetsScreen.shouldRender(PresetsScreen.Preset.CURRENT_CONFIG)) {
			PresetsScreen.renderLegacyPreviewCube(guiGraphics, PresetsScreen.Preset.CURRENT_CONFIG, (guiGraphics.guiWidth() / 6F) * 5F, guiGraphics.guiHeight() / 2F, 75, xAngleTweener.getF(), yAngleTweener.getF());
		}
		*///?}
	}
}
