package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum DepthTestMode implements NameableEnum {
	//GREATER_THAN_OR_EQUAL
	NORMAL,
	ALWAYS_PASS,
	//LESS_THAN
	HIDDEN_ONLY;

	@Override
	public Component getDisplayName() {
		return Component.literal(switch (this) {
			case NORMAL -> "Normal";
			case ALWAYS_PASS -> "Always Pass";
			case HIDDEN_ONLY -> "Only Concealed";
		});
	}
}
