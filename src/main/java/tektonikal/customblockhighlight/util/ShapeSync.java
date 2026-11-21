package tektonikal.customblockhighlight.util;

import net.minecraft.world.phys.Vec3;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.util.ArrayList;
import java.util.List;

public final class ShapeSync {
	private ShapeSync() {
	}

	public static final class UnifiedEdges {
		private List<Quad> source;
		List<Quad> pairedWith;
		List<Line> linesPairedWith;
		private List<Line> merged = List.of();
		boolean stale = true;

		public List<Line> of(List<Quad> quads) {
			if (!stale && quads == source) return merged;
			List<Line> edges = new ArrayList<>();
			for (Quad quad : quads) {
				for (int e = 0; e < 4; e++) if (quad.edgeMasks[e] != 0) edges.add(new Line(quad.corners[e], quad.corners[(e + 1) % 4], quad.edgeMasks[e]));
			}
			source = quads;
			stale = false;
			return merged = ShapeGeometry.mergeLines(edges);
		}
	}

	public static List<Line> syncLines(List<Line> current, List<Line> targets, Vec3 center, List<Line> leaving, UnifiedEdges edges, boolean snapping, double k) {
		if (snapping || current == targets) {
			List<Line> copied = new ArrayList<>(targets.size());
			for (Line target : targets) copied.add(new Line(target.minPos, target.maxPos, target.dirMask));
			edges.linesPairedWith = targets;
			if (snapping) return copied;
			current = copied;
		}
		if (targets != edges.linesPairedWith) {
			current = pairLines(current, targets, center, leaving);
			edges.linesPairedWith = targets;
		}
		for (int i = 0; i < current.size(); i++) {
			Line line = current.get(i);
			Line target = targets.get(i);
			line.moveTo(target.minPos, target.maxPos, k);
			line.dirMask = target.dirMask;
			line.update(true);
		}
		return current;
	}

	private static List<Line> pairLines(List<Line> current, List<Line> targets, Vec3 center, List<Line> leaving) {
		List<Line> paired = new ArrayList<>(targets.size());
		boolean[] used = new boolean[current.size()];
		for (Line target : targets) {
			int best = -1;
			boolean flip = false;
			double bestDistance = Double.MAX_VALUE;
			for (int i = 0; i < current.size(); i++) {
				if (used[i]) continue;
				Line line = current.get(i);
				double straight = line.minPos.distanceToSqr(target.minPos) + line.maxPos.distanceToSqr(target.maxPos);
				double flipped = line.minPos.distanceToSqr(target.maxPos) + line.maxPos.distanceToSqr(target.minPos);
				if (Math.min(straight, flipped) < bestDistance) {
					bestDistance = Math.min(straight, flipped);
					best = i;
					flip = flipped < straight;
				}
			}
			if (best < 0) {
				paired.add(appearing(new Line(center, center), new Line(target.minPos, target.maxPos)));
				continue;
			}
			used[best] = true;
			Line line = current.get(best);
			if (flip) {
				Vec3 min = line.minPos;
				line.minPos = line.maxPos;
				line.maxPos = min;
			}
			paired.add(line);
		}
		for (int i = 0; i < current.size(); i++) if (!used[i]) leaving.add(current.get(i));
		return paired;
	}

	private static Line appearing(Line grown, Line inPlace) {
		BlockHighlightConfig cfg = BlockHighlightConfig.getActiveInstance();
		Line line = cfg.scale ? grown : inPlace;
		if (cfg.fadeIn) line.alphaMultiplier = 0;
		return line;
	}

	public static List<Quad> syncQuads(List<Quad> current, List<Quad> targets, Vec3 center, List<Quad> leaving, UnifiedEdges edges, boolean snapping, double k) {
		if (snapping || current == targets) {
			List<Quad> copied = new ArrayList<>(targets.size());
			for (Quad target : targets) copied.add(target.copy());
			edges.pairedWith = targets;
			if (snapping) return copied;
			current = copied;
		}
		boolean changed = false;
		if (targets != edges.pairedWith) {
			current = Quad.pair(current, targets, center, leaving);
			edges.pairedWith = targets;
			changed = true;
		}
		for (int i = 0; i < current.size(); i++) {
			Quad quad = current.get(i);
			changed |= quad.moveTo(targets.get(i), k);
			quad.update(true);
		}
		if (changed) edges.stale = true;
		return current;
	}
}
