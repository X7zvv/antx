package axiom.modules;

import axiom.Module;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;

/** Flags chunks with many storage blocks (chests, barrels, shulkers, hoppers) or spawners = likely player bases. */
public class BaseFinder extends Module {
    public static final Set<Long> SEEN = new HashSet<>();
    private int timer;

    public BaseFinder() {
        super("BaseFinder");
        settings.put("minStorage", "4");
        settings.put("spawners", "1");   // 1 = also flag chunks with any spawner
        settings.put("radius", "10");    // chunks
        settings.put("interval", "40");
    }
    @Override public void onEnable() { SEEN.clear(); timer = 0; }
    @Override public void onTick() {
        if (++timer < num("interval", 40)) return;
        timer = 0;
        scanOnce((int) num("minStorage", 4), num("spawners", 1) >= 1, (int) num("radius", 10));
    }

    /** Returns the number of newly flagged chunks. */
    public static int scanOnce(int minStorage, boolean spawnersCount, int radius) {
        Minecraft mc = Minecraft.getInstance();
        int pcx = mc.player.blockPosition().getX() >> 4, pcz = mc.player.blockPosition().getZ() >> 4;
        int found = 0;
        for (int cx = pcx - radius; cx <= pcx + radius; cx++) for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
            LevelChunk ch = mc.level.getChunk(cx, cz);
            if (ch == null || ch instanceof EmptyLevelChunk) continue;
            long key = ChunkPos.asLong(cx, cz);
            if (SEEN.contains(key)) continue;
            int storage = 0, spawners = 0;
            BlockPos any = null;
            for (var be : ch.getBlockEntities().values()) {
                BlockEntityType<?> t = be.getType();
                if (t == BlockEntityType.CHEST || t == BlockEntityType.TRAPPED_CHEST || t == BlockEntityType.BARREL
                        || t == BlockEntityType.SHULKER_BOX || t == BlockEntityType.HOPPER || t == BlockEntityType.ENDER_CHEST) storage++;
                else if (t == BlockEntityType.MOB_SPAWNER) spawners++;
                if (any == null) any = be.getBlockPos();
            }
            if (storage >= minStorage || (spawnersCount && spawners > 0)) {
                SEEN.add(key);
                found++;
                mc.player.displayClientMessage(Component.literal("[Axiom] Base? chunk " + cx + "," + cz + ": " + storage + " storage, "
                    + spawners + " spawner(s) near " + any.getX() + " " + any.getY() + " " + any.getZ()), false);
            }
        }
        return found;
    }
}
