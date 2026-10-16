package com.stashtools;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_2561;

public class StashToolsClient implements ClientModInitializer {
	private static boolean pendingGui;
	private int ticks;

	@Override
	public void onInitializeClient() {
		Config.load();
		StorageEsp esp = new StorageEsp();
		esp.register();

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			ticks++;
			try {
				// Opened a tick later, otherwise the chat screen closing would cancel it.
				if (pendingGui && mc.field_1755 == null) {
					pendingGui = false;
					mc.method_1507(new ToolsScreen());
				}
				if (ticks % 10 == 0) esp.refresh(mc);
				if (ticks % 20 == 0) Finders.tick(mc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> esp.close());

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("st")
					.executes(ctx -> { pendingGui = true; return 1; })
					.then(ClientCommandManager.literal("gui").executes(ctx -> { pendingGui = true; return 1; }))
					.then(ClientCommandManager.literal("esp").executes(ctx -> {
						Config c = Config.get();
						c.espEnabled = !c.espEnabled;
						Config.save();
						return say(ctx, "Storage ESP: " + (c.espEnabled ? "ON" : "OFF"));
					}))
					.then(ClientCommandManager.literal("finder").executes(ctx -> {
						Config c = Config.get();
						c.stashEnabled = !c.stashEnabled;
						Config.save();
						return say(ctx, "Stash Finder: " + (c.stashEnabled ? "ON" : "OFF"));
					}))
					.then(ClientCommandManager.literal("adv").executes(ctx -> {
						Config c = Config.get();
						c.advEnabled = !c.advEnabled;
						Config.save();
						return say(ctx, "Advanced Stash Finder: " + (c.advEnabled ? "ON" : "OFF"));
					}))
					.then(ClientCommandManager.literal("list").executes(ctx -> {
						say(ctx, "Stash Finder: " + Finders.basic.size() + " | Advanced: " + Finders.adv.size());
						Finders.adv.values().stream().limit(15).forEach(h ->
								say(ctx, " adv x=" + h.x() + " z=" + h.z() + " shulker=" + h.shulkers()
										+ " spawner=" + h.spawners()));
						Finders.basic.values().stream().limit(15).forEach(h ->
								say(ctx, " basic x=" + h.x() + " z=" + h.z() + " shulker=" + h.shulkers()));
						return 1;
					}))
					.then(ClientCommandManager.literal("clear").executes(ctx -> {
						Finders.clear();
						return say(ctx, "Cleared.");
					}))
					.then(ClientCommandManager.literal("webhook")
							.then(ClientCommandManager.argument("url", StringArgumentType.greedyString()).executes(ctx -> {
								Config c = Config.get();
								c.advWebhookUrl = StringArgumentType.getString(ctx, "url").trim();
								Config.save();
								return say(ctx, "Webhook URL saved. Turn it on in /st > Advanced.");
							})))
					.then(ClientCommandManager.literal("set")
							.then(ClientCommandManager.argument("name", StringArgumentType.word())
									.then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0, 100000))
											.executes(ctx -> {
												Config c = Config.get();
												String n = StringArgumentType.getString(ctx, "name");
												int v = IntegerArgumentType.getInteger(ctx, "value");
												switch (n) {
													case "esprange" -> c.espRange = Math.max(16, Math.min(256, v));
													case "stashshulker" -> c.stashMinShulker = Math.max(1, v);
													case "stashdist" -> c.stashMinDistance = v;
													case "advshulker" -> c.advMinShulker = Math.max(1, v);
													case "advdist" -> c.advMinDistance = v;
													default -> {
														return say(ctx, "Names: esprange stashshulker stashdist advshulker advdist");
													}
												}
												Config.save();
												return say(ctx, n + " = " + v);
											})))));
		});
	}

	private static int say(CommandContext<FabricClientCommandSource> ctx, String msg) {
		ctx.getSource().sendFeedback(class_2561.method_43470("[StashTools] " + msg));
		return 1;
	}
}