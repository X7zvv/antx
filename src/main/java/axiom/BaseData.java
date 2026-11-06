package axiom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;

/** Shared store for everything the base finders learn about chunks. */
public final class BaseData {
    public static final class Info {
        public int cx, cz, x, y, z;
        public int storage, spawners, placed, tunnels, shafts;
        public boolean announced;
        public double dist;
        public int score() { return (int) (storage * wStorage + spawners * wSpawner + Math.min(placed, 20) * wPlaced + tunnels * wTunnel + shafts * wShaft); }
    }

    public static final Map<Long, Info> CHUNKS = new HashMap<>();
    public static final Set<Long> SCANNED = new HashSet<>();
    public static int minScore = 12;
    public static double wStorage = 3, wSpawner = 5, wPlaced = 0.5, wTunnel = 6, wShaft = 6;

    private BaseData() {}

    public static void clear() { CHUNKS.clear(); SCANNED.clear(); }

    public static List<Info> flagged() {
        Minecraft mc = Minecraft.getInstance();
        List<Info> out = new ArrayList<>();
        if (mc.player == null) return out;
        for (Info i : CHUNKS.values()) {
            if (i.score() >= minScore) {
                i.dist = Math.sqrt(mc.player.distanceToSqr(i.x + 0.5, i.y, i.z + 0.5));
                out.add(i);
            }
        }
        out.sort(Comparator.comparingDouble(i -> i.dist));
        return out;
    }
}
