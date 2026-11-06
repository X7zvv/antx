package axiom.modules;

import axiom.Module;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/** Moves items out of an open chest/barrel/shulker. filter = words in the item id (empty = everything). */
public class ChestStealer extends Module {
    private int timer;
    public ChestStealer() {
        super("ChestStealer");
        settings.put("delay", "2");
        settings.put("filter", "");
        settings.put("autoClose", "1");
    }
    @Override public void onTick() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> s)) { timer = 0; return; }
        var m = s.getMenu();
        if (!(m instanceof ChestMenu) && !(m instanceof ShulkerBoxMenu)) return;
        if (++timer < num("delay", 2)) return;
        timer = 0;
        int top = m.slots.size() - 36;
        String[] words = str("filter", "").toLowerCase().split("[ ,]+");
        boolean any = false;
        for (int i = 0; i < top; i++) {
            var st = m.getSlot(i).getItem();
            if (st.isEmpty()) continue;
            String key = BuiltInRegistries.ITEM.getKey(st.getItem()).getPath();
            boolean ok = words.length == 0 || words[0].isEmpty();
            for (String w : words) if (!w.isEmpty() && key.contains(w)) ok = true;
            if (!ok) continue;
            mc.gameMode.handleInventoryMouseClick(m.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
            return;
        }
        if (num("autoClose", 1) >= 1) mc.player.closeContainer();
    }
}
