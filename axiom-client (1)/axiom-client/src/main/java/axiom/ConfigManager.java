package axiom;

import com.google.gson.*;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Persists module settings and the friend list (not enabled states, on purpose). */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("axiom.json"); }

    public static void save() {
        try {
            JsonObject root = new JsonObject(), mods = new JsonObject();
            for (Module m : AxiomClient.MODULES) {
                JsonObject o = new JsonObject();
                m.settings.forEach(o::addProperty);
                mods.add(m.name, o);
            }
            root.add("modules", mods);
            JsonArray fr = new JsonArray();
            Util.FRIENDS.forEach(fr::add);
            root.add("friends", fr);
            try (Writer w = Files.newBufferedWriter(file())) { GSON.toJson(root, w); }
        } catch (Exception e) { AxiomClient.LOG.error("Config save failed", e); }
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            JsonObject mods = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module m : AxiomClient.MODULES) {
                if (!mods.has(m.name)) continue;
                JsonObject o = mods.getAsJsonObject(m.name);
                for (String k : m.settings.keySet())
                    if (o.has(k)) m.settings.put(k, o.get(k).getAsString());
            }
            if (root.has("friends")) for (JsonElement e : root.getAsJsonArray("friends")) Util.FRIENDS.add(e.getAsString().toLowerCase());
        } catch (Exception e) { AxiomClient.LOG.error("Config load failed", e); }
    }
}
