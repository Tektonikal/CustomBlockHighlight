package tektonikal.customblockhighlight.legacy.vertex;

//? if =1.8.9 {
/*import org.joml.Vector3f;

public interface VertexConsumer {
	VertexConsumer addVertex(float x, float y, float z);

	VertexConsumer setColor(int r, int g, int b, int a);

	VertexConsumer setLineWidth(float width);

	default VertexConsumer addVertex(PoseStack.Pose pose, float x, float y, float z) {
		Vector3f transformed = pose.pose().transformPosition(x, y, z, new Vector3f());
		return addVertex(transformed.x(), transformed.y(), transformed.z());
	}

	default VertexConsumer addVertex(PoseStack.Pose pose, Vector3f vec) {
		return addVertex(pose, vec.x(), vec.y(), vec.z());
	}

	// fixed function lines have no normals
	default VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {
		return this;
	}
}
*///?}
