package tektonikal.customblockhighlight.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import tektonikal.customblockhighlight.Vertexer;

import static tektonikal.customblockhighlight.CustomBlockHighlight.easeFactor;
import static tektonikal.customblockhighlight.CustomBlockHighlight.easeF;
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
		moveTo(minPosTo, maxPosTo, easeFactor(getActiveInstance().easeSpeed));
	}

	public void moveTo(Vec3 minPosTo, Vec3 maxPosTo, double k) {
		if (!minPos.equals(minPosTo)) {
			this.minPos = new Vec3(this.minPos.x + (minPosTo.x - this.minPos.x) * k, this.minPos.y + (minPosTo.y - this.minPos.y) * k, this.minPos.z + (minPosTo.z - this.minPos.z) * k);
		}
		if (!maxPos.equals(maxPosTo)) {
			this.maxPos = new Vec3(this.maxPos.x + (maxPosTo.x - this.maxPos.x) * k, this.maxPos.y + (maxPosTo.y - this.maxPos.y) * k, this.maxPos.z + (maxPosTo.z - this.maxPos.z) * k);
		}
	}

	public void update(boolean in) {
		if (in) {
			if (this.alphaMultiplier == 1) return;
			this.alphaMultiplier = getActiveInstance().fadeIn ? easeF(this.alphaMultiplier, 1, getActiveInstance().fadeInSpeed) : 1;
		} else {
			this.alphaMultiplier = getActiveInstance().fadeOut ? easeF(this.alphaMultiplier, 0, getActiveInstance().fadeOutSpeed) : 0;
		}
	}

	public void render(PoseStack.Pose pose, VertexConsumer buf, int rgb1, int rgb2, int alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		int a = Math.round(alpha * alphaMultiplier);
		if (a < 1) return;
		float k = (float) (maxPos.x - minPos.x);
		float l = (float) (maxPos.y - minPos.y);
		float m = (float) (maxPos.z - minPos.z);
		float n = Mth.sqrt(k * k + l * l + m * m);
		Vertexer.vertexLine(pose, buf, (float) minPos.x, (float) minPos.y, (float) minPos.z, (float) maxPos.x, (float) maxPos.y, (float) maxPos.z, rgb1, rgb2, a, k / n, l / n, m / n, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;

		Line line = (Line) o;
		return ((minPos.equals(line.minPos) && maxPos.equals(line.maxPos)) || (minPos.equals(line.maxPos) && maxPos.equals(line.minPos)));
	}

	@Override
	public int hashCode() {
		int result = minPos.hashCode();
		result = 31 * result + maxPos.hashCode();
		return result;
	}
}
