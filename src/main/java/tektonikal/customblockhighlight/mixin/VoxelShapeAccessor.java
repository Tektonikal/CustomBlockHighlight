package tektonikal.customblockhighlight.mixin;

//? if >=1.21.4 {
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VoxelShape.class)
public interface VoxelShapeAccessor {
	@Invoker("isCubeLike")
	boolean invokeIsCubeLike();
}
//?}
