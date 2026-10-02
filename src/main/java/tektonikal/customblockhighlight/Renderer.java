package tektonikal.customblockhighlight;
//? if = 26.3{
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
//?}
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
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.renderer.StagedVertexBuffer;
//?} elif >=1.21.5 {
/*import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.lwjgl.system.MemoryUtil;
*///?} elif >=1.21.4 {
/*import net.minecraft.client.renderer.CoreShaders;
import org.lwjgl.opengl.GL11;
*///?} else {
/*import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
*///?}
//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
//?}
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.vertex.VertexFormat;
// We use the one from fastutil because it makes the Java go faster. It's like putting flame stickers on your car
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
//?} elif >=1.21.5 {
/*import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
*///?} else {
/*import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
*///?}
//? if >=26.2 {
import net.minecraft.client.renderer.rendertype.LayeringTransform;
//? if <26.3
//import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;
//? if >=1.21.11 {
import org.jspecify.annotations.NonNull;
//?}
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
//? if >=1.21.4
import tektonikal.customblockhighlight.mixin.VoxelShapeAccessor;
import tektonikal.customblockhighlight.util.*;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;
//? if >=26.2
import java.util.function.Function;
import java.util.stream.Stream;

//? if >=1.21.5 {
import static net.minecraft.client.renderer.RenderPipelines.DEBUG_QUADS;
import static net.minecraft.client.renderer.RenderPipelines.LINES;
//?}
//? if >=1.21.2
import static net.minecraft.util.profiling.Profiler.get;
import static tektonikal.customblockhighlight.CustomBlockHighlight.ease;
import static tektonikal.customblockhighlight.CustomBlockHighlight.easeFactor;
import static tektonikal.customblockhighlight.CustomBlockHighlight.easeF;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;

public class Renderer {
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Camera camera = mc.gameRenderer.mainCamera();

	//? if >=1.21.4
	private static boolean isCubeLike(VoxelShape shape) { return ((VoxelShapeAccessor) shape).invokeIsCubeLike(); }
	//? if <1.21.4 {
	
	/*private static boolean isCubeLike(VoxelShape shape) {
		if (shape.isEmpty()) return false;
		AABB bounds = shape.bounds();
		return bounds.minX <= 0 && bounds.minY <= 0 && bounds.minZ <= 0 && bounds.maxX >= 1 && bounds.maxY >= 1 && bounds.maxZ >= 1;
	}
	*///?}

	//? if >=1.21.2
	private static Vec3 unitVec3(Direction dir) { return dir.getUnitVec3(); }
	//? if <1.21.2 {
	/*private static Vec3 unitVec3(Direction dir) {
		return new Vec3(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ());
	}
	private static net.minecraft.util.profiling.ProfilerFiller get() { return mc.getProfiler(); }
	*///?}

	public static final float[] sideFades = new float[6];
	public static List<Line> lines = new ArrayList<>();
	public static List<Line> modelLines = new ArrayList<>();
	public static List<Line> toRemove = new ArrayList<>();


	//? if >=1.21.5 {
	public static final RenderPipeline LINE_NO_DEPTH = evilPipeline(RenderPipelines.LINES_SNIPPET, "pipeline/evil-lines", true);
	public static final RenderPipeline FILL_NO_DEPTH = evilPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET, "pipeline/evil-fill", true);
	public static final RenderPipeline LINES_CONCEALED_ONLY = evilPipeline(RenderPipelines.LINES_SNIPPET, "pipeline/eviler-lines", false);
	public static final RenderPipeline FILL_CONCEALED_ONLY = evilPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET, "pipeline/eviler-fill", false);

	private static RenderPipeline evilPipeline(RenderPipeline.Snippet snippet, String path, boolean alwaysPass) {
		RenderPipeline.Builder builder = RenderPipeline.builder(snippet)
				.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", path))
				//? if 26.3{
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				//?}
				.withCull(false);
		return RenderPipelines.register(withDepth(builder, alwaysPass).build());
	}

	public static RenderPipeline getPipeline(DepthTestMode mode, boolean lines) {
		return switch (mode) {
			case ALWAYS_PASS -> lines ? LINE_NO_DEPTH : FILL_NO_DEPTH;
			case HIDDEN_ONLY -> lines ? LINES_CONCEALED_ONLY : FILL_CONCEALED_ONLY;
			//~ if 26.3 'LINES' -> 'LINES_TRANSLUCENT'
			case NORMAL -> lines ? LINES : DEBUG_QUADS;
		};
	}
	//?}

	//? if >=1.21.4 && <1.21.5 {
	/*public static void setDrawShader(boolean lines) {
		RenderSystem.setShader(lines ? CoreShaders.RENDERTYPE_LINES : CoreShaders.POSITION_COLOR);
	}
	*///?} elif <1.21.4 {
	/*public static void setDrawShader(boolean lines) {
		RenderSystem.setShader(lines ? GameRenderer::getRendertypeLinesShader : GameRenderer::getPositionColorShader);
	}
	*///?}

	//? if >=26.1 {
	private static RenderPipeline.Builder withDepth(RenderPipeline.Builder builder, boolean alwaysPass) {
		return builder.withDepthStencilState(new DepthStencilState(alwaysPass ? CompareOp.ALWAYS_PASS : CompareOp.LESS_THAN, true));
	}
	//?} elif >=1.21.5 {
	/*private static RenderPipeline.Builder withDepth(RenderPipeline.Builder builder, boolean alwaysPass) {
		return builder.withDepthTestFunction(alwaysPass ? DepthTestFunction.NO_DEPTH_TEST : DepthTestFunction.GREATER_DEPTH_TEST).withDepthWrite(true);
	}
	*///?} else {
	/*public static void applyDepth(DepthTestMode mode) {
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
	public static final RenderType linesNoDepth = RenderType.create("lines_no_depth",
			RenderSetup.builder(Renderer.LINE_NO_DEPTH)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					//? if <26.3
					//.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
					.createRenderSetup());
	public static final RenderType linesConcealed = RenderType.create("lines_concealed",
			RenderSetup.builder(Renderer.LINES_CONCEALED_ONLY)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					//? if <26.3
					//.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
					.createRenderSetup());
    public static final RenderType fillNoDepth = RenderType.create(
            "fill_no_depth", RenderSetup.builder(FILL_NO_DEPTH).sortOnUpload().createRenderSetup()
    );
    public static final RenderType fillConcealed = RenderType.create(
            "fill_concealed", RenderSetup.builder(FILL_CONCEALED_ONLY).sortOnUpload().createRenderSetup()
    );
	//?}

	//? if >=26.2 {
	public static final StagedVertexBuffer stagedOutlineBuffer = new StagedVertexBuffer(() -> " CBH outline", RenderType.SMALL_BUFFER_SIZE);
	//?}

	public static AABB easeBox = new AABB(0, 0, 0, 0, 0, 0);

	public static float scaleProg = 0;
	public static float lineProg = 0;
	public static Quaternionf rotation = new Quaternionf();
	private static Direction lastHorizontalDirection = Direction.NORTH;

	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();

	public static final List<LineState> lineStates = new ArrayList<>(Stream.of(new LineState(), new LineState(), new LineState()).toList());
	//TODO:
	/*
	Things that model mode hates:
	- small dripleaf
	- Waterlogged non-full blocks
	- Fluids
	- All (block) entities
	 */

	/*
	- 1 layer: ~2.5% frametime
	- 2 unique layers: ~20%
	- 3 unique layers: ~27%
	- 4 unique layers: ~30%

	Conclusion: kill yourself mojang
	 */
    //TODO: batching layers together means that identical layers that do not have always pass will always z fight
	//? if >=26.2 {
	private static StagedVertexBuffer.Draw currentDraw;
	//?} elif >=1.21.5 {
	/*private static MappableRingBuffer vertexBuffer;
	private static final ByteBufferBuilder allocator = new ByteBufferBuilder(786432);

	private static BufferBuilder currentDraw;
	*///?} else {
	/*private static BufferBuilder currentDraw;
	*///?}

	//? if <26.2
	//private static float currentLineWidth = 1F;

	private static VertexFormat lineFormat() {
		//? if >=1.21.11 {
		return DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH;
		//?} else
		//return DefaultVertexFormat.POSITION_COLOR_NORMAL;
	}

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
	private static Function<RenderType, VertexConsumer> featureSink;
	private static final RenderType[] worldRenderTypes = new RenderType[DepthTestMode.values().length * 2];

	private static RenderType worldRenderType(DepthTestMode mode, boolean lines) {
		int i = mode.ordinal() * 2 + (lines ? 1 : 0);
		RenderType type = worldRenderTypes[i];
		if (type == null) {
			RenderPipeline pipeline = getPipeline(mode, lines);
			type = worldRenderTypes[i] = RenderType.create("cbh_world_" + (lines ? "lines_" : "fill_") + mode.name().toLowerCase(Locale.ROOT),
					lines ? RenderSetup.builder(pipeline).createRenderSetup() : RenderSetup.builder(pipeline).sortOnUpload().createRenderSetup());
		}
		return type;
	}

	public static VertexConsumer startDrawing(boolean lines, DepthTestMode mode) {
		if (featureSink != null) return featureSink.apply(worldRenderType(mode, lines));
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

    // Only queues the draw. StagedVertexBuffer is meant to be uploaded and endFrame'd once per frame:
    // endFrame destroys every pooled buffer not reused, and each pool rounds allocations up to 256KB, so
    // uploading once per layer re-created GPU buffers every frame (~30% of the render thread with a few layers).
    private static void finishDraw(boolean lines, DepthTestMode mode) {
        if (featureSink != null) return;
        pendingDraws.add(new PendingDraw(currentDraw, getPipeline(lines ? mode : getActiveInstance().fillDepthTest, lines)));
    }

    private static void flushDraws() {
        if (pendingDraws.isEmpty()) return;
        try {
            stagedOutlineBuffer.upload();
            RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
            GpuTextureView colorTexture = mainTarget.getColorTextureView();
            if (colorTexture == null) return;
            GpuBufferSlice dynamicTransforms = transformUniform();
            for (PendingDraw pending : pendingDraws) {
                StagedVertexBuffer.ExecuteInfo info = stagedOutlineBuffer.getExecuteInfo(pending.draw());
                if (info == null) continue;
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "CBH pass", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
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
	/*public static VertexConsumer startDrawing(boolean lines, DepthTestMode mode) {
		currentDraw = new BufferBuilder(allocator, lines ? VertexFormat.Mode.LINES : VertexFormat.Mode.QUADS, lines ? lineFormat() : DefaultVertexFormat.POSITION_COLOR);
		return currentDraw;
	}

	private static void finishDraw(boolean lines, DepthTestMode mode) {
		MeshData builtBuffer = currentDraw.build();
		if (builtBuffer == null) return;
		MeshData.DrawState drawState = builtBuffer.drawState();
		DepthTestMode fillMode = lines ? mode : getActiveInstance().fillDepthTest;
		float width = lines ? currentLineWidth : 1F;
		GpuBuffer vertices = upload(drawState, builtBuffer);
		draw(builtBuffer, drawState, vertices, lines, fillMode, width);
	}

	private static void draw(MeshData builtBuffer, MeshData.DrawState state, GpuBuffer vertices, boolean lines, DepthTestMode mode, float width) {
		RenderPipeline p = getPipeline(mode, lines);
		RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(p.getVertexFormatMode());
		GpuBuffer indices = shapeIndexBuffer.getBuffer(state.indexCount());
		VertexFormat.IndexType indexType = shapeIndexBuffer.type();
		GpuBufferSlice dynamicTransforms = transformUniform(width);
		RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();

		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "CBH pass", mainTarget.getColorTextureView(), OptionalInt.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
			renderPass.setPipeline(p);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setVertexBuffer(0, vertices);
			renderPass.setIndexBuffer(indices, indexType);
			renderPass.drawIndexed(0, 0, state.indexCount(), 1);
		}

		builtBuffer.close();
	}

	private static GpuBuffer upload(MeshData.DrawState drawState, MeshData builtBuffer) {
		int vertexBufferSize = drawState.vertexCount() * drawState.format().getVertexSize();
		if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
			if (vertexBuffer != null) {
				vertexBuffer.close();
			}
			vertexBuffer = new MappableRingBuffer(() -> "CBH render pipeline", 34, vertexBufferSize);
		}

		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
			MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
		}

		GpuBuffer result = vertexBuffer.currentBuffer();
		vertexBuffer.rotate();
		return result;
	}
	*///?} else {
	/*public static VertexConsumer startDrawing(boolean lines, DepthTestMode mode) {
		currentDraw = Tesselator.getInstance().begin(lines ? VertexFormat.Mode.LINES : VertexFormat.Mode.QUADS, lines ? lineFormat() : DefaultVertexFormat.POSITION_COLOR);
		return currentDraw;
	}

	private static void finishDraw(boolean lines, DepthTestMode mode) {
		MeshData builtBuffer = currentDraw.build();
		if (builtBuffer == null) return;
		DepthTestMode fillMode = lines ? mode : getActiveInstance().fillDepthTest;
		setDrawShader(lines);
		RenderSystem.lineWidth(lines ? currentLineWidth : 1F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		applyDepth(fillMode);
		BufferUploader.drawWithShader(builtBuffer);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.lineWidth(1F);
	}
	*///?}

	public static void drawBoxFill(PoseStack stack, AABB box, Pair<Color, Color> cols, float[] alpha) {
		doEvilMatrixPreparations(stack, box, getActiveInstance().fillExpandBlocks, getActiveInstance().fillExpandPercent);
		VertexConsumer buffer = startDrawing(false, getActiveInstance().fillDepthTest);
		Vertexer.vertexBoxQuads(stack.last(), buffer, moveToZero(box), cols, alpha);
		finishDraw(false, getActiveInstance().fillDepthTest);
		stack.popPose();
	}

	private static void doEvilMatrixPreparations(PoseStack stack, AABB box, float scaleBlocks, float scalePercentage) {
		stack.pushPose();
		stack.translate(box.minX - camera.position().x, box.minY - camera.position().y, box.minZ - camera.position().z);
		Vec3 vec = moveToZero(box).getCenter();
		stack.translate(vec.x, vec.y, vec.z);
		if (getActiveInstance().rotations) {
			stack.rotateAround(rotation, 0, 0, 0);
		}
		AABB scaled = box.inflate(scaleBlocks);
		Vector3f boxDim = new Vector3f((float) (scaled.getXsize() / box.getXsize()), (float) (scaled.getYsize() / box.getYsize()), (float) (scaled.getZsize() / box.getZsize()));
		stack.scale(boxDim.x, boxDim.y, boxDim.z);
		stack.scale(scalePercentage, scalePercentage, scalePercentage);
		stack.scale(scaleProg, scaleProg, scaleProg);
		stack.translate(-vec.x, -vec.y, -vec.z);
	}

	public static void drawLineLayer(PoseStack stack, BlockHighlightConfig.LineConfig cfg, boolean obstructed, int layer, VertexConsumer buffer) {
		//? if <26.2
		//currentLineWidth = cfg.lineWidth;
		doEvilMatrixPreparations(stack, easeBox, cfg.lineExpandBlocks, cfg.lineExpandPercentage);
		AABB zeroed = moveToZero(easeBox);
		Pair<Color, Color> cols = cfg.color.getColors(obstructed, getActiveInstance().crystalHelperLineColor);
		if (cfg.shapeStyle != ShapeStyle.CLASSIC_BOX) {
			int alpha = Math.round(lineStates.get(layer).getEdgeAlpha());
			if (alpha >= 1) {
				PoseStack.Pose pose = stack.last();
				int c1 = cols.first().getRGB() & 0xFFFFFF;
				int c2 = cols.second().getRGB() & 0xFFFFFF;
				Vec3 zeroMin = zeroed.getMinPosition();
				double normalised = zeroMin.distanceTo(zeroed.getMaxPosition());
				List<Line> src = cfg.shapeStyle == ShapeStyle.COLLISION_SHAPE ? lines : modelLines;
				for (int i = 0; i < src.size(); i++) renderLine(src.get(i), pose, buffer, c1, c2, zeroMin, normalised, alpha, cfg);
				for (int i = 0; i < toRemove.size(); i++) renderLine(toRemove.get(i), pose, buffer, c1, c2, zeroMin, normalised, alpha, cfg);
			}
		} else {
			Vertexer.vertexBoxLines(stack.last(), buffer, zeroed, cols, lineStates.get(layer).getLineFades(), cfg.lineWidth * lineProg, cfg.cutFromCenter, cfg.cutFromCorner, cfg.outerThicknessMult, cfg.innerThicknessMult);
		}
		stack.popPose();
	}

	private static void renderLine(Line line, PoseStack.Pose pose, VertexConsumer buffer, int c1, int c2, Vec3 zeroMin, double normalised, int alpha, BlockHighlightConfig.LineConfig cfg) {
		int a = c1, b = c2;
		if (c1 != c2) { // lerping between equal colors is the identity, skip the sqrt
			a = getLerpedRGB(c1, c2, (float) (zeroMin.distanceTo(line.minPos) / normalised));
			b = getLerpedRGB(c1, c2, (float) (zeroMin.distanceTo(line.maxPos) / normalised));
		}
		line.render(pose, buffer, a, b, alpha, cfg.lineWidth, cfg.cutFromCenter, cfg.cutFromCorner, cfg.outerThicknessMult, cfg.innerThicknessMult);
	}

	//? if >=26.1 {
	private static void addModelLines(BlockState state, RandomSource randomSource, Vec3 offset, ArrayList<Line> newLines) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		mc.getModelManager().getBlockStateModelSet().get(state).collectParts(randomSource, parts);
		parts.forEach(part -> ((SimpleModelWrapper) part).quads().getAll().forEach(quad -> {
			newLines.add(new Line(new Vec3(quad.position0()).add(offset), new Vec3(quad.position1()).add(offset)));
			newLines.add(new Line(new Vec3(quad.position1()).add(offset), new Vec3(quad.position2()).add(offset)));
			newLines.add(new Line(new Vec3(quad.position2()).add(offset), new Vec3(quad.position3()).add(offset)));
			newLines.add(new Line(new Vec3(quad.position3()).add(offset), new Vec3(quad.position0()).add(offset)));
		}));
	}
	//?} elif >=1.21.11 {
	/*private static void addModelLines(BlockState state, RandomSource randomSource, Vec3 offset, ArrayList<Line> newLines) {
		BlockStateModel model = mc.getModelManager().getBlockModelShaper().getBlockModel(state);
		for (BlockModelPart part : model.collectParts(randomSource)) {
			for (BakedQuad quad : part.getQuads(null)) {
				newLines.add(new Line(new Vec3(quad.position0()).add(offset), new Vec3(quad.position1()).add(offset)));
				newLines.add(new Line(new Vec3(quad.position1()).add(offset), new Vec3(quad.position2()).add(offset)));
				newLines.add(new Line(new Vec3(quad.position2()).add(offset), new Vec3(quad.position3()).add(offset)));
				newLines.add(new Line(new Vec3(quad.position3()).add(offset), new Vec3(quad.position0()).add(offset)));
			}
		}
	}
	*///?} elif >=1.21.5 {
	/*private static void addModelLines(BlockState state, RandomSource randomSource, Vec3 offset, ArrayList<Line> newLines) {
		BlockStateModel model = mc.getModelManager().getBlockModelShaper().getBlockModel(state);
		for (BlockModelPart part : model.collectParts(randomSource)) {
			for (BakedQuad quad : part.getQuads(null)) {
				addRawQuadLines(quad, offset, newLines);
			}
		}
	}
	*///?} else {
	/*private static void addModelLines(BlockState state, RandomSource randomSource, Vec3 offset, ArrayList<Line> newLines) {
		BakedModel model = mc.getModelManager().getBlockModelShaper().getBlockModel(state);
		for (BakedQuad quad : model.getQuads(state, null, randomSource)) {
			addRawQuadLines(quad, offset, newLines);
		}
	}
	*///?}

	//? if >=1.21.5 && <1.21.11 {
	/*private static void addRawQuadLines(BakedQuad quad, Vec3 offset, ArrayList<Line> newLines) {
		int[] v = quad.vertices();
		Vec3[] corners = new Vec3[4];
		for (int i = 0; i < 4; i++) {
			int base = i * 8;
			corners[i] = new Vec3(Float.intBitsToFloat(v[base]), Float.intBitsToFloat(v[base + 1]), Float.intBitsToFloat(v[base + 2])).add(offset);
		}
		newLines.add(new Line(corners[0], corners[1]));
		newLines.add(new Line(corners[1], corners[2]));
		newLines.add(new Line(corners[2], corners[3]));
		newLines.add(new Line(corners[3], corners[0]));
	}
	*///?} elif <1.21.5 {
	/*private static void addRawQuadLines(BakedQuad quad, Vec3 offset, ArrayList<Line> newLines) {
		int[] v = quad.getVertices();
		Vec3[] corners = new Vec3[4];
		for (int i = 0; i < 4; i++) {
			int base = i * 8;
			corners[i] = new Vec3(Float.intBitsToFloat(v[base]), Float.intBitsToFloat(v[base + 1]), Float.intBitsToFloat(v[base + 2])).add(offset);
		}
		newLines.add(new Line(corners[0], corners[1]));
		newLines.add(new Line(corners[1], corners[2]));
		newLines.add(new Line(corners[2], corners[3]));
		newLines.add(new Line(corners[3], corners[0]));
	}
	*///?}

	private static VoxelShape clKey, mlKey;
	private static Direction mlKeyDir;
	private static BlockState mlKeyNeighbor;
	private static List<Line> clTargets = List.of(), mlTargets = List.of();
	private static Vec3 clCenter = Vec3.ZERO, mlCenter = Vec3.ZERO;

	public static void updateModelLines(VoxelShape shape, HitResult evilHitResult) {
		Direction keyDir = null;
		BlockState keyNeighbor = null;
		if (evilHitResult instanceof BlockHitResult bhr) {
			keyDir = joinConnected(bhr.getBlockPos());
			if (keyDir != null) keyNeighbor = mc.level.getBlockState(bhr.getBlockPos().relative(keyDir));
		}
		if (shape != mlKey || keyDir != mlKeyDir || keyNeighbor != mlKeyNeighbor) {
			mlKey = shape;
			mlKeyDir = keyDir;
			mlKeyNeighbor = keyNeighbor;
			mlTargets = buildModelLines(evilHitResult);
			mlCenter = moveToZero(shape).bounds().getCenter();
		}
		modelLines = syncLines(modelLines, mlTargets, mlCenter);
	}

	private static List<Line> buildModelLines(HitResult evilHitResult) {
		ArrayList<Line> newLines = new ArrayList<>();
		if (evilHitResult instanceof BlockHitResult bhr) {
			RandomSource randomSource = RandomSource.create(0);
			BlockPos pos = bhr.getBlockPos();
			Direction dir = joinConnected(pos);
			if (dir != null) {
				Vec3 offset = Vec3.ZERO;
				try {
//					offset = mc.level.getBlockState(pos).getShape(mc.level, pos.relative(dir)).bounds().getMinPosition().reverse();
					if (dir.getAxisDirection() != Direction.AxisDirection.NEGATIVE) {
						offset = unitVec3(dir);
					}
				} catch (Exception ignored) {
				}
				addModelLines(mc.level.getBlockState(pos.relative(dir)), randomSource, offset, newLines);
			}
			Vec3 offset = Vec3.ZERO;
			try {
//				offset = mc.level.getBlockState(pos).getShape(mc.level, pos).bounds().getMinPosition().reverse();
				if (dir != null) {
					if (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
						offset = unitVec3(dir.getOpposite()).subtract(mc.level.getBlockState(pos).getOffset(pos));
					}
				} else {
					offset = mc.level.getBlockState(pos).getShape(mc.level, pos).bounds().getMinPosition().reverse().subtract(mc.level.getBlockState(pos).getOffset(pos).reverse());
				}
			} catch (Exception ignored) {
			}
			addModelLines(mc.level.getBlockState(pos), randomSource, offset, newLines);
		}
		return newLines;
	}

	private static List<Line> syncLines(List<Line> current, List<Line> targets, Vec3 center) {
		while (current.size() < targets.size()) {
			current.add(new Line(center, center));
		}
		while (current.size() > targets.size()) {
			toRemove.add(current.getLast());
			current.removeLast();
		}
		if (!getActiveInstance().doEasing) {
			return targets;
		}
		double k = easeFactor(getActiveInstance().easeSpeed);
		for (int i = 0; i < current.size(); i++) {
			Line line = current.get(i);
			Line target = targets.get(i);
			line.moveTo(target.minPos, target.maxPos, k);
			line.update(true);
		}
		return current;
	}

	public static void updateLinesCommon() {
		if (toRemove.isEmpty()) return;
		Vec3 center = getCenter();
		double k = easeFactor(getActiveInstance().easeSpeed);
		for (Line line : toRemove) {
			line.moveTo(center, center, k);
			line.update(false);
		}
		toRemove.removeIf(line -> line.alphaMultiplier < 1 / 255f);
	}

	public static @NonNull Vec3 getCenter() {
		return moveToZero(easeBox).getCenter();
	}

	public static void updateCollisionLines(VoxelShape shape) {
		if (shape != clKey) {
			clKey = shape;
			VoxelShape zeroed = moveToZero(shape);
			ArrayList<Line> newLines = new ArrayList<>();
			zeroed.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> newLines.add(new Line(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ))));
			clTargets = newLines;
			clCenter = zeroed.bounds().getCenter();
		}
		lines = syncLines(lines, clTargets, clCenter);
	}

	//TODO: minimize usage of moveToZero
	public static AABB moveToZero(AABB box) {
		return box.move(box.getMinPosition().reverse());
	}

	public static VoxelShape moveToZero(VoxelShape shape) {
		Vec3 min = shape.bounds().getMinPosition();
		return shape.move(-min.x, -min.y, -min.z);
	}

	//TODO: make this adjust based on rotation
	private static EnumSet<Direction> getSides(FaceMode type, BlockPos pos, HitResult evilHitResult) {
		return switch (type) {
			case LOOKAT ->
					(evilHitResult instanceof BlockHitResult block) ? EnumSet.of(block.getDirection()) : EnumSet.allOf(Direction.class);
			case AIR_EXPOSED -> EnumSet.complementOf(getConcealedFaces(pos));
			case CONCEALED -> getConcealedFaces(pos);
			default -> EnumSet.allOf(Direction.class);
		};
	}

	public static boolean isBlockEmpty(BlockPos pos) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		//TODO: fix this
		BlockState state = mc.level.getBlockState(pos);
		if (state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED) && !mc.level.getFluidState(pos).isEmpty()) {
			//ignore liquids
			return true;
		}
		return state.isAir(); // == Level.isEmptyBlock(pos)
	}

	public static EnumSet<Direction> getConcealedFaces(BlockPos pos) {
        /*
        I don't know if I should keep the original behavior for this
        As of now, this method means that even when rendering the box for a block with multiple parts,
        it will still cull faces relative to the selected block, and not the entire rendered selection
         */
		EnumSet<Direction> set = EnumSet.allOf(Direction.class);
		if (isBlockEmpty(pos.above())) set.remove(Direction.UP);
		if (isBlockEmpty(pos.below())) set.remove(Direction.DOWN);
		if (isBlockEmpty(pos.north())) set.remove(Direction.NORTH);
		if (isBlockEmpty(pos.east())) set.remove(Direction.EAST);
		if (isBlockEmpty(pos.south())) set.remove(Direction.SOUTH);
		if (isBlockEmpty(pos.west())) set.remove(Direction.WEST);
		return set;
	}

	public static int getLerpedRGB(int c1, int c2, float percent) {
		int r = Math.clamp(Mth.lerpInt(percent, c1 >> 16 & 0xFF, c2 >> 16 & 0xFF), 0, 255);
		int g = Math.clamp(Mth.lerpInt(percent, c1 >> 8 & 0xFF, c2 >> 8 & 0xFF), 0, 255);
		int b = Math.clamp(Mth.lerpInt(percent, c1 & 0xFF, c2 & 0xFF), 0, 255);
		return r << 16 | g << 8 | b;
	}

	public static Color getLerpedColor(Color c1, Color c2, float percent) {
		return new Color(Math.clamp(Mth.lerpInt(percent, c1.getRed(), c2.getRed()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getGreen(), c2.getGreen()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getBlue(), c2.getBlue()), 0, 255));
	}

    public static void mainLoop(LevelRenderContext c) {
        //? if >=26.2 {
        if (featureFrame) { // already advanced + submitted through the feature pipeline this frame
            featureFrame = false;
            return;
        }
        //?}
        HitResult evilHitResult = tick();
        if (evilHitResult == null || !isAnythingVisible()) return;
        get().push("Custom block outline render");
        draw(c.poseStack(), isCrystalObstructed(evilHitResult));
        //? if >=26.2
        flushDraws();
        get().pop();
    }

    //? if >=26.2 {
    private static boolean featureFrame;
    private static boolean featureObstructed;
    private static final PoseStack featurePoseStack = new PoseStack();

    public static void collectSubmits(LevelRenderContext c) {
        //? if >=26.3
        if (mc.gameRenderer.useImprovedTransparency()) return; // AFTER_TERRAIN becomes the OIT phase; keep the END_MAIN pass
        featureFrame = true;
        HitResult evilHitResult = tick();
        if (evilHitResult == null || !isAnythingVisible()) return;
        featureObstructed = isCrystalObstructed(evilHitResult);
        c.submitNodeCollector().submitCustom(SubmitRenderPhases.AFTER_TERRAIN, CBHFeatureRenderer.WORLD);
    }

    public static void drawWorld(Function<RenderType, VertexConsumer> sink) {
        get().push("Custom block outline render");
        featureSink = sink;
        try {
            draw(featurePoseStack, featureObstructed);
        } finally {
            featureSink = null;
            get().pop();
        }
    }
    //?}

    private static HitResult tick() {
        if (mc.player == null || mc.player.gameMode() == null || !getActiveInstance().enableModRendering) return null;
        if (!((!mc.gui.hud.isHidden() || getActiveInstance().showWhenNoHud) && (!mc.player.gameMode().isBlockPlacingRestricted() || getActiveInstance().showWhenNoInteraction))) return null;
        get().push("Custom block outline pre");
        HitResult evilHitResult = getHitResult();
        if (evilHitResult != null) {
            easeBoxAndEdges(evilHitResult, getVoxelShape(evilHitResult));
            updateProgresses(evilHitResult);
        }
        get().pop();
        return evilHitResult;
    }

    private static boolean isAnythingVisible() {
        BlockHighlightConfig cfg = getActiveInstance();
        if (cfg.fillEnabled) {
            for (float f : sideFades) if (Math.round(f) >= 1) return true;
        }
        if (cfg.primary.enabled) {
            for (int i = 0; i < lineStates.size(); i++) {
                LineConfig lc = cfg.getLineConfig(i);
                if (!lc.enabled) continue;
                LineState state = lineStates.get(i);
                if (lc.shapeStyle == ShapeStyle.CLASSIC_BOX) {
                    for (float f : state.lineFades) if (Math.round(f) >= 1) return true;
                } else if (Math.round(state.edgeAlpha) >= 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean usesLineFades(LineConfig lc) {
        return lc.enabled && lc.shapeStyle == ShapeStyle.CLASSIC_BOX;
    }

	public static HitResult getHitResult() {
		if (mc.level == null || mc.player == null || mc.getCameraEntity() == null) return null;
		if (mc.hitResult instanceof EntityHitResult) return mc.hitResult;
		if (getActiveInstance().allowLiquids && isHoldingValidItem()) {
			HitResult yeah = pick(mc.getCameraEntity(), mc.player.blockInteractionRange(), mc.getDeltaTracker().getRealtimeDeltaTicks());
			if (yeah instanceof BlockHitResult) {
				return yeah;
			}
		}
		return mc.hitResult;
	}

	public static boolean isHoldingValidItem() {
		if (getActiveInstance().onlyWhenHoldingAppropriate) {
			return mc.player.getMainHandItem().is(Items.BUCKET) || mc.player.getOffhandItem().is(Items.BUCKET) || mc.player.getMainHandItem().is(Items.LILY_PAD) || mc.player.getOffhandItem().is(Items.LILY_PAD) || mc.player.getMainHandItem().getItem() instanceof BoatItem || mc.player.getOffhandItem().getItem() instanceof BoatItem;
		} else {
			return true;
		}
	}

	private static VoxelShape cachedShape;
	private static BlockPos shapeKeyPos;
	private static BlockState shapeKeyState;
	private static VoxelShape shapeKeyRaw, shapeKeyNeighbor;
	private static Direction shapeKeyDir;
	private static AABB shapeKeyEntityBox;
	private static VoxelShape boundsKey;
	private static AABB boundsValue;

	public static @NonNull VoxelShape getVoxelShape(HitResult evilHitResult) {
		if (mc.level == null || mc.getCameraEntity() == null) return Shapes.block();
		if (evilHitResult instanceof BlockHitResult block) {
			BlockPos pos = block.getBlockPos();
			BlockState state = mc.level.getBlockState(pos);
			var fluid = mc.level.getFluidState(pos);
			VoxelShape raw = fluid.isEmpty() ? state.getShape(mc.level, pos) : fluid.getShape(mc.level, pos);
			Direction connected = null;
			VoxelShape neighbor = null;
			//get connected blocks
			if (getActiveInstance().connectedBlocks) {
				connected = joinConnected(pos);
				if (connected != null) {
					neighbor = mc.level.getBlockState(pos.relative(connected)).getShape(mc.level, pos.relative(connected), CollisionContext.of(mc.getCameraEntity()));
				}
			}
			if (cachedShape != null && pos.equals(shapeKeyPos) && state == shapeKeyState && raw == shapeKeyRaw && connected == shapeKeyDir && neighbor == shapeKeyNeighbor) {
				return cachedShape;
			}
			shapeKeyPos = pos;
			shapeKeyState = state;
			shapeKeyRaw = raw;
			shapeKeyDir = connected;
			shapeKeyNeighbor = neighbor;
			shapeKeyEntityBox = null;
			VoxelShape shape = raw.isEmpty() ? Shapes.block() : raw;
			if (connected != null) {
				shape = Shapes.join(shape, neighbor.move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR);
			}
			return cachedShape = shape.move(pos.getX(), pos.getY(), pos.getZ());
		} else if (evilHitResult instanceof EntityHitResult entityHitResult && getActiveInstance().allowEntities && !entityHitResult.getEntity().isInvisible()) {
			Entity entity = entityHitResult.getEntity();
			//so, so sloppy. might also have the worst workaround of the century for hanging stuff
			float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			AABB boundingBox = moveToZero(entity.getBoundingBox());
			AABB box = boundingBox.move(entity.getPosition(delta).subtract(boundingBox.getCenter()).add(0, entity instanceof HangingEntity ? 0 : boundingBox.maxY / 2F, 0));
			if (cachedShape != null && box.equals(shapeKeyEntityBox)) {
				return cachedShape;
			}
			shapeKeyPos = null;
			shapeKeyEntityBox = box;
			return cachedShape = Shapes.create(box);
		}
		return Shapes.block();
	}

	public static void easeBoxAndEdges(HitResult evilHitResult, VoxelShape shape) {
		if (shape != boundsKey) {
			boundsKey = shape;
			boundsValue = shape.bounds();
		}
		AABB targetBox = boundsValue;
		BlockHighlightConfig cfg = getActiveInstance();
		if (cfg.doEasing) {
			if (cfg.updateWhenUnfocused || evilHitResult.getType() != HitResult.Type.MISS) {
				double k = easeFactor(cfg.easeSpeed);
				easeBox = new AABB(easeBox.minX + (targetBox.minX - easeBox.minX) * k, easeBox.minY + (targetBox.minY - easeBox.minY) * k, easeBox.minZ + (targetBox.minZ - easeBox.minZ) * k, easeBox.maxX + (targetBox.maxX - easeBox.maxX) * k, easeBox.maxY + (targetBox.maxY - easeBox.maxY) * k, easeBox.maxZ + (targetBox.maxZ - easeBox.maxZ) * k);
			}
		} else {
			easeBox = targetBox;
		}
		boolean updateCL = false, updateML = false;
		for (int i = 0; i < 3; i++) {
			ShapeStyle style = cfg.getLineConfig(i).shapeStyle;
			if (style == ShapeStyle.COLLISION_SHAPE) updateCL = true;
			if (style == ShapeStyle.MODEL_SHAPE) updateML = true;
		}
		if (updateCL) {
			updateCollisionLines(shape);
		}
		if (updateML) {
			updateModelLines(shape, evilHitResult);
		}
		updateLinesCommon();
	}

	private static void draw(PoseStack stack, boolean isCrystalObstructed) {
		//render the fill first, we don't want it drawn over the outline
		if (getActiveInstance().fillEnabled) {
			get().push("drawFill");
			drawFill(stack, isCrystalObstructed);
			get().pop();
		}
		//now the outline itself
		if (getActiveInstance().primary.enabled) {
			get().push("drawOutline");
			drawOutlines(stack, isCrystalObstructed);
			get().pop();
		}
	}

	private static boolean isCrystalObstructed(HitResult evilHitResult) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		if (!(evilHitResult instanceof BlockHitResult block)) return false;
		BlockState state = mc.level.getBlockState(block.getBlockPos());

		if (getActiveInstance().crystalHelper) {
			if (state.getBlock().equals(Blocks.OBSIDIAN) || state.getBlock().equals(Blocks.BEDROCK)) {
				double pd = block.getBlockPos().above().getX();
				double pe = block.getBlockPos().above().getY();
				double pf = block.getBlockPos().above().getZ();
				return !mc.level.isEmptyBlock(block.getBlockPos().above()) || !mc.level.getEntities(null, new AABB(pd, pe, pf, pd + 1.0, pe + 2.0, pf + 1.0)).isEmpty();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private static void drawFill(PoseStack stack, boolean isCrystalObstructed) {
		boolean b = getActiveInstance().fillDepthTest != DepthTestMode.ALWAYS_PASS && getActiveInstance().fillExpandBlocks == 0 && getActiveInstance().fillExpandPercent == 1;
		Renderer.drawBoxFill(stack, easeBox.inflate(b ? 0.00005 : 0), getActiveInstance().fillCol.getColors(isCrystalObstructed, getActiveInstance().crystalHelperFillColor), sideFades);
	}

	private static void drawOutlines(PoseStack stack, boolean isCrystalObstructed) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		DepthTestMode prevMode = null;
		VertexConsumer buffer = null;
		for (int layer = 0; layer < 3; layer++) {
			LineConfig lineConfig = getActiveInstance().getLineConfig(layer);
			if (lineConfig.enabled) {
				if (prevMode != lineConfig.lineDepthTest) {
					if (prevMode != null) {
						finishDraw(true, prevMode);
					}
					buffer = startDrawing(true, lineConfig.lineDepthTest);
					prevMode = lineConfig.lineDepthTest;
				}
				drawLineLayer(stack, lineConfig, isCrystalObstructed, layer, buffer);
			}
		}
		finishDraw(true, getActiveInstance().primary.lineDepthTest);
	}

	// this is so bad
	private static void updateProgresses(HitResult evilHitResult) {
		if (mc.level == null) return;
		BlockHighlightConfig cfg = getActiveInstance();
		boolean miss = evilHitResult.getType() == HitResult.Type.MISS;
		if (evilHitResult instanceof EntityHitResult entityHitResult && !entityHitResult.getEntity().isInvisible()) {
			if (cfg.allowEntities) {
				if (cfg.fillEnabled) {
					for (int d = 0; d < 6; d++) {
						sideFades[d] = cfg.fadeIn ? easeF(sideFades[d], cfg.fillCol.alpha, cfg.fadeInSpeed) : cfg.fillCol.alpha;
					}
				}
				for (int i = 0; i < lineStates.size(); i++) {
					LineState lineState = lineStates.get(i);
					LineConfig lc = cfg.getLineConfig(i);
					int targetAlpha = lc.color.alpha;
					if (usesLineFades(lc)) {
						for (int d = 0; d < 6; d++) {
							lineState.lineFades[d] = cfg.fadeIn ? easeF(lineState.lineFades[d], targetAlpha, cfg.fadeInSpeed) : targetAlpha;
						}
					}
					lineState.edgeAlpha = cfg.fadeIn ? easeF(lineState.edgeAlpha, targetAlpha, cfg.fadeInSpeed) : targetAlpha;
				}
			} else {
				miss = true;
				exitFades();
			}
			if (cfg.rotations) {
				rotation.nlerp(new Quaternionf(), (float) (1 - Math.exp((mc.options.enableVsync().get() || !getActiveInstance().improvedEasing ? -(1.0F / mc.getFps()) : -((double) mc.getFrameTimeNs() / 1000000000)) * cfg.rotationSpeed)));
			}
		} else if (evilHitResult instanceof BlockHitResult block) {
			if (mc.level.isEmptyBlock(block.getBlockPos()) || miss) {
				exitFades();
			} else {
				@SuppressWarnings("unchecked")
				EnumSet<Direction>[] sidesByMode = new EnumSet[FaceMode.values().length]; // several layers often share a mode
				for (int i = 0; i < lineStates.size(); i++) {
					LineState lineState = lineStates.get(i);
					LineConfig lc = cfg.getLineConfig(i);
					int targetAlpha = lc.color.alpha;
					if (usesLineFades(lc)) {
						EnumSet<Direction> lines = sidesByMode[lc.outlineType.ordinal()];
						if (lines == null) lines = sidesByMode[lc.outlineType.ordinal()] = getSides(lc.outlineType, block.getBlockPos(), evilHitResult);
						for (Direction d : Direction.values()) {
							if (lines.contains(d)) {
								lineState.lineFades[d.ordinal()] = cfg.fadeIn ? easeF(lineState.lineFades[d.ordinal()], targetAlpha, cfg.fadeInSpeed) : targetAlpha;
							} else {
								lineState.lineFades[d.ordinal()] = cfg.fadeOut ? easeF(lineState.lineFades[d.ordinal()], 0, cfg.fadeOutSpeed) : 0;
							}
						}
					}
					lineState.edgeAlpha = cfg.fadeIn ? easeF(lineState.edgeAlpha, targetAlpha, cfg.fadeInSpeed) : targetAlpha;
				}
				if (cfg.fillEnabled) {
					EnumSet<Direction> sides = sidesByMode[cfg.fillType.ordinal()];
					if (sides == null) sides = getSides(cfg.fillType, block.getBlockPos(), evilHitResult);
					for (Direction dir : Direction.values()) {
						if (sides.contains(dir)) {
							sideFades[dir.ordinal()] = cfg.fadeIn ? easeF(sideFades[dir.ordinal()], cfg.fillCol.alpha, cfg.fadeInSpeed) : cfg.fillCol.alpha;
						} else {
							sideFades[dir.ordinal()] = cfg.fadeOut ? easeF(sideFades[dir.ordinal()], 0, cfg.fadeOutSpeed) : 0;
						}
					}
				}
			}
			if (cfg.rotations) {
				Direction d = block.getDirection();
				Quaternionf target = d.getRotation();

				if (d != Direction.UP && d != Direction.DOWN) {
					lastHorizontalDirection = d;
				} else {
					float pitch = (float) ((d == Direction.UP) ? (-Math.PI / 2F) : (Math.PI / 2F));
					target = new Quaternionf(lastHorizontalDirection.getRotation()).rotateX(pitch);
				}
				if (!isCubeLike(mc.level.getBlockState(block.getBlockPos()).getShape(mc.level, block.getBlockPos()))) {
					target = new Quaternionf();
				}

				rotation.nlerp(target, (float) (1 - Math.exp((mc.options.enableVsync().get() || !getActiveInstance().improvedEasing ? -(1.0F / mc.getFps()) : -((double) mc.getFrameTimeNs() / 1000000000)) * cfg.rotationSpeed)));
			}
		}
		//I didn't add in/out because it would BREAKKK. TODO THIS
		scaleProg = cfg.scale ? easeF(scaleProg, miss ? 0 : 1, cfg.scaleSpeed) : 1;
		lineProg = cfg.animateLineThickness ? easeF(lineProg, miss ? 0 : 1, cfg.lineThicknessAnimationSpeed) : 1;
	}

	public static void exitFades() {
		BlockHighlightConfig cfg = getActiveInstance();
		if (cfg.fillEnabled) {
			for (int d = 0; d < 6; d++) {
				sideFades[d] = cfg.fadeOut ? easeF(sideFades[d], 0, cfg.fadeOutSpeed) : 0;
			}
		}
		for (int i = 0; i < lineStates.size(); i++) {
			LineState lineState = lineStates.get(i);
			if (usesLineFades(cfg.getLineConfig(i))) {
				for (int n = 0; n < 6; n++) lineState.fadeOutSides();
			}
			lineState.edgeAlpha = cfg.fadeOut ? easeF(lineState.edgeAlpha, 0, cfg.fadeOutSpeed) : 0;
		}
	}

	public static HitResult pick(Entity e, final double range, final float a) {
		if (mc.level == null) return null;
		Vec3 from = e.getEyePosition(a);
		Vec3 viewVector = e.getViewVector(a);
		Vec3 to = from.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
		return mc.level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, getActiveInstance().onlySourceBlocks ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.ANY, e));
	}

	private static Direction joinConnected(BlockPos pos) {
		if (mc.level == null) return null;
		BlockState connectedState;
		Direction dir;
		BlockPos connectedPos;
		BlockState state = mc.level.getBlockState(pos);
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
			DoubleBlockHalf halfState = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
			Direction d = halfState == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
			connectedPos = pos.relative(d);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock().getClass().equals(state.getBlock().getClass())) {
				if (connectedState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && connectedState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == halfState.getOtherHalf()) {
					return d;
				}
			}
		}
		if (state.getBlock() instanceof ChestBlock && !state.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)) {
			dir = ChestBlock.getConnectedDirection(state);
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof ChestBlock) {
				return dir;
			}
		}
		if (state.getBlock() instanceof BedBlock) {
			BedPart part = state.getValue(BedBlock.PART);
			dir = state.getValue(HorizontalDirectionalBlock.FACING);
			if (part == BedPart.HEAD) {
				dir = dir.getOpposite();
			}
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof BedBlock && connectedState.getValue(BedBlock.PART) != part) {
				return dir;
			}
		}
		if (state.getBlock() instanceof PistonHeadBlock) {
			dir = state.getValue(PistonBaseBlock.FACING);
			Direction oppDir = dir.getOpposite();
			connectedPos = pos.relative(oppDir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof PistonBaseBlock && connectedState.getValue(PistonBaseBlock.FACING) == dir) {
				return oppDir;
			}
		}
		if (state.getBlock() instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED)) {
			dir = state.getValue(PistonBaseBlock.FACING);
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof PistonHeadBlock && connectedState.getValue(PistonBaseBlock.FACING) == dir) {
				return dir;
			}
		}
		return null;
	}
}
