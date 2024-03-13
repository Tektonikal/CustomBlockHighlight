package tektonikal.blockhighlight;

import net.fabricmc.api.ModInitializer;
import tektonikal.blockhighlight.config.BlockHighlightConfig;

public class Blockhighlight implements ModInitializer {
    @Override
    public void onInitialize() {
        BlockHighlightConfig.INSTANCE.load();
    }
}
