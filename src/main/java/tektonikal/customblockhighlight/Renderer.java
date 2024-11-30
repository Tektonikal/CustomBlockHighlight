package tektonikal.customblockhighlight;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Unique;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.util.Line;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;

import static net.minecraft.block.enums.ChestType.SINGLE;


public class Renderer {
    @Unique
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    @Unique
    private static Box easeBox = new Box(0, 0, 0, 0, 0, 0);
    @Unique
    private static final float[] sideFades = new float[6];
    @Unique
    private static final float[] lineFades = new float[6];
    static Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
    public static ArrayList<Line> lines = new ArrayList<>();
    public static ArrayList<Line> toRemove = new ArrayList<>();
    public static BlockPos prevPos = new BlockPos(0, 0, 0);
    public static BlockPos tempPos = new BlockPos(0, 0, 0);

    public static void drawBoxFill(MatrixStack ms, Box box, Color cols, Color col2, float[] alpha) {
        ms.push();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        setup();
        if (BlockHighlightConfig.INSTANCE.getConfig().fillDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vertexer.vertexBoxQuads(ms, buffer, moveToZero(box), cols, col2, alpha);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end();
        ms.pop();
    }

    public static void drawBoxOutline(MatrixStack ms, Box box, Color color, Color col2, float[] alpha, float lineWidth) {
        ms.push();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        setup();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        if (BlockHighlightConfig.INSTANCE.getConfig().lineDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.lineWidth(lineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexBoxLines(ms, buffer, moveToZero(box), color, col2, alpha);
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        ms.pop();
    }

    public static void drawEdgeOutline(MatrixStack matrices, VoxelShape shape, Color c1, Color c2, float alpha, float lineWidth) {
        matrices.push();
        matrices.translate(shape.getBoundingBox().minX - camera.getPos().x, shape.getBoundingBox().minY - camera.getPos().y, shape.getBoundingBox().minZ - camera.getPos().z);
        setup();
        ArrayList<Line> newLines = new ArrayList<>();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        if (BlockHighlightConfig.INSTANCE.getConfig().lineDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.lineWidth(lineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        VoxelShape finalShape = shape;
        moveToZero(shape).forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> newLines.add(new Line(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), getMinVec(finalShape.getBoundingBox()))));
        if (lines.isEmpty()) {
            lines = newLines;
        }
        while (lines.size() < newLines.size()) {
//            if (!toRemove.isEmpty()) {
//                lines.add(toRemove.getFirst());
//                toRemove.removeFirst();
//            } else {
            lines.add(new Line(moveToZero(shape).getBoundingBox().getCenter(), moveToZero(shape).getBoundingBox().getCenter(), getMinVec(shape.getBoundingBox())));
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
        double blegh = moveToZero(shape).getBoundingBox().getMinPos().distanceTo(shape.getBoundingBox().getMaxPos());
        finalLines.forEach(line -> {
            line.update(true);
            line.render(matrices, buffer, getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha));
        });
        toRemove.removeIf(line -> line.alphaMultiplier < 0.0039);
        toRemove.forEach(line -> {
            line.update(false);
            line.render(matrices, buffer, getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha));
        });
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        matrices.pop();
    }

    public static Vec3d getMinVec(Box box) {
        return new Vec3d(box.minX, box.minY, box.minZ);
    }

    public static Box moveToZero(Box box) {
        return box.offset(getMinVec(box).negate());
    }

    public static VoxelShape moveToZero(VoxelShape shape) {
        return shape.offset(getMinVec(shape.getBoundingBox()).x * -1, getMinVec(shape.getBoundingBox()).y * -1, getMinVec(shape.getBoundingBox()).z * -1);
    }

    //TODO: clean up these calls, some of them are useless iirc
    public static void setup() {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
    }

    public static void end() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

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
                return new Direction[]{((BlockHitResult) mc.crosshairTarget).getSide()};
            }
            case AIR_EXPOSED -> {
                return invert(getAirDirs(pos));
            }
            case CONCEALED -> {
                return getAirDirs(pos);
            }
            default -> {
                return Direction.values();
            }
        }
    }

    @Unique
    private static Color getRainbowCol(int delay) {
        return getRainbow(((System.currentTimeMillis() + delay) % 10000L / 10000.0f) * BlockHighlightConfig.INSTANCE.getConfig().rainbowSpeed);
    }

    @Unique
    public static double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-(1.0F / mc.getCurrentFps()) * speed)));
    }

    @Unique
    public static Direction[] getAirDirs(BlockPos pos) {
        //future update todo: prevent blocks from detecting their own parts as obstructing air (maybe use gradients to show it?)
        //todo: fix fluid detection
        Direction[] dirs = new Direction[6];
        if (!mc.world.isAir(pos.up())) {
            dirs[0] = (Direction.UP);
        }
        if (!mc.world.isAir(pos.down())) {
            dirs[1] = (Direction.DOWN);
        }
        if (!mc.world.isAir(pos.north())) {
            dirs[2] = (Direction.NORTH);
        }
        if (!mc.world.isAir(pos.east())) {
            dirs[3] = (Direction.EAST);
        }
        if (!mc.world.isAir(pos.south())) {
            dirs[4] = (Direction.SOUTH);
        }
        if (!mc.world.isAir(pos.west())) {
            dirs[5] = (Direction.WEST);
        }
        return dirs;
    }

    //https://github.com/Splzh/ClearHitboxes/blob/main/src/main/java/splash/utils/ColorUtils.java !!
    @Unique
    private static Color getRainbow(double percent) {
        double offset = Math.PI * 2 / 3;
        double pos = percent * (Math.PI * 2);
        float red = (float) ((Math.sin(pos) * 127) + 128);
        float green = (float) ((Math.sin(pos + offset) * 127) + 128);
        float blue = (float) ((Math.sin(pos + offset * 2) * 127) + 128);
        return new Color((int) (red), (int) (green), (int) (blue), 255);
    }

    public static Color getLerpedColor(Color c1, Color c2, float percent) {
        return new Color(Math.clamp(MathHelper.lerp(percent, c1.getRed(), c2.getRed()), 0, 255), Math.clamp(MathHelper.lerp(percent, c1.getGreen(), c2.getGreen()), 0, 255), Math.clamp(MathHelper.lerp(percent, c1.getBlue(), c2.getBlue()), 0, 255));
    }

    public static boolean main(WorldRenderContext c, HitResult h) {
        checkForUpdate(h);
        if (h == null || h.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = ((BlockHitResult) h).getBlockPos();
        BlockState state = MinecraftClient.getInstance().world.getBlockState(pos);
        Box targetBox;
        try {
            targetBox = state.getOutlineShape(mc.world, pos).getBoundingBox().offset(pos);
        } catch (UnsupportedOperationException ex) {
            //if there is no actual outline, like for light blocks, just get a box around their coordinates.
            targetBox = new Box(((BlockHitResult) mc.crosshairTarget).getBlockPos());
        }
        //get connected blocks
        if (BlockHighlightConfig.INSTANCE.getConfig().connected) {
            if (state.getBlock() instanceof ChestBlock && !state.get(ChestBlock.CHEST_TYPE).equals(SINGLE)) {
                Direction facing = ChestBlock.getFacing(state);
                BlockPos tempPos = ((BlockHitResult) mc.crosshairTarget).getBlockPos().offset(facing);
                BlockState tempState = mc.world.getBlockState(tempPos);
                if (tempState.getBlock() instanceof ChestBlock) {
                    targetBox = targetBox.union(tempState.getOutlineShape(mc.world, tempPos).getBoundingBox().offset(tempPos));
                }
            }
            if (state.getBlock() instanceof DoorBlock) {
                DoubleBlockHalf half = state.get(DoorBlock.HALF);
                Direction otherDirection;
                if (half == DoubleBlockHalf.LOWER) {
                    otherDirection = Direction.UP;
                } else {
                    otherDirection = Direction.DOWN;
                }
                BlockPos otherPos = pos.offset(otherDirection);
                BlockState otherState = mc.world.getBlockState(otherPos);
                if (otherState.getBlock() instanceof DoorBlock && otherState.get(DoorBlock.HALF) != half) {
                    targetBox = targetBox.union(otherState.getOutlineShape(mc.world, otherPos).getBoundingBox().offset(otherPos));
                }
            }
            if (state.getBlock() instanceof BedBlock) {
                BedPart part = state.get(BedBlock.PART);
                Direction dir = state.get(HorizontalFacingBlock.FACING);
                if (part == BedPart.HEAD) {
                    dir = dir.getOpposite();
                }
                BlockPos oP = pos.offset(dir);
                BlockState other = mc.world.getBlockState(oP);
                if (other.getBlock() instanceof BedBlock && other.get(BedBlock.PART) != part) {
                    targetBox = targetBox.union(other.getOutlineShape(mc.world, oP).getBoundingBox().offset(oP));
                }
            }
            if (state.getBlock() instanceof TallPlantBlock) {
                DoubleBlockHalf half = state.get(TallPlantBlock.HALF);
                Direction direction;
                if (half == DoubleBlockHalf.LOWER) {
                    direction = Direction.UP;
                } else {
                    direction = Direction.DOWN;
                }
                BlockPos otherPos = pos.offset(direction);
                BlockState otherState = mc.world.getBlockState(otherPos);
                if (otherState.getBlock() instanceof TallPlantBlock) {
                    targetBox = targetBox.union(otherState.getOutlineShape(mc.world, otherPos).getBoundingBox().offset(otherPos));
                }
            }
        }
        boolean erm;
        if (BlockHighlightConfig.INSTANCE.getConfig().crystalHelper) {
            if (state.getBlock().equals(Blocks.OBSIDIAN) || state.getBlock().equals(Blocks.BEDROCK)) {
                double pd = pos.up().getX();
                double pe = pos.up().getY();
                double pf = pos.up().getZ();
                erm = !mc.world.isAir(pos.up()) || !mc.world.getOtherEntities(null, new Box(pd, pe, pf, pd + 1.0, pe + 2.0, pf + 1.0)).isEmpty();
            } else {
                erm = false;
            }
        } else {
            erm = false;
        }

        //calculate where to render the block
        if (BlockHighlightConfig.INSTANCE.getConfig().doEasing) {
            easeBox = new Box(ease(easeBox.minX, targetBox.minX, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.minY, targetBox.minY, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.minZ, targetBox.minZ, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.maxX, targetBox.maxX, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.maxY, targetBox.maxY, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.maxZ, targetBox.maxZ, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed));
        } else {
            easeBox = targetBox;
        }
        //render the fill first, we don't want it drawn over the outline
        if (BlockHighlightConfig.INSTANCE.getConfig().fillEnabled) {
            Color finalFillCol = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().fillRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.getConfig().fillCol;
            Color finalFillCol2 = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().fillRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.getConfig().delay) : BlockHighlightConfig.INSTANCE.getConfig().fillCol2;
            Renderer.drawBoxFill(c.matrixStack(), easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().fillExpand), finalFillCol, finalFillCol2, sideFades);
            if (BlockHighlightConfig.INSTANCE.getConfig().fadeIn) {
                for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.getConfig().fillType, pos)) {
                    if (dir != null) {
                        sideFades[dir.ordinal()] = (float) ease(sideFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.getConfig().fillOpacity, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed);
                    }
                }
            } else {
                for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.getConfig().fillType, pos)) {
                    if (dir != null) {
                        sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().fillOpacity;
                    }
                }
            }
            if (BlockHighlightConfig.INSTANCE.getConfig().fadeOut) {
                for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.getConfig().fillType, pos))) {
                    if (dir != null) {
                        sideFades[dir.ordinal()] = (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed);
                    }
                }
            } else {
                for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.getConfig().fillType, pos))) {
                    if (dir != null) {
                        sideFades[dir.ordinal()] = 0;
                    }
                }
            }
        }
        //now the outline itself
        if (BlockHighlightConfig.INSTANCE.getConfig().outlineEnabled) {

            Color finalLineCol = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().outlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.getConfig().lineCol;
            Color finalLineCol2 = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().outlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.getConfig().delay) : BlockHighlightConfig.INSTANCE.getConfig().lineCol2;

            if (BlockHighlightConfig.INSTANCE.getConfig().outlineType == OutlineType.EDGES) {
                VoxelShape s = state.getOutlineShape(c.world(), pos, ShapeContext.of(c.camera().getFocusedEntity()));
                Renderer.drawEdgeOutline(c.matrixStack(), s.offset(easeBox.minX - s.getBoundingBox().getMinPos().x, easeBox.minY - s.getBoundingBox().getMinPos().y, easeBox.minZ - s.getBoundingBox().getMinPos().z), finalLineCol, finalLineCol2, BlockHighlightConfig.INSTANCE.getConfig().lineAlpha, BlockHighlightConfig.INSTANCE.getConfig().lineWidth);
            } else {
                Renderer.drawBoxOutline(c.matrixStack(), easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().lineExpand), finalLineCol, finalLineCol2, lineFades, BlockHighlightConfig.INSTANCE.getConfig().lineWidth);
                Renderer.drawBoxOutline(c.matrixStack(), new Box(tempPos), finalLineCol, finalLineCol2, lineFades, BlockHighlightConfig.INSTANCE.getConfig().lineWidth);
                if (BlockHighlightConfig.INSTANCE.getConfig().fadeIn) {
                    for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.getConfig().outlineType, pos)) {
                        if (dir != null) {
                            lineFades[dir.ordinal()] = (float) ease(lineFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.getConfig().lineAlpha, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed);
                        }
                    }
                } else {
                    for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.getConfig().outlineType, pos)) {
                        if (dir != null) {
                            lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().lineAlpha;
                        }
                    }
                }
                if (BlockHighlightConfig.INSTANCE.getConfig().fadeOut) {
                    for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.getConfig().outlineType, pos))) {
                        if (dir != null) {
                            lineFades[dir.ordinal()] = (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed);
                        }
                    }
                } else {
                    for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.getConfig().outlineType, pos))) {
                        if (dir != null) {
                            lineFades[dir.ordinal()] = 0;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static ActionResult update(BlockPos blockPos, BlockPos blockPos1) {
        return ActionResult.PASS;
    }

    public static void checkForUpdate(HitResult h) {
        if (h == null) {
            if (prevPos != null) {
                tempPos = prevPos;
                prevPos = null;
            }
        } else {
            if(prevPos == null){
                tempPos = prevPos;
                prevPos = ((BlockHitResult)h).getBlockPos();
            }
            if (!prevPos.equals(((BlockHitResult)h).getBlockPos())) {
                tempPos = prevPos;
                prevPos = ((BlockHitResult)h).getBlockPos();
//                BlockTargetCallback.EVENT.invoker().interact(prevPos, ((BlockHitResult)h).getBlockPos());
            }
        }
    }
}