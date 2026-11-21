package tektonikal.customblockhighlight;

//? if >=1.21.5 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;

public final class IrisCompat {
	private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("iris");

	static void assign(RenderPipeline pipeline, boolean lines) {
		if (LOADED) Api.assign(pipeline, lines);
	}

	static boolean isShaderPackInUse() {
		return LOADED && Api.packInUse();
	}

	static boolean isRenderingShadowPass() {
		return LOADED && Api.shadowPass();
	}

	private static final class Api {
		private static final boolean CAN_ASSIGN = IrisApi.getInstance().getMinorApiRevision() >= 3;

		static void assign(RenderPipeline pipeline, boolean lines) {
			if (!CAN_ASSIGN) return;
			try {
				IrisApi.getInstance().assignPipeline(pipeline, lines ? IrisProgram.LINES : IrisProgram.BASIC);
			} catch (IllegalStateException ignored) {
			}
		}

		static boolean packInUse() {
			return IrisApi.getInstance().isShaderPackInUse();
		}

		static boolean shadowPass() {
			return IrisApi.getInstance().isRenderingShadowPass();
		}
	}
}
//?}
