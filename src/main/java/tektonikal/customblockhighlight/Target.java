package tektonikal.customblockhighlight;

//? if =1.8.9 {
/*import tektonikal.customblockhighlight.legacy.LegacyClient;
import tektonikal.customblockhighlight.legacy.LegacyWorld;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoatItem;
//? if >1.8.9 {
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
//?}
import net.minecraft.world.item.Items;
//? if >1.8.9
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
//? if >1.8.9 {
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
//?}
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
//? if >=1.21.11 {
import org.jspecify.annotations.NonNull;
//?}

import static tektonikal.customblockhighlight.Renderer.mc;
import static tektonikal.customblockhighlight.Renderer.moveToZero;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.getActiveInstance;

public record Target(HitResult hit, BlockPos pos, BlockState state, Direction connected, VoxelShape raw, VoxelShape shape, VoxelShape local, boolean fluid) {
	boolean ignored() {
		if (isIgnoredEntity(hit) || shape.isEmpty()) return true;
		if (pos == null || hit.getType() == HitResult.Type.MISS) return false;
		if (state.getBlock() == Blocks.BARRIER && !getActiveInstance().highlightBarriers && !barriersShown()) return true;
		//~ if =1.8.9 'camera.position()' -> 'camera.eye()'
		Vec3 eye = Renderer.camera.position();
		if (!inside(bounds(), eye)) return false;
		for (AABB box : shape.toAabbs()) if (inside(box, eye)) return true;
		return false;
	}

	private static VoxelShape boundsOf;
	private static AABB bounds;

	AABB bounds() {
		if (shape != boundsOf) {
			boundsOf = shape;
			bounds = shape.bounds();
		}
		return bounds;
	}

	private static boolean inside(AABB box, Vec3 p) {
		return p.x >= box.minX && p.x < box.maxX && p.y >= box.minY && p.y < box.maxY && p.z >= box.minZ && p.z < box.maxZ;
	}

	private static boolean barriersShown() {
		//? if >1.8.9 {
		return mc.player.isCreative() && mc.player.getMainHandItem().is(Items.BARRIER);
		//?} else {
		/*net.minecraft.item.ItemStack stack = mc.player.getItemInHand();
		return mc.player.abilities.creativeMode && stack != null && stack.getItem() == net.minecraft.item.Item.byBlock(Blocks.BARRIER);
		*///?}
	}

	static boolean isIgnoredEntity(HitResult hitResult) {
		return hitResult instanceof EntityHitResult entityHitResult && (!getActiveInstance().allowEntities || entityHitResult.getEntity().isInvisible());
	}

	private static boolean pickedFluid;

	//? if >1.8.9 {
	public static HitResult getHitResult() {
		pickedFluid = false;
		if (mc.level == null || mc.player == null || mc.getCameraEntity() == null) return null;
		if (mc.hitResult instanceof EntityHitResult) return mc.hitResult;
		if (getActiveInstance().allowLiquids && isHoldingValidItem()) {
			float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			HitResult yeah = pick(mc.getCameraEntity(), mc.player.blockInteractionRange(), delta);
			if (yeah instanceof BlockHitResult block && !block.getBlockPos().equals(BlockPos.containing(mc.getCameraEntity().getEyePosition(delta)))) {
				return yeah;
			}
			pickedFluid = false;
		}
		return mc.hitResult;
	}

	public static boolean isHoldingValidItem() {
		return !getActiveInstance().onlyWhenHoldingAppropriate || actsOnFluids(mc.player.getMainHandItem().getItem()) || actsOnFluids(mc.player.getOffhandItem().getItem());
	}

	static boolean actsOnFluids(Item item) {
		return item == Items.BUCKET || item instanceof BottleItem || item instanceof PlaceOnWaterBlockItem || item instanceof BoatItem;
	}
	//?} else {
	/*public static HitResult getHitResult() {
		if (mc.level == null || mc.player == null || mc.getCameraEntity() == null) return null;
		HitResult crosshair = HitResult.of(mc.crosshairTarget);
		if (crosshair instanceof EntityHitResult) return crosshair;
		if (getActiveInstance().allowLiquids && isHoldingValidItem()) {
			float delta = LegacyClient.tickDelta();
			HitResult yeah = pick(mc.getCameraEntity(), mc.interactionManager.getReach(), delta);
			net.minecraft.util.math.Vec3d eye = mc.getCameraEntity().getEyePosition(delta);
			if (yeah instanceof BlockHitResult block && !block.getBlockPos().equals(new BlockPos(eye.x, eye.y, eye.z))) {
				return yeah;
			}
		}
		return crosshair;
	}

	public static boolean isHoldingValidItem() {
		if (!getActiveInstance().onlyWhenHoldingAppropriate) return true;
		net.minecraft.item.ItemStack stack = mc.player.getItemInHand();
		if (stack == null || stack.getItem() == null) return false;
		return stack.getItem() == Items.BUCKET || stack.getItem() instanceof net.minecraft.item.GlassBottleItem || stack.getItem() instanceof net.minecraft.item.LilyPadItem || stack.getItem() instanceof BoatItem;
	}
	*///?}

	private static Target cached;
	private static BlockState keyNeighborState;
	private static AABB keyEntitySize;

	static Target current() {
		HitResult hit = getHitResult();
		if (hit == null) return null;
		if (hit instanceof BlockHitResult block) {
			BlockPos pos = block.getBlockPos();
			//? if >1.8.9 {
			BlockState state = mc.level.getBlockState(pos);
			var fluid = mc.level.getFluidState(pos);
			VoxelShape raw = state.getShape(mc.level, pos, shapeContext());
			boolean isFluid = !fluid.isEmpty() && (pickedFluid || raw.isEmpty());
			if (isFluid) raw = fluid.getShape(mc.level, pos);
			//?} else {
			/*BlockState state = mc.level.getBlockState(pos);
			VoxelShape raw = LegacyWorld.getShape(pos);
			boolean isFluid = false;
			*///?}
			Direction connected = null;
			VoxelShape neighbor = null;
			BlockState neighborState = null;
			if (getActiveInstance().connectedBlocks && !isFluid) {
				connected = joinConnected(pos);
				if (connected != null) {
					neighborState = mc.level.getBlockState(pos.relative(connected));
					//? if >1.8.9 {
					neighbor = neighborState.getShape(mc.level, pos.relative(connected), shapeContext());
					//?} else
					//neighbor = LegacyWorld.getShape(pos.relative(connected));
				}
			}
			if (cached != null && pos.equals(cached.pos) && state == cached.state && connected == cached.connected && isFluid == cached.fluid && neighborState == keyNeighborState && (raw == cached.raw || raw.toAabbs().equals(cached.raw.toAabbs()))) {
				return cached.hit == hit ? cached : (cached = new Target(hit, pos, state, connected, cached.raw, cached.shape, cached.local, isFluid));
			}
			keyNeighborState = neighborState;
			keyEntitySize = null;
			VoxelShape shape = raw.isEmpty() ? Shapes.block() : raw;
			if (connected != null) {
				shape = Shapes.join(shape, neighbor.move(connected.getStepX(), connected.getStepY(), connected.getStepZ()), BooleanOp.OR);
			}
			return cached = new Target(hit, pos, state, connected, raw, shape.move(pos.getX(), pos.getY(), pos.getZ()), moveToZero(shape), isFluid);
		} else if (hit instanceof EntityHitResult entityHitResult && !isIgnoredEntity(hit)) {
			Entity entity = entityHitResult.getEntity();
			//? if >1.8.9 {
			float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			AABB box = entity.getBoundingBox().move(entity.getPosition(delta).subtract(entity.getX(), entity.getY(), entity.getZ()));
			AABB boundingBox = moveToZero(entity.getBoundingBox());
			//?} else {
			/*float delta = LegacyClient.tickDelta();
			Vec3 position = new Vec3(Mth.lerp(delta, entity.prevX, entity.x), Mth.lerp(delta, entity.prevY, entity.y), Mth.lerp(delta, entity.prevZ, entity.z));
			AABB box = AABB.of(entity.getShape()).move(position.subtract(entity.x, entity.y, entity.z));
			AABB boundingBox = moveToZero(AABB.of(entity.getShape()));
			*///?}
			VoxelShape local = cached != null && boundingBox.equals(keyEntitySize) ? cached.local : Shapes.create(boundingBox);
			keyEntitySize = boundingBox;
			return cached = new Target(hit, null, null, null, null, Shapes.create(box), local, false);
		}
		return new Target(hit, null, null, null, null, Shapes.block(), Shapes.block(), false);
	}

	//? if >1.8.9 {
	static CollisionContext shapeContext() {
		return mc.getCameraEntity() == null ? CollisionContext.empty() : CollisionContext.of(mc.getCameraEntity());
	}
	//?}

	static boolean isCrystalObstructed(HitResult evilHitResult) {
		//? if >1.8.9 {
		if (!getActiveInstance().crystalHelper || !(evilHitResult instanceof BlockHitResult block)) return false;
		if (getActiveInstance().crystalHelperOnlyWhenHolding && !mc.player.getMainHandItem().is(Items.END_CRYSTAL) && !mc.player.getOffhandItem().is(Items.END_CRYSTAL)) return false;
		Block target = mc.level.getBlockState(block.getBlockPos()).getBlock();
		if (target != Blocks.OBSIDIAN && target != Blocks.BEDROCK) return false;
		BlockPos above = block.getBlockPos().above();
		return !mc.level.isEmptyBlock(above) || !mc.level.getEntities(null, new AABB(above).expandTowards(0, 1, 0)).isEmpty();
		//?} else
		//return false;
	}

	//? if >1.8.9 {
	public static HitResult pick(Entity e, final double range, final float a) {
		if (mc.level == null) return null;
		Vec3 from = e.getEyePosition(a);
		Vec3 viewVector = e.getViewVector(a);
		Vec3 to = from.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
		ClipContext context = new ClipContext(from, to, ClipContext.Block.OUTLINE, getActiveInstance().onlySourceBlocks ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.ANY, e);
		BlockHitResult hit = mc.level.clip(context);
		if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = hit.getBlockPos();
			BlockHitResult blockHit = context.getBlockShape(mc.level.getBlockState(pos), mc.level, pos).clip(from, to, pos);
			BlockHitResult fluidHit = context.getFluidShape(mc.level.getFluidState(pos), mc.level, pos).clip(from, to, pos);
			pickedFluid = fluidHit != null && (blockHit == null || from.distanceToSqr(fluidHit.getLocation()) < from.distanceToSqr(blockHit.getLocation()));
		}
		return hit;
	}
	//?} else {
	/*public static HitResult pick(Entity e, final double range, final float a) {
		if (mc.level == null) return null;
		net.minecraft.util.math.Vec3d from = e.getEyePosition(a);
		net.minecraft.util.math.Vec3d viewVector = e.getRotationVector(a);
		net.minecraft.util.math.Vec3d to = from.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);
		LegacyWorld.fluidPickThread = getActiveInstance().onlySourceBlocks ? null : Thread.currentThread();
		try {
			return HitResult.of(mc.level.rayTrace(from, to, true, false, true));
		} finally {
			LegacyWorld.fluidPickThread = null;
		}
	}
	*///?}

	//? if >1.8.9 {
	static Direction joinConnected(BlockPos pos) {
		if (mc.level == null) return null;
		BlockState connectedState;
		Direction dir;
		BlockPos connectedPos;
		BlockState state = mc.level.getBlockState(pos);
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
			DoubleBlockHalf halfState = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
			Direction d = halfState == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
			connectedPos = pos.relative(d);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock().getClass().equals(state.getBlock().getClass())) {
				if (connectedState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && connectedState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == halfState.getOtherHalf()) {
					return d;
				}
			}
		}
		if (state.getBlock() instanceof ChestBlock && !state.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)) {
			dir = ChestBlock.getConnectedDirection(state);
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof ChestBlock) {
				return dir;
			}
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
				return dir;
			}
		}
		if (state.getBlock() instanceof PistonHeadBlock) {
			dir = state.getValue(PistonBaseBlock.FACING);
			Direction oppDir = dir.getOpposite();
			connectedPos = pos.relative(oppDir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof PistonBaseBlock && connectedState.getValue(PistonBaseBlock.FACING) == dir) {
				return oppDir;
			}
		}
		if (state.getBlock() instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED)) {
			dir = state.getValue(PistonBaseBlock.FACING);
			connectedPos = pos.relative(dir);
			connectedState = mc.level.getBlockState(connectedPos);
			if (connectedState.getBlock() instanceof PistonHeadBlock && connectedState.getValue(PistonBaseBlock.FACING) == dir) {
				return dir;
			}
		}
		return null;
	}
	//?} else {
	/*static Direction joinConnected(BlockPos pos) {
		if (mc.level == null) return null;
		BlockState state = mc.level.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof DoorBlock) {
			Direction d = state.get(DoorBlock.HALF) == DoorBlock.Half.LOWER ? Direction.UP : Direction.DOWN;
			BlockState connectedState = mc.level.getBlockState(pos.relative(d));
			if (connectedState.getBlock() == block && connectedState.get(DoorBlock.HALF) != state.get(DoorBlock.HALF)) {
				return d;
			}
		}
		if (block instanceof DoublePlantBlock) {
			Direction d = state.get(DoublePlantBlock.HALF) == DoublePlantBlock.Half.LOWER ? Direction.UP : Direction.DOWN;
			BlockState connectedState = mc.level.getBlockState(pos.relative(d));
			if (connectedState.getBlock() == block && connectedState.get(DoublePlantBlock.HALF) != state.get(DoublePlantBlock.HALF)) {
				return d;
			}
		}
		if (block instanceof ChestBlock) {
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				if (mc.level.getBlockState(pos.relative(dir)).getBlock() == block) {
					return dir;
				}
			}
		}
		if (block instanceof BedBlock) {
			BedBlock.Part part = state.get(BedBlock.PART);
			Direction dir = state.get(BedBlock.FACING);
			if (part == BedBlock.Part.HEAD) {
				dir = dir.getOpposite();
			}
			BlockState connectedState = mc.level.getBlockState(pos.relative(dir));
			if (connectedState.getBlock() instanceof BedBlock && connectedState.get(BedBlock.PART) != part) {
				return dir;
			}
		}
		if (block instanceof PistonHeadBlock) {
			Direction dir = state.get(PistonHeadBlock.FACING);
			Direction oppDir = dir.getOpposite();
			BlockState connectedState = mc.level.getBlockState(pos.relative(oppDir));
			if (connectedState.getBlock() instanceof PistonBaseBlock && connectedState.get(PistonBaseBlock.FACING) == dir) {
				return oppDir;
			}
		}
		if (block instanceof PistonBaseBlock && state.get(PistonBaseBlock.EXTENDED)) {
			Direction dir = state.get(PistonBaseBlock.FACING);
			BlockState connectedState = mc.level.getBlockState(pos.relative(dir));
			if (connectedState.getBlock() instanceof PistonHeadBlock && connectedState.get(PistonHeadBlock.FACING) == dir) {
				return dir;
			}
		}
		return null;
	}
	*///?}
}
