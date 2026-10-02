package tektonikal.customblockhighlight.legacy;

//? if =1.8.9 {
/*public final class Mth {
	private Mth() {
	}

	public static float sqrt(float value) {
		return (float) Math.sqrt(value);
	}

	public static int floor(float value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	public static int ceil(float value) {
		int i = (int) value;
		return value > i ? i + 1 : i;
	}

	public static int ceil(double value) {
		int i = (int) value;
		return value > i ? i + 1 : i;
	}

	public static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public static float clamp(float value, float min, float max) {
		return value < min ? min : Math.min(value, max);
	}

	public static double clamp(double value, double min, double max) {
		return value < min ? min : Math.min(value, max);
	}

	public static int lerpInt(float delta, int start, int end) {
		return start + floor(delta * (end - start));
	}

	public static float lerp(float delta, float start, float end) {
		return start + delta * (end - start);
	}

	public static double lerp(double delta, double start, double end) {
		return start + delta * (end - start);
	}
}
*///?}
