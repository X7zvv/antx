package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
    public TriggerBot() {
        super("TriggerBot");
        settings.put("cooldown", "0.95");
        settings.put("crit", "0");          // 1 = only attack while falling (critical hits)
        settings.put("weaponOnly", "0");    // 1 = only with sword / axe / mace in hand
        settings.put("hitDelay", "0");      // extra ticks between hits
    }
    private int wait;
    @Override public void onTick() {
        var p = mc.player;
        if (mc.gameMode == null || mc.screen != null || mc.getCameraEntity() != p) return;
        if (wait > 0) { wait--; return; }
        if (!(mc.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player t)) return;
        if (!t.isAlive() || t.isSpectator() || Util.isFriend(t)) return;
        if (p.getAttackStrengthScale(0.5f) < num("cooldown", 0.95)) return;
        if (num("crit", 0) >= 1 && (p.onGround() || p.getDeltaMovement().y >= 0 || p.isInWater())) return;
        if (num("weaponOnly", 0) >= 1) {
            var h = p.getMainHandItem();
            if (!h.is(ItemTags.SWORDS) && !h.is(ItemTags.AXES) && !h.is(Items.MACE)) return;
        }
        mc.gameMode.attack(p, t);
        p.swing(InteractionHand.MAIN_HAND);
        wait = (int) num("hitDelay", 0);
    }
}
