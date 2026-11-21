package tektonikal.customblockhighlight.util;

import net.minecraft.world.phys.Vec3;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tektonikal.customblockhighlight.CustomBlockHighlight.fadeIn;
import static tektonikal.customblockhighlight.CustomBlockHighlight.fadeOut;

public class Quad {
	public final Vec3[] corners;
	public int dir;
	public final int[] edgeMasks = new int[4];
	public float alphaMultiplier = 1;

	public Quad(Vec3[] corners, int dir) {
		this.corners = corners;
		this.dir = dir;
	}

	public static Quad point(Vec3 point) {
		return new Quad(new Vec3[]{point, point, point, point}, 0);
	}

	public Quad copy() {
		Quad quad = new Quad(corners.clone(), dir);
		System.arraycopy(edgeMasks, 0, quad.edgeMasks, 0, 4);
		quad.alphaMultiplier = alphaMultiplier;
		return quad;
	}

	public boolean moveTo(Quad target, double k) {
		boolean changed = dir != target.dir || !Arrays.equals(edgeMasks, target.edgeMasks);
		for (int i = 0; i < 4; i++) {
			Vec3 from = corners[i];
			Vec3 next = Line.ease(from, target.corners[i], k);
			if (next.equals(from)) continue;
			corners[i] = next;
			changed = true;
		}
		dir = target.dir;
		System.arraycopy(target.edgeMasks, 0, edgeMasks, 0, 4);
		return changed;
	}

	public static List<Quad> pair(List<Quad> current, List<Quad> targets, Vec3 center, List<Quad> leaving) {
		List<Quad> paired = new ArrayList<>(targets.size());
		boolean[] used = new boolean[current.size()];
		for (Quad target : targets) {
			Vec3 at = target.center();
			int unused = -1, any = -1;
			double unusedDistance = Double.MAX_VALUE, anyDistance = Double.MAX_VALUE;
			for (int i = 0; i < current.size(); i++) {
				if (current.get(i).dir != target.dir) continue;
				double distance = current.get(i).center().distanceToSqr(at);
				if (distance < anyDistance) {
					anyDistance = distance;
					any = i;
				}
				if (!used[i] && distance < unusedDistance) {
					unusedDistance = distance;
					unused = i;
				}
			}
			Quad quad;
			if (unused >= 0) {
				used[unused] = true;
				quad = current.get(unused);
			} else if (any >= 0) {
				quad = current.get(any).copy();
			} else {
				BlockHighlightConfig cfg = BlockHighlightConfig.getActiveInstance();
				quad = cfg.scale ? point(center) : target.copy();
				if (cfg.fadeIn) quad.alphaMultiplier = 0;
			}
			quad.alignTo(target);
			paired.add(quad);
		}
		for (int i = 0; i < current.size(); i++) if (!used[i]) leaving.add(current.get(i));
		return paired;
	}

	private void alignTo(Quad target) {
		int shift = 0;
		double best = Double.MAX_VALUE;
		for (int s = 0; s < 4; s++) {
			double cost = 0;
			for (int i = 0; i < 4; i++) cost += corners[(i + s) % 4].distanceToSqr(target.corners[i]);
			if (cost < best) {
				best = cost;
				shift = s;
			}
		}
		Vec3[] shifted = new Vec3[4];
		for (int i = 0; i < 4; i++) shifted[i] = corners[(i + shift) % 4];
		System.arraycopy(shifted, 0, corners, 0, 4);
	}

	private Vec3 center() {
		return corners[0].add(corners[1]).add(corners[2]).add(corners[3]).scale(0.25);
	}

	public void collapseTo(Vec3 point, double k) {
		for (int i = 0; i < 4; i++) corners[i] = corners[i].add(point.subtract(corners[i]).scale(k));
	}

	public void update(boolean in) {
		if (in) {
			if (alphaMultiplier == 1) return;
			alphaMultiplier = fadeIn(alphaMultiplier, 1);
		} else {
			alphaMultiplier = fadeOut(alphaMultiplier);
		}
	}
}
