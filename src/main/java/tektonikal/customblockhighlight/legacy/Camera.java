package tektonikal.customblockhighlight.legacy;

//? if =1.8.9 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class Camera {
	public static final Camera MAIN = new Camera();

	private Camera() {
	}

	public Vec3 position() {
		Entity camera = Minecraft.getInstance().getCameraEntity();
		if (camera == null) return Vec3.ZERO;
		float tickDelta = LegacyClient.tickDelta();
		return new Vec3(
				camera.prevX + (camera.x - camera.prevX) * tickDelta,
				camera.prevY + (camera.y - camera.prevY) * tickDelta,
				camera.prevZ + (camera.z - camera.prevZ) * tickDelta
		);
	}
}
*///?}
