package tektonikal.customblockhighlight;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
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
import org.jspecify.annotations.NonNull;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.Line;
import tektonikal.customblockhighlight.util.OutlineType;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_QUADS;
import static net.minecraft.client.renderer.RenderPipelines.LINES;
import static tektonikal.customblockhighlight.Blockhighlight.ease;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.getActiveInstance;

//POST V2.8
//TODO: fix thick lines not fully joining at corners
//TODO: animations / visuals for block breaking progress?
//TODO: edge line animation modes
//TODO: option for easebox to teleport to new position if it's fully faded out
//TODO: fancier "looked at" mode
//TODO: fancy rotations for the box. i'm vagueposting
//TODO: in edges mode, switching between a normal block and a connected one causes the center of the outline to prioritize the position closest to 0,0?
//not super important, but something i'd want to keep in check. can be reproduced with normal block under a bed facing north
//TODO: separate options from the extras tab to be more specific
//TODO: more options for the extra line layers
//TODO: ensure iris compat
//there is no real solution to the Z-fighting issue without disabling depth test.
//even if the lines are drawn correctly spaced out, the thicker line might be rotated differently, causing it to fully appear in front or z-fight. whateverrrrrr man
//maybe it would be best into looking into creating a pipeline to draw lines that aren't in screenspace. idk
//TODO: hotkey toggle?
//TODO: inverse box animation
//TODO: flat outline
//TODO: split out modes
//TODO: line cut animations
//TODO: ignore non-full blocks when doing stuff like leaves
//TODO: change per-side alpha array from storing alpha to general animation state

public class Renderer {
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Camera camera = mc.gameRenderer.mainCamera();

	public static final float[] sideFades = new float[6];
	public static final float[] lineFades = new float[6];

	public static final RenderPipeline LINE_NO_DEPTH = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", "pipeline/evil-lines"))
					.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
					.withCull(false)
					.build()
	);
	public static final RenderPipeline FILL_NO_DEPTH = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", "pipeline/evil-fill"))
					.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
					.withCull(false)
					.build()
	);
	public static final RenderPipeline LINES_CONCEALED_ONLY = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", "pipeline/eviler-lines"))
					.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true))
					.withCull(false)
					.build()
	);
	public static final RenderPipeline FILL_CONCEALED_ONLY = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", "pipeline/eviler-fill"))
					.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true))
					.withCull(false)
					.build()
	);
	public static final RenderType linesNoDepth = RenderType.create("lines_no_depth",
			RenderSetup.builder(Renderer.LINE_NO_DEPTH)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
					.createRenderSetup());
	public static final RenderType linesConcealed = RenderType.create("lines_concealed",
			RenderSetup.builder(Renderer.LINES_CONCEALED_ONLY)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
					.createRenderSetup());
	public static final StagedVertexBuffer stagedFaceBuffer = new StagedVertexBuffer(() -> " CBH sides", RenderType.SMALL_BUFFER_SIZE);
	public static final StagedVertexBuffer stagedOutlineBuffer = new StagedVertexBuffer(() -> " CBH outline", RenderType.SMALL_BUFFER_SIZE);

	public static AABB easeBox = new AABB(0, 0, 0, 0, 0, 0);
	public static AABB targetBox = new AABB(0, 0, 0, 0, 0, 0);

	public static List<Line> lines = new ArrayList<>();
	public static List<Line> toRemove = new ArrayList<>();

	public static VoxelShape shape = Shapes.block();
	public static float edgeAlpha = 0;
	public static float scaleProg = 0;
	public static float lineProg = 0;
	public static Quaternionf rotation = new Quaternionf();
	public static HitResult evilHitResult;

	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();

	public static StagedVertexBuffer.Draw startDrawing(boolean lines) {
		if (lines) {
			return stagedOutlineBuffer.appendDraw(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, PrimitiveTopology.LINES);
		} else {
			return stagedFaceBuffer.appendDraw(DefaultVertexFormat.POSITION_COLOR, PrimitiveTopology.QUADS, RenderSystem.getProjectionType().vertexSorting());
		}
	}

	private static void finishDraw(boolean lines, StagedVertexBuffer.Draw draw, int layer) {
		StagedVertexBuffer.ExecuteInfo info;
		if (lines) {
			stagedOutlineBuffer.upload();
			info = stagedOutlineBuffer.getExecuteInfo(draw);
		} else {
			stagedFaceBuffer.upload();
			info = stagedFaceBuffer.getExecuteInfo(draw);
		}
		if (info == null) return;

		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f());
		RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
		GpuTextureView colorTexture = mainTarget.getColorTextureView();
		assert colorTexture != null;
		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "CBH pass", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
			if (lines) {
				switch (layer) {
					case 0 -> renderPass.setPipeline(getPipeline(getActiveInstance().lineDepthTest, true));
					case 1 -> renderPass.setPipeline(getPipeline(getActiveInstance().slineDepthTest, true));
					case 2 -> renderPass.setPipeline(getPipeline(getActiveInstance().tlineDepthTest, true));
				}
			} else {
				renderPass.setPipeline(getPipeline(getActiveInstance().fillDepthTest, false));
			}

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
			renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
			renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
		}

		if (lines) {
			stagedOutlineBuffer.endFrame();
		} else {
			stagedFaceBuffer.endFrame();
		}
	}

	public static RenderPipeline getPipeline(DepthTestMode mode, boolean lines) {
		return switch (mode) {
			case ALWAYS_PASS -> lines ? LINE_NO_DEPTH : FILL_NO_DEPTH;
			case HIDDEN_ONLY -> lines ? LINES_CONCEALED_ONLY : FILL_CONCEALED_ONLY;
			case NORMAL -> lines ? LINES : DEBUG_QUADS;
		};
	}

	public static void drawBoxFill(PoseStack stack, AABB box, Color cols, Color col2, float[] alpha) {
		doEvilMatrixPreparations(stack, box, false);
		StagedVertexBuffer.Draw draw = startDrawing(false);
		VertexConsumer buffer = stagedFaceBuffer.getVertexBuilder(draw);
//		if (evilHitResult instanceof BlockHitResult bhr) {
//			List<BlockStateModelPart> s = new ArrayList();
//			mc.getModelManager().getBlockStateModelSet().get(mc.level.getBlockState(bhr.getBlockPos())).collectParts(RandomSource.create(), s);
//			s.forEach(blockStateModelPart -> {
//				((SimpleModelWrapper) blockStateModelPart).quads().getAll().forEach(quad -> {
//					Vector3fc p0 = new Vector3f(quad.position0());
//					Vector3fc p1 = new Vector3f(quad.position1());
//					Vector3fc p2 = new Vector3f(quad.position2());
//					Vector3fc p3 = new Vector3f(quad.position3());
//					Vertexer.vertexQuad(stack, buffer, p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z(), p2.x(), p2.y(), p2.z(), p3.x(), p3.y(), p3.z(), Color.BLACK, Color.WHITE, Color.RED, Color.BLUE, 255);
//				});
//			});
//		}
		Vertexer.vertexBoxQuads(stack.last(), buffer, moveToZero(box), cols, col2, alpha);
		finishDraw(false, draw, 0);
		stack.popPose();
	}

	private static void doEvilMatrixPreparations(PoseStack stack, AABB box, boolean horribleWorkaroundForEdges) {
		stack.pushPose();
		stack.translate(box.minX - camera.position().x, box.minY - camera.position().y, box.minZ - camera.position().z);
		Vec3 vec = moveToZero(box).getCenter();
		stack.translate(vec);
		if (getActiveInstance().rotations) {
			stack.rotateAround(rotation, 0, 0, 0);
		}
		stack.scale(scaleProg, scaleProg, scaleProg);
		if (horribleWorkaroundForEdges) {
			float yeah = getActiveInstance().lineExpand * 2 + 1;
			stack.scale(yeah, yeah, yeah);
		}
		stack.translate(vec.reverse());
	}

	public static void drawBoxOutline(PoseStack stack, AABB box, Color color, Color col2, float[] alpha, float lineWidth, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult, int layer) {
		doEvilMatrixPreparations(stack, box, false);
		StagedVertexBuffer.Draw draw = startDrawing(true);
		VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);
		Vertexer.vertexBoxLines(stack.last(), buffer, moveToZero(box), color, col2, alpha, lineWidth * lineProg, cutFromCenter, cutFromCorner, outerMult, innerMult);
//		stack.pushPose();
//		stack.translate(0.5F, 0.5F, 0.5F);
//		float scale = 1 / (float) Math.sqrt(3);
//		stack.scale(scale, scale, scale);
//		stack.translate(-0.5F, -0.5F, -0.5F);
//		stack.rotateAround(new Quaternionf().rotateXYZ(35.26F, 45, 0), 0.5F, 0.5F, 0.5F);
//		Vertexer.vertexBoxLines(stack, buffer, moveToZero(box), color, col2, alpha, layer);
//		stack.popPose();
//		stack.pushPose();
//		stack.translate(0.5F, 0.5F, 0.5F);
//		scale = (float) (1 / (float) Math.sqrt(3) / Math.sqrt(3));
//		stack.scale(scale, scale, scale);
//		stack.translate(-0.5F, -0.5F, -0.5F);
//		stack.rotateAround(new Quaternionf().rotateXYZ(35.26F * 2, 90, 0), 0.5F, 0.5F, 0.5F);
//		Vertexer.vertexBoxLines(stack, buffer, moveToZero(box), color, col2, alpha, layer);
//		stack.popPose();
		finishDraw(true, draw, layer);
		stack.popPose();
	}

	public static void drawEdgeOutline(PoseStack matrices, VoxelShape shape, List<Line> sortedLines, Color c1, Color c2, float alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult, int layer) {
		doEvilMatrixPreparations(matrices, shape.bounds(), true);
		StagedVertexBuffer.Draw draw = startDrawing(true);
		VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);

		shape = moveToZero(shape);
		double normalised = shape.bounds().getMinPosition().distanceTo(shape.bounds().getMaxPosition());
		for (Line finalLine : sortedLines) {
			finalLine.render(matrices, buffer, getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(finalLine.minPos.x, finalLine.minPos.y, finalLine.minPos.z)) / normalised)), getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(finalLine.maxPos.x, finalLine.maxPos.y, finalLine.maxPos.z)) / normalised)), Math.round(alpha), width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		}
		for (Line line : toRemove) {
			line.render(matrices, buffer, getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(line.minPos.x, line.minPos.y, line.minPos.z)) / normalised)), getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / normalised)), Math.round(alpha), width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		}
		finishDraw(true, draw, layer);
		matrices.popPose();
	}

	public static void updateLines(VoxelShape shape, List<Line> newLines, List<Line> sortedLines) {
		moveToZero(shape).forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> newLines.add(new Line(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ))));
		if (lines.isEmpty() || !getActiveInstance().doEasing) {
			lines = newLines;
		}
		while (lines.size() < newLines.size()) {
//            if (!toRemove.isEmpty()) {
//                lines.add(toRemove.getFirst());
//                toRemove.removeFirst();
//            } else {
			lines.add(new Line(moveToZero(shape).bounds().getCenter(), moveToZero(shape).bounds().getCenter()));
//            }
		}
		while (lines.size() > newLines.size()) {
			toRemove.add(lines.getLast());
			lines.removeLast();
		}
		for (int i = 0; i < lines.size(); i++) {
			lines.get(i).moveTo(newLines.get(i).minPos, newLines.get(i).maxPos);
		}
		for (Line finalLine : sortedLines) {
			finalLine.update(true);
		}
		toRemove.removeIf(line -> line.alphaMultiplier < 1 / 255f);
		for (Line line : toRemove) {
			line.update(false);
		}
	}


	public static AABB moveToZero(AABB box) {
		return box.move(box.getMinPosition().reverse());
	}

	public static VoxelShape moveToZero(VoxelShape shape) {
		Vec3 minVec = shape.bounds().getMinPosition();
		return shape.move(minVec.reverse());
	}

	//TODO: allow combining / excluding side sets?
	//TODO: make this adjust based on rotation
	private static EnumSet<Direction> getSides(OutlineType type, BlockPos pos) {
		return switch (type) {
			case LOOKAT ->
					(evilHitResult instanceof BlockHitResult block) ? EnumSet.of(block.getDirection()) : EnumSet.allOf(Direction.class);
			case AIR_EXPOSED -> EnumSet.complementOf(getConcealedFaces(pos));
			case CONCEALED -> getConcealedFaces(pos);
			default -> EnumSet.allOf(Direction.class);
		};
	}

	public static Color getRainbowCol(float delay) {
		double rainbowState = Math.ceil((System.currentTimeMillis() + (int) (delay))) * getActiveInstance().rainbowSpeed / 50;
		rainbowState %= 360;
		return Color.getHSBColor((float) (rainbowState / 360.0f), getActiveInstance().saturation, getActiveInstance().brightness);
	}

	public static boolean isBlockOccupied(BlockPos pos) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		if (mc.level.getBlockState(pos).hasProperty(BlockStateProperties.WATERLOGGED) && !mc.level.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED) && !mc.level.getFluidState(pos).isEmpty()) {
			//ignore liquids
			return false;
		}
		return !mc.level.isEmptyBlock(pos);
	}

	public static EnumSet<Direction> getConcealedFaces(BlockPos pos) {
        /*
        I don't know if I should keep the original behavior for this
        As of now, this method means that even when rendering the box for a block with multiple parts,
        it will still cull faces relative to the selected block, and not the entire rendered selection
         */
		EnumSet<Direction> set = EnumSet.allOf(Direction.class);
		if (!isBlockOccupied(pos.above())) set.remove(Direction.UP);
		if (!isBlockOccupied(pos.below())) set.remove(Direction.DOWN);
		if (!isBlockOccupied(pos.north())) set.remove(Direction.NORTH);
		if (!isBlockOccupied(pos.east())) set.remove(Direction.EAST);
		if (!isBlockOccupied(pos.south())) set.remove(Direction.SOUTH);
		if (!isBlockOccupied(pos.west())) set.remove(Direction.WEST);
		return set;
	}

	public static Color getLerpedColor(Color c1, Color c2, float percent) {
		return new Color(Math.clamp(Mth.lerpInt(percent, c1.getRed(), c2.getRed()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getGreen(), c2.getGreen()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getBlue(), c2.getBlue()), 0, 255));
	}

	public static void mainLoop(LevelRenderContext c) {
		Profiler.get().push("Custom block outline pre");
		evilHitResult = mc.hitResult;
		//this is just for the warnings to go away
		if (evilHitResult == null || mc.level == null || mc.getCameraEntity() == null || mc.player == null) return;
		if (getActiveInstance().allowLiquids && (mc.player.getMainHandItem().is(Items.BUCKET) || mc.player.getOffhandItem().is(Items.BUCKET))) {
			HitResult yeah = pick(mc.getCameraEntity(), mc.player.blockInteractionRange(), mc.getDeltaTracker().getRealtimeDeltaTicks(), true);
			if (yeah instanceof BlockHitResult hit) {
				if (mc.level.getFluidState(hit.getBlockPos()).isSource()) {
					evilHitResult = yeah;
				}
			}
		}
			if (evilHitResult instanceof BlockHitResult block) {
				BlockState state = mc.level.getBlockState(block.getBlockPos());
				VoxelShape shapeLocal = state.getShape(mc.level, block.getBlockPos());
				if (shapeLocal.isEmpty()) {
					targetBox = new AABB(block.getBlockPos());
				} else {
					targetBox = shapeLocal.bounds().move(block.getBlockPos());
				}
				if (isBlockOccupied(block.getBlockPos())) {
					shape = mc.level.getBlockState(block.getBlockPos()).getShape(mc.level, block.getBlockPos(), CollisionContext.of(mc.getCameraEntity()));
				}
			} else if (evilHitResult instanceof EntityHitResult entityHitResult && getActiveInstance().allowEntities) {
				Entity entity = entityHitResult.getEntity();
				//so, so sloppy. might also have the worst workaround of the century for hanging stuff
				float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
				targetBox = moveToZero(entity.getBoundingBox()).move(entity.getPosition(delta).subtract(moveToZero(entity.getBoundingBox()).getCenter()).add(0, entity instanceof HangingEntity ? 0 : moveToZero(entity.getBoundingBox()).maxY / 2F, 0));
				shape = Shapes.create(entity.getBoundingBox());
			}

		//get connected blocks
		if (getActiveInstance().connectedBlocks && evilHitResult instanceof BlockHitResult block) {
			BlockState state = mc.level.getBlockState(block.getBlockPos());
			Direction connected = joinConnected(state, block.getBlockPos());
			if (connected != null) {
				shape = Shapes.join(shape, mc.level.getBlockState(block.getBlockPos().relative(connected)).getShape(mc.level, block.getBlockPos().relative(connected), CollisionContext.of(mc.getCameraEntity())).move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR);
			}
		}
		//calculate where to render the block
		if (getActiveInstance().doEasing) {
			if (getActiveInstance().updateWhenUnfocused || !isMiss()) {
				easeBox = new AABB(ease(easeBox.minX, targetBox.minX, getActiveInstance().easeSpeed), ease(easeBox.minY, targetBox.minY, getActiveInstance().easeSpeed), ease(easeBox.minZ, targetBox.minZ, getActiveInstance().easeSpeed), ease(easeBox.maxX, targetBox.maxX, getActiveInstance().easeSpeed), ease(easeBox.maxY, targetBox.maxY, getActiveInstance().easeSpeed), ease(easeBox.maxZ, targetBox.maxZ, getActiveInstance().easeSpeed));
			}
		} else {
			easeBox = targetBox;
		}
		Profiler.get().popPush("Custom block outline render");
		renderOutline(c.poseStack(), isCrystalObstructed(), isMiss(), c.submitNodeCollector());
		Profiler.get().pop();
	}

	public static boolean isMiss() {
		return evilHitResult.getType() == HitResult.Type.MISS;
	}

	private static void renderOutline(PoseStack stack, boolean isCrystalObstructed, boolean shouldFade, SubmitNodeCollector submitNodeCollector) {
		//render the fill first, we don't want it drawn over the outline
		Profiler.get().push("updateProgresses");
		updateProgresses(shouldFade);
		if (edgeAlpha > 1) {
			if (getActiveInstance().fillEnabled) {
				Profiler.get().popPush("drawFill");
				drawFill(stack, isCrystalObstructed);
			}
			//now the outline itself
			if (getActiveInstance().outlineEnabled) {
				Profiler.get().popPush("drawOutline");
				drawOutline(stack, isCrystalObstructed, submitNodeCollector);
			}
		}
		Profiler.get().pop();
	}

	private static boolean isCrystalObstructed() {
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
		Color finalFillCol = isCrystalObstructed ? getActiveInstance().crystalHelperFillColor : getActiveInstance().fillRainbow ? getRainbowCol(0) : getActiveInstance().fillCol;
		Color finalFillCol2 = isCrystalObstructed ? getActiveInstance().crystalHelperFillColor : getActiveInstance().fillRainbow ? getRainbowCol(getActiveInstance().delay) : getActiveInstance().fillCol2;
		boolean b = getActiveInstance().fillDepthTest != DepthTestMode.ALWAYS_PASS && getActiveInstance().fillExpand == 0;
		//TODO: instead of expanding by fixed amount, do it based on a percentage of the box size
		Renderer.drawBoxFill(stack, easeBox.inflate(getActiveInstance().fillExpand + (b ? 0.001 : 0)), finalFillCol, finalFillCol2, sideFades);
	}

	private static void drawOutline(PoseStack stack, boolean isCrystalObstructed, SubmitNodeCollector submitNodeCollector) {
		var profiler = Profiler.get();
		profiler.push("pre");
		if (mc.level == null) throw new IllegalStateException("level == null");
		//TODO: make check so that cut from corner and cut from center do not add up to higher than 0.95

		Pair<Color, Color> mainCols = getColors(isCrystalObstructed, getActiveInstance().outlineRainbow, getActiveInstance().delay, getActiveInstance().lineCol, getActiveInstance().lineCol2, getActiveInstance().crystalHelperLineColor);
		if (getActiveInstance().outlineType == OutlineType.EDGES) {
			if (!shape.isEmpty()) {
				List<Line> lines = getSortedLines();
				updateLines(shape, new ArrayList<>(), lines);
				VoxelShape moved = shape.move(easeBox.minX - shape.bounds().getMinPosition().x, easeBox.minY - shape.bounds().getMinPosition().y, easeBox.minZ - shape.bounds().getMinPosition().z);
				if (getActiveInstance().tertiary) {
					Pair<Color, Color> colors = getColors(isCrystalObstructed, getActiveInstance().toutlineRainbow, getActiveInstance().delay, getActiveInstance().tlineCol, getActiveInstance().tlineCol2, getActiveInstance().crystalHelperLineColor);
					Renderer.drawEdgeOutline(stack, moved, lines, colors.first, colors.second, edgeAlpha * getActiveInstance().tlineAlphaMultiplier, getActiveInstance().tlineWidth, getActiveInstance().tcutFromCenter, getActiveInstance().tcutFromCorner, getActiveInstance().touterThicknessMult, getActiveInstance().tinnerThicknessMult, 2);
				}
				if (getActiveInstance().secondary) {
					Pair<Color, Color> colors = getColors(isCrystalObstructed, getActiveInstance().soutlineRainbow, getActiveInstance().delay, getActiveInstance().slineCol, getActiveInstance().slineCol2, getActiveInstance().crystalHelperLineColor);
					Renderer.drawEdgeOutline(stack, moved, lines, colors.first, colors.second, edgeAlpha * getActiveInstance().slineAlphaMultiplier, getActiveInstance().slineWidth, getActiveInstance().scutFromCenter, getActiveInstance().scutFromCorner, getActiveInstance().souterThicknessMult, getActiveInstance().sinnerThicknessMult, 1);
				}
				Renderer.drawEdgeOutline(stack, moved, lines, mainCols.first, mainCols.second, edgeAlpha, getActiveInstance().lineWidth, getActiveInstance().cutFromCenter, getActiveInstance().cutFromCorner, getActiveInstance().outerThicknessMult, getActiveInstance().innerThicknessMult, 0);
			}
		} else {
			AABB inflated = easeBox.inflate(getActiveInstance().lineExpand);
			if (getActiveInstance().tertiary) {
				Pair<Color, Color> colors = getColors(isCrystalObstructed, getActiveInstance().toutlineRainbow, getActiveInstance().delay, getActiveInstance().tlineCol, getActiveInstance().tlineCol2, getActiveInstance().crystalHelperLineColor);
				float[] newFades = getNewFades(getActiveInstance().tlineAlphaMultiplier);
				Renderer.drawBoxOutline(stack, inflated, colors.first, colors.second, newFades, getActiveInstance().tlineWidth, getActiveInstance().tcutFromCenter, getActiveInstance().tcutFromCorner, getActiveInstance().touterThicknessMult, getActiveInstance().tinnerThicknessMult, 2);
			}
			if (getActiveInstance().secondary) {
				Pair<Color, Color> colors = getColors(isCrystalObstructed, getActiveInstance().soutlineRainbow, getActiveInstance().delay, getActiveInstance().slineCol, getActiveInstance().slineCol2, getActiveInstance().crystalHelperLineColor);
				float[] newFades = getNewFades(getActiveInstance().slineAlphaMultiplier);
				Renderer.drawBoxOutline(stack, inflated, colors.first, colors.second, newFades, getActiveInstance().slineWidth, getActiveInstance().scutFromCenter, getActiveInstance().scutFromCorner, getActiveInstance().souterThicknessMult, getActiveInstance().sinnerThicknessMult, 1);
			}
//			doEvilMatrixPreparations(stack, easeBox.inflate(getActiveInstance().lineExpand), false);
//			submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new CBHFeatureRenderer.Submit(CBHLineRenderInfo.of(shape, finalLineCol, finalLineCol2, lineFades, getActiveInstance()), stack.last()));
			Renderer.drawBoxOutline(stack, inflated, mainCols.first, mainCols.second, lineFades, getActiveInstance().lineWidth, getActiveInstance().cutFromCenter, getActiveInstance().cutFromCorner, getActiveInstance().outerThicknessMult, getActiveInstance().innerThicknessMult, 0);
		}
		profiler.pop();
	}

	public static float @NonNull [] getNewFades(float alphaMultiplier) {
		float[] newFades = new float[6];
		for (int i = 0; i < 6; i++) {
			newFades[i] = Mth.clamp(lineFades[i] * alphaMultiplier, 0, 255F);
		}
		return newFades;
	}

	public static Pair<Color, Color> getColors(boolean isCrystalObstructed, boolean rainbow, int delay, Color col, Color col2, Color crystalHelperCol) {
		return Pair.of(isCrystalObstructed ? crystalHelperCol : rainbow ? getRainbowCol(0) : col, isCrystalObstructed ? crystalHelperCol : rainbow ? getRainbowCol(delay) : col2);
	}

	private static List<Line> getSortedLines() {
		Vec3 minVec = shape.bounds().getMinPosition();
		return Renderer.lines.stream().sorted(Comparator.comparing(o -> ((Line) o).getDistanceToCamera(minVec)).reversed()).toList();
	}

	private static void updateProgresses(boolean shouldExit) {
		if (evilHitResult == null || mc.level == null) return;

		if (evilHitResult instanceof EntityHitResult) {
			if (getActiveInstance().allowEntities) {
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = getActiveInstance().fadeIn ? (float) ease(sideFades[dir.ordinal()], getActiveInstance().fillOpacity, getActiveInstance().fadeInSpeed) : getActiveInstance().fillOpacity;
					lineFades[dir.ordinal()] = getActiveInstance().fadeIn ? (float) ease(lineFades[dir.ordinal()], getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
				}
				edgeAlpha = getActiveInstance().fadeIn ? (float) ease(edgeAlpha, getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
			} else {
				shouldExit = true;
				exitFades();
			}
		} else if (evilHitResult instanceof BlockHitResult block) {
			if (mc.level.isEmptyBlock(block.getBlockPos()) || shouldExit) {
				exitFades();
			} else {
				edgeAlpha = getActiveInstance().fadeIn ? (float) ease(edgeAlpha, getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
				for (Direction dir : getSides(getActiveInstance().fillType, block.getBlockPos())) {
					sideFades[dir.ordinal()] = getActiveInstance().fadeIn ? (float) ease(sideFades[dir.ordinal()], getActiveInstance().fillOpacity, getActiveInstance().fadeInSpeed) : getActiveInstance().fillOpacity;
				}
				for (Direction dir : EnumSet.complementOf(getSides(getActiveInstance().fillType, block.getBlockPos()))) {
					sideFades[dir.ordinal()] = getActiveInstance().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
				}
				for (Direction dir : getSides(getActiveInstance().outlineType, block.getBlockPos())) {
					lineFades[dir.ordinal()] = getActiveInstance().fadeIn ? (float) ease(lineFades[dir.ordinal()], getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
				}
				for (Direction dir : EnumSet.complementOf(getSides(getActiveInstance().outlineType, block.getBlockPos()))) {
					lineFades[dir.ordinal()] = getActiveInstance().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
				}
			}
		}
		//I didn't add in/out because it would BREAKKK. TODO THIS
		scaleProg = getActiveInstance().scale ? (float) ease(scaleProg, shouldExit ? 0 : 1, getActiveInstance().scaleSpeed) : 1;
		lineProg = getActiveInstance().animateLineThickness ? (float) ease(lineProg, shouldExit ? 0 : 1, getActiveInstance().lineThicknessAnimationSpeed) : 1;
		if (evilHitResult instanceof BlockHitResult block) {
			Direction d = block.getDirection();
			Quaternionf target = d.getRotation();

			if (d != Direction.UP && d != Direction.DOWN) {
				lastHorizontalDirection = d;
			} else {
				float pitch = (d == Direction.UP) ? (float) (-Math.PI / 2.0) : (float) (Math.PI / 2.0);
				target = new Quaternionf(lastHorizontalDirection.getRotation()).rotateX(pitch);
			}

			List<AABB> aabbs = mc.level.getBlockState(block.getBlockPos()).getShape(mc.level, block.getBlockPos()).toAabbs();
			if (aabbs.size() == 1) {
				Vec3 vec = aabbs.getFirst().getMinPosition().subtract(aabbs.getFirst().getMaxPosition());
				if (!(vec.x == vec.y && vec.y == vec.z)) {
					target = new Quaternionf();
				}
			} else {
				target = new Quaternionf();
			}

			rotation.nlerp(target, 0.05F);
		} else {
			rotation.nlerp(new Quaternionf(), 0.05F);
		}
	}

	public static void exitFades() {
		for (Direction dir : Direction.values()) {
			sideFades[dir.ordinal()] = getActiveInstance().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
			lineFades[dir.ordinal()] = getActiveInstance().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
		}
		edgeAlpha = getActiveInstance().fadeOut ? (float) ease(edgeAlpha, 0, getActiveInstance().fadeOutSpeed) : 0;
	}

	private static Direction lastHorizontalDirection = Direction.NORTH;


	public static HitResult pick(Entity e, final double range, final float a, final boolean withLiquids) {
		Vec3 from = e.getEyePosition(a);
		Vec3 viewVector = e.getViewVector(a);
		Vec3 to = from.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
		assert mc.level != null;
		return mc.level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, withLiquids ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE, e));
	}

	private static Direction joinConnected(BlockState state, BlockPos pos) {
		if (mc.level == null) return null;

		BlockState connectedState;
		Direction dir;
		BlockPos connectedPos;
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
			DoubleBlockHalf halfState = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
			Direction d = halfState == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
			connectedPos = pos.relative(d);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock().getClass().equals(state.getBlock().getClass())) {
				if (connectedState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && connectedState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == halfState.getOtherHalf()) {
					targetBox = targetBox.minmax(connectedState.getShape(mc.level, connectedPos).bounds().move(connectedPos));
				}
			}
			return d;
		}
		if (state.getBlock() instanceof ChestBlock && !state.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)) {
			dir = ChestBlock.getConnectedDirection(state);
			connectedPos = ((BlockHitResult) evilHitResult).getBlockPos().relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof ChestBlock) {
				targetBox = targetBox.minmax(connectedState.getShape(mc.level, connectedPos).bounds().move(connectedPos));
			}
			return dir;
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
				targetBox = targetBox.minmax(connectedState.getShape(mc.level, connectedPos).bounds().move(connectedPos));
			}
			return dir;
		}
		if (state.getBlock() instanceof PistonHeadBlock) {
			dir = state.getValue(PistonBaseBlock.FACING);
			Direction oppDir = dir.getOpposite();
			connectedPos = pos.relative(oppDir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof PistonBaseBlock && connectedState.getValue(PistonBaseBlock.FACING) == dir) {
				targetBox = targetBox.minmax(connectedState.getShape(mc.level, connectedPos).bounds().move(connectedPos));
			}
			return oppDir;
		}
		if (state.getBlock() instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED)) {
			dir = state.getValue(PistonBaseBlock.FACING);
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof PistonHeadBlock && connectedState.getValue(PistonBaseBlock.FACING) == dir) {
				targetBox = targetBox.minmax(connectedState.getShape(mc.level, connectedPos).bounds().move(connectedPos));
			}
			return dir;
		}
		return null;
	}
}