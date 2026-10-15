package tektonikal.customblockhighlight.util;

import tektonikal.customblockhighlight.legacy.NameableEnum;
import tektonikal.customblockhighlight.legacy.Component;

public enum OutlineStyle implements NameableEnum  {
	CLASSIC_LINE,
	FLAT,
	SCREEN;

    @Override
    public Component getDisplayName() {
        return Component.translatable(switch (this) {
            case CLASSIC_LINE -> "cbh.enum.outlineStyle.classic";
            case FLAT -> "cbh.enum.outlineStyle.flat";
            case SCREEN -> "cbh.enum.outlineStyle.screen";
        });
    }
}
