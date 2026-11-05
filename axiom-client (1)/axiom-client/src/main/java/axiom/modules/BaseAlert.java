package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class BaseAlert extends Module {
    private int cd;
    public BaseAlert() {
        super("BaseAlert");
        settings.put("range", "64");
        settings.put("cooldown", "200");
    }
    @Override public void onTick() {
        if (cd > 0) { cd--; return; }
        double range = num("range", 64);
        for (Player p : mc.level.players()) {
            if (p == mc.player || p.isSpectator() || Util.isFriend(p)) continue;
            double d = mc.player.distanceTo(p);
            if (d > range) continue;
            mc.player.displayClientMessage(Component.literal("[Axiom] ALERT: " + p.getName().getString() + " is " + (int) d + " blocks away"), false);
            mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            cd = (int) num("cooldown", 200);
            return;
        }
    }
}
