package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Loop: /rtp, wait for chunks, scan for bases, alert on players. Stops when a base is flagged (stopOnFind=1). */
public class RTPBaseFinder extends Module {
    private int phase, timer, cooldown;
    private Vec3 last = Vec3.ZERO;
    private boolean alerted;

    public RTPBaseFinder() {
        super("RTPBaseFinder");
        settings.put("command", "rtp");
        settings.put("wait", "100");        // ticks to wait after teleport before scanning
        settings.put("cooldown", "200");    // ticks between /rtp commands
        settings.put("minStorage", "4");
        settings.put("radius", "10");
        settings.put("playerAlert", "48");  // blocks, 0 = off
        settings.put("stopOnFind", "1");
    }
    @Override public void onEnable() { phase = 0; timer = 0; cooldown = 0; BaseFinder.SEEN.clear(); }
    @Override public void onTick() {
        double pa = num("playerAlert", 48);
        if (pa > 0 && !alerted) {
            Player p = Util.nearest(pa);
            if (p != null) {
                alerted = true;
                mc.player.displayClientMessage(Component.literal("[Axiom] PLAYER near: " + p.getName().getString()), false);
                mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            }
        }
        switch (phase) {
            case 0 -> {
                if (cooldown > 0) { cooldown--; return; }
                if (mc.screen != null) return;
                last = mc.player.position();
                mc.player.connection.sendCommand(str("command", "rtp"));
                phase = 1; timer = 0; alerted = false;
            }
            case 1 -> {
                timer++;
                if (mc.player.position().distanceTo(last) > 50 || timer > 400) { phase = 2; timer = 0; }
            }
            case 2 -> {
                if (++timer < num("wait", 100)) return;
                int f = BaseFinder.scanOnce((int) num("minStorage", 4), true, (int) num("radius", 10));
                if (f > 0 && num("stopOnFind", 1) >= 1) {
                    mc.player.displayClientMessage(Component.literal("[Axiom] RTPBaseFinder: base found, stopped."), false);
                    mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0f, 1.0f);
                    setEnabled(false);
                    return;
                }
                phase = 0; timer = 0; cooldown = (int) num("cooldown", 200);
            }
        }
    }
}
