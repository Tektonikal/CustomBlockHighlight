package tektonikal.customblockhighlight.legacy.vertex;

//? if =1.8.9 {
/*import org.joml.Matrix4f;
import org.joml.Quaternionfc;

import java.util.ArrayDeque;
import java.util.Deque;

// no normal matrix, fixed function lines don't use normals
public class PoseStack {
	private final Deque<Pose> poses = new ArrayDeque<>();

	public PoseStack() {
		poses.addLast(new Pose(new Matrix4f()));
	}

	public void translate(double x, double y, double z) {
		translate((float) x, (float) y, (float) z);
	}

	public void translate(float x, float y, float z) {
		last().pose.translate(x, y, z);
	}

	public void scale(float x, float y, float z) {
		last().pose.scale(x, y, z);
	}

	public void rotateAround(Quaternionfc rotation, float x, float y, float z) {
		last().pose.rotateAround(rotation, x, y, z);
	}

	public void pushPose() {
		poses.addLast(new Pose(new Matrix4f(last().pose)));
	}

	public void popPose() {
		poses.removeLast();
	}

	public Pose last() {
		return poses.getLast();
	}

	public record Pose(Matrix4f pose) {
	}
}
*///?}
