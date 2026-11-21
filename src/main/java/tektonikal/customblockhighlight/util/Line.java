package tektonikal.customblockhighlight.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import tektonikal.customblockhighlight.Vertexer;

import static tektonikal.customblockhighlight.CustomBlockHighlight.fadeIn;
import static tektonikal.customblockhighlight.CustomBlockHighlight.fadeOut;

public class Line {
	public Vec3 minPos;
	public Vec3 maxPos;
	public float alphaMultiplier = 1;
	public int dirMask;

	public Line(Vec3 minPos, Vec3 maxPos) {
		this.minPos = minPos;
		this.maxPos = maxPos;
	}

	public Line(Vec3 minPos, Vec3 maxPos, int dirMask) {
		this(minPos, maxPos);
		this.dirMask = dirMask;
	}

	public void moveTo(Vec3 minPosTo, Vec3 maxPosTo, double k) {
		minPos = ease(minPos, minPosTo, k);
		maxPos = ease(maxPos, maxPosTo, k);
	}

	private static final double LAND = 1.0 / 512;

	public static Vec3 ease(Vec3 from, Vec3 to, double k) {
		if (from.equals(to)) return from;
		Vec3 next = new Vec3(from.x + (to.x - from.x) * k, from.y + (to.y - from.y) * k, from.z + (to.z - from.z) * k);
		return next.distanceToSqr(to) < LAND * LAND ? to : next;
	}

	public void update(boolean in) {
		if (in) {
			if (this.alphaMultiplier == 1) return;
			this.alphaMultiplier = fadeIn(this.alphaMultiplier, 1);
		} else {
			this.alphaMultiplier = fadeOut(this.alphaMultiplier);
		}
	}

	public void render(PoseStack.Pose pose, VertexConsumer buf, int rgb1, int rgb2, int alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		int a = Math.round(alpha * alphaMultiplier);
		if (a < 1) return;
		float k = (float) (maxPos.x - minPos.x);
		float l = (float) (maxPos.y - minPos.y);
		float m = (float) (maxPos.z - minPos.z);
		float n = Mth.sqrt(k * k + l * l + m * m);
		if (n == 0) return;
		Vertexer.vertexLine(pose, buf, (float) minPos.x, (float) minPos.y, (float) minPos.z, (float) maxPos.x, (float) maxPos.y, (float) maxPos.z, rgb1, rgb2, a, k / n, l / n, m / n, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}
}
