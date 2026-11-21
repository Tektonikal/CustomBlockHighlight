package tektonikal.customblockhighlight.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;

public enum BlendMode implements NameableEnum {
	NORMAL(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA),
	ADDITIVE(GL_SRC_ALPHA, GL_ONE),
	MULTIPLY(GL_ZERO, GL_SRC_COLOR),
	SCREEN(GL_ONE, GL_ONE_MINUS_SRC_COLOR),
	INVERT(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR),
	// 1.21.5 to 26.1 pipelines can't change the blend op
	//? if >=26.2 || <1.21.5 {
	SUBTRACT(GL_SRC_ALPHA, GL_ONE, GL_FUNC_REVERSE_SUBTRACT),
	LIGHTEN(GL_ONE, GL_ONE, GL_MAX),
	DARKEN(GL_ONE, GL_ONE, GL_MIN),
	//?}
	;

	public final int glSrc;
	public final int glDst;
	public final int glEquation;

	BlendMode(int glSrc, int glDst) {
		this(glSrc, glDst, GL_FUNC_ADD);
	}

	BlendMode(int glSrc, int glDst, int glEquation) {
		this.glSrc = glSrc;
		this.glDst = glDst;
		this.glEquation = glEquation;
	}

	public String key() {
		return "cbh.enum.blendMode." + name().toLowerCase(Locale.ROOT);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(key());
	}
}
