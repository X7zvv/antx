package axiom.modules;

import axiom.Module;
import axiom.Util;

/** Keeps you from being kicked for AFK. mode: 0 = look around, 1 = jump, 2 = walk a bit, 3 = all of them. */
public class AntiAFK extends Module {
    private int timer, walk;
    public AntiAFK() {
        super("AntiAFK");
        settings.put("interval", "300");
        settings.put("mode", "3");
        settings.put("walkTicks", "8");
    }
    @Override public void onDisable() { if (mc.player != null) Util.releaseKeys(); walk = 0; }
    @Override public void onTick() {
        if (walk > 0) {
            mc.options.keyUp.setDown(true);
            if (--walk == 0) mc.options.keyUp.setDown(false);
        }
        if (++timer < num("interval", 300) || mc.screen != null) return;
        timer = 0;
        int mode = (int) num("mode", 3);
        if (mode == 0 || mode == 3) { mc.player.setYRot(mc.player.getYRot() + (mc.level.random.nextFloat() - 0.5f) * 60f); mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND); }
        if ((mode == 1 || mode == 3) && mc.player.onGround()) mc.player.jumpFromGround();
        if (mode == 2 || mode == 3) walk = (int) num("walkTicks", 8);
    }
}
