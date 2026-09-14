package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum DepthTestMode implements NameableEnum {
	//GREATER_THAN_OR_EQUAL
	NORMAL,
	//ALWAYS_PASS
	ALWAYS_PASS,
	//LESS_THAN
	HIDDEN_ONLY;

	@Override
	public Component getDisplayName() {
		return Component.translatable(switch (this) {
			case NORMAL -> "cbh.enum.depthTestMode.normal";
			case ALWAYS_PASS -> "cbh.enum.depthTestMode.alwaysPass";
			case HIDDEN_ONLY -> "cbh.enum.depthTestMode.hiddenOnly";
		});
	}
}
