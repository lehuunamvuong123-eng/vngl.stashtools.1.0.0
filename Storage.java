package com.stashtools;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2586;
import net.minecraft.class_2791;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_7923;

public final class Storage {
	private Storage() {}

	public enum Kind {
		CHEST(1f, 0.65f, 0.1f),
		BARREL(0.8f, 0.5f, 0.2f),
		SHULKER(0.8f, 0.3f, 1f),
		ENDER(0.4f, 0.2f, 0.9f),
		HOPPER(0.6f, 0.6f, 0.6f),
		FURNACE(0.9f, 0.3f, 0.3f),
		SPAWNER(0.1f, 1f, 0.3f);

		public final float r, g, b;
		Kind(float r, float g, float b) { this.r = r; this.g = g; this.b = b; }
	}

	/** Identify the block entity by its registry id efficiently. */
	public static Kind kindOf(class_2586 be) {
		var key = class_7923.field_41181.method_10221(be.method_11017());
		if (key == null) return null;
		return switch (key.method_12832()) {
			case "chest", "trapped_chest" -> Kind.CHEST;
			case "barrel" -> Kind.BARREL;
			case "shulker_box" -> Kind.SHULKER;
			case "ender_chest" -> Kind.ENDER;
			case "hopper", "dropper", "dispenser" -> Kind.HOPPER;
			case "furnace", "blast_furnace", "smoker" -> Kind.FURNACE;
			case "mob_spawner", "spawner" -> Kind.SPAWNER;
			default -> null;
		};
	}

	public static boolean espWants(Kind k, Config c) {
		return switch (k) {
			case CHEST -> c.espChests;
			case BARREL -> c.espBarrels;
			case SHULKER -> c.espShulkers;
			case ENDER -> c.espEnder;
			case HOPPER -> c.espHoppers;
			case FURNACE -> c.espFurnaces;
			case SPAWNER -> c.espSpawners;
		};
	}

	/** Loaded client chunks around the player with optimized capacity allocation. */
	public static List<class_2818> loadedChunks(class_310 mc, int radiusChunks) {
		if (mc.field_1687 == null || mc.field_1724 == null) return List.of();
		int r = Math.min(radiusChunks, mc.field_1690.method_42503().method_41753());
		int diameter = r * 2 + 1;
		// Tối ưu cấp phát bộ nhớ trước để tránh tái cấp phát mảng (re-allocation) gây tụt khung hình
		List<class_2818> out = new ArrayList<>(diameter * diameter);
		
		int pcx = class_3532.method_15357(mc.field_1724.method_23317()) >> 4;
		int pcz = class_3532.method_15357(mc.field_1724.method_23321()) >> 4;
		
		for (int dx = -r; dx <= r; dx++) {
			for (int dz = -r; dz <= r; dz++) {
				class_2791 c = mc.field_1687.method_8402(pcx + dx, pcz + dz, class_2806.field_12803, false);
				if (c instanceof class_2818 lc) {
					out.add(lc);
				}
			}
		}
		return out;
	}
}