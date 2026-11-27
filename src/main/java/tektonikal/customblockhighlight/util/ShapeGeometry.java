package tektonikal.customblockhighlight.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.DoubleStream;

public record ShapeGeometry(List<Line> lines, List<Quad> quads) {
	public static final ShapeGeometry EMPTY = new ShapeGeometry(List.of(), List.of());

	private static final int[] AXIS = {1, 1, 2, 2, 0, 0};
	private static final int[] SIGN = {-1, 1, -1, 1, -1, 1};
	private static final double EPS = 1.0E-5;

	public static ShapeGeometry ofShape(VoxelShape shape) {
		List<AABB> boxes = shape.toAabbs();
		if (boxes.isEmpty()) return EMPTY;
		double[][] c = new double[3][];
		for (int a = 0; a < 3; a++) {
			int axis = a;
			double[] sorted = boxes.stream().flatMapToDouble(box -> DoubleStream.of(min(box, axis), max(box, axis))).sorted().toArray();
			double[] grid = new double[sorted.length];
			int count = 0;
			for (double value : sorted) if (count == 0 || value - grid[count - 1] > EPS) grid[count++] = value;
			c[a] = Arrays.copyOf(grid, count);
		}
		int[] n = {c[0].length - 1, c[1].length - 1, c[2].length - 1};
		boolean[][][] full = new boolean[n[0]][n[1]][n[2]];
		for (AABB box : boxes) {
			int x0 = line(c[0], box.minX), x1 = line(c[0], box.maxX), y0 = line(c[1], box.minY), y1 = line(c[1], box.maxY), z0 = line(c[2], box.minZ), z1 = line(c[2], box.maxZ);
			for (int i = x0; i < x1; i++) for (int j = y0; j < y1; j++) for (int k = z0; k < z1; k++) full[i][j][k] = true;
		}

		List<Quad> quads = new ArrayList<>();
		List<Line> edges = new ArrayList<>();
		int[] cell = new int[3];
		for (int d = 0; d < 6; d++) {
			int a = AXIS[d], u = (a + 1) % 3, v = (a + 2) % 3;
			for (int i = 0; i < n[0]; i++) for (int j = 0; j < n[1]; j++) for (int k = 0; k < n[2]; k++) {
				cell[0] = i; cell[1] = j; cell[2] = k;
				if (!exposed(full, n, d, cell)) continue;
				double plane = c[a][cell[a] + (SIGN[d] > 0 ? 1 : 0)];
				double u0 = c[u][cell[u]], u1 = c[u][cell[u] + 1], v0 = c[v][cell[v]], v1 = c[v][cell[v] + 1];
				Vec3[] corners = SIGN[d] > 0
						? new Vec3[]{point(a, plane, u, u0, v, v0), point(a, plane, u, u1, v, v0), point(a, plane, u, u1, v, v1), point(a, plane, u, u0, v, v1)}
						: new Vec3[]{point(a, plane, u, u0, v, v0), point(a, plane, u, u0, v, v1), point(a, plane, u, u1, v, v1), point(a, plane, u, u1, v, v0)};
				int[][] across = SIGN[d] > 0
						? new int[][]{{v, -1}, {u, 1}, {v, 1}, {u, -1}}
						: new int[][]{{u, -1}, {v, 1}, {u, 1}, {v, -1}};
				Quad quad = new Quad(corners, d);
				for (int e = 0; e < 4; e++) {
					cell[across[e][0]] += across[e][1];
					boolean boundary = !exposed(full, n, d, cell);
					cell[across[e][0]] -= across[e][1];
					if (boundary) {
						quad.edgeMasks[e] = 1 << d;
						edges.add(new Line(corners[e], corners[(e + 1) % 4], 1 << d));
					}
				}
				quads.add(quad);
			}
		}
		claimSharedEdges(quads);
		return new ShapeGeometry(mergeLines(edges), quads);
	}

	public static ShapeGeometry ofQuads(List<Vec3[]> raw) {
		List<Quad> quads = new ArrayList<>();
		Map<Set<List<Long>>, Integer> seen = new HashMap<>();
		Map<Set<List<Long>>, List<int[]>> byEdge = new HashMap<>();
		for (Vec3[] corners : raw) {
			Set<List<Long>> key = new HashSet<>();
			for (Vec3 corner : corners) key.add(key(corner));
			int dir = direction(corners);
			if (dir < 0) continue;
			Integer kept = seen.putIfAbsent(key, quads.size());
			if (kept != null) {
				// the block draws this face from both sides, so the fill can't push it away
				if (quads.get(kept).dir != dir) quads.get(kept).twoSided = true;
				continue;
			}
			Quad quad = new Quad(corners.clone(), dir);
			for (int e = 0; e < 4; e++) {
				Set<List<Long>> edge = edgeKey(corners[e], corners[(e + 1) % 4]);
				if (edge == null) continue;
				quad.edgeMasks[e] = 1 << dir;
				byEdge.computeIfAbsent(edge, k -> new ArrayList<>()).add(new int[]{quads.size(), e});
			}
			quads.add(quad);
		}
		for (List<int[]> sharing : byEdge.values()) {
			for (int[] a : sharing) {
				for (int[] b : sharing) {
					if (a != b && quads.get(a[0]).dir == quads.get(b[0]).dir) {
						quads.get(a[0]).edgeMasks[a[1]] = 0;
						quads.get(b[0]).edgeMasks[b[1]] = 0;
					}
				}
			}
		}
		List<Line> edges = new ArrayList<>();
		for (Quad quad : quads) {
			for (int e = 0; e < 4; e++) {
				if (quad.edgeMasks[e] != 0) edges.add(new Line(quad.corners[e], quad.corners[(e + 1) % 4], quad.edgeMasks[e]));
			}
		}
		claimSharedEdges(quads);
		return new ShapeGeometry(mergeLines(edges), quads);
	}

	//TODO: only axis-aligned rectangles work
	public static List<Vec3[]> cullBuried(List<Vec3[]> raw) {
		int n = raw.size();
		int[] dirs = new int[n];
		double[][] rects = new double[n][];
		for (int q = 0; q < n; q++) {
			dirs[q] = direction(raw.get(q));
			rects[q] = dirs[q] < 0 ? null : rectangle(raw.get(q), AXIS[dirs[q]]);
		}
		List<Vec3[]> kept = new ArrayList<>();
		for (int q = 0; q < n; q++) {
			double[] rect = rects[q];
			if (rect == null) {
				kept.add(raw.get(q));
				continue;
			}
			int d = dirs[q], a = AXIS[d], u = (a + 1) % 3, v = (a + 2) % 3;
			List<Double> us = new ArrayList<>(List.of(rect[1], rect[2])), vs = new ArrayList<>(List.of(rect[3], rect[4]));
			for (int o = 0; o < n; o++) {
				double[] other = rects[o];
				if (o == q || other == null) continue;
				int b = AXIS[dirs[o]];
				if (b == a) {
					if (dirs[o] != d || Math.abs(other[0] - rect[0]) > EPS || !meets(other[1], other[2], rect[1], rect[2]) || !meets(other[3], other[4], rect[3], rect[4])) continue;
					us.add(other[1]);
					us.add(other[2]);
					vs.add(other[3]);
					vs.add(other[4]);
				} else {
					int w = b == u ? v : u;
					double[] across = range(other, b, a), along = range(other, b, w);
					double[] own = w == u ? new double[]{rect[1], rect[2]} : new double[]{rect[3], rect[4]};
					if (meets(across[0], across[1], rect[0], rect[0]) && meets(along[0], along[1], own[0], own[1])) (b == u ? us : vs).add(other[0]);
				}
			}
			double[] uCuts = grid(us, rect[1], rect[2]), vCuts = grid(vs, rect[3], rect[4]);
			for (int i = 0; i + 1 < uCuts.length; i++) {
				for (int j = 0; j + 1 < vCuts.length; j++) {
					double u0 = uCuts[i], u1 = uCuts[i + 1], v0 = vCuts[j], v1 = vCuts[j + 1];
					double um = u0 + (u1 - u0) * 0.4871, vm = v0 + (v1 - v0) * 0.5137;
					if (insideModel(rects, dirs, q, d, rect[0], um, vm)) continue;
					kept.add(SIGN[d] > 0
							? new Vec3[]{point(a, rect[0], u, u0, v, v0), point(a, rect[0], u, u1, v, v0), point(a, rect[0], u, u1, v, v1), point(a, rect[0], u, u0, v, v1)}
							: new Vec3[]{point(a, rect[0], u, u0, v, v0), point(a, rect[0], u, u0, v, v1), point(a, rect[0], u, u1, v, v1), point(a, rect[0], u, u1, v, v0)});
				}
			}
		}
		return kept;
	}

	private static boolean insideModel(double[][] rects, int[] dirs, int q, int d, double plane, double pu, double pv) {
		int a = AXIS[d];
		int winding = 0;
		for (int o = 0; o < rects.length; o++) {
			double[] other = rects[o];
			if (o == q || other == null || AXIS[dirs[o]] != a) continue;
			if (!(other[1] < pu && other[2] > pu && other[3] < pv && other[4] > pv)) continue;
			double ahead = (other[0] - plane) * SIGN[d];
			if (Math.abs(ahead) <= EPS) {
				if (o < q && dirs[o] == d) return true;
				continue;
			}
			if (ahead > 0) winding += SIGN[dirs[o]] == SIGN[d] ? 1 : -1;
		}
		return winding > 0;
	}

	private static boolean meets(double from, double to, double otherFrom, double otherTo) {
		return from <= otherTo + EPS && to >= otherFrom - EPS;
	}

	private static double[] range(double[] rect, int facing, int axis) {
		if (axis == facing) return new double[]{rect[0], rect[0]};
		return axis == (facing + 1) % 3 ? new double[]{rect[1], rect[2]} : new double[]{rect[3], rect[4]};
	}

	private static double[] grid(List<Double> cuts, double from, double to) {
		double[] sorted = cuts.stream().mapToDouble(Double::doubleValue).filter(c -> c > from + EPS && c < to - EPS).sorted().toArray();
		double[] grid = new double[sorted.length + 2];
		int count = 0;
		grid[count++] = from;
		for (double c : sorted) if (c - grid[count - 1] > EPS) grid[count++] = c;
		if (to - grid[count - 1] > EPS) grid[count++] = to;
		else grid[count - 1] = to;
		return Arrays.copyOf(grid, count);
	}

	private static double[] rectangle(Vec3[] corners, int a) {
		int u = (a + 1) % 3, v = (a + 2) % 3;
		double plane = get(corners[0], a);
		double u0 = Double.MAX_VALUE, u1 = -Double.MAX_VALUE, v0 = Double.MAX_VALUE, v1 = -Double.MAX_VALUE;
		for (Vec3 p : corners) {
			if (Math.abs(get(p, a) - plane) > EPS) return null;
			u0 = Math.min(u0, get(p, u)); u1 = Math.max(u1, get(p, u));
			v0 = Math.min(v0, get(p, v)); v1 = Math.max(v1, get(p, v));
		}
		for (Vec3 p : corners) {
			boolean onU = Math.abs(get(p, u) - u0) < EPS || Math.abs(get(p, u) - u1) < EPS;
			boolean onV = Math.abs(get(p, v) - v0) < EPS || Math.abs(get(p, v) - v1) < EPS;
			if (!onU || !onV) return null;
		}
		return new double[]{plane, u0, u1, v0, v1};
	}

	private static double get(Vec3 p, int axis) {
		return axis == 0 ? p.x : axis == 1 ? p.y : p.z;
	}

	private static int direction(Vec3[] q) {
		double[] normal = cross(q[0], q[1], q[2]);
		if (Math.abs(normal[0]) + Math.abs(normal[1]) + Math.abs(normal[2]) < EPS) normal = cross(q[0], q[2], q[3]);
		int axis = 0;
		for (int a = 1; a < 3; a++) if (Math.abs(normal[a]) > Math.abs(normal[axis])) axis = a;
		if (Math.abs(normal[axis]) < EPS) return -1;
		for (int d = 0; d < 6; d++) if (AXIS[d] == axis && SIGN[d] == Math.signum(normal[axis])) return d;
		return -1;
	}

	private static double[] cross(Vec3 o, Vec3 a, Vec3 b) {
		double ax = a.x - o.x, ay = a.y - o.y, az = a.z - o.z, bx = b.x - o.x, by = b.y - o.y, bz = b.z - o.z;
		return new double[]{ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx};
	}

	private static void claimSharedEdges(List<Quad> quads) {
		Map<Set<List<Long>>, int[]> owners = new HashMap<>();
		for (int q = 0; q < quads.size(); q++) {
			Quad quad = quads.get(q);
			for (int e = 0; e < 4; e++) {
				Set<List<Long>> key = edgeKey(quad.corners[e], quad.corners[(e + 1) % 4]);
				if (quad.edgeMasks[e] == 0 || key == null) continue;
				int[] owner = owners.putIfAbsent(key, new int[]{q, e});
				if (owner != null) {
					quads.get(owner[0]).edgeMasks[owner[1]] |= quad.edgeMasks[e];
					quad.edgeMasks[e] = 0;
				}
			}
		}
	}

	public static List<Line> mergeLines(List<Line> edges) {
		Map<List<Long>, List<double[]>> groups = new HashMap<>();
		Map<List<Long>, double[][]> frames = new HashMap<>();
		for (Line edge : edges) {
			double dx = edge.maxPos.x - edge.minPos.x, dy = edge.maxPos.y - edge.minPos.y, dz = edge.maxPos.z - edge.minPos.z;
			double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (length < EPS) continue;
			dx /= length; dy /= length; dz /= length;
			if (dx < -EPS || (Math.abs(dx) <= EPS && (dy < -EPS || (Math.abs(dy) <= EPS && dz < 0)))) {
				dx = -dx; dy = -dy; dz = -dz;
			}
			double along = edge.minPos.x * dx + edge.minPos.y * dy + edge.minPos.z * dz;
			Vec3 base = new Vec3(edge.minPos.x - dx * along, edge.minPos.y - dy * along, edge.minPos.z - dz * along);
			List<Long> key = new ArrayList<>(key(base));
			key.addAll(key(new Vec3(dx, dy, dz)));
			double t0 = along, t1 = edge.maxPos.x * dx + edge.maxPos.y * dy + edge.maxPos.z * dz;
			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(new double[]{Math.min(t0, t1), Math.max(t0, t1), edge.dirMask});
			frames.putIfAbsent(key, new double[][]{{base.x, base.y, base.z}, {dx, dy, dz}});
		}
		List<Line> lines = new ArrayList<>();
		for (Map.Entry<List<Long>, List<double[]>> group : groups.entrySet()) {
			double[][] frame = frames.get(group.getKey());
			List<double[]> segments = group.getValue();
			double[] cuts = segments.stream().flatMapToDouble(s -> DoubleStream.of(s[0], s[1])).sorted().toArray();
			List<double[]> pieces = new ArrayList<>();
			double from = cuts[0];
			for (int i = 1; i < cuts.length; i++) {
				double to = cuts[i];
				if (to - from < EPS) continue;
				double middle = (from + to) / 2;
				int mask = 0;
				boolean covered = false;
				for (double[] s : segments) {
					if (s[0] < middle && s[1] > middle) {
						mask |= (int) s[2];
						covered = true;
					}
				}
				if (covered) pieces.add(new double[]{from, to, mask});
				from = to;
			}
			double[] run = null;
			for (double[] piece : pieces) {
				if (run != null && piece[0] - run[1] < EPS && piece[2] == run[2]) {
					run[1] = piece[1];
					continue;
				}
				if (run != null) lines.add(new Line(at(frame, run[0]), at(frame, run[1]), (int) run[2]));
				run = piece;
			}
			if (run != null) lines.add(new Line(at(frame, run[0]), at(frame, run[1]), (int) run[2]));
		}
		return lines;
	}

	private static Vec3 at(double[][] frame, double t) {
		return new Vec3(frame[0][0] + frame[1][0] * t, frame[0][1] + frame[1][1] * t, frame[0][2] + frame[1][2] * t);
	}

	private static boolean exposed(boolean[][][] full, int[] n, int d, int[] cell) {
		if (!inside(n, cell) || !full[cell[0]][cell[1]][cell[2]]) return false;
		int a = AXIS[d];
		cell[a] += SIGN[d];
		boolean neighbour = inside(n, cell) && full[cell[0]][cell[1]][cell[2]];
		cell[a] -= SIGN[d];
		return !neighbour;
	}

	private static boolean inside(int[] n, int[] cell) {
		return cell[0] >= 0 && cell[1] >= 0 && cell[2] >= 0 && cell[0] < n[0] && cell[1] < n[1] && cell[2] < n[2];
	}

	private static Vec3 point(int a, double plane, int u, double uValue, int v, double vValue) {
		double[] p = new double[3];
		p[a] = plane;
		p[u] = uValue;
		p[v] = vValue;
		return new Vec3(p[0], p[1], p[2]);
	}

	private static int line(double[] grid, double value) {
		int index = Arrays.binarySearch(grid, value);
		if (index >= 0) return index;
		int above = -index - 1;
		return above > 0 && (above == grid.length || value - grid[above - 1] < grid[above] - value) ? above - 1 : above;
	}

	private static double min(AABB box, int axis) {
		return axis == 0 ? box.minX : axis == 1 ? box.minY : box.minZ;
	}

	private static double max(AABB box, int axis) {
		return axis == 0 ? box.maxX : axis == 1 ? box.maxY : box.maxZ;
	}

	private static Set<List<Long>> edgeKey(Vec3 a, Vec3 b) {
		List<Long> first = key(a), second = key(b);
		return first.equals(second) ? null : Set.of(first, second);
	}

	private static List<Long> key(Vec3 p) {
		return Arrays.asList(Math.round(p.x / EPS), Math.round(p.y / EPS), Math.round(p.z / EPS));
	}
}
