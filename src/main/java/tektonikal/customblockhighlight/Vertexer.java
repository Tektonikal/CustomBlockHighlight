package tektonikal.customblockhighlight;
import org.apache.commons.lang3.ArrayUtils;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Vertexer {

    public static final int CULL_BACK = 0;
    public static final int CULL_FRONT = 1;
    public static final int CULL_NONE = 2;

    public static void vertexBoxQuads(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, int[] quadColor, Direction... dirs) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        int cullMode = dirs.length == 0 ? CULL_BACK : CULL_NONE;

        if (ArrayUtils.contains(dirs, Direction.DOWN)) {
            vertexQuad(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, cullMode, quadColor);
        }

        if (ArrayUtils.contains(dirs, Direction.WEST)) {
            vertexQuad(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, cullMode, quadColor);
        }

        if (ArrayUtils.contains(dirs, Direction.EAST)) {
            vertexQuad(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, cullMode, quadColor);
        }

        if (ArrayUtils.contains(dirs, Direction.NORTH)) {
            vertexQuad(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, cullMode, quadColor);
        }

        if (ArrayUtils.contains(dirs, Direction.SOUTH)) {
            vertexQuad(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, cullMode, quadColor);
        }

        if (ArrayUtils.contains(dirs, Direction.UP)) {
            vertexQuad(matrices, vertexConsumer, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, cullMode, quadColor);
        }
    }

    public static void vertexQuad(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int cullMode, int[] cols) {
        if (cullMode != CULL_FRONT) {
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x1, y1, z1).color(cols[0], cols[1], cols[2], cols[3]).next();
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x2, y2, z2).color(cols[0], cols[1], cols[2], cols[3]).next();
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x3, y3, z3).color(cols[0], cols[1], cols[2], cols[3]).next();
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x4, y4, z4).color(cols[0], cols[1], cols[2], cols[3]).next();
        }

        if (cullMode != CULL_BACK) {
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x4, y4, z4).color(cols[0], cols[1], cols[2], cols[3]).next();
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x3, y3, z3).color(cols[0], cols[1], cols[2], cols[3]).next();
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x2, y2, z2).color(cols[0], cols[1], cols[2], cols[3]).next();
            vertexConsumer.vertex(matrices.peek().getPositionMatrix(), x1, y1, z1).color(cols[0], cols[1], cols[2], cols[3]).next();
        }
    }

    public static void vertexBoxLines(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, int[] cols, Direction... dirs) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        boolean exDown = ArrayUtils.contains(dirs, Direction.DOWN);
        boolean exWest = ArrayUtils.contains(dirs, Direction.WEST);
        boolean exEast = ArrayUtils.contains(dirs, Direction.EAST);
        boolean exNorth = ArrayUtils.contains(dirs, Direction.NORTH);
        boolean exSouth = ArrayUtils.contains(dirs, Direction.SOUTH);
        boolean exUp = ArrayUtils.contains(dirs, Direction.UP);


        if (exDown) {
            vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z2, x1, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y1, z1, cols);
        }

        if (exWest) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, cols);
            if (!exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x1, y2, z2, cols);
        }

        if (exEast) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, cols);
            if (!exUp) vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, cols);
        }

        if (exNorth) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, cols);
            if (!exEast) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, cols);
            if (!exWest) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, cols);
            if (!exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, cols);
        }

        if (exSouth) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x1, y1, z2, x2, y1, z2, cols);
            if (!exEast) vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, cols);
            if (!exWest) vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, cols);
            if (!exUp) vertexLine(matrices, vertexConsumer, x1, y2, z2, x2, y2, z2, cols);
        }

        if (exUp) {
            vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, cols);
            vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y2, z2, x1, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y2, z2, x1, y2, z1, cols);
        }
    }
    public static void vertexBoxLines(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, int[] cols, int[] col2, Direction... dirs) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        boolean exDown = ArrayUtils.contains(dirs, Direction.DOWN);
        boolean exWest = ArrayUtils.contains(dirs, Direction.WEST);
        boolean exEast = ArrayUtils.contains(dirs, Direction.EAST);
        boolean exNorth = ArrayUtils.contains(dirs, Direction.NORTH);
        boolean exSouth = ArrayUtils.contains(dirs, Direction.SOUTH);
        boolean exUp = ArrayUtils.contains(dirs, Direction.UP);
        int[] firstThird = new int[]{interp(cols[0], col2[0], 3, 1), interp(cols[1], col2[1], 3, 1), interp(cols[2], col2[2], 3, 1), cols[3]};
        int[] secondThird = new int[]{interp(cols[0], col2[0], 3, 2), interp(cols[1], col2[1], 3, 2), interp(cols[2], col2[2], 3, 2), cols[3]};
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
        if (exUp) {
            vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, cols, firstThird);
            vertexLine(matrices, vertexConsumer, x1, y2, z1, x1, y2, z2, cols, firstThird);
            vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, firstThird, secondThird);
            vertexLine(matrices, vertexConsumer, x1, y2, z2, x2, y2, z2, firstThird, secondThird);
        }
        if (exDown) {
            vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, firstThird, secondThird);
            vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y1, z2, firstThird, secondThird);
            vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, secondThird, col2);
            vertexLine(matrices, vertexConsumer, x1, y1, z2, x2, y1, z2, secondThird, col2);
        }

        if (exWest) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y1, z2, firstThird, secondThird);
            vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, secondThird, firstThird);
            vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, firstThird, cols);
            if (!exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x1, y2, z2, cols, firstThird);
        }

        if (exEast) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, secondThird, col2);
            vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, col2, secondThird);
            vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, secondThird, firstThird);
            if (!exUp) vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, firstThird, secondThird);
        }

        if (exNorth) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, firstThird, secondThird);
            if (!exEast) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, secondThird, firstThird);
            if (!exWest) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, firstThird, cols);
            if (!exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, cols, firstThird);
        }

        if (exSouth) {
            if (!exDown) vertexLine(matrices, vertexConsumer, x1, y1, z2, x2, y1, z2, secondThird, col2);
            if (!exEast) vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, col2, secondThird);
            if (!exWest) vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, secondThird, firstThird);
            if (!exUp) vertexLine(matrices, vertexConsumer, x1, y2, z2, x2, y2, z2, firstThird, secondThird);
        }
    }

    private static int interp(int in1, int in2, int div, int mul){
        return Math.min(in1, in2) + (((Math.max(in1, in2) - Math.min(in1, in2)) / div) * mul);
    }

    public static void vertexLine(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, int[] cols) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();

        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);

        vertexConsumer.vertex(model, x1, y1, z1).color(cols[0], cols[1], cols[2], cols[3]).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
        vertexConsumer.vertex(model, x2, y2, z2).color(cols[0], cols[1], cols[2], cols[3]).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
    }
    public static void vertexLine(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, int[] cols, int[] col2) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();

        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);

        vertexConsumer.vertex(model, x1, y1, z1).color(cols[0], cols[1], cols[2], cols[3]).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
        vertexConsumer.vertex(model, x2, y2, z2).color(col2[0], col2[1], col2[2], cols[3]).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

}