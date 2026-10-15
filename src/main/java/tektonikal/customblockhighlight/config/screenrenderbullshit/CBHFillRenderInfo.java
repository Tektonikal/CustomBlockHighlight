package tektonikal.customblockhighlight.config.screenrenderbullshit;

import tektonikal.customblockhighlight.legacy.Pair;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public record CBHFillRenderInfo(Pair<Color, Color> cols, float[] alphas, DepthTestMode mode, float scaleBlocks,
                                float scalePercent) {
}
