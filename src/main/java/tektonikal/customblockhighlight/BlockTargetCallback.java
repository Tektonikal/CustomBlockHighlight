package tektonikal.customblockhighlight;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;

public interface BlockTargetCallback {
    Event<BlockTargetCallback> EVENT = EventFactory.createArrayBacked(BlockTargetCallback.class,
            (listeners) -> (prev, cur) -> {
                for (BlockTargetCallback listener : listeners) {
                    InteractionResult result = listener.interact(prev, cur);

                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            });
    InteractionResult interact(BlockPos prev, BlockPos cur);
}