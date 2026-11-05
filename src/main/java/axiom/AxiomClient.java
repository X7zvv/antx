package axiom;

import axiom.modules.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AxiomClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("axiom");
    public static final List<Module> MODULES = new ArrayList<>();
    private static boolean openGui;

    public static Module find(String n) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(n)) return m;
        return null;
    }

    private static void say(FabricClientCommandSource s, String msg) {
        s.sendFeedback(Component.literal("[Axiom] " + msg));
    }

    @Override
    public void onInitializeClient() {
        // Combat
        MODULES.add(new TriggerBot().in("Combat"));
        MODULES.add(new AimAssist().in("Combat"));
        MODULES.add(new AutoTotem().in("Combat"));
        MODULES.add(new AutoCrystal().in("Combat"));
        MODULES.add(new CrystalPvP().in("Combat"));
        MODULES.add(new SwordPvP().in("Combat"));
        MODULES.add(new MacePvP().in("Combat"));
        // Movement
        MODULES.add(new Fly().in("Movement"));
        MODULES.add(new AutoSprint().in("Movement"));
        MODULES.add(new ElytraPilot().in("Movement"));
        // Render
        MODULES.add(new PlayerESP().in("Render"));
        MODULES.add(new Fullbright().in("Render"));
        MODULES.add(new HudModule().in("Render"));
        // Donut
        MODULES.add(new AutoSell().in("Donut"));
        MODULES.add(new AutoBuy().in("Donut"));
        MODULES.add(new ItemDropper().in("Donut"));
        MODULES.add(new RTPBaseFinder().in("Donut"));
        MODULES.add(new SpawnerProtect().in("Donut"));
        MODULES.add(new AutoTPA().in("Donut"));
        MODULES.add(new BaseAlert().in("Donut"));
        MODULES.add(new TraderFinder().in("Donut"));
        // Finder
        MODULES.add(new BaseFinder().in("Finder"));
        MODULES.add(new BlockFinder().in("Finder"));
        // Auto
        MODULES.add(new AutoBuilder().in("Auto"));
        MODULES.add(new AreaMiner().in("Auto"));
        MODULES.add(new AutoMine().in("Auto"));
        MODULES.add(new AutoEat().in("Auto"));
        MODULES.add(new AutoTool().in("Auto"));
        // Misc
        MODULES.add(new CordSnapper().in("Misc"));
        ConfigManager.load();

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("axiom", "main"));
        KeyMapping guiKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.axiom.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("axiom", "hud"), (g, dt) -> HudModule.render(g));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.consumeClick()) if (client.screen == null) openGui = true;
            if (openGui) { openGui = false; if (client.screen == null) client.setScreen(new ClickGui()); }
            if (client.player == null || client.level == null) return;
            for (Module m : MODULES) {
                if (!m.enabled) continue;
                try { m.onTick(); }
                catch (Throwable t) {
                    LOG.error("Module {} crashed and was disabled", m.name, t);
                    m.enabled = false;
                    client.player.displayClientMessage(Component.literal("[Axiom] " + m.name + " crashed, disabled. See log."), false);
                }
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            Module m = find("AutoTPA");
            if (m instanceof AutoTPA tpa && tpa.enabled) {
                try { tpa.handle(message.getString()); } catch (Throwable t) { LOG.error("AutoTPA failed", t); }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
            SuggestionProvider<FabricClientCommandSource> names = (ctx, b) -> {
                for (Module m : MODULES) b.suggest(m.name);
                return b.buildFuture();
            };
            dispatcher.register(literal("axiom")
                .then(literal("gui").executes(c -> { openGui = true; return 1; }))
                .then(literal("panic").executes(c -> {
                    for (Module m : MODULES) m.setEnabled(false);
                    say(c.getSource(), "All modules disabled.");
                    return 1;
                }))
                .then(literal("list").executes(c -> {
                    for (Module m : MODULES) say(c.getSource(), (m.enabled ? "[ON]  " : "[off] ") + m.name);
                    return 1;
                }))
                .then(literal("toggle").then(argument("module", StringArgumentType.word()).suggests(names).executes(c -> {
                    Module m = find(StringArgumentType.getString(c, "module"));
                    if (m == null) { say(c.getSource(), "No such module."); return 0; }
                    m.toggle();
                    say(c.getSource(), m.name + (m.enabled ? " enabled" : " disabled"));
                    return 1;
                })))
                .then(literal("info").then(argument("module", StringArgumentType.word()).suggests(names).executes(c -> {
                    Module m = find(StringArgumentType.getString(c, "module"));
                    if (m == null) { say(c.getSource(), "No such module."); return 0; }
                    say(c.getSource(), m.name + " settings: " + (m.settings.isEmpty() ? "none" : m.settings.toString()));
                    return 1;
                })))
                .then(literal("set").then(argument("module", StringArgumentType.word()).suggests(names)
                    .then(argument("key", StringArgumentType.word())
                        .then(argument("value", StringArgumentType.greedyString()).executes(c -> {
                            Module m = find(StringArgumentType.getString(c, "module"));
                            String k = StringArgumentType.getString(c, "key");
                            String v = StringArgumentType.getString(c, "value").trim();
                            if (m == null) { say(c.getSource(), "No such module."); return 0; }
                            if (!m.settings.containsKey(k)) { say(c.getSource(), "Unknown setting. Keys: " + m.settings.keySet()); return 0; }
                            Minecraft mc = Minecraft.getInstance();
                            if (v.equalsIgnoreCase("here") && mc.player != null)
                                v = mc.player.blockPosition().getX() + " " + mc.player.blockPosition().getY() + " " + mc.player.blockPosition().getZ();
                            m.settings.put(k, v);
                            ConfigManager.save();
                            say(c.getSource(), m.name + "." + k + " = " + v);
                            return 1;
                        })))))
                .then(literal("friend")
                    .then(literal("list").executes(c -> { say(c.getSource(), "Friends: " + Util.FRIENDS); return 1; }))
                    .then(literal("add").then(argument("name", StringArgumentType.word()).executes(c -> {
                        Util.FRIENDS.add(StringArgumentType.getString(c, "name").toLowerCase());
                        ConfigManager.save();
                        say(c.getSource(), "Friend added.");
                        return 1;
                    })))
                    .then(literal("remove").then(argument("name", StringArgumentType.word()).executes(c -> {
                        Util.FRIENDS.remove(StringArgumentType.getString(c, "name").toLowerCase());
                        ConfigManager.save();
                        say(c.getSource(), "Friend removed.");
                        return 1;
                    }))))
                .then(literal("build")
                    .then(literal("pause").executes(c -> {
                        AutoBuilder b = (AutoBuilder) find("AutoBuilder");
                        say(c.getSource(), b.togglePause() ? "Builder paused." : "Builder resumed.");
                        return 1;
                    }))
                    .then(literal("stop").executes(c -> {
                        Module b = find("AutoBuilder");
                        if (b != null) b.setEnabled(false);
                        say(c.getSource(), "Builder stopped.");
                        return 1;
                    }))
                    .then(argument("file", StringArgumentType.greedyString()).executes(c -> {
                        String f = StringArgumentType.getString(c, "file").trim();
                        Path dir = FabricLoader.getInstance().getGameDir().resolve("schematics");
                        Path p = dir.resolve(f);
                        if (!Files.exists(p)) p = dir.resolve(f + ".litematic");
                        if (!Files.exists(p)) { say(c.getSource(), "Not found in " + dir + ": " + f); return 0; }
                        AutoBuilder b = (AutoBuilder) find("AutoBuilder");
                        String err = b.start(p);
                        say(c.getSource(), err == null ? "Building " + p.getFileName() + " (" + b.remaining() + " blocks)" : "Failed: " + err);
                        return err == null ? 1 : 0;
                    }))));
        });
    }
}
