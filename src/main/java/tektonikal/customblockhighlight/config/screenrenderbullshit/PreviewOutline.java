package tektonikal.customblockhighlight.config.screenrenderbullshit;

//? if <26.2 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import tektonikal.customblockhighlight.Renderer;
import tektonikal.customblockhighlight.Vertexer;
import tektonikal.customblockhighlight.util.DepthTestMode;

import java.util.List;

//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else {
/^import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderType;
^///?}
//?} else {
/^import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
^///?}

public class PreviewOutline {
	private static final float UNDO_VIEW_SHRINK = 256.0F / 255.0F;

	//? if >=1.21.5 {
	private static final Map<DepthTestMode, RenderType> LINE_TYPES = renderTypes(true);
	private static final Map<DepthTestMode, RenderType> FILL_TYPES = renderTypes(false);

	private static Map<DepthTestMode, RenderType> renderTypes(boolean lines) {
		Map<DepthTestMode, RenderType> types = new EnumMap<>(DepthTestMode.class);
		for (DepthTestMode mode : DepthTestMode.values()) {
			String name = "cbh_preview_" + (lines ? "lines_" : "fill_") + mode.name().toLowerCase(Locale.ROOT);
			RenderPipeline pipeline = Renderer.getPipeline(mode, lines);
			//? if >=1.21.11 {
			RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline);
			if (!lines) setup.sortOnUpload();
			types.put(mode, RenderType.create(name, setup.createRenderSetup()));
			//?} else {
			/^types.put(mode, lines
					? RenderType.create(name, 1536, pipeline, RenderType.CompositeState.builder().createCompositeState(false))
					: RenderType.create(name, 1536, false, true, pipeline, RenderType.CompositeState.builder().createCompositeState(false)));
			^///?}
		}
		return types;
	}
	//?} else {
	/^private static BufferBuilder currentDraw;
	^///?}

	public static void draw(PoseStack stack, MultiBufferSource.BufferSource bufferSource, PresetsScreen.Preset preset) {
		Pair<List<CBHLineRenderInfo>, CBHFillRenderInfo> info = preset.renderInfo.get();

		bufferSource.endBatch();

		CBHFillRenderInfo fillInfo = info.right();
		AABB fillBox = Shapes.block().move(-0.5F, -0.5F, -0.5F).bounds();
		stack.pushPose();
		Vertexer.applyExpansion(stack, fillBox, fillInfo.scaleBlocks(), fillInfo.scalePercent());
		Vertexer.vertexBoxQuads(stack.last(), beginLayer(bufferSource, fillInfo.mode(), false), fillBox.inflate(0.0001), fillInfo.cols(), fillInfo.alphas());
		endLayer(bufferSource, fillInfo.mode(), false, 1F);
		stack.popPose();

		for (CBHLineRenderInfo lineInfo : info.first().reversed()) {
			AABB lineBox = lineInfo.shape().bounds();
			stack.pushPose();
			stack.last().pose().scaleLocal(UNDO_VIEW_SHRINK);
			Vertexer.applyExpansion(stack, lineBox, lineInfo.scaleBlocks(), lineInfo.scalePercent());
			Vertexer.vertexBoxLines(stack.last(), beginLayer(bufferSource, lineInfo.mode(), true), lineBox, lineInfo.cols(), lineInfo.alphas(), lineInfo.width(), lineInfo.cutFromCenter(), lineInfo.cutFromCorner(), lineInfo.outerMult(), lineInfo.innerMult());
			endLayer(bufferSource, lineInfo.mode(), true, lineInfo.width());
			stack.popPose();
		}
	}

	private static VertexConsumer beginLayer(MultiBufferSource.BufferSource bufferSource, DepthTestMode mode, boolean lines) {
		//? if >=1.21.5 {
		return bufferSource.getBuffer((lines ? LINE_TYPES : FILL_TYPES).get(mode));
		//?} else {
		/^currentDraw = Tesselator.getInstance().begin(lines ? VertexFormat.Mode.LINES : VertexFormat.Mode.QUADS, lines ? DefaultVertexFormat.POSITION_COLOR_NORMAL : DefaultVertexFormat.POSITION_COLOR);
		return currentDraw;
		^///?}
	}

	private static void endLayer(MultiBufferSource.BufferSource bufferSource, DepthTestMode mode, boolean lines, float width) {
		//? if >=1.21.11 {
		bufferSource.endBatch();
		//?} elif >=1.21.5 {
		/^RenderSystem.lineWidth(lines ? width : 1F);
		bufferSource.endBatch();
		RenderSystem.lineWidth(1F);
		^///?} else {
		/^MeshData builtBuffer = currentDraw.build();
		currentDraw = null;
		if (builtBuffer == null) return;
		Renderer.setDrawShader(lines);
		RenderSystem.lineWidth(lines ? width : 1F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		Renderer.applyDepth(mode);
		BufferUploader.drawWithShader(builtBuffer);
		Renderer.applyDepth(DepthTestMode.NORMAL);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.lineWidth(1F);
		^///?}
	}
}
*///?}
