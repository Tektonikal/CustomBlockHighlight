package tektonikal.customblockhighlight;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.opengl.GL11;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;


public class Renderer {
    static Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

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

    public static void drawEdgeOutline(MatrixStack matrices, VoxelShape shape, double offsetX, double offsetY, double offsetZ, Color c1, Color c2, float alpha, float lineWidth) {
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
        double blegh = shape.getBoundingBox().getMinPos().distanceTo(shape.getBoundingBox().getMaxPos());
        ArrayList<Vertexer.Line> sides = new ArrayList<>();
        shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float k = (float) (maxX - minX);
            float l = (float) (maxY - minY);
            float m = (float) (maxZ - minZ);
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;
            sides.add(new Vertexer.Line(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), new Vec3d(k, l, m)));
        });
        sides.sort(Comparator.comparing(Vertexer.Line::getDistance));
        sides.forEach(line -> Vertexer.vertexLine(matrices, buffer, (float) (line.minPos.x + offsetX), (float) (line.minPos.y + offsetY), (float) (line.minPos.z + offsetZ), (float) (line.maxPos.x + offsetX), (float) (line.maxPos.y + offsetY), (float) (line.maxPos.z + offsetZ), getLerpedColor(c1, c2, (float) (shape.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (shape.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha), (float) line.normal.x, (float) line.normal.y, (float) line.normal.z));
//            Vertexer.vertexLine(matrices, buffer, (float) (minX + offsetX), (float) (minY + offsetY), (float) (minZ + offsetZ), (float) (maxX + offsetX), (float) (maxY + offsetY), (float) (maxZ + offsetZ), getLerpedColor(c1, c2, (float) (shape.getBoundingBox().getMinPos().distanceTo(new Vec3d(minX, minY, minZ)) / blegh)), getLerpedColor(c1, c2, (float) (shape.getBoundingBox().getMinPos().distanceTo(new Vec3d(maxX, maxY, maxZ)) / blegh)), Math.round(alpha), k, l, m);
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static Vec3d getMinVec(Box box) {
        return new Vec3d(box.minX, box.minY, box.minZ);
    }

    public static Box moveToZero(Box box) {
        return box.offset(getMinVec(box).negate());
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