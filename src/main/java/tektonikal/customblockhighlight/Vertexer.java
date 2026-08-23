package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;

public class Vertexer {
	public static void vertexBoxQuads(PoseStack.Pose pose, VertexConsumer builder, AABB box, Color cols, Color col2, float[] alpha) {
		float normaliser = (float) box.getMinPosition().distanceTo(box.getMaxPosition());
		vertexQuad(pose, builder, cols, col2, Math.round(alpha[0]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.minY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.minY, (float) box.maxZ));
		vertexQuad(pose, builder, cols, col2, Math.round(alpha[1]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.minZ));
		vertexQuad(pose, builder, cols, col2, Math.round(alpha[2]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.minY, (float) box.minZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.minZ));
		vertexQuad(pose, builder, cols, col2, Math.round(alpha[3]), box.getMinPosition(), normaliser, new Vec3((float) box.maxX, (float) box.minY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.minY, (float) box.maxZ));
		vertexQuad(pose, builder, cols, col2, Math.round(alpha[4]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.minY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.minX, (float) box.minY, (float) box.minZ));
		vertexQuad(pose, builder, cols, col2, Math.round(alpha[5]), box.getMinPosition(), normaliser, new Vec3((float) box.maxX, (float) box.minY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.maxZ));
	}
	public static void vertexQuad(PoseStack.Pose pose, VertexConsumer builder, Color c1, Color c2, int alpha, Vec3 minPos, float normaliser, Vec3... vecs) {
		Color[] cols = new Color[vecs.length];
		for(int i = 0; i < vecs.length; i++){
			cols[i] = getLerpedColor(c1, c2, (float) (minPos.distanceTo(vecs[i]) / normaliser));
		}
		builder.addVertex(pose, vecs[0].toVector3f()).setColor(cols[3].getRed(), cols[3].getGreen(), cols[3].getBlue(), alpha);
		builder.addVertex(pose, vecs[1].toVector3f()).setColor(cols[2].getRed(), cols[2].getGreen(), cols[2].getBlue(), alpha);
		builder.addVertex(pose, vecs[2].toVector3f()).setColor(cols[1].getRed(), cols[1].getGreen(), cols[1].getBlue(), alpha);
		builder.addVertex(pose, vecs[3].toVector3f()).setColor(cols[0].getRed(), cols[0].getGreen(), cols[0].getBlue(), alpha);
	}

	public static Color getLerpedColor(Color c1, Color c2, float percent) {
		return new Color(Math.clamp(Mth.lerpInt(percent, c1.getRed(), c2.getRed()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getGreen(), c2.getGreen()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getBlue(), c2.getBlue()), 0, 255));
	}

	public static void vertexBoxLines(PoseStack.Pose pose, VertexConsumer builder, AABB box, Color col, Color col2, float[] alpha, float width, float cutFromCenter, float cutFromCorner) {
		float x1 = (float) box.minX;
		float y1 = (float) box.minY;
		float z1 = (float) box.minZ;
		float x2 = (float) box.maxX;
		float y2 = (float) box.maxY;
		float z2 = (float) box.maxZ;
		double normaliser = box.getMinPosition().distanceTo(box.getMaxPosition());
		Color x1y1z1 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y1, z1)) / normaliser));
		Color x2y1z1 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y1, z1)) / normaliser));
		Color x1y1z2 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y1, z2)) / normaliser));
		Color x1y2z2 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y2, z2)) / normaliser));
		Color x2y2z2 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y2, z2)) / normaliser));
		Color x2y2z1 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y2, z1)) / normaliser));
		Color x1y2z1 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y2, z1)) / normaliser));
		Color x2y1z2 = getLerpedColor(col, col2, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y1, z2)) / normaliser));

        /*
        (facing west)
               +--------+ <- start here with col1 (min X, max Y, min Z)
              /        /|
             /        / |
     2/3 -> +--------+  | <- 1/3 of the way there
            |        |  |
            |        |  +
            |        | /
            |        |/
   final -> +--------+
         */
		//i don't wanna bother checking for <0.5 alpha here, surely it makes no difference?
		//down
		vertexLine(pose, builder, x1, y1, z1, x2, y1, z1, x1y1z1, x2y1z1, Math.round(Math.max(alpha[0], alpha[2])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y1, z1, x1, y1, z2, x1y1z1, x1y1z2, Math.round(Math.max(alpha[4], alpha[0])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x2, y1, z1, x2, y1, z2, x2y1z1, x2y1z2, Math.round(Math.max(alpha[5], alpha[0])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y1, z2, x2, y1, z2, x1y1z2, x2y1z2, Math.round(Math.max(alpha[3], alpha[0])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
		//west
		vertexLine(pose, builder, x1, y1, z2, x1, y2, z2, x1y1z2, x1y2z2, Math.round(Math.max(alpha[3], alpha[4])), 0, 1, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y1, z1, x1, y2, z1, x1y1z1, x1y2z1, Math.round(Math.max(alpha[2], alpha[4])), 0, 1, 0, width, cutFromCenter, cutFromCorner);
		//east
		vertexLine(pose, builder, x2, y1, z2, x2, y2, z2, x2y1z2, x2y2z2, Math.round(Math.max(alpha[3], alpha[5])), 0, -1, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x2, y1, z1, x2, y2, z1, x2y1z1, x2y2z1, Math.round(Math.max(alpha[2], alpha[5])), 0, 1, 0, width, cutFromCenter, cutFromCorner);
		//north and south are skipped, as they are not needed

		//up
		vertexLine(pose, builder, x1, y2, z1, x2, y2, z1, x1y2z1, x2y2z1, Math.round(Math.max(alpha[2], alpha[1])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y2, z1, x1, y2, z2, x1y2z1, x1y2z2, Math.round(Math.max(alpha[4], alpha[1])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x2, y2, z1, x2, y2, z2, x2y2z1, x2y2z2, Math.round(Math.max(alpha[5], alpha[1])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y2, z2, x2, y2, z2, x1y2z2, x2y2z2, Math.round(Math.max(alpha[3], alpha[1])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
	}

	public static Vec3 screenSpaceToWorldSpace(double x, double y, double d) {
		Camera camera = Renderer.mc.getEntityRenderDispatcher().camera;
		int displayHeight = Renderer.mc.getWindow().getGuiScaledHeight();
		int displayWidth = Renderer.mc.getWindow().getGuiScaledWidth();
		int[] viewport = new int[4];
		viewport[0] = 0;
		viewport[1] = 0;
		viewport[2] = 128;
		viewport[3] = 128;
		Vector3f target = new Vector3f();

		Matrix4f matrixProj = new Matrix4f(Renderer.lastProjMat);
		Matrix4f matrixModel = new Matrix4f(Renderer.lastModMat);

		matrixProj.mul(matrixModel)
				.mul(Renderer.lastWorldSpaceMatrix)
				.unproject((float) x / displayWidth * viewport[2],
						(float) (displayHeight - y) / displayHeight * viewport[3], (float) d, viewport, target);

		return new Vec3(target.x, target.y, target.z).add(camera.position());
	}

	public static Vec3 worldSpaceToScreenSpace(Vec3 pos) {
		Camera camera = Renderer.mc.getEntityRenderDispatcher().camera;
		int displayHeight = Renderer.mc.getWindow().getGuiScaledHeight();
		int[] viewport = new int[4];
		viewport[0] = 0;
		viewport[1] = 0;
		viewport[2] = 128;
		viewport[3] = 128;
		Vector3f target = new Vector3f();

		double deltaX = pos.x - camera.position().x;
		double deltaY = pos.y - camera.position().y;
		double deltaZ = pos.z - camera.position().z;

		Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(
				Renderer.lastWorldSpaceMatrix);

		Matrix4f matrixProj = new Matrix4f(Renderer.lastProjMat);
		Matrix4f matrixModel = new Matrix4f(Renderer.lastModMat);

		matrixProj.mul(matrixModel)
				.project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport,
						target);

		return new Vec3(target.x / Renderer.mc.getWindow().getGuiScale(),
				(displayHeight - target.y) / Renderer.mc.getWindow().getGuiScale(), target.z);
	}

	private static int interp(int in1, int in2, int mul) {
		if (in1 != in2) {
			int diff = ((Math.max(in1, in2) - Math.min(in1, in2)) / 3);
			return in1 > in2 ? in2 + (diff * (mul == 2 ? 1 : 2)) : in1 + diff * mul;
		}
		return in1;
	}

	public static void vertexLine(PoseStack.Pose pose, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, Color cols, Color col2, int alpha, float nx, float ny, float nz, float width, float cutFromCenter, float cutFromCorner) {
		if (cutFromCenter == 0 && cutFromCorner == 0) {
			builder.addVertex(pose, x1, y1, z1).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);
			builder.addVertex(pose, x2, y2, z2).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);
			return;
		}
		/*

		--------------------------------------------------------
		^         ^                           ^         ^
		minOuter, minInner                    maxInner, maxOuter

		*/
		Vector3f v1 = new Vector3f(x1, y1, z1);
		Vector3f v2 = new Vector3f(x2, y2, z2);
		Vector3f minOuter = new Vector3f();
		Vector3f maxOuter = new Vector3f();
		v1.lerp(v2, cutFromCorner / 2, minOuter);
		v2.lerp(v1, cutFromCorner / 2, maxOuter);
		if (cutFromCenter == 0) {
			//draw only one line
			builder.addVertex(pose, minOuter.x, minOuter.y, minOuter.z).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);
			builder.addVertex(pose, maxOuter.x, maxOuter.y, maxOuter.z).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);
		} else {
			Vector3f center = new Vector3f();
			v1.lerp(v2, 0.5F, center);
			Vector3f minInner = new Vector3f();
			Vector3f maxInner = new Vector3f();
			center.lerp(v1, cutFromCenter, minInner);
			center.lerp(v2, cutFromCenter, maxInner);

			float yeah = Math.clamp(minInner.distance(minOuter) / minOuter.distance(maxOuter), 0, 1);
			Color minInnerCol = new Color((int) Mth.lerp(yeah, cols.getRed(), col2.getRed()), (int) Mth.lerp(yeah, cols.getGreen(), col2.getGreen()), (int) Mth.lerp(yeah, cols.getBlue(), col2.getBlue()));
			Color maxInnerCol = new Color((int) Mth.lerp(1 - yeah, cols.getRed(), col2.getRed()), (int) Mth.lerp(1 - yeah, cols.getGreen(), col2.getGreen()), (int) Mth.lerp(1 - yeah, cols.getBlue(), col2.getBlue()));

			builder.addVertex(pose, minOuter.x, minOuter.y, minOuter.z).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);
			builder.addVertex(pose, minInner.x, minInner.y, minInner.z).setColor(minInnerCol.getRed(), minInnerCol.getGreen(), minInnerCol.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);

			builder.addVertex(pose, maxInner.x, maxInner.y, maxInner.z).setColor(maxInnerCol.getRed(), maxInnerCol.getGreen(), maxInnerCol.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);
			builder.addVertex(pose, maxOuter.x, maxOuter.y, maxOuter.z).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(pose, nx, ny, nz).setLineWidth(width);
		}
	}

//	private static float getWidth(int layer) {
//		return switch (layer) {
//			case 0 -> config().lineWidth;
//			case 1 -> config().slineWidth;
//			case 2 -> config().tlineWidth;
//			default -> 1;
//		};
//	}

	public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
		float xNormal = x2 - x1;
		float yNormal = y2 - y1;
		float zNormal = z2 - z1;
		float normalSqrt = Mth.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

		return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
	}
}