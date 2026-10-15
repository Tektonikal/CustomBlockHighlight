package tektonikal.customblockhighlight.config.screenrenderbullshit;

import tektonikal.customblockhighlight.legacy.Pair;
import tektonikal.customblockhighlight.legacy.phys.shapes.VoxelShape;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public record CBHLineRenderInfo(VoxelShape shape, Pair<Color, Color> cols, float[] alphas, float width,
                                DepthTestMode mode, float cutFromCenter, float cutFromCorner, float outerMult,
                                float innerMult, float scaleBlocks, float scalePercent) {
}
