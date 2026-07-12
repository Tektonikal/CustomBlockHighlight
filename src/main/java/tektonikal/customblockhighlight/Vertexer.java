package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Vertexer {
	public static Minecraft mc = Minecraft.getInstance();

	record Side(float distance, Direction direction) {
	}

	public static void vertexBoxQuads(PoseStack matrices, VertexConsumer builder, AABB box, Color cols, Color col2, float[] alpha) {
		Color firstThird = new Color(interp(cols.getRed(), col2.getRed(), 1), interp(cols.getGreen(), col2.getGreen(), 1), interp(cols.getBlue(), col2.getBlue(), 1), 255);
		Color secondThird = new Color(interp(cols.getRed(), col2.getRed(), 2), interp(cols.getGreen(), col2.getGreen(), 2), interp(cols.getBlue(), col2.getBlue(), 2), 255);
		List<Side> sides = new ArrayList<>();
		for (int i = 0; i < alpha.length; i++) {
			AABB aabb;
			if (mc.hitResult instanceof BlockHitResult block) {
				aabb = new AABB(block.getBlockPos());
			} else if (mc.hitResult instanceof EntityHitResult entity) {
				aabb = entity.getEntity().getBoundingBox();
			} else {
				aabb = new AABB(BlockPos.ZERO);
			}
			sides.add(new Side(getCenter(Direction.from3DDataValue(i), aabb).toVector3f().distance(Minecraft.getInstance().gameRenderer.mainCamera().position().toVector3f()), Direction.from3DDataValue(i)));
		}
		sides.sort(Comparator.comparing(Side::distance));
		if (BlockHighlightConfig.INSTANCE.instance().invert) {
			sides = sides.reversed();
		}

		for (int i = 0; i < alpha.length; i++) {
			var direction = sides.get(i).direction();
			drawSide(matrices, builder, box, cols, col2, firstThird, secondThird, alpha[direction.ordinal()], direction);
		}
	}

	private static Vec3 getCenter(Direction direction, AABB box) {
		Vector3d avgVec = new Vector3d((box.minX + box.maxX) / 2.0F, (box.minY + box.maxY) / 2.0F, (box.minZ + box.maxZ) / 2.0F);
		switch (direction.getAxis()) {
			case X -> avgVec.x = direction == Direction.WEST ? box.minX : box.maxX;
			case Y -> avgVec.y = direction == Direction.DOWN ? box.minY : box.maxY;
			case Z -> avgVec.z = direction == Direction.NORTH ? box.minZ : box.maxZ;
		}
		return new Vec3(avgVec.x, avgVec.y, avgVec.z);
	}

	public static void drawSide(PoseStack matrices, VertexConsumer builder, AABB box, Color cols, Color col2, Color firstThird, Color secondThird, float relevantAlpha, Direction direction) {
		switch (direction) {
			case UP ->
					vertexQuad(matrices, builder, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.minX, (float) box.maxY, (float) box.minZ, cols, firstThird, secondThird, firstThird, Math.round(relevantAlpha));
			case SOUTH ->
					vertexQuad(matrices, builder, (float) box.maxX, (float) box.minY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.minY, (float) box.maxZ, secondThird, firstThird, secondThird, col2, Math.round(relevantAlpha));
			case NORTH ->
					vertexQuad(matrices, builder, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.minX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.minZ, secondThird, firstThird, cols, firstThird, Math.round(relevantAlpha));
			case EAST ->
					vertexQuad(matrices, builder, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, col2, secondThird, firstThird, secondThird, Math.round(relevantAlpha));
			case WEST ->
					vertexQuad(matrices, builder, (float) box.minX, (float) box.minY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.minZ, (float) box.minX, (float) box.minY, (float) box.minZ, firstThird, cols, firstThird, secondThird, Math.round(relevantAlpha));
			case DOWN ->
					vertexQuad(matrices, builder, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, (float) box.minX, (float) box.minY, (float) box.maxZ, secondThird, col2, secondThird, firstThird, Math.round(relevantAlpha));
		}
	}

	public static void vertexQuad(PoseStack matrices, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Color cols, Color col2, Color col3, Color col4, int alpha) {
		Matrix4f model = matrices.last().pose();
		builder.addVertex(model, x1, y1, z1).setColor(col4.getRed(), col4.getGreen(), col4.getBlue(), alpha);
		builder.addVertex(model, x2, y2, z2).setColor(col3.getRed(), col3.getGreen(), col3.getBlue(), alpha);
		builder.addVertex(model, x3, y3, z3).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha);
		builder.addVertex(model, x4, y4, z4).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha);
	}

	public static void vertexBoxLines(PoseStack matrices, VertexConsumer builder, AABB box, Color cols, Color col2, float[] alpha, int layer) {
		float x1 = (float) box.minX;
		float y1 = (float) box.minY;
		float z1 = (float) box.minZ;
		float x2 = (float) box.maxX;
		float y2 = (float) box.maxY;
		float z2 = (float) box.maxZ;
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
		vertexLine(matrices, builder, x1, y1, z1, x2, y1, z1, firstThird, secondThird, Math.round(Math.max(alpha[0], alpha[2])), 1, 0, 0, layer);
		vertexLine(matrices, builder, x1, y1, z1, x1, y1, z2, firstThird, secondThird, Math.round(Math.max(alpha[4], alpha[0])), 0, 0, 1, layer);
		vertexLine(matrices, builder, x2, y1, z1, x2, y1, z2, secondThird, col2, Math.round(Math.max(alpha[5], alpha[0])), 0, 0, 1, layer);
		vertexLine(matrices, builder, x1, y1, z2, x2, y1, z2, secondThird, col2, Math.round(Math.max(alpha[3], alpha[0])), 1, 0, 0, layer);
		//west
		vertexLine(matrices, builder, x1, y1, z2, x1, y2, z2, secondThird, firstThird, Math.round(Math.max(alpha[3], alpha[4])), 0, 1, 0, layer);
		vertexLine(matrices, builder, x1, y1, z1, x1, y2, z1, firstThird, cols, Math.round(Math.max(alpha[2], alpha[4])), 0, 1, 0, layer);

		//east
		vertexLine(matrices, builder, x2, y1, z2, x2, y2, z2, col2, secondThird, Math.round(Math.max(alpha[3], alpha[5])), 0, -1, 0, layer);
		vertexLine(matrices, builder, x2, y1, z1, x2, y2, z1, secondThird, firstThird, Math.round(Math.max(alpha[2], alpha[5])), 0, 1, 0, layer);

		//north and south are skipped, as they are not needed

		//up
		vertexLine(matrices, builder, x1, y2, z1, x2, y2, z1, cols, firstThird, Math.round(Math.max(alpha[2], alpha[1])), 1, 0, 0, layer);
		vertexLine(matrices, builder, x1, y2, z1, x1, y2, z2, cols, firstThird, Math.round(Math.max(alpha[4], alpha[1])), 0, 0, 1, layer);
		vertexLine(matrices, builder, x2, y2, z1, x2, y2, z2, firstThird, secondThird, Math.round(Math.max(alpha[5], alpha[1])), 0, 0, 1, layer);
		vertexLine(matrices, builder, x1, y2, z2, x2, y2, z2, firstThird, secondThird, Math.round(Math.max(alpha[3], alpha[1])), 1, 0, 0, layer);
	}

	private static int interp(int in1, int in2, int mul) {
		if (in1 != in2) {
			int diff = ((Math.max(in1, in2) - Math.min(in1, in2)) / 3);
			return in1 > in2 ? in2 + (diff * (mul == 2 ? 1 : 2)) : in1 + diff * mul;
		}
		return in1;
	}

	public static void vertexLine(PoseStack matrices, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, Color cols, Color col2, int alpha, float nx, float ny, float nz, int layer) {
		Matrix4f model = matrices.last().pose();
		int width = getWidth(layer);
		Vector3f v1 = new Vector3f(x1, y1, z1);
		Vector3f v2 = new Vector3f(x2, y2, z2);
		Vector3f center = new Vector3f();
		v1.lerp(v2, 0.5f, center);
		float distanceBetweenCornersHalved = (v1.distance(v2) / 2);
		float cutDistanceCorner = distanceBetweenCornersHalved * (1 - BlockHighlightConfig.INSTANCE.instance().cutFromCorner);
		float cutDistanceCenter = distanceBetweenCornersHalved * (1 - BlockHighlightConfig.INSTANCE.instance().cutFromCenter);

			boolean x = x1 == x2;
			boolean y = y1 == y2;
			boolean z = z1 == z2;
		if (BlockHighlightConfig.INSTANCE.instance().cutFromCorner == 0) {
			//draw only one line
			builder.addVertex(model, x ? x1 : center.x - cutDistanceCenter, y ? y1 : center.y - cutDistanceCenter, z ? z1 : center.z - cutDistanceCenter).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(width);
			builder.addVertex(model, x ? x2 : center.x + cutDistanceCenter, y ? y2 : center.y + cutDistanceCenter, z ? z2 : center.z + cutDistanceCenter).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(width);
		}else{
			builder.addVertex(model, x ? x1 : center.x - cutDistanceCenter, y ? y1 : center.y - cutDistanceCenter, z ? z1 : center.z - cutDistanceCenter).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(width);
			builder.addVertex(model, x ? x1 : x1 + cutDistanceCorner, y ? y1 : y1 + cutDistanceCorner, z ? z1 : z1 + cutDistanceCorner).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(width);

			builder.addVertex(model, x ? x2 : x2 - cutDistanceCorner, y ? y2 : y2 - cutDistanceCorner, z ? z2 : z2 - cutDistanceCorner).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(width);
			builder.addVertex(model, x ? x2 : center.x + cutDistanceCenter, y ? y2 : center.y + cutDistanceCenter, z ? z2 : center.z + cutDistanceCenter).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(width);
		}
	}

	public static void vertexLine(PoseStack matrices, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, Color cols, Color col2, int alpha, Vec3 normal, int layer) {
		Matrix4f model = matrices.last().pose();
		int width = getWidth(layer);
		builder.addVertex(model, x1, y1, z1).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(matrices.last(), (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(width);
		builder.addVertex(model, x2, y2, z2).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(matrices.last(), (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(width);
	}

	private static int getWidth(int layer) {
		return switch (layer) {
			case 0 -> BlockHighlightConfig.INSTANCE.instance().lineWidth;
			case 1 -> BlockHighlightConfig.INSTANCE.instance().slineWidth;
			case 2 -> BlockHighlightConfig.INSTANCE.instance().tlineWidth;
			default -> 1;
		};
	}
}