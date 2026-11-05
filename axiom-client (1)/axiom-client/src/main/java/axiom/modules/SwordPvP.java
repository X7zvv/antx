package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SwordPvP extends Module {
    private int tick;
    public SwordPvP() {
        super("SwordPvP");
        settings.put("range", "20");
        settings.put("reach", "3.0");
        settings.put("cooldown", "0.95");
        settings.put("strafe", "1");
    }
    @Override public void onDisable() { if (mc.player != null) Util.releaseKeys(); }
    @Override public void onTick() {
        var me = mc.player;
        Player t = Util.nearest(num("range", 20));
        if (t == null) { Util.releaseKeys(); return; }
        int sw = Util.findHotbar(s -> s.is(ItemTags.SWORDS));
        if (sw >= 0) Util.select(sw);
        Util.lookAt(t.position().add(0, t.getBbHeight() * 0.6, 0));
        double d = me.distanceTo(t);
        var o = mc.options;
        o.keyUp.setDown(d > 2.4);
        o.keySprint.setDown(true);
        o.keyJump.setDown(me.horizontalCollision);
        boolean strafe = num("strafe", 1) >= 1 && d < 4;
        boolean left = (tick++ / 20) % 2 == 0;
        o.keyLeft.setDown(strafe && left);
        o.keyRight.setDown(strafe && !left);
        if (d <= num("reach", 3.0) && me.getAttackStrengthScale(0.5f) >= num("cooldown", 0.95)) {
            mc.gameMode.attack(me, t);
            me.swing(InteractionHand.MAIN_HAND);
        }
    }
}
