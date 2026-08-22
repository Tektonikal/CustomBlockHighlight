package tektonikal.customblockhighlight.config.screenrenderbullshit;

import net.minecraft.world.phys.shapes.VoxelShape;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public record CBHLineRenderInfo(VoxelShape shape, Color primaryCol, Color secondaryCol, float[] alphas, float width, DepthTestMode mode, float cutFromCenter, float cutFromCorner) {
	public static CBHLineRenderInfo of(VoxelShape shape, Color finalLineCol, Color finalLineCol2, float[] lineFades, BlockHighlightConfig instance) {
		return new CBHLineRenderInfo(shape, finalLineCol, finalLineCol2, lineFades, instance.lineWidth, instance.lineDepthTest, instance.cutFromCenter, instance.cutFromCorner);
	}
}
