package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.world.entity.player.Player;

/** Glow outline on other players (seen through walls). */
public class PlayerESP extends Module {
    public static volatile boolean active;
    private static double range;
    private static boolean invisible, friends;

    public PlayerESP() {
        super("PlayerESP");
        settings.put("range", "0");        // blocks, 0 = unlimited
        settings.put("invisible", "1");    // include invisible players
        settings.put("friends", "1");      // also outline friends
    }
    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }
    @Override public void onTick() {
        range = num("range", 0);
        invisible = num("invisible", 1) >= 1;
        friends = num("friends", 1) >= 1;
    }
    public static boolean shouldGlow(Player p) {
        if (!invisible && p.isInvisible()) return false;
        if (!friends && Util.isFriend(p)) return false;
        return range <= 0 || net.minecraft.client.Minecraft.getInstance().player.distanceTo(p) <= range;
    }
}
