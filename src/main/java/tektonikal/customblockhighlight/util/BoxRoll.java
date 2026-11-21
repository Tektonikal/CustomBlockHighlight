package tektonikal.customblockhighlight.util;

import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BoxRoll {
	public final Quaternionf target = new Quaternionf();
	private Direction lastFace;
	private Direction lastHorizontal = Direction.NORTH;

	public void towards(Direction face) {
		if (face.getStepY() == 0) lastHorizontal = face;
		Direction from = lastFace;
		lastFace = face;
		if (from == null || from == face) return;
		Vector3f a = new Vector3f(from.getStepX(), from.getStepY(), from.getStepZ());
		Vector3f b = new Vector3f(face.getStepX(), face.getStepY(), face.getStepZ());
		Quaternionf roll;
		if (face == from.getOpposite()) {
			Vector3f axis = a.y == 0 ? new Vector3f(0, 1, 0) : new Vector3f(lastHorizontal.getStepZ(), 0, -lastHorizontal.getStepX());
			roll = new Quaternionf().rotationAxis((float) Math.PI, axis);
		} else {
			roll = new Quaternionf().rotationTo(a, b);
		}
		target.premul(roll);
		Matrix3f m = target.get(new Matrix3f());
		m.m00 = Math.round(m.m00); m.m01 = Math.round(m.m01); m.m02 = Math.round(m.m02);
		m.m10 = Math.round(m.m10); m.m11 = Math.round(m.m11); m.m12 = Math.round(m.m12);
		m.m20 = Math.round(m.m20); m.m21 = Math.round(m.m21); m.m22 = Math.round(m.m22);
		target.setFromNormalized(m);
	}

	public void reset(Direction face) {
		target.identity();
		lastFace = face;
	}
}
