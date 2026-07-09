package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum OutlineType implements NameableEnum {
    AIR_EXPOSED,
    ALL,
    CONCEALED,
    EDGES,
    LOOKAT;

    @Override
    public Component getDisplayName() {
        return switch (this) {
            case ALL -> Component.literal("All");
            case EDGES -> Component.literal("Edges");
            case AIR_EXPOSED -> Component.literal("Air Exposed");
            case CONCEALED -> Component.literal("Concealed Faces");
            case LOOKAT -> Component.literal("Looked At");
        };
    }
}