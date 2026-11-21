package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum OutlineSides implements NameableEnum {
	EXACT(ScreenOutline.MIN_SIDES),
	TRIANGLE(3),
	SQUARE(4),
	CIRCLE(ScreenOutline.MAX_SIDES);

	public final int count;

	OutlineSides(int count) {
		this.count = count;
	}

	public static OutlineSides of(int count) {
		OutlineSides nearest = EXACT;
		for (OutlineSides sides : values()) if (Math.abs(sides.count - count) < Math.abs(nearest.count - count)) nearest = sides;
		return nearest;
	}

	public String key() {
		return "cbh.config.screen_sides." + name().toLowerCase(Locale.ROOT);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(key());
	}
}
