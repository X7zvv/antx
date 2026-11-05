package axiom.modules;

import axiom.Module;

public class Fly extends Module {
    public Fly() { super("Fly"); settings.put("speed", "1.0"); }
    @Override public void onTick() {
        var a = mc.player.getAbilities();
        a.mayfly = true;
        a.flying = true;
        a.setFlyingSpeed((float) (0.05 * num("speed", 1.0)));
    }
    @Override public void onDisable() {
        if (mc.player == null) return;
        var a = mc.player.getAbilities();
        a.setFlyingSpeed(0.05f);
        if (!mc.player.isCreative() && !mc.player.isSpectator()) { a.mayfly = false; a.flying = false; }
    }
}
