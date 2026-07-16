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

public class Line {
	public Vec3 minPos;
	public Vec3 maxPos;
	public float alphaMultiplier = 1;

	public Line(Vec3 minPos, Vec3 maxPos) {
		this.minPos = minPos;
		this.maxPos = maxPos;
	}

	public float getDistanceToCamera(Vec3 minVec) {
		return (float) minPos.add(maxPos).scale(0.5F).distanceTo(Renderer.mc.gameRenderer.getMainCamera().position().subtract(minVec));
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
		this.minPos = new Vec3(ease(this.minPos.x, minPosTo.x, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.minPos.y, minPosTo.y, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.minPos.z, minPosTo.z, BlockHighlightConfig.INSTANCE.instance().easeSpeed));
		this.maxPos = new Vec3(ease(this.maxPos.x, maxPosTo.x, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.maxPos.y, maxPosTo.y, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.maxPos.z, maxPosTo.z, BlockHighlightConfig.INSTANCE.instance().easeSpeed));
	}

	public void updateAndRender(PoseStack ms, VertexConsumer buf, Color c1, Color c2, int alpha, boolean b, int layer) {
		update(b);
		render(ms, buf, c1, c2, alpha, layer);
	}

	public void update(boolean b) {
		this.alphaMultiplier = (float) ease(this.alphaMultiplier, b ? 1 : 0, 10);
	}

	public void render(PoseStack ms, VertexConsumer buf, Color c1, Color c2, int alpha, int layer) {
		Vec3 normal = getNormal();
		Vertexer.vertexLine(ms, buf, (float) minPos.x, (float) minPos.y, (float) minPos.z, (float) maxPos.x, (float) maxPos.y, (float) maxPos.z, c1, c2, Math.round(alpha * alphaMultiplier), (float) normal.x, (float) normal.y, (float) normal.z, layer);
	}
}
