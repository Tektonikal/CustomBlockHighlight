package tektonikal.customblockhighlight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Vertexer {
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public static void vertexBoxQuads(MatrixStack matrices, BufferBuilder builder, Box box, Color cols, Color col2, float[] alpha) {
        Color firstThird = new Color(interp(cols.getRed(), col2.getRed(), 1), interp(cols.getGreen(), col2.getGreen(), 1), interp(cols.getBlue(), col2.getBlue(), 1), 255);
        Color secondThird = new Color(interp(cols.getRed(), col2.getRed(), 2), interp(cols.getGreen(), col2.getGreen(), 2), interp(cols.getBlue(), col2.getBlue(), 2), 255);
        ArrayList<Side> sides = new ArrayList<>();
        for (int i = 0; i < alpha.length; i++) {
            sides.add(new Side(getCenter(Direction.byId(i), new Box(((BlockHitResult) mc.crosshairTarget).getBlockPos())).toVector3f().distance(mc.player.getEyePos().toVector3f()), Direction.byId(i)));
        }
        if (BlockHighlightConfig.INSTANCE.getConfig().invert) {
            sides.sort(Comparator.comparing(Side::getDistance));
        } else {
            sides.sort(Comparator.comparing(Side::getDistance).reversed());
        }
        for (int i = 0; i < alpha.length; i++) {
            drawSide(matrices, builder, box, cols, col2, firstThird, secondThird, alpha, sides.get(i).dir);
        }
    }

    private static Vec3d getCenter(Direction direction, Box box) {
        switch (direction) {
            case UP -> {
                return new Vec3d((box.minX + box.maxX) / 2.0F, box.maxY, (box.minZ + box.maxZ) / 2.0F);
            }
            case DOWN -> {
                return new Vec3d((box.minX + box.maxX) / 2.0F, box.minY, (box.minZ + box.maxZ) / 2.0F);
            }
            case EAST -> {
                return new Vec3d(box.maxX, (box.minY + box.maxY) / 2.0F, (box.minZ + box.maxZ) / 2.0F);
            }
            case WEST -> {
                return new Vec3d(box.minX, (box.minY + box.maxY) / 2.0F, (box.minZ + box.maxZ) / 2.0F);
            }
            case NORTH -> {
                return new Vec3d((box.minX + box.maxX) / 2.0F, (box.minY + box.maxY) / 2.0F, box.minZ);
            }
            case SOUTH -> {
                return new Vec3d((box.minX + box.maxX) / 2.0F, (box.minY + box.maxY) / 2.0F, box.maxZ);
            }
        }
        return null;
    }

    public static void drawSide(MatrixStack matrices, BufferBuilder builder, Box box, Color cols, Color col2, Color firstThird, Color secondThird, float[] alpha, Direction d) {
        //IT WAS YOU !!!!!!
//        if (alpha[d.ordinal()] > 0.49F) {
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
//        }
    }

    public static void vertexQuad(MatrixStack matrices, BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Color cols, Color col2, Color col3, Color col4, int alpha) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        builder.vertex(model, x4, y4, z4).color(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha);
        builder.vertex(model, x3, y3, z3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha);
        builder.vertex(model, x2, y2, z2).color(col3.getRed(), col3.getGreen(), col3.getBlue(), alpha);
        builder.vertex(model, x1, y1, z1).color(col4.getRed(), col4.getGreen(), col4.getBlue(), alpha);
    }

    public static void vertexBoxLines(MatrixStack matrices, BufferBuilder builder, Box box, Color cols, Color col2, float[] alpha) {
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

        //north

        //south

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

    public static void vertexLine(MatrixStack matrices, BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2, Color cols, Color col2, int alpha, float nx, float ny, float nz) {
        Matrix4f model = matrices.peek().getPositionMatrix();

        builder.vertex(model, x1, y1, z1).color(cols.getRed(), cols.getGreen(), cols.getBlue(), alpha).normal(matrices.peek(), nx, ny, nz);
        builder.vertex(model, x2, y2, z2).color(col2.getRed(), col2.getGreen(), col2.getBlue(), alpha).normal(matrices.peek(), nx, ny, nz);
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