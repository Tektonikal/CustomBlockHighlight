package tektonikal.customblockhighlight.legacy.phys;

//? if =1.8.9 {
/*import net.minecraft.util.math.Box;

public class AABB {
	public final double minX;
	public final double minY;
	public final double minZ;
	public final double maxX;
	public final double maxY;
	public final double maxZ;

	public AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
		this.minX = Math.min(x1, x2);
		this.minY = Math.min(y1, y2);
		this.minZ = Math.min(z1, z2);
		this.maxX = Math.max(x1, x2);
		this.maxY = Math.max(y1, y2);
		this.maxZ = Math.max(z1, z2);
	}

	public AABB(Vec3 min, Vec3 max) {
		this(min.x, min.y, min.z, max.x, max.y, max.z);
	}

	public static AABB of(Box box) {
		return new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	public AABB inflate(double amount) {
		return inflate(amount, amount, amount);
	}

	public AABB inflate(double x, double y, double z) {
		return new AABB(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z);
	}

	public AABB move(double x, double y, double z) {
		return new AABB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
	}

	public AABB move(Vec3 vec) {
		return move(vec.x, vec.y, vec.z);
	}

	public AABB intersect(AABB other) {
		return new AABB(Math.max(minX, other.minX), Math.max(minY, other.minY), Math.max(minZ, other.minZ), Math.min(maxX, other.maxX), Math.min(maxY, other.maxY), Math.min(maxZ, other.maxZ));
	}

	public AABB minmax(AABB other) {
		return new AABB(Math.min(minX, other.minX), Math.min(minY, other.minY), Math.min(minZ, other.minZ), Math.max(maxX, other.maxX), Math.max(maxY, other.maxY), Math.max(maxZ, other.maxZ));
	}

	public double getXsize() {
		return maxX - minX;
	}

	public double getYsize() {
		return maxY - minY;
	}

	public double getZsize() {
		return maxZ - minZ;
	}

	public Vec3 getCenter() {
		return new Vec3((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
	}

	public Vec3 getMinPosition() {
		return new Vec3(minX, minY, minZ);
	}

	public Vec3 getMaxPosition() {
		return new Vec3(maxX, maxY, maxZ);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AABB box)) return false;
		return Double.compare(box.minX, minX) == 0 && Double.compare(box.minY, minY) == 0 && Double.compare(box.minZ, minZ) == 0
				&& Double.compare(box.maxX, maxX) == 0 && Double.compare(box.maxY, maxY) == 0 && Double.compare(box.maxZ, maxZ) == 0;
	}

	@Override
	public int hashCode() {
		int result = Double.hashCode(minX);
		result = 31 * result + Double.hashCode(minY);
		result = 31 * result + Double.hashCode(minZ);
		result = 31 * result + Double.hashCode(maxX);
		result = 31 * result + Double.hashCode(maxY);
		return 31 * result + Double.hashCode(maxZ);
	}

	@Override
	public String toString() {
		return "AABB[" + minX + ", " + minY + ", " + minZ + "] -> [" + maxX + ", " + maxY + ", " + maxZ + "]";
	}
}
*///?}
