package tektonikal.customblockhighlight.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import tektonikal.customblockhighlight.Vertexer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Line {
    public Vec3 minPos;
    public Vec3 maxPos;
    //TODO: remove minVec, i don't think it's used at all
    public Vec3 minVec;
    public float alphaMultiplier = 1;

    public Line(Vec3 minPos, Vec3 maxPos, Vec3 minVec) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.minVec = minVec;
    }

    public float getDistanceToCamera() {
        return (float) minPos.add(maxPos).scale(0.5F).distanceTo(Vertexer.mc.gameRenderer.mainCamera().position().subtract(minVec));
    }
    public float distanceTo(Vec3 pos) {
        return (float) minPos.add(maxPos).scale(0.5F).distanceTo(pos.subtract(minVec));
    }

    public Vec3 getNormal() {
        float k = (float) (maxPos.x - minPos.x);
        float l = (float) (maxPos.y - minPos.y);
        float m = (float) (maxPos.z - minPos.z);
        float n = Mth.sqrt(k * k + l * l + m * m);
        k /= n;
        l /= n;
        m /= n;
        return new Vec3(k, l, m);
    }

    public void render(PoseStack ms, BufferBuilder buf, Color c1, Color c2, int alpha, int layer) {
        Vertexer.vertexLine(ms, buf, (float) minPos.x, (float) minPos.y, (float) minPos.z, (float) maxPos.x, (float) maxPos.y, (float) maxPos.z, c1, c2, Math.round(alpha * alphaMultiplier), getNormal(), layer);
    }

    public void moveTo(Vec3 minPosTo, Vec3 maxPosTo, Vec3 minVecTo) {
        this.minPos = new Vec3(ease(this.minPos.x, minPosTo.x, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.minPos.y, minPosTo.y, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.minPos.z, minPosTo.z, BlockHighlightConfig.INSTANCE.instance().easeSpeed));
        this.maxPos = new Vec3(ease(this.maxPos.x, maxPosTo.x, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.maxPos.y, maxPosTo.y, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.maxPos.z, maxPosTo.z, BlockHighlightConfig.INSTANCE.instance().easeSpeed));
        this.minVec = new Vec3(ease(this.minVec.x, minVecTo.x, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.minVec.y, minVecTo.y, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(this.minVec.z, minVecTo.z, BlockHighlightConfig.INSTANCE.instance().easeSpeed));
    }

    public double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-(1.0F / Minecraft.getInstance().getFps()) * speed)));
    }
    public void update(boolean b){
        this.alphaMultiplier = (float) ease(this.alphaMultiplier, b ? 1 : 0, 10);
    }
    public void updateAndRender(PoseStack ms, VertexConsumer buf, Color c1, Color c2, int alpha, boolean b, int layer){
        this.alphaMultiplier = (float) ease(this.alphaMultiplier, b ? 1 : 0, 10);
        Vertexer.vertexLine(ms, buf, (float) minPos.x, (float) minPos.y, (float) minPos.z, (float) maxPos.x, (float) maxPos.y, (float) maxPos.z, c1, c2, Math.round(alpha * alphaMultiplier), getNormal(), layer);

    }
}
