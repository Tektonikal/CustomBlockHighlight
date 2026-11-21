package tektonikal.customblockhighlight.legacy;

//? if =1.8.9 {
/*import net.minecraft.client.options.KeyBinding;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;

public final class LegacyClient {
	private static float tickDelta;
	private static KeyBinding toggleKey;

	private LegacyClient() {
	}

	public static void onFrame(float tickDelta) {
		LegacyClient.tickDelta = tickDelta;
	}

	public static float tickDelta() {
		return tickDelta;
	}

	public static void registerToggleKey() {
		KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> toggleKey = registry.register("cbh.key.toggle", 0, "key.categories.misc"));
	}

	public static boolean toggleKeyPressed() {
		return toggleKey != null && toggleKey.consumeClick();
	}
}
*///?}
