package tektonikal.customblockhighlight.config.screenrenderbullshit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Quaternionf;
import tektonikal.customblockhighlight.Vertexer;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.BlockHighlightConfig.LineConfig;
import tektonikal.customblockhighlight.util.BlendMode;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
//? if <26.2 {
/*import tektonikal.customblockhighlight.RenderBackend;
//? if >1.8.9
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=1.21.5 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else {
/^import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderType;
^///?}
//?} elif >1.8.9 {
/^import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.renderpearl.api.vertex.VertexFormat;
^///?} else {
/^import tektonikal.customblockhighlight.legacy.vertex.LegacyVertexBuffer;
^///?}
*///?}

public final class PreviewOutline {
	private PreviewOutline() {
	}

	private static final float UNDO_VIEW_SHRINK = 256.0F / 255.0F;
	private static final AABB CUBE = Shapes.block().move(-0.5F, -0.5F, -0.5F).bounds();

	public static Quaternionf rotation(float xAngle, float yAngle) {
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 30.0F * (float) (Math.PI / 180.0));
		xRotation.rotateLocalY(-xAngle * 30.0F * (float) (Math.PI / 180.0));
		return rotation.mul(xRotation);
	}

	public interface Layers {
		VertexConsumer begin(DepthTestMode mode, boolean lines, boolean screen, BlendMode blend);

		void end(DepthTestMode mode, boolean lines, BlendMode blend, float width);
	}

	public static void draw(PoseStack stack, BlockHighlightConfig cfg, float pixelsPerUnit, Layers layers) {
		float[] fillAlphas = new float[6];
		Arrays.fill(fillAlphas, cfg.fillEnabled ? cfg.fillCol.alpha : 0);
		stack.pushPose();
		Vertexer.applyExpansion(stack, CUBE, cfg.fillExpandBlocks, cfg.fillExpandPercent);
		Vertexer.blend = cfg.fillBlend;
		Vertexer.vertexBoxQuads(stack.last(), layers.begin(cfg.fillDepthTest, false, false, cfg.fillBlend), CUBE.inflate(0.0001), cfg.fillCol.getColors(false, Color.WHITE), fillAlphas);
		layers.end(cfg.fillDepthTest, false, cfg.fillBlend, 1F);
		stack.popPose();

		for (int layer = 2; layer >= 0 && cfg.primary.enabled; layer--) {
			LineConfig lc = cfg.getLineConfig(layer);
			if (!lc.enabled) continue;
			float[] alphas = new float[6];
			Arrays.fill(alphas, lc.color.alpha);
			Pair<Color, Color> cols = lc.color.getColors(false, Color.WHITE);
			stack.pushPose();
			//? if >1.8.9
			stack.last().pose().scaleLocal(UNDO_VIEW_SHRINK);
			Vertexer.applyExpansion(stack, CUBE, lc.lineExpandBlocks, lc.lineExpandPercentage);
			Vertexer.blend = lc.lineBlend;
			if (lc.screenOutline.enabled) {
				Vertexer.vertexGuiScreenBox(stack.last(), layers.begin(lc.lineDepthTest, false, true, lc.lineBlend), List.<Vec3[]>of(Vertexer.corners(CUBE)), lc.screenOutline, cols, lc.color.alpha, lc.lineWidth, lc.cutFromCenter, lc.cutFromCorner, lc.outerThicknessMult, lc.innerThicknessMult, pixelsPerUnit);
				layers.end(lc.lineDepthTest, false, lc.lineBlend, 1F);
			} else {
				Vertexer.vertexBoxLines(stack.last(), layers.begin(lc.lineDepthTest, true, false, lc.lineBlend), CUBE, cols, alphas, lc.lineWidth, lc.cutFromCenter, lc.cutFromCorner, lc.outerThicknessMult, lc.innerThicknessMult);
				layers.end(lc.lineDepthTest, true, lc.lineBlend, lc.lineWidth);
			}
			stack.popPose();
		}
		Vertexer.blend = BlendMode.NORMAL;
	}

	//? if <26.2 {
	/*//? if >=1.21.5 {
	private static final RenderType[] TYPES = new RenderType[BlendMode.values().length * DepthTestMode.values().length * 2];

	private static RenderType renderType(DepthTestMode mode, boolean lines, BlendMode blend) {
		return RenderBackend.cached(TYPES, RenderBackend.pipelineIndex(mode, lines, blend), () -> {
			String name = RenderBackend.typeName(lines ? "cbh_preview_lines_" : "cbh_preview_fill_", mode, blend);
			RenderPipeline pipeline = RenderBackend.getPipeline(mode, lines, blend);
			//? if >=1.21.11 {
			RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline);
			if (!lines) setup.sortOnUpload();
			return RenderType.create(name, setup.createRenderSetup());
			//?} else {
			/^return lines
					? RenderType.create(name, 1536, pipeline, RenderType.CompositeState.builder().createCompositeState(false))
					: RenderType.create(name, 1536, false, true, pipeline, RenderType.CompositeState.builder().createCompositeState(false));
			^///?}
		});
	}
	//?} elif >1.8.9 {
	/^private static BufferBuilder currentDraw;
	^///?}

	//? if >1.8.9 {
	public static void draw(PoseStack stack, MultiBufferSource.BufferSource bufferSource, PresetsScreen.Preset preset, float pixelsPerUnit) {
	//?} else
	//public static void draw(PoseStack stack, LegacyVertexBuffer bufferSource, PresetsScreen.Preset preset, float pixelsPerUnit) {
		//? if >1.8.9 {
		bufferSource.endBatch();
		//?} else
		//bufferSource.beginFrame();
		draw(stack, preset.config(), pixelsPerUnit, new Layers() {
			@Override
			public VertexConsumer begin(DepthTestMode mode, boolean lines, boolean screen, BlendMode blend) {
				return beginLayer(bufferSource, mode, lines, blend);
			}

			@Override
			public void end(DepthTestMode mode, boolean lines, BlendMode blend, float width) {
				endLayer(bufferSource, mode, lines, blend, width);
			}
		});
		//? if =1.8.9
		//bufferSource.drawFrame(0, 0, 0, false);
	}

	//? if >1.8.9 {
	private static VertexConsumer beginLayer(MultiBufferSource.BufferSource bufferSource, DepthTestMode mode, boolean lines, BlendMode blend) {
	//?} else
	//private static VertexConsumer beginLayer(LegacyVertexBuffer bufferSource, DepthTestMode mode, boolean lines, BlendMode blend) {
		//? if >=1.21.5 {
		return bufferSource.getBuffer(renderType(mode, lines, blend));
		//?} elif >1.8.9 {
		/^currentDraw = Tesselator.getInstance().begin(lines ? VertexFormat.Mode.LINES : VertexFormat.Mode.QUADS, lines ? DefaultVertexFormat.POSITION_COLOR_NORMAL : DefaultVertexFormat.POSITION_COLOR);
		return currentDraw;
		^///?} else {
		/^return bufferSource.beginBatch(lines);
		^///?}
	}

	//? if >1.8.9 {
	private static void endLayer(MultiBufferSource.BufferSource bufferSource, DepthTestMode mode, boolean lines, BlendMode blend, float width) {
	//?} else
	//private static void endLayer(LegacyVertexBuffer bufferSource, DepthTestMode mode, boolean lines, BlendMode blend, float width) {
		//? if >=1.21.11 {
		bufferSource.endBatch();
		//?} elif >=1.21.5 {
		/^RenderSystem.lineWidth(lines ? width : 1F);
		bufferSource.endBatch();
		RenderSystem.lineWidth(1F);
		^///?} elif >1.8.9 {
		/^MeshData builtBuffer = currentDraw.build();
		currentDraw = null;
		if (builtBuffer == null) return;
		RenderBackend.setDrawShader(lines);
		RenderSystem.lineWidth(lines ? width : 1F);
		RenderSystem.enableBlend();
		RenderBackend.applyBlend(blend);
		RenderSystem.disableCull();
		RenderBackend.applyDepth(mode);
		BufferUploader.drawWithShader(builtBuffer);
		RenderBackend.applyDepth(DepthTestMode.NORMAL);
		RenderSystem.enableCull();
		RenderBackend.applyBlend(BlendMode.NORMAL);
		RenderSystem.disableBlend();
		RenderSystem.lineWidth(1F);
		^///?} else {
		/^bufferSource.endBatch(mode, blend);
		^///?}
	}
	*///?}
}
