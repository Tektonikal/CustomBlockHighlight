package tektonikal.customblockhighlight.util;

public class ScreenOutline {
	public static final int MIN_SIDES = 2, MAX_SIDES = 32;

	public boolean enabled = false;
	public int sides = MIN_SIDES;
	public OutlineFit fit = OutlineFit.SURROUNDING;
	public float rotation = 0;
	public boolean keepOnScreen = false;

	public boolean exact() {
		return sides < 3;
	}
}
