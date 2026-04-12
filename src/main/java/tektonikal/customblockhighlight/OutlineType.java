package tektonikal.customblockhighlight;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.text.Text;

public enum OutlineType implements NameableEnum {
    AIR_EXPOSED,
    ALL,
    CONCEALED,
    EDGES,
    LOOKAT;

    @Override
    public Text getDisplayName() {
        return switch (name()){
            case "ALL" -> Text.literal("All");
            case "EDGES" -> Text.literal("Edges");
            case "AIR_EXPOSED" -> Text.literal("Air Exposed");
            case "CONCEALED" -> Text.literal("Concealed Faces");
            case "LOOKAT" -> Text.literal("Looked At");
            default -> Text.literal("you done goofed !.");
        };
    }
}