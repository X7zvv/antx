package axiom.modules;

import axiom.Module;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class Fullbright extends Module {
    public Fullbright() { super("Fullbright"); }
    @Override public void onTick() {
        if (!mc.player.hasEffect(MobEffects.NIGHT_VISION))
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false, false));
    }
    @Override public void onDisable() {
        if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}
