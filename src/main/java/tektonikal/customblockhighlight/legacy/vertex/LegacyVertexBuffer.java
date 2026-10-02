package tektonikal.customblockhighlight.legacy.vertex;

//? if =1.8.9 {
/*import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import org.lwjgl.opengl.GL11;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.util.Arrays;

public final class LegacyVertexBuffer implements VertexConsumer {
	private static final int STRIDE = 8;
	private static final int X = 0, Y = 1, Z = 2, R = 3, G = 4, B = 5, A = 6, WIDTH = 7;

	private float[] data = new float[STRIDE * 256];
	private int vertices;
	private boolean lines;

	public LegacyVertexBuffer begin(boolean lines) {
		this.lines = lines;
		this.vertices = 0;
		return this;
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		int base = vertices * STRIDE;
		if (base + STRIDE > data.length) data = Arrays.copyOf(data, data.length * 2);
		data[base + X] = x;
		data[base + Y] = y;
		data[base + Z] = z;
		data[base + R] = 255;
		data[base + G] = 255;
		data[base + B] = 255;
		data[base + A] = 255;
		data[base + WIDTH] = 1F;
		vertices++;
		return this;
	}

	@Override
	public VertexConsumer setColor(int r, int g, int b, int a) {
		int base = (vertices - 1) * STRIDE;
		data[base + R] = r;
		data[base + G] = g;
		data[base + B] = b;
		data[base + A] = a;
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		data[(vertices - 1) * STRIDE + WIDTH] = width;
		return this;
	}

	public void draw(DepthTestMode mode) {
		if (vertices == 0) return;
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture();
		GlStateManager.disableCull();
		GlStateManager.disableAlphaTest();
		GlStateManager.disableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		applyDepth(mode);

		if (lines) {
			drawLines();
		} else {
			drawPrimitive(GL11.GL_QUADS, 0, vertices - vertices % 4);
		}

		GL11.glLineWidth(1.0F);
		GlStateManager.enableDepthTest();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlphaTest();
		GlStateManager.enableCull();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
		vertices = 0;
	}

	private void drawLines() {
		int segments = vertices / 2;
		int start = 0;
		float width = segmentWidth(0);
		for (int i = 1; i <= segments; i++) {
			float next = i < segments ? segmentWidth(i) : Float.NaN;
			if (next != width) {
				GL11.glLineWidth(Math.max(width, 0.01F));
				drawPrimitive(GL11.GL_LINES, start * 2, i * 2);
				start = i;
				width = next;
			}
		}
	}

	private float segmentWidth(int segment) {
		int base = segment * 2 * STRIDE;
		return (data[base + WIDTH] + data[base + STRIDE + WIDTH]) / 2F;
	}

	private void drawPrimitive(int glMode, int from, int to) {
		if (to <= from) return;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuffer();
		builder.begin(glMode, DefaultVertexFormat.POSITION_COLOR);
		for (int i = from; i < to; i++) {
			int base = i * STRIDE;
			builder.vertex(data[base + X], data[base + Y], data[base + Z])
					.color((int) data[base + R], (int) data[base + G], (int) data[base + B], (int) data[base + A])
					.nextVertex();
		}
		tesselator.end();
	}

	public static void applyDepth(DepthTestMode mode) {
		switch (mode) {
			case ALWAYS_PASS -> GlStateManager.disableDepthTest();
			case HIDDEN_ONLY -> {
				GlStateManager.enableDepthTest();
				GlStateManager.depthFunc(GL11.GL_GREATER);
			}
			case NORMAL -> {
				GlStateManager.enableDepthTest();
				GlStateManager.depthFunc(GL11.GL_LEQUAL);
			}
		}
	}
}
*///?}
