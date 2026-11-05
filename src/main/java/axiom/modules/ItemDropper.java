package axiom.modules;

import axiom.Module;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;

/** Drops every stack whose item id contains one of the words in "items" (BoneDropper / BlazeRodDropper / ShulkerDropper in one). */
public class ItemDropper extends Module {
    private int timer;
    public ItemDropper() {
        super("ItemDropper");
        settings.put("items", "bone");        // comma list, e.g. bone,blaze_rod,shulker_box
        settings.put("delay", "2");
        settings.put("keepHotbar", "0");
    }
    @Override public void onTick() {
        if (mc.gameMode == null || ++timer < num("delay", 2)) return;
        timer = 0;
        String[] words = str("items", "bone").toLowerCase().split("[ ,]+");
        int from = num("keepHotbar", 0) >= 1 ? 9 : 9;
        int to = num("keepHotbar", 0) >= 1 ? 35 : 44;
        var menu = mc.player.inventoryMenu;
        for (int i = from; i <= to; i++) {
            var st = menu.getSlot(i).getItem();
            if (st.isEmpty()) continue;
            String key = BuiltInRegistries.ITEM.getKey(st.getItem()).toString();
            for (String w : words) {
                if (!w.isEmpty() && key.contains(w)) {
                    mc.gameMode.handleInventoryMouseClick(menu.containerId, i, 1, ClickType.THROW, mc.player);
                    return;
                }
            }
        }
    }
}
