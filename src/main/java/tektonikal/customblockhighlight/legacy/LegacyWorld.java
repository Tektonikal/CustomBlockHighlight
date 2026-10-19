package tektonikal.customblockhighlight.legacy;

//? if =1.8.9 {
/*import net.minecraft.block.Block;
import net.minecraft.block.LiquidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.core.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public final class LegacyWorld {
	public static boolean pickAnyFluid;

	private LegacyWorld() {
	}

	public static VoxelShape getShape(BlockPos pos) {
		ClientWorld world = Minecraft.getInstance().level;
		if (world == null) return Shapes.empty();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		Material material = block.getMaterial();
		if (material == Material.AIR) return Shapes.empty();
		if (material.isLiquid()) {
			return Shapes.box(0, 0, 0, 1, fluidHeight(world, pos, state), 1);
		}

		block.updateShape(world, pos);
		AABB outline = AABB.of(block.getOutlineShape(world, pos)).move(-pos.getX(), -pos.getY(), -pos.getZ());
		List<Box> collisions = new ArrayList<>();
		Box mask = new Box(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
		block.addCollisions(world, pos, state, mask, collisions, Minecraft.getInstance().player);
		// addCollisions changes the block's shared bounds for multi-box blocks, restore them for vanilla's outline
		block.updateShape(world, pos);

		if (collisions.size() > 1) {
			List<AABB> boxes = new ArrayList<>(collisions.size());
			for (Box collision : collisions) {
				boxes.add(AABB.of(collision).move(-pos.getX(), -pos.getY(), -pos.getZ()).intersect(outline));
			}
			VoxelShape shape = Shapes.create(boxes);
			if (!shape.isEmpty()) return shape;
		}
		return Shapes.create(outline);
	}

	private static double fluidHeight(ClientWorld world, BlockPos pos, BlockState state) {
		if (world.getBlockState(pos.above()).getBlock().getMaterial() == state.getBlock().getMaterial()) return 1;
		return 1 - LiquidBlock.getHeightLoss(state.get(LiquidBlock.LEVEL));
	}

	public static Vec3 getOffset(BlockPos pos) {
		ClientWorld world = Minecraft.getInstance().level;
		if (world == null) return Vec3.ZERO;
		Block.OffsetType type = world.getBlockState(pos).getBlock().getOffsetType();
		if (type == Block.OffsetType.NONE) return Vec3.ZERO;
		long seed = MathHelper.hashCode(pos);
		double x = ((float) (seed >> 16 & 15L) / 15.0F - 0.5) * 0.5;
		double z = ((float) (seed >> 24 & 15L) / 15.0F - 0.5) * 0.5;
		double y = type == Block.OffsetType.XYZ ? ((float) (seed >> 20 & 15L) / 15.0F - 1.0) * 0.2 : 0;
		return new Vec3(x, y, z);
	}

	public static BlockState getActualState(BlockPos pos) {
		ClientWorld world = Minecraft.getInstance().level;
		BlockState state = world.getBlockState(pos);
		return state.getBlock().resolveVirtualProperties(state, world, pos);
	}

	public static Quaternionf rotation(Direction direction) {
		return switch (direction) {
			case DOWN -> new Quaternionf().rotationX((float) Math.PI);
			case UP -> new Quaternionf();
			case NORTH -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) Math.PI);
			case SOUTH -> new Quaternionf().rotationX((float) (Math.PI / 2));
			case WEST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2));
			case EAST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (-Math.PI / 2));
		};
	}
}
*///?}
