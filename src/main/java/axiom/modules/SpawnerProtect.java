package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** When a non-friend comes near: mines spawners within reach (use a silk touch pick), then disconnects. */
public class SpawnerProtect extends Module {
    private boolean tripped;
    public SpawnerProtect() {
        super("SpawnerProtect");
        settings.put("range", "24");
        settings.put("reach", "5");
        settings.put("logoff", "1");   // 1 = disconnect when done, 0 = just stop
    }
    @Override public void onEnable() { tripped = false; Util.miningPos = null; }
    @Override public void onDisable() { if (mc.player != null) Util.releaseKeys(); Util.miningPos = null; }
    @Override public void onTick() {
        var me = mc.player;
        if (!tripped) {
            var p = Util.nearest(num("range", 24));
            if (p == null) return;
            tripped = true;
            me.displayClientMessage(Component.literal("[Axiom] SpawnerProtect: " + p.getName().getString() + " is near!"), false);
        }
        int r = (int) num("reach", 5);
        BlockPos c = me.blockPosition(), best = null;
        double bd = Double.MAX_VALUE;
        Vec3 eye = me.getEyePosition();
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            BlockPos p = c.offset(dx, dy, dz);
            if (!mc.level.getBlockState(p).is(Blocks.SPAWNER)) continue;
            double d = eye.distanceTo(Vec3.atCenterOf(p));
            if (d <= r && d < bd) { bd = d; best = p; }
        }
        if (best != null) { Util.mine(best); return; }
        Util.miningPos = null;
        setEnabled(false);
        if (num("logoff", 1) >= 1) me.connection.getConnection().disconnect(Component.literal("[Axiom] SpawnerProtect: disconnected"));
        else me.displayClientMessage(Component.literal("[Axiom] SpawnerProtect: spawners mined."), false);
    }
}
