package tektonikal.customblockhighlight;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Unique;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.util.Line;

import java.awt.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.*;

import static net.minecraft.world.level.block.state.properties.ChestType.SINGLE;

public class Renderer {
	@Unique
	private static final Minecraft mc = Minecraft.getInstance();
	static final Camera camera = mc.gameRenderer.mainCamera();
	@Unique
	private static final float[] sideFades = new float[6];
	@Unique
	private static final float[] lineFades = new float[6];
	public static ArrayList<Line> lines = new ArrayList<>();
	public static ArrayList<Line> toRemove = new ArrayList<>();
	public static BlockPos prevPos = new BlockPos(0, 0, 0);
	public static BlockPos tempPos = new BlockPos(0, 0, 0);
	public static BlockPos pos = new BlockPos(0, 0, 0);
	public static VoxelShape s = Shapes.block();
	public static AABB targetBox = new AABB(pos);
	public static Direction connected = null;
	public static float edgeAlpha = 0;
	@Unique
	private static AABB easeBox = new AABB(0, 0, 0, 0, 0, 0);
	private static final RenderPipeline OUTLINE_THROUGH_WALLS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", "pipeline/evil-lines"))
					.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
					.withCull(false)
					.build()
	);
	private static final RenderPipeline FILL_NO_DEPTH = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(Identifier.fromNamespaceAndPath("custom-block-highlight", "pipeline/evil-fill"))
					.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
					.withCull(false)
					.build()
	);
	private static final StagedVertexBuffer stagedFaceBuffer = new StagedVertexBuffer(() -> " CBH sides", RenderType.SMALL_BUFFER_SIZE);
	private static final StagedVertexBuffer stagedOutlineBuffer = new StagedVertexBuffer(() -> " CBH outline", RenderType.SMALL_BUFFER_SIZE);

	public static StagedVertexBuffer.Draw startDrawing(boolean lines) {
		if (lines) {
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
		}
		StagedVertexBuffer.Draw draw;
		if (lines) {
			draw = stagedOutlineBuffer.appendDraw(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, PrimitiveTopology.LINES);
		} else {
			draw = stagedFaceBuffer.appendDraw(DefaultVertexFormat.POSITION_COLOR, PrimitiveTopology.QUADS, RenderSystem.getProjectionType().vertexSorting());
		}
		return draw;
	}


	private static void yeah(boolean lines, StagedVertexBuffer.Draw draw) {
		stagedOutlineBuffer.upload();
		stagedFaceBuffer.upload();
		StagedVertexBuffer.ExecuteInfo info = lines ? stagedOutlineBuffer.getExecuteInfo(draw) : stagedFaceBuffer.getExecuteInfo(draw);
		if (info == null) {
			return;
		}
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f());
		RenderTarget mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTextureView colorTexture = mainTarget.getColorTextureView();
		assert colorTexture != null;
		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "CBH pass", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
			if (lines) {
				renderPass.setPipeline(BlockHighlightConfig.INSTANCE.instance().lineDepthTest ? RenderPipelines.LINES : OUTLINE_THROUGH_WALLS);
			} else {
				renderPass.setPipeline(BlockHighlightConfig.INSTANCE.instance().fillDepthTest ? RenderPipelines.DEBUG_QUADS : FILL_NO_DEPTH);
			}

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
			renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
			renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
		}
		stagedFaceBuffer.endFrame();
		stagedOutlineBuffer.endFrame();
	}

	public static void drawBoxFill(PoseStack ms, AABB box, Color cols, Color col2, float[] alpha) {
		ms.pushPose();
		ms.translate(box.minX - camera.position().x, box.minY - camera.position().y, box.minZ - camera.position().z);
		StagedVertexBuffer.Draw draw = startDrawing(false);
		VertexConsumer buffer = stagedFaceBuffer.getVertexBuilder(draw);
		Vertexer.vertexBoxQuads(ms, buffer, moveToZero(box), cols, col2, alpha);
		yeah(false, draw);
		ms.popPose();
	}

	public static void drawBoxOutline(PoseStack ms, AABB box, Color color, Color col2, float[] alpha) {
		ms.pushPose();
		StagedVertexBuffer.Draw draw = startDrawing(true);
		ms.translate(box.minX - camera.position().x, box.minY - camera.position().y, box.minZ - camera.position().z);
		VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);
		Vertexer.vertexBoxLines(ms, buffer, moveToZero(box), color, col2, alpha);
		yeah(true, draw);
//        endDrawing(buffer, true);
		ms.popPose();
	}

	public static void drawEdgeOutline(PoseStack matrices, VoxelShape shape, Color c1, Color c2, float alpha) {
		matrices.pushPose();
		matrices.translate(shape.bounds().minX - camera.position().x, shape.bounds().minY - camera.position().y, shape.bounds().minZ - camera.position().z);
		ArrayList<Line> newLines = new ArrayList<>();
		StagedVertexBuffer.Draw draw = startDrawing(true);
		VertexConsumer buffer = stagedOutlineBuffer.getVertexBuilder(draw);
		VoxelShape finalShape = shape;
		moveToZero(shape).forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> newLines.add(new Line(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ), getMinVec(finalShape.bounds()))));
		if (lines.isEmpty()) {
			lines = newLines;
		}
		while (lines.size() < newLines.size()) {
//            if (!toRemove.isEmpty()) {
//                lines.add(toRemove.getFirst());
//                toRemove.removeFirst();
//            } else {
			lines.add(new Line(moveToZero(shape).bounds().getCenter(), moveToZero(shape).bounds().getCenter(), getMinVec(shape.bounds())));
//            }
		}
		while (lines.size() > newLines.size()) {
			toRemove.add(lines.getLast());
			lines.removeLast();
		}
		for (int i = 0; i < lines.size(); i++) {
			lines.get(i).moveTo(newLines.get(i).minPos, newLines.get(i).maxPos, newLines.get(i).minVec);
		}
		ArrayList<Line> finalLines = new ArrayList<>(lines);
		finalLines.sort(Comparator.comparing(Line::getDistanceToCamera).reversed());
		shape = moveToZero(shape);
		VoxelShape finalShape1 = shape;
		double blegh = moveToZero(shape).bounds().getMinPosition().distanceTo(shape.bounds().getMaxPosition());
		finalLines.forEach(line -> line.updateAndRender(matrices, buffer, getLerpedColor(c1, c2, (float) (finalShape1.bounds().getMinPosition().distanceTo(new Vec3(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (finalShape1.bounds().getMinPosition().distanceTo(new Vec3(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha), true));
		toRemove.removeIf(line -> line.alphaMultiplier < 0.0039);
		toRemove.forEach(line -> line.updateAndRender(matrices, buffer, getLerpedColor(c1, c2, (float) (finalShape1.bounds().getMinPosition().distanceTo(new Vec3(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (finalShape1.bounds().getMinPosition().distanceTo(new Vec3(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha), false));
		yeah(true, draw);
//        endDrawing(buffer, true);
		matrices.popPose();
	}

	public static Vec3 getMinVec(AABB box) {
		return new Vec3(box.minX, box.minY, box.minZ);
	}

	public static AABB moveToZero(AABB box) {
		return box.move(getMinVec(box).reverse());
	}

	public static VoxelShape moveToZero(VoxelShape shape) {
		return shape.move(getMinVec(shape.bounds()).x * -1, getMinVec(shape.bounds()).y * -1, getMinVec(shape.bounds()).z * -1);
	}

	//TODO: clean up these calls, some of them are useless iirc
//    public static void setup() {
//        RenderSystem.depthMask(false);
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.disableCull();
//    }
//
//    public static void end() {
//        RenderSystem.depthMask(true);
//        RenderSystem.enableDepthTest();
//        RenderSystem.disableBlend();
//        RenderSystem.enableCull();
//    }

	@Unique
	private static Direction[] invert(Direction[] invertDirs) {
		EnumSet<Direction> dirs = EnumSet.allOf(Direction.class);
		for (Direction d : invertDirs) {
			dirs.remove(d);
		}
		return dirs.toArray(Direction[]::new);
	}

	@Unique
	private static Direction[] getSides(OutlineType type, BlockPos pos) {
		switch (type) {
			case LOOKAT -> {
				return new Direction[]{((BlockHitResult) mc.hitResult).getDirection()};
			}
			case AIR_EXPOSED -> {
				return invert(getConcealedFaces(pos));
			}
			case CONCEALED -> {
				return getConcealedFaces(pos);
			}
			default -> {
				return Direction.values();
			}
		}
	}

	@Unique
	public static Color getRainbowCol(float delay) {
		double rainbowState = Math.ceil((System.currentTimeMillis() + (int) (delay))) * BlockHighlightConfig.INSTANCE.instance().rainbowSpeed / 50;
		rainbowState %= 360;
		return Color.getHSBColor((float) (rainbowState / 360.0f), BlockHighlightConfig.INSTANCE.instance().saturation, BlockHighlightConfig.INSTANCE.instance().brightness);
	}

	@Unique
	public static double ease(double start, double end, float speed) {
		return (start + (end - start) * (1 - Math.exp(-(1.0F / mc.getFps()) * speed)));
	}

	public static boolean isBlockOccupied(BlockPos pos) {
		if (mc.level.getBlockState(pos).hasProperty(BlockStateProperties.WATERLOGGED) && !mc.level.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED) && !mc.level.getFluidState(pos).isEmpty()) {
			//ignore liquids
			return false;
		}
		return !mc.level.isEmptyBlock(pos);
	}

	@Unique
	public static Direction[] getConcealedFaces(BlockPos pos) {
        /*
        I don't know if I should keep the original behaviour for this
        As of now, this method means that even when rendering the box for a block with multiple parts,
        it will still cull faces relative to the selected block, and not the entire rendered selection
         */
		Direction[] dirs = new Direction[6];
		if (isBlockOccupied(pos.above())) {
			dirs[0] = (Direction.UP);
		}
		if (isBlockOccupied(pos.below())) {
			dirs[1] = (Direction.DOWN);
		}
		if (isBlockOccupied(pos.north())) {
			dirs[2] = (Direction.NORTH);
		}
		if (isBlockOccupied(pos.east())) {
			dirs[3] = (Direction.EAST);
		}
		if (isBlockOccupied(pos.south())) {
			dirs[4] = (Direction.SOUTH);
		}
		if (isBlockOccupied(pos.west())) {
			dirs[5] = (Direction.WEST);
		}
		return dirs;
	}


	public static Color getLerpedColor(Color c1, Color c2, float percent) {
		return new Color(Math.clamp(Mth.lerpInt(percent, c1.getRed(), c2.getRed()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getGreen(), c2.getGreen()), 0, 255), Math.clamp(Mth.lerpInt(percent, c1.getBlue(), c2.getBlue()), 0, 255));
	}

	public static boolean yeah(Class<? extends BlockBehaviour> clazz) {
		Class<?>[] targetParams = {BlockState.class, Level.class, BlockPos.class, Player.class, BlockHitResult.class};

		Method foundMethod = Arrays.stream(clazz.getDeclaredMethods())
				.filter(method -> Arrays.equals(method.getParameterTypes(), targetParams))
				.findFirst()
				.orElse(null);

		if (foundMethod != null) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("SameReturnValue")
	public static void mainLoop(LevelRenderContext c) {
		HitResult h = Minecraft.getInstance().hitResult;
        /*
        TODO: better fluid logic? just to spite microcontrollers?
        - Is player holding water bucket?
            - Is player holding water bucket in offhand?
                - Can mainhand item be placed?
                    - Is player looking at something that can be used?
                        - Is player sneaking?
         */
		BlockState state = mc.level.getBlockState(pos);
		boolean erm = isCrystalObstructed(state);
		try {
			targetBox = state.getShape(mc.level, pos).bounds().move(pos);
		} catch (UnsupportedOperationException ex) {
			//if there is no actual outline, like for light blocks, just get a box around their coordinates.
			targetBox = new AABB((((BlockHitResult) h).getBlockPos()));
		}
		//get connected blocks
		if (BlockHighlightConfig.INSTANCE.instance().connectedBlocks) {
			connected = joinConnected(state, pos);
		}
		//calculate where to render the block
		if (BlockHighlightConfig.INSTANCE.instance().doEasing) {
			easeBox = new AABB(ease(easeBox.minX, targetBox.minX, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.minY, targetBox.minY, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.minZ, targetBox.minZ, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.maxX, targetBox.maxX, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.maxY, targetBox.maxY, BlockHighlightConfig.INSTANCE.instance().easeSpeed), ease(easeBox.maxZ, targetBox.maxZ, BlockHighlightConfig.INSTANCE.instance().easeSpeed));
		} else {
			easeBox = targetBox;
		}
		if (h.getType() != HitResult.Type.BLOCK) {
			renderBlockOutline(c.poseStack(), erm, state, true);
			return;
		}
		checkForUpdate(h);
		pos = ((BlockHitResult) h).getBlockPos();
		//fade out without updating position if we start looking at air
		renderBlockOutline(c.poseStack(), erm, state, false);
	}

	private static void renderBlockOutline(PoseStack ms, boolean erm, BlockState state, boolean shouldFade) {
		//render the fill first, we don't want it drawn over the outline
		updateFades(shouldFade);
		if (BlockHighlightConfig.INSTANCE.instance().fillEnabled) {
			drawFill(ms, erm);
		}
		//now the outline itself
		if (BlockHighlightConfig.INSTANCE.instance().outlineEnabled) {
			drawOutline(ms, erm, state);
		}
	}

	private static boolean isCrystalObstructed(BlockState state) {
		if (BlockHighlightConfig.INSTANCE.instance().crystalHelper) {
			if (state.getBlock().equals(Blocks.OBSIDIAN) || state.getBlock().equals(Blocks.BEDROCK)) {
				double pd = pos.above().getX();
				double pe = pos.above().getY();
				double pf = pos.above().getZ();
				return !mc.level.isEmptyBlock(pos.above()) || !mc.level.getEntities(null, new AABB(pd, pe, pf, pd + 1.0, pe + 2.0, pf + 1.0)).isEmpty();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private static void drawFill(PoseStack ms, boolean erm) {
		Color finalFillCol = erm ? BlockHighlightConfig.INSTANCE.instance().crystalHelperColor : BlockHighlightConfig.INSTANCE.instance().fillRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().fillCol;
		Color finalFillCol2 = erm ? BlockHighlightConfig.INSTANCE.instance().crystalHelperColor : BlockHighlightConfig.INSTANCE.instance().fillRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().fillCol2;
		Renderer.drawBoxFill(ms, easeBox.inflate(BlockHighlightConfig.INSTANCE.instance().fillExpand), finalFillCol, finalFillCol2, sideFades);
	}

	private static void drawOutline(PoseStack ms, boolean erm, BlockState state) {
		Color finalLineCol = erm ? BlockHighlightConfig.INSTANCE.instance().crystalHelperColor : BlockHighlightConfig.INSTANCE.instance().outlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.instance().lineCol;
		Color finalLineCol2 = erm ? BlockHighlightConfig.INSTANCE.instance().crystalHelperColor : BlockHighlightConfig.INSTANCE.instance().outlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.instance().delay) : BlockHighlightConfig.INSTANCE.instance().lineCol2;
		if (BlockHighlightConfig.INSTANCE.instance().outlineType == OutlineType.EDGES) {
			if (isBlockOccupied(pos)) {
				s = state.getShape(mc.level, pos, CollisionContext.of(camera.entity()));
				if (connected != null) {
					s = Shapes.joinUnoptimized(s, mc.level.getBlockState(pos.relative(connected)).getShape(mc.level, pos.relative(connected), CollisionContext.of(camera.entity())).move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR).optimize();
				}
			}
			if (!s.isEmpty()) {
				Renderer.drawEdgeOutline(ms, s.move(easeBox.minX - s.bounds().getMinPosition().x, easeBox.minY - s.bounds().getMinPosition().y, easeBox.minZ - s.bounds().getMinPosition().z), finalLineCol, finalLineCol2, edgeAlpha);
			}
		} else {
			Renderer.drawBoxOutline(ms, easeBox.inflate(BlockHighlightConfig.INSTANCE.instance().lineExpand), finalLineCol, finalLineCol2, lineFades);
		}
//        BakedModel b = mc.getBlockRenderManager().getModel(state);
//        b.getQuads(state, Direction.UP, Random.create()).forEach(bakedQuad -> bakedQuad.getVertexData());
	}

	private static void updateFades(boolean shouldFade) {
		//clean this up later
		if (mc.level.isEmptyBlock(pos) || shouldFade) {
			for (Direction dir : Direction.values()) {
				sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : 0;
				lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : 0;
			}
			edgeAlpha = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(edgeAlpha, 0, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : 0;
		} else {
			edgeAlpha = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(edgeAlpha, BlockHighlightConfig.INSTANCE.instance().lineAlpha, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : BlockHighlightConfig.INSTANCE.instance().lineAlpha;
			for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.instance().fillType, pos)) {
				if (dir != null) {
					sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(sideFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.instance().fillOpacity, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : BlockHighlightConfig.INSTANCE.instance().fillOpacity;
				}
			}
			for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.instance().fillType, pos))) {
				if (dir != null) {
					sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : 0;
				}
			}
			for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.instance().outlineType, pos)) {
				if (dir != null) {
					lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeIn ? (float) ease(lineFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.instance().lineAlpha, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : BlockHighlightConfig.INSTANCE.instance().lineAlpha;
				}
			}
			for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.instance().outlineType, pos))) {
				if (dir != null) {
					lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.instance().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.instance().fadeSpeed) : 0;
				}
			}
		}
	}

	private static Direction joinConnected(BlockState state, BlockPos pos) {
		BlockState connectedState;
		Direction dir;
		BlockPos connectedPos;
		DoubleBlockHalf half;
		if (state.getBlock() instanceof ChestBlock && !state.getValue(ChestBlock.TYPE).equals(SINGLE)) {
			dir = ChestBlock.getConnectedDirection(state);
			connectedPos = ((BlockHitResult) mc.hitResult).getBlockPos().relative(dir);
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


	@SuppressWarnings("SameReturnValue")
	public static InteractionResult update(BlockPos prev, BlockPos curr) {
//        if (!mc.world.isAir(blockPos1)) {
//            System.out.println("!!!!!!!!!");
//        }
		return InteractionResult.PASS;
	}

	public static void checkForUpdate(HitResult h) {
		if (h == null) {
			if (prevPos != null) {
				tempPos = prevPos;
				prevPos = null;
			}
		} else {
			if (prevPos == null) {
				tempPos = null;
				prevPos = ((BlockHitResult) h).getBlockPos();
			}
			if (!prevPos.equals(((BlockHitResult) h).getBlockPos())) {
				tempPos = prevPos;
				prevPos = ((BlockHitResult) h).getBlockPos();
				BlockTargetCallback.EVENT.invoker().interact(prevPos, ((BlockHitResult) h).getBlockPos());
			}
		}
	}
//    public static void quad(
//            MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float f, int[] is, int i, boolean bl
//    ) {
//        int[] js = quad.getVertexData();
//        Vec3i vec3i = quad.getFace().getVector();
//        Matrix4f matrix4f = matrixEntry.getPositionMatrix();
//        Vector3f vector3f = matrixEntry.transformNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), new Vector3f());
//        int j = 8;
//        int k = js.length / 8;
//        int l = (int)(f * 255.0F);
//
//        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
//            ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte());
//            IntBuffer intBuffer = byteBuffer.asIntBuffer();
//
//            for (int m = 0; m < k; m++) {
//                intBuffer.clear();
//                intBuffer.put(js, m * 8, 8);
//                float g = byteBuffer.getFloat(0);
//                float h = byteBuffer.getFloat(4);
//                float n = byteBuffer.getFloat(8);
//                float r;
//                float s;
//                float t;
//                if (bl) {
//                    float o = (float)(byteBuffer.get(12) & 255);
//                    float p = (float)(byteBuffer.get(13) & 255);
//                    float q = (float)(byteBuffer.get(14) & 255);
//                    r = o * brightnesses[m] * red;
//                    s = p * brightnesses[m] * green;
//                    t = q * brightnesses[m] * blue;
//                } else {
//                    r = brightnesses[m] * red * 255.0F;
//                    s = brightnesses[m] * green * 255.0F;
//                    t = brightnesses[m] * blue * 255.0F;
//                }
//
//                int u = ColorHelper.Argb.getArgb(l, (int)r, (int)s, (int)t);
//                int v = is[m];
//                float q = byteBuffer.getFloat(16);
//                float w = byteBuffer.getFloat(20);
//                Vector3f vector3f2 = matrix4f.transformPosition(g, h, n, new Vector3f());
//                this.vertex(vector3f2.x(), vector3f2.y(), vector3f2.z(), u, q, w, i, v, vector3f.x(), vector3f.y(), vector3f.z());
//            }
//        }
//    }
}