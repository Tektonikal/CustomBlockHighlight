package tektonikal.customblockhighlight;

import java.util.Arrays;

import static tektonikal.customblockhighlight.CustomBlockHighlight.easeF;
import static tektonikal.customblockhighlight.CustomBlockHighlight.fadeOut;

public class LineState {
	public float edgeAlpha = 0;
	public final float[] lineFades = new float[6];
	public float cutFromCenter = Float.NaN, cutFromCorner = Float.NaN;

	public void fadeOutSides() {
		for (int d = 0; d < lineFades.length; d++) lineFades[d] = fadeOut(lineFades[d]);
	}

	public void clearFades() {
		Arrays.fill(lineFades, 0);
		edgeAlpha = 0;
	}

	public void easeCuts(float center, float corner, double k) {
		cutFromCenter = ease(cutFromCenter, center, k);
		cutFromCorner = ease(cutFromCorner, corner, k);
	}

	public static float[] animatedCuts(float center, float corner, float progress) {
		float gaps = center + corner, total = 1 - (1 - gaps) * progress;
		if (gaps <= 0) return new float[]{total, 0};
		return new float[]{total * center / gaps, total * corner / gaps};
	}

	static float progress(float prog, boolean miss, boolean in, float inSpeed, boolean out, float outSpeed, boolean visible) {
		if (miss && out) return settle(easeF(prog, 0, outSpeed), 0);
		if (miss && !visible) return 0;
		return in ? settle(easeF(prog, 1, inSpeed), 1) : 1;
	}

	private static float settle(float value, float target) {
		return Math.abs(target - value) < 1F / 512 ? target : value;
	}

	static int faceAlpha(int dirMask, float[] fades, int fallback) {
		if (dirMask == 0) return fallback;
		float alpha = 0;
		for (int d = 0; d < 6; d++) if ((dirMask & 1 << d) != 0) alpha = Math.max(alpha, fades[d]);
		return Math.round(alpha);
	}

	private static float ease(float value, float target, double k) {
		if (Float.isNaN(value)) return target;
		float next = (float) (value + (target - value) * k);
		return Math.abs(target - next) < 1.0E-3F ? target : next;
	}
}
