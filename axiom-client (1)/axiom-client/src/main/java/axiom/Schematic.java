package axiom;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

/** Minimal .litematic reader. Block properties (stairs facing etc.) are ignored. */
public class Schematic {
    public record Entry(int x, int y, int z, String block) {}
    public final List<Entry> blocks = new ArrayList<>();

    public static Schematic load(Path path) throws IOException {
        CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
        CompoundTag regions = root.getCompoundOrEmpty("Regions");
        Schematic out = new Schematic();
        for (String key : regions.keySet()) {
            CompoundTag r = regions.getCompoundOrEmpty(key);
            CompoundTag pos = r.getCompoundOrEmpty("Position");
            CompoundTag size = r.getCompoundOrEmpty("Size");
            int sx = size.getIntOr("x", 0), sy = size.getIntOr("y", 0), sz = size.getIntOr("z", 0);
            int ax = Math.abs(sx), ay = Math.abs(sy), az = Math.abs(sz);
            int ox = pos.getIntOr("x", 0) + (sx < 0 ? sx + 1 : 0);
            int oy = pos.getIntOr("y", 0) + (sy < 0 ? sy + 1 : 0);
            int oz = pos.getIntOr("z", 0) + (sz < 0 ? sz + 1 : 0);

            ListTag pal = r.getListOrEmpty("BlockStatePalette");
            if (pal.isEmpty() || ax * ay * az == 0) continue;
            String[] names = new String[pal.size()];
            for (int i = 0; i < names.length; i++) names[i] = pal.getCompoundOrEmpty(i).getStringOr("Name", "minecraft:air");
            long[] arr = r.getLongArray("BlockStates").orElse(new long[0]);
            int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(pal.size() - 1));
            long mask = (1L << bits) - 1L;

            for (int y = 0; y < ay; y++) for (int z = 0; z < az; z++) for (int x = 0; x < ax; x++) {
                long idx = ((long) y * az + z) * ax + x;
                long startBit = idx * bits;
                int a = (int) (startBit >> 6);
                int b = (int) (((idx + 1) * bits - 1) >> 6);
                int off = (int) (startBit & 63);
                if (b >= arr.length) continue;
                long v = (a == b) ? (arr[a] >>> off) & mask
                                  : ((arr[a] >>> off) | (arr[b] << (64 - off))) & mask;
                if (v >= names.length) continue;
                String n = names[(int) v];
                if (n.equals("minecraft:air") || n.equals("minecraft:cave_air") || n.equals("minecraft:void_air")) continue;
                out.blocks.add(new Entry(ox + x, oy + y, oz + z, n));
            }
        }
        if (out.blocks.isEmpty()) return out;
        int mx = Integer.MAX_VALUE, my = Integer.MAX_VALUE, mz = Integer.MAX_VALUE;
        for (Entry e : out.blocks) { mx = Math.min(mx, e.x); my = Math.min(my, e.y); mz = Math.min(mz, e.z); }
        List<Entry> norm = new ArrayList<>(out.blocks.size());
        for (Entry e : out.blocks) norm.add(new Entry(e.x - mx, e.y - my, e.z - mz, e.block));
        norm.sort(Comparator.<Entry>comparingInt(Entry::y).thenComparingInt(Entry::x).thenComparingInt(Entry::z));
        out.blocks.clear();
        out.blocks.addAll(norm);
        return out;
    }
}
