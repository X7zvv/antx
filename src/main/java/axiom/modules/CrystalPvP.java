package axiom.modules;

import axiom.AxiomClient;
import axiom.Module;
import axiom.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;

/** Bot: runs AutoCrystal + AutoTotem and keeps a crystal-fight distance to the nearest player. */
public class CrystalPvP extends Module {
    private final List<Module> started = new ArrayList<>();
    public CrystalPvP() { super("CrystalPvP"); settings.put("range", "25"); }
    @Override public void onEnable() {
        started.clear();
        for (String n : new String[] { "AutoCrystal", "AutoTotem" }) {
            Module m = AxiomClient.find(n);
            if (m != null && !m.enabled) { m.setEnabled(true); started.add(m); }
        }
    }
    @Override public void onDisable() {
        for (Module m : started) m.setEnabled(false);
        started.clear();
        if (mc.player != null) Util.releaseKeys();
    }
    @Override public void onTick() {
        Player t = Util.nearest(num("range", 25));
        if (t == null) { Util.releaseKeys(); return; }
        double d = mc.player.distanceTo(t);
        Util.lookAt(t.position().add(0, t.getBbHeight() * 0.5, 0));
        var o = mc.options;
        o.keyUp.setDown(d > 6);
        o.keyDown.setDown(d < 3.5);
        o.keySprint.setDown(d > 6);
        o.keyJump.setDown(mc.player.horizontalCollision);
    }
}
