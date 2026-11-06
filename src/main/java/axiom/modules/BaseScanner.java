package axiom.modules;

import axiom.BaseData;
import axiom.Module;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Background base detector. Per chunk it checks: storage/spawner block entities, blocks that do not generate naturally
 * (glass, redstone parts, beds, ...), and perfectly straight 1x2 tunnels / 1x1 shafts. Results go to BaseData
 * (shown on the HUD map and in the GUI "Bases" tab). Villages can cause false positives.
 */
public class BaseScanner extends Module {
    private static final BlockPos.MutableBlockPos MP = new BlockPos.MutableBlockPos();
    private static final Set<Block> UNNATURAL = new HashSet<>(Arrays.asList(
        Blocks.GLASS, Blocks.GLASS_PANE, Blocks.TINTED_GLASS, Blocks.CRYING_OBSIDIAN, Blocks.BEACON, Blocks.FURNACE,
        Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.CRAFTING_TABLE, Blocks.ENCHANTING_TABLE, Blocks.ANVIL,
        Blocks.REDSTONE_LAMP, Blocks.REPEATER, Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.PISTON, Blocks.STICKY_PISTON,
        Blocks.REDSTONE_BLOCK, Blocks.LEVER, Blocks.SEA_LANTERN, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
        Blocks.JUKEBOX, Blocks.NOTE_BLOCK, Blocks.SMITHING_TABLE, Blocks.GRINDSTONE));
    private static final Predicate<BlockState> IS_UNNATURAL =
        s -> UNNATURAL.contains(s.getBlock()) || s.is(BlockTags.WOOL) || s.is(BlockTags.BEDS);

    private static int tMinY = -60, tMaxY = 60, tMinRun = 8;
    private static boolean sound = true;
    private Object lastLevel;
    private int timer;

    public BaseScanner() {
        super("BaseScanner");
        settings.put("minScore", "12");
        settings.put("radius", "12");        // chunks
        settings.put("chunksPerStep", "1");
        settings.put("interval", "2");       // ticks between steps
        settings.put("storage", "1");
        settings.put("placed", "1");
        settings.put("tunnels", "1");
        settings.put("alert", "1");
        settings.put("sound", "1");
        settings.put("wStorage", "3");       // score weights
        settings.put("wSpawner", "5");
        settings.put("wPlaced", "0.5");
        settings.put("wTunnel", "6");
        settings.put("wShaft", "6");
        settings.put("tunnelLength", "8");   // minimum straight length
        settings.put("yMin", "-60");         // tunnel scan height range
        settings.put("yMax", "60");
    }

    @Override public void onTick() {
        BaseData.minScore = (int) num("minScore", 12);
        BaseData.wStorage = num("wStorage", 3); BaseData.wSpawner = num("wSpawner", 5); BaseData.wPlaced = num("wPlaced", 0.5);
        BaseData.wTunnel = num("wTunnel", 6); BaseData.wShaft = num("wShaft", 6);
        tMinRun = Math.max(3, (int) num("tunnelLength", 8)); tMinY = (int) num("yMin", -60); tMaxY = (int) num("yMax", 60);
        sound = num("sound", 1) >= 1;
        if (mc.level != lastLevel) { BaseData.clear(); lastLevel = mc.level; }
        if (++timer < num("interval", 2)) return;
        timer = 0;
        for (int n = 0; n < (int) num("chunksPerStep", 1); n++) {
            int[] c = next((int) num("radius", 12));
            if (c == null) return;
            analyze(c[0], c[1], num("storage", 1) >= 1, num("placed", 1) >= 1, num("tunnels", 1) >= 1, num("alert", 1) >= 1);
        }
    }

    private static int[] next(int radius) {
        int pcx = mc.player.blockPosition().getX() >> 4, pcz = mc.player.blockPosition().getZ() >> 4;
        int[] best = null;
        int bd = Integer.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            int cx = pcx + dx, cz = pcz + dz;
            if (BaseData.SCANNED.contains(ChunkPos.asLong(cx, cz))) continue;
            int d = dx * dx + dz * dz;
            if (d >= bd) continue;
            LevelChunk ch = mc.level.getChunk(cx, cz);
            if (ch == null || ch instanceof EmptyLevelChunk) continue;
            bd = d; best = new int[] { cx, cz };
        }
        return best;
    }

    /** Scans every not-yet-scanned chunk in range right now. Returns how many new bases were flagged. */
    public static int scanNow(int radius, boolean alert) {
        int flagged = 0;
        int pcx = mc.player.blockPosition().getX() >> 4, pcz = mc.player.blockPosition().getZ() >> 4;
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            if (BaseData.SCANNED.contains(ChunkPos.asLong(pcx + dx, pcz + dz))) continue;
            if (analyze(pcx + dx, pcz + dz, true, true, true, alert)) flagged++;
        }
        return flagged;
    }

    private static boolean air(int x, int y, int z) { return mc.level.getBlockState(MP.set(x, y, z)).isAir(); }
    private static boolean solid(int x, int y, int z) {
        BlockState s = mc.level.getBlockState(MP.set(x, y, z));
        return !s.isAir() && s.getFluidState().isEmpty();
    }

    /** Returns true if this chunk was newly flagged. */
    private static boolean analyze(int cx, int cz, boolean storage, boolean placed, boolean tunnels, boolean alert) {
        LevelChunk ch = mc.level.getChunk(cx, cz);
        if (ch == null || ch instanceof EmptyLevelChunk) return false;
        BaseData.Info in = new BaseData.Info();
        in.cx = cx; in.cz = cz; in.x = (cx << 4) + 8; in.y = 0; in.z = (cz << 4) + 8;
        boolean sample = false;

        if (storage) {
            for (var be : ch.getBlockEntities().values()) {
                BlockEntityType<?> t = be.getType();
                boolean counted = false;
                if (t == BlockEntityType.CHEST || t == BlockEntityType.TRAPPED_CHEST || t == BlockEntityType.BARREL
                        || t == BlockEntityType.SHULKER_BOX || t == BlockEntityType.HOPPER || t == BlockEntityType.ENDER_CHEST
                        || t == BlockEntityType.FURNACE || t == BlockEntityType.BLAST_FURNACE || t == BlockEntityType.SMOKER) { in.storage++; counted = true; }
                else if (t == BlockEntityType.MOB_SPAWNER) { in.spawners++; counted = true; }
                if (counted && !sample) { BlockPos p = be.getBlockPos(); in.x = p.getX(); in.y = p.getY(); in.z = p.getZ(); sample = true; }
            }
        }
        if (placed) {
            LevelChunkSection[] secs = ch.getSections();
            int minSec = mc.level.getMinSectionY();
            for (int i = 0; i < secs.length; i++) {
                LevelChunkSection sec = secs[i];
                if (sec == null || sec.hasOnlyAir() || !sec.maybeHas(IS_UNNATURAL)) continue;
                for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                    if (!IS_UNNATURAL.test(sec.getBlockState(x, y, z))) continue;
                    in.placed++;
                    if (!sample) { in.x = (cx << 4) + x; in.y = ((minSec + i) << 4) + y; in.z = (cz << 4) + z; sample = true; }
                }
            }
        }
        if (tunnels) {
            int minY = tMinY, maxY = tMaxY, minRun = tMinRun;
            int bx0 = cx << 4, bz0 = cz << 4;
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {          // vertical 1x1 shafts
                int bx = bx0 + x, bz = bz0 + z, run = 0;
                for (int y = minY; y <= maxY + 1; y++) {
                    boolean ok = y <= maxY && air(bx, y, bz) && solid(bx + 1, y, bz) && solid(bx - 1, y, bz) && solid(bx, y, bz + 1) && solid(bx, y, bz - 1);
                    if (ok) run++;
                    else { if (run >= minRun) { in.shafts++; if (!sample) { in.x = bx; in.y = y - run; in.z = bz; sample = true; } } run = 0; }
                }
            }
            for (int y = minY; y <= maxY; y++) for (int a = 0; a < 16; a++) {    // horizontal 1x2 tunnels, both axes
                int runX = 0, runZ = 0;
                for (int b = 0; b <= 16; b++) {
                    boolean okX = false, okZ = false;
                    if (b < 16) {
                        int bx = bx0 + b, bz = bz0 + a;
                        okX = air(bx, y, bz) && air(bx, y + 1, bz) && solid(bx, y - 1, bz) && solid(bx, y + 2, bz)
                            && solid(bx, y, bz - 1) && solid(bx, y + 1, bz - 1) && solid(bx, y, bz + 1) && solid(bx, y + 1, bz + 1);
                        int ax = bx0 + a, az = bz0 + b;
                        okZ = air(ax, y, az) && air(ax, y + 1, az) && solid(ax, y - 1, az) && solid(ax, y + 2, az)
                            && solid(ax - 1, y, az) && solid(ax - 1, y + 1, az) && solid(ax + 1, y, az) && solid(ax + 1, y + 1, az);
                    }
                    if (okX) runX++; else { if (runX >= minRun) { in.tunnels++; if (!sample) { in.x = bx0 + b - runX; in.y = y; in.z = bz0 + a; sample = true; } } runX = 0; }
                    if (okZ) runZ++; else { if (runZ >= minRun) { in.tunnels++; if (!sample) { in.x = bx0 + a; in.y = y; in.z = bz0 + b - runZ; sample = true; } } runZ = 0; }
                }
            }
        }
        long key = ChunkPos.asLong(cx, cz);
        BaseData.SCANNED.add(key);
        if (in.score() <= 0) return false;
        BaseData.CHUNKS.put(key, in);
        if (in.score() >= BaseData.minScore) {
            in.announced = true;
            if (alert) {
                mc.player.displayClientMessage(Component.literal("[Axiom] BASE? " + in.x + " " + in.y + " " + in.z + "  score " + in.score()
                    + " (storage " + in.storage + ", spawner " + in.spawners + ", placed " + in.placed + ", tunnel " + in.tunnels + ", shaft " + in.shafts + ")"), false);
                if (sound) mc.player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.0f, 1.0f);
            }
            return true;
        }
        return false;
    }
}
