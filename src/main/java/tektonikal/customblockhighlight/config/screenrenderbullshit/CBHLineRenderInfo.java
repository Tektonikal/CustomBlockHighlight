package tektonikal.customblockhighlight.config.screenrenderbullshit;

import net.minecraft.world.phys.AABB;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.*;

public record CBHLineRenderInfo(AABB box, Color primaryCol, Color secondaryCol, float[] alphas, float width, DepthTestMode mode) {
}
