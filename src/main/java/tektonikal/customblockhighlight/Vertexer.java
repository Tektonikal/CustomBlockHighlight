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
		Color firstThird = new Color(interp(cols.getRed(), col2.getRed(), 1), interp(cols.getGreen(), col2.getGreen(), 1), interp(cols.getBlue(), col2.getBlue(), 1), 255);
		Color secondThird = new Color(interp(cols.getRed(), col2.getRed(), 2), interp(cols.getGreen(), col2.getGreen(), 2), interp(cols.getBlue(), col2.getBlue(), 2), 255);

		//TODO: good news and bad news!
		// good news is that new rendering system means i don't have to sort these faces for whatever reason?
		// bad news is that the invert feature is dead
		vertexQuad(pose, builder, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, (float) box.minX, (float) box.minY, (float) box.maxZ, secondThird, col2, secondThird, firstThird, Math.round(alpha[0]));
		vertexQuad(pose, builder, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.minX, (float) box.maxY, (float) box.minZ, cols, firstThird, secondThird, firstThird, Math.round(alpha[1]));
		vertexQuad(pose, builder, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.minX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.minZ, secondThird, firstThird, cols, firstThird, Math.round(alpha[2]));
		vertexQuad(pose, builder, (float) box.maxX, (float) box.minY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.minY, (float) box.maxZ, secondThird, firstThird, secondThird, col2, Math.round(alpha[3]));
		vertexQuad(pose, builder, (float) box.minX, (float) box.minY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.minZ, (float) box.minX, (float) box.minY, (float) box.minZ, firstThird, cols, firstThird, secondThird, Math.round(alpha[4]));
		vertexQuad(pose, builder, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, col2, secondThird, firstThird, secondThird, Math.round(alpha[5]));

	}

	public static void vertexQuad(PoseStack.Pose pose, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Color cols, Color col2, Color col3, Color col4, int alpha) {
		builder.addVertex(pose, x1, y1, z1).setColor(col4.getRed(), col4.getGreen(), col4.getBlue(), alpha);
		builder.addVertex(pose, x2, y2, z2).setColor(col3.getRed(), col3.getGreen(), col3.getBlue(), alpha);
		builder.addVertex(pose, x3, y3, z3).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha);
		builder.addVertex(pose, x4, y4, z4).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha);
	}

	public static void vertexBoxLines(PoseStack.Pose pose, VertexConsumer builder, AABB box, Color cols, Color col2, float[] alpha, float width, float cutFromCenter, float cutFromCorner) {
		float x1 = (float) box.minX;
		float y1 = (float) box.minY;
		float z1 = (float) box.minZ;
		float x2 = (float) box.maxX;
		float y2 = (float) box.maxY;
		float z2 = (float) box.maxZ;
		Vec3 vec = box.getCenter();
		Color firstThird = new Color(interp(cols.getRed(), col2.getRed(), 1), interp(cols.getGreen(), col2.getGreen(), 1), interp(cols.getBlue(), col2.getBlue(), 1), 255);
		Color secondThird = new Color(interp(cols.getRed(), col2.getRed(), 2), interp(cols.getGreen(), col2.getGreen(), 2), interp(cols.getBlue(), col2.getBlue(), 2), 255);
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
		vertexLine(pose, builder, x1, y1, z1, x2, y1, z1, firstThird, secondThird, Math.round(Math.max(alpha[0], alpha[2])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y1, z1, x1, y1, z2, firstThird, secondThird, Math.round(Math.max(alpha[4], alpha[0])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x2, y1, z1, x2, y1, z2, secondThird, col2, Math.round(Math.max(alpha[5], alpha[0])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y1, z2, x2, y1, z2, secondThird, col2, Math.round(Math.max(alpha[3], alpha[0])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
		//west
		vertexLine(pose, builder, x1, y1, z2, x1, y2, z2, secondThird, firstThird, Math.round(Math.max(alpha[3], alpha[4])), 0, 1, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y1, z1, x1, y2, z1, firstThird, cols, Math.round(Math.max(alpha[2], alpha[4])), 0, 1, 0, width, cutFromCenter, cutFromCorner);
		//east
		vertexLine(pose, builder, x2, y1, z2, x2, y2, z2, col2, secondThird, Math.round(Math.max(alpha[3], alpha[5])), 0, -1, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x2, y1, z1, x2, y2, z1, secondThird, firstThird, Math.round(Math.max(alpha[2], alpha[5])), 0, 1, 0, width, cutFromCenter, cutFromCorner);
		//north and south are skipped, as they are not needed

		//up
		vertexLine(pose, builder, x1, y2, z1, x2, y2, z1, cols, firstThird, Math.round(Math.max(alpha[2], alpha[1])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y2, z1, x1, y2, z2, cols, firstThird, Math.round(Math.max(alpha[4], alpha[1])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x2, y2, z1, x2, y2, z2, firstThird, secondThird, Math.round(Math.max(alpha[5], alpha[1])), 0, 0, 1, width, cutFromCenter, cutFromCorner);
		vertexLine(pose, builder, x1, y2, z2, x2, y2, z2, firstThird, secondThird, Math.round(Math.max(alpha[3], alpha[1])), 1, 0, 0, width, cutFromCenter, cutFromCorner);
		//corner to center
		//hmm. i know which direction the vector will be but not its length, hmm
//		Vector3f normal = getNormal(x1, y2, z1, (float) vec.x, (float) vec.y, (float) vec.z);
//		vertexLine(matrices, builder, x1, y2, z1, (float) vec.x, (float) vec.y, (float) vec.z, cols, firstThird, Math.round(Math.max(alpha[2], alpha[1])), normal.x, normal.y, normal.z, layer);
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
			Color minInnerCol = new Color((int) Mth.lerp(yeah, cols.getRed(), col2.getRed()),  (int) Mth.lerp(yeah, cols.getGreen(), col2.getGreen()), (int) Mth.lerp(yeah, cols.getBlue(), col2.getBlue()));
			Color maxInnerCol = new Color((int) Mth.lerp(1 - yeah, cols.getRed(), col2.getRed()), (int) Mth.lerp( 1 - yeah, cols.getGreen(), col2.getGreen()), (int) Mth.lerp( 1 - yeah, cols.getBlue(), col2.getBlue()));

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