package tektonikal.customblockhighlight.config.screenrenderbullshit;

import net.minecraft.world.phys.shapes.VoxelShape;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public record CBHLineRenderInfo(VoxelShape shape, Color primaryCol, Color secondaryCol, float[] alphas, float width, DepthTestMode mode, float cutFromCenter, float cutFromCorner) {
}
