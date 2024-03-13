package tektonikal.blockhighlight.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
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
import tektonikal.blockhighlight.*;
import tektonikal.blockhighlight.config.BlockHighlightConfig;

import java.awt.*;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Unique
    MinecraftClient mc = MinecraftClient.getInstance();
            @Unique
            Box finalBox = new Box(0, 0, 0, 0, 0, 0);
    @Unique
    public BlockPos prevPos = new BlockPos(0,0,0);
    @Unique
    public BlockPos tempPos = new BlockPos(0,0,0);
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    private void render_drawBlockOutline(WorldRenderer worldRenderer, MatrixStack ms, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState) {
            float width = BlockHighlightConfig.INSTANCE.getConfig().width; // thickness
            int fill = BlockHighlightConfig.INSTANCE.getConfig().fillOpacity; //opacity
            BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
            BlockState state = mc.world.getBlockState(pos);
            Color col;
            Color col1;
            try {
                col = BlockHighlightConfig.INSTANCE.getConfig().lineCol;
                col1 = BlockHighlightConfig.INSTANCE.getConfig().fillCol1;
            } catch (Exception ex) {
                col = new Color(255, 0, 0);
                col1 = new Color(255, 0, 0);
            }
            int[] color = {col.getRed(), col.getGreen(), col.getBlue(), BlockHighlightConfig.INSTANCE.getConfig().lineAlpha};
            int[] color1 = {col1.getRed(), col1.getGreen(), col1.getBlue(), fill};
            Box box;
            try{
                box = state.getOutlineShape(mc.world, pos).getBoundingBox().offset(pos);
            }
            catch (UnsupportedOperationException ex){
                //if there is no actual outline, like for light blocks, just get a box around their coordinates.
                box = new Box(((BlockHitResult) mc.crosshairTarget).getBlockPos());
            }
        if ((!(mc.crosshairTarget instanceof BlockHitResult)) || state.getBlock() == Blocks.AIR || !mc.world.getWorldBorder().contains(pos)) {
                return;
            }
/*
TODO:
1. make color chroma options / gradients
2. screen-space outline
3. advanced outline models
4. more rendering modes? fancy stuff?
5. fix the fucking easing because i'm doing it in the worst way possible
6. backend rewrite because this code is just nasty man
 */
        //get connected blocks
        //i don't know how i'm gonna make the AIR_EXPOSED flag work with connected outlines without looking jank, since i can only exclude faces from a box, and i'm rendering a single box.
        if(BlockHighlightConfig.INSTANCE.getConfig().connected) {
            if (state.getBlock() instanceof ChestBlock) {
                Direction facing = ChestBlock.getFacing(state);
                BlockPos tempPos = ((BlockHitResult) mc.crosshairTarget).getBlockPos().offset(facing);
                BlockState tempState = mc.world.getBlockState(tempPos);
                if (tempState.getBlock() instanceof ChestBlock) {
                    box = box.union(tempState.getOutlineShape(mc.world, tempPos).getBoundingBox().offset(tempPos));
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
                    box = box.union(otherState.getOutlineShape(mc.world, otherPos).getBoundingBox().offset(otherPos));
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
                    box = box.union(other.getOutlineShape(mc.world, oP).getBoundingBox().offset(oP));
                }
            }
            if (state.getBlock() instanceof TallPlantBlock && !(state.getBlock() instanceof SmallDripleafBlock)) {
                DoubleBlockHalf half = state.get(TallPlantBlock.HALF);
                Direction direction;
                if (half == DoubleBlockHalf.LOWER) {
                    direction = Direction.UP;
                } else {
                    direction = Direction.DOWN;
                }
                BlockPos otherPos = pos.offset(direction);
                BlockState otherState = mc.world.getBlockState(otherPos);
                if (otherState.getBlock() instanceof TallPlantBlock && !(state.getBlock() instanceof SmallDripleafBlock)) {
                    box = box.union(otherState.getOutlineShape(mc.world, otherPos).getBoundingBox().offset(otherPos));
                }
            }
        }
        //too lazy to do pistons
        //go fuck yourself, what do you expect of me? I can only render SQUARE BOXES, not whole model outlines, although I want that in the future
        //because right now, stair outlines look UGLY AS FUCK

            //calculate where to render the block
                if (BlockHighlightConfig.INSTANCE.getConfig().doEasing) {
                    finalBox = new Box(
                            ease(BlockHighlightConfig.INSTANCE.getConfig().easeSpeed, finalBox.minX, box.minX, BlockHighlightConfig.INSTANCE.getConfig().easing),
                            ease(BlockHighlightConfig.INSTANCE.getConfig().easeSpeed, finalBox.minY, box.minY, BlockHighlightConfig.INSTANCE.getConfig().easing),
                            ease(BlockHighlightConfig.INSTANCE.getConfig().easeSpeed, finalBox.minZ, box.minZ, BlockHighlightConfig.INSTANCE.getConfig().easing),
                            ease(BlockHighlightConfig.INSTANCE.getConfig().easeSpeed, finalBox.maxX, box.maxX, BlockHighlightConfig.INSTANCE.getConfig().easing),
                            ease(BlockHighlightConfig.INSTANCE.getConfig().easeSpeed, finalBox.maxY, box.maxY, BlockHighlightConfig.INSTANCE.getConfig().easing),
                            ease(BlockHighlightConfig.INSTANCE.getConfig().easeSpeed, finalBox.maxZ, box.maxZ, BlockHighlightConfig.INSTANCE.getConfig().easing));
                } else {
                    finalBox = box;
                }
                //render the fill first, we don't want it drawn over the outline
            if(BlockHighlightConfig.INSTANCE.getConfig().fillEnabled){
                Renderer.drawBoxFill(finalBox.expand(BlockHighlightConfig.INSTANCE.getConfig().fillExpand), color1, BlockHighlightConfig.INSTANCE.getConfig().fillType, ((BlockHitResult) mc.crosshairTarget).getSide(), getAirDirs(pos));
            }
            //now the outline itself
            if(BlockHighlightConfig.INSTANCE.getConfig().outlineEnabled) {
                Renderer.drawBoxOutline(finalBox.expand(BlockHighlightConfig.INSTANCE.getConfig().expand), color, width, BlockHighlightConfig.INSTANCE.getConfig().type, ((BlockHitResult) mc.crosshairTarget).getSide(), getAirDirs(pos));
            }
            //update the positions
            if (!prevPos.equals(blockPos)) {
                tempPos = prevPos;
                prevPos = blockPos;
            }
    }
    @Unique
    public double ease(double delta, double start, double end, Easing easing){
        return start + easing.eval((float) delta) * (end - start);
    }
    @Unique
    public Direction[] getAirDirs(BlockPos pos){
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
