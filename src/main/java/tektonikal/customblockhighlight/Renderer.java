package tektonikal.customblockhighlight;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.opengl.GL11;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;


public class Renderer {
    static Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
    public static ArrayList<Vertexer.Line> lines = new ArrayList<>();

    public static void drawBoxFill(MatrixStack ms, Box box, Color cols, Color col2, float[] alpha) {
        ms.push();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        setup();
        if (BlockHighlightConfig.INSTANCE.getConfig().fillDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vertexer.vertexBoxQuads(ms, buffer, moveToZero(box), cols, col2, alpha);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end();
        ms.pop();
    }

    public static void drawBoxOutline(MatrixStack ms, Box box, Color color, Color col2, float[] alpha, float lineWidth) {
        ms.push();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        setup();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        if (BlockHighlightConfig.INSTANCE.getConfig().lineDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.lineWidth(lineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexBoxLines(ms, buffer, moveToZero(box), color, col2, alpha);
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        ms.pop();
    }

    public static void drawEdgeOutline(MatrixStack matrices, VoxelShape shape, Color c1, Color c2, float alpha, float lineWidth) {
        matrices.push();
        matrices.translate(shape.getBoundingBox().minX - camera.getPos().x, shape.getBoundingBox().minY - camera.getPos().y, shape.getBoundingBox().minZ - camera.getPos().z);
        setup();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        if (BlockHighlightConfig.INSTANCE.getConfig().lineDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.lineWidth(lineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        VoxelShape finalShape = shape;
        moveToZero(shape).forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float k = (float) (maxX - minX);
            float l = (float) (maxY - minY);
            float m = (float) (maxZ - minZ);
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;
            lines.add(new Vertexer.Line(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), new Vec3d(k, l, m), getMinVec(finalShape.getBoundingBox())));
        });
        lines.sort(Comparator.comparing(Vertexer.Line::getDistance).reversed());
        shape = moveToZero(shape);
        VoxelShape finalShape1 = shape;
        double blegh = moveToZero(shape).getBoundingBox().getMinPos().distanceTo(shape.getBoundingBox().getMaxPos());
        lines.forEach(line -> Vertexer.vertexLine(matrices, buffer, (float) (line.minPos.x), (float) (line.minPos.y), (float) (line.minPos.z ), (float) (line.maxPos.x ), (float) (line.maxPos.y ), (float) (line.maxPos.z), getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha), (float) line.normal.x, (float) line.normal.y, (float) line.normal.z));
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        matrices.pop();
    }

    public static Vec3d getMinVec(Box box) {
        return new Vec3d(box.minX, box.minY, box.minZ);
    }

    public static Box moveToZero(Box box) {
        return box.offset(getMinVec(box).negate());
    }
    public static VoxelShape moveToZero(VoxelShape shape){
        return shape.offset(getMinVec(shape.getBoundingBox()).x * -1, getMinVec(shape.getBoundingBox()).y * -1, getMinVec(shape.getBoundingBox()).z * -1);
    }

    //TODO: clean up these calls, some of them are useless iirc
    public static void setup() {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
    }

    public static void end() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public static Color getLerpedColor(Color c1, Color c2, float percent) {
        return new Color(MathHelper.lerp(percent, c1.getRed(), c2.getRed()), MathHelper.lerp(percent, c1.getGreen(), c2.getGreen()), MathHelper.lerp(percent, c1.getBlue(), c2.getBlue()));
    }
}