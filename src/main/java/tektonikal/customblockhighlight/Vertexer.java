package tektonikal.customblockhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import tektonikal.customblockhighlight.util.BlendMode;
import tektonikal.customblockhighlight.util.OutlineFit;
import tektonikal.customblockhighlight.util.ScreenOutline;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
//? if =1.8.9 {
/*import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
*///?}

public final class Vertexer {
	private Vertexer() {
	}

	public static int getLerpedRGB(int c1, int c2, float percent) {
		int r = Math.clamp(Mth.lerpInt(percent, c1 >> 16 & 0xFF, c2 >> 16 & 0xFF), 0, 255);
		int g = Math.clamp(Mth.lerpInt(percent, c1 >> 8 & 0xFF, c2 >> 8 & 0xFF), 0, 255);
		int b = Math.clamp(Mth.lerpInt(percent, c1 & 0xFF, c2 & 0xFF), 0, 255);
		return r << 16 | g << 8 | b;
	}

	public static void applyExpansion(PoseStack stack, AABB box, float scaleBlocks, float scalePercent) {
		Vector3f scale = expansionScale(box, scaleBlocks, scalePercent);
		stack.scale(scale.x, scale.y, scale.z);
	}

	public static Vector3f expansionScale(AABB box, float blocks, float percent) {
		return new Vector3f(grow(box.getXsize(), blocks), grow(box.getYsize(), blocks), grow(box.getZsize(), blocks)).mul(percent);
	}

	private static float grow(double size, float blocks) {
		return (float) Math.max((size + 2 * blocks) / size, 1.0E-3);
	}

	public static void vertexBoxQuads(PoseStack.Pose pose, VertexConsumer builder, AABB box, Pair<Color, Color> cols, float[] alpha) {
		float normaliser = (float) box.getMinPosition().distanceTo(box.getMaxPosition());
		vertexQuad(pose, builder, cols, Math.round(alpha[0]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.minY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.minY, (float) box.maxZ));
		vertexQuad(pose, builder, cols, Math.round(alpha[1]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.minZ));
		vertexQuad(pose, builder, cols, Math.round(alpha[2]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.minY, (float) box.minZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.minZ));
		vertexQuad(pose, builder, cols, Math.round(alpha[3]), box.getMinPosition(), normaliser, new Vec3((float) box.maxX, (float) box.minY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.minY, (float) box.maxZ));
		vertexQuad(pose, builder, cols, Math.round(alpha[4]), box.getMinPosition(), normaliser, new Vec3((float) box.minX, (float) box.minY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.minX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.minX, (float) box.minY, (float) box.minZ));
		vertexQuad(pose, builder, cols, Math.round(alpha[5]), box.getMinPosition(), normaliser, new Vec3((float) box.maxX, (float) box.minY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.minZ), new Vec3((float) box.maxX, (float) box.maxY, (float) box.maxZ), new Vec3((float) box.maxX, (float) box.minY, (float) box.maxZ));
	}

	public static void vertexQuad(PoseStack.Pose pose, VertexConsumer builder, Pair<Color, Color> cols, int alpha, Vec3 minPos, float normaliser, Vec3... vecs) {
		if(alpha < 1) return;
		int c1 = cols.first().getRGB() & 0xFFFFFF, c2 = cols.second().getRGB() & 0xFFFFFF;
		for (int i = 3; i >= 0; i--) {
			int rgb = getLerpedRGB(c1, c2, (float) (minPos.distanceTo(vecs[i]) / normaliser));
			color(builder.addVertex(pose, vecs[i].toVector3f()), rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha);
		}
	}

	public static void vertexBoxLines(PoseStack.Pose pose, VertexConsumer builder, AABB box, Pair<Color, Color> cols, float[] alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		float x1 = (float) box.minX;
		float y1 = (float) box.minY;
		float z1 = (float) box.minZ;
		float x2 = (float) box.maxX;
		float y2 = (float) box.maxY;
		float z2 = (float) box.maxZ;
		double normaliser = box.getMinPosition().distanceTo(box.getMaxPosition());
		int first = cols.first().getRGB() & 0xFFFFFF;
		int second = cols.second().getRGB() & 0xFFFFFF;
		int x1y1z1 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y1, z1)) / normaliser));
		int x2y1z1 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y1, z1)) / normaliser));
		int x1y1z2 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y1, z2)) / normaliser));
		int x1y2z2 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y2, z2)) / normaliser));
		int x2y2z2 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y2, z2)) / normaliser));
		int x2y2z1 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y2, z1)) / normaliser));
		int x1y2z1 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x1, y2, z1)) / normaliser));
		int x2y1z2 = getLerpedRGB(first, second, (float) (box.getMinPosition().distanceTo(new Vec3(x2, y1, z2)) / normaliser));

		vertexLine(pose, builder, x1, y1, z1, x2, y1, z1, x1y1z1, x2y1z1, Math.round(Math.max(alpha[0], alpha[2])), 1, 0, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x1, y1, z1, x1, y1, z2, x1y1z1, x1y1z2, Math.round(Math.max(alpha[4], alpha[0])), 0, 0, 1, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x2, y1, z1, x2, y1, z2, x2y1z1, x2y1z2, Math.round(Math.max(alpha[5], alpha[0])), 0, 0, 1, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x1, y1, z2, x2, y1, z2, x1y1z2, x2y1z2, Math.round(Math.max(alpha[3], alpha[0])), 1, 0, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x1, y1, z2, x1, y2, z2, x1y1z2, x1y2z2, Math.round(Math.max(alpha[3], alpha[4])), 0, 1, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x1, y1, z1, x1, y2, z1, x1y1z1, x1y2z1, Math.round(Math.max(alpha[2], alpha[4])), 0, 1, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x2, y1, z2, x2, y2, z2, x2y1z2, x2y2z2, Math.round(Math.max(alpha[3], alpha[5])), 0, 1, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x2, y1, z1, x2, y2, z1, x2y1z1, x2y2z1, Math.round(Math.max(alpha[2], alpha[5])), 0, 1, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);

		vertexLine(pose, builder, x1, y2, z1, x2, y2, z1, x1y2z1, x2y2z1, Math.round(Math.max(alpha[2], alpha[1])), 1, 0, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x1, y2, z1, x1, y2, z2, x1y2z1, x1y2z2, Math.round(Math.max(alpha[4], alpha[1])), 0, 0, 1, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x2, y2, z1, x2, y2, z2, x2y2z1, x2y2z2, Math.round(Math.max(alpha[5], alpha[1])), 0, 0, 1, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
		vertexLine(pose, builder, x1, y2, z2, x2, y2, z2, x1y2z2, x2y2z2, Math.round(Math.max(alpha[3], alpha[1])), 1, 0, 0, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}

	private static final int[] viewport = new int[4];
	private static final Matrix4f viewProjection = new Matrix4f(), inverseViewProjection = new Matrix4f();
	private static final PoseStack.Pose IDENTITY = new PoseStack().last();
	//? if =1.8.9
	//private static final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);

	public static Vec3 screenSpaceToWorldSpace(double x, double y, double d) {
		Vector3f target = inverseViewProjection.unprojectInv((float) x, (float) (viewport[3] - y), (float) d, viewport, new Vector3f());
		return new Vec3(target.x, target.y, target.z).add(Renderer.camera.position());
	}

	public static Vec3 worldSpaceToScreenSpace(Vec3 pos) {
		Vec3 delta = pos.subtract(Renderer.camera.position());
		Vector3f target = viewProjection.project((float) delta.x, (float) delta.y, (float) delta.z, viewport, new Vector3f());
		return new Vec3(target.x, viewport[3] - target.y, target.z);
	}

	private static void updateScreenMatrices() {
		//? if >1.8.9 {
		viewport[2] = Renderer.mc.getWindow().getWidth();
		viewport[3] = Renderer.mc.getWindow().getHeight();
		//?} else {
		/*viewport[2] = Renderer.mc.width;
		viewport[3] = Renderer.mc.height;
		*///?}
		//? if >1.8.9 {
		Renderer.lastModMat.rotation(Renderer.camera.rotation().conjugate(new Quaternionf()));
		//?} else {
		/*
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, matrix);
		Renderer.lastProjMat.set(matrix);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
		Renderer.lastModMat.set(matrix);
		*///?}
		viewProjection.set(Renderer.lastProjMat).mul(Renderer.lastModMat);
		viewProjection.invert(inverseViewProjection);
		updateOrigin();
		originToEye.set(originToCamera).sub(eyeFromCamera(Renderer.lastModMat));
	}

	private static final Vector3f originToCamera = new Vector3f();
	private static final Vector3f originToEye = new Vector3f();

	static Vector3f eyeFromCamera(Matrix4f view) {
		return view.invert(new Matrix4f()).getTranslation(new Vector3f());
	}

	private static void updateOrigin() {
		Vec3 offset = Renderer.renderOrigin().subtract(Renderer.camera.position());
		originToCamera.set((float) offset.x, (float) offset.y, (float) offset.z);
	}

	public static void vertexScreenBox(PoseStack.Pose pose, VertexConsumer builder, List<Vec3[]> parts, ScreenOutline outline, Pair<Color, Color> cols, int alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		updateScreenMatrices();
		screenBox(0, pose, builder, parts, outline, cols, alpha, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}

	public static void vertexGuiScreenBox(PoseStack.Pose pose, VertexConsumer builder, List<Vec3[]> parts, ScreenOutline outline, Pair<Color, Color> cols, int alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult, float pixelsPerUnit) {
		screenBox(pixelsPerUnit, pose, builder, parts, outline, cols, alpha, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}

	public static void vertexScreenFill(PoseStack.Pose pose, VertexConsumer builder, List<Vec3[]> parts, ScreenOutline outline, Pair<Color, Color> cols, int alpha) {
		if (alpha < 1 || parts.isEmpty()) return;
		updateScreenMatrices();
		ScreenShape screen = screenShape(0, pose, parts, outline, 0);
		if (screen != null) fillRings(screen.rings(), screen.vertex(builder, cols, alpha, 0));
	}

	private static void screenBox(float guiPixelsPerUnit, PoseStack.Pose pose, VertexConsumer builder, List<Vec3[]> parts, ScreenOutline outline, Pair<Color, Color> cols, int alpha, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		if (alpha < 1 || width <= 0 || parts.isEmpty()) return;
		ScreenShape screen = screenShape(guiPixelsPerUnit, pose, parts, outline, width * Math.max(outerMult, innerMult) / 2 + 1);
		if (screen == null) return;
		ScreenVertex vertex = screen.vertex(builder, cols, alpha, guiPixelsPerUnit);
		for (List<Vector2f> ring : screen.rings()) vertexRing(ring, vertex, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}

	static FloatArrayList depthCopy;

	private record ScreenShape(List<List<Vector2f>> rings, Vector2f min, float normaliser, double depth) {
		ScreenVertex vertex(VertexConsumer builder, Pair<Color, Color> cols, int alpha, float guiPixelsPerUnit) {
			boolean gui = guiPixelsPerUnit > 0;
			Vec3 cam = gui ? Vec3.ZERO : Renderer.renderOrigin();
			int c1 = cols.first().getRGB() & 0xFFFFFF, c2 = cols.second().getRGB() & 0xFFFFFF;
			PoseStack.Pose identity = IDENTITY;
			Vec3[] face = gui ? null : Renderer.lookedAtFace();
			Vector3f normal = new Vector3f();
			float planeAt = 0;
			if (face != null) {
				face[1].subtract(face[0]).toVector3f().cross(face[2].subtract(face[0]).toVector3f(), normal).normalize();
				planeAt = normal.dot(face[0].subtract(cam).toVector3f().add(originToEye));
			}
			boolean onFace = face != null && Math.abs(planeAt) > NEAR * 2;
			float plane = planeAt;
			return (p, offset, opacity) -> {
				float x = p.x + offset.x, y = p.y + offset.y;
				Vector3f out = gui ? new Vector3f(x / guiPixelsPerUnit, y / guiPixelsPerUnit, (float) depth) : screenSpaceToWorldSpace(x, y, depth).subtract(cam).toVector3f();
				if (onFace) out.add(originToEye).mul(towardPlane(normal, plane, out)).sub(originToEye);
				int c = getLerpedRGB(c1, c2, p.distance(min) / normaliser);
				int a = Math.round(alpha * opacity);
				if (depthCopy != null) {
					depthCopy.add(out.x);
					depthCopy.add(out.y);
					depthCopy.add(out.z);
					depthCopy.add(a);
				}
				color(builder.addVertex(identity, out), c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, a);
			};
		}
	}

	static float towardPlane(Vector3f normal, float planeAt, Vector3f fromEye) {
		float t = planeAt / normal.dot(fromEye);
		return t > 0 && t < 1 ? t * Renderer.HIDDEN_PULL : 1;
	}

	//? if >=26.2 {
	static boolean behindPlane(Vector3f normal, float planeAt, Matrix4f pose, AABB box) {
		Vector3f corner = new Vector3f();
		for (Vec3 c : corners(box)) {
			if (normal.dot(pose.transformPosition((float) c.x, (float) c.y, (float) c.z, corner)) > planeAt - 1.0E-3F) return false;
		}
		return true;
	}
	//?}

	record ShapeKey(List<List<Vec3>> parts, Matrix4f pose, Matrix4f viewProjection, int width, int height, Vec3 origin, Vec3 camera, float guiPixelsPerUnit,
					int sides, OutlineFit fit, float rotation, boolean keepOnScreen, float inset) {
		static ShapeKey of(List<Vec3[]> parts, Matrix4f pose, Matrix4f viewProjection, int width, int height, Vec3 origin, Vec3 camera, float guiPixelsPerUnit, ScreenOutline outline, float inset) {
			List<List<Vec3>> copied = new ArrayList<>(parts.size());
			for (Vec3[] part : parts) copied.add(List.of(part));
			return new ShapeKey(copied, new Matrix4f(pose), new Matrix4f(viewProjection), width, height, origin, camera, guiPixelsPerUnit, outline.sides, outline.fit, outline.rotation, outline.keepOnScreen, inset);
		}
	}

	private static final Map<ShapeKey, ScreenShape> AREA_SHAPES = recent(8);

	private static final Map<List<List<Vector2f>>, Area> SILHOUETTES = recent(4);

	record SpotKey(List<List<Vec3>> parts, int sides, float rotation) {
	}

	private static final Map<SpotKey, float[]> SPOTS = recent(8);

	private static <K, V> Map<K, V> recent(int kept) {
		return new LinkedHashMap<>(16, 0.75F, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > kept;
			}
		};
	}

	private static ScreenShape screenShape(float guiPixelsPerUnit, PoseStack.Pose pose, List<Vec3[]> parts, ScreenOutline outline, float inset) {
		if (!outline.exact() && outline.fit == OutlineFit.SURROUNDING) return computeScreenShape(guiPixelsPerUnit, pose, parts, null, outline, inset);
		ShapeKey key = ShapeKey.of(parts, pose.pose(), viewProjection, viewport[2], viewport[3], guiPixelsPerUnit > 0 ? Vec3.ZERO : Renderer.renderOrigin(), guiPixelsPerUnit > 0 ? Vec3.ZERO : Renderer.camera.position(), guiPixelsPerUnit, outline, inset);
		return AREA_SHAPES.computeIfAbsent(key, k -> computeScreenShape(guiPixelsPerUnit, pose, parts, k.parts(), outline, inset));
	}

	private static ScreenShape computeScreenShape(float guiPixelsPerUnit, PoseStack.Pose pose, List<Vec3[]> parts, List<List<Vec3>> copied, ScreenOutline outline, float inset) {
		boolean gui = guiPixelsPerUnit > 0;
		Vec3 cam = gui ? Vec3.ZERO : Renderer.renderOrigin();
		double[] depth = {gui ? -Double.MAX_VALUE : 1, Double.MAX_VALUE};
		List<List<Vector2f>> partCorners = new ArrayList<>(parts.size());
		Vector3f low = new Vector3f(Float.MAX_VALUE), high = new Vector3f(-Float.MAX_VALUE);
		for (Vec3[] part : parts) {
			Vector3f[] corners = new Vector3f[part.length];
			for (int i = 0; i < part.length; i++) {
				low.min(new Vector3f((float) part[i].x, (float) part[i].y, (float) part[i].z));
				high.max(new Vector3f((float) part[i].x, (float) part[i].y, (float) part[i].z));
				corners[i] = pose.pose().transformPosition((float) part[i].x, (float) part[i].y, (float) part[i].z, new Vector3f());
			}
			List<Vector2f> onScreen = new ArrayList<>(corners.length);
			for (Vector3f p : gui ? List.of(corners) : cutAtNearPlane(corners)) onScreen.add(toScreen(p, guiPixelsPerUnit, cam, depth));
			if (!onScreen.isEmpty()) partCorners.add(onScreen);
		}
		if (partCorners.isEmpty()) return null;
		List<Vector2f> all = partCorners.stream().flatMap(List::stream).toList();
		List<List<Vector2f>> rings;
		if (outline.exact()) {
			rings = union(partCorners);
		} else {
			Vector3f mid = low.add(high).div(2);
			Vector3f middle = pose.pose().transformPosition(mid.x, mid.y, mid.z, new Vector3f());
			Vector2f center;
			if (gui || clipW(middle) >= NEAR) {
				center = toScreen(middle, guiPixelsPerUnit, cam, depth.clone());
			} else {
				center = mean(all);
			}
			if (outline.fit == OutlineFit.SURROUNDING) {
				rings = List.of(polygon(all, center, outline));
			} else {
				float[] spot = SPOTS.computeIfAbsent(new SpotKey(copied, outline.sides, outline.rotation), k -> new float[]{Float.NaN, 0, 0});
				rings = List.of(inside(partCorners, center, outline, spot));
			}
		}
		if (outline.keepOnScreen && !gui) rings = keepOnScreen(rings, inset);
		Vector2f min = new Vector2f(Float.MAX_VALUE), max = new Vector2f(-Float.MAX_VALUE);
		for (Vector2f p : all) {
			min.min(p);
			max.max(p);
		}
		List<List<Vector2f>> cleaned = new ArrayList<>(rings.size());
		for (List<Vector2f> ring0 : rings) {
			List<Vector2f> ring = new ArrayList<>(ring0.size());
			for (Vector2f p : ring0) if (ring.isEmpty() || ring.getLast().distance(p) > 0.5F) ring.add(p);
			if (ring.size() > 1 && ring.getLast().distance(ring.getFirst()) <= 0.5F) ring.removeLast();
			if (ring.size() < 3 || Math.abs(area(ring)) < 4) continue;
			cleaned.add(ring);
		}
		return new ScreenShape(cleaned, min, Math.max(min.distance(max), 1), depth[0]);
	}

	static void fillRings(List<List<Vector2f>> rings, ScreenVertex vertex) {
		List<Vector2f[]> edges = new ArrayList<>();
		TreeSet<Float> heights = new TreeSet<>();
		for (List<Vector2f> ring : rings) {
			for (int i = 0; i < ring.size(); i++) {
				Vector2f a = ring.get(i), b = ring.get((i + 1) % ring.size());
				heights.add(a.y);
				if (a.y != b.y) edges.add(a.y < b.y ? new Vector2f[]{a, b} : new Vector2f[]{b, a});
			}
		}
		Vector2f none = new Vector2f();
		Float top = null;
		for (float bottom : heights) {
			if (top != null) {
				float middle = (top + bottom) / 2;
				List<Vector2f[]> crossing = new ArrayList<>();
				for (Vector2f[] edge : edges) if (edge[0].y < middle && edge[1].y > middle) crossing.add(edge);
				crossing.sort(Comparator.comparingDouble(edge -> xAt(edge, middle)));
				for (int i = 0; i + 1 < crossing.size(); i += 2) {
					Vector2f[] left = crossing.get(i), right = crossing.get(i + 1);
					vertex.add(new Vector2f(xAt(left, top), top), none, 1);
					vertex.add(new Vector2f(xAt(right, top), top), none, 1);
					vertex.add(new Vector2f(xAt(right, bottom), bottom), none, 1);
					vertex.add(new Vector2f(xAt(left, bottom), bottom), none, 1);
				}
			}
			top = bottom;
		}
	}

	private static float xAt(Vector2f[] edge, float y) {
		return edge[0].x + (edge[1].x - edge[0].x) * (y - edge[0].y) / (edge[1].y - edge[0].y);
	}

	private static final float NEAR = 0.05F;

	//keep the nearest point's depth in depth[0] and squared distance in depth[1]
	private static Vector2f toScreen(Vector3f p, float guiPixelsPerUnit, Vec3 cam, double[] depth) {
		if (guiPixelsPerUnit > 0) {
			depth[0] = Math.max(depth[0], p.z);
			return new Vector2f(p.x * guiPixelsPerUnit, p.y * guiPixelsPerUnit);
		}
		Vec3 screen = worldSpaceToScreenSpace(cam.add(p.x, p.y, p.z));
		float distance = p.distanceSquared(-originToEye.x, -originToEye.y, -originToEye.z);
		if (distance < depth[1]) {
			depth[0] = screen.z;
			depth[1] = distance;
		}
		return new Vector2f((float) screen.x, (float) screen.y);
	}

	private static float clipW(Vector3f p) {
		return viewProjection.transform(new Vector4f(p.x + originToCamera.x, p.y + originToCamera.y, p.z + originToCamera.z, 1)).w;
	}

	private static List<Vector3f> cutAtNearPlane(Vector3f[] corners) {
		float[] w = new float[corners.length];
		for (int i = 0; i < corners.length; i++) w[i] = clipW(corners[i]);
		return cutAtNearPlane(corners, w);
	}

	static List<Vector3f> cutAtNearPlane(Vector3f[] corners, float[] w) {
		List<Vector3f> kept = new ArrayList<>(corners.length);
		for (int i = 0; i < corners.length; i++) {
			if (w[i] < NEAR) continue;
			kept.add(corners[i]);
			for (int j = 0; j < corners.length; j++) {
				if (w[j] < NEAR) kept.add(new Vector3f(corners[i]).lerp(corners[j], (NEAR - w[i]) / (w[j] - w[i])));
			}
		}
		return kept;
	}

	public static Vec3[] corners(AABB box) {
		Vec3[] corners = new Vec3[8];
		for (int i = 0; i < 8; i++) corners[i] = new Vec3((i & 1) == 0 ? box.minX : box.maxX, (i & 2) == 0 ? box.minY : box.maxY, (i & 4) == 0 ? box.minZ : box.maxZ);
		return corners;
	}

	private static final int[] FACE_AXIS_BIT = {2, 2, 4, 4, 1, 1};

	public static Vec3[] face(AABB box, int dir) {
		Vec3[] corners = corners(box), face = new Vec3[4];
		int n = 0;
		for (int i = 0; i < 8; i++) if (((i & FACE_AXIS_BIT[dir]) != 0) == ((dir & 1) == 1)) face[n++] = corners[i];
		return face;
	}

	static List<Vector2f> polygon(List<Vector2f> points, Vector2f center, ScreenOutline outline) {
		int sides = outline.sides;
		if (sides >= ScreenOutline.MAX_SIDES) center = smallestCircleCenter(convexHull(points));
		Vector2f[] normals = new Vector2f[sides];
		float[] reach = new float[sides];
		float farthest = -Float.MAX_VALUE;
		for (int k = 0; k < sides; k++) {
			double angle = Math.toRadians(outline.rotation) + 2 * Math.PI * k / sides;
			Vector2f n = normals[k] = new Vector2f((float) Math.cos(angle), (float) Math.sin(angle));
			reach[k] = -Float.MAX_VALUE;
			for (Vector2f p : points) reach[k] = Math.max(reach[k], (p.x - center.x) * n.x + (p.y - center.y) * n.y);
			farthest = Math.max(farthest, reach[k]);
		}
		if (sides >= ScreenOutline.MAX_SIDES) java.util.Arrays.fill(reach, farthest);
		float det = (float) Math.sin(2 * Math.PI / sides);
		List<Vector2f> corners = new ArrayList<>(sides);
		for (int k = 0; k < sides; k++) {
			Vector2f a = normals[k], b = normals[(k + 1) % sides];
			float ha = reach[k], hb = reach[(k + 1) % sides];
			corners.add(new Vector2f((ha * b.y - hb * a.y) / det, (a.x * hb - b.x * ha) / det).add(center));
		}
		return corners;
	}

	static Vector2f smallestCircleCenter(List<Vector2f> points) {
		Vector2f center = new Vector2f(points.getFirst());
		float radius = 0;
		for (int i = 1; i < points.size(); i++) {
			Vector2f p = points.get(i);
			if (p.distance(center) <= radius + 1.0E-3F) continue;
			center = new Vector2f(p);
			radius = 0;
			for (int j = 0; j < i; j++) {
				Vector2f q = points.get(j);
				if (q.distance(center) <= radius + 1.0E-3F) continue;
				center = new Vector2f(p).lerp(q, 0.5F);
				radius = p.distance(center);
				for (int k = 0; k < j; k++) {
					Vector2f s = points.get(k);
					if (s.distance(center) <= radius + 1.0E-3F) continue;
					float qx = q.x - p.x, qy = q.y - p.y, sx = s.x - p.x, sy = s.y - p.y;
					float d = 2 * (qx * sy - qy * sx);
					if (Math.abs(d) < 1.0E-6F) continue;
					float q2 = qx * qx + qy * qy, s2 = sx * sx + sy * sy;
					center = new Vector2f(p.x + (sy * q2 - qy * s2) / d, p.y + (qx * s2 - sx * q2) / d);
					radius = p.distance(center);
				}
			}
		}
		return center;
	}

	//spot keeps track of the last outline's middle as a share of the bounds, then how often it was reused; NaN for none. updated in place
	static List<Vector2f> inside(List<List<Vector2f>> partCorners, Vector2f center, ScreenOutline outline, float[] spot) {
		Area shape = silhouette(partCorners);
		if (!shape.contains(center.x, center.y)) {
			List<Vector2f> biggest = List.of();
			for (List<Vector2f> corners : partCorners) {
				List<Vector2f> hull = convexHull(corners);
				if (hull.size() >= 3 && (biggest.isEmpty() || Math.abs(area(hull)) > Math.abs(area(biggest)))) biggest = hull;
			}
			if (biggest.isEmpty()) return List.of();
			center = mean(biggest);
		}
		List<List<Vector2f>> edges = rings(shape);
		edges.removeIf(ring -> Math.abs(area(ring)) < 4);
		Vector2f[] rays = new Vector2f[outline.sides];
		for (int k = 0; k < rays.length; k++) {
			double angle = Math.toRadians(outline.rotation) + 2 * Math.PI * (k + 0.5) / rays.length;
			rays[k] = new Vector2f((float) Math.cos(angle), (float) Math.sin(angle));
		}
		Rectangle2D bounds = shape.getBounds2D();
		float span = (float) Math.max(bounds.getWidth(), bounds.getHeight());
		List<Vector2f> middle = shrunk(edges, center, rays, radius(edges, center, rays, 0)), near = null;
		Vector2f last = new Vector2f((float) (bounds.getX() + spot[0] * bounds.getWidth()), (float) (bounds.getY() + spot[1] * bounds.getHeight()));
		if (!Float.isNaN(spot[0]) && shape.contains(last.x, last.y)) {
			near = searched(shape, edges, rays, last, span / 32);
			if (Math.abs(area(near)) < Math.abs(area(middle))) near = null;
		}
		List<Vector2f> corners = near;
		if (near == null || ++spot[2] % 8 == 0) {
			corners = searched(shape, edges, rays, center, span / 4);
			if (Math.abs(area(corners)) < Math.abs(area(middle))) corners = middle;
			if (near != null && Math.abs(area(near)) >= Math.abs(area(corners)) * 0.8F) corners = near;
		}
		Vector2f at = mean(corners);
		spot[0] = (float) ((at.x - bounds.getX()) / bounds.getWidth());
		spot[1] = (float) ((at.y - bounds.getY()) / bounds.getHeight());
		return corners;
	}

	private static List<Vector2f> searched(Area shape, List<List<Vector2f>> edges, Vector2f[] rays, Vector2f center, float step) {
		float radius = radius(edges, center, rays, 0);
		for (; step >= 0.5F; step /= 2) {
			for (int moves = 0; moves < 4; moves++) {
				Vector2f better = null;
				for (int way = 0; way < 8; way++) {
					Vector2f moved = new Vector2f(center.x + step * (float) Math.cos(way * Math.PI / 4), center.y + step * (float) Math.sin(way * Math.PI / 4));
					if (!shape.contains(moved.x, moved.y)) continue;
					float movedRadius = radius(edges, moved, rays, radius * 1.0005F);
					if (movedRadius <= radius * 1.0005F) continue;
					radius = movedRadius;
					better = moved;
				}
				if (better == null) break;
				center = better;
			}
		}
		return shrunk(edges, center, rays, radius);
	}

	private static Vector2f mean(List<Vector2f> points) {
		Vector2f mean = new Vector2f();
		for (Vector2f p : points) mean.add(p);
		return mean.div(points.size());
	}

	private static float radius(List<List<Vector2f>> edges, Vector2f center, Vector2f[] rays, float toBeat) {
		float shortest = Float.MAX_VALUE;
		for (Vector2f ray : rays) {
			float reach = Float.MAX_VALUE;
			for (List<Vector2f> ring : edges) {
				for (int i = 0; i < ring.size(); i++) {
					Vector2f a = ring.get(i), b = ring.get((i + 1) % ring.size());
					float ex = b.x - a.x, ey = b.y - a.y, wx = a.x - center.x, wy = a.y - center.y;
					float denom = ray.x * ey - ray.y * ex;
					if (Math.abs(denom) < 1.0E-6F) continue;
					float t = (wx * ey - wy * ex) / denom, u = (wx * ray.y - wy * ray.x) / denom;
					if (t > 0 && u >= 0 && u <= 1) reach = Math.min(reach, t);
				}
			}
			if (reach == Float.MAX_VALUE) return 0;
			if (reach <= toBeat) return reach;
			shortest = Math.min(shortest, reach);
		}
		return shortest;
	}

	private static List<Vector2f> shrunk(List<List<Vector2f>> edges, Vector2f center, Vector2f[] rays, float radius) {
		int n = rays.length;
		float scale = 1;
		float inradius = (float) Math.cos(Math.PI / n);
		for (List<Vector2f> ring : edges) {
			for (Vector2f p : ring) {
				float out = 0;
				for (int k = 0; k < n && radius > 0; k++) {
					Vector2f a = rays[k], b = rays[(k + 1) % n];
					float nx = a.x + b.x, ny = a.y + b.y;
					out = Math.max(out, (nx * (p.x - center.x) + ny * (p.y - center.y)) / (2 * inradius * inradius * radius));
				}
				scale = Math.min(scale, out);
			}
		}
		List<Vector2f> corners = new ArrayList<>(n);
		for (Vector2f ray : rays) corners.add(new Vector2f(ray).mul(radius * scale).add(center));
		return corners;
	}

	private static List<List<Vector2f>> keepOnScreen(List<List<Vector2f>> rings, float inset) {
		Path2D.Float path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
		for (List<Vector2f> ring : rings) {
			if (ring.size() < 3) continue;
			path.moveTo(ring.getFirst().x, ring.getFirst().y);
			for (int i = 1; i < ring.size(); i++) path.lineTo(ring.get(i).x, ring.get(i).y);
			path.closePath();
		}
		Area area = new Area(path);
		area.intersect(new Area(new Rectangle2D.Float(inset, inset, viewport[2] - 2 * inset, viewport[3] - 2 * inset)));
		return rings(area);
	}

	private static List<List<Vector2f>> union(List<List<Vector2f>> partCorners) {
		return rings(silhouette(partCorners));
	}

	private static Area silhouette(List<List<Vector2f>> partCorners) {
		return SILHOUETTES.computeIfAbsent(partCorners, Vertexer::merged);
	}

	//fuck it. do everything at once
	static Area merged(List<List<Vector2f>> partCorners) {
		List<Area> areas = new ArrayList<>();
		for (int from = 0; from < partCorners.size(); from += 6) {
			Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
			for (List<Vector2f> corners : partCorners.subList(from, Math.min(from + 6, partCorners.size()))) {
				List<Vector2f> hull = convexHull(corners);
				if (hull.size() < 3) continue;
				path.moveTo(hull.getFirst().x, hull.getFirst().y);
				for (int i = 1; i < hull.size(); i++) path.lineTo(hull.get(i).x, hull.get(i).y);
				path.closePath();
			}
			areas.add(new Area(path));
		}
		for (int gap = 1; gap < areas.size(); gap *= 2) {
			for (int i = 0; i + gap < areas.size(); i += 2 * gap) areas.get(i).add(areas.get(i + gap));
		}
		return areas.isEmpty() ? new Area() : areas.getFirst();
	}

	private static List<List<Vector2f>> rings(Area area) {
		List<List<Vector2f>> rings = new ArrayList<>();
		float[] coords = new float[6];
		for (PathIterator it = area.getPathIterator(null); !it.isDone(); it.next()) {
			switch (it.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO -> rings.add(new ArrayList<>(List.of(new Vector2f(coords[0], coords[1]))));
				case PathIterator.SEG_LINETO -> rings.getLast().add(new Vector2f(coords[0], coords[1]));
				default -> {
				}
			}
		}
		return rings;
	}

	interface ScreenVertex {
		void add(Vector2f point, Vector2f offset, float opacity);
	}

	private static void vertexRing(List<Vector2f> ring, ScreenVertex vertex, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		int n = ring.size();
		float outer = width * outerMult / 2, inner = width * innerMult / 2;
		Vector2f[] miters = new Vector2f[n];
		for (int i = 0; i < n; i++) {
			Vector2f in = edgeNormal(ring.get((i + n - 1) % n), ring.get(i));
			Vector2f out = edgeNormal(ring.get(i), ring.get((i + 1) % n));
			Vector2f miter = new Vector2f(in).add(out).normalize();
			//TODO: clamped so it doesn't look weird with very sharp corners
			miters[i] = miter.mul(outer / Math.max(miter.dot(out), 0.25F));
		}
		for (int i = 0; i < n; i++) {
			Vector2f a = ring.get(i), b = ring.get((i + 1) % n);
			Vector2f normal = edgeNormal(a, b);
			Vector2f middle = new Vector2f(a).lerp(b, 0.5F);
			Vector2f outerOffset = new Vector2f(normal).mul(outer), innerOffset = new Vector2f(normal).mul(inner);
			quad(vertex, new Vector2f(a).lerp(b, cutFromCorner / 2), cutFromCorner == 0 ? miters[i] : outerOffset, outer, new Vector2f(middle).lerp(a, cutFromCenter), innerOffset, inner);
			quad(vertex, new Vector2f(middle).lerp(b, cutFromCenter), innerOffset, inner, new Vector2f(b).lerp(a, cutFromCorner / 2), cutFromCorner == 0 ? miters[(i + 1) % n] : outerOffset, outer);
		}
	}

	private static void quad(ScreenVertex vertex, Vector2f from, Vector2f fromOffset, float fromHalf, Vector2f to, Vector2f toOffset, float toHalf) {
		Vector2f fromCore = resize(fromOffset, fromHalf, -0.5F), toCore = resize(toOffset, toHalf, -0.5F);
		Vector2f fromEdge = resize(fromOffset, fromHalf, 0.5F), toEdge = resize(toOffset, toHalf, 0.5F);
		strip(vertex, from, fromCore, to, toCore, from, new Vector2f(fromCore).negate(), to, new Vector2f(toCore).negate(), 1, 1);
		strip(vertex, from, fromEdge, to, toEdge, from, fromCore, to, toCore, 0, 1);
		strip(vertex, from, new Vector2f(fromCore).negate(), to, new Vector2f(toCore).negate(), from, new Vector2f(fromEdge).negate(), to, new Vector2f(toEdge).negate(), 1, 0);
	}

	private static void strip(ScreenVertex vertex, Vector2f a, Vector2f aOffset, Vector2f b, Vector2f bOffset, Vector2f c, Vector2f cOffset, Vector2f d, Vector2f dOffset, float firstOpacity, float secondOpacity) {
		vertex.add(a, aOffset, firstOpacity);
		vertex.add(b, bOffset, firstOpacity);
		vertex.add(d, dOffset, secondOpacity);
		vertex.add(c, cOffset, secondOpacity);
	}

	private static Vector2f resize(Vector2f offset, float half, float delta) {
		return half <= 0 ? new Vector2f() : new Vector2f(offset).mul(Math.max(half + delta, 0) / half);
	}

	private static Vector2f edgeNormal(Vector2f from, Vector2f to) {
		Vector2f dir = new Vector2f(to).sub(from).normalize();
		return new Vector2f(-dir.y, dir.x);
	}

	private static float area(List<Vector2f> polygon) {
		float area = 0;
		for (int i = 0; i < polygon.size(); i++) area += cross(new Vector2f(), polygon.get(i), polygon.get((i + 1) % polygon.size()));
		return area;
	}

	static List<Vector2f> convexHull(List<Vector2f> points) {
		List<Vector2f> sorted = new ArrayList<>(points);
		sorted.sort(Comparator.<Vector2f>comparingDouble(p -> p.x).thenComparingDouble(p -> p.y));
		List<Vector2f> hull = new ArrayList<>();
		for (int pass = 0; pass < 2; pass++) {
			int start = hull.size();
			for (Vector2f p : sorted) {
				while (hull.size() >= start + 2 && cross(hull.get(hull.size() - 2), hull.getLast(), p) <= 0) hull.removeLast();
				hull.add(p);
			}
			hull.removeLast();
			Collections.reverse(sorted);
		}
		return hull;
	}

	private static float cross(Vector2f o, Vector2f a, Vector2f b) {
		return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
	}

	public static BlendMode blend = BlendMode.NORMAL;

	private static VertexConsumer color(VertexConsumer vertex, int r, int g, int b, int a) {
		return switch (blend) {
			case MULTIPLY -> vertex.setColor(255 - (255 - r) * a / 255, 255 - (255 - g) * a / 255, 255 - (255 - b) * a / 255, 255);
			case SCREEN, INVERT -> vertex.setColor(r * a / 255, g * a / 255, b * a / 255, a);
			//? if >=26.2 || <1.21.5 {
			case LIGHTEN -> vertex.setColor(r * a / 255, g * a / 255, b * a / 255, a);
			case DARKEN -> vertex.setColor(255 - (255 - r) * a / 255, 255 - (255 - g) * a / 255, 255 - (255 - b) * a / 255, 255);
			//?}
			default -> vertex.setColor(r, g, b, a);
		};
	}

	private static List<Segment> queued;

	private record Segment(PoseStack.Pose pose, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int rgb1, int rgb2, int alpha, float nx, float ny, float nz, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult, float distance) {
	}

	public static void beginLayer(BlendMode blend) {
		Vertexer.blend = blend;
		queued = new ArrayList<>();
		updateOrigin();
	}

	public static void endLayer() {
		List<Segment> segments = queued;
		queued = null;
		try {
			segments.sort(Comparator.comparingDouble(Segment::distance).reversed());
			for (Segment s : segments) {
				emitLine(s.pose, s.builder, s.x1, s.y1, s.z1, s.x2, s.y2, s.z2, s.rgb1, s.rgb2, s.alpha, s.nx, s.ny, s.nz, s.width, s.cutFromCenter, s.cutFromCorner, s.outerMult, s.innerMult);
			}
		} finally {
			blend = BlendMode.NORMAL;
		}
	}

	public static void vertexLine(PoseStack.Pose pose, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int rgb1, int rgb2, int alpha, float nx, float ny, float nz, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		if (alpha < 1) return;
		if (queued != null) {
			float distance = pose.pose().transformPosition((x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2, new Vector3f()).add(originToCamera).lengthSquared();
			queued.add(new Segment(pose, builder, x1, y1, z1, x2, y2, z2, rgb1, rgb2, alpha, nx, ny, nz, width, cutFromCenter, cutFromCorner, outerMult, innerMult, distance));
			return;
		}
		emitLine(pose, builder, x1, y1, z1, x2, y2, z2, rgb1, rgb2, alpha, nx, ny, nz, width, cutFromCenter, cutFromCorner, outerMult, innerMult);
	}

	private static void emitLine(PoseStack.Pose pose, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, int rgb1, int rgb2, int alpha, float nx, float ny, float nz, float width, float cutFromCenter, float cutFromCorner, float outerMult, float innerMult) {
		Vector3f v1 = new Vector3f(x1, y1, z1);
		Vector3f v2 = new Vector3f(x2, y2, z2);
		Vector3f minOuter = new Vector3f();
		Vector3f maxOuter = new Vector3f();
		v1.lerp(v2, cutFromCorner / 2, minOuter);
		v2.lerp(v1, cutFromCorner / 2, maxOuter);
		if (cutFromCenter == 0 && outerMult == innerMult) {
			segment(pose, builder, minOuter, rgb1, maxOuter, rgb2, alpha, nx, ny, nz, width * outerMult, width * outerMult);
		} else {
			Vector3f center = new Vector3f();
			v1.lerp(v2, 0.5F, center);
			Vector3f minInner = new Vector3f();
			Vector3f maxInner = new Vector3f();
			center.lerp(v1, cutFromCenter, minInner);
			center.lerp(v2, cutFromCenter, maxInner);

			float yeah = Math.clamp(minInner.distance(minOuter) / minOuter.distance(maxOuter), 0, 1);

			segment(pose, builder, minOuter, rgb1, minInner, getLerpedRGB(rgb1, rgb2, yeah), alpha, nx, ny, nz, width * outerMult, width * innerMult);
			segment(pose, builder, maxInner, getLerpedRGB(rgb1, rgb2, 1 - yeah), maxOuter, rgb2, alpha, nx, ny, nz, width * innerMult, width * outerMult);
		}
	}

	private static void segment(PoseStack.Pose pose, VertexConsumer builder, Vector3f a, int rgbA, Vector3f b, int rgbB, int alpha, float nx, float ny, float nz, float widthA, float widthB) {
		lineWidth(color(builder.addVertex(pose, a.x, a.y, a.z), rgbA >> 16 & 0xFF, rgbA >> 8 & 0xFF, rgbA & 0xFF, alpha).setNormal(pose, nx, ny, nz), widthA);
		lineWidth(color(builder.addVertex(pose, b.x, b.y, b.z), rgbB >> 16 & 0xFF, rgbB >> 8 & 0xFF, rgbB & 0xFF, alpha).setNormal(pose, nx, ny, nz), widthB);
	}

	private static void lineWidth(VertexConsumer builder, float width) {
		//? if >= 1.21.11 || =1.8.9
		builder.setLineWidth(width);
	}
}
