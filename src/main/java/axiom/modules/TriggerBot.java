package axiom.modules;

import axiom.Module;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
    public TriggerBot() { super("TriggerBot"); settings.put("cooldown", "0.95"); }
    @Override public void onTick() {
        var p = mc.player;
        if (mc.gameMode == null || mc.screen != null) return;
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Player t
                && t.isAlive() && !t.isSpectator() && !axiom.Util.isFriend(t) && p.getAttackStrengthScale(0.5f) >= num("cooldown", 0.95)) {
            mc.gameMode.attack(p, t);
            p.swing(InteractionHand.MAIN_HAND);
        }
    }
}
