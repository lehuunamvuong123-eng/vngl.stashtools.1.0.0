package com.stashtools;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Touch-friendly settings screen: 4 tabs, including direct Found list management. */
public class ToolsScreen extends class_437 {
	private static int page = 0;
	private int slot = 0;

	public ToolsScreen() {
		super(class_2561.method_43470("Stash Tools"));
	}

	@Override
	protected void method_25426() {
		slot = 0;
		Config c = Config.get();

		// NÂNG CẤP: Mở rộng thành 4 tab, thêm tab "Found" để xem trực tiếp danh sách trên GUI
		String[] tabs = {"ESP", "Stash", "Advanced", "Found"};
		int tw = 75; // Thu nhỏ chiều rộng nút để chứa vừa vặn 4 tab
		int x0 = this.field_22789 / 2 - (tw * 4 + 12) / 2;
		for (int i = 0; i < tabs.length; i++) {
			final int idx = i;
			class_4185 b = class_4185.method_46430(class_2561.method_43470(tabs[i]), btn -> {
				page = idx;
				this.method_41843();
			}).method_46434(x0 + i * (tw + 4), 24, tw, 20).method_46431();
			b.field_22763 = i != page;
			this.method_37063(b);
		}

		if (page == 0) {
			// Tab 0: ESP Settings
			toggle("Storage ESP", () -> c.espEnabled, v -> c.espEnabled = v);
			number("Range", () -> c.espRange, v -> c.espRange = v, 16, 16, 256);
			toggle("Chests", () -> c.espChests, v -> c.espChests = v);
			toggle("Barrels", () -> c.espBarrels, v -> c.espBarrels = v);
			toggle("Shulkers", () -> c.espShulkers, v -> c.espShulkers = v);
			toggle("Ender chests", () -> c.espEnder, v -> c.espEnder = v);
			toggle("Hoppers", () -> c.espHoppers, v -> c.espHoppers = v);
			toggle("Furnaces", () -> c.espFurnaces, v -> c.espFurnaces = v);
			toggle("Spawners", () -> c.espSpawners, v -> c.espSpawners = v);
			number("Opacity %", () -> c.espAlphaPct, v -> c.espAlphaPct = v, 5, 10, 80);
		} else if (page == 1) {
			// Tab 1: Stash Finder (Chuyên biệt Shulker, min mặc định từ 5)
			toggle("Stash Finder", () -> c.stashEnabled, v -> c.stashEnabled = v);
			number("Min shulker", () -> c.stashMinShulker, v -> c.stashMinShulker = v, 1, 1, 100);
			number("Min dist", () -> c.stashMinDistance, v -> c.stashMinDistance = v, 250, 0, 100000);
			action("Print found list", () -> printList(Finders.basic));
			action("Clear list", Finders.basic::clear);
		} else if (page == 2) {
			// Tab 2: Advanced Finder (Chuyên biệt Shulker, min mặc định từ 5)
			toggle("Adv. Finder", () -> c.advEnabled, v -> c.advEnabled = v);
			number("Min shulker", () -> c.advMinShulker, v -> c.advMinShulker = v, 1, 1, 100);
			number("Min dist", () -> c.advMinDistance, v -> c.advMinDistance = v, 250, 0, 100000);
			toggle("Spawner alert", () -> c.advSpawnerAlert, v -> c.advSpawnerAlert = v);
			toggle("Disconnect", () -> c.advDisconnect, v -> c.advDisconnect = v);
			toggle("Save to file", () -> c.advSaveFile, v -> c.advSaveFile = v);
			toggle("Discord webhook", () -> c.advWebhook, v -> c.advWebhook = v);
			action("Print found list", () -> printList(Finders.adv));
			action("Clear list", Finders.adv::clear);
		} else {
			// TAB 3 (MỚI): Hiển thị trực tiếp danh sách tọa độ Shulker tìm thấy ngay trên GUI
			var map = Finders.adv.isEmpty() ? Finders.basic : Finders.adv;
			if (map.isEmpty()) {
				action("§7[Danh sách trống]", () -> {});
			} else {
				int count = 0;
				for (Finders.Hit h : map.values()) {
					if (count >= 10) break; // Hiển thị tối đa 10 mục gần nhất
					final int hx = h.x();
					final int hz = h.z();
					final int shulkers = h.shulkers();
					
					int x = sx(), y = sy();
					slot++;
					
					String labelText = "x=" + hx + " z=" + hz + " (Shulker: " + shulkers + ")";
					this.method_37063(class_4185.method_46430(class_2561.method_43470(labelText), btn -> {
						var mc = this.field_22787;
						Finders.say(mc, "§e[StashTools] Đã chọn tọa độ Stash -> X: " + hx + ", Z: " + hz + " | Shulker: " + shulkers);
					}).method_46434(x, y, 150, 20).method_46431());
					
					count++;
				}
			}
			action("Xóa tất cả danh sách", () -> {
				Finders.clear();
				this.method_41843();
			});
		}

		this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
				.method_46434(this.field_22789 / 2 - 50, this.field_22790 - 26, 100, 20).method_46431());
	}

	private int sx() { return this.field_22789 / 2 - 155 + (slot % 2) * 160; }
	private int sy() { return 52 + (slot / 2) * 23; }

	private static class_2561 label(String name, boolean on) {
		return class_2561.method_43470(name + ": " + (on ? "ON" : "OFF"));
	}

	private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
		int x = sx(), y = sy();
		slot++;
		this.method_37063(class_4185.method_46430(label(name, get.getAsBoolean()), btn -> {
			set.accept(!get.getAsBoolean());
			btn.method_25355(label(name, get.getAsBoolean()));
		}).method_46434(x, y, 150, 20).method_46431());
	}

	private void number(String name, IntSupplier get, IntConsumer set, int step, int min, int max) {
		int x = sx(), y = sy();
		slot++;
		class_4185 mid = class_4185.method_46430(class_2561.method_43470(name + ": " + get.getAsInt()), b -> { })
				.method_46434(x + 22, y, 106, 20).method_46431();
		class_4185 minus = class_4185.method_46430(class_2561.method_43470("-"), b -> {
			set.accept(Math.max(min, get.getAsInt() - step));
			mid.method_25355(class_2561.method_43470(name + ": " + get.getAsInt()));
		}).method_46434(x, y, 20, 20).method_46431();
		class_4185 plus = class_4185.method_46430(class_2561.method_43470("+"), b -> {
			set.accept(Math.min(max, get.getAsInt() + step));
			mid.method_25355(class_2561.method_43470(name + ": " + get.getAsInt()));
		}).method_46434(x + 130, y, 20, 20).method_46431();
		this.method_37063(minus);
		this.method_37063(mid);
		this.method_37063(plus);
	}

	private void action(String name, Runnable r) {
		int x = sx(), y = sy();
		slot++;
		this.method_37063(class_4185.method_46430(class_2561.method_43470(name), b -> r.run())
				.method_46434(x, y, 150, 20).method_46431());
	}

	private void printList(java.util.Map<Long, Finders.Hit> map) {
		var mc = this.field_22787;
		if (map.isEmpty()) {
			Finders.say(mc, "\u00a77[StashTools] list is empty");
			return;
		}
		int n = 0;
		for (Finders.Hit h : map.values()) {
			Finders.say(mc, "\u00a77- x=" + h.x() + " z=" + h.z() + " shulker=" + h.shulkers() + " spawner=" + h.spawners());
			if (++n >= 15) break;
		}
	}

	@Override
	public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
		super.method_25394(g, mouseX, mouseY, delta);
		g.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 8, 0xFFFFFFFF);
	}

	@Override
	public void method_25432() {
		Config.save();
	}
}