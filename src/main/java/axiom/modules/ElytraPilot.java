package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

/** Flies to x/z at the given altitude using fireworks from the hotbar. Elytra must be worn. */
public class ElytraPilot extends Module {
    private int cd;
    public ElytraPilot() {
        super("ElytraPilot");
        settings.put("x", "0");
        settings.put("z", "0");
        settings.put("altitude", "200");
    }
    @Override public void onDisable() { if (mc.player != null) Util.releaseKeys(); }
    @Override public void onTick() {
        var me = mc.player;
        if (!me.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            me.displayClientMessage(Component.literal("[Axiom] ElytraPilot: wear an elytra"), false);
            setEnabled(false);
            return;
        }
        if (cd > 0) cd--;
        if (!me.isFallFlying()) {
            if (me.onGround()) mc.options.keyJump.setDown(!mc.options.keyJump.isDown());
            else if (me.tryToStartFallFlying())
                me.connection.send(new ServerboundPlayerCommandPacket(me, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            return;
        }
        mc.options.keyJump.setDown(false);
        double tx = num("x", 0), tz = num("z", 0), alt = num("altitude", 200);
        double dx = tx - me.getX(), dz = tz - me.getZ();
        if (Math.hypot(dx, dz) < 25) {
            me.displayClientMessage(Component.literal("[Axiom] ElytraPilot: arrived."), false);
            setEnabled(false);
            return;
        }
        me.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        float pitch = me.getY() < alt - 5 ? -35f : (me.getY() > alt + 10 ? 20f : 2f);
        me.setXRot(pitch);
        if (cd == 0 && me.getDeltaMovement().length() < 0.9) {
            int fw = Util.findHotbar(s -> s.is(Items.FIREWORK_ROCKET));
            if (fw >= 0) {
                Util.select(fw);
                mc.gameMode.useItem(me, InteractionHand.MAIN_HAND);
                cd = 40;
            }
        }
    }
}
