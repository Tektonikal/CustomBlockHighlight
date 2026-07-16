package tektonikal.customblockhighlight;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
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
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.util.DepthTestMode;
import tektonikal.customblockhighlight.util.Line;
import tektonikal.customblockhighlight.util.OutlineType;

import java.awt.*;
import java.util.*;
import java.util.List;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_QUADS;
import static net.minecraft.client.renderer.RenderPipelines.LINES;
import static tektonikal.customblockhighlight.Blockhighlight.ease;

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
public class Renderer {
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Camera camera = mc.gameRenderer.getMainCamera();

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
					.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, true))
					.withCull(false)
					.build()
	);
	public static final RenderPipeline FILL_CONCEALED_ONLY = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", "pipeline/eviler-fill"))
					.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, true))
					.withCull(false)
					.build()
	);

	public static AABB easeBox = new AABB(0, 0, 0, 0, 0, 0);
	public static AABB targetBox = new AABB(0, 0, 0, 0, 0, 0);

	public static List<Line> lines = new ArrayList<>();
	public static List<Line> toRemove = new ArrayList<>();

	public static VoxelShape shape = Shapes.block();
	public static Direction connected = null;
	public static float edgeAlpha = 0;
	public static float scaleProg = 0;
	public static HitResult evilHitResult;

	private static MappableRingBuffer vertexBuffer;
	private static final ByteBufferBuilder allocator = new ByteBufferBuilder(786432);

	public static BufferBuilder startDrawing(boolean lines) {
		return new BufferBuilder(allocator, lines ? VertexFormat.Mode.LINES : VertexFormat.Mode.QUADS, lines ? DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH : DefaultVertexFormat.POSITION_COLOR);
	}

	private static void yeah(MeshData builtBuffer, MeshData.DrawState state, GpuBuffer vertices, boolean lines, int layer) {
		RenderPipeline p;
		if (lines) {
			p = switch (layer) {
				case 0 -> getPipeline(BlockHighlightConfig.INSTANCE.instance().lineDepthTest, true);
				case 1 -> getPipeline(BlockHighlightConfig.INSTANCE.instance().slineDepthTest, true);
				case 2 -> getPipeline(BlockHighlightConfig.INSTANCE.instance().tlineDepthTest, true);
				default -> throw new IllegalStateException("Unexpected value: " + layer);
			};
		} else {
			p = getPipeline(BlockHighlightConfig.INSTANCE.instance().fillDepthTest, false);
		}
		RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(p.getVertexFormatMode());
		GpuBuffer indices = shapeIndexBuffer.getBuffer(state.indexCount());
		VertexFormat.IndexType indexType = shapeIndexBuffer.type();
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "block_outline outline rendering", mc.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), mc.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
			renderPass.setPipeline(p);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setVertexBuffer(0, vertices);
			renderPass.setIndexBuffer(indices, indexType);
			renderPass.drawIndexed(0, 0, state.indexCount(), 1);
		}

		builtBuffer.close();
	}

	private static void draw(BufferBuilder buffer, boolean lines, int layer) {
		MeshData builtBuffer = buffer.build();
		MeshData.DrawState drawParameters = builtBuffer.drawState();
		VertexFormat format = drawParameters.format();
		GpuBuffer vertices = upload(drawParameters, format, builtBuffer);
		yeah(builtBuffer, drawParameters, vertices, lines, layer);
		vertexBuffer.rotate();
	}

	private static GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
		int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();
		if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
			if (vertexBuffer != null) {
				vertexBuffer.close();
			}
			vertexBuffer = new MappableRingBuffer(() -> "block_outline render pipeline", 34, vertexBufferSize);
		}

		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0L, builtBuffer.vertexBuffer().remaining()), false, true)) {
			MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
		}

		return vertexBuffer.currentBuffer();
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
		BufferBuilder buf = startDrawing(false);
		Vertexer.vertexBoxQuads(stack, buf, moveToZero(box), cols, col2, alpha);
		draw(buf, false, 0);
		stack.popPose();
	}

	private static void doEvilMatrixPreparations(PoseStack stack, AABB box, boolean horribleWorkaroundForEdges) {
		stack.pushPose();
		stack.translate(box.minX - camera.position().x, box.minY - camera.position().y, box.minZ - camera.position().z);
		Vec3 vec = moveToZero(box).getCenter();
		stack.translate(vec);
		stack.scale(scaleProg, scaleProg, scaleProg);
		if (horribleWorkaroundForEdges) {
			float yeah = BlockHighlightConfig.INSTANCE.instance().lineExpand * 2 + 1;
			stack.scale(yeah, yeah, yeah);
		}
		stack.translate(vec.reverse());
	}

	public static void drawBoxOutline(PoseStack stack, AABB box, Color color, Color col2, float[] alpha, int layer) {
		doEvilMatrixPreparations(stack, box, false);
		com.mojang.blaze3d.vertex.BufferBuilder buf = startDrawing(true);
		Vertexer.vertexBoxLines(stack, buf, moveToZero(box), color, col2, alpha, layer);
		draw(buf, true, layer);
		stack.popPose();
	}

	public static void drawEdgeOutline(PoseStack matrices, VoxelShape shape, Color c1, Color c2, float alpha, int layer) {
		doEvilMatrixPreparations(matrices, shape.bounds(), true);
		List<Line> newLines = new ArrayList<>();
		BufferBuilder buffer = startDrawing(true);
		moveToZero(shape).forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> newLines.add(new Line(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ))));
		Vec3 minVec = shape.bounds().getMinPosition();
		if (lines.isEmpty() || !BlockHighlightConfig.INSTANCE.instance().doEasing) {
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
		draw(buffer, true, layer);
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
		double rainbowState = Math.ceil((System.currentTimeMillis() + (int) (delay))) * BlockHighlightConfig.INSTANCE.instance().rainbowSpeed / 50;
		rainbowState %= 360;
		return Color.getHSBColor((float) (rainbowState / 360.0f), BlockHighlightConfig.INSTANCE.instance().saturation, BlockHighlightConfig.INSTANCE.instance().brightness);
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
		if (BlockHighlightConfig.INSTANCE.instance().allowLiquids && (mc.player.getMainHandItem().is(Items.BUCKET) || mc.player.getOffhandItem().is(Items.BUCKET))) {
			HitResult yeah = pick(mc.getCameraEntity(), mc.player.blockInteractionRange(), mc.getDeltaTracker().getRealtimeDeltaTicks(), true);
			if (yeah instanceof BlockHitResult hit) {
				if (mc.level.getFluidState(hit.getBlockPos()).isSource()) {
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
			} else if (evilHitResult instanceof EntityHitResult entityHitResult && BlockHighlightConfig.INSTANCE.instance().allowEntities) {
				Entity entity = entityHitResult.getEntity();
				//so, so sloppy. might also have the worst workaround of the century for hanging stuff
				float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
				targetBox = moveToZero(entity.getBoundingBox()).move(entity.getPosition(delta).subtract(moveToZero(entity.getBoundingBox()).getCenter()).add(0, entity instanceof HangingEntity ? 0 : moveToZero(entity.getBoundingBox()).maxY / 2F, 0));

			}
		}


		//get connected blocks
		if (BlockHighlightConfig.INSTANCE.instance().connectedBlocks && evilHitResult instanceof BlockHitResult block) {
			if (evilHitResult.getType() == HitResult.Type.MISS) {
				connected = null;
			} else {
				BlockState state = mc.level.getBlockState(block.getBlockPos());
				connected = joinConnected(state, block.getBlockPos());
			}
		}
		//calculate where to render the block
		if (BlockHighlightConfig.INSTANCE.instance().doEasing) {
			if (BlockHighlightConfig.INSTANCE.instance().updateWhenUnfocused || evilHitResult.getType() != HitResult.Type.MISS) {
				easeBox = new AABB(ease(easeBox.minX, targetBox.minX, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.minY, targetBox.minY, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.minZ, targetBox.minZ, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.maxX, targetBox.maxX, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.maxY, targetBox.maxY, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.maxZ, targetBox.maxZ, BlockHighlightConfig.INSTANCE.instance().easeSpeed));
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
			if (BlockHighlightConfig.INSTANCE.instance().fillEnabled) {
				drawFill(stack, isCrystalObstructed);
			}
			//now the outline itself
			if (BlockHighlightConfig.INSTANCE.instance().outlineEnabled) {
				drawOutline(stack, isCrystalObstructed);
			}
		}
	}

	private static boolean isCrystalObstructed() {
		if (mc.level == null) throw new IllegalStateException("level == null");
		if (!(evilHitResult instanceof BlockHitResult block)) return false;
		BlockState state = mc.level.getBlockState(block.getBlockPos());

		if (BlockHighlightConfig.INSTANCE.instance().crystalHelper) {
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
		Color finalFillCol = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperFillColor : BlockHighlightConfig.INSTANCE.instance().fillRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().fillCol;
		Color finalFillCol2 = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperFillColor : BlockHighlightConfig.INSTANCE.instance().fillRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().fillCol2;
		Renderer.drawBoxFill(stack, easeBox.inflate(BlockHighlightConfig.INSTANCE.instance().fillExpand), finalFillCol, finalFillCol2, sideFades);
	}

	private static void drawOutline(PoseStack stack, boolean isCrystalObstructed) {
		if (mc.level == null) throw new IllegalStateException("level == null");
		var cameraEntity = camera.entity();
		//TODO: make check so that cut from corner and cut from center do not add up to higher than 0.95

		Color finalLineCol = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().outlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().lineCol;
		Color finalLineCol2 = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().outlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().lineCol2;
		if (BlockHighlightConfig.INSTANCE.instance().outlineType == OutlineType.EDGES) {
			if (evilHitResult != null && evilHitResult.getType() != HitResult.Type.MISS) {
				if (evilHitResult instanceof BlockHitResult block) {
					if (isBlockOccupied(block.getBlockPos())) {
						shape = mc.level.getBlockState(block.getBlockPos()).getShape(mc.level, block.getBlockPos(), CollisionContext.of(cameraEntity));
						if (connected != null) {
							shape = Shapes.joinUnoptimized(shape, mc.level.getBlockState(block.getBlockPos().relative(connected)).getShape(mc.level, block.getBlockPos().relative(connected), CollisionContext.of(cameraEntity)).move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR).optimize();
						}
					}
				} else if (evilHitResult instanceof EntityHitResult entity && BlockHighlightConfig.INSTANCE.instance().allowEntities) {
					shape = Shapes.create(entity.getEntity().getBoundingBox());
				}
			}
			if (!shape.isEmpty()) {
				if (BlockHighlightConfig.INSTANCE.instance().tertiary) {
					Color tfinalLineCol = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().toutlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().tlineCol;
					Color tfinalLineCol2 = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().toutlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().tlineCol2;
					Renderer.drawEdgeOutline(stack, shape.move(easeBox.minX - shape.bounds().getMinPosition().x, easeBox.minY - shape.bounds().getMinPosition().y, easeBox.minZ - shape.bounds().getMinPosition().z), tfinalLineCol, tfinalLineCol2, edgeAlpha * BlockHighlightConfig.INSTANCE.instance().tlineAlphaMultiplier, 2);
				}
				if (BlockHighlightConfig.INSTANCE.instance().secondary) {
					Color sfinalLineCol = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().soutlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().slineCol;
					Color sfinalLineCol2 = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().soutlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().slineCol2;
					Renderer.drawEdgeOutline(stack, shape.move(easeBox.minX - shape.bounds().getMinPosition().x, easeBox.minY - shape.bounds().getMinPosition().y, easeBox.minZ - shape.bounds().getMinPosition().z), sfinalLineCol, sfinalLineCol2, edgeAlpha * BlockHighlightConfig.INSTANCE.instance().slineAlphaMultiplier, 1);
				}
				Renderer.drawEdgeOutline(stack, shape.move(easeBox.minX - shape.bounds().getMinPosition().x, easeBox.minY - shape.bounds().getMinPosition().y, easeBox.minZ - shape.bounds().getMinPosition().z), finalLineCol, finalLineCol2, edgeAlpha, 0);
			}
		} else {
			if (BlockHighlightConfig.INSTANCE.instance().tertiary) {
				Color tfinalLineCol = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().toutlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().tlineCol;
				Color tfinalLineCol2 = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().toutlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().tlineCol2;
				float[] newFades = new float[6];
				for (int i = 0; i < 6; i++) {
					newFades[i] = Mth.clamp(lineFades[i] * BlockHighlightConfig.INSTANCE.instance().tlineAlphaMultiplier, 0, 255F);
				}
				Renderer.drawBoxOutline(stack, easeBox.inflate(BlockHighlightConfig.INSTANCE.instance().lineExpand), tfinalLineCol, tfinalLineCol2, newFades, 2);
			}
			if (BlockHighlightConfig.INSTANCE.instance().secondary) {
				Color sfinalLineCol = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().soutlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().slineCol;
				Color sfinalLineCol2 = isCrystalObstructed ? BlockHighlightConfig.INSTANCE.instance().crystalHelperLineColor : BlockHighlightConfig.INSTANCE.instance().soutlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().slineCol2;
				float[] newFades = new float[6];
				for (int i = 0; i < 6; i++) {
					newFades[i] = Mth.clamp(lineFades[i] * BlockHighlightConfig.INSTANCE.instance().slineAlphaMultiplier, 0, 255F);
				}
				Renderer.drawBoxOutline(stack, easeBox.inflate(BlockHighlightConfig.INSTANCE.instance().lineExpand), sfinalLineCol, sfinalLineCol2, newFades, 1);
			}
			Renderer.drawBoxOutline(stack, easeBox.inflate(BlockHighlightConfig.INSTANCE.instance().lineExpand), finalLineCol, finalLineCol2, lineFades, 0);
		}
		// insert model data pulling render idk code here
	}

	private static void updateProgresses(boolean shouldFadeOut) {
		if (evilHitResult == null || mc.level == null) return;
		//TODO: clean this up later
		if (evilHitResult.getType() == HitResult.Type.ENTITY) {
			if (BlockHighlightConfig.INSTANCE.instance().allowEntities) {
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(sideFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.instance().fillOpacity, BlockHighlightConfig.INSTANCE.instance().fadeInSpeed) : BlockHighlightConfig.INSTANCE.instance().fillOpacity;
					lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(lineFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.instance().lineAlpha, BlockHighlightConfig.INSTANCE.instance().fadeInSpeed) : BlockHighlightConfig.INSTANCE.instance().lineAlpha;
				}
				edgeAlpha = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(edgeAlpha, BlockHighlightConfig.INSTANCE.instance().lineAlpha, BlockHighlightConfig.INSTANCE.instance().fadeInSpeed) : BlockHighlightConfig.INSTANCE.instance().lineAlpha;
			} else {
				shouldFadeOut = true;
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
					lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
				}
				edgeAlpha = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(edgeAlpha, 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
			}
		} else if (evilHitResult instanceof BlockHitResult block) {
			if ((mc.level.isEmptyBlock(block.getBlockPos()) || shouldFadeOut)) {
				for (Direction dir : Direction.values()) {
					sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
					lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
				}
				edgeAlpha = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(edgeAlpha, 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
			} else {
				edgeAlpha = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(edgeAlpha, BlockHighlightConfig.INSTANCE.instance().lineAlpha, BlockHighlightConfig.INSTANCE.instance().fadeInSpeed) : BlockHighlightConfig.INSTANCE.instance().lineAlpha;
				for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.instance().fillType, block.getBlockPos())) {
					if (dir != null) {
						sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(sideFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.instance().fillOpacity, BlockHighlightConfig.INSTANCE.instance().fadeInSpeed) : BlockHighlightConfig.INSTANCE.instance().fillOpacity;
					}
				}
				for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.instance().fillType, block.getBlockPos()))) {
					if (dir != null) {
						sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
					}
				}
				for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.instance().outlineType, block.getBlockPos())) {
					if (dir != null) {
						lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(lineFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.instance().lineAlpha, BlockHighlightConfig.INSTANCE.instance().fadeInSpeed) : BlockHighlightConfig.INSTANCE.instance().lineAlpha;
					}
				}
				for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.instance().outlineType, block.getBlockPos()))) {
					if (dir != null) {
						lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeOutSpeed) : 0;
					}
				}
			}
		}
		//I didn't add in/out because it would BREAKKK. TODO THIS
		scaleProg = BlockHighlightConfig.INSTANCE.instance().scale ? (float) ease(scaleProg, shouldFadeOut ? 0 : 1, BlockHighlightConfig.INSTANCE.instance().scaleSpeed) : 1;
	}

	public static HitResult pick(Entity e, final double range, final float a, final boolean withLiquids) {
		if (mc.level == null) return null;
		Vec3 from = e.getEyePosition(a);
		Vec3 viewVector = e.getViewVector(a);
		Vec3 to = from.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
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