package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;

public class AutoEat extends Module {
    private boolean eating;
    public AutoEat() { super("AutoEat"); settings.put("hunger", "14"); }
    @Override public void onDisable() { if (eating && mc.player != null) mc.options.keyUse.setDown(false); eating = false; }
    @Override public void onTick() {
        boolean need = mc.player.getFoodData().getFoodLevel() <= num("hunger", 14);
        if (eating && !need) { mc.options.keyUse.setDown(false); eating = false; return; }
        if (!need || mc.screen != null) return;
        if (!eating) {
            int slot = Util.findHotbar(s -> {
                if (!s.has(DataComponents.FOOD)) return false;
                String k = BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
                return !(k.contains("rotten") || k.contains("spider_eye") || k.contains("poisonous") || k.contains("pufferfish")
                    || k.contains("chorus") || k.contains("enchanted_golden") || k.contains("suspicious"));
            });
            if (slot < 0) return;
            Util.select(slot);
            eating = true;
        }
        mc.options.keyUse.setDown(true);
    }
}
