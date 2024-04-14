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

    public static void vertexBoxQuads(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, int[] quadColor, Direction... excludeDirs) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        int cullMode = excludeDirs.length == 0 ? CULL_BACK : CULL_NONE;

        if (!ArrayUtils.contains(excludeDirs, Direction.DOWN)) {
            vertexQuad(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, cullMode, quadColor);
        }

        if (!ArrayUtils.contains(excludeDirs, Direction.WEST)) {
            vertexQuad(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, cullMode, quadColor);
        }

        if (!ArrayUtils.contains(excludeDirs, Direction.EAST)) {
            vertexQuad(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, cullMode, quadColor);
        }

        if (!ArrayUtils.contains(excludeDirs, Direction.NORTH)) {
            vertexQuad(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, cullMode, quadColor);
        }

        if (!ArrayUtils.contains(excludeDirs, Direction.SOUTH)) {
            vertexQuad(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, cullMode, quadColor);
        }

        if (!ArrayUtils.contains(excludeDirs, Direction.UP)) {
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

    public static void vertexBoxLines(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, int[] cols, Direction... excludeDirs) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        boolean exDown = ArrayUtils.contains(excludeDirs, Direction.DOWN);
        boolean exWest = ArrayUtils.contains(excludeDirs, Direction.WEST);
        boolean exEast = ArrayUtils.contains(excludeDirs, Direction.EAST);
        boolean exNorth = ArrayUtils.contains(excludeDirs, Direction.NORTH);
        boolean exSouth = ArrayUtils.contains(excludeDirs, Direction.SOUTH);
        boolean exUp = ArrayUtils.contains(excludeDirs, Direction.UP);


        if (!exDown) {
            vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z2, x1, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y1, z1, cols);
        }

        if (!exWest) {
            if (exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, cols);
            if (exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x1, y2, z2, cols);
        }

        if (!exEast) {
            if (exDown) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, cols);
            if (exUp) vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, cols);
        }

        if (!exNorth) {
            if (exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, cols);
            if (exEast) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, cols);
            if (exWest) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, cols);
            if (exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, cols);
        }

        if (!exSouth) {
            if (exDown) vertexLine(matrices, vertexConsumer, x1, y1, z2, x2, y1, z2, cols);
            if (exEast) vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, cols);
            if (exWest) vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, cols);
            if (exUp) vertexLine(matrices, vertexConsumer, x1, y2, z2, x2, y2, z2, cols);
        }

        if (!exUp) {
            vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, cols);
            vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x2, y2, z2, x1, y2, z2, cols);
            vertexLine(matrices, vertexConsumer, x1, y2, z2, x1, y2, z1, cols);
        }
    }

    public static void vertexLine(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, int[] cols) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();

        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);

        vertexConsumer.vertex(model, x1, y1, z1).color(cols[0], cols[1], cols[2], cols[3]).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
        vertexConsumer.vertex(model, x2, y2, z2).color(cols[0], cols[1], cols[2], cols[3]).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

}