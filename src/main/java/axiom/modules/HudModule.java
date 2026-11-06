package axiom.modules;

import axiom.AxiomClient;
import axiom.Module;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class HudModule extends Module {
    public HudModule() {
        super("HUD");
        enabled = true;
        settings.put("watermark", "1");
        settings.put("fps", "1");
        settings.put("coords", "1");
        settings.put("speed", "0");
        settings.put("arraylist", "1");
        settings.put("rainbow", "0");
        settings.put("targetHud", "1");
    }

    public static void render(GuiGraphics g) {
        if (AxiomClient.find("HUD") instanceof HudModule h && h.enabled && mc.player != null && !mc.options.hideGui) h.draw(g);
    }

    private void draw(GuiGraphics g) {
        int accent = ClientSettings.accent();
        int y = 4;
        boolean wm = num("watermark", 1) >= 1, fps = num("fps", 1) >= 1;
        if (wm || fps) {
            String s = (wm ? "Axiom" : "") + (wm && fps ? "  |  " : "") + (fps ? mc.getFps() + " fps" : "");
            g.drawString(mc.font, s, 4, y, accent);
            y += 10;
        }
        if (num("coords", 1) >= 1) {
            var p = mc.player;
            g.drawString(mc.font, String.format(Locale.ROOT, "XYZ %d %d %d (%s)", (int) Math.floor(p.getX()), (int) Math.floor(p.getY()), (int) Math.floor(p.getZ()), p.getDirection().getName()), 4, y, 0xFFFFFFFF);
            y += 10;
        }
        if (num("speed", 0) >= 1) {
            Vec3 dm = mc.player.getDeltaMovement();
            g.drawString(mc.font, String.format(Locale.ROOT, "%.1f b/s", Math.hypot(dm.x, dm.z) * 20), 4, y, 0xFFFFFFFF);
        }
        if (num("arraylist", 1) >= 1) {
            List<Module> on = AxiomClient.MODULES.stream()
                .filter(x -> x.enabled && x != this && !(x instanceof ClientSettings))
                .sorted(Comparator.comparingInt((Module x) -> -mc.font.width(x.name)))
                .toList();
            boolean rainbow = num("rainbow", 0) >= 1;
            int ay = 4, i = 0;
            for (Module x : on) {
                int col = rainbow ? (0xFF000000 | Mth.hsvToRgb(((System.currentTimeMillis() / 15 + i * 8L) % 360) / 360f, 0.6f, 1f)) : 0xFFFFFFFF;
                g.drawString(mc.font, x.name, g.guiWidth() - mc.font.width(x.name) - 4, ay, col);
                ay += 10; i++;
            }
        }
        if (num("targetHud", 1) >= 1 && mc.hitResult instanceof EntityHitResult e && e.getEntity() instanceof Player p) {
            String t = p.getName().getString() + "  " + (int) (p.getHealth() + p.getAbsorptionAmount()) + " HP";
            g.drawString(mc.font, t, g.guiWidth() / 2 + 12, g.guiHeight() / 2 + 12, 0xFFFF5555);
        }
    }
}
