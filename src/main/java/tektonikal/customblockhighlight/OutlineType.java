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
            case "AIR_EXPOSED" -> Text.literal("Air exposed");
            case "ALL" -> Text.literal("All");
            case "CONCEALED" -> Text.literal("Concealed faces");
            case "EDGES" -> Text.literal("Edges");
            case "LOOKAT" -> Text.literal("Looked at");
            default -> Text.literal("you done goofed !.");
        };
    }
}