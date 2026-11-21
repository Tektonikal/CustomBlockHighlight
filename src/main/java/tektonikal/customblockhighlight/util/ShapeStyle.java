package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum ShapeStyle implements NameableEnum {
	CLASSIC_BOX,
	COLLISION_SHAPE,
	MODEL_SHAPE;

	public String key() {
		return switch (this) {
			case CLASSIC_BOX -> "cbh.enum.shape_style.classic";
			case COLLISION_SHAPE -> "cbh.enum.shape_style.collision";
			case MODEL_SHAPE -> "cbh.enum.shape_style.model";
		};
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(key());
	}
}
