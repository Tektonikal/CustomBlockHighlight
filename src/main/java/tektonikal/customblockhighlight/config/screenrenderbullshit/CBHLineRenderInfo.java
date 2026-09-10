package tektonikal.customblockhighlight.config.screenrenderbullshit;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.phys.shapes.VoxelShape;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public record CBHLineRenderInfo(VoxelShape shape, Pair<Color, Color> cols, float[] alphas, float width, DepthTestMode mode, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult, float scaleBlocks, float scalePercent) {
}
