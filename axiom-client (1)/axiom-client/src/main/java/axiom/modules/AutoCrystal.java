package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoCrystal extends Module {
    private int placeCd, breakCd;
    public AutoCrystal() {
        super("AutoCrystal");
        settings.put("range", "4.5");
        settings.put("targetRange", "10");
        settings.put("placeDelay", "2");
        settings.put("breakDelay", "1");
        settings.put("minSelfDist", "3.0");
        settings.put("minHealth", "6");
    }
    @Override public void onTick() {
        var me = mc.player;
        if (mc.gameMode == null || mc.screen != null) return;
        if (placeCd > 0) placeCd--;
        if (breakCd > 0) breakCd--;
        Player t = Util.nearest(num("targetRange", 10));
        if (t == null) return;
        double range = num("range", 4.5);
        Vec3 eye = me.getEyePosition();

        if (breakCd == 0) {
            EndCrystal best = null; double bd = Double.MAX_VALUE;
            for (EndCrystal c : mc.level.getEntitiesOfClass(EndCrystal.class, me.getBoundingBox().inflate(range))) {
                if (eye.distanceTo(c.position()) > range) continue;
                double dt = c.distanceTo(t);
                if (dt > 7) continue;
                if (dt < bd) { bd = dt; best = c; }
            }
            if (best != null && me.getHealth() + me.getAbsorptionAmount() >= num("minHealth", 6)) {
                mc.gameMode.attack(me, best);
                me.swing(InteractionHand.MAIN_HAND);
                breakCd = (int) num("breakDelay", 1);
                return;
            }
        }
        if (placeCd > 0 || me.getHealth() + me.getAbsorptionAmount() < num("minHealth", 6)) return;

        InteractionHand hand = InteractionHand.OFF_HAND;
        if (!me.getOffhandItem().is(Items.END_CRYSTAL)) {
            int slot = Util.findHotbar(s -> s.is(Items.END_CRYSTAL));
            if (slot < 0) return;
            Util.select(slot);
            hand = InteractionHand.MAIN_HAND;
        }
        BlockPos tp = t.blockPosition(), bestPos = null;
        double bs = Double.MAX_VALUE, minSelf = num("minSelfDist", 3.0);
        for (int dx = -4; dx <= 4; dx++) for (int dy = -3; dy <= 1; dy++) for (int dz = -4; dz <= 4; dz++) {
            BlockPos b = tp.offset(dx, dy, dz);
            BlockState st = mc.level.getBlockState(b);
            if (!st.is(Blocks.OBSIDIAN) && !st.is(Blocks.BEDROCK)) continue;
            if (!mc.level.getBlockState(b.above()).isAir() || !mc.level.getBlockState(b.above(2)).isAir()) continue;
            Vec3 cc = new Vec3(b.getX() + 0.5, b.getY() + 1, b.getZ() + 0.5);
            if (eye.distanceTo(cc) > range || me.position().distanceTo(cc) < minSelf) continue;
            double score = cc.distanceTo(t.position());
            if (score > 6 || score >= bs) continue;
            AABB box = new AABB(b.getX(), b.getY() + 1, b.getZ(), b.getX() + 1, b.getY() + 3, b.getZ() + 1);
            if (!mc.level.getEntities((net.minecraft.world.entity.Entity) null, box).isEmpty()) continue;
            bs = score; bestPos = b;
        }
        if (bestPos == null) return;
        var hit = new BlockHitResult(new Vec3(bestPos.getX() + 0.5, bestPos.getY() + 1.0, bestPos.getZ() + 0.5), Direction.UP, bestPos, false);
        if (mc.gameMode.useItemOn(me, hand, hit).consumesAction()) {
            me.swing(hand);
            placeCd = (int) num("placeDelay", 2);
        }
    }
}
