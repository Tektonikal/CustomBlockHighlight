package tektonikal.customblockhighlight;
//? if =26.3 {
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.BlendOp;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
//?} elif >=26.2 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.BlendOp;
*///?} elif >=26.1 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
*///?} elif >=1.21.5 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
*///?}
//? if >=26.2
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
//? if >=1.21.5 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
//?}
//? if >=26.1 {
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
//?} elif >=1.21.5
//import com.mojang.blaze3d.platform.DepthTestFunction;
//? if >=26.2 {
import net.minecraft.client.renderer.StagedVertexBuffer;
//?} elif >=1.21.5 {
/*import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.lwjgl.system.MemoryUtil;
*///?} elif >=1.21.4 {
/*import net.minecraft.client.renderer.CoreShaders;
import org.lwjgl.opengl.GL11;
*///?} elif >1.8.9 {
/*import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
*///?}
//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget;
//? if >=26.3
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
//?}
//? if >1.8.9 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.vertex.VertexFormat;
//?} else {
/*import tektonikal.customblockhighlight.legacy.vertex.*;
*///?}
//? if >=26.2 {
import net.minecraft.client.renderer.rendertype.LayeringTransform;
//? if <26.3
//import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
//?}
//? if >1.8.9
import net.minecraft.resources.Identifier;
//? if >=26.3
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.phys.*;
import org.joml.*;
import tektonikal.customblockhighlight.util.*;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;
import static tektonikal.customblockhighlight.Renderer.mc;

public final class RenderBackend {
	private RenderBackend() {
	}

	public static <T> T cached(T[] cache, int i, Supplier<T> create) {
		if (cache[i] == null) cache[i] = create.get();
		return cache[i];
	}

	public static String typeName(String prefix, DepthTestMode mode, BlendMode blend) {
		return prefix + mode.name().toLowerCase(Locale.ROOT) + "_" + blend.name().toLowerCase(Locale.ROOT);
	}

	//? if >=1.21.5 {
	public static final RenderPipeline LINE_NO_DEPTH = evilPipeline(RenderPipelines.LINES_SNIPPET, "pipeline/evil-lines", DepthTestMode.ALWAYS_PASS, true, BlendFunction.TRANSLUCENT);
	public static final RenderPipeline FILL_NO_DEPTH = evilPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET, "pipeline/evil-fill", DepthTestMode.ALWAYS_PASS, false, BlendFunction.TRANSLUCENT);
	public static final RenderPipeline LINES_CONCEALED_ONLY = evilPipeline(RenderPipelines.LINES_SNIPPET, "pipeline/eviler-lines", DepthTestMode.HIDDEN_ONLY, true, BlendFunction.TRANSLUCENT);
	public static final RenderPipeline FILL_CONCEALED_ONLY = evilPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET, "pipeline/eviler-fill", DepthTestMode.HIDDEN_ONLY, false, BlendFunction.TRANSLUCENT);
	//don't use vanilla quad shader so we don't break other stuff with iris
	private static final RenderPipeline FILL_NORMAL = evilPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET, "pipeline/evil-fill-normal", DepthTestMode.NORMAL, false, BlendFunction.TRANSLUCENT);
	//? if >=26.2 {
	private static final BlendFunction ADDITIVE_BLEND = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA);
	private static final BlendFunction MULTIPLY_BLEND = new BlendFunction(BlendFactor.ZERO, BlendFactor.SRC_COLOR, BlendFactor.ZERO, BlendFactor.ONE);
	private static final BlendFunction SCREEN_BLEND = new BlendFunction(BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_COLOR, BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA);
	private static final BlendFunction INVERT_BLEND = new BlendFunction(BlendFactor.ONE_MINUS_DST_COLOR, BlendFactor.ONE_MINUS_SRC_COLOR, BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA);
	private static final BlendFunction SUBTRACT_BLEND = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE, BlendOp.REVERSE_SUBTRACT, BlendFactor.ZERO, BlendFactor.ONE, BlendOp.ADD);
	private static final BlendFunction LIGHTEN_BLEND = new BlendFunction(BlendFactor.ONE, BlendFactor.ONE, BlendOp.MAX, BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendOp.ADD);
	private static final BlendFunction DARKEN_BLEND = new BlendFunction(BlendFactor.ONE, BlendFactor.ONE, BlendOp.MIN, BlendFactor.ZERO, BlendFactor.ONE, BlendOp.ADD);
	//?} else {
	/*private static final BlendFunction ADDITIVE_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
	private static final BlendFunction MULTIPLY_BLEND = new BlendFunction(SourceFactor.ZERO, DestFactor.SRC_COLOR, SourceFactor.ZERO, DestFactor.ONE);
	private static final BlendFunction SCREEN_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
	private static final BlendFunction INVERT_BLEND = new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
	*///?}
	private static final RenderPipeline[] BLENDED_PIPELINES = blendedPipelines();

	public static int pipelineIndex(DepthTestMode mode, boolean lines, BlendMode blend) {
		return (blend.ordinal() * DepthTestMode.values().length + mode.ordinal()) * 2 + (lines ? 1 : 0);
	}

	private static RenderPipeline[] blendedPipelines() {
		RenderPipeline[] pipelines = new RenderPipeline[BlendMode.values().length * DepthTestMode.values().length * 2];
		for (BlendMode blend : BlendMode.values()) {
			if (blend == BlendMode.NORMAL) continue;
			BlendFunction function = switch (blend) {
				//unreachable but i don't want a default branch
				case NORMAL -> BlendFunction.TRANSLUCENT;
				case ADDITIVE -> ADDITIVE_BLEND;
				case MULTIPLY -> MULTIPLY_BLEND;
				case SCREEN -> SCREEN_BLEND;
				case INVERT -> INVERT_BLEND;
				//? if >=26.2 {
				case SUBTRACT -> SUBTRACT_BLEND;
				case LIGHTEN -> LIGHTEN_BLEND;
				case DARKEN -> DARKEN_BLEND;
				//?}
			};
			String suffix = blend.name().toLowerCase(Locale.ROOT);
			for (DepthTestMode mode : DepthTestMode.values()) {
				String name = mode.name().toLowerCase(Locale.ROOT) + "-" + suffix;
				pipelines[pipelineIndex(mode, true, blend)] = evilPipeline(RenderPipelines.LINES_SNIPPET, "pipeline/evil-lines-" + name, mode, true, function);
				pipelines[pipelineIndex(mode, false, blend)] = evilPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET, "pipeline/evil-fill-" + name, mode, false, function);
			}
		}
		return pipelines;
	}

	static {
		IrisCompat.assign(LINE_NO_DEPTH, true);
		IrisCompat.assign(LINES_CONCEALED_ONLY, true);
		IrisCompat.assign(FILL_NO_DEPTH, false);
		IrisCompat.assign(FILL_CONCEALED_ONLY, false);
		for (int i = 0; i < BLENDED_PIPELINES.length; i++) {
			if (BLENDED_PIPELINES[i] != null) IrisCompat.assign(BLENDED_PIPELINES[i], i % 2 == 1);
		}
		IrisCompat.assign(FILL_NORMAL, false);
	}

	private static RenderPipeline evilPipeline(RenderPipeline.Snippet snippet, String path, DepthTestMode mode, boolean writeDepth, BlendFunction blend) {
		RenderPipeline.Builder builder = RenderPipeline.builder(snippet)
				.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", path))
				//? if >=26.1 {
				.withColorTargetState(new ColorTargetState(blend))
				//?} else
				//.withBlend(blend)
				.withCull(false);
		return RenderPipelines.register(withDepth(builder, mode, writeDepth).build());
	}

	public static RenderPipeline getPipeline(DepthTestMode mode, boolean lines, BlendMode blend) {
		return blend == BlendMode.NORMAL ? getPipeline(mode, lines) : BLENDED_PIPELINES[pipelineIndex(mode, lines, blend)];
	}

	public static RenderPipeline getPipeline(DepthTestMode mode, boolean lines) {
		return switch (mode) {
			case ALWAYS_PASS -> lines ? LINE_NO_DEPTH : FILL_NO_DEPTH;
			case HIDDEN_ONLY -> lines ? LINES_CONCEALED_ONLY : FILL_CONCEALED_ONLY;
			//~ if <26.3 'LINES_TRANSLUCENT' -> 'LINES'
			case NORMAL -> lines ? RenderPipelines.LINES_TRANSLUCENT : FILL_NORMAL;
		};
	}
	//?}

	//? if >=1.21.4 && <1.21.5 {
	/*public static void setDrawShader(boolean lines) {
		RenderSystem.setShader(lines ? CoreShaders.RENDERTYPE_LINES : CoreShaders.POSITION_COLOR);
	}
	*///?} elif <1.21.4 && >1.8.9 {
	/*public static void setDrawShader(boolean lines) {
		RenderSystem.setShader(lines ? GameRenderer::getRendertypeLinesShader : GameRenderer::getPositionColorShader);
	}
	*///?}

	//? if >=26.1 {
	private static RenderPipeline.Builder withDepth(RenderPipeline.Builder builder, DepthTestMode mode, boolean writeDepth) {
		//26.2+ uses reverse-z
		//~ if <26.2 'LESS_THAN' -> 'GREATER_THAN'
		CompareOp concealed = CompareOp.LESS_THAN;
		//~ if <26.2 'GREATER_THAN_OR_EQUAL' -> 'LESS_THAN_OR_EQUAL'
		CompareOp visible = CompareOp.GREATER_THAN_OR_EQUAL;
		return builder.withDepthStencilState(new DepthStencilState(mode == DepthTestMode.ALWAYS_PASS ? CompareOp.ALWAYS_PASS : mode == DepthTestMode.HIDDEN_ONLY ? concealed : visible, writeDepth));
	}
	//?} elif >=1.21.5 {
	/*private static RenderPipeline.Builder withDepth(RenderPipeline.Builder builder, DepthTestMode mode, boolean writeDepth) {
		return builder.withDepthTestFunction(mode == DepthTestMode.ALWAYS_PASS ? DepthTestFunction.NO_DEPTH_TEST : mode == DepthTestMode.HIDDEN_ONLY ? DepthTestFunction.GREATER_DEPTH_TEST : DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(writeDepth);
	}
	*///?} elif >1.8.9 {
	/*public static void applyBlend(BlendMode blend) {
		RenderSystem.blendFuncSeparate(blend.glSrc, blend.glDst, GL11.GL_ONE, GL11.GL_ZERO);
		RenderSystem.blendEquation(blend.glEquation);
	}

	public static void applyDepth(DepthTestMode mode) {
		switch (mode) {
			case ALWAYS_PASS -> RenderSystem.disableDepthTest();
			case HIDDEN_ONLY -> {
				RenderSystem.enableDepthTest();
				RenderSystem.depthFunc(GL11.GL_GREATER);
			}
			case NORMAL -> {
				RenderSystem.enableDepthTest();
				RenderSystem.depthFunc(GL11.GL_LEQUAL);
			}
		}
	}
	*///?}

	//? if >=26.2 {
	public static final StagedVertexBuffer stagedOutlineBuffer = new StagedVertexBuffer(() -> " CBH outline", RenderType.SMALL_BUFFER_SIZE);
	//?}

	//? if >=26.2 {
	private static StagedVertexBuffer.Draw currentDraw;
	//?} elif >=1.21.5 {
	/*private static MappableRingBuffer vertexBuffer;
	private static final ByteBufferBuilder allocator = new ByteBufferBuilder(786432);

	private static BufferBuilder currentDraw;
	*///?} elif >1.8.9 {
	/*private static BufferBuilder currentDraw;
	*///?} else {
	/*public static final LegacyVertexBuffer legacyBuffer = new LegacyVertexBuffer();
	*///?}

	//? if <26.2
	//static float currentLineWidth = 1F;

	//? if >1.8.9 {
	private static VertexFormat lineFormat() {
		//? if >=1.21.11 {
		return DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH;
		//?} else
		//return DefaultVertexFormat.POSITION_COLOR_NORMAL;
	}
	//?}

	//? if >=26.2 {
	private static GpuBufferSlice transformUniform() {
		return RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f());
	}
	//?} elif >=1.21.11 {
	/*private static GpuBufferSlice transformUniform(float width) {
		return RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f());
	}
	*///?} elif >=1.21.5 {
	/*private static GpuBufferSlice transformUniform(float width) {
		return RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f(), width);
	}
	*///?}

	//? if >=26.2 {
	static Function<RenderType, VertexConsumer> featureSink;
	private static final RenderType[] worldRenderTypes = new RenderType[BLENDED_PIPELINES.length];

	public static RenderType worldRenderType(DepthTestMode mode, boolean lines, BlendMode blend) {
		return cached(worldRenderTypes, pipelineIndex(mode, lines, blend), () -> {
			RenderPipeline pipeline = getPipeline(mode, lines, blend);
			return RenderType.create(typeName(lines ? "cbh_world_lines_" : "cbh_world_fill_", mode, blend),
					lines ? RenderSetup.builder(pipeline).createRenderSetup() : RenderSetup.builder(pipeline).sortOnUpload().createRenderSetup());
		});
	}

	private static final RenderType[] previewLineTypes = new RenderType[BLENDED_PIPELINES.length];

	public static RenderType previewLineType(DepthTestMode mode, BlendMode blend) {
		return cached(previewLineTypes, pipelineIndex(mode, true, blend), () -> RenderType.create(typeName("cbh_preview_lines_", mode, blend),
				RenderSetup.builder(getPipeline(mode, true, blend))
						.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
						//? if <26.3
						//.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
						.createRenderSetup()));
	}

	private static final RenderType[] screenRenderTypes = new RenderType[BLENDED_PIPELINES.length];

	public static RenderType screenRenderType(DepthTestMode mode, BlendMode blend) {
		return cached(screenRenderTypes, pipelineIndex(mode, false, blend), () -> RenderType.create(typeName("cbh_screen_", mode, blend), RenderSetup.builder(getPipeline(mode, false, blend)).createRenderSetup()));
	}

	//lie to the feature renderer so everything looks right
	private static final RenderType[] outlineRenderTypes = new RenderType[BLENDED_PIPELINES.length * 3];

	static RenderType outlineRenderType(DepthTestMode mode, boolean lines, BlendMode blend, int layer) {
		int index = layer * BLENDED_PIPELINES.length + pipelineIndex(mode, lines, blend);
		return cached(outlineRenderTypes, index, () -> RenderType.create(typeName("cbh_outline_" + layer + (lines ? "_lines_" : "_screen_"), mode, blend),
				RenderSetup.builder(getPipeline(mode, lines, blend)).createRenderSetup()));
	}

	static final RenderType SCREEN_DEPTH = RenderType.create("cbh_screen_depth", RenderSetup.builder(evilPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET, "pipeline/evil-screen-depth", DepthTestMode.NORMAL, true,
			new BlendFunction(BlendFactor.ZERO, BlendFactor.ONE, BlendFactor.ZERO, BlendFactor.ONE))).createRenderSetup());

	public static VertexConsumer startDrawing(boolean lines, DepthTestMode mode, BlendMode blend) {
		if (featureSink != null) return featureSink.apply(worldRenderType(mode, lines, blend));
		if (lines) {
			currentDraw = stagedOutlineBuffer.appendDraw(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, PrimitiveTopology.LINES);
		} else {
			currentDraw = stagedOutlineBuffer.appendDraw(DefaultVertexFormat.POSITION_COLOR, PrimitiveTopology.QUADS, RenderSystem.getProjectionType().vertexSorting());
		}
		return stagedOutlineBuffer.getVertexBuilder(currentDraw);
	}

	private record PendingDraw(StagedVertexBuffer.Draw draw, RenderPipeline pipeline) {
	}

	private static final List<PendingDraw> pendingDraws = new ArrayList<>();

	static void finishDraw(boolean lines, DepthTestMode mode, BlendMode blend) {
		if (featureSink != null) return;
		pendingDraws.add(new PendingDraw(currentDraw, getPipeline(mode, lines, blend)));
	}

	//? if >=26.3 {
	private static TextureTarget opaqueDepth;
	private static boolean opaqueDepthCopied;
	private static boolean drewLastFrame;

	public static void copyOpaqueDepth() {
		if (!getActiveInstance().enableModRendering) return;
		HitResult hit = drewLastFrame ? null : Target.getHitResult();
		if (!drewLastFrame && (hit == null || hit.getType() == HitResult.Type.MISS)) return;
		var cfg = getActiveInstance();
		boolean testsDepth = cfg.fillEnabled && cfg.fillDepthTest != DepthTestMode.ALWAYS_PASS;
		for (LineConfig lc : cfg.lineConfigs()) testsDepth |= Renderer.isLayerDrawn(cfg, lc) && lc.lineDepthTest != DepthTestMode.ALWAYS_PASS;
		if (!testsDepth) return;
		RenderTarget main = mc.gameRenderer.mainRenderTarget();
		if (main.getDepthTexture() == null) return;
		var profiler = Profiler.get();
		profiler.push("customBlockHighlight");
		profiler.push("depthCopy");
		if (opaqueDepth == null) opaqueDepth = new TextureTarget("CBH opaque depth", main.width, main.height, null, main.getDepthTexture().getFormat());
		else if (opaqueDepth.width != main.width || opaqueDepth.height != main.height) opaqueDepth.resize(main.width, main.height);
		opaqueDepth.copyDepthFrom(main);
		opaqueDepthCopied = true;
		profiler.pop();
		profiler.pop();
	}
	//?}

	static void flushDraws() {
		//? if >=26.3 {
		boolean useOpaqueDepth = opaqueDepthCopied;
		opaqueDepthCopied = false;
		drewLastFrame = !pendingDraws.isEmpty();
		//?}
		if (pendingDraws.isEmpty()) return;
		try {
			stagedOutlineBuffer.upload();
			RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
			GpuTextureView colorTexture = mainTarget.getColorTextureView();
			if (colorTexture == null) return;
			GpuTextureView depthTexture = mainTarget.getDepthTextureView();
			//? if >=26.3
			if (useOpaqueDepth) depthTexture = opaqueDepth.getDepthTextureView();
			GpuBufferSlice dynamicTransforms = transformUniform();
			try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "CBH pass", colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty())) {
				for (PendingDraw pending : pendingDraws) {
					StagedVertexBuffer.ExecuteInfo info = stagedOutlineBuffer.getExecuteInfo(pending.draw());
					if (info == null) continue;
					//? if >=26.3 {
					renderPass.setPipeline(RenderSystem.getCompiledPipeline(pending.pipeline()));
					//?} else
					//renderPass.setPipeline(pending.pipeline());
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setUniform("DynamicTransforms", dynamicTransforms);
					renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
					renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
					renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
				}
			}
		} finally {
			pendingDraws.clear();
			stagedOutlineBuffer.endFrame();
		}
	}
	//?} elif >=1.21.5 {
	/*public static VertexConsumer startDrawing(boolean lines, DepthTestMode mode, BlendMode blend) {
		currentDraw = new BufferBuilder(allocator, lines ? VertexFormat.Mode.LINES : VertexFormat.Mode.QUADS, lines ? lineFormat() : DefaultVertexFormat.POSITION_COLOR);
		return currentDraw;
	}

	private record PendingDraw(MeshData mesh, RenderPipeline pipeline, float width) {
	}

	private static final List<PendingDraw> pendingDraws = new ArrayList<>();

	static void finishDraw(boolean lines, DepthTestMode mode, BlendMode blend) {
		MeshData builtBuffer = currentDraw.build();
		if (builtBuffer == null) return;
		pendingDraws.add(new PendingDraw(builtBuffer, getPipeline(mode, lines, blend), lines ? currentLineWidth : 1F));
	}

	static void flushDraws() {
		if (pendingDraws.isEmpty()) return;
		try {
			int[] offsets = new int[pendingDraws.size()];
			int size = 0;
			for (int i = 0; i < offsets.length; i++) {
				int vertexSize = pendingDraws.get(i).mesh().drawState().format().getVertexSize();
				offsets[i] = (size + vertexSize - 1) / vertexSize * vertexSize;
				size = offsets[i] + pendingDraws.get(i).mesh().vertexBuffer().remaining();
			}
			if (vertexBuffer == null || vertexBuffer.size() < size) {
				if (vertexBuffer != null) {
					vertexBuffer.close();
				}
				vertexBuffer = new MappableRingBuffer(() -> "CBH render pipeline", 34, size);
			}
			GpuBuffer vertices = vertexBuffer.currentBuffer();
			CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
			try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertices.slice(0, size), false, true)) {
				for (int i = 0; i < offsets.length; i++) {
					MemoryUtil.memCopy(pendingDraws.get(i).mesh().vertexBuffer(), mappedView.data().position(offsets[i]));
				}
			}
			vertexBuffer.rotate();
			RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
			GpuBufferSlice[] transforms = new GpuBufferSlice[offsets.length];
			for (int i = 0; i < offsets.length; i++) {
				PendingDraw pending = pendingDraws.get(i);
				RenderSystem.getSequentialBuffer(pending.pipeline().getVertexFormatMode()).getBuffer(pending.mesh().drawState().indexCount());
				transforms[i] = transformUniform(pending.width());
			}
			try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "CBH pass", mainTarget.getColorTextureView(), OptionalInt.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
				for (int i = 0; i < offsets.length; i++) {
					PendingDraw pending = pendingDraws.get(i);
					MeshData.DrawState state = pending.mesh().drawState();
					RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pending.pipeline().getVertexFormatMode());
					renderPass.setPipeline(pending.pipeline());
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setUniform("DynamicTransforms", transforms[i]);
					renderPass.setVertexBuffer(0, vertices);
					renderPass.setIndexBuffer(shapeIndexBuffer.getBuffer(state.indexCount()), shapeIndexBuffer.type());
					renderPass.drawIndexed(offsets[i] / state.format().getVertexSize(), 0, state.indexCount(), 1);
				}
			}
		} finally {
			for (PendingDraw pending : pendingDraws) pending.mesh().close();
			pendingDraws.clear();
		}
	}
	*///?} elif >1.8.9 {
	/*public static VertexConsumer startDrawing(boolean lines, DepthTestMode mode, BlendMode blend) {
		currentDraw = Tesselator.getInstance().begin(lines ? VertexFormat.Mode.LINES : VertexFormat.Mode.QUADS, lines ? lineFormat() : DefaultVertexFormat.POSITION_COLOR);
		return currentDraw;
	}

	static void finishDraw(boolean lines, DepthTestMode mode, BlendMode blend) {
		MeshData builtBuffer = currentDraw.build();
		if (builtBuffer == null) return;
		setDrawShader(lines);
		RenderSystem.lineWidth(lines ? currentLineWidth : 1F);
		RenderSystem.enableBlend();
		applyBlend(blend);
		RenderSystem.disableCull();
		applyDepth(mode);
		RenderSystem.depthMask(lines);
		BufferUploader.drawWithShader(builtBuffer);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.enableCull();
		applyBlend(BlendMode.NORMAL);
		RenderSystem.disableBlend();
		RenderSystem.lineWidth(1F);
	}
	*///?} else {
	/*public static VertexConsumer startDrawing(boolean lines, DepthTestMode mode, BlendMode blend) {
		return legacyBuffer.beginBatch(lines);
	}

	static void finishDraw(boolean lines, DepthTestMode mode, BlendMode blend) {
		legacyBuffer.endBatch(mode, blend);
	}
	*///?}
}
