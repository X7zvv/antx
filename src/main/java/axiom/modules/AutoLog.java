package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.network.chat.Component;

/** Disconnects when health is low or a non-friend gets close. */
public class AutoLog extends Module {
    public AutoLog() {
        super("AutoLog");
        settings.put("health", "6");
        settings.put("playerRange", "0");   // blocks, 0 = off
    }
    @Override public void onTick() {
        var me = mc.player;
        double pr = num("playerRange", 0);
        String why = null;
        if (me.getHealth() <= num("health", 6)) why = "health low";
        else if (pr > 0 && Util.nearest(pr) != null) why = "player near";
        if (why == null) return;
        setEnabled(false);
        me.connection.getConnection().disconnect(Component.literal("[Axiom] AutoLog: " + why));
    }
}
