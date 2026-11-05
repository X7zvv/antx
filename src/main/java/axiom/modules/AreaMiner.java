package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Mines everything inside the box pos1..pos2. Set corners with: /axiom set AreaMiner pos1 here  (or "x y z"). */
public class AreaMiner extends Module {
    private BlockPos current;
    public AreaMiner() {
        super("AreaMiner");
        settings.put("pos1", "");
        settings.put("pos2", "");
        settings.put("reach", "4.5");
    }
    private static int[] parse(String s) {
        String[] p = s.trim().split("[ ,]+");
        if (p.length != 3) return null;
        try { return new int[] { Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]) }; }
        catch (NumberFormatException e) { return null; }
    }
    @Override public void onDisable() {
        current = null;
        if (mc.player != null) Util.releaseKeys();
        if (mc.gameMode != null) mc.gameMode.stopDestroyBlock();
    }
    @Override public void onTick() {
        int[] a = parse(str("pos1", "")), b = parse(str("pos2", ""));
        var me = mc.player;
        if (a == null || b == null) {
            me.displayClientMessage(Component.literal("[Axiom] AreaMiner: set pos1 and pos2 first (/axiom set AreaMiner pos1 here)"), false);
            setEnabled(false);
            return;
        }
        int x0 = Math.min(a[0], b[0]), x1 = Math.max(a[0], b[0]);
        int y0 = Math.min(a[1], b[1]), y1 = Math.max(a[1], b[1]);
        int z0 = Math.min(a[2], b[2]), z1 = Math.max(a[2], b[2]);
        if ((long) (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1) > 300_000L) {
            me.displayClientMessage(Component.literal("[Axiom] AreaMiner: area too large (max 300000 blocks)"), false);
            setEnabled(false);
            return;
        }
        Vec3 eye = me.getEyePosition();
        double reach = num("reach", 4.5);
        BlockPos under = me.blockPosition().below();
        BlockPos inReach = null, nearestAny = null;
        double rd = Double.MAX_VALUE, ad = Double.MAX_VALUE;
        for (int y = y1; y >= y0; y--) {
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
                BlockPos p = new BlockPos(x, y, z);
                if (p.equals(under)) continue;
                BlockState st = mc.level.getBlockState(p);
                if (st.isAir() || !st.getFluidState().isEmpty() || st.getDestroySpeed(mc.level, p) < 0) continue;
                double d = eye.distanceTo(Vec3.atCenterOf(p));
                if (d <= reach && d < rd) { rd = d; inReach = p; }
                if (d < ad) { ad = d; nearestAny = p; }
            }
            if (inReach != null) break;   // finish the top-most layer that has reachable blocks first
        }
        if (inReach != null) {
            Util.releaseKeys();
            BlockState st = mc.level.getBlockState(inReach);
            int best = -1; float bestSpeed = 1f;
            for (int i = 0; i < 9; i++) {
                float sp = me.getInventory().getItem(i).getDestroySpeed(st);
                if (sp > bestSpeed) { bestSpeed = sp; best = i; }
            }
            Util.select(best);
            Util.lookAt(Vec3.atCenterOf(inReach));
            Direction face = Direction.getApproximateNearest(eye.x - (inReach.getX() + 0.5), eye.y - (inReach.getY() + 0.5), eye.z - (inReach.getZ() + 0.5));
            if (!inReach.equals(current)) { mc.gameMode.startDestroyBlock(inReach, face); current = inReach; }
            else mc.gameMode.continueDestroyBlock(inReach, face);
            me.swing(InteractionHand.MAIN_HAND);
        } else if (nearestAny != null) {
            Vec3 t = Vec3.atCenterOf(nearestAny);
            me.setYRot((float) (Math.toDegrees(Math.atan2(t.z - me.getZ(), t.x - me.getX())) - 90.0));
            mc.options.keyUp.setDown(true);
            mc.options.keyJump.setDown(me.horizontalCollision);
        } else {
            me.displayClientMessage(Component.literal("[Axiom] AreaMiner: area cleared."), false);
            setEnabled(false);
        }
    }
}
