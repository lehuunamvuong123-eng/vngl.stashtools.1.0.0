package com.stashtools;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stash Finder (basic) and Advanced Stash Finder - Specialized for Shulker Boxes (>= 5). */
public final class Finders {
	private Finders() {}

	public record Hit(int cx, int cz, int storage, int shulkers, int spawners, int score) {
		public int x() { return cx * 16 + 8; }
		public int z() { return cz * 16 + 8; }
	}

	public static final Map<Long, Hit> basic = new LinkedHashMap<>();
	public static final Map<Long, Hit> adv = new LinkedHashMap<>();
	private static Object lastLevel;
	private static final HttpClient HTTP = HttpClient.newHttpClient();

	public static void clear() {
		basic.clear();
		adv.clear();
	}

	public static void tick(class_310 mc) {
		if (mc.field_1687 == null || mc.field_1724 == null) return;
		if (mc.field_1687 != lastLevel) { // new world / dimension
			lastLevel = mc.field_1687;
			clear();
		}
		Config c = Config.get();
		if (!c.stashEnabled && !c.advEnabled) return;

		for (class_2818 chunk : Storage.loadedChunks(mc, 32)) {
			int cx = chunk.method_12004().field_9181;
			int cz = chunk.method_12004().field_9180;
			long key = ((long) cx << 32) | (cz & 0xFFFFFFFFL);

			boolean needBasic = c.stashEnabled && !basic.containsKey(key);
			boolean needAdv = c.advEnabled && !adv.containsKey(key);
			if (!needBasic && !needAdv) continue;

			int shulkers = 0, spawners = 0;
			for (class_2586 be : chunk.method_12214().values()) {
				Storage.Kind k = Storage.kindOf(be);
				if (k == null) continue;
				switch (k) {
					case SHULKER -> shulkers++;
					case SPAWNER -> spawners++;
					default -> { }
				}
			}
			// Chỉ quan tâm nếu có Shulker Box hoặc Spawner
			if (shulkers == 0 && spawners == 0) continue;

			double dist = Math.hypot(cx * 16 + 8, cz * 16 + 8); // distance from 0,0

			// Stash Finder cơ bản: kiểm tra số lượng Shulker >= cấu hình (mặc định 5)
			if (needBasic && shulkers >= c.stashMinShulker && dist >= c.stashMinDistance) {
				Hit h = new Hit(cx, cz, shulkers, shulkers, spawners, shulkers);
				basic.put(key, h);
				say(mc, "\u00a76[StashFinder] \u00a7fchunk " + cx + "," + cz + "  x=" + h.x() + " z=" + h.z()
						+ "  shulkers=" + shulkers);
			}

			// Advanced Stash Finder: kiểm tra số lượng Shulker >= cấu hình nâng cao (mặc định 5)
			if (needAdv) {
				int score = shulkers * 3 + (c.advSpawnerAlert ? spawners * 5 : 0);
				if (shulkers >= c.advMinShulker && dist >= c.advMinDistance) {
					Hit h = new Hit(cx, cz, shulkers, shulkers, spawners, score);
					adv.put(key, h);
					onAdvancedHit(mc, c, h);
				}
			}
		}
	}

	private static void onAdvancedHit(class_310 mc, Config c, Hit h) {
		String text = "chunk " + h.cx() + "," + h.cz() + " | x=" + h.x() + " z=" + h.z()
				+ " | shulkers=" + h.shulkers()
				+ " spawner=" + h.spawners() + " score=" + h.score();
		String tag = (c.advSpawnerAlert && h.spawners() > 0) ? " \u00a7c[SPAWNER]" : "";
		say(mc, "\u00a7b[AdvStash] \u00a7f" + text + tag);

		String server = mc.method_1558() != null ? mc.method_1558().field_3761 : "singleplayer";

		if (c.advSaveFile) {
			try {
				var file = FabricLoader.getInstance().getConfigDir().resolve("stashtools-found.txt");
				String line = LocalDateTime.now() + " | " + server + " | " + text + System.lineSeparator();
				Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			} catch (Exception ignored) { }
		}

		if (c.advWebhook && c.advWebhookUrl != null && c.advWebhookUrl.startsWith("https://")) {
			try {
				String json = "{\"content\":\"" + esc("Stash found on " + server + ": " + text) + "\"}";
				HttpRequest req = HttpRequest.newBuilder(URI.create(c.advWebhookUrl))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(json))
						.build();
				HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding());
			} catch (Exception ignored) { }
		}

		if (c.advDisconnect && mc.method_1562() != null) {
			c.advEnabled = false; // avoid a reconnect loop
			Config.save();
			mc.method_1562().method_48296().method_10747(class_2561.method_43470("StashTools: stash found at " + h.x() + ", " + h.z()));
		}
	}

	private static String esc(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}

	public static void say(class_310 mc, String msg) {
		if (mc.field_1724 != null) mc.field_1724.method_7353(class_2561.method_43470(msg), false);
	}
}