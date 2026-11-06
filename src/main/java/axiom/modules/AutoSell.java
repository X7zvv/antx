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
        settings.put("minItems", "1");     // only start a cycle with at least this many item stacks
        settings.put("wait", "6");         // ticks to wait after the menu opens / before it closes
    }
    @Override public void onEnable() { phase = 0; timer = (int) num("interval", 600); }
    private int stacks() {
        int n = 0;
        for (int i = 9; i < 36; i++) if (!mc.player.getInventory().getItem(i).isEmpty()) n++;
        return n;
    }
    @Override public void onTick() {
        timer++;
        int wait = (int) num("wait", 6);
        switch (phase) {
            case 0 -> {
                if (timer >= num("interval", 600) && mc.screen == null) {
                    if (stacks() < num("minItems", 1)) { timer = 0; return; }
                    mc.player.connection.sendCommand(str("command", "sell"));
                    phase = 1; timer = 0;
                }
            }
            case 1 -> {
                if (mc.screen instanceof AbstractContainerScreen<?> s) {
                    if (timer >= wait) { moveAll(s); phase = 2; timer = 0; }
                } else if (timer > 60) { phase = 0; timer = 0; }
            }
            case 2 -> {
                if (timer >= wait) { mc.player.closeContainer(); phase = 0; timer = 0; }
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
