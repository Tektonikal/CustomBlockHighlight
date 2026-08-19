package tektonikal.customblockhighlight.config.screenrenderbullshit;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

public record EvilRenderState(
		float y,
		PresetsScreen.Preset preset,
		int x0,
		int y0,
		int x1,
		int y1,
		float scale,
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
	public EvilRenderState(
			final float y,
			final PresetsScreen.Preset preset,
			final int x0,
			final int y0,
			final int x1,
			final int y1,
			final float scale,
			@Nullable final ScreenRectangle scissorArea
	) {
		this(y, preset, x0, y0, x1, y1, scale, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
	}
}
