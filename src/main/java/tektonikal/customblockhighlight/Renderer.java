package tektonikal.customblockhighlight;
//? if >=26.2
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
//? if >1.8.9 {
import com.mojang.blaze3d.vertex.*;
//?} else {
/*import tektonikal.customblockhighlight.legacy.vertex.*;
*///?}
import it.unimi.dsi.fastutil.Pair;
//? if >=26.2
import it.unimi.dsi.fastutil.floats.FloatArrayList;
//? if >1.8.9 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
//?} else
//import tektonikal.customblockhighlight.legacy.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
//? if >=26.2
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
//? if >1.8.9 {
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
//?} else {
/*import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.WorldSettings;
*///?}
//? if >1.8.9 && <1.21.2
//import net.minecraft.util.profiling.ProfilerFiller;
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
import java.util.function.Function;
import java.util.function.Supplier;

//? if >=1.21.2
import static net.minecraft.util.profiling.Profiler.get;
import static tektonikal.customblockhighlight.CustomBlockHighlight.easeFactor;
import static tektonikal.customblockhighlight.CustomBlockHighlight.fadeIn;
import static tektonikal.customblockhighlight.CustomBlockHighlight.fadeOut;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;
import static tektonikal.customblockhighlight.RenderBackend.*;

public final class Renderer {
	private Renderer() {
	}

	public static final Minecraft mc = Minecraft.getInstance();
	//? if >1.8.9 {
	public static final Camera camera = mc.gameRenderer.mainCamera();
	//?} else
	//public static final Camera camera = Camera.MAIN;

	//? if >=1.21.4
	private static boolean isCubeLike(VoxelShape shape) { return ((VoxelShapeAccessor) shape).invokeIsCubeLike(); }
	//? if <1.21.4 {

	/*private static boolean isCubeLike(VoxelShape shape) {
		if (shape.isEmpty()) return false;
		AABB bounds = shape.bounds();
		return bounds.minX <= 0 && bounds.minY <= 0 && bounds.minZ <= 0 && bounds.maxX >= 1 && bounds.maxY >= 1 && bounds.maxZ >= 1;
	}
	*///?}

	//? if >1.8.9 && <1.21.2 {
	/*private static ProfilerFiller get() { return mc.getProfiler(); }
	*///?}
	//? if =1.8.9
	//private static Profiler get() { return mc.profiler; }

	private static final String PROFILED = "customBlockHighlight";

	private static final float[] sideFades = new float[6];
	private static final class ShapeLayer {
		List<Line> lines = new ArrayList<>();
		final List<Line> leavingLines = new ArrayList<>();
		List<Quad> quads = new ArrayList<>();
		final List<Quad> leavingQuads = new ArrayList<>();
		final ShapeSync.UnifiedEdges edges = new ShapeSync.UnifiedEdges();
		VoxelShape key;
		Object model;
		ShapeGeometry geometry = ShapeGeometry.EMPTY;
		Vec3 center = Vec3.ZERO;

		void update(VoxelShape local, Object model, Supplier<ShapeGeometry> build, boolean wantLines, boolean wantQuads) {
			if (local != key || model != this.model) {
				key = local;
				this.model = model;
				geometry = build.get();
				center = local.bounds().getCenter();
			}
			if (wantLines) lines = syncLines(lines, geometry.lines(), center, leavingLines, edges);
			if (wantQuads) quads = syncQuads(quads, geometry.quads(), center, leavingQuads, edges);
		}

		void easeLeaving() {
			if (leavingLines.isEmpty() && leavingQuads.isEmpty()) return;
			Vec3 middle = getCenter();
			double k = getActiveInstance().scaleOut ? easeFactor(getActiveInstance().easeSpeed) : 0;
			for (Line line : leavingLines) {
				line.moveTo(middle, middle, k);
				line.update(false);
			}
			leavingLines.removeIf(line -> line.alphaMultiplier < 1 / 255f);
			for (Quad quad : leavingQuads) {
				quad.collapseTo(middle, k);
				quad.update(false);
			}
			leavingQuads.removeIf(quad -> quad.alphaMultiplier < 1 / 255f);
		}
	}

	private static final ShapeLayer collisionShape = new ShapeLayer(), modelShape = new ShapeLayer();

	private static ShapeLayer shapeLayer(ShapeStyle style) {
		return style == ShapeStyle.COLLISION_SHAPE ? collisionShape : modelShape;
	}


	private static AABB easeBox = new AABB(0, 0, 0, 0, 0, 0);
	private static Direction lookedAt;

	static Vec3[] lookedAtFace() {
		return lookedAt == null ? null : Vertexer.face(easeBox, lookedAt.ordinal());
	}

	private static float scaleProg = 0;
	private static float lineProg = 0;
	private static float cutProg = 0;
	private static Quaternionf rotation = new Quaternionf();
	private static final BoxRoll roll = new BoxRoll();

	public static final Matrix4f lastProjMat = new Matrix4f().setPerspective((float) Math.toRadians(70), 16 / 9F, 0.05F, 1024);
	public static final Matrix4f lastModMat = new Matrix4f();

	public static final float HIDDEN_PULL = 1F - 1F / 64F;

	private static final List<LineState> lineStates = List.of(new LineState(), new LineState(), new LineState());

	//? if =1.8.9 {
	/*
	private static Vec3 legacyOrigin = Vec3.ZERO;
	*///?}

	private static void drawBoxFill(PoseStack stack, AABB box, Pair<Color, Color> cols, float[] alpha) {
		doEvilMatrixPreparations(stack, box, getActiveInstance().fillExpandBlocks, getActiveInstance().fillExpandPercent);
		pullFill(stack);
		VertexConsumer buffer = startDrawing(false, getActiveInstance().fillDepthTest, getActiveInstance().fillBlend);
		Vertexer.vertexBoxQuads(stack.last(), buffer, moveToZero(box), cols, rotateFaceFades(alpha));
		finishDraw(false, getActiveInstance().fillDepthTest, getActiveInstance().fillBlend);
		stack.popPose();
	}

	private static final float[] rotatedFaceFades = new float[6];

	private static float[] rotateFaceFades(float[] fades) {
		if (!getActiveInstance().rotations) return fades;
		Vector3f v = new Vector3f();
		for (Direction face : Direction.values()) {
			rotation.transform(v.set(face.getStepX(), face.getStepY(), face.getStepZ()));
			//~ if <1.21.2 'getApproximateNearest' -> 'getNearest'
			rotatedFaceFades[face.ordinal()] = fades[Direction.getApproximateNearest(v.x, v.y, v.z).ordinal()];
		}
		return rotatedFaceFades;
	}

	private static boolean snapping;
	public static Vec3 renderOrigin() {
		//~ if =1.8.9 'camera.position()' -> 'legacyOrigin'
		return camera.position();
	}

	private static void doEvilMatrixPreparations(PoseStack stack, AABB box, float scaleBlocks, float scalePercentage) {
		stack.pushPose();
		Vec3 origin = renderOrigin();
		stack.translate(box.minX - origin.x, box.minY - origin.y, box.minZ - origin.z);
		Vec3 vec = moveToZero(box).getCenter();
		stack.translate(vec.x, vec.y, vec.z);
		if (getActiveInstance().rotations) {
			stack.rotateAround(rotation, 0, 0, 0);
		}
		Vertexer.applyExpansion(stack, box, scaleBlocks, scalePercentage);
		stack.scale(scaleProg, scaleProg, scaleProg);
		stack.translate(-vec.x, -vec.y, -vec.z);
	}

	private static void drawLineLayer(PoseStack stack, BlockHighlightConfig.LineConfig cfg, boolean obstructed, int layer, VertexConsumer buffer) {
		get().push(cfg.screenOutline.enabled ? "screenSpace" : "lines");
		//? if <26.2
		//currentLineWidth = cfg.lineWidth * lineProg;
		doEvilMatrixPreparations(stack, easeBox, cfg.lineExpandBlocks, cfg.lineExpandPercentage);
		//? if >1.8.9
		if (cfg.lineDepthTest == DepthTestMode.HIDDEN_ONLY) stack.last().pose().scaleLocal(HIDDEN_PULL);
		AABB zeroed = moveToZero(easeBox);
		LineState state = lineStates.get(layer);
		float[] cuts = LineState.animatedCuts(state.cutFromCenter, state.cutFromCorner, cutProg);
		float center = cuts[0], corner = cuts[1];
		Pair<Color, Color> cols = cfg.color.getColors(obstructed, getActiveInstance().crystalHelperLineColor);
		Vertexer.beginLayer(cfg.lineBlend);
		try {
			if (cfg.screenOutline.enabled) {
				float[] fades = state.lineFades;
				Vertexer.vertexScreenBox(stack.last(), buffer, screenParts(cfg.shapeStyle, fades, zeroed), cfg.screenOutline, cols, Math.round(mostVisible(fades)), cfg.lineWidth * lineProg, center, corner, cfg.outerThicknessMult, cfg.innerThicknessMult);
			} else if (cfg.shapeStyle != ShapeStyle.CLASSIC_BOX) {
				float[] fades = rotateFaceFades(state.lineFades);
				PoseStack.Pose pose = stack.last();
				int c1 = cols.first().getRGB() & 0xFFFFFF;
				int c2 = cols.second().getRGB() & 0xFFFFFF;
				Vec3 zeroMin = zeroed.getMinPosition();
				double normalised = zeroMin.distanceTo(zeroed.getMaxPosition());
				float width = cfg.lineWidth * lineProg;
				ShapeLayer shape = shapeLayer(cfg.shapeStyle);
				if (unified(getActiveInstance())) {
					for (Line line : shape.edges.of(shape.quads)) renderLine(line, pose, buffer, c1, c2, zeroMin, normalised, LineState.faceAlpha(line.dirMask, fades, 0), width, center, corner, cfg);
					for (Quad quad : shape.leavingQuads) renderQuadEdges(quad, pose, buffer, c1, c2, zeroMin, normalised, fades, width, center, corner, cfg);
				} else {
					int edgeAlpha = Math.round(state.edgeAlpha);
					for (Line line : shape.lines) renderLine(line, pose, buffer, c1, c2, zeroMin, normalised, LineState.faceAlpha(line.dirMask, fades, edgeAlpha), width, center, corner, cfg);
					for (Line line : shape.leavingLines) renderLine(line, pose, buffer, c1, c2, zeroMin, normalised, LineState.faceAlpha(line.dirMask, fades, edgeAlpha), width, center, corner, cfg);
				}
			} else {
				Vertexer.vertexBoxLines(stack.last(), buffer, zeroed, cols, rotateFaceFades(state.lineFades), cfg.lineWidth * lineProg, center, corner, cfg.outerThicknessMult, cfg.innerThicknessMult);
			}
		} finally {
			Vertexer.endLayer();
		}
		stack.popPose();
		get().pop();
	}

	private static void renderLine(Line line, PoseStack.Pose pose, VertexConsumer buffer, int c1, int c2, Vec3 zeroMin, double normalised, int alpha, float width, float cutFromCenter, float cutFromCorner, BlockHighlightConfig.LineConfig cfg) {
		int a = c1, b = c2;
		if (c1 != c2) {
			a = Vertexer.getLerpedRGB(c1, c2, (float) (zeroMin.distanceTo(line.minPos) / normalised));
			b = Vertexer.getLerpedRGB(c1, c2, (float) (zeroMin.distanceTo(line.maxPos) / normalised));
		}
		line.render(pose, buffer, a, b, alpha, width, cutFromCenter, cutFromCorner, cfg.outerThicknessMult, cfg.innerThicknessMult);
	}

	private static void renderQuadEdges(Quad quad, PoseStack.Pose pose, VertexConsumer buffer, int c1, int c2, Vec3 zeroMin, double normalised, float[] fades, float width, float cutFromCenter, float cutFromCorner, BlockHighlightConfig.LineConfig cfg) {
		for (int e = 0; e < 4; e++) {
			if (quad.edgeMasks[e] == 0) continue;
			Vec3 from = quad.corners[e], to = quad.corners[(e + 1) % 4];
			if (from.distanceToSqr(to) < 1.0E-12) continue;
			Line edge = new Line(from, to);
			edge.alphaMultiplier = quad.alphaMultiplier;
			renderLine(edge, pose, buffer, c1, c2, zeroMin, normalised, LineState.faceAlpha(quad.edgeMasks[e], fades, 0), width, cutFromCenter, cutFromCorner, cfg);
		}
	}

	private static void pullFill(PoseStack stack) {
		//? if >1.8.9
		if (getActiveInstance().fillDepthTest != DepthTestMode.ALWAYS_PASS) stack.last().pose().scaleLocal(HIDDEN_PULL);
	}

	private static void drawScreenFill(PoseStack stack, Pair<Color, Color> cols) {
		BlockHighlightConfig cfg = getActiveInstance();
		doEvilMatrixPreparations(stack, easeBox, cfg.fillExpandBlocks, cfg.fillExpandPercent);
		VertexConsumer buffer = startDrawing(false, cfg.fillDepthTest, cfg.fillBlend);
		Vertexer.vertexScreenFill(stack.last(), buffer, screenParts(cfg.fillShape, sideFades, moveToZero(easeBox)), cfg.fillScreenOutline, cols, Math.round(mostVisible(sideFades)));
		finishDraw(false, cfg.fillDepthTest, cfg.fillBlend);
		stack.popPose();
	}

	private static float mostVisible(float[] fades) {
		float most = 0;
		for (float fade : fades) most = Math.max(most, fade);
		return most;
	}

	private static List<Vec3[]> screenParts(ShapeStyle style, float[] fades, AABB zeroed) {
		float half = mostVisible(fades) / 2;
		float[] rotated = rotateFaceFades(fades);
		List<Vec3[]> parts = new ArrayList<>();
		if (style == ShapeStyle.CLASSIC_BOX) {
			boolean all = true;
			for (float fade : rotated) all &= fade >= half;
			if (all) return List.<Vec3[]>of(Vertexer.corners(zeroed));
			for (int d = 0; d < 6; d++) if (rotated[d] >= half) parts.add(Vertexer.face(zeroed, d));
			return parts;
		}
		ShapeLayer shape = shapeLayer(style);
		for (Quad quad : shape.quads) if (rotated[quad.dir] >= half) parts.add(quad.corners);
		for (Quad quad : shape.leavingQuads) {
			if (quad.alphaMultiplier >= 0.5F && rotated[quad.dir] >= half) parts.add(quad.corners);
		}
		return parts;
	}

	private static void drawShapeFill(PoseStack stack, ShapeLayer shape, Pair<Color, Color> cols) {
		BlockHighlightConfig cfg = getActiveInstance();
		doEvilMatrixPreparations(stack, easeBox, cfg.fillExpandBlocks, cfg.fillExpandPercent);
		pullFill(stack);
		VertexConsumer buffer = startDrawing(false, cfg.fillDepthTest, cfg.fillBlend);
		float[] fades = rotateFaceFades(sideFades);
		AABB zeroed = moveToZero(easeBox);
		Vec3 zeroMin = zeroed.getMinPosition();
		float normaliser = (float) zeroMin.distanceTo(zeroed.getMaxPosition());
		for (List<Quad> list : List.of(shape.quads, shape.leavingQuads)) {
			for (Quad quad : list) {
				int alpha = Math.round(fades[quad.dir] * quad.alphaMultiplier);
				if (alpha < 1) continue;
				Vertexer.vertexQuad(stack.last(), buffer, cols, alpha, zeroMin, normaliser, quad.corners);
			}
		}
		finishDraw(false, cfg.fillDepthTest, cfg.fillBlend);
		stack.popPose();
	}

	private static List<Line> syncLines(List<Line> current, List<Line> targets, Vec3 center, List<Line> leaving, ShapeSync.UnifiedEdges edges) {
		if (!getActiveInstance().doEasing) {
			return targets;
		}
		return ShapeSync.syncLines(current, targets, center, leaving, edges, snapping, easeFactor(getActiveInstance().easeSpeed));
	}

	private static List<Quad> syncQuads(List<Quad> current, List<Quad> targets, Vec3 center, List<Quad> leaving, ShapeSync.UnifiedEdges edges) {
		if (!getActiveInstance().doEasing) {
			return targets;
		}
		return ShapeSync.syncQuads(current, targets, center, leaving, edges, snapping, easeFactor(getActiveInstance().easeSpeed));
	}

	private static void updateLinesCommon() {
		collisionShape.easeLeaving();
		modelShape.easeLeaving();
	}

	public static @NonNull Vec3 getCenter() {
		return moveToZero(easeBox).getCenter();
	}

	public static AABB moveToZero(AABB box) {
		return box.move(box.getMinPosition().reverse());
	}

	public static VoxelShape moveToZero(VoxelShape shape) {
		Vec3 min = shape.bounds().getMinPosition();
		return shape.move(-min.x, -min.y, -min.z);
	}

	private static final int ALL_SIDES = 63;

	private static int sides(FaceMode type, Target target) {
		if (!(target.hit() instanceof BlockHitResult block)) return ALL_SIDES;
		return switch (type) {
			case LOOKAT -> 1 << block.getDirection().ordinal();
			case AIR_EXPOSED -> ALL_SIDES & ~concealedFaces(target.pos(), target.connected());
			case CONCEALED -> concealedFaces(target.pos(), target.connected());
			default -> ALL_SIDES;
		};
	}

	public static boolean isBlockEmpty(BlockPos pos) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		//? if >=1.21.4 {
		return !mc.level.getBlockState(pos).isSolidRender();
		//?} elif >1.8.9 {
		/*return !mc.level.getBlockState(pos).isSolidRender(mc.level, pos);
		*///?} else {
		/*return !mc.level.getBlockState(pos).getBlock().isOpaque();
		*///?}
	}

	private static int concealed;

	private static int concealedFaces(BlockPos pos, Direction connected) {
		if (concealed >= 0) return concealed;
		BlockPos other = connected == null ? null : pos.relative(connected);
		concealed = ALL_SIDES;
		for (Direction d : Direction.values()) {
			boolean open;
			if (other == null || d == connected.getOpposite()) open = isBlockEmpty(pos.relative(d));
			else if (d == connected) open = isBlockEmpty(other.relative(d));
			else open = isBlockEmpty(pos.relative(d)) || isBlockEmpty(other.relative(d));
			if (open) concealed &= ~(1 << d.ordinal());
		}
		return concealed;
	}

	public static void mainLoop(LevelRenderContext c) {
		//? if >=1.21.5
		if (IrisCompat.isRenderingShadowPass()) return;
		//? if >=26.2 {
		if (featureFrame) {
			featureFrame = false;
			return;
		}
		//?}
		HitResult evilHitResult = tick();
		get().push(PROFILED);
		//? if >=26.3
		drawQueuedShapeOutlines();
		if (evilHitResult != null) {
			//? if =1.8.9 {
			/*Vec3 cam = camera.position();
			double ox = Math.floor(cam.x), oy = Math.floor(cam.y), oz = Math.floor(cam.z);
			if (ox != legacyOrigin.x || oy != legacyOrigin.y || oz != legacyOrigin.z) legacyOrigin = new Vec3(ox, oy, oz);
			legacyBuffer.beginFrame();
			*///?}
			draw(c.poseStack(), Target.isCrystalObstructed(evilHitResult));
			//? if =1.8.9 {
			/*get().push("flush");
			legacyBuffer.drawFrame(ox - cam.x, oy - cam.y, oz - cam.z, true);
			get().pop();
			*///?}
		}
		//? if >=1.21.5 {
		get().push("flush");
		flushDraws();
		get().pop();
		//?}
		get().pop();
	}

	//? if >=26.2 {
	private static boolean featureFrame;
	private static boolean featureObstructed;
	private static final PoseStack featurePoseStack = new PoseStack();

	public static void collectSubmits(LevelRenderContext c) {
		if (IrisCompat.isRenderingShadowPass()) return;
		//? if >=26.3
		if (mc.gameRenderer.useImprovedTransparency()) return;
		featureFrame = true;
		HitResult evilHitResult = tick();
		if (evilHitResult == null) return;
		boolean translucentTarget = isTranslucentTarget(evilHitResult);
		featureObstructed = Target.isCrystalObstructed(evilHitResult);
		c.submitNodeCollector().submitCustom(IrisCompat.isShaderPackInUse() ? SubmitRenderPhases.TRANSLUCENT_CUSTOM_GEOMETRY : translucentTarget ? SubmitRenderPhases.AFTER_TERRAIN : SubmitRenderPhases.SHAPE_OUTLINES, CBHFeatureRenderer.WORLD);
	}

	private static boolean isTranslucentTarget(HitResult hit) {
		return hit instanceof BlockHitResult block && hit.getType() != HitResult.Type.MISS
				&& mc.getModelManager().getBlockStateModelSet().get(mc.level.getBlockState(block.getBlockPos())).hasMaterialFlag(1);
	}

	public static void drawWorld(Function<RenderType, VertexConsumer> sink) {
		get().push(PROFILED);
		featureSink = sink;
		try {
			draw(featurePoseStack, featureObstructed);
		} finally {
			featureSink = null;
			get().pop();
		}
	}

	public static boolean restylesShapeOutlines() {
		BlockHighlightConfig cfg = getActiveInstance();
		return cfg.enableModRendering && cfg.restyleOtherOutlines && cfg.primary.enabled && !cfg.drawVanillaOutline;
	}

	public static void drawShapeOutline(PoseStack.Pose pose, VoxelShape shape, Function<RenderType, VertexConsumer> sink) {
		if (shape.isEmpty()) return;
		//? if >=26.3 {
		if (mc.gameRenderer.useImprovedTransparency()) {
			queuedOutlines.add(new QueuedOutline(pose, shape));
			return;
		}
		//?}
		get().push(PROFILED);
		get().push("otherOutlines");
		featureSink = sink;
		try {
			drawShapeOutline(pose, shape);
		} finally {
			featureSink = null;
			get().pop();
			get().pop();
		}
	}

	//? if >=26.3 {
	private record QueuedOutline(PoseStack.Pose pose, VoxelShape shape) {
	}

	private static final List<QueuedOutline> queuedOutlines = new ArrayList<>();

	private static void drawQueuedShapeOutlines() {
		if (queuedOutlines.isEmpty()) return;
		get().push("otherOutlines");
		for (QueuedOutline outline : queuedOutlines) drawShapeOutline(outline.pose(), outline.shape());
		queuedOutlines.clear();
		get().pop();
	}
	//?}

	private static boolean behindLookedAtFace(Direction looked, PoseStack.Pose pose, AABB bounds) {
		Vector3f normal = new Vector3f(looked.getStepX(), looked.getStepY(), looked.getStepZ());
		float planeAt = normal.dot(Vertexer.face(easeBox, looked.ordinal())[0].subtract(camera.position()).toVector3f());
		return Vertexer.behindPlane(normal, planeAt, pose.pose(), bounds);
	}

	private static void drawShapeOutline(PoseStack.Pose pose, VoxelShape shape) {
		BlockHighlightConfig cfg = getActiveInstance();
		AABB bounds = shape.bounds();
		Vec3 min = bounds.getMinPosition();
		Vec3 center = bounds.getCenter();
		double normalised = min.distanceTo(bounds.getMaxPosition());
		Direction looked = lookedAt;
		for (int layer = 2; layer >= 0; layer--) {
			LineConfig lc = cfg.getLineConfig(layer);
			if (!lc.enabled) continue;
			PoseStack.Pose layerPose = pose.copy();
			layerPose.translate((float) center.x, (float) center.y, (float) center.z);
			CBHFeatureRenderer.applyExpansion(layerPose, bounds, lc.lineExpandBlocks, lc.lineExpandPercentage);
			layerPose.translate((float) -center.x, (float) -center.y, (float) -center.z);
			boolean quads = lc.screenOutline.enabled;
			VertexConsumer buffer = quads && featureSink != null ? featureSink.apply(screenRenderType(lc.lineDepthTest, lc.lineBlend)) : startDrawing(!quads, lc.lineDepthTest, lc.lineBlend);
			Pair<Color, Color> cols = lc.color.getColors(false, cfg.crystalHelperLineColor);
			Vertexer.beginLayer(lc.lineBlend);
			try {
				if (quads) {
					if (looked != null && behindLookedAtFace(looked, pose, bounds)) lookedAt = null;
					Vertexer.vertexScreenBox(layerPose, buffer, lc.shapeStyle == ShapeStyle.CLASSIC_BOX ? List.<Vec3[]>of(Vertexer.corners(bounds)) : shape.toAabbs().stream().map(Vertexer::corners).toList(), lc.screenOutline, cols, lc.color.alpha, lc.lineWidth, lc.cutFromCenter, lc.cutFromCorner, lc.outerThicknessMult, lc.innerThicknessMult);
				} else if (lc.shapeStyle == ShapeStyle.CLASSIC_BOX) {
					float[] alphas = new float[6];
					Arrays.fill(alphas, lc.color.alpha);
					Vertexer.vertexBoxLines(layerPose, buffer, bounds, cols, alphas, lc.lineWidth, lc.cutFromCenter, lc.cutFromCorner, lc.outerThicknessMult, lc.innerThicknessMult);
				} else {
					int c1 = cols.first().getRGB() & 0xFFFFFF;
					int c2 = cols.second().getRGB() & 0xFFFFFF;
					shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> renderLine(new Line(new Vec3(x1, y1, z1), new Vec3(x2, y2, z2)), layerPose, buffer, c1, c2, min, normalised, lc.color.alpha, lc.lineWidth, lc.cutFromCenter, lc.cutFromCorner, lc));
				}
			} finally {
				Vertexer.endLayer();
			}
			finishDraw(!quads, lc.lineDepthTest, lc.lineBlend);
		}
		lookedAt = looked;
	}
	//?}

	private static int tickedFrame = -1;
	private static HitResult tickedHit;

	private static HitResult tick() {
		if (tickedFrame != CustomBlockHighlight.frame) {
			tickedFrame = CustomBlockHighlight.frame;
			get().push(PROFILED);
			tickedHit = advance();
			get().pop();
		}
		return tickedHit;
	}

	private static HitResult advance() {
		//? if >1.8.9 {
		if (mc.player == null || mc.player.gameMode() == null) return null;
		//?} else
		//if (mc.player == null || mc.interactionManager == null) return null;
		if (!getActiveInstance().enableModRendering || mc.gui.hud.isHidden() && !getActiveInstance().showWhenNoHud) {
			scaleProg = lineProg = cutProg = 0;
			Arrays.fill(sideFades, 0);
			for (LineState state : lineStates) state.clearFades();
			return null;
		}
		get().push("target");
		Target target = Target.current();
		get().pop();
		get().push("update");
		boolean visible = false;
		if (target != null) {
			boolean ignored = !mayHighlight(target) || target.ignored();
			boolean wasVisible = isAnythingVisible();
			if (!ignored) easeBoxAndEdges(target, wasVisible);
			else updateLinesCommon();
			visible = updateProgresses(target, ignored, wasVisible);
		}
		get().pop();
		return visible ? target.hit() : null;
	}

	private static boolean mayHighlight(Target target) {
		//? if >1.8.9 {
		if (!mc.player.gameMode().isBlockPlacingRestricted() || getActiveInstance().showWhenNoInteraction) return true;
		if (target.hit() instanceof EntityHitResult) return mc.player.gameMode() != GameType.SPECTATOR;
		if (target.pos() == null || target.hit().getType() == HitResult.Type.MISS) return false;
		BlockPos pos = target.pos();
		if (mc.player.gameMode() == GameType.SPECTATOR) {
			//? if >=26.3 {
			return target.state().showAsInteractableInSpectatorMode(mc.level, pos, (BlockHitResult) target.hit());
			//?} else
			//return target.state().getMenuProvider(mc.level, pos) != null;
		}
		ItemStack stack = mc.player.getMainHandItem();
		BlockInWorld block = new BlockInWorld(mc.level, pos, false);
		return !stack.isEmpty() && (stack.canBreakBlockInAdventureMode(block) || stack.canPlaceOnBlockInAdventureMode(block));
		//?} else {
		/*if (mc.player.abilities.canModifyWorld || getActiveInstance().showWhenNoInteraction) return true;
		if (target.hit() instanceof EntityHitResult) return mc.interactionManager.getGameMode() != WorldSettings.GameMode.SPECTATOR;
		if (target.pos() == null || target.hit().getType() == HitResult.Type.MISS) return false;
		Block block = target.state().getBlock();
		if (mc.interactionManager.getGameMode() == WorldSettings.GameMode.SPECTATOR) {
			return block.hasBlockEntity() && mc.level.getBlockEntity(target.pos()) instanceof Inventory;
		}
		ItemStack stack = mc.player.getItemInHand();
		return stack != null && (stack.hasMineBlockOverride(block) || stack.hasPlaceOnBlockOverride(block));
		*///?}
	}

	private static boolean isAnythingVisible() {
		BlockHighlightConfig cfg = getActiveInstance();
		if (cfg.fillEnabled) {
			for (float f : sideFades) if (Math.round(f) >= 1) return true;
		}
		for (int i = 0; i < lineStates.size(); i++) {
			LineConfig lc = cfg.getLineConfig(i);
			if (!isLayerDrawn(cfg, lc)) continue;
			LineState state = lineStates.get(i);
			for (float f : state.lineFades) if (Math.round(f) >= 1) return true;
		}
		return false;
	}

	static boolean isLayerDrawn(BlockHighlightConfig cfg, LineConfig lc) {
		return cfg.primary.enabled && lc.enabled;
	}

	private static Object easedEntity;
	private static AABB easedEntityBox;

	private static void easeBoxAndEdges(Target target, boolean visible) {
		HitResult evilHitResult = target.hit();
		BlockHighlightConfig cfg = getActiveInstance();
		if (evilHitResult.getType() == HitResult.Type.MISS && !cfg.updateWhenUnfocused) {
			easedEntity = null;
			updateLinesCommon();
			return;
		}
		lookedAt = evilHitResult instanceof BlockHitResult block ? block.getDirection() : null;
		AABB targetBox = target.bounds();
		Object entity = evilHitResult instanceof EntityHitResult hit ? hit.getEntity() : null;
		if (entity != null && entity == easedEntity) {
			easeBox = easeBox.move(targetBox.minX - easedEntityBox.minX, targetBox.minY - easedEntityBox.minY, targetBox.minZ - easedEntityBox.minZ);
		}
		easedEntity = entity;
		easedEntityBox = targetBox;
		snapping = cfg.doEasing && cfg.snapWhenFadedOut && evilHitResult.getType() != HitResult.Type.MISS && !visible;
		if (snapping) {
			easeBox = targetBox;
			for (ShapeLayer layer : List.of(collisionShape, modelShape)) {
				layer.leavingLines.clear();
				layer.leavingQuads.clear();
			}
		}
		if (cfg.doEasing) {
			easeBox = lerpBox(easeBox, targetBox, easeFactor(cfg.easeSpeed));
		} else {
			easeBox = targetBox;
		}
		boolean collisionLines = false, modelLines = false;
		boolean collisionFaces = cfg.fillEnabled && cfg.fillShape == ShapeStyle.COLLISION_SHAPE, modelFaces = cfg.fillEnabled && cfg.fillShape == ShapeStyle.MODEL_SHAPE;
		for (LineConfig lc : cfg.lineConfigs()) {
			if (!isLayerDrawn(cfg, lc)) continue;
			boolean faces = unified(cfg) || lc.screenOutline.enabled;
			if (lc.shapeStyle == ShapeStyle.COLLISION_SHAPE) {
				if (faces) collisionFaces = true;
				else collisionLines = true;
			}
			if (lc.shapeStyle == ShapeStyle.MODEL_SHAPE) {
				if (faces) modelFaces = true;
				else modelLines = true;
			}
		}
		if (isEmptyTarget(target)) {
			updateLinesCommon();
			return;
		}
		VoxelShape local = target.local();
		if (collisionLines || collisionFaces) collisionShape.update(local, null, () -> ShapeGeometry.ofShape(local), collisionLines, collisionFaces);
		if (modelLines || modelFaces) modelShape.update(local, ModelGeometry.modelKey(target), () -> ModelGeometry.buildModelGeometry(target), modelLines, modelFaces);
		updateLinesCommon();
	}

	private static boolean unified(BlockHighlightConfig cfg) {
		return cfg.unifiedQuads && cfg.doEasing;
	}

	private static AABB lerpBox(AABB from, AABB to, double k) {
		Vec3 min = Line.ease(from.getMinPosition(), to.getMinPosition(), k), max = Line.ease(from.getMaxPosition(), to.getMaxPosition(), k);
		return min.equals(to.getMinPosition()) && max.equals(to.getMaxPosition()) ? to : new AABB(min, max);
	}

	private static void draw(PoseStack stack, boolean isCrystalObstructed) {
		if (getActiveInstance().fillEnabled) {
			get().push("fill");
			drawFill(stack, isCrystalObstructed);
			get().pop();
		}
		if (getActiveInstance().primary.enabled) {
			get().push("outlines");
			drawOutlines(stack, isCrystalObstructed);
			get().pop();
		}
	}

	private static void drawFill(PoseStack stack, boolean isCrystalObstructed) {
		BlockHighlightConfig cfg = getActiveInstance();
		Pair<Color, Color> cols = cfg.fillCol.getColors(isCrystalObstructed, cfg.crystalHelperFillColor);
		Vertexer.blend = cfg.fillBlend;
		if (cfg.fillScreenOutline.enabled) {
			drawScreenFill(stack, cols);
		} else {
			switch (cfg.fillShape) {
				case CLASSIC_BOX -> drawBoxFill(stack, easeBox, cols, sideFades);
				case COLLISION_SHAPE, MODEL_SHAPE -> drawShapeFill(stack, shapeLayer(cfg.fillShape), cols);
			}
		}
		Vertexer.blend = BlendMode.NORMAL;
	}

	//? if >=26.2
	private static final FloatArrayList screenDepth = new FloatArrayList();

	private static void drawOutlines(PoseStack stack, boolean isCrystalObstructed) {
		DepthTestMode prevMode = null;
		BlendMode prevBlend = null;
		//? if >1.8.9 && <1.21.11
		//float prevWidth = 0;
		VertexConsumer buffer = null;
		for (int layer = 2; layer >= 0; layer--) {
			LineConfig lineConfig = getActiveInstance().getLineConfig(layer);
			if (!lineConfig.enabled) continue;
			if (lineConfig.screenOutline.enabled) {
				if (prevMode != null) {
					finishDraw(true, prevMode, prevBlend);
					prevMode = null;
				}
				//? if >=26.2 {
				VertexConsumer quads = featureSink != null ? featureSink.apply(outlineRenderType(lineConfig.lineDepthTest, false, lineConfig.lineBlend, layer)) : startDrawing(false, lineConfig.lineDepthTest, lineConfig.lineBlend);
				if (featureSink != null && !IrisCompat.isShaderPackInUse() && lineConfig.lineDepthTest != DepthTestMode.HIDDEN_ONLY) Vertexer.depthCopy = screenDepth;
				//?} else
				//VertexConsumer quads = startDrawing(false, lineConfig.lineDepthTest, lineConfig.lineBlend);
				drawLineLayer(stack, lineConfig, isCrystalObstructed, layer, quads);
				//? if >=26.2
				Vertexer.depthCopy = null;
				finishDraw(false, lineConfig.lineDepthTest, lineConfig.lineBlend);
				continue;
			}
			if (prevMode != lineConfig.lineDepthTest || prevBlend != lineConfig.lineBlend
						//? if >1.8.9 && <1.21.11
						//|| prevWidth != lineConfig.lineWidth
			) {
				if (prevMode != null) {
					finishDraw(true, prevMode, prevBlend);
				}
				//? if >=26.2 {
				buffer = featureSink != null ? featureSink.apply(outlineRenderType(lineConfig.lineDepthTest, true, lineConfig.lineBlend, layer)) : startDrawing(true, lineConfig.lineDepthTest, lineConfig.lineBlend);
				//?} else
				//buffer = startDrawing(true, lineConfig.lineDepthTest, lineConfig.lineBlend);
				prevMode = lineConfig.lineDepthTest;
				prevBlend = lineConfig.lineBlend;
				//? if >1.8.9 && <1.21.11
				//prevWidth = lineConfig.lineWidth;
			}
			drawLineLayer(stack, lineConfig, isCrystalObstructed, layer, buffer);
		}
		if (prevMode != null) finishDraw(true, prevMode, prevBlend);
		//? if >=26.2 {
		if (screenDepth.isEmpty()) return;
		VertexConsumer depth = featureSink.apply(SCREEN_DEPTH);
		for (int i = 0; i < screenDepth.size(); i += 4) depth.addVertex(screenDepth.getFloat(i), screenDepth.getFloat(i + 1), screenDepth.getFloat(i + 2)).setColor(255, 255, 255, (int) screenDepth.getFloat(i + 3));
		screenDepth.clear();
		//?}
	}

	private static boolean updateProgresses(Target target, boolean ignored, boolean wasVisible) {
		HitResult evilHitResult = target.hit();
		BlockHighlightConfig cfg = getActiveInstance();
		double cutEase = cfg.animateLineCuts && wasVisible ? easeFactor(cfg.lineCutSpeed) : 1;
		for (int i = 0; i < lineStates.size(); i++) lineStates.get(i).easeCuts(cfg.getLineConfig(i).cutFromCenter, cfg.getLineConfig(i).cutFromCorner, cutEase);
		boolean miss = evilHitResult.getType() == HitResult.Type.MISS;
		if (ignored) {
			miss = true;
			exitFades();
		} else if (evilHitResult instanceof EntityHitResult) {
			updateFades(target);
			updateRotation(target);
		} else if (evilHitResult instanceof BlockHitResult) {
			if (isEmptyTarget(target)) {
				exitFades();
				easeRotation();
			} else {
				updateFades(target);
				updateRotation(target);
			}
		}
		boolean visible = isAnythingVisible();
		scaleProg = LineState.progress(scaleProg, miss, cfg.scale, cfg.scaleSpeed, cfg.scaleOut, cfg.scaleOutSpeed, visible);
		lineProg = LineState.progress(lineProg, miss, cfg.animateLineThickness, cfg.lineThicknessAnimationSpeed, cfg.lineThicknessOut, cfg.lineThicknessOutSpeed, visible);
		cutProg = LineState.progress(cutProg, miss, cfg.animateLineCuts, cfg.lineCutSpeed, cfg.lineCutOut, cfg.lineCutOutSpeed, visible);
		return visible;
	}

	private static void updateFades(Target target) {
		BlockHighlightConfig cfg = getActiveInstance();
		concealed = -1;
		for (int i = 0; i < lineStates.size(); i++) {
			LineConfig lc = cfg.getLineConfig(i);
			LineState lineState = lineStates.get(i);
			if (!isLayerDrawn(cfg, lc)) {
				lineState.clearFades();
				continue;
			}
			fadeSides(lineState.lineFades, sides(lc.outlineType, target), lc.color.alpha);
			lineState.edgeAlpha = fadeIn(lineState.edgeAlpha, lc.color.alpha);
		}
		if (cfg.fillEnabled) fadeSides(sideFades, sides(cfg.fillType, target), cfg.fillCol.alpha);
		else Arrays.fill(sideFades, 0);
	}

	private static void fadeSides(float[] fades, int sides, float target) {
		for (int d = 0; d < 6; d++) fades[d] = (sides & 1 << d) != 0 ? fadeIn(fades[d], target) : fadeOut(fades[d]);
	}

	private static void updateRotation(Target target) {
		BlockHighlightConfig cfg = getActiveInstance();
		if (!cfg.rotations) return;
		if (target.hit() instanceof BlockHitResult block) {
			Direction d = block.getDirection();
			if (isCubeLike(target.raw())) {
				roll.towards(d);
			} else {
				roll.reset(d);
			}
		} else {
			roll.reset(null);
		}
		easeRotation();
	}

	private static void easeRotation() {
		BlockHighlightConfig cfg = getActiveInstance();
		if (!cfg.rotations) return;
		rotation.nlerp(roll.target, (float) easeFactor(cfg.rotationSpeed));
		if (Math.abs(rotation.dot(roll.target)) > 1 - 1.0E-5F) rotation.set(roll.target);
	}

	private static boolean isEmptyTarget(Target target) {
		return target.hit() instanceof BlockHitResult && (target.hit().getType() == HitResult.Type.MISS || mc.level.isEmptyBlock(target.pos()));
	}

	private static void exitFades() {
		BlockHighlightConfig cfg = getActiveInstance();
		if (cfg.fillEnabled) {
			for (int d = 0; d < sideFades.length; d++) sideFades[d] = fadeOut(sideFades[d]);
		} else Arrays.fill(sideFades, 0);
		for (int i = 0; i < lineStates.size(); i++) {
			LineConfig lc = cfg.getLineConfig(i);
			LineState lineState = lineStates.get(i);
			if (!isLayerDrawn(cfg, lc)) {
				lineState.clearFades();
				continue;
			}
			lineState.fadeOutSides();
			lineState.edgeAlpha = fadeOut(lineState.edgeAlpha);
		}
	}
}
