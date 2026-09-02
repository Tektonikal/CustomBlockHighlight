package tektonikal.customblockhighlight;

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
// We use the one from fastutil because it makes the Java go faster. It's like putting flame stickers on your car
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.vehicle.boat.Boat;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.mixin.VoxelShapeAccessor;
import tektonikal.customblockhighlight.util.*;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_QUADS;
import static net.minecraft.client.renderer.RenderPipelines.LINES;
import static net.minecraft.util.profiling.Profiler.get;
import static tektonikal.customblockhighlight.Blockhighlight.ease;
import static tektonikal.customblockhighlight.Blockhighlight.easeF;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.*;

// TODO :!! !! reset shit when changing configs
public class Renderer {
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Camera camera = mc.gameRenderer.mainCamera();

	public static final float[] sideFades = new float[6];

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

	public static float scaleProg = 0;
	public static float lineProg = 0;
	public static Quaternionf rotation = new Quaternionf();
	private static Direction lastHorizontalDirection = Direction.NORTH;

	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();

	public static final List<LineState> lineStates = new ArrayList<>(Stream.of(new LineState(), new LineState(), new LineState()).toList());


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
		if (colorTexture == null) return;
		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "CBH pass", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
			if (lines) {
				renderPass.setPipeline(getPipeline(getActiveInstance().getLineConfig(layer).lineDepthTest, true));
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

	public static void drawBoxFill(PoseStack stack, AABB box, Pair<Color, Color> cols, float[] alpha) {
		doEvilMatrixPreparations(stack, box, getActiveInstance().fillExpandBlocks, getActiveInstance().fillExpandPercent);
		StagedVertexBuffer.Draw draw = startDrawing(false);
		VertexConsumer buffer = stagedFaceBuffer.getVertexBuilder(draw);
		Vertexer.vertexBoxQuads(stack.last(), buffer, moveToZero(box), cols, alpha);
		finishDraw(false, draw, 0);
		stack.popPose();
	}

	private static void doEvilMatrixPreparations(PoseStack stack, AABB box, float scaleBlocks, float scalePercentage) {
		stack.pushPose();
		stack.translate(box.minX - camera.position().x, box.minY - camera.position().y, box.minZ - camera.position().z);
		Vec3 vec = moveToZero(box).getCenter();
		stack.translate(vec);
		if (getActiveInstance().rotations) {
			stack.rotateAround(rotation, 0, 0, 0);
		}
		AABB scaled = box.inflate(scaleBlocks);
		Vector3f boxDim = new Vector3f((float) (scaled.getXsize() / box.getXsize()), (float) (scaled.getYsize() / box.getYsize()), (float) (scaled.getZsize() / box.getZsize()));
		stack.scale(boxDim.x, boxDim.y, boxDim.z);
		stack.scale(scalePercentage, scalePercentage, scalePercentage);
		stack.scale(scaleProg, scaleProg, scaleProg);
		stack.translate(vec.reverse());
	}

	public static void drawLineLayer(PoseStack stack, BlockHighlightConfig.LineConfig cfg, boolean obstructed, int layer) {
		if (cfg.enabled) {
			doEvilMatrixPreparations(stack, easeBox, cfg.lineExpandBlocks, cfg.lineExpandPercentage);
			StagedVertexBuffer.Draw draw = startDrawing(true);
			VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);
			AABB zeroed = moveToZero(easeBox);
			Pair<Color, Color> cols = cfg.color.getColors(obstructed, getActiveInstance().crystalHelperLineColor);
			if (cfg.shapeStyle != ShapeStyle.CLASSIC_BOX) {
				double normalised = zeroed.getMinPosition().distanceTo(zeroed.getMaxPosition());
				for (Line line : Util.concat(lineStates.get(layer).lines, lineStates.get(layer).toRemove)) {
					line.render(stack, buffer,
							getLerpedColor(cols.first(), cols.second(), (float) (zeroed.getMinPosition().distanceTo(line.minPos) / normalised)),
							getLerpedColor(cols.first(), cols.second(), (float) (zeroed.getMinPosition().distanceTo(line.maxPos) / normalised)),
							Math.round(lineStates.get(layer).getEdgeAlpha()), cfg.lineWidth, cfg.cutFromCenter, cfg.cutFromCorner, cfg.outerThicknessMult, cfg.innerThicknessMult);
				}
			} else {
				Vertexer.vertexBoxLines(stack.last(), buffer, zeroed, cols, lineStates.get(layer).getLineFades(), cfg.lineWidth * lineProg, cfg.cutFromCenter, cfg.cutFromCorner, cfg.outerThicknessMult, cfg.innerThicknessMult);
			}
			finishDraw(true, draw, layer);
			stack.popPose();
		}
	}

	public static void updateLines(VoxelShape shape, HitResult evilHitResult) {
		getActiveInstance().lineConfigs().forEach(lineConfig -> {
//			final VoxelShape evilShape = scaleBoth(shape, lineConfig.lineExpandPercentage, lineConfig.lineExpandBlocks);
			LineState state = lineStates.get(getActiveInstance().reversedLineConfigs().indexOf(lineConfig));
			//TODO: these don't sort by depth anymore
			ArrayList<Line> newLines = new ArrayList<>();
			if (lineConfig.shapeStyle == ShapeStyle.MODEL_SHAPE) {
				if (evilHitResult instanceof BlockHitResult bhr) {
					List<BlockStateModelPart> s = new ArrayList<>();
					mc.getModelManager().getBlockStateModelSet().get(mc.level.getBlockState(bhr.getBlockPos())).collectParts(RandomSource.create(), s);
					Direction dir = getDirection(bhr);
					if (dir != null) {
						List<BlockStateModelPart> s2 = new ArrayList<>();
						mc.getModelManager().getBlockStateModelSet().get(mc.level.getBlockState(bhr.getBlockPos().relative(dir))).collectParts(RandomSource.create(), s2);
						s2.forEach(blockStateModelPart -> {
							((SimpleModelWrapper) blockStateModelPart).quads().getAll().forEach(quad -> {
//								newLines.add(new Line(new Vec3(quad.position0()).add(dir.getUnitVec3()), new Vec3(quad.position1()).add(dir.getUnitVec3())));
//								newLines.add(new Line(new Vec3(quad.position1()).add(dir.getUnitVec3()), new Vec3(quad.position2()).add(dir.getUnitVec3())));
//								newLines.add(new Line(new Vec3(quad.position2()).add(dir.getUnitVec3()), new Vec3(quad.position3()).add(dir.getUnitVec3())));
//								newLines.add(new Line(new Vec3(quad.position3()).add(dir.getUnitVec3()), new Vec3(quad.position0()).add(dir.getUnitVec3())));
							});
						});
					}
					ArrayList<Line> finalNewLines = newLines;
					s.forEach(blockStateModelPart -> {
						((SimpleModelWrapper) blockStateModelPart).quads().getAll().forEach(quad -> {
							finalNewLines.add(new Line(new Vec3(quad.position0()), new Vec3(quad.position1())));
							finalNewLines.add(new Line(new Vec3(quad.position1()), new Vec3(quad.position2())));
							finalNewLines.add(new Line(new Vec3(quad.position2()), new Vec3(quad.position3())));
							finalNewLines.add(new Line(new Vec3(quad.position3()), new Vec3(quad.position0())));
						});
					});
					newLines = newLines.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
				}
			} else {
				ArrayList<Line> finalNewLines1 = newLines;
				shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> finalNewLines1.add(new Line(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ))));
			}
			if (state.lines.isEmpty() || !getActiveInstance().doEasing) {
				state.lines = newLines;
			}
			while (state.lines.size() < newLines.size()) {
//            if (!toRemove.isEmpty()) {
//                lines.add(toRemove.getFirst());
//                toRemove.removeFirst();
//            } else {
				state.lines.add(new Line(shape.bounds().getCenter(), shape.bounds().getCenter()));
//            }
			}
			while (state.lines.size() > newLines.size()) {
				state.toRemove.add(state.lines.getLast());
				state.lines.removeLast();
			}
			ArrayList<Line> finalNewLines2 = newLines;
			state.lines.forEach(line -> {
				Line target = finalNewLines2.get(state.lines.indexOf(line));
				line.moveTo(target.minPos, target.maxPos);
				line.update(true);
			});
			state.toRemove.forEach(line -> line.update(false));
			state.toRemove.removeIf(line -> line.alphaMultiplier < 1 / 255f);
		});
	}

	public static @Nullable Direction getDirection(BlockHitResult bhr) {
		return joinConnected(bhr.getBlockPos());
	}

	//TODO: minimize usage of moveToZero
	public static AABB moveToZero(AABB box) {
		return box.move(box.getMinPosition().reverse());
	}

	public static VoxelShape moveToZero(VoxelShape shape) {
		return shape.move(shape.bounds().getMinPosition().reverse());
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
		if ((!mc.gui.hud.isHidden() || getActiveInstance().showWhenNoHud) && (!mc.player.gameMode().isBlockPlacingRestricted() || getActiveInstance().showWhenNoInteraction)) {
			get().push("Custom block outline pre");
			HitResult evilHitResult = getHitResult();
			easeBoxAndEdges(evilHitResult, getVoxelShape(evilHitResult));
			get().popPush("Custom block outline render");
			renderEverything(c, evilHitResult);
			get().pop();
		}
	}

	public static HitResult getHitResult() {
		if (mc.level == null || mc.player == null || mc.getCameraEntity() == null) return null;
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

	public static @NonNull VoxelShape getVoxelShape(HitResult evilHitResult) {
		VoxelShape shape = Shapes.block();
		if (mc.level == null || mc.getCameraEntity() == null) return shape;
		if (evilHitResult instanceof BlockHitResult block) {
			BlockPos pos = block.getBlockPos();
			BlockState state = mc.level.getBlockState(pos);
			shape = mc.level.getFluidState(pos).isEmpty() ? state.getShape(mc.level, pos) : mc.level.getFluidState(pos).getShape(mc.level, pos);
			shape = shape.isEmpty() ? Shapes.block() : shape;
			//get connected blocks
			if (getActiveInstance().connectedBlocks) {
				Direction connected = joinConnected(pos);
				if (connected != null) {
					shape = Shapes.join(shape, mc.level.getBlockState(pos.relative(connected)).getShape(mc.level, pos.relative(connected), CollisionContext.of(mc.getCameraEntity())).move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR);
				}
			}
			shape = shape.move(pos);
		} else if (evilHitResult instanceof EntityHitResult entityHitResult && getActiveInstance().allowEntities) {
			Entity entity = entityHitResult.getEntity();
			//so, so sloppy. might also have the worst workaround of the century for hanging stuff
			float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			AABB boundingBox = moveToZero(entity.getBoundingBox());
			shape = Shapes.create(boundingBox.move(entity.getPosition(delta).subtract(boundingBox.getCenter()).add(0, entity instanceof HangingEntity ? 0 : boundingBox.maxY / 2F, 0)));
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
		updateLines(moveToZero(shape), evilHitResult);
	}

	private static void renderEverything(LevelRenderContext c, HitResult hitResult) {
		//render the fill first, we don't want it drawn over the outline
		get().push("updateProgresses");
		updateProgresses(hitResult);
		get().popPush("isCrystalObstructed");
		boolean isCrystalObstructed = isCrystalObstructed(hitResult);
		if (getActiveInstance().fillEnabled) {
			get().popPush("drawFill");
			drawFill(c.poseStack(), isCrystalObstructed);
		}
		//now the outline itself
		if (getActiveInstance().primary.enabled) {
			get().popPush("drawOutline");
			drawOutlines(c.poseStack(), isCrystalObstructed);
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
		boolean b = getActiveInstance().fillDepthTest != DepthTestMode.ALWAYS_PASS && getActiveInstance().fillExpandBlocks == 0 && getActiveInstance().fillExpandPercent == 0;
		Renderer.drawBoxFill(stack, easeBox.inflate(b ? 0.001 : 0), getActiveInstance().fillCol.getColors(isCrystalObstructed, getActiveInstance().crystalHelperFillColor), sideFades);
	}

	private static void drawOutlines(PoseStack stack, boolean isCrystalObstructed) {
		// todo fix up profiling
		get().push("pre");
		if (mc.level == null) throw new IllegalStateException("level == null");
		for (var lineConfig : getActiveInstance().reversedLineConfigs()) {
			drawLineLayer(stack, lineConfig, isCrystalObstructed, getActiveInstance().reversedLineConfigs().indexOf(lineConfig));
		}
		get().pop();
	}

	// this is so bad
	private static void updateProgresses(HitResult evilHitResult) {
		if (mc.level == null) return;
		boolean miss = evilHitResult.getType() == HitResult.Type.MISS;
		if (evilHitResult instanceof EntityHitResult) {
			if (getActiveInstance().allowEntities) {
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = getActiveInstance().fadeIn ? easeF(sideFades[dir.ordinal()], getActiveInstance().fillCol.alpha, getActiveInstance().fadeInSpeed) : getActiveInstance().fillCol.alpha;
				}
				lineStates.forEach(lineState -> {
					for (Direction dir : Direction.values()) {
						lineState.lineFades[dir.ordinal()] = getActiveInstance().fadeIn ? easeF(lineState.lineFades[dir.ordinal()], getActiveInstance().getLineConfig(lineStates.indexOf(lineState)).color.alpha, getActiveInstance().fadeInSpeed) : getActiveInstance().getLineConfig(lineStates.indexOf(lineState)).color.alpha;
					}
					lineState.edgeAlpha = getActiveInstance().fadeIn ? easeF(lineState.edgeAlpha, getActiveInstance().getLineConfig(lineStates.indexOf(lineState)).color.alpha, getActiveInstance().fadeInSpeed) : getActiveInstance().getLineConfig(lineStates.indexOf(lineState)).color.alpha;
				});
			} else {
				miss = true;
				exitFades();
			}
			rotation.nlerp(new Quaternionf(), 0.05F);
		} else if (evilHitResult instanceof BlockHitResult block) {
			if (mc.level.isEmptyBlock(block.getBlockPos()) || miss) {
				exitFades();
			} else {
				lineStates.forEach(lineState -> {
					//TODO: something wrong is here because it will use force the last line layer's outline mode onto the other ones
					EnumSet<Direction> lines = getSides(getActiveInstance().reversedLineConfigs().get(lineStates.indexOf(lineState)).outlineType, block.getBlockPos(), evilHitResult);
					int targetAlpha = getActiveInstance().getLineConfig(lineStates.indexOf(lineState)).color.alpha;
					for (Direction d : Direction.values()) {
						if (lines.contains(d)) {
							lineState.lineFades[d.ordinal()] = getActiveInstance().fadeIn ? easeF(lineState.lineFades[d.ordinal()], targetAlpha, getActiveInstance().fadeInSpeed) : targetAlpha;
						} else {
							lineState.lineFades[d.ordinal()] = getActiveInstance().fadeOut ? easeF(lineState.lineFades[d.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
						}
					}
					lineState.edgeAlpha = getActiveInstance().fadeIn ? easeF(lineState.edgeAlpha, targetAlpha, getActiveInstance().fadeInSpeed) : targetAlpha;
				});
				EnumSet<Direction> sides = getSides(getActiveInstance().fillType, block.getBlockPos(), evilHitResult);
				for (Direction dir : Direction.values()) {
					if (sides.contains(dir)) {
						sideFades[dir.ordinal()] = getActiveInstance().fadeIn ? easeF(sideFades[dir.ordinal()], getActiveInstance().fillCol.alpha, getActiveInstance().fadeInSpeed) : getActiveInstance().fillCol.alpha;
					} else {
						sideFades[dir.ordinal()] = getActiveInstance().fadeOut ? easeF(sideFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
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
			if (!((VoxelShapeAccessor) mc.level.getBlockState(block.getBlockPos()).getShape(mc.level, block.getBlockPos())).invokeIsCubeLike()) {
				target = new Quaternionf();
			}

			rotation.nlerp(target, 0.05F);
		}
		//I didn't add in/out because it would BREAKKK. TODO THIS
		scaleProg = getActiveInstance().scale ? easeF(scaleProg, miss ? 0 : 1, getActiveInstance().scaleSpeed) : 1;
		lineProg = getActiveInstance().animateLineThickness ? easeF(lineProg, miss ? 0 : 1, getActiveInstance().lineThicknessAnimationSpeed) : 1;
	}

	public static void exitFades() {
		for (Direction dir : Direction.values()) {
			sideFades[dir.ordinal()] = getActiveInstance().fadeOut ? easeF(sideFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
			lineStates.forEach(LineState::fadeOutSides);
		}
		lineStates.forEach(lineState -> lineState.edgeAlpha = getActiveInstance().fadeOut ? easeF(lineState.edgeAlpha, 0, getActiveInstance().fadeOutSpeed) : 0);
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
