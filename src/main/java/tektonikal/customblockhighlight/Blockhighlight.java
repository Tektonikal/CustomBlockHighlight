package tektonikal.customblockhighlight;

import net.fabricmc.api.ModInitializer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;

public class Blockhighlight implements ModInitializer {
    @Override
    public void onInitialize() {
        BlockHighlightConfig.INSTANCE.load();
    }

}
