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
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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
import tektonikal.customblockhighlight.util.FaceMode;
import tektonikal.customblockhighlight.util.Util;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_QUADS;
import static net.minecraft.client.renderer.RenderPipelines.LINES;
import static net.minecraft.util.Util.getMillis;
import static net.minecraft.util.profiling.Profiler.get;
import static tektonikal.customblockhighlight.Blockhighlight.ease;
import static tektonikal.customblockhighlight.Blockhighlight.easeF;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.getActiveInstance;

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
	public static List<Line> lines = new ArrayList<>();
	public static List<Line> toRemove = new ArrayList<>();

	public static float edgeAlpha = 0;
	public static float scaleProg = 0;
	public static float lineProg = 0;
	public static Quaternionf rotation = new Quaternionf();
	private static Direction lastHorizontalDirection = Direction.NORTH;

	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();

	// todo: consider `activeBuffer` which holds the  active buffer? see finishDraw too, duplicated logic where only difference is fields
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
		if(colorTexture == null) return;
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
		finishDraw(true, draw, layer);
		stack.popPose();
	}

	public static void drawEdgeOutline(PoseStack matrices, AABB bounds, List<Line> lines, Color c1, Color c2, float alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult, int layer) {
		doEvilMatrixPreparations(matrices, bounds, true);
		StagedVertexBuffer.Draw draw = startDrawing(true);
		VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);

		AABB zeroed = moveToZero(bounds);
		double normalised = zeroed.getMinPosition().distanceTo(zeroed.getMaxPosition());
		for (Line line : Util.concat(lines, toRemove)) {
			line.render(matrices, buffer,
					getLerpedColor(c1, c2, (float) (zeroed.getMinPosition().distanceTo(line.minPos) / normalised)),
					getLerpedColor(c1, c2, (float) (zeroed.getMinPosition().distanceTo(line.maxPos) / normalised)),
					Math.round(alpha), width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		}
		finishDraw(true, draw, layer);
		matrices.popPose();
	}

	public static void updateLines(VoxelShape shape) {
        //TODO: these don't sort by depth anymore
		List<Line> newLines = new ArrayList<>();
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
        lines.forEach(line -> {
            Line target = newLines.get(newLines.indexOf(line));
            line.moveTo(target.minPos, target.maxPos);
            line.update(true);
        });
        toRemove.forEach(line -> line.update(false));
		toRemove.removeIf(line -> line.alphaMultiplier < 1 / 255f);
	}

	public static AABB moveToZero(AABB box) {
		return box.move(box.getMinPosition().reverse());
	}

	public static VoxelShape moveToZero(VoxelShape shape) {
		return shape.move(shape.bounds().getMinPosition().reverse());
	}

	//TODO: allow combining / excluding side sets?
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

	public static Color getRainbowCol(float delay) {
		float rainbowState = Mth.ceil((getMillis() + delay)) * getActiveInstance().rainbowSpeed / 50;
		rainbowState %= 360;
		return Color.getHSBColor(rainbowState / 360, getActiveInstance().saturation, getActiveInstance().brightness);
	}

	public static boolean isBlockEmpty(BlockPos pos) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		//TODO: fix this
		if (mc.level.getBlockState(pos).hasProperty(BlockStateProperties.WATERLOGGED) && !mc.level.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED) && !mc.level.getFluidState(pos).isEmpty()) {
			//ignore liquids
			return true;
		}
		return mc.level.isEmptyBlock(pos);
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

	public static Color getLerpedColor(Color c1, Color c2, float percent) {
		return new Color(Math.clamp(Mth.lerpInt(percent, c1.getRed(), c2.getRed()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getGreen(), c2.getGreen()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getBlue(), c2.getBlue()), 0, 255));
	}

	public static void mainLoop(LevelRenderContext c) {
		get().push("Custom block outline pre");
		HitResult evilHitResult = getHitResult();

		easeBoxAndEdges(evilHitResult, getVoxelShape(evilHitResult));
		get().popPush("Custom block outline render");
		renderEverything(c, evilHitResult);
		get().pop();
	}

	public static HitResult getHitResult() {
		if (mc.level == null || mc.player == null || mc.getCameraEntity() == null) return null;
		if (getActiveInstance().allowLiquids && (mc.player.getMainHandItem().is(Items.BUCKET) || mc.player.getOffhandItem().is(Items.BUCKET))) {
			HitResult yeah = pick(mc.getCameraEntity(), mc.player.blockInteractionRange(), mc.getDeltaTracker().getRealtimeDeltaTicks(), true);
			if (yeah instanceof BlockHitResult hit) {
				if (mc.level.getFluidState(hit.getBlockPos()).isSource()) {
					return yeah;
				}
			}
		}
		return mc.hitResult;
	}

	public static @NonNull VoxelShape getVoxelShape(HitResult evilHitResult) {
		VoxelShape shape = Shapes.block();
		if (mc.level == null || mc.getCameraEntity() == null) return shape;
		if (evilHitResult instanceof BlockHitResult block) {
			BlockState state = mc.level.getBlockState(block.getBlockPos());
			shape = state.getShape(mc.level, block.getBlockPos());
			shape = shape.isEmpty() ? Shapes.block() : shape;
			//get connected blocks
			if (getActiveInstance().connectedBlocks) {
				Direction connected = joinConnected(block.getBlockPos());
				if (connected != null) {
					shape = Shapes.join(shape, mc.level.getBlockState(block.getBlockPos().relative(connected)).getShape(mc.level, block.getBlockPos().relative(connected), CollisionContext.of(mc.getCameraEntity())).move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR);
				}
			}
			shape = shape.move(block.getBlockPos());
		} else if (evilHitResult instanceof EntityHitResult entityHitResult && getActiveInstance().allowEntities) {
			Entity entity = entityHitResult.getEntity();
			//so, so sloppy. might also have the worst workaround of the century for hanging stuff
			float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			AABB boundingBox = entity.getBoundingBox();
			shape = Shapes.create(moveToZero(boundingBox).move(entity.getPosition(delta).subtract(moveToZero(boundingBox).getCenter()).add(0, entity instanceof HangingEntity ? 0 : moveToZero(boundingBox).maxY / 2F, 0)));
		}
		return shape;
	}

	public static void easeBoxAndEdges(HitResult evilHitResult, VoxelShape shape) {
		AABB targetBox = shape.bounds();
		if (getActiveInstance().doEasing) {
			if (getActiveInstance().updateWhenUnfocused || evilHitResult.getType() != HitResult.Type.MISS) {
				easeBox = new AABB(ease(easeBox.minX, targetBox.minX, getActiveInstance().easeSpeed), ease(easeBox.minY, targetBox.minY, getActiveInstance().easeSpeed), ease(easeBox.minZ, targetBox.minZ, getActiveInstance().easeSpeed), ease(easeBox.maxX, targetBox.maxX, getActiveInstance().easeSpeed), ease(easeBox.maxY, targetBox.maxY, getActiveInstance().easeSpeed), ease(easeBox.maxZ, targetBox.maxZ, getActiveInstance().easeSpeed));
			}
		} else {
			easeBox = targetBox;
		}
		if (getActiveInstance().outlineType == FaceMode.EDGES) {
			updateLines(shape);
		}
	}

	private static void renderEverything(LevelRenderContext c, HitResult hitResult) {
		//render the fill first, we don't want it drawn over the outline
		get().push("updateProgresses");
		updateProgresses(hitResult);
		get().popPush("isCrystalObstructed");
		boolean isCrystalObstructed = isCrystalObstructed(hitResult);
		if (edgeAlpha > 1) {
			if (getActiveInstance().fillEnabled) {
				get().popPush("drawFill");
				drawFill(c.poseStack(), isCrystalObstructed);
			}
			//now the outline itself
			if (getActiveInstance().outlineEnabled) {
				get().popPush("drawOutline");
				drawOutline(c.poseStack(), isCrystalObstructed);
			}
		}
		get().pop();
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
		Color finalFillCol = isCrystalObstructed ? getActiveInstance().crystalHelperFillColor : getActiveInstance().fillRainbow ? getRainbowCol(0) : getActiveInstance().fillCol;
		Color finalFillCol2 = isCrystalObstructed ? getActiveInstance().crystalHelperFillColor : getActiveInstance().fillRainbow ? getRainbowCol(getActiveInstance().delay) : getActiveInstance().fillCol2;
		boolean b = getActiveInstance().fillDepthTest != DepthTestMode.ALWAYS_PASS && getActiveInstance().fillExpand == 0;
		//TODO: instead of expanding by fixed amount, do it based on a percentage of the box size
		Renderer.drawBoxFill(stack, easeBox.inflate(getActiveInstance().fillExpand + (b ? 0.001 : 0)), finalFillCol, finalFillCol2, sideFades);
	}

	private static void drawOutline(PoseStack stack, boolean isCrystalObstructed) {
		// todo fix up profiling
		get().push("pre");
		if (mc.level == null) throw new IllegalStateException("level == null");
		//TODO: make check so that cut from corner and cut from center do not add up to higher than 0.95

		Pair<Color, Color> mainCols = getColors(isCrystalObstructed, getActiveInstance().outlineRainbow, getActiveInstance().delay, getActiveInstance().lineCol, getActiveInstance().lineCol2, getActiveInstance().crystalHelperLineColor);
		if (getActiveInstance().outlineType == FaceMode.EDGES) {
			if (getActiveInstance().tertiary) {
				Pair<Color, Color> colors = getColors(isCrystalObstructed, getActiveInstance().toutlineRainbow, getActiveInstance().delay, getActiveInstance().tlineCol, getActiveInstance().tlineCol2, getActiveInstance().crystalHelperLineColor);
				Renderer.drawEdgeOutline(stack, easeBox, lines, colors.first, colors.second, edgeAlpha * getActiveInstance().tlineAlphaMultiplier, getActiveInstance().tlineWidth, getActiveInstance().tcutFromCenter, getActiveInstance().tcutFromCorner, getActiveInstance().touterThicknessMult, getActiveInstance().tinnerThicknessMult, 2);
			}
			if (getActiveInstance().secondary) {
				Pair<Color, Color> colors = getColors(isCrystalObstructed, getActiveInstance().soutlineRainbow, getActiveInstance().delay, getActiveInstance().slineCol, getActiveInstance().slineCol2, getActiveInstance().crystalHelperLineColor);
				Renderer.drawEdgeOutline(stack, easeBox, lines, colors.first, colors.second, edgeAlpha * getActiveInstance().slineAlphaMultiplier, getActiveInstance().slineWidth, getActiveInstance().scutFromCenter, getActiveInstance().scutFromCorner, getActiveInstance().souterThicknessMult, getActiveInstance().sinnerThicknessMult, 1);
			}
			Renderer.drawEdgeOutline(stack, easeBox, lines, mainCols.first, mainCols.second, edgeAlpha, getActiveInstance().lineWidth, getActiveInstance().cutFromCenter, getActiveInstance().cutFromCorner, getActiveInstance().outerThicknessMult, getActiveInstance().innerThicknessMult, 0);
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
			Renderer.drawBoxOutline(stack, inflated, mainCols.first, mainCols.second, lineFades, getActiveInstance().lineWidth, getActiveInstance().cutFromCenter, getActiveInstance().cutFromCorner, getActiveInstance().outerThicknessMult, getActiveInstance().innerThicknessMult, 0);
		}
		get().pop();
	}

	public static float @NonNull [] getNewFades(float alphaMultiplier) {
		float[] newFades = Arrays.copyOf(lineFades, 6);
		for (int i = 0; i < 6; i++) {
			newFades[i] = Mth.clamp(newFades[i] * alphaMultiplier, 0, 255F);
		}
		return newFades;
	}

	public static Pair<Color, Color> getColors(boolean isCrystalObstructed, boolean rainbow, int delay, Color col, Color col2, Color crystalHelperCol) {
		return Pair.of(
				isCrystalObstructed ? crystalHelperCol : rainbow ? getRainbowCol(0) : col,
				isCrystalObstructed ? crystalHelperCol : rainbow ? getRainbowCol(delay) : col2
		);
	}

	private static void updateProgresses(HitResult evilHitResult) {
		if (mc.level == null) return;
		boolean miss = evilHitResult.getType() == HitResult.Type.MISS;
		if (evilHitResult instanceof EntityHitResult) {
			if (getActiveInstance().allowEntities) {
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = getActiveInstance().fadeIn ? easeF(sideFades[dir.ordinal()], getActiveInstance().fillOpacity, getActiveInstance().fadeInSpeed) : getActiveInstance().fillOpacity;
					lineFades[dir.ordinal()] = getActiveInstance().fadeIn ? easeF(lineFades[dir.ordinal()], getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
				}
				edgeAlpha = getActiveInstance().fadeIn ? easeF(edgeAlpha, getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
			} else {
				miss = true;
				exitFades();
			}
		} else if (evilHitResult instanceof BlockHitResult block) {
			if (mc.level.isEmptyBlock(block.getBlockPos()) || miss) {
				exitFades();
			} else {
				edgeAlpha = getActiveInstance().fadeIn ? easeF(edgeAlpha, getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
				EnumSet<Direction> sides = getSides(getActiveInstance().fillType, block.getBlockPos(), evilHitResult);
				EnumSet<Direction> lines = getSides(getActiveInstance().outlineType, block.getBlockPos(), evilHitResult);
				for (Direction dir : Direction.values()) {
					if (sides.contains(dir)) {
						sideFades[dir.ordinal()] = getActiveInstance().fadeIn ? easeF(sideFades[dir.ordinal()], getActiveInstance().fillOpacity, getActiveInstance().fadeInSpeed) : getActiveInstance().fillOpacity;
					} else {
						sideFades[dir.ordinal()] = getActiveInstance().fadeOut ? easeF(sideFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
					}
					if (lines.contains(dir)) {
						lineFades[dir.ordinal()] = getActiveInstance().fadeIn ? easeF(lineFades[dir.ordinal()], getActiveInstance().lineAlpha, getActiveInstance().fadeInSpeed) : getActiveInstance().lineAlpha;
					} else {
						lineFades[dir.ordinal()] = getActiveInstance().fadeOut ? easeF(lineFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
					}
				}
			}
			Direction d = block.getDirection();
			Quaternionf target = d.getRotation();

			if (d != Direction.UP && d != Direction.DOWN) {
				lastHorizontalDirection = d;
			} else {
				float pitch = (float) ((d == Direction.UP) ? (-Math.PI / 2F) : (Math.PI / 2F));
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
		//I didn't add in/out because it would BREAKKK. TODO THIS
		scaleProg = getActiveInstance().scale ? easeF(scaleProg, miss ? 0 : 1, getActiveInstance().scaleSpeed) : 1;
		lineProg = getActiveInstance().animateLineThickness ? easeF(lineProg, miss ? 0 : 1, getActiveInstance().lineThicknessAnimationSpeed) : 1;
	}

	public static void exitFades() {
		for (Direction dir : Direction.values()) {
			sideFades[dir.ordinal()] = getActiveInstance().fadeOut ? easeF(sideFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
			lineFades[dir.ordinal()] = getActiveInstance().fadeOut ? easeF(lineFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
		}
		edgeAlpha = getActiveInstance().fadeOut ? easeF(edgeAlpha, 0, getActiveInstance().fadeOutSpeed) : 0;
	}

	public static HitResult pick(Entity e, final double range, final float a, final boolean withLiquids) {
        if (mc.level == null) return null;
		Vec3 from = e.getEyePosition(a);
		Vec3 viewVector = e.getViewVector(a);
		Vec3 to = from.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
		return mc.level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, withLiquids ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE, e));
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