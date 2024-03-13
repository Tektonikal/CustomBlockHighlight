package tektonikal.blockhighlight;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Renderer {
//    public static RenderPhase.DepthTest DISABLED = new RenderPhase.DepthTest("never", 512);

    public static void drawBoxFill(Box box, int[] cols, OutlineType fillType, Direction dir, Direction... excludeDirs) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if(!fillType.equals(OutlineType.DEFAULT)){
            RenderSystem.disableDepthTest();
        }
        else{
            RenderSystem.enableDepthTest();
        }
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
//        RenderLayer.MultiPhaseParameters.Builder builder = RenderLayer.MultiPhaseParameters.builder();
//        builder.depthTest(RenderPhase.LEQUAL_DEPTH_TEST);
        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Direction[] bleh = new Direction[1];
        if(fillType.equals(OutlineType.LOOKAT)){
            List<Direction> temp = new ArrayList<>(Arrays.asList(Direction.values()));
            temp.remove(dir);
            excludeDirs = fix(temp.toArray());
        }
        Vertexer.vertexBoxQuads(matrices, buffer, moveToZero(box), cols, fillType.equals(OutlineType.AIR_EXPOSED) || fillType.equals(OutlineType.LOOKAT) ? excludeDirs : fillType.equals(OutlineType.CONCEALED) ? invert(excludeDirs) : bleh);
        tessellator.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static Direction[] invert(Direction[] excludeDirs) {
        ArrayList<Direction> ret = new ArrayList<>(Arrays.asList(Direction.values()));
        for (Direction dir : excludeDirs) {
            ret.remove(dir);
        }
        return fix(ret.toArray());
    }

    private static Direction[] fix(Object[] toArray) {
        //TRUST ME I KNOW WHAT I'M DOING IT DOESN'T WORK OTHERWISE
        Direction[] temp = new Direction[6];
        for(int i = 0; i < toArray.length; i++){
            temp[i] = (Direction) toArray[i];
        }
        return temp;
    }


    public static void drawBoxOutline(Box box, int[] color, float lineWidth, OutlineType type, Direction dir, Direction... excludeDirs) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.disableCull();
        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        if(type != OutlineType.DEFAULT){
            RenderSystem.disableDepthTest();
        }
        else{
            RenderSystem.enableDepthTest();
        }
        if(type == OutlineType.LOOKAT){
            List<Direction> temp = new ArrayList<>(Arrays.asList(Direction.values()));
            temp.remove(dir);
            excludeDirs = fix(temp.toArray());
        }
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexBoxLines(matrices, buffer, moveToZero(box), color, type.equals(OutlineType.AIR_EXPOSED) || type.equals(OutlineType.LOOKAT) ? excludeDirs : type.equals(OutlineType.CONCEALED) ? invert(excludeDirs) : null);
        tessellator.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);
        return matrices;
    }
    public static Vec3d getMinVec(Box box) {
        return new Vec3d(box.minX, box.minY, box.minZ);
    }
    public static Box moveToZero(Box box) {
        return box.offset(getMinVec(box).negate());
    }
}