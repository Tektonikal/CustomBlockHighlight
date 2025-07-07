package tektonikal.customblockhighlight;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
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
    static final Camera camera = mc.gameRenderer.getCamera();
    @Unique
    private static final float[] sideFades = new float[6];
    @Unique
    private static final float[] lineFades = new float[6];
    public static ArrayList<Line> lines = new ArrayList<>();
    public static ArrayList<Line> toRemove = new ArrayList<>();
    public static BlockPos prevPos = new BlockPos(0, 0, 0);
    public static BlockPos tempPos = new BlockPos(0, 0, 0);
    public static BlockPos pos = new BlockPos(0, 0, 0);
    public static VoxelShape s = VoxelShapes.fullCube();
    public static Box targetBox = new Box(pos);
    public static Direction connected = null;
    public static float edgeAlpha = 0;
    @Unique
    private static Box easeBox = new Box(0, 0, 0, 0, 0, 0);

    public static BufferBuilder startDrawing(boolean lines) {
        setup();
        if (lines) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.lineWidth(BlockHighlightConfig.INSTANCE.getConfig().lineWidth);
        }
        if (lines ? BlockHighlightConfig.INSTANCE.getConfig().lineDepthTest : BlockHighlightConfig.INSTANCE.getConfig().fillDepthTest) {

            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        return Tessellator.getInstance().begin(lines ? VertexFormat.DrawMode.LINES : VertexFormat.DrawMode.QUADS, lines ? VertexFormats.LINES : VertexFormats.POSITION_COLOR);
    }

    public static void endDrawing(BufferBuilder buffer, boolean lines) {
        RenderSystem.setShader(lines ? ShaderProgramKeys.RENDERTYPE_LINES : ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        end();
    }

    public static void drawBoxFill(MatrixStack ms, Box box, Color cols, Color col2, float[] alpha) {
        ms.push();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        BufferBuilder buffer = startDrawing(false);
        Vertexer.vertexBoxQuads(ms, buffer, moveToZero(box), cols, col2, alpha);
        endDrawing(buffer, false);
        ms.pop();
    }

    public static void drawBoxOutline(MatrixStack ms, Box box, Color color, Color col2, float[] alpha) {
        ms.push();
        ms.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);
        BufferBuilder buffer = startDrawing(true);
        Vertexer.vertexBoxLines(ms, buffer, moveToZero(box), color, col2, alpha);
        endDrawing(buffer, true);
        ms.pop();
    }

    public static void drawEdgeOutline(MatrixStack matrices, VoxelShape shape, Color c1, Color c2, float alpha) {
        matrices.push();
        matrices.translate(shape.getBoundingBox().minX - camera.getPos().x, shape.getBoundingBox().minY - camera.getPos().y, shape.getBoundingBox().minZ - camera.getPos().z);
        ArrayList<Line> newLines = new ArrayList<>();
        BufferBuilder buffer = startDrawing(true);
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
        finalLines.forEach(line -> line.updateAndRender(matrices, buffer, getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha), true));
        toRemove.removeIf(line -> line.alphaMultiplier < 0.0039);
        toRemove.forEach(line -> line.updateAndRender(matrices, buffer, getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.minPos.x, line.minPos.y, line.minPos.z)) / blegh)), getLerpedColor(c1, c2, (float) (finalShape1.getBoundingBox().getMinPos().distanceTo(new Vec3d(line.maxPos.x, line.maxPos.y, line.maxPos.z)) / blegh)), Math.round(alpha), false));
        endDrawing(buffer, true);
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
    private static Color getRainbowCol(int delay) {
        return getRainbow(((System.currentTimeMillis() + delay) % 10000L / 10000.0f) * BlockHighlightConfig.INSTANCE.getConfig().rainbowSpeed);
    }

    @Unique
    public static double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-(1.0F / mc.getCurrentFps()) * speed)));
    }

    public static boolean isBlockOccupied(BlockPos pos) {
        if (mc.world.getBlockState(pos).contains(Properties.WATERLOGGED) && !mc.world.getBlockState(pos).get(Properties.WATERLOGGED) && !mc.world.getFluidState(pos).isEmpty()) {
            //ignore liquids
            return false;
        }
        return !mc.world.isAir(pos);
    }

    @Unique
    public static Direction[] getConcealedFaces(BlockPos pos) {
        /*
        I don't know if I should keep the original behaviour for this
        As of now, this method means that even when rendering the box for a block with multiple parts,
        it will still cull faces relative to the selected block, and not the entire rendered selection
         */
        Direction[] dirs = new Direction[6];
        if (isBlockOccupied(pos.up())) {
            dirs[0] = (Direction.UP);
        }
        if (isBlockOccupied(pos.down())) {
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

    @SuppressWarnings("SameReturnValue")
    public static boolean mainLoop(WorldRenderContext c, HitResult h) {
        //TODO: water and clouds take priority over outline rendering? i don't like it
        if(!(h instanceof BlockHitResult)){
            return false;
        }
        checkForUpdate(h);
        pos = ((BlockHitResult) h).getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        boolean erm = isCrystalObstructed(state);
        //fade out without updating position if we start looking at air
        if (h.getType() != HitResult.Type.BLOCK) {
            updateFades();
            renderBlockOutline(c.matrixStack(), erm, state);
            return false;
        }
        try {
            targetBox = state.getOutlineShape(mc.world, pos).getBoundingBox().offset(pos);
        } catch (UnsupportedOperationException ex) {
            //if there is no actual outline, like for light blocks, just get a box around their coordinates.
            targetBox = new Box(((BlockHitResult) mc.crosshairTarget).getBlockPos());
        }
        //get connected blocks
        if (BlockHighlightConfig.INSTANCE.getConfig().connectedBlocks) {
            connected = joinConnected(state, pos);
        }
        //calculate where to render the block
        if (BlockHighlightConfig.INSTANCE.getConfig().doEasing) {
            easeBox = new Box(ease(easeBox.minX, targetBox.minX, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.minY, targetBox.minY, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.minZ, targetBox.minZ, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.maxX, targetBox.maxX, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.maxY, targetBox.maxY, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed), ease(easeBox.maxZ, targetBox.maxZ, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed));
        } else {
            easeBox = targetBox;
        }
        renderBlockOutline(c.matrixStack(), erm, state);
        return false;
    }

    private static void renderBlockOutline(MatrixStack ms, boolean erm, BlockState state) {
        //render the fill first, we don't want it drawn over the outline
        updateFades();
        if (BlockHighlightConfig.INSTANCE.getConfig().fillEnabled) {
            drawFill(ms, erm);
        }
        //now the outline itself
        if (BlockHighlightConfig.INSTANCE.getConfig().outlineEnabled) {
            drawOutline(ms, erm, state);
        }
    }

    private static boolean isCrystalObstructed(BlockState state) {
        if (BlockHighlightConfig.INSTANCE.getConfig().crystalHelper) {
            if (state.getBlock().equals(Blocks.OBSIDIAN) || state.getBlock().equals(Blocks.BEDROCK)) {
                double pd = pos.up().getX();
                double pe = pos.up().getY();
                double pf = pos.up().getZ();
                return !mc.world.isAir(pos.up()) || !mc.world.getOtherEntities(null, new Box(pd, pe, pf, pd + 1.0, pe + 2.0, pf + 1.0)).isEmpty();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static void drawFill(MatrixStack ms, boolean erm) {
        Color finalFillCol = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().fillRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.getConfig().fillCol;
        Color finalFillCol2 = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().fillRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.getConfig().delay) : BlockHighlightConfig.INSTANCE.getConfig().fillCol2;
        Renderer.drawBoxFill(ms, easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().fillExpand), finalFillCol, finalFillCol2, sideFades);
    }

    private static void drawOutline(MatrixStack ms, boolean erm, BlockState state) {
        Color finalLineCol = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().outlineRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.getConfig().lineCol;
        Color finalLineCol2 = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().outlineRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.getConfig().delay) : BlockHighlightConfig.INSTANCE.getConfig().lineCol2;
        if (BlockHighlightConfig.INSTANCE.getConfig().outlineType == OutlineType.EDGES) {
            if (isBlockOccupied(pos)) {
                s = state.getOutlineShape(mc.world, pos, ShapeContext.of(camera.getFocusedEntity()));
                if (connected != null) {
                    s = VoxelShapes.combine(s, mc.world.getBlockState(pos.offset(connected)).getOutlineShape(mc.world, pos.offset(connected), ShapeContext.of(camera.getFocusedEntity())).offset(connected.getOffsetX(), connected.getOffsetY(), connected.getOffsetZ()), BooleanBiFunction.OR).simplify();
                }
            }
            Renderer.drawEdgeOutline(ms, s.offset(easeBox.minX - s.getBoundingBox().getMinPos().x, easeBox.minY - s.getBoundingBox().getMinPos().y, easeBox.minZ - s.getBoundingBox().getMinPos().z), finalLineCol, finalLineCol2, edgeAlpha);
        } else {
            Renderer.drawBoxOutline(ms, easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().lineExpand), finalLineCol, finalLineCol2, lineFades);
        }
//        BakedModel b = mc.getBlockRenderManager().getModel(state);
//        b.getQuads(state, Direction.UP, Random.create()).forEach(bakedQuad -> bakedQuad.getVertexData());
    }

    private static void updateFades() {
        //clean this up later
        if (mc.world.isAir(pos)) {
            for (Direction dir : Direction.values()) {
                sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : 0;
                lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : 0;
            }
            edgeAlpha = BlockHighlightConfig.INSTANCE.getConfig().fadeOut ? (float) ease(edgeAlpha, 0, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : 0;
        } else {
            edgeAlpha = BlockHighlightConfig.INSTANCE.getConfig().fadeIn ? (float) ease(edgeAlpha, BlockHighlightConfig.INSTANCE.getConfig().lineAlpha, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : BlockHighlightConfig.INSTANCE.getConfig().lineAlpha;
            for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.getConfig().fillType, pos)) {
                if (dir != null) {
                    sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().fadeIn ? (float) ease(sideFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.getConfig().fillOpacity, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : BlockHighlightConfig.INSTANCE.getConfig().fillOpacity;
                }
            }
            for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.getConfig().fillType, pos))) {
                if (dir != null) {
                    sideFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().fadeOut ? (float) ease(sideFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : 0;
                }
            }
            for (Direction dir : getSides(BlockHighlightConfig.INSTANCE.getConfig().outlineType, pos)) {
                if (dir != null) {
                    lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().fadeIn ? (float) ease(lineFades[dir.ordinal()], BlockHighlightConfig.INSTANCE.getConfig().lineAlpha, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : BlockHighlightConfig.INSTANCE.getConfig().lineAlpha;
                }
            }
            for (Direction dir : invert(getSides(BlockHighlightConfig.INSTANCE.getConfig().outlineType, pos))) {
                if (dir != null) {
                    lineFades[dir.ordinal()] = BlockHighlightConfig.INSTANCE.getConfig().fadeOut ? (float) ease(lineFades[dir.ordinal()], 0, BlockHighlightConfig.INSTANCE.getConfig().fadeSpeed) : 0;
                }
            }
        }
    }

    private static Direction joinConnected(BlockState state, BlockPos pos) {
        BlockState connectedState;
        Direction dir;
        BlockPos connectedPos;
        DoubleBlockHalf half;
        if (state.getBlock() instanceof ChestBlock && !state.get(ChestBlock.CHEST_TYPE).equals(SINGLE)) {
            dir = ChestBlock.getFacing(state);
            connectedPos = ((BlockHitResult) mc.crosshairTarget).getBlockPos().offset(dir);
            connectedState = mc.world.getBlockState(connectedPos);
            if (connectedState.getBlock() instanceof ChestBlock) {
                targetBox = targetBox.union(connectedState.getOutlineShape(mc.world, connectedPos).getBoundingBox().offset(connectedPos));
            }
            return dir;
        }
        if (state.getBlock() instanceof DoorBlock) {
            half = state.get(DoorBlock.HALF);
            if (half == DoubleBlockHalf.LOWER) {
                dir = Direction.UP;
            } else {
                dir = Direction.DOWN;
            }
            connectedPos = pos.offset(dir);
            connectedState = mc.world.getBlockState(connectedPos);
            if (connectedState.getBlock() instanceof DoorBlock && connectedState.get(DoorBlock.HALF) != half) {
                targetBox = targetBox.union(connectedState.getOutlineShape(mc.world, connectedPos).getBoundingBox().offset(connectedPos));
            }
            return dir;
        }
        if (state.getBlock() instanceof BedBlock) {
            BedPart part = state.get(BedBlock.PART);
            dir = state.get(HorizontalFacingBlock.FACING);
            if (part == BedPart.HEAD) {
                dir = dir.getOpposite();
            }
            connectedPos = pos.offset(dir);
            connectedState = mc.world.getBlockState(connectedPos);
            if (connectedState.getBlock() instanceof BedBlock && connectedState.get(BedBlock.PART) != part) {
                targetBox = targetBox.union(connectedState.getOutlineShape(mc.world, connectedPos).getBoundingBox().offset(connectedPos));
            }
            return dir;
        }
        if (state.getBlock() instanceof TallPlantBlock) {
            half = state.get(TallPlantBlock.HALF);
            if (half == DoubleBlockHalf.LOWER) {
                dir = Direction.UP;
            } else {
                dir = Direction.DOWN;
            }
            connectedPos = pos.offset(dir);
            connectedState = mc.world.getBlockState(connectedPos);
            if (connectedState.getBlock() instanceof TallPlantBlock) {
                targetBox = targetBox.union(connectedState.getOutlineShape(mc.world, connectedPos).getBoundingBox().offset(connectedPos));
            }
            return dir;
        }
        if (state.getBlock() instanceof PistonHeadBlock) {
            dir = state.get(PistonBlock.FACING);
            Direction oppDir = dir.getOpposite();
            connectedPos = pos.offset(oppDir);
            connectedState = mc.world.getBlockState(connectedPos);
            if (connectedState.getBlock() instanceof PistonBlock && connectedState.get(PistonBlock.FACING) == dir) {
                targetBox = targetBox.union(connectedState.getOutlineShape(mc.world, connectedPos).getBoundingBox().offset(connectedPos));
            }
            return oppDir;
        }
        if (state.getBlock() instanceof PistonBlock && state.get(PistonBlock.EXTENDED)) {
            dir = state.get(PistonBlock.FACING);
            connectedPos = pos.offset(dir);
            connectedState = mc.world.getBlockState(connectedPos);
            if (connectedState.getBlock() instanceof PistonHeadBlock && connectedState.get(PistonBlock.FACING) == dir) {
                targetBox = targetBox.union(connectedState.getOutlineShape(mc.world, connectedPos).getBoundingBox().offset(connectedPos));
            }
            return dir;
        }
        return null;
    }


    @SuppressWarnings("SameReturnValue")
    public static ActionResult update(BlockPos prev, BlockPos curr) {
//        if (!mc.world.isAir(blockPos1)) {
//            System.out.println("!!!!!!!!!");
//        }
        return ActionResult.PASS;
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