package tektonikal.customblockhighlight.legacy.phys.shapes;

//? if =1.8.9 {
/*import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class VoxelShape {
	private final List<AABB> boxes;

	VoxelShape(List<AABB> boxes) {
		this.boxes = boxes;
	}

	public List<AABB> toAabbs() {
		return Collections.unmodifiableList(boxes);
	}

	public boolean isEmpty() {
		return boxes.isEmpty();
	}

	public AABB bounds() {
		if (boxes.isEmpty()) throw new UnsupportedOperationException("No bounds for empty shape.");
		AABB bounds = boxes.getFirst();
		for (AABB box : boxes) bounds = bounds.minmax(box);
		return bounds;
	}

	public VoxelShape move(double x, double y, double z) {
		List<AABB> moved = new ArrayList<>(boxes.size());
		for (AABB box : boxes) moved.add(box.move(x, y, z));
		return new VoxelShape(moved);
	}

	public void forAllEdges(Shapes.DoubleLineConsumer consumer) {
		if (boxes.isEmpty()) return;
		double[] xs = coords(0);
		double[] ys = coords(1);
		double[] zs = coords(2);
		boolean[][][] full = new boolean[xs.length - 1][ys.length - 1][zs.length - 1];
		for (int i = 0; i < xs.length - 1; i++) {
			for (int j = 0; j < ys.length - 1; j++) {
				for (int k = 0; k < zs.length - 1; k++) {
					double cx = (xs[i] + xs[i + 1]) / 2;
					double cy = (ys[j] + ys[j + 1]) / 2;
					double cz = (zs[k] + zs[k + 1]) / 2;
					for (AABB box : boxes) {
						if (cx > box.minX && cx < box.maxX && cy > box.minY && cy < box.maxY && cz > box.minZ && cz < box.maxZ) {
							full[i][j][k] = true;
							break;
						}
					}
				}
			}
		}
		// same axis order as modern (Z, Y then X edges) so eased lines pair up the same way
		axisEdges(consumer, full, xs, ys, zs, 2);
		axisEdges(consumer, full, xs, ys, zs, 1);
		axisEdges(consumer, full, xs, ys, zs, 0);
	}

	private static void axisEdges(Shapes.DoubleLineConsumer consumer, boolean[][][] full, double[] xs, double[] ys, double[] zs, int along) {
		double[][] axes = {xs, ys, zs};
		int a = along == 2 ? 0 : along == 1 ? 2 : 1;
		int b = along == 2 ? 1 : along == 1 ? 0 : 2;
		int sizeA = axes[a].length - 1;
		int sizeB = axes[b].length - 1;
		int sizeAlong = axes[along].length - 1;
		int[] cell = new int[3];
		for (int l = 0; l <= sizeA; l++) {
			for (int m = 0; m <= sizeB; m++) {
				int start = -1;
				for (int o = 0; o <= sizeAlong; o++) {
					int count = 0;
					int parity = 0;
					for (int r = 0; r <= 1; r++) {
						for (int s = 0; s <= 1; s++) {
							cell[a] = l + r - 1;
							cell[b] = m + s - 1;
							cell[along] = o;
							if (isFull(full, cell)) {
								count++;
								parity ^= r ^ s;
							}
						}
					}
					if (count == 1 || count == 3 || count == 2 && (parity & 1) == 0) {
						if (start == -1) start = o;
					} else if (start != -1) {
						double[] from = new double[3];
						double[] to = new double[3];
						from[a] = to[a] = axes[a][l];
						from[b] = to[b] = axes[b][m];
						from[along] = axes[along][start];
						to[along] = axes[along][o];
						consumer.consume(from[0], from[1], from[2], to[0], to[1], to[2]);
						start = -1;
					}
				}
			}
		}
	}

	private static boolean isFull(boolean[][][] full, int[] cell) {
		if (cell[0] < 0 || cell[1] < 0 || cell[2] < 0) return false;
		if (cell[0] >= full.length || cell[1] >= full[0].length || cell[2] >= full[0][0].length) return false;
		return full[cell[0]][cell[1]][cell[2]];
	}

	private double[] coords(int axis) {
		TreeSet<Double> set = new TreeSet<>();
		for (AABB box : boxes) {
			set.add(axis == 0 ? box.minX : axis == 1 ? box.minY : box.minZ);
			set.add(axis == 0 ? box.maxX : axis == 1 ? box.maxY : box.maxZ);
		}
		return set.stream().mapToDouble(Double::doubleValue).toArray();
	}

	@Override
	public String toString() {
		return "VoxelShape" + Arrays.toString(boxes.toArray());
	}
}
*///?}
