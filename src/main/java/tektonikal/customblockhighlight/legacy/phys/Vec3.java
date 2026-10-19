package tektonikal.customblockhighlight.legacy.phys;

//? if =1.8.9 {
/*import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vec3 {
	public static final Vec3 ZERO = new Vec3(0, 0, 0);

	public final double x;
	public final double y;
	public final double z;

	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(Vector3fc vec) {
		this(vec.x(), vec.y(), vec.z());
	}

	public Vec3 add(Vec3 vec) {
		return add(vec.x, vec.y, vec.z);
	}

	public Vec3 add(double x, double y, double z) {
		return new Vec3(this.x + x, this.y + y, this.z + z);
	}

	public Vec3 subtract(Vec3 vec) {
		return subtract(vec.x, vec.y, vec.z);
	}

	public Vec3 subtract(double x, double y, double z) {
		return add(-x, -y, -z);
	}

	public Vec3 reverse() {
		return scale(-1);
	}

	public Vec3 scale(double factor) {
		return new Vec3(x * factor, y * factor, z * factor);
	}

	public double distanceTo(Vec3 vec) {
		return Math.sqrt(distanceToSqr(vec));
	}

	public double distanceToSqr(Vec3 vec) {
		double dx = vec.x - x;
		double dy = vec.y - y;
		double dz = vec.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	public Vector3f toVector3f() {
		return new Vector3f((float) x, (float) y, (float) z);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Vec3 vec)) return false;
		return Double.compare(vec.x, x) == 0 && Double.compare(vec.y, y) == 0 && Double.compare(vec.z, z) == 0;
	}

	@Override
	public int hashCode() {
		long bits = Double.doubleToLongBits(x);
		int result = (int) (bits ^ bits >>> 32);
		bits = Double.doubleToLongBits(y);
		result = 31 * result + (int) (bits ^ bits >>> 32);
		bits = Double.doubleToLongBits(z);
		return 31 * result + (int) (bits ^ bits >>> 32);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
*///?}
