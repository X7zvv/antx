package axiom.modules;

import axiom.Module;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/** Scans loaded chunks for a block (default: ancient debris) and prints coordinates. Only sees what the server sends. */
public class BlockFinder extends Module {
    private int timer;
    private final Set<Long> seen = new HashSet<>();
    public BlockFinder() {
        super("BlockFinder");
        settings.put("block", "minecraft:ancient_debris");
        settings.put("radius", "6");     // chunks
        settings.put("interval", "60");  // ticks
    }
    @Override public void onEnable() { seen.clear(); timer = 0; }
    private static String key(BlockState s) { return BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString(); }
    @Override public void onTick() {
        if (++timer < num("interval", 60)) return;
        timer = 0;
        String id = str("block", "minecraft:ancient_debris").trim();
        int r = (int) num("radius", 6);
        int pcx = mc.player.blockPosition().getX() >> 4, pcz = mc.player.blockPosition().getZ() >> 4;
        int minSec = mc.level.getMinSectionY();
        int found = 0;
        for (int cx = pcx - r; cx <= pcx + r; cx++) for (int cz = pcz - r; cz <= pcz + r; cz++) {
            LevelChunk ch = mc.level.getChunk(cx, cz);
            if (ch == null || ch instanceof EmptyLevelChunk) continue;
            LevelChunkSection[] secs = ch.getSections();
            for (int i = 0; i < secs.length; i++) {
                LevelChunkSection sec = secs[i];
                if (sec == null || sec.hasOnlyAir() || !sec.maybeHas(s -> key(s).equals(id))) continue;
                int baseY = (minSec + i) << 4;
                for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                    if (!key(sec.getBlockState(x, y, z)).equals(id)) continue;
                    BlockPos p = new BlockPos((cx << 4) + x, baseY + y, (cz << 4) + z);
                    if (!seen.add(p.asLong())) continue;
                    if (found++ < 5)
                        mc.player.displayClientMessage(Component.literal("[Axiom] " + id + " at " + p.getX() + " " + p.getY() + " " + p.getZ()
                            + " (" + (int) Math.sqrt(mc.player.blockPosition().distSqr(p)) + " blocks)"), false);
                }
            }
        }
        if (found > 5) mc.player.displayClientMessage(Component.literal("[Axiom] +" + (found - 5) + " more found"), false);
    }
}
