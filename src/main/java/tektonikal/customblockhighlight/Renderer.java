package tektonikal.customblockhighlight;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Renderer {
    //    public static RenderPhase.DepthTest DISABLED = new RenderPhase.DepthTest("never", 512);
    static Direction[] lineDirs;
    static Direction[] fillDirs;

    public static void drawBoxFill(MatrixStack ms, Box box, int[] cols, OutlineType fillType, Direction dir, Direction[] prevDirs, Direction... excludeDirs) {
        ms.push();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (!fillType.equals(OutlineType.DEFAULT)) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        if (!BlockHighlightConfig.INSTANCE.getConfig().blending) {
            //TODO
//            RenderSystem.blendEquation(32769);
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Direction[] bleh = new Direction[1];
        if (fillType.equals(OutlineType.LOOKAT)) {
            EnumSet<Direction> temp = EnumSet.allOf(Direction.class);
            temp.remove(dir);
            excludeDirs = temp.toArray(new Direction[0]);
        }
        Vertexer.vertexBoxQuads(ms, buffer, moveToZero(box), cols, fillType.equals(OutlineType.AIR_EXPOSED) || fillType.equals(OutlineType.LOOKAT) ? excludeDirs : fillType.equals(OutlineType.CONCEALED) ? invert(excludeDirs) : bleh);
        tessellator.draw();
        fillDirs = excludeDirs;
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        ms.pop();
    }

    private static Direction[] invert(Direction[] invertDirs) {
        EnumSet<Direction> dirs = EnumSet.allOf(Direction.class);
        for (Direction d : invertDirs) {
            dirs.remove(d);
        }
        return dirs.toArray(new Direction[0]);
    }


    public static void drawBoxOutline(MatrixStack ms, Box box, int[] color, float lineWidth, OutlineType type, Direction dir, Direction[] prevDirs, Direction... excludeDirs) {
        ms.push();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        if (type != OutlineType.DEFAULT) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        if (type == OutlineType.LOOKAT) {
            EnumSet<Direction> temp = EnumSet.allOf(Direction.class);
            temp.remove(dir);
            excludeDirs = temp.toArray(new Direction[0]);
        }
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexBoxLines(ms, buffer, moveToZero(box), color, type.equals(OutlineType.AIR_EXPOSED) || type.equals(OutlineType.LOOKAT) ? excludeDirs : type.equals(OutlineType.CONCEALED) ? invert(excludeDirs) : null);
        tessellator.draw();
        lineDirs = excludeDirs;
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        ms.pop();
    }

    public static Vec3d getMinVec(Box box) {
        return new Vec3d(box.minX, box.minY, box.minZ);
    }

    public static Box moveToZero(Box box) {
        return box.offset(getMinVec(box).negate());
    }

    public static Direction[] getLineDirs() {
        return lineDirs;
    }

    public static Direction[] getFillDirs() {
        return fillDirs;
    }
}