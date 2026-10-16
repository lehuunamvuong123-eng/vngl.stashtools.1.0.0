package com.stashtools;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_10799;
import net.minecraft.class_11285;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2818;
import net.minecraft.class_287;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Storage ESP: draws translucent boxes through walls around chests, barrels, shulkers...
 * Rendering follows the Fabric docs "Rendering in the World" for 1.21.11.
 */
public class StorageEsp {
	private static final RenderPipeline PIPELINE = class_10799.method_67887(
			RenderPipeline.builder(class_10799.field_56860)
					.withLocation(class_2960.method_60655("stashtools", "pipeline/esp_through_walls"))
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.build());

	private static final class_9799 ALLOCATOR = new class_9799(class_1921.field_64009);
	private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
	private static final Vector3f MODEL_OFFSET = new Vector3f();
	private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

	private class_11285 vertexBuffer;
	/** x, y, z, r, g, b */
	private final List<float[]> boxes = new ArrayList<>();

	public void register() {
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::onRender);
	}

	/** Called every few ticks: collect storage block entities near the player. */
	public void refresh(class_310 mc) {
		boxes.clear();
		Config c = Config.get();
		if (!c.espEnabled || mc.field_1687 == null || mc.field_1724 == null) return;

		double px = mc.field_1724.method_23317(), py = mc.field_1724.method_23318(), pz = mc.field_1724.method_23321();
		double r2 = (double) c.espRange * c.espRange;

		for (class_2818 chunk : Storage.loadedChunks(mc, c.espRange / 16 + 1)) {
			for (class_2586 be : chunk.method_12214().values()) {
				Storage.Kind k = Storage.kindOf(be);
				if (k == null || !Storage.espWants(k, c)) continue;
				class_2338 p = be.method_11016();
				double dx = p.method_10263() + 0.5 - px, dy = p.method_10264() + 0.5 - py, dz = p.method_10260() + 0.5 - pz;
				if (dx * dx + dy * dy + dz * dz > r2) continue;
				boxes.add(new float[]{p.method_10263(), p.method_10264(), p.method_10260(), k.r, k.g, k.b});
				if (boxes.size() >= 2000) return;
			}
		}
	}

	private void onRender(WorldRenderContext context) {
		Config c = Config.get();
		if (!c.espEnabled || boxes.isEmpty()) return;

		class_4587 matrices = context.matrices();
		class_243 cam = context.worldState().field_63082.field_63078;

		matrices.method_22903();
		matrices.method_22904(-cam.field_1352, -cam.field_1351, -cam.field_1350);

		class_287 buffer = new class_287(ALLOCATOR, PIPELINE.getVertexFormatMode(), PIPELINE.getVertexFormat());
		float a = c.espAlphaPct / 100f;
		for (float[] b : boxes) {
			float e = 0.002f;
			addBox(matrices.method_23760().method_23761(), buffer,
					b[0] - e, b[1] - e, b[2] - e, b[0] + 1 + e, b[1] + 1 + e, b[2] + 1 + e,
					b[3], b[4], b[5], a);
		}
		matrices.method_22909();

		draw(class_310.method_1551(), buffer);
	}

	private static void addBox(Matrix4fc m, class_287 buf, float x0, float y0, float z0, float x1, float y1, float z1,
							   float r, float g, float b, float a) {
		// front
		buf.method_22918(m, x0, y0, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y0, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y1, z1).method_22915(r, g, b, a);
		// back
		buf.method_22918(m, x1, y0, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y0, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y1, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y1, z0).method_22915(r, g, b, a);
		// left
		buf.method_22918(m, x0, y0, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y0, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y1, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y1, z0).method_22915(r, g, b, a);
		// right
		buf.method_22918(m, x1, y0, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y0, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y1, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
		// top
		buf.method_22918(m, x0, y1, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y1, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y1, z0).method_22915(r, g, b, a);
		// bottom
		buf.method_22918(m, x0, y0, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y0, z0).method_22915(r, g, b, a);
		buf.method_22918(m, x1, y0, z1).method_22915(r, g, b, a);
		buf.method_22918(m, x0, y0, z1).method_22915(r, g, b, a);
	}

	private void draw(class_310 client, class_287 buffer) {
		class_9801 built = buffer.method_60800();
		class_9801.class_4574 dp = built.method_60822();
		VertexFormat format = dp.comp_749();

		GpuBuffer vertices = upload(dp, format, built);

		GpuBuffer indices;
		VertexFormat.class_5595 indexType;
		if (PIPELINE.getVertexFormatMode() == VertexFormat.class_5596.field_27382) {
			built.method_60819(ALLOCATOR, RenderSystem.getProjectionType().method_65045());
			indices = PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(built.method_60821());
			indexType = built.method_60822().comp_753();
		} else {
			RenderSystem.class_5590 shape = RenderSystem.getSequentialBuffer(PIPELINE.getVertexFormatMode());
			indices = shape.method_68274(dp.comp_751());
			indexType = shape.method_31924();
		}

		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
				.method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				() -> "stashtools esp", client.method_1522().method_71639(), OptionalInt.empty(),
				client.method_1522().method_71640(), OptionalDouble.empty())) {
			pass.setPipeline(PIPELINE);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", dynamicTransforms);
			pass.setVertexBuffer(0, vertices);
			pass.setIndexBuffer(indices, indexType);
			pass.drawIndexed(0, 0, dp.comp_751(), 1);
		}

		built.close();
		vertexBuffer.method_71121();
	}

	private GpuBuffer upload(class_9801.class_4574 dp, VertexFormat format, class_9801 built) {
		int size = dp.comp_750() * format.getVertexSize();
		if (vertexBuffer == null || vertexBuffer.method_71312() < size) {
			if (vertexBuffer != null) vertexBuffer.close();
			vertexBuffer = new class_11285(() -> "stashtools esp",
					GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, size);
		}
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		try (GpuBuffer.MappedView view = encoder.mapBuffer(
				vertexBuffer.method_71119().slice(0, built.method_60818().remaining()), false, true)) {
			MemoryUtil.memCopy(built.method_60818(), view.data());
		}
		return vertexBuffer.method_71119();
	}

	public void close() {
		ALLOCATOR.close();
		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}
	}
}
