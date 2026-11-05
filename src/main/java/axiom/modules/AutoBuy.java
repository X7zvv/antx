package axiom.modules;

import axiom.Module;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/** Opens the auction house, buys the first listing at or under maxPrice, clicks Confirm. */
public class AutoBuy extends Module {
    private static final Pattern PRICE = Pattern.compile("\\$\\s*([0-9][0-9,]*\\.?[0-9]*)\\s*([kKmMbB]?)");
    private int timer, cd;
    public AutoBuy() {
        super("AutoBuy");
        settings.put("command", "ah");
        settings.put("item", "");          // name/id substring to buy, empty = any
        settings.put("maxPrice", "1000");  // price read from item lore
        settings.put("refresh", "100");    // ticks before closing + reopening the AH
        settings.put("delay", "4");        // ticks between clicks
    }
    @Override public void onEnable() { timer = 0; cd = 0; }
    @Override public void onTick() {
        if (cd > 0) { cd--; return; }
        if (!(mc.screen instanceof AbstractContainerScreen<?> s)) {
            if (mc.screen == null && ++timer >= 20) {
                mc.player.connection.sendCommand(str("command", "ah"));
                timer = 0; cd = 10;
            }
            return;
        }
        var m = s.getMenu();
        int top = m.slots.size() - 36;
        if (top <= 0) return;
        // 1) confirmation screen
        for (int i = 0; i < top; i++) {
            ItemStack st = m.getSlot(i).getItem();
            if (!st.isEmpty() && st.getHoverName().getString().toLowerCase().contains("confirm")) {
                click(m.containerId, i); cd = 4; return;
            }
        }
        // 2) listing scan
        double max = num("maxPrice", 1000);
        String f = str("item", "").toLowerCase().trim();
        for (int i = 0; i < top; i++) {
            ItemStack st = m.getSlot(i).getItem();
            if (st.isEmpty()) continue;
            if (!f.isEmpty() && !st.getHoverName().getString().toLowerCase().contains(f)
                    && !BuiltInRegistries.ITEM.getKey(st.getItem()).toString().contains(f)) continue;
            double price = price(st);
            if (price >= 0 && price <= max) {
                click(m.containerId, i);
                cd = (int) num("delay", 4);
                return;
            }
        }
        // 3) nothing found: refresh
        if (++timer >= num("refresh", 100)) { mc.player.closeContainer(); timer = 0; }
    }
    private void click(int id, int slot) {
        mc.gameMode.handleInventoryMouseClick(id, slot, 0, ClickType.PICKUP, mc.player);
    }
    private static double price(ItemStack st) {
        ItemLore lore = st.get(DataComponents.LORE);
        if (lore == null) return -1;
        for (Component line : lore.lines()) {
            Matcher mt = PRICE.matcher(line.getString());
            if (!mt.find()) continue;
            try {
                double v = Double.parseDouble(mt.group(1).replace(",", ""));
                switch (mt.group(2).toLowerCase()) {
                    case "k" -> v *= 1e3;
                    case "m" -> v *= 1e6;
                    case "b" -> v *= 1e9;
                    default -> {}
                }
                return v;
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }
}
