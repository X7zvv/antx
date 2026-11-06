package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/** Straight 1x2 tunnel in the direction you face. Stops at lava/water ahead. */
public class AutoMine extends Module {
    private Direction dir = Direction.NORTH;
    private BlockPos start = BlockPos.ZERO;
    public AutoMine() { super("AutoMine"); settings.put("length", "0"); settings.put("lavaCheck", "1"); }   // length 0 = unlimited
    @Override public void onEnable() { dir = mc.player.getDirection(); start = mc.player.blockPosition(); Util.miningPos = null; }
    @Override public void onDisable() { if (mc.player != null) Util.releaseKeys(); Util.miningPos = null; }

    private boolean solid(BlockPos p) {
        var st = mc.level.getBlockState(p);
        return !st.isAir() && st.getFluidState().isEmpty() && st.getDestroySpeed(mc.level, p) >= 0;
    }
    private void stop(String why) {
        mc.player.displayClientMessage(Component.literal("[Axiom] AutoMine stopped: " + why), false);
        setEnabled(false);
    }
    @Override public void onTick() {
        var me = mc.player;
        BlockPos feet = me.blockPosition();
        for (int k = 1; k <= (num("lavaCheck", 1) >= 1 ? 3 : 0); k++) {
            BlockPos p = feet.relative(dir, k);
            for (BlockPos q : new BlockPos[] { p, p.above(), p.below() })
                if (!mc.level.getBlockState(q).getFluidState().isEmpty()) { stop("liquid ahead"); return; }
        }
        int len = (int) num("length", 0);
        if (len > 0 && feet.distManhattan(start) >= len) { stop("length reached"); return; }
        BlockPos a1 = feet.relative(dir), a2 = a1.above();
        BlockPos tgt = solid(a2) ? a2 : (solid(a1) ? a1 : null);
        if (tgt != null) {
            Util.releaseKeys();
            Util.mine(tgt);
        } else {
            Util.miningPos = null;
            me.setYRot(dir.toYRot());
            mc.options.keyUp.setDown(true);
        }
    }
}
