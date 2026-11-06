package axiom;

import com.google.gson.*;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/** Persists module settings + friends (axiom.json). Profiles (config/axiom_profiles/<name>.json) also store on/off states. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("axiom.json"); }
    private static Path dir() { return FabricLoader.getInstance().getConfigDir().resolve("axiom_profiles"); }
    private static String clean(String n) { return n.replaceAll("[^A-Za-z0-9_-]", "_"); }

    public static void save() { write(file(), false); }
    public static void load() { if (Files.exists(file())) read(file(), false); }

    public static boolean saveProfile(String name) {
        try { Files.createDirectories(dir()); } catch (Exception e) { return false; }
        return write(dir().resolve(clean(name) + ".json"), true);
    }
    public static boolean loadProfile(String name) {
        Path p = dir().resolve(clean(name) + ".json");
        return Files.exists(p) && read(p, true);
    }
    public static List<String> profiles() {
        List<String> out = new ArrayList<>();
        try (var s = Files.list(dir())) {
            s.forEach(p -> { String n = p.getFileName().toString(); if (n.endsWith(".json")) out.add(n.substring(0, n.length() - 5)); });
        } catch (Exception ignored) {}
        return out;
    }

    private static boolean write(Path f, boolean withState) {
        try {
            JsonObject root = new JsonObject(), mods = new JsonObject();
            for (Module m : AxiomClient.MODULES) {
                JsonObject o = new JsonObject();
                m.settings.forEach(o::addProperty);
                if (withState) o.addProperty("__enabled", m.enabled);
                mods.add(m.name, o);
            }
            root.add("modules", mods);
            JsonArray fr = new JsonArray();
            Util.FRIENDS.forEach(fr::add);
            root.add("friends", fr);
            try (Writer w = Files.newBufferedWriter(f)) { GSON.toJson(root, w); }
            return true;
        } catch (Exception e) { AxiomClient.LOG.error("Config save failed", e); return false; }
    }

    private static boolean read(Path f, boolean withState) {
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            JsonObject mods = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module m : AxiomClient.MODULES) {
                if (!mods.has(m.name)) continue;
                JsonObject o = mods.getAsJsonObject(m.name);
                for (String k : m.settings.keySet()) if (o.has(k)) m.settings.put(k, o.get(k).getAsString());
                if (withState && o.has("__enabled")) m.setEnabled(o.get("__enabled").getAsBoolean());
            }
            if (root.has("friends")) for (JsonElement e : root.getAsJsonArray("friends")) Util.FRIENDS.add(e.getAsString().toLowerCase());
            return true;
        } catch (Exception e) { AxiomClient.LOG.error("Config load failed", e); return false; }
    }
}
