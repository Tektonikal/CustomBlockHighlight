package tektonikal.customblockhighlight.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;

import static net.minecraft.block.enums.ChestType.SINGLE;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Unique
    MinecraftClient mc = MinecraftClient.getInstance();
    @Unique
    Box easeBox = new Box(0, 0, 0, 0, 0, 0);
    @Unique
    boolean erm = false;
    @Unique
    Direction[] prevLineDirs;
    @Unique
    Direction[] prevFillDirs;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    private void render_drawBlockOutline(WorldRenderer worldRenderer, MatrixStack ms, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState) {
        float width = BlockHighlightConfig.INSTANCE.getConfig().width; // thickness
        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        int[] lineCol;
        int[] fillCol;
        try {
            lineCol = new int[]{BlockHighlightConfig.INSTANCE.getConfig().lineCol.getRed(), BlockHighlightConfig.INSTANCE.getConfig().lineCol.getGreen(), BlockHighlightConfig.INSTANCE.getConfig().lineCol.getBlue(), BlockHighlightConfig.INSTANCE.getConfig().lineAlpha};
            fillCol = new int[]{BlockHighlightConfig.INSTANCE.getConfig().fillCol.getRed(), BlockHighlightConfig.INSTANCE.getConfig().fillCol.getGreen(), BlockHighlightConfig.INSTANCE.getConfig().fillCol.getBlue(), BlockHighlightConfig.INSTANCE.getConfig().fillOpacity};
        } catch (Exception ex) {
            fillCol = new int[]{255, 0, 0, 0};
            lineCol = new int[]{255, 0, 0, 0};
        }
        Box targetBox;
        try {
            targetBox = state.getOutlineShape(mc.world, pos).getBoundingBox().offset(pos);
        } catch (UnsupportedOperationException ex) {
            //if there is no actual outline, like for light blocks, just get a box around their coordinates.
            targetBox = new Box(((BlockHitResult) mc.crosshairTarget).getBlockPos());
        }
        if ((!(mc.crosshairTarget instanceof BlockHitResult)) || state.getBlock() == Blocks.AIR || !mc.world.getWorldBorder().contains(pos)) {
            return;
        }
/*
TODO:
2. screen-space outline / shader based
3. advanced outline models
4. blending
4. 1.21 update (surely it's trivial :clueless:)
6. backend rewrite because this code is just nasty man
 */
        //get connected blocks
        //i don't know how i'm gonna make the AIR_EXPOSED flag work with connected outlines without looking jank, since i can only exclude faces from a box, and i'm rendering a single box.
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
        if (BlockHighlightConfig.INSTANCE.getConfig().crystalHelper) {
            if (state.getBlock().equals(Blocks.OBSIDIAN) || state.getBlock().equals(Blocks.BEDROCK)) {
                double pd = pos.up().getX();
                double pe = pos.up().getY();
                double pf = pos.up().getZ();
                if (!mc.world.isAir(pos.up()) || !mc.world.getOtherEntities(null, new Box(pd, pe, pf, pd + 1.0, pe + 2.0, pf + 1.0)).isEmpty()) {
                    erm = true;
                } else {
                    erm = false;
                }
            } else {
                erm = false;
            }
        } else {
            //I am going fucking insane
            erm = false;
        }
        //too lazy to do pistons
        //go fuck yourself, what do you expect of me? I can only render SQUARE BOXES, not whole model outlines, although I want that in the future
        //because right now, stair outlines look UGLY AS FUCK

        //calculate where to render the block
        if (BlockHighlightConfig.INSTANCE.getConfig().doEasing) {
            easeBox = new Box(
                    ease(easeBox.minX, targetBox.minX),
                    ease(easeBox.minY, targetBox.minY),
                    ease(easeBox.minZ, targetBox.minZ),
                    ease(easeBox.maxX, targetBox.maxX),
                    ease(easeBox.maxY, targetBox.maxY),
                    ease(easeBox.maxZ, targetBox.maxZ)
            );
        } else {
            easeBox = targetBox;
        }
        //render the fill first, we don't want it drawn over the outline
        if (BlockHighlightConfig.INSTANCE.getConfig().fillEnabled) {
            Renderer.drawBoxFill(ms, easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().fillExpand), BlockHighlightConfig.INSTANCE.getConfig().fillRainbow ? getRainbowCol(true) : erm ? new int[]{255, 0, 0, BlockHighlightConfig.INSTANCE.getConfig().fillOpacity} : fillCol, BlockHighlightConfig.INSTANCE.getConfig().fillType, ((BlockHitResult) mc.crosshairTarget).getSide(), prevFillDirs, getAirDirs(pos));
            prevFillDirs = Renderer.getFillDirs();
        }
        //now the outline itself
        if (BlockHighlightConfig.INSTANCE.getConfig().outlineEnabled) {
            Renderer.drawBoxOutline(ms, easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().expand), BlockHighlightConfig.INSTANCE.getConfig().outlineRainbow ? getRainbowCol(false) : erm ? new int[]{255, 0, 0, BlockHighlightConfig.INSTANCE.getConfig().lineAlpha} : lineCol, width, BlockHighlightConfig.INSTANCE.getConfig().type, ((BlockHitResult) mc.crosshairTarget).getSide(), prevLineDirs, getAirDirs(pos));
            prevLineDirs = Renderer.getLineDirs();
        }
    }

    @Unique
    private int[] getRainbowCol(boolean tempb) {
        //fix the jank with opacity later
        Color temp = getRainbow((System.currentTimeMillis() % 10000L / 10000.0f) * BlockHighlightConfig.INSTANCE.getConfig().rainbowSpeed, 128, 128, 128, 127, 127, 127);
        return new int[]{temp.getRed(), temp.getGreen(), temp.getBlue(), tempb ? BlockHighlightConfig.INSTANCE.getConfig().fillOpacity : BlockHighlightConfig.INSTANCE.getConfig().lineAlpha};
    }

    //https://github.com/Splzh/ClearHitboxes/blob/main/src/main/java/splash/utils/ColorUtils.java !!
    @Unique
    private static Color getRainbow(double percent, int rMid, int gMid, int bMid, int rRange, int gRange, int bRange) {
        double offset = Math.PI * 2 / 3;
        double pos = percent * (Math.PI * 2);
        float red = (float) ((Math.sin(pos) * rRange) + rMid);
        float green = (float) ((Math.sin(pos + offset) * gRange) + gMid);
        float blue = (float) ((Math.sin(pos + offset * 2) * bRange) + bMid);
        return new Color((int) (red), (int) (green), (int) (blue), 255);
    }

    @Unique
    public double ease(double start, double end) {
        return start + (end - start) * (1 - Math.exp(-(1.0F / mc.getCurrentFps()) * BlockHighlightConfig.INSTANCE.getConfig().easeSpeed));
    }

    @Unique
    public Direction[] getAirDirs(BlockPos pos) {
        //future update todo: prevent blocks from detecting their own parts as obstructing air
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
}
