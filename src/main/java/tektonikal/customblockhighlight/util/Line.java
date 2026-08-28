package tektonikal.customblockhighlight.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.Vertexer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;

import static tektonikal.customblockhighlight.Blockhighlight.ease;
import static tektonikal.customblockhighlight.Blockhighlight.easeF;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.getActiveInstance;

public class Line {
	public Vec3 minPos;
	public Vec3 maxPos;
	public float alphaMultiplier = 1;

	public Line(Vec3 minPos, Vec3 maxPos) {
		this.minPos = minPos;
		this.maxPos = maxPos;
	}

	public Vec3 getNormal() {
		float k = (float) (maxPos.x - minPos.x);
		float l = (float) (maxPos.y - minPos.y);
		float m = (float) (maxPos.z - minPos.z);
		float n = Mth.sqrt(k * k + l * l + m * m);
		k /= n;
		l /= n;
		m /= n;
		return new Vec3(k, l, m);
	}

	public void moveTo(Vec3 minPosTo, Vec3 maxPosTo) {
		this.minPos = new Vec3(ease(this.minPos.x, minPosTo.x, getActiveInstance().easeSpeed), ease(this.minPos.y, minPosTo.y, getActiveInstance().easeSpeed), ease(this.minPos.z, minPosTo.z, getActiveInstance().easeSpeed));
		this.maxPos = new Vec3(ease(this.maxPos.x, maxPosTo.x, getActiveInstance().easeSpeed), ease(this.maxPos.y, maxPosTo.y, getActiveInstance().easeSpeed), ease(this.maxPos.z, maxPosTo.z, getActiveInstance().easeSpeed));
	}

	public void update(boolean in) {
		if (in) {
			this.alphaMultiplier = getActiveInstance().fadeIn ? easeF(this.alphaMultiplier, 1, getActiveInstance().fadeInSpeed) : 1;
		} else {
			this.alphaMultiplier = getActiveInstance().fadeOut ? easeF(this.alphaMultiplier, 0, getActiveInstance().fadeOutSpeed) : 0;
		}
	}

	public void render(PoseStack ms, VertexConsumer buf, Color c1, Color c2, int alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		Vec3 normal = getNormal();
		Vertexer.vertexLine(ms.last(), buf, (float) minPos.x, (float) minPos.y, (float) minPos.z, (float) maxPos.x, (float) maxPos.y, (float) maxPos.z, c1, c2, Math.round(alpha * alphaMultiplier), (float) normal.x, (float) normal.y, (float) normal.z, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}
}
