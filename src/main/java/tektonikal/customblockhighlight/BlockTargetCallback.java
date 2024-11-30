package tektonikal.customblockhighlight;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public interface BlockTargetCallback {
    Event<BlockTargetCallback> EVENT = EventFactory.createArrayBacked(BlockTargetCallback.class,
            (listeners) -> (prev, cur) -> {
                for (BlockTargetCallback listener : listeners) {
                    ActionResult result = listener.interact(prev, cur);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });
    ActionResult interact(BlockPos prev, BlockPos cur);
}