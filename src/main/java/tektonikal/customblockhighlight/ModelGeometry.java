package tektonikal.customblockhighlight;

//? if =1.8.9
//import tektonikal.customblockhighlight.legacy.LegacyWorld;
//? if >=26.1 {
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
//?} elif >=1.21.5 {
/*import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
*///?} elif >1.8.9 {
/*import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
*///?} else {
/*import net.minecraft.client.render.model.block.BakedModel;
import net.minecraft.client.render.model.block.BakedQuad;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//? if >1.8.9
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
//? if >1.8.9 {
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
//?}
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import tektonikal.customblockhighlight.util.*;

import java.util.ArrayList;
import java.util.List;

import static tektonikal.customblockhighlight.Renderer.mc;

public final class ModelGeometry {
	private ModelGeometry() {
	}

	//? if >=1.21.2
	private static Vec3 unitVec3(Direction dir) { return dir.getUnitVec3(); }
	//? if <1.21.2 {
	/*private static Vec3 unitVec3(Direction dir) {
		return new Vec3(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ());
	}
	*///?}

	//? if >=26.1 {
	private static void addModelQuads(BlockState state, BlockPos pos, Vec3 offset, List<Vec3[]> quads) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		mc.getModelManager().getBlockStateModelSet().get(state).collectParts(RandomSource.create(state.getSeed(pos)), parts);
		for (BlockStateModelPart part : parts) {
			for (Direction side : SIDES_AND_NULL) {
				for (var quad : part.getQuads(side)) {
					quads.add(new Vec3[]{new Vec3(quad.position0()).add(offset), new Vec3(quad.position1()).add(offset), new Vec3(quad.position2()).add(offset), new Vec3(quad.position3()).add(offset)});
				}
			}
		}
	}
	//?} elif >=1.21.5 {
	/*private static void addModelQuads(BlockState state, BlockPos pos, Vec3 offset, List<Vec3[]> quads) {
		BlockStateModel model = mc.getModelManager().getBlockModelShaper().getBlockModel(state);
		for (BlockModelPart part : model.collectParts(RandomSource.create(state.getSeed(pos)))) {
			for (Direction side : SIDES_AND_NULL) {
				for (BakedQuad quad : part.getQuads(side)) {
					//? if >=1.21.11 {
					quads.add(new Vec3[]{new Vec3(quad.position0()).add(offset), new Vec3(quad.position1()).add(offset), new Vec3(quad.position2()).add(offset), new Vec3(quad.position3()).add(offset)});
					//?} else
					//quads.add(rawQuad(quad.vertices(), 8, offset));
				}
			}
		}
	}
	*///?} elif >1.8.9 {
	/*private static void addModelQuads(BlockState state, BlockPos pos, Vec3 offset, List<Vec3[]> quads) {
		BakedModel model = mc.getModelManager().getBlockModelShaper().getBlockModel(state);
		RandomSource random = RandomSource.create();
		for (Direction side : SIDES_AND_NULL) {
			random.setSeed(state.getSeed(pos));
			for (BakedQuad quad : model.getQuads(state, side, random)) {
				quads.add(rawQuad(quad.getVertices(), 8, offset));
			}
		}
	}
	*///?} else {
	/*private static void addModelQuads(BlockState state, BlockPos pos, Vec3 offset, List<Vec3[]> quads) {
		BakedModel model = mc.getBlockRenderDispatcher().getModel(state, mc.level, pos);
		for (Direction face : Direction.values()) {
			for (BakedQuad quad : model.getQuads(face)) {
				quads.add(rawQuad(quad.getVertices(), 7, offset));
			}
		}
		for (BakedQuad quad : model.getQuads()) {
			quads.add(rawQuad(quad.getVertices(), 7, offset));
		}
	}
	*///?}

	private static final Direction[] SIDES_AND_NULL = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

	//? if <1.21.11 {
	/*private static Vec3[] rawQuad(int[] vertices, int stride, Vec3 offset) {
		Vec3[] corners = new Vec3[4];
		for (int i = 0; i < 4; i++) {
			int base = i * stride;
			corners[i] = new Vec3(Float.intBitsToFloat(vertices[base]), Float.intBitsToFloat(vertices[base + 1]), Float.intBitsToFloat(vertices[base + 2])).add(offset);
		}
		return corners;
	}
	*///?}

	static Object modelKey(Target target) {
		if (target.pos() == null || target.fluid()) return null;
		//? if >=26.1 {
		return mc.getModelManager().getBlockStateModelSet().get(target.state());
		//?} elif >1.8.9 {
		/*return mc.getModelManager().getBlockModelShaper().getBlockModel(target.state());
		*///?} else {
		/*return mc.getBlockRenderDispatcher().getModel(modelState(target.pos()), mc.level, target.pos());
		*///?}
	}

	static ShapeGeometry buildModelGeometry(Target target) {
		if (target.pos() == null || target.fluid()) return ShapeGeometry.ofShape(target.local());
		List<Vec3[]> quads = new ArrayList<>();
		BlockPos pos = target.pos();
		Vec3 min = target.shape().bounds().getMinPosition().subtract(Vec3.atLowerCornerOf(pos));
		Direction dir = target.connected();
		VoxelShape fallback = Shapes.empty();
		if (dir != null) fallback = addBlockQuads(pos.relative(dir), unitVec3(dir).subtract(min), quads, fallback);
		fallback = addBlockQuads(pos, min.reverse(), quads, fallback);
		for (Quad quad : ShapeGeometry.ofShape(fallback).quads()) quads.add(quad.corners);
		return ShapeGeometry.ofQuads(ShapeGeometry.cullBuried(quads));
	}

	//get shapes from things that don't have model quads to steal like BER
	private static VoxelShape addBlockQuads(BlockPos pos, Vec3 offset, List<Vec3[]> quads, VoxelShape fallback) {
		int before = quads.size();
		BlockState state = modelState(pos);
		if (drawnFromModel(state) && !outlinedByShape(state)) addModelQuads(state, pos, offset.add(modelOffset(pos)), quads);
		if (quads.size() > before) return fallback;
		VoxelShape shape = blockShape(pos).move(offset.x, offset.y, offset.z);
		//? if >1.8.9 {
		if (state.hasProperty(BlockStateProperties.ROTATION_16) && state.getValue(BlockStateProperties.ROTATION_16) != 0) {
			double angle = Math.toRadians(-RotationSegment.convertToDegrees(state.getValue(BlockStateProperties.ROTATION_16)));
			double cos = Math.cos(angle), sin = Math.sin(angle), cx = offset.x + 0.5, cz = offset.z + 0.5;
			for (Quad quad : ShapeGeometry.ofShape(shape).quads()) {
				Vec3[] turned = new Vec3[4];
				for (int i = 0; i < 4; i++) {
					Vec3 p = quad.corners[i];
					double x = p.x - cx, z = p.z - cz;
					turned[i] = new Vec3(cx + x * cos + z * sin, p.y, cz - x * sin + z * cos);
				}
				quads.add(turned);
			}
			return fallback;
		}
		//?}
		return Shapes.join(fallback, shape, BooleanOp.OR);
	}

	private static VoxelShape blockShape(BlockPos pos) {
		//? if >1.8.9 {
		VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos, Target.shapeContext());
		return shape.isEmpty() ? mc.level.getFluidState(pos).getShape(mc.level, pos) : shape;
		//?} else
		//return LegacyWorld.getShape(pos);
	}

	private static BlockState modelState(BlockPos pos) {
		//? if >1.8.9 {
		return mc.level.getBlockState(pos);
		//?} else
		//return LegacyWorld.getActualState(pos);
	}

	private static boolean drawnFromModel(BlockState state) {
		//? if >1.8.9 {
		return state.getRenderShape() == RenderShape.MODEL;
		//?} else
		//return state.getBlock().getRenderType() == 3;
	}

	private static boolean outlinedByShape(BlockState state) {
		//? if >1.8.9 {
		//~ if <26.3 'RedstoneWireBlock' -> 'RedStoneWireBlock'
		return state.getBlock() instanceof RedstoneWireBlock || state.getBlock() instanceof BaseFireBlock;
		//?} else
		//return state.getBlock() instanceof net.minecraft.block.RedstoneWireBlock || state.getBlock() instanceof net.minecraft.block.FireBlock;
	}

	private static Vec3 modelOffset(BlockPos pos) {
		//? if >1.8.9 {
		return mc.level.getBlockState(pos).getOffset(pos);
		//?} else
		//return LegacyWorld.getOffset(pos);
	}
}
