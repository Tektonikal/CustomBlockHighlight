package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum FaceMode implements NameableEnum {
	AIR_EXPOSED,
	ALL,
	CONCEALED,
	EDGES,
	LOOKAT;

	@Override
	public Component getDisplayName() {
		return Component.translatable(switch (this) {
			case ALL -> "cbh.config.enum.faceMode.all";
            //THIS HAS TO GO!
			case EDGES -> "Edges";

			case AIR_EXPOSED -> "cbh.config.enum.faceMode.airExposed";
			case CONCEALED -> "cbh.config.enum.faceMode.concealed";
			case LOOKAT -> "cbh.config.enum.faceMode.lookAt";
		});
	}
}