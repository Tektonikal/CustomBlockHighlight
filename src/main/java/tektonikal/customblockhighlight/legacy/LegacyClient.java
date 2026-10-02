package tektonikal.customblockhighlight.legacy;

//? if =1.8.9 {
/*public final class LegacyClient {
	private static float tickDelta;
	private static long lastFrameNs = System.nanoTime();
	private static long frameTimeNs = 16_666_666L;

	private LegacyClient() {
	}

	public static void onFrame(float tickDelta) {
		LegacyClient.tickDelta = tickDelta;
		long now = System.nanoTime();
		frameTimeNs = now - lastFrameNs;
		lastFrameNs = now;
	}

	public static float tickDelta() {
		return tickDelta;
	}

	public static long frameTimeNs() {
		return frameTimeNs;
	}
}
*///?}
