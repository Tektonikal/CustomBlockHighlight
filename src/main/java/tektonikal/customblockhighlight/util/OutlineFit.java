package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum OutlineFit implements NameableEnum {
	SURROUNDING,
	INSIDE;

	public String key() {
		return this == SURROUNDING ? "cbh.enum.outline_fit.surrounding" : "cbh.enum.outline_fit.inside";
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(key());
	}
}
