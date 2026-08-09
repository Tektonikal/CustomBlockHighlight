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
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
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
import static tektonikal.customblockhighlight.Vertexer.worldSpaceToScreenSpace;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.config;

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

//NOW
//TODO: presets
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
	public static final StagedVertexBuffer stagedFaceBuffer = new StagedVertexBuffer(() -> " CBH sides", RenderType.SMALL_BUFFER_SIZE);
	public static final StagedVertexBuffer stagedOutlineBuffer = new StagedVertexBuffer(() -> " CBH outline", RenderType.SMALL_BUFFER_SIZE);

	public static AABB easeBox = new AABB(0, 0, 0, 0, 0, 0);
	public static AABB targetBox = new AABB(0, 0, 0, 0, 0, 0);

	public static List<Line> lines = new ArrayList<>();
	public static List<Line> toRemove = new ArrayList<>();

	public static VoxelShape shape = Shapes.block();
	public static Direction connected = null;
	public static float edgeAlpha = 0;
	public static float scaleProg = 0;
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
					case 0 ->
							renderPass.setPipeline(getPipeline(config().lineDepthTest, true));
					case 1 ->
							renderPass.setPipeline(getPipeline(config().slineDepthTest, true));
					case 2 ->
							renderPass.setPipeline(getPipeline(config().tlineDepthTest, true));
				}
			} else {
				renderPass.setPipeline(getPipeline(config().fillDepthTest, false));
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
	public static RenderPipeline getPipeline(DepthTestMode mode, boolean lines){
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
		Vertexer.vertexBoxQuads(stack, buffer, moveToZero(box), cols, col2, alpha);
		finishDraw(false, draw, 0);
		stack.popPose();
	}

	private static void doEvilMatrixPreparations(PoseStack stack, AABB box, boolean horribleWorkaroundForEdges) {
		stack.pushPose();
		stack.translate(box.minX - camera.position().x, box.minY - camera.position().y, box.minZ - camera.position().z);
		Vec3 vec = moveToZero(box).getCenter();
		stack.translate(vec);
		stack.scale(scaleProg, scaleProg, scaleProg);
		if (horribleWorkaroundForEdges) {
			float yeah = config().lineExpand * 2 + 1;
			stack.scale(yeah, yeah, yeah);
		}
		stack.translate(vec.reverse());
	}

	public static void drawBoxOutline(PoseStack stack, AABB box, Color color, Color col2, float[] alpha, int layer) {
		doEvilMatrixPreparations(stack, box, false);
		StagedVertexBuffer.Draw draw = startDrawing(true);
		VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);
		Vertexer.vertexBoxLines(stack, buffer, moveToZero(box), color, col2, alpha, layer);
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

	public static void drawEdgeOutline(PoseStack matrices, VoxelShape shape, Color c1, Color c2, float alpha, int layer) {
		doEvilMatrixPreparations(matrices, shape.bounds(), true);
		List<Line> newLines = new ArrayList<>();
		StagedVertexBuffer.Draw draw = startDrawing(true);
		VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);
		moveToZero(shape).forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> newLines.add(new Line(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ))));
		Vec3 minVec = shape.bounds().getMinPosition();
		if (lines.isEmpty() || !config().doEasing) {
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
		List<Line> sortedLines = Renderer.lines.stream().sorted(Comparator.comparing(o -> ((Line) o).getDistanceToCamera(minVec)).reversed()).toList();

		shape = moveToZero(shape);
		double normalised = shape.bounds().getMinPosition().distanceTo(shape.bounds().getMaxPosition());
		for (Line finalLine : sortedLines) {
			finalLine.updateAndRender(matrices, buffer, getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(finalLine.minPos.x, finalLine.minPos.y, finalLine.minPos.z)) / normalised)), getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(finalLine.maxPos.x, finalLine.maxPos.y, finalLine.maxPos.z)) / normalised)), Math.round(alpha), true, layer);
		}
		toRemove.removeIf(line -> line.alphaMultiplier < 1 / 255f);
		for (Line line : toRemove) {
			line.updateAndRender(matrices, buffer, getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(line.minPos.x, line.minPos.y, line.minPos.z)) / normalised)), getLerpedColor(c1, c2, (float) (shape.bounds().getMinPosition().distanceTo(new Vec3(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / normalised)), Math.round(alpha), false, layer);
		}
		finishDraw(true, draw, layer);
		matrices.popPose();
	}


	public static AABB moveToZero(AABB box) {
		return box.move(box.getMinPosition().reverse());
	}

	public static VoxelShape moveToZero(VoxelShape shape) {
		Vec3 minVec = shape.bounds().getMinPosition();
		return shape.move(minVec.reverse());
	}

	private static Direction[] invert(Direction[] invertDirs) {
		EnumSet<Direction> dirs = EnumSet.allOf(Direction.class);
		for (Direction d : invertDirs) {
			dirs.remove(d);
		}
		return dirs.toArray(Direction[]::new);
	}

	private static Direction[] getSides(OutlineType type, BlockPos pos) {
		return switch (type) {
			case LOOKAT ->
					(evilHitResult instanceof BlockHitResult block) ? new Direction[]{block.getDirection()} : Direction.values();
			case AIR_EXPOSED -> invert(getConcealedFaces(pos));
			case CONCEALED -> getConcealedFaces(pos);
			default -> Direction.values();
		};
	}

	public static Color getRainbowCol(float delay) {
		double rainbowState = Math.ceil((System.currentTimeMillis() + (int) (delay))) * config().rainbowSpeed / 50;
		rainbowState %= 360;
		return Color.getHSBColor((float) (rainbowState / 360.0f), config().saturation, config().brightness);
	}

	public static boolean isBlockOccupied(BlockPos pos) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		if (mc.level.getBlockState(pos).hasProperty(BlockStateProperties.WATERLOGGED) && !mc.level.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED) && !mc.level.getFluidState(pos).isEmpty()) {
			//ignore liquids
			return false;
		}
		return !mc.level.isEmptyBlock(pos);
	}

	public static Direction[] getConcealedFaces(BlockPos pos) {
        /*
        I don't know if I should keep the original behavior for this
        As of now, this method means that even when rendering the box for a block with multiple parts,
        it will still cull faces relative to the selected block, and not the entire rendered selection
         */
		Direction[] dirs = new Direction[6];
		if (isBlockOccupied(pos.above())) dirs[0] = Direction.UP;
		if (isBlockOccupied(pos.below())) dirs[1] = Direction.DOWN;
		if (isBlockOccupied(pos.north())) dirs[2] = Direction.NORTH;
		if (isBlockOccupied(pos.east())) dirs[3] = Direction.EAST;
		if (isBlockOccupied(pos.south())) dirs[4] = Direction.SOUTH;
		if (isBlockOccupied(pos.west())) dirs[5] = Direction.WEST;
		return dirs;
	}

	public static Color getLerpedColor(Color c1, Color c2, float percent) {
		return new Color(Math.clamp(Mth.lerpInt(percent, c1.getRed(), c2.getRed()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getGreen(), c2.getGreen()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getBlue(), c2.getBlue()), 0, 255));
	}

	public static void mainLoop(LevelRenderContext c) {
		evilHitResult = mc.hitResult;
		//this is just for the warnings to go away
		if (evilHitResult == null || mc.level == null || mc.getCameraEntity() == null || mc.player == null) return;
		if(config().allowLiquids && (mc.player.getMainHandItem().is(Items.BUCKET) || mc.player.getOffhandItem().is(Items.BUCKET))) {
			HitResult yeah = pick(mc.getCameraEntity(), mc.player.blockInteractionRange(), mc.getDeltaTracker().getRealtimeDeltaTicks(), true);
			if(yeah instanceof BlockHitResult hit) {
				if(mc.level.getFluidState(hit.getBlockPos()).isSource()){
					evilHitResult = yeah;
				}
			}
		}
		if (evilHitResult.getType() != HitResult.Type.MISS) {
			if (evilHitResult instanceof BlockHitResult block) {
				BlockState state = mc.level.getBlockState(block.getBlockPos());
				VoxelShape shape = state.getShape(mc.level, block.getBlockPos());
				if (shape.isEmpty()) {
					targetBox = new AABB(block.getBlockPos());
				} else {
					targetBox = shape.bounds().move(block.getBlockPos());
				}
			} else if (evilHitResult instanceof EntityHitResult entityHitResult && config().allowEntities) {
				Entity entity = entityHitResult.getEntity();
				//so, so sloppy. might also have the worst workaround of the century for hanging stuff
				float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
				targetBox = moveToZero(entity.getBoundingBox()).move(entity.getPosition(delta).subtract(moveToZero(entity.getBoundingBox()).getCenter()).add(0, entity instanceof HangingEntity ? 0 : moveToZero(entity.getBoundingBox()).maxY / 2F, 0));

			}
		}


		//get connected blocks
		if (config().connectedBlocks && evilHitResult instanceof BlockHitResult block) {
			if (evilHitResult.getType() == HitResult.Type.MISS) {
				connected = null;
			} else {
				BlockState state = mc.level.getBlockState(block.getBlockPos());
				connected = joinConnected(state, block.getBlockPos());
			}
		}
		//calculate where to render the block
		if (config().doEasing) {
			if (config().updateWhenUnfocused || evilHitResult.getType() != HitResult.Type.MISS) {
				easeBox = new AABB(ease(easeBox.minX, targetBox.minX, config().easeSpeed), ease(easeBox.minY, targetBox.minY, config().easeSpeed), ease(easeBox.minZ, targetBox.minZ, config().easeSpeed), ease(easeBox.maxX, targetBox.maxX, config().easeSpeed), ease(easeBox.maxY, targetBox.maxY, config().easeSpeed), ease(easeBox.maxZ, targetBox.maxZ, config().easeSpeed));
			}
		} else {
			easeBox = targetBox;
		}
		renderOutline(c.poseStack(), isCrystalObstructed(), evilHitResult.getType() == HitResult.Type.MISS);
	}

	private static void renderOutline(PoseStack stack, boolean isCrystalObstructed, boolean shouldFade) {
		//render the fill first, we don't want it drawn over the outline
		updateProgresses(shouldFade);
		if (edgeAlpha > 1) {
			if (config().fillEnabled) {
				drawFill(stack, isCrystalObstructed);
			}
			//now the outline itself
			if (config().outlineEnabled) {
				drawOutline(stack, isCrystalObstructed);
			}
		}
	}

	private static boolean isCrystalObstructed() {
		if (mc.level == null) throw new IllegalStateException("level == null");
		if (!(evilHitResult instanceof BlockHitResult block)) return false;
		BlockState state = mc.level.getBlockState(block.getBlockPos());

		if (config().crystalHelper) {
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
		Color finalFillCol = isCrystalObstructed ? config().crystalHelperFillColor : config().fillRainbow ? getRainbowCol(0) : config().fillCol;
		Color finalFillCol2 = isCrystalObstructed ? config().crystalHelperFillColor : config().fillRainbow ? getRainbowCol(config().delay) : config().fillCol2;
		boolean b = config().fillDepthTest != DepthTestMode.ALWAYS_PASS && config().fillExpand == 0;
		Renderer.drawBoxFill(stack, easeBox.inflate(config().fillExpand + (b ? 0.001 : 0)), finalFillCol, finalFillCol2, sideFades);
	}

	private static void drawOutline(PoseStack stack, boolean isCrystalObstructed) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		var cameraEntity = camera.entity();
		if (cameraEntity == null) return;
		//TODO: make check so that cut from corner and cut from center do not add up to higher than 0.95
		Color finalLineCol = isCrystalObstructed ? config().crystalHelperLineColor : config().outlineRainbow ? getRainbowCol(0) : config().lineCol;
		Color finalLineCol2 = isCrystalObstructed ? config().crystalHelperLineColor : config().outlineRainbow ? getRainbowCol(config().delay) : config().lineCol2;
		if (config().outlineType == OutlineType.EDGES) {
			if (evilHitResult != null && evilHitResult.getType() != HitResult.Type.MISS) {
				if (evilHitResult instanceof BlockHitResult block) {
					if (isBlockOccupied(block.getBlockPos())) {
						shape = mc.level.getBlockState(block.getBlockPos()).getShape(mc.level, block.getBlockPos(), CollisionContext.of(cameraEntity));
						if (connected != null) {
							shape = Shapes.joinUnoptimized(shape, mc.level.getBlockState(block.getBlockPos().relative(connected)).getShape(mc.level, block.getBlockPos().relative(connected), CollisionContext.of(cameraEntity)).move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR).optimize();
						}
					}
				} else if (evilHitResult instanceof EntityHitResult entity && config().allowEntities) {
					shape = Shapes.create(entity.getEntity().getBoundingBox());
				}
			}
			if (!shape.isEmpty()) {
				if (config().tertiary) {
					Color tfinalLineCol = isCrystalObstructed ? config().crystalHelperLineColor : config().toutlineRainbow ? getRainbowCol(0) : config().tlineCol;
					Color tfinalLineCol2 = isCrystalObstructed ? config().crystalHelperLineColor : config().toutlineRainbow ? getRainbowCol(config().delay) : config().tlineCol2;
					Renderer.drawEdgeOutline(stack, shape.move(easeBox.minX - shape.bounds().getMinPosition().x, easeBox.minY - shape.bounds().getMinPosition().y, easeBox.minZ - shape.bounds().getMinPosition().z), tfinalLineCol, tfinalLineCol2, edgeAlpha * config().tlineAlphaMultiplier, 2);
				}
				if (config().secondary) {
					Color sfinalLineCol = isCrystalObstructed ? config().crystalHelperLineColor : config().soutlineRainbow ? getRainbowCol(0) : config().slineCol;
					Color sfinalLineCol2 = isCrystalObstructed ? config().crystalHelperLineColor : config().soutlineRainbow ? getRainbowCol(config().delay) : config().slineCol2;
					Renderer.drawEdgeOutline(stack, shape.move(easeBox.minX - shape.bounds().getMinPosition().x, easeBox.minY - shape.bounds().getMinPosition().y, easeBox.minZ - shape.bounds().getMinPosition().z), sfinalLineCol, sfinalLineCol2, edgeAlpha * config().slineAlphaMultiplier, 1);
				}
				Renderer.drawEdgeOutline(stack, shape.move(easeBox.minX - shape.bounds().getMinPosition().x, easeBox.minY - shape.bounds().getMinPosition().y, easeBox.minZ - shape.bounds().getMinPosition().z), finalLineCol, finalLineCol2, edgeAlpha, 0);
			}
		} else {
			if (config().tertiary) {
				Color tfinalLineCol = isCrystalObstructed ? config().crystalHelperLineColor : config().toutlineRainbow ? getRainbowCol(0) : config().tlineCol;
				Color tfinalLineCol2 = isCrystalObstructed ? config().crystalHelperLineColor : config().toutlineRainbow ? getRainbowCol(config().delay) : config().tlineCol2;
				float[] newFades = new float[6];
				for (int i = 0; i < 6; i++) {
					newFades[i] = Mth.clamp(lineFades[i] * config().tlineAlphaMultiplier, 0, 255F);
				}
				Renderer.drawBoxOutline(stack, easeBox.inflate(config().lineExpand), tfinalLineCol, tfinalLineCol2, newFades, 2);
			}
			if (config().secondary) {
				Color sfinalLineCol = isCrystalObstructed ? config().crystalHelperLineColor : config().soutlineRainbow ? getRainbowCol(0) : config().slineCol;
				Color sfinalLineCol2 = isCrystalObstructed ? config().crystalHelperLineColor : config().soutlineRainbow ? getRainbowCol(config().delay) : config().slineCol2;
				float[] newFades = new float[6];
				for (int i = 0; i < 6; i++) {
					newFades[i] = Mth.clamp(lineFades[i] * config().slineAlphaMultiplier, 0, 255F);
				}
				Renderer.drawBoxOutline(stack, easeBox.inflate(config().lineExpand), sfinalLineCol, sfinalLineCol2, newFades, 1);
			}
			Renderer.drawBoxOutline(stack, easeBox.inflate(config().lineExpand), finalLineCol, finalLineCol2, lineFades, 0);
		}
		// insert model data pulling render idk code here
	}

	private static void updateProgresses(boolean shouldFadeOut) {
		if (evilHitResult == null || mc.level == null) return;
		//TODO: clean this up later
		if (evilHitResult.getType() == HitResult.Type.ENTITY) {
			if (config().allowEntities) {
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = config().fadeIn ? (float) ease(sideFades[dir.ordinal()], config().fillOpacity, config().fadeInSpeed) : config().fillOpacity;
					lineFades[dir.ordinal()] = config().fadeIn ? (float) ease(lineFades[dir.ordinal()], config().lineAlpha, config().fadeInSpeed) : config().lineAlpha;
				}
				edgeAlpha = config().fadeIn ? (float) ease(edgeAlpha, config().lineAlpha, config().fadeInSpeed) : config().lineAlpha;
			}else{
				shouldFadeOut = true;
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = config().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, config().fadeOutSpeed) : 0;
					lineFades[dir.ordinal()] = config().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, config().fadeOutSpeed) : 0;
				}
				edgeAlpha = config().fadeOut ? (float) ease(edgeAlpha, 0, config().fadeOutSpeed) : 0;
			}
		} else if (evilHitResult instanceof BlockHitResult block) {
			if ((mc.level.isEmptyBlock(block.getBlockPos()) || shouldFadeOut)) {
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = config().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, config().fadeOutSpeed) : 0;
					lineFades[dir.ordinal()] = config().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, config().fadeOutSpeed) : 0;
				}
				edgeAlpha = config().fadeOut ? (float) ease(edgeAlpha, 0, config().fadeOutSpeed) : 0;
			} else {
				edgeAlpha = config().fadeIn ? (float) ease(edgeAlpha, config().lineAlpha, config().fadeInSpeed) : config().lineAlpha;
				for (Direction dir : getSides(config().fillType, block.getBlockPos())) {
					if (dir != null) {
						sideFades[dir.ordinal()] = config().fadeIn ? (float) ease(sideFades[dir.ordinal()], config().fillOpacity, config().fadeInSpeed) : config().fillOpacity;
					}
				}
				for (Direction dir : invert(getSides(config().fillType, block.getBlockPos()))) {
					if (dir != null) {
						sideFades[dir.ordinal()] = config().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, config().fadeOutSpeed) : 0;
					}
				}
				for (Direction dir : getSides(config().outlineType, block.getBlockPos())) {
					if (dir != null) {
						lineFades[dir.ordinal()] = config().fadeIn ? (float) ease(lineFades[dir.ordinal()], config().lineAlpha, config().fadeInSpeed) : config().lineAlpha;
					}
				}
				for (Direction dir : invert(getSides(config().outlineType, block.getBlockPos()))) {
					if (dir != null) {
						lineFades[dir.ordinal()] = config().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, config().fadeOutSpeed) : 0;
					}
				}
			}
		}
		//I didn't add in/out because it would BREAKKK. TODO THIS
		scaleProg = config().scale ? (float) ease(scaleProg, shouldFadeOut ? 0 : 1, config().scaleSpeed) : 1;
	}

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
		DoubleBlockHalf half;
		if (state.getBlock() instanceof ChestBlock && !state.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)) {
			dir = ChestBlock.getConnectedDirection(state);
			if (evilHitResult instanceof BlockHitResult block) {
				connectedPos = block.getBlockPos().relative(dir);
			} else {
				return null;
			}
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof ChestBlock) {
				targetBox = targetBox.minmax(connectedState.getShape(mc.level, connectedPos).bounds().move(connectedPos));
			}
			return dir;
		}
		if (state.getBlock() instanceof DoorBlock) {
			half = state.getValue(DoorBlock.HALF);
			if (half == DoubleBlockHalf.LOWER) {
				dir = Direction.UP;
			} else {
				dir = Direction.DOWN;
			}
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof DoorBlock && connectedState.getValue(DoorBlock.HALF) != half) {
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
		if (state.getBlock() instanceof DoublePlantBlock) {
			half = state.getValue(DoublePlantBlock.HALF);
			if (half == DoubleBlockHalf.LOWER) {
				dir = Direction.UP;
			} else {
				dir = Direction.DOWN;
			}
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof DoublePlantBlock) {
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