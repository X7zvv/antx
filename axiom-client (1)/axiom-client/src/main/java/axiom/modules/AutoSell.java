package axiom.modules;

import axiom.Module;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;

/** Opens the sell GUI, shift-clicks inventory items into it, closes it, repeats. */
public class AutoSell extends Module {
    private int phase, timer;
    public AutoSell() {
        super("AutoSell");
        settings.put("command", "sell");
        settings.put("interval", "600");   // ticks between cycles
        settings.put("only", "");          // item-name filter, empty = everything
        settings.put("keepHotbar", "1");   // 1 = never sell hotbar items
    }
    @Override public void onEnable() { phase = 0; timer = (int) num("interval", 600); }
    @Override public void onTick() {
        timer++;
        switch (phase) {
            case 0 -> {
                if (timer >= num("interval", 600) && mc.screen == null) {
                    mc.player.connection.sendCommand(str("command", "sell"));
                    phase = 1; timer = 0;
                }
            }
            case 1 -> {
                if (mc.screen instanceof AbstractContainerScreen<?> s) {
                    if (timer >= 6) { moveAll(s); phase = 2; timer = 0; }
                } else if (timer > 60) { phase = 0; timer = 0; }
            }
            case 2 -> {
                if (timer >= 6) { mc.player.closeContainer(); phase = 0; timer = 0; }
            }
        }
    }
    private void moveAll(AbstractContainerScreen<?> s) {
        var m = s.getMenu();
        int total = m.slots.size();
        int start = total - 36;
        if (start <= 0) return;
        String only = str("only", "").toLowerCase().trim();
        boolean keep = num("keepHotbar", 1) >= 1;
        for (int i = start; i < total; i++) {
            if (keep && i >= total - 9) continue;
            var st = m.getSlot(i).getItem();
            if (st.isEmpty()) continue;
            if (!only.isEmpty() && !BuiltInRegistries.ITEM.getKey(st.getItem()).toString().contains(only)) continue;
            mc.gameMode.handleInventoryMouseClick(m.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
        }
    }
}
