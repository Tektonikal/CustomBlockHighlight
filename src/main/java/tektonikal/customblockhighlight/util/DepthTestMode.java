package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum DepthTestMode implements NameableEnum {
	NORMAL,
	ALWAYS_PASS,
	HIDDEN_ONLY;

	public String key() {
		return switch (this) {
			case NORMAL -> "cbh.enum.depthTestMode.normal";
			case ALWAYS_PASS -> "cbh.enum.depthTestMode.alwaysPass";
			case HIDDEN_ONLY -> "cbh.enum.depthTestMode.hiddenOnly";
		};
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(key());
	}
}
