package tektonikal.customblockhighlight.legacy.phys.shapes;

//? if =1.8.9 {
/*import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class Shapes {
	private static final VoxelShape BLOCK = new VoxelShape(List.of(new AABB(0, 0, 0, 1, 1, 1)));
	private static final VoxelShape EMPTY = new VoxelShape(List.of());

	private Shapes() {
	}

	public static VoxelShape block() {
		return BLOCK;
	}

	public static VoxelShape empty() {
		return EMPTY;
	}

	public static VoxelShape box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return create(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
	}

	public static VoxelShape create(AABB box) {
		if (box.getXsize() <= 0 || box.getYsize() <= 0 || box.getZsize() <= 0) return EMPTY;
		return new VoxelShape(List.of(box));
	}

	public static VoxelShape create(List<AABB> boxes) {
		List<AABB> valid = new ArrayList<>(boxes.size());
		for (AABB box : boxes) {
			if (box.getXsize() > 0 && box.getYsize() > 0 && box.getZsize() > 0) valid.add(box);
		}
		return new VoxelShape(valid);
	}

	public static VoxelShape join(VoxelShape first, VoxelShape second, BooleanOp op) {
		if (op != BooleanOp.OR) throw new UnsupportedOperationException("Only OR is supported on 1.8.9");
		List<AABB> boxes = new ArrayList<>(first.toAabbs());
		boxes.addAll(second.toAabbs());
		return new VoxelShape(boxes);
	}

	public interface DoubleLineConsumer {
		void consume(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
	}
}
*///?}
