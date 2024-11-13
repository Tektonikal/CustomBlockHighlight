package tektonikal.customblockhighlight.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tektonikal.customblockhighlight.OutlineType;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

import java.awt.*;
import java.util.EnumSet;

import static net.minecraft.block.enums.ChestType.SINGLE;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    @Nullable
    private ClientWorld world;
    @Unique
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    @Unique
    private static Box easeBox = new Box(0, 0, 0, 0, 0, 0);
    @Unique
    private static float[] sideFades = new float[6];
    @Unique
    private static float[] lineFades = new float[6];

    /*
    TODO:
    2. screen-space outline / shader based
    3. advanced outline models
    4. 1.21 update (surely it's trivial :clueless:)
    5. fade in/out when not looking at block
     */
    @Redirect(method = "renderTargetBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;DDDLnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V"))
    private void CBH$thingamajig(WorldRenderer instance, MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, int color) {
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
            easeBox = new Box(
                    ease(easeBox.minX, targetBox.minX, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed),
                    ease(easeBox.minY, targetBox.minY, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed),
                    ease(easeBox.minZ, targetBox.minZ, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed),
                    ease(easeBox.maxX, targetBox.maxX, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed),
                    ease(easeBox.maxY, targetBox.maxY, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed),
                    ease(easeBox.maxZ, targetBox.maxZ, BlockHighlightConfig.INSTANCE.getConfig().easeSpeed)
            );
        } else {
            easeBox = targetBox;
        }
        //render the fill first, we don't want it drawn over the outline
        if (BlockHighlightConfig.INSTANCE.getConfig().fillEnabled) {
            Color finalFillCol = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().fillRainbow ? getRainbowCol(0) : BlockHighlightConfig.INSTANCE.getConfig().fillCol;
            Color finalFillCol2 = erm ? Color.RED : BlockHighlightConfig.INSTANCE.getConfig().fillRainbow ? getRainbowCol(BlockHighlightConfig.INSTANCE.getConfig().delay) : BlockHighlightConfig.INSTANCE.getConfig().fillCol2;
            Renderer.drawBoxFill(matrices, easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().fillExpand), finalFillCol, finalFillCol2, sideFades);
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
                Renderer.drawEdgeOutline(matrices, state.getOutlineShape(world, pos, ShapeContext.of(entity)), (double) easeBox.minX - cameraX, easeBox.minY - cameraY, easeBox.minZ - cameraZ, finalLineCol, finalLineCol2, BlockHighlightConfig.INSTANCE.getConfig().lineAlpha, BlockHighlightConfig.INSTANCE.getConfig().lineWidth);
            } else {


                Renderer.drawBoxOutline(matrices, easeBox.expand(BlockHighlightConfig.INSTANCE.getConfig().lineExpand), finalLineCol, finalLineCol2, lineFades, BlockHighlightConfig.INSTANCE.getConfig().lineWidth);

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
    private Direction[] getSides(OutlineType type, BlockPos pos) {
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
    private Color getRainbowCol(int delay) {
        return getRainbow(((System.currentTimeMillis() + delay) % 10000L / 10000.0f) * BlockHighlightConfig.INSTANCE.getConfig().rainbowSpeed);
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

    @Unique
    public double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-(1.0F / mc.getCurrentFps()) * speed)));
    }

    @Unique
    public Direction[] getAirDirs(BlockPos pos) {
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
}