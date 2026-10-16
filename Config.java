package com.stashtools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	// Storage ESP[cite: 3]
	public boolean espEnabled = false; //[cite: 3]
	public int espRange = 128; //[cite: 3]
	public int espAlphaPct = 35; //[cite: 3]
	public boolean espChests = true, espBarrels = true, espShulkers = true, espEnder = true; //[cite: 3]
	public boolean espHoppers = false, espFurnaces = false, espSpawners = true; //[cite: 3]

	// Stash Finder (Chỉ tập trung Shulker Box, tối thiểu 5)[cite: 3]
	public boolean stashEnabled = false; //[cite: 3]
	public int stashMinShulker = 5; //[cite: 3]
	public int stashMinDistance = 1000; //[cite: 3]

	// Advanced Stash Finder (Chỉ tập trung Shulker Box, tối thiểu 5)[cite: 3]
	public boolean advEnabled = false; //[cite: 3]
	public int advMinShulker = 5; //[cite: 3]
	public int advMinDistance = 1000; //[cite: 3]
	public boolean advSpawnerAlert = true; //[cite: 3]
	public boolean advDisconnect = false; //[cite: 3]
	public boolean advSaveFile = true; //[cite: 3]
	public boolean advWebhook = false; //[cite: 3]
	public String advWebhookUrl = ""; //[cite: 3]

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create(); //[cite: 3]
	private static Config instance = new Config(); //[cite: 3]

	public static Config get() { return instance; } //[cite: 3]

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("stashtools.json"); //[cite: 3]
	}

	public static void load() {
		try {
			Path p = path();
			if (Files.exists(p)) {
				Config c = GSON.fromJson(Files.readString(p), Config.class); //[cite: 3]
				if (c != null) instance = c; //[cite: 3]
			}
		} catch (Exception e) {
			System.err.println("[StashTools] config load failed: " + e); //[cite: 3]
		}
	}

	public static void save() {
		try {
			Files.writeString(path(), GSON.toJson(instance)); //[cite: 3]
		} catch (Exception e) {
			System.err.println("[StashTools] config save failed: " + e); //[cite: 3]
		}
	}
}