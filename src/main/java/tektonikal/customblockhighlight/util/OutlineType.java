package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum OutlineType implements NameableEnum {
	AIR_EXPOSED,
	ALL,
	CONCEALED,
	EDGES,
	LOOK_AT;

	@Override
	public Component getDisplayName() {
		return Component.literal(switch (this) {
			case ALL -> "All";
			case EDGES -> "Edges";
			case AIR_EXPOSED -> "Air Exposed";
			case CONCEALED -> "Concealed Faces";
			case LOOK_AT -> "Looked At";
		});
	}
}