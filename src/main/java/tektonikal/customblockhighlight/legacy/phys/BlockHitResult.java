package tektonikal.customblockhighlight.legacy.phys;

//? if =1.8.9 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockHitResult extends HitResult {
	private final Direction direction;
	private final BlockPos blockPos;
	private final boolean miss;

	public BlockHitResult(Direction direction, BlockPos blockPos, boolean miss) {
		this.direction = direction;
		this.blockPos = blockPos;
		this.miss = miss;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public Type getType() {
		return miss ? Type.MISS : Type.BLOCK;
	}
}
*///?}
