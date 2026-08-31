package tektonikal.customblockhighlight;

import net.minecraft.core.Direction;
import tektonikal.customblockhighlight.util.Line;

import java.util.ArrayList;
import java.util.List;

import static tektonikal.customblockhighlight.Blockhighlight.easeF;
import static tektonikal.customblockhighlight.config.BlockHighlightConfig.getActiveInstance;

public class LineState {
	public float edgeAlpha = 0;
	public final float[] lineFades = new float[6];
	public List<Line> lines = new ArrayList<>();
	public List<Line> toRemove = new ArrayList<>();

	public LineState() {

	}

	public float getEdgeAlpha() {
		return edgeAlpha;
	}

	public float[] getLineFades() {
		return lineFades;
	}

	public void fadeOutSides() {
		for (Direction dir : Direction.values()) {
			lineFades[dir.ordinal()] = getActiveInstance().fadeOut ? easeF(lineFades[dir.ordinal()], 0, getActiveInstance().fadeOutSpeed) : 0;
		}
	}
}
