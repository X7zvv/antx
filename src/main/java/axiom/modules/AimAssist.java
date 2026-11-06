package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimAssist extends Module {
    public AimAssist() {
        super("AimAssist");
        settings.put("range", "4.5");
        settings.put("fov", "90");
        settings.put("speed", "6");
        settings.put("aimHeight", "0.6");   // 0 = feet, 1 = head
        settings.put("vertical", "1");      // 0 = only left/right
        settings.put("onlyAttack", "0");    // 1 = only while holding attack
    }
    @Override public void onTick() {
        var me = mc.player;
        if (mc.screen != null || mc.getCameraEntity() != me) return;
        if (num("onlyAttack", 0) >= 1 && !mc.options.keyAttack.isDown()) return;
        double bestDist = num("range", 4.5);
        float bestYaw = 0, bestPitch = 0;
        boolean found = false;
        Vec3 eye = me.getEyePosition();
        for (Player p : mc.level.players()) {
            if (p == me || !p.isAlive() || p.isSpectator() || Util.isFriend(p)) continue;
            double d = me.distanceTo(p);
            if (d > bestDist) continue;
            Vec3 t = p.position().add(0, p.getBbHeight() * num("aimHeight", 0.6), 0);
            double dx = t.x - eye.x, dy = t.y - eye.y, dz = t.z - eye.z;
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
            if (Math.abs(Mth.wrapDegrees(yaw - me.getYRot())) > num("fov", 90) / 2) continue;
            bestDist = d; bestYaw = yaw; bestPitch = pitch; found = true;
        }
        if (!found) return;
        float step = (float) num("speed", 6);
        me.setYRot(me.getYRot() + Mth.clamp(Mth.wrapDegrees(bestYaw - me.getYRot()), -step, step));
        if (num("vertical", 1) >= 1)
            me.setXRot(Mth.clamp(me.getXRot() + Mth.clamp(Mth.wrapDegrees(bestPitch - me.getXRot()), -step, step), -90f, 90f));
    }
}
