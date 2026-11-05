package axiom.modules;

import axiom.Module;
import axiom.Util;

/** Accepts teleport requests from friends (or everyone if acceptAll=1). Fed by a chat hook in AxiomClient. */
public class AutoTPA extends Module {
    public AutoTPA() {
        super("AutoTPA");
        settings.put("acceptAll", "0");
        settings.put("command", "tpaccept");
    }
    public void handle(String text) {
        if (mc.player == null) return;
        String low = text.toLowerCase();
        if (!low.contains("teleport") || !(low.contains("request") || low.contains("tpa"))) return;
        boolean ok = num("acceptAll", 0) >= 1;
        if (!ok) for (String tok : low.split("[^a-z0-9_]+")) if (Util.FRIENDS.contains(tok)) { ok = true; break; }
        if (ok) mc.player.connection.sendCommand(str("command", "tpaccept"));
    }
}
