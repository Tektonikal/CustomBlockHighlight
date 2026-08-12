package tektonikal.customblockhighlight;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

public class GuiOutlineRenderer extends PictureInPictureRenderer<EvilRenderState> {
	@Override
	public Class<EvilRenderState> getRenderStateClass() {
		return EvilRenderState.class;
	}

	@Override
	protected void renderToTexture(EvilRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		poseStack.translate(0, -renderState.y1() / 2F / renderState.scale(), 0.0D);
		poseStack.rotateAround(new Quaternionf().rotateYXZ((float) (Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow()) / renderState.x1() * 4), 10, 0), 0, 0, 0);
		CBHFeatureRenderer.Submit t = new CBHFeatureRenderer.Submit(poseStack.last(), Shapes.block().move(-0.5F, -0.5F, -0.5F), RenderTypes.lines(), -1, 5);
		submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, t);
	}


	@Override
	protected String getTextureLabel() {
		return "outline";
	}
}
