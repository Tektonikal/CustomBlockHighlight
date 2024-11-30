package tektonikal.customblockhighlight.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tektonikal.customblockhighlight.Vertexer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;

public class Line {
    public Vec3d minPos;
    public Vec3d maxPos;
    //TODO: remove minVec, i don't think it's used at all
    public Vec3d minVec;
    public float alphaMultiplier = 1;

    public Line(Vec3d minPos, Vec3d maxPos, Vec3d minVec) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.minVec = minVec;
    }

    public float getDistanceToCamera() {
        return (float) minPos.add(maxPos).multiply(0.5F).distanceTo(Vertexer.mc.gameRenderer.getCamera().getPos().subtract(minVec));
    }
    public float distanceTo(Vec3d pos) {
        return (float) minPos.add(maxPos).multiply(0.5F).distanceTo(pos.subtract(minVec));
    }

    public Vec3d getNormal() {
        float k = (float) (maxPos.x - minPos.x);
        float l = (float) (maxPos.y - minPos.y);
        float m = (float) (maxPos.z - minPos.z);
        float n = MathHelper.sqrt(k * k + l * l + m * m);
        k /= n;
        l /= n;
        m /= n;
        return new Vec3d(k, l, m);
    }

    public void render(MatrixStack ms, BufferBuilder buf, Color c1, Color c2, int alpha) {
        Vertexer.vertexLine(ms, buf, (float) minPos.x, (float) minPos.y, (float) minPos.z, (float) maxPos.x, (float) maxPos.y, (float) maxPos.z, c1, c2, Math.round(alpha * alphaMultiplier), getNormal());
    }

    public void moveTo(Vec3d minPosTo, Vec3d maxPosTo, Vec3d minVecTo) {
        this.minPos = new Vec3d(ease(this.minPos.x, minPosTo.x, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(this.minPos.y, minPosTo.y, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(this.minPos.z, minPosTo.z, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed));
        this.maxPos = new Vec3d(ease(this.maxPos.x, maxPosTo.x, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(this.maxPos.y, maxPosTo.y, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(this.maxPos.z, maxPosTo.z, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed));
        this.minVec = new Vec3d(ease(this.minVec.x, minVecTo.x, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(this.minVec.y, minVecTo.y, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(this.minVec.z, minVecTo.z, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed));
    }

    public double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-(1.0F / MinecraftClient.getInstance().getCurrentFps()) * speed)));
    }
    public void update(boolean b){
        this.alphaMultiplier = (float) ease(this.alphaMultiplier, b ? 1 : 0, 10);
    }
}
