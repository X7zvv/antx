package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/** Wind-charge launch, then smash with the mace on the way down. Needs a mace (and ideally wind charges) in the hotbar. */
public class MacePvP extends Module {
    private int prep, cd;
    private double peakY;
    private boolean warned;
    public MacePvP() {
        super("MacePvP");
        settings.put("range", "20");
        settings.put("reach", "3.5");
        settings.put("minFall", "1.5");
    }
    @Override public void onEnable() { warned = false; prep = 0; cd = 0; }
    @Override public void onDisable() { if (mc.player != null) Util.releaseKeys(); }
    @Override public void onTick() {
        var me = mc.player;
        Player t = Util.nearest(num("range", 20));
        if (t == null) { Util.releaseKeys(); return; }
        int mace = Util.findHotbar(s -> s.is(Items.MACE));
        if (mace < 0) {
            if (!warned) { me.displayClientMessage(Component.literal("[Axiom] MacePvP: put a mace in your hotbar"), false); warned = true; }
            return;
        }
        int wind = Util.findHotbar(s -> s.is(Items.WIND_CHARGE));
        if (cd > 0) cd--;
        peakY = me.onGround() ? me.getY() : Math.max(peakY, me.getY());
        double fall = peakY - me.getY();
        double d = me.distanceTo(t);

        if (me.onGround() && wind >= 0 && cd == 0 && d > 2.5 && d <= 10) {
            Util.select(wind);
            me.setXRot(90f);               // look down first, fire on the next tick once the server has the rotation
            if (++prep >= 2) { mc.gameMode.useItem(me, InteractionHand.MAIN_HAND); prep = 0; cd = 30; }
            return;
        }
        prep = 0;
        Util.select(mace);
        Util.lookAt(t.position().add(0, t.getBbHeight() * 0.5, 0));
        var o = mc.options;
        o.keyUp.setDown(d > 2.5);
        o.keySprint.setDown(true);
        o.keyJump.setDown(me.onGround() && wind < 0 && d < 5);
        if (!me.onGround() && fall >= num("minFall", 1.5) && d <= num("reach", 3.5) && me.getAttackStrengthScale(0.5f) >= 0.9f) {
            mc.gameMode.attack(me, t);
            me.swing(InteractionHand.MAIN_HAND);
        }
    }
}
