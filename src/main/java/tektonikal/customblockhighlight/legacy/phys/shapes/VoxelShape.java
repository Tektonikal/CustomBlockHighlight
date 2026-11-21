package tektonikal.customblockhighlight.legacy.phys.shapes;

//? if =1.8.9 {
/*import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof VoxelShape other && boxes.equals(other.boxes);
	}

	@Override
	public int hashCode() {
		return boxes.hashCode();
	}

	@Override
	public String toString() {
		return "VoxelShape" + Arrays.toString(boxes.toArray());
	}
}
*///?}
