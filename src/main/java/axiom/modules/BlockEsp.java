package axiom.modules;

import axiom.AxiomClient;
import axiom.Module;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

/**
 * Shows chosen blocks through walls. Drawn as a 2D overlay: every block is projected onto the screen with our own
 * camera maths (works in first person and in Freecam, not in F5 third person). Pick blocks with the toggles or the
 * "custom" list (words that appear in the block id, e.g. "diamond_ore,ancient_debris,beacon").
 */
public class BlockEsp extends Module {
    private record P(int x, int y, int z, int color, String name) {}
    private List<P> cache = new ArrayList<>();
    private int timer;

    public BlockEsp() {
        super("BlockESP");
        settings.put("diamond", "1");
        settings.put("debris", "1");
        settings.put("gold", "0");
        settings.put("iron", "0");
        settings.put("emerald", "0");
        settings.put("redstone", "0");
        settings.put("lapis", "0");
        settings.put("coal", "0");
        settings.put("copper", "0");
        settings.put("quartz", "0");
        settings.put("spawner", "1");
        settings.put("chests", "1");
        settings.put("beacon", "0");
        settings.put("custom", "");        // extra words, comma separated
        settings.put("range", "48");
        settings.put("maxBlocks", "400");
        settings.put("interval", "20");    // ticks between scans
        settings.put("fill", "40");        // fill alpha 0-255, 0 = outline only
        settings.put("tracers", "0");
        settings.put("tracerMax", "30");
        settings.put("labels", "0");
        settings.put("fovOffset", "0");    // calibrate if boxes drift when sprinting
    }

    @Override public void onEnable() { timer = 999; }
    @Override public void onDisable() { cache = new ArrayList<>(); }

    private List<String> words() {
        List<String> w = new ArrayList<>();
        if (num("diamond", 1) >= 1) w.add("diamond_ore");
        if (num("debris", 1) >= 1) w.add("ancient_debris");
        if (num("gold", 0) >= 1) w.add("gold_ore");
        if (num("iron", 0) >= 1) w.add("iron_ore");
        if (num("emerald", 0) >= 1) w.add("emerald_ore");
        if (num("redstone", 0) >= 1) w.add("redstone_ore");
        if (num("lapis", 0) >= 1) w.add("lapis_ore");
        if (num("coal", 0) >= 1) w.add("coal_ore");
        if (num("copper", 0) >= 1) w.add("copper_ore");
        if (num("quartz", 0) >= 1) w.add("quartz_ore");
        if (num("spawner", 1) >= 1) w.add("spawner");
        if (num("chests", 1) >= 1) { w.add("chest"); w.add("barrel"); w.add("shulker_box"); }
        if (num("beacon", 0) >= 1) w.add("beacon");
        for (String s : str("custom", "").toLowerCase().split("[ ,]+")) if (!s.isEmpty()) w.add(s);
        return w;
    }

    private static int color(Block b, List<String> words, Map<Block, Integer> memo) {
        return memo.computeIfAbsent(b, k -> {
            String path = BuiltInRegistries.BLOCK.getKey(k).getPath();
            for (String w : words) if (path.contains(w)) return 0xFFFFFF & Mth.hsvToRgb((path.hashCode() & 0xFFFF) / 65535f, 0.8f, 1f);
            return -1;
        });
    }

    @Override public void onTick() {
        if (++timer < num("interval", 20)) return;
        timer = 0;
        List<String> words = words();
        if (words.isEmpty()) { cache = new ArrayList<>(); return; }
        int range = (int) num("range", 48), r = (range >> 4) + 1;
        int pcx = mc.player.blockPosition().getX() >> 4, pcz = mc.player.blockPosition().getZ() >> 4;
        Map<Block, Integer> memo = new HashMap<>();
        Predicate<BlockState> pred = s -> color(s.getBlock(), words, memo) != -1;
        Vec3 pp = mc.player.position();
        double r2 = (double) range * range;
        int minSec = mc.level.getMinSectionY();
        List<P> out = new ArrayList<>();
        for (int cx = pcx - r; cx <= pcx + r; cx++) for (int cz = pcz - r; cz <= pcz + r; cz++) {
            LevelChunk ch = mc.level.getChunk(cx, cz);
            if (ch == null || ch instanceof EmptyLevelChunk) continue;
            LevelChunkSection[] secs = ch.getSections();
            for (int i = 0; i < secs.length; i++) {
                LevelChunkSection sec = secs[i];
                if (sec == null || sec.hasOnlyAir() || !sec.maybeHas(pred)) continue;
                for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                    BlockState st = sec.getBlockState(x, y, z);
                    int c = color(st.getBlock(), words, memo);
                    if (c == -1) continue;
                    int bx = (cx << 4) + x, by = ((minSec + i) << 4) + y, bz = (cz << 4) + z;
                    double dx = bx + 0.5 - pp.x, dy = by + 0.5 - pp.y, dz = bz + 0.5 - pp.z;
                    if (dx * dx + dy * dy + dz * dz > r2) continue;
                    out.add(new P(bx, by, bz, c, BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath()));
                }
            }
        }
        out.sort(Comparator.comparingDouble(p -> {
            double dx = p.x + 0.5 - pp.x, dy = p.y + 0.5 - pp.y, dz = p.z + 0.5 - pp.z;
            return dx * dx + dy * dy + dz * dz;
        }));
        int max = Math.max(10, (int) num("maxBlocks", 400));
        cache = out.size() > max ? new ArrayList<>(out.subList(0, max)) : out;
    }

    public static void render(GuiGraphics g, float pt) {
        if (AxiomClient.find("BlockESP") instanceof BlockEsp b && b.enabled && mc.player != null && mc.level != null && !mc.options.hideGui) b.draw(g, pt);
    }

    private static void line(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.min(300, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) / 2);
        if (steps <= 0) return;
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps, y = y1 + (y2 - y1) * i / steps;
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    private void draw(GuiGraphics g, float pt) {
        Entity ce = mc.getCameraEntity();
        if (ce == null) return;
        if (ce == mc.player && !mc.options.getCameraType().isFirstPerson()) return;
        Vec3 cp = ce.getEyePosition(pt);
        double yaw = Math.toRadians(ce.getViewYRot(pt)), pit = Math.toRadians(ce.getViewXRot(pt));
        double fx = -Math.sin(yaw) * Math.cos(pit), fy = -Math.sin(pit), fz = Math.cos(yaw) * Math.cos(pit);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double ux = -rz * fy, uy = rz * fx - rx * fz, uz = rx * fy;
        int w = g.guiWidth(), h = g.guiHeight();
        double fov = Math.toRadians(mc.options.fov().get() + num("fovOffset", 0));
        double scale = (h / 2.0) / Math.tan(fov / 2.0);
        int fillA = Math.max(0, Math.min(255, (int) num("fill", 40)));
        boolean tracers = num("tracers", 0) >= 1, labels = num("labels", 0) >= 1;
        int tracerMax = (int) num("tracerMax", 30), n = 0;

        for (P p : cache) {
            double minX = 1e9, minY = 1e9, maxX = -1e9, maxY = -1e9;
            boolean skip = false;
            for (int i = 0; i < 8; i++) {
                double relx = p.x + (i & 1) - cp.x, rely = p.y + ((i >> 1) & 1) - cp.y, relz = p.z + ((i >> 2) & 1) - cp.z;
                double depth = relx * fx + rely * fy + relz * fz;
                if (depth < 0.1) { skip = true; break; }
                double sx = w / 2.0 + (relx * rx + relz * rz) / depth * scale;
                double sy = h / 2.0 - (relx * ux + rely * uy + relz * uz) / depth * scale;
                minX = Math.min(minX, sx); maxX = Math.max(maxX, sx);
                minY = Math.min(minY, sy); maxY = Math.max(maxY, sy);
            }
            n++;
            if (skip || maxX < 0 || minX > w || maxY < 0 || minY > h) continue;
            int x1 = (int) minX, y1 = (int) minY, x2 = Math.max((int) maxX, x1 + 3), y2 = Math.max((int) maxY, y1 + 3);
            int col = 0xFF000000 | p.color;
            if (fillA > 0) g.fill(x1, y1, x2, y2, (fillA << 24) | p.color);
            g.fill(x1, y1, x2, y1 + 1, col);
            g.fill(x1, y2 - 1, x2, y2, col);
            g.fill(x1, y1, x1 + 1, y2, col);
            g.fill(x2 - 1, y1, x2, y2, col);
            if (tracers && n <= tracerMax) line(g, w / 2, h, (x1 + x2) / 2, (y1 + y2) / 2, col);
            if (labels && n <= 20) {
                double d = Math.sqrt((p.x + 0.5 - cp.x) * (p.x + 0.5 - cp.x) + (p.y + 0.5 - cp.y) * (p.y + 0.5 - cp.y) + (p.z + 0.5 - cp.z) * (p.z + 0.5 - cp.z));
                String s = p.name + " " + (int) d + "m";
                g.drawString(mc.font, s, (x1 + x2) / 2 - mc.font.width(s) / 2, y1 - 10, col);
            }
        }
    }
}
