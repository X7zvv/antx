package axiom.modules;

import axiom.AxiomClient;
import axiom.Module;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class HudModule extends Module {
    public HudModule() {
        super("HUD");
        enabled = true;
        settings.put("arraylist", "1");
        settings.put("targetHud", "1");
    }

    public static void render(GuiGraphics g) {
        if (AxiomClient.find("HUD") instanceof HudModule h && h.enabled && mc.player != null && !mc.options.hideGui) h.draw(g);
    }

    private void draw(GuiGraphics g) {
        g.drawString(mc.font, "Axiom", 4, 4, 0xFF55FFFF);
        if (num("arraylist", 1) >= 1) {
            List<Module> on = AxiomClient.MODULES.stream()
                .filter(x -> x.enabled && x != this)
                .sorted(Comparator.comparingInt((Module x) -> -mc.font.width(x.name)))
                .toList();
            int y = 4;
            for (Module x : on) {
                g.drawString(mc.font, x.name, g.guiWidth() - mc.font.width(x.name) - 4, y, 0xFFFFFFFF);
                y += 10;
            }
        }
        if (num("targetHud", 1) >= 1 && mc.hitResult instanceof EntityHitResult e && e.getEntity() instanceof Player p) {
            String t = p.getName().getString() + "  " + (int) (p.getHealth() + p.getAbsorptionAmount()) + " HP";
            g.drawString(mc.font, t, g.guiWidth() / 2 + 12, g.guiHeight() / 2 + 12, 0xFFFF5555);
        }
    }
}
