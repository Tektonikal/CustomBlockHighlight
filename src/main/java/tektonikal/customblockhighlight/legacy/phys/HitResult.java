package tektonikal.customblockhighlight.legacy.phys;

//? if =1.8.9 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public abstract class HitResult {
	public abstract Type getType();

	public static HitResult of(net.minecraft.world.HitResult hit) {
		if (hit == null) return new BlockHitResult(Direction.UP, BlockPos.ORIGIN, true);
		if (hit.type == net.minecraft.world.HitResult.Type.ENTITY && hit.entity != null) {
			return new EntityHitResult(hit.entity);
		}
		Direction face = hit.face == null ? Direction.UP : hit.face;
		BlockPos pos = hit.getPos() == null ? BlockPos.ORIGIN : hit.getPos();
		return new BlockHitResult(face, pos, hit.type != net.minecraft.world.HitResult.Type.BLOCK);
	}

	public enum Type {
		MISS,
		BLOCK,
		ENTITY
	}
}
*///?}
