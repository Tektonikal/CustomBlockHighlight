package tektonikal.customblockhighlight.legacy.vertex;

//? if =1.8.9 {
/*import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.platform.MemoryTracker;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LegacyVertexBuffer implements VertexConsumer {
	private static final int STRIDE = 8;
	private static final int X = 0, Y = 1, Z = 2, R = 3, G = 4, B = 5, A = 6, WIDTH = 7;
	// same as vanilla's modern rendertype_lines.vsh, pulls lines toward the camera so they don't z-fight with faces
	private static final float VIEW_SHRINK = 1F - 1F / 256F;
	private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);

	private static final int PACKED_STRIDE = 4;
	private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

	private float[] data = new float[STRIDE * 256];
	private int vertices;
	private boolean lines;

	private final List<Batch> batches = new ArrayList<>();
	private int batchStart;
	private int[] packed = new int[0];
	private int[] uploaded = new int[0];
	private int uploadedLength = -1;
	private ByteBuffer uploadBuffer;
	private int vbo = -1;

	private record Batch(boolean lines, DepthTestMode mode, int from, int to) {
	}

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

	public void beginFrame() {
		batches.clear();
		vertices = 0;
	}

	public VertexConsumer beginBatch(boolean lines) {
		this.lines = lines;
		batchStart = vertices;
		return this;
	}

	public void endBatch(DepthTestMode mode) {
		if (vertices > batchStart) batches.add(new Batch(lines, mode, batchStart, vertices));
	}

	public void drawFrame(double x, double y, double z) {
		if (batches.isEmpty()) {
			vertices = 0;
			return;
		}
		upload();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture();
		GlStateManager.disableCull();
		GlStateManager.disableAlphaTest();
		GlStateManager.disableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, PACKED_STRIDE * 4, 0L);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, PACKED_STRIDE * 4, 12L);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		for (Batch batch : batches) {
			GlStateManager.depthMask(batch.lines());
			applyDepth(batch.mode());
			if (batch.lines()) {
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView);
				GL11.glPushMatrix();
				GL11.glLoadIdentity();
				GL11.glScalef(VIEW_SHRINK, VIEW_SHRINK, VIEW_SHRINK);
				GL11.glMultMatrixf(modelView);
				drawLineRuns(batch.from(), batch.to());
				GL11.glPopMatrix();
			} else {
				int count = batch.to() - batch.from();
				count -= count % 4;
				if (count > 0) GL11.glDrawArrays(GL11.GL_QUADS, batch.from(), count);
			}
		}
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// The color array leaves the current color undefined
		GlStateManager.clearColor();

		GL11.glLineWidth(1.0F);
		GlStateManager.depthMask(true);
		GlStateManager.enableDepthTest();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlphaTest();
		GlStateManager.enableCull();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		batches.clear();
		vertices = 0;
	}

	private void upload() {
		int length = vertices * PACKED_STRIDE;
		if (packed.length < length) packed = new int[Math.max(length, packed.length * 2)];
		for (int i = 0; i < vertices; i++) {
			int base = i * STRIDE;
			int out = i * PACKED_STRIDE;
			packed[out] = Float.floatToRawIntBits(data[base + X]);
			packed[out + 1] = Float.floatToRawIntBits(data[base + Y]);
			packed[out + 2] = Float.floatToRawIntBits(data[base + Z]);
			int r = (int) data[base + R] & 0xFF;
			int g = (int) data[base + G] & 0xFF;
			int b = (int) data[base + B] & 0xFF;
			int a = (int) data[base + A] & 0xFF;
			// bytes in memory must read R, G, B, A
			packed[out + 3] = LITTLE_ENDIAN ? a << 24 | b << 16 | g << 8 | r : r << 24 | g << 16 | b << 8 | a;
		}
		if (vbo != -1 && length == uploadedLength && Arrays.equals(packed, 0, length, uploaded, 0, length)) return;

		if (vbo == -1) vbo = GL15.glGenBuffers();
		if (uploadBuffer == null || uploadBuffer.capacity() < length * 4) {
			uploadBuffer = MemoryTracker.createByteBuffer(Math.max(length * 4, 4096));
		}
		IntBuffer ints = uploadBuffer.asIntBuffer();
		ints.put(packed, 0, length);
		uploadBuffer.position(0).limit(length * 4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uploadBuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		uploadBuffer.clear();

		if (uploaded.length < length) uploaded = new int[packed.length];
		System.arraycopy(packed, 0, uploaded, 0, length);
		uploadedLength = length;
	}

	private void drawLineRuns(int from, int to) {
		int segments = (to - from) / 2;
		if (segments == 0) return;
		int start = 0;
		float width = widthAt(from);
		for (int i = 1; i <= segments; i++) {
			float next = i < segments ? widthAt(from + i * 2) : Float.NaN;
			if (next != width) {
				GL11.glLineWidth(Math.max(width, 0.01F));
				GL11.glDrawArrays(GL11.GL_LINES, from + start * 2, (i - start) * 2);
				start = i;
				width = next;
			}
		}
	}

	private float widthAt(int vertex) {
		int base = vertex * STRIDE;
		return (data[base + WIDTH] + data[base + STRIDE + WIDTH]) / 2F;
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
		GlStateManager.depthMask(lines);
		applyDepth(mode);

		if (lines) {
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glScalef(VIEW_SHRINK, VIEW_SHRINK, VIEW_SHRINK);
			GL11.glMultMatrixf(modelView);
			drawLines();
			GL11.glPopMatrix();
		} else {
			drawPrimitive(GL11.GL_QUADS, 0, vertices - vertices % 4);
		}

		GL11.glLineWidth(1.0F);
		GlStateManager.depthMask(true);
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
