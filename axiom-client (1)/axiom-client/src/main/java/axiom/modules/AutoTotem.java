package axiom.modules;

import axiom.Module;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
    private int cooldown;
    public AutoTotem() { super("AutoTotem"); }
    @Override public void onTick() {
        if (mc.gameMode == null || mc.screen != null) return;
        if (cooldown > 0) { cooldown--; return; }
        var p = mc.player;
        if (p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        int menuSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) { menuSlot = i < 9 ? i + 36 : i; break; }
        }
        if (menuSlot < 0) return;
        int id = p.inventoryMenu.containerId;
        mc.gameMode.handleInventoryMouseClick(id, menuSlot, 0, ClickType.PICKUP, p);
        mc.gameMode.handleInventoryMouseClick(id, 45, 0, ClickType.PICKUP, p);
        if (!p.inventoryMenu.getCarried().isEmpty())
            mc.gameMode.handleInventoryMouseClick(id, menuSlot, 0, ClickType.PICKUP, p);
        cooldown = 4;
    }
}
