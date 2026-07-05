package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Vertexer {
    public static Minecraft mc = Minecraft.getInstance();

    public static void vertexBoxQuads(PoseStack matrices, VertexConsumer builder, AABB box, Color cols, Color col2, float[] alpha) {
        Color firstThird = new Color(interp(cols.getRed(), col2.getRed(), 1), interp(cols.getGreen(), col2.getGreen(), 1), interp(cols.getBlue(), col2.getBlue(), 1), 255);
        Color secondThird = new Color(interp(cols.getRed(), col2.getRed(), 2), interp(cols.getGreen(), col2.getGreen(), 2), interp(cols.getBlue(), col2.getBlue(), 2), 255);
        ArrayList<Side> sides = new ArrayList<>();
        for (int i = 0; i < alpha.length; i++) {
            sides.add(new Side(getCenter(Direction.from3DDataValue(i), new AABB(Renderer.pos)).toVector3f().distance(Minecraft.getInstance().gameRenderer.mainCamera().position().toVector3f()), Direction.from3DDataValue(i)));
        }
        if (BlockHighlightConfig.INSTANCE.instance().invert) {
            sides.sort(Comparator.comparing(Side::getDistance));
        } else {
            sides.sort(Comparator.comparing(Side::getDistance).reversed());
        }
        for (int i = 0; i < alpha.length; i++) {
            drawSide(matrices, builder, box, cols, col2, firstThird, secondThird, alpha, sides.get(i).dir);
        }
    }

    private static Vec3 getCenter(Direction direction, AABB box) {
        switch (direction) {
            case UP -> {
                return new Vec3((box.minX + box.maxX) / 2.0F, box.maxY, (box.minZ + box.maxZ) / 2.0F);
            }
            case DOWN -> {
                return new Vec3((box.minX + box.maxX) / 2.0F, box.minY, (box.minZ + box.maxZ) / 2.0F);
            }
            case EAST -> {
                return new Vec3(box.maxX, (box.minY + box.maxY) / 2.0F, (box.minZ + box.maxZ) / 2.0F);
            }
            case WEST -> {
                return new Vec3(box.minX, (box.minY + box.maxY) / 2.0F, (box.minZ + box.maxZ) / 2.0F);
            }
            case NORTH -> {
                return new Vec3((box.minX + box.maxX) / 2.0F, (box.minY + box.maxY) / 2.0F, box.minZ);
            }
            case SOUTH -> {
                return new Vec3((box.minX + box.maxX) / 2.0F, (box.minY + box.maxY) / 2.0F, box.maxZ);
            }
        }
        return null;
    }

    public static void drawSide(PoseStack matrices, VertexConsumer builder, AABB box, Color cols, Color col2, Color firstThird, Color secondThird, float[] alpha, Direction d) {
            switch (d) {
                case UP ->
                        vertexQuad(matrices, builder, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.minX, (float) box.maxY, (float) box.minZ, cols, firstThird, secondThird, firstThird, Math.round(alpha[Direction.UP.ordinal()]));
                case SOUTH ->
                        vertexQuad(matrices, builder, (float) box.maxX, (float) box.minY, (float) box.maxZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.minY, (float) box.maxZ, secondThird, firstThird, secondThird, col2, Math.round(alpha[Direction.SOUTH.ordinal()]));
                case NORTH ->
                        vertexQuad(matrices, builder, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.minX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.minZ, secondThird, firstThird, cols, firstThird, Math.round(alpha[Direction.NORTH.ordinal()]));
                case EAST ->
                        vertexQuad(matrices, builder, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, col2, secondThird, firstThird, secondThird, Math.round(alpha[Direction.EAST.ordinal()]));
                case WEST ->
                        vertexQuad(matrices, builder, (float) box.minX, (float) box.minY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.maxZ, (float) box.minX, (float) box.maxY, (float) box.minZ, (float) box.minX, (float) box.minY, (float) box.minZ, firstThird, cols, firstThird, secondThird, Math.round(alpha[Direction.WEST.ordinal()]));
                case DOWN ->
                        vertexQuad(matrices, builder, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, (float) box.minX, (float) box.minY, (float) box.maxZ, secondThird, col2, secondThird, firstThird, Math.round(alpha[Direction.DOWN.ordinal()]));
            }
    }

    public static void vertexQuad(PoseStack matrices, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Color cols, Color col2, Color col3, Color col4, int alpha) {
        Matrix4f model = matrices.last().pose();
        builder.addVertex(model, x1, y1, z1).setColor(col4.getRed(), col4.getGreen(), col4.getBlue(), alpha);
        builder.addVertex(model, x2, y2, z2).setColor(col3.getRed(), col3.getGreen(), col3.getBlue(), alpha);
        builder.addVertex(model, x3, y3, z3).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha);
        builder.addVertex(model, x4, y4, z4).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha);
    }

    public static void vertexBoxLines(PoseStack matrices, VertexConsumer builder, AABB box, Color cols, Color col2, float[] alpha) {
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
        vertexLine(matrices, builder, x1, y1, z1, x2, y1, z1, firstThird, secondThird, Math.round(Math.max(alpha[0], alpha[2])), 1, 0, 0);
        vertexLine(matrices, builder, x1, y1, z1, x1, y1, z2, firstThird, secondThird, Math.round(Math.max(alpha[4], alpha[0])), 0, 0, 1);
        vertexLine(matrices, builder, x2, y1, z1, x2, y1, z2, secondThird, col2, Math.round(Math.max(alpha[5], alpha[0])), 0, 0, 1);
        vertexLine(matrices, builder, x1, y1, z2, x2, y1, z2, secondThird, col2, Math.round(Math.max(alpha[3], alpha[0])), 1, 0, 0);
        //west
        vertexLine(matrices, builder, x1, y1, z2, x1, y2, z2, secondThird, firstThird, Math.round(Math.max(alpha[3], alpha[4])), 0, 1, 0);
        vertexLine(matrices, builder, x1, y1, z1, x1, y2, z1, firstThird, cols, Math.round(Math.max(alpha[2], alpha[4])), 0, 1, 0);

        //east
        vertexLine(matrices, builder, x2, y2, z2, x2, y1, z2, secondThird, col2, Math.round(Math.max(alpha[3], alpha[5])), 0, -1, 0);
        vertexLine(matrices, builder, x2, y1, z1, x2, y2, z1, secondThird, firstThird, Math.round(Math.max(alpha[2], alpha[5])), 0, 1, 0);

        //north and south are skipped, as they are not needed

        //up
        vertexLine(matrices, builder, x1, y2, z1, x2, y2, z1, cols, firstThird, Math.round(Math.max(alpha[2], alpha[1])), 1, 0, 0);
        vertexLine(matrices, builder, x1, y2, z1, x1, y2, z2, cols, firstThird, Math.round(Math.max(alpha[4], alpha[1])), 0, 0, 1);
        vertexLine(matrices, builder, x2, y2, z1, x2, y2, z2, firstThird, secondThird, Math.round(Math.max(alpha[5], alpha[1])), 0, 0, 1);
        vertexLine(matrices, builder, x1, y2, z2, x2, y2, z2, firstThird, secondThird, Math.round(Math.max(alpha[3], alpha[1])), 1, 0, 0);
    }

    private static int interp(int in1, int in2, int mul) {
        if (in1 != in2) {
            int diff = ((Math.max(in1, in2) - Math.min(in1, in2)) / 3);
            return in1 > in2 ? in2 + (diff * (mul == 2 ? 1 : 2)) : in1 + diff * mul;
        }
        return in1;
    }

    public static void vertexLine(PoseStack matrices, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, Color cols, Color col2, int alpha, float nx, float ny, float nz) {
        Matrix4f model = matrices.last().pose();

        builder.addVertex(model, x1, y1, z1).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(BlockHighlightConfig.INSTANCE.instance().lineWidth);
        builder.addVertex(model, x2, y2, z2).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(matrices.last(), nx, ny, nz).setLineWidth(BlockHighlightConfig.INSTANCE.instance().lineWidth);
    }
    public static void vertexLine(PoseStack matrices, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, Color cols, Color col2, int alpha, Vec3 normal) {
        Matrix4f model = matrices.last().pose();

        builder.addVertex(model, x1, y1, z1).setColor(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).setNormal(matrices.last(), (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(BlockHighlightConfig.INSTANCE.instance().lineWidth);
        builder.addVertex(model, x2, y2, z2).setColor(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).setNormal(matrices.last(), (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(BlockHighlightConfig.INSTANCE.instance().lineWidth);
    }

    static class Side {
        public float distance;
        public Direction dir;

        public Side(float distance, Direction dir) {
            this.distance = distance;
            this.dir = dir;
        }
        public float getDistance() {
            return distance;
        }
    }
}