package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum FaceMode implements NameableEnum {
	AIR_EXPOSED,
	ALL,
	CONCEALED,
	LOOKAT;

	@Override
	public Component getDisplayName() {
		return Component.translatable(switch (this) {
			case ALL -> "cbh.enum.faceMode.all";
			case AIR_EXPOSED -> "cbh.enum.faceMode.airExposed";
			case CONCEALED -> "cbh.enum.faceMode.concealed";
			case LOOKAT -> "cbh.enum.faceMode.lookAt";
		});
	}
}