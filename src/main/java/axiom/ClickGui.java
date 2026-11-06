package axiom;

import axiom.modules.ClientSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Fully custom-drawn client menu: sidebar tabs, search, module cards with animated switches, settings panel, base list. */
public class ClickGui extends Screen {
    private static final String[] TABS = { "Combat", "Movement", "Render", "Donut", "Finder", "Auto", "Misc", "Bases" };
    private static final int GREEN = 0xFF3DDC84, SIDE = 0xFF0B0B12, PANEL = 0xFF14141C, CARD = 0xFF181822;
    private static final Set<String> BOOLS = Set.of("acceptAll", "keepHotbar", "spawners", "stopOnFind", "rotate", "arraylist",
        "targetHud", "strafe", "perItem", "storage", "placed", "tunnels", "alert", "showScanned", "omni", "crit", "weaponOnly",
        "vertical", "onlyAttack", "logoff", "sound", "autoClose", "invisible", "friends", "watermark", "coords", "fps", "speed_hud",
        "rainbow", "lavaCheck", "diamond", "debris", "gold", "iron", "emerald", "redstone", "lapis", "coal", "copper", "quartz",
        "spawner", "chests", "beacon", "tracers", "labels");

    private static String tab = "Combat", query = "";
    private static Module selected;
    private static double scroll, sscroll, bscroll;

    private record Hit(int x1, int y1, int x2, int y2, IntConsumer act) {}
    private final List<Hit> hits = new ArrayList<>();
    private int mx, my, setX, accent, accentL, bg;
    private EditBox search;

    public ClickGui() { super(Component.literal("Axiom")); }

    // ---------- helpers ----------
    private boolean hover(int x1, int y1, int x2, int y2) { return mx >= x1 && mx < x2 && my >= y1 && my < y2; }
    private void hit(int x1, int y1, int x2, int y2, int clipY1, int clipY2, IntConsumer a) {
        int a1 = Math.max(y1, clipY1), a2 = Math.min(y2, clipY2);
        if (a2 > a1) hits.add(new Hit(x1, a1, x2, a2, a));
    }
    private void hit(int x1, int y1, int x2, int y2, IntConsumer a) { hit(x1, y1, x2, y2, 0, height, a); }
    private static void round(GuiGraphics g, int x1, int y1, int x2, int y2, int c) {
        g.fill(x1 + 1, y1, x2 - 1, y2, c);
        g.fill(x1, y1 + 1, x2, y2 - 1, c);
    }
    private static int blend(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int gr = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (gr << 8) | bl;
    }
    private static void sw(GuiGraphics g, int x, int y, float t) {
        round(g, x, y, x + 22, y + 12, blend(0xFF3A3A4C, GREEN, t));
        int kx = x + 2 + (int) (10 * t);
        g.fill(kx, y + 2, kx + 8, y + 10, 0xFFFFFFFF);
    }
    private static boolean isNum(String s) {
        try { Double.parseDouble(s.trim()); return true; } catch (NumberFormatException e) { return false; }
    }
    private static String fmt(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) Math.rint(v));
        String s = String.format(Locale.ROOT, "%.2f", v);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    private static double step(String s, double v) {
        if (s.contains(".")) return Math.abs(v) < 2 ? 0.05 : 0.5;
        double a = Math.abs(v);
        return a >= 1000 ? 100 : a >= 200 ? 50 : a >= 50 ? 10 : a >= 10 ? 2 : 1;
    }

    @Override protected void init() {
        int w = Math.min(width - 12, 600), x0 = (width - w) / 2, y0 = (height - Math.min(height - 12, 360)) / 2;
        search = new EditBox(font, x0 + w - 176, y0 + 4, 168, 16, Component.literal("Suche"));
        search.setHint(Component.literal("Suche..."));
        search.setMaxLength(32);
        search.setValue(query);
        search.setResponder(v -> { query = v; scroll = 0; });
        addRenderableWidget(search);
    }

    // ---------- main render ----------
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float dt) {
        mx = mouseX; my = mouseY; hits.clear();
        accent = ClientSettings.accent();
        accentL = blend(accent, 0xFFFFFFFF, 0.4f);
        bg = (ClientSettings.opacity() << 24) | 0x101018;
        boolean searching = !query.isBlank();

        g.fill(0, 0, width, height, 0x88000000);
        int w = Math.min(width - 12, 600), h = Math.min(height - 12, 360);
        int x0 = (width - w) / 2, y0 = (height - h) / 2;
        round(g, x0 - 1, y0 - 1, x0 + w + 1, y0 + h + 1, accent);
        round(g, x0, y0, x0 + w, y0 + h, bg);
        g.fillGradient(x0 + 1, y0, x0 + w - 1, y0 + 24, blend(accent, 0xFF000000, 0.55f), 0xFF14141E);
        g.drawString(font, "AXIOM", x0 + 10, y0 + 8, 0xFFFFFFFF);
        g.drawString(font, "ESC = schliessen", x0 + 52, y0 + 8, 0xFF9A9AB0);

        int sw = 92;
        g.fill(x0 + 1, y0 + 24, x0 + sw, y0 + h - 1, SIDE);
        for (int i = 0; i < TABS.length; i++) {
            String t = TABS[i];
            int ty = y0 + 32 + i * 22;
            boolean sel = t.equals(tab) && !searching, hov = hover(x0 + 4, ty, x0 + sw - 4, ty + 18);
            if (sel) round(g, x0 + 4, ty, x0 + sw - 4, ty + 18, blend(accent, 0xFF101018, 0.72f));
            else if (hov) round(g, x0 + 4, ty, x0 + sw - 4, ty + 18, 0xFF1B1B2A);
            if (sel) g.fill(x0 + 4, ty + 3, x0 + 6, ty + 15, accent);
            g.drawString(font, t, x0 + 12, ty + 5, sel ? 0xFFFFFFFF : 0xFFA0A0B8);
            hit(x0 + 4, ty, x0 + sw - 4, ty + 18, b -> { tab = t; scroll = 0; bscroll = 0; query = ""; if (search != null) search.setValue(""); });
        }
        int cx = x0 + sw + 8, cy = y0 + 32, x1 = x0 + w - 8, ch = h - 40;
        if (tab.equals("Bases") && !searching) renderBases(g, cx, cy, x1, ch);
        else renderModules(g, cx, cy, x1, ch, searching);
        super.render(g, mouseX, mouseY, dt);
    }

    // ---------- module tabs ----------
    private void renderModules(GuiGraphics g, int cx, int cy, int x1, int ch, boolean searching) {
        int setW = (x1 - cx) > 420 ? 200 : 150;
        int modsW = x1 - cx - setW - 8;
        setX = cx + modsW + 8;
        List<Module> list = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Module m : AxiomClient.MODULES) {
            if (searching ? m.name.toLowerCase().contains(q) : m.category.equals(tab)) list.add(m);
        }
        int cardH = 28, gap = 6, cardW = (modsW - gap) / 2;
        int rows = (list.size() + 1) / 2, content = rows * (cardH + gap);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, content - ch)));
        if (list.isEmpty()) g.drawString(font, "Keine Module gefunden", cx + 4, cy + 6, 0xFF8888A0);

        g.enableScissor(cx, cy, cx + modsW, cy + ch);
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int x = cx + (i % 2) * (cardW + gap), y = cy + (i / 2) * (cardH + gap) - (int) scroll;
            if (y + cardH < cy || y > cy + ch) continue;
            boolean hov = hover(x, Math.max(y, cy), x + cardW, Math.min(y + cardH, cy + ch));
            boolean sel = m == selected;
            round(g, x, y, x + cardW, y + cardH, sel ? blend(accent, 0xFF101018, 0.72f) : (hov ? 0xFF22222F : CARD));
            if (m.enabled) g.fill(x, y + 4, x + 2, y + cardH - 4, GREEN);
            g.drawString(font, m.name, x + 8, y + (cardH - 8) / 2, m.enabled ? 0xFFFFFFFF : 0xFFB0B0C4);
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.3f;
            sw(g, x + cardW - 28, y + (cardH - 12) / 2, m.anim);
            if (!m.settings.isEmpty()) g.drawString(font, "cfg", x + cardW - 52, y + (cardH - 8) / 2, sel ? accentL : 0xFF6E6E88);
            hit(x, y, x + cardW, y + cardH, cy, cy + ch, b -> {
                if (b == 1) { selected = m; sscroll = 0; } else m.toggle();
            });
            hit(x + cardW - 56, y, x + cardW - 32, y + cardH, cy, cy + ch, b -> { selected = m; sscroll = 0; });
        }
        g.disableScissor();
        renderSettings(g, setX, cy, x1, ch);
    }

    private void renderSettings(GuiGraphics g, int sx, int cy, int x1, int ch) {
        round(g, sx, cy, x1, cy + ch, PANEL);
        if (selected == null) {
            g.drawString(font, "Klick auf \"cfg\" oder", sx + 8, cy + 10, 0xFF8888A0);
            g.drawString(font, "Rechtsklick auf ein Modul", sx + 8, cy + 22, 0xFF8888A0);
            g.drawString(font, "= Einstellungen", sx + 8, cy + 34, 0xFF8888A0);
            return;
        }
        Module m = selected;
        g.drawString(font, m.name, sx + 8, cy + 8, accentL);
        sw(g, x1 - 30, cy + 6, m.anim);
        hit(x1 - 32, cy + 4, x1 - 6, cy + 20, b -> m.toggle());

        int ty = cy + 24;
        String desc = Descs.of(m.name);
        if (!desc.isEmpty()) {
            List<FormattedCharSequence> lines = font.split(Component.literal(desc), x1 - sx - 16);
            for (FormattedCharSequence l : lines) { g.drawString(font, l, sx + 8, ty, 0xFF8888A0); ty += 10; }
            ty += 4;
        }
        int top = ty, view = cy + ch - top;
        if (m.settings.isEmpty()) { g.drawString(font, "Keine Einstellungen", sx + 8, top + 2, 0xFF8888A0); return; }
        int content = m.settings.size() * 24;
        sscroll = Math.max(0, Math.min(sscroll, Math.max(0, content - view)));

        g.enableScissor(sx, top, x1, cy + ch);
        int i = 0;
        for (Map.Entry<String, String> e : m.settings.entrySet()) {
            int ry = top + i * 24 - (int) sscroll;
            i++;
            if (ry + 22 < top || ry > cy + ch) continue;
            String k = e.getKey(), v = e.getValue();
            round(g, sx + 4, ry, x1 - 4, ry + 20, 0xFF1B1B26);
            g.drawString(font, k, sx + 10, ry + 6, 0xFFC8C8DC);
            boolean isBool = BOOLS.contains(k) && (v.trim().equals("0") || v.trim().equals("1"));
            if (isBool) {
                boolean on = !v.trim().equals("0");
                sw(g, x1 - 32, ry + 4, on ? 1f : 0f);
                hit(x1 - 34, ry, x1 - 4, ry + 20, top, cy + ch, b -> { m.settings.put(k, on ? "0" : "1"); ConfigManager.save(); });
            } else if (isNum(v)) {
                double d = Double.parseDouble(v.trim()), st = step(v.trim(), d);
                int bx = x1 - 100;
                round(g, bx, ry + 3, bx + 14, ry + 17, 0xFF2A2A3C);
                round(g, x1 - 18, ry + 3, x1 - 4, ry + 17, 0xFF2A2A3C);
                g.drawString(font, "-", bx + 5, ry + 6, 0xFFFFFFFF);
                g.drawString(font, "+", x1 - 14, ry + 6, 0xFFFFFFFF);
                String s = fmt(d);
                g.drawString(font, s, (bx + 14 + x1 - 18) / 2 - font.width(s) / 2, ry + 6, 0xFFFFFFFF);
                boolean negOk = k.equals("yMin") || k.equals("fovOffset");
                hit(bx, ry, bx + 16, ry + 20, top, cy + ch, b -> { double nv = d - st; m.settings.put(k, fmt(negOk ? nv : Math.max(0, nv))); ConfigManager.save(); });
                hit(x1 - 20, ry, x1 - 2, ry + 20, top, cy + ch, b -> { m.settings.put(k, fmt(d + st)); ConfigManager.save(); });
            } else {
                String s = v.length() > 14 ? v.substring(0, 13) + "..." : (v.isEmpty() ? "(leer)" : v);
                g.drawString(font, s, x1 - 10 - font.width(s), ry + 6, accentL);
                hit(sx + 4, ry, x1 - 4, ry + 20, top, cy + ch, b -> Minecraft.getInstance().setScreen(new SettingsScreen(m, this)));
            }
        }
        g.disableScissor();
    }

    // ---------- bases tab ----------
    private void toggleBtn(GuiGraphics g, int x, int y, int w, String label, Module m) {
        boolean on = m != null && m.enabled, hov = hover(x, y, x + w, y + 16);
        round(g, x, y, x + w, y + 16, on ? 0xFF1F5C3A : (hov ? 0xFF2A2A3A : 0xFF22222F));
        g.drawString(font, label + (on ? ": AN" : ": AUS"), x + 6, y + 4, 0xFFFFFFFF);
        hit(x, y, x + w, y + 16, b -> { if (m != null) m.toggle(); });
    }

    private void renderBases(GuiGraphics g, int cx, int cy, int x1, int ch) {
        setX = Integer.MAX_VALUE;
        round(g, cx, cy, x1, cy + ch, PANEL);
        g.drawString(font, "Gefundene Basen  (Klick = Koordinaten kopieren)", cx + 8, cy + 8, 0xFFFFFFFF);
        int bx = cx + 8, by = cy + 22;
        toggleBtn(g, bx, by, 96, "BaseScanner", AxiomClient.find("BaseScanner"));
        toggleBtn(g, bx + 102, by, 80, "BaseMap", AxiomClient.find("BaseMap"));
        boolean hov = hover(bx + 188, by, bx + 240, by + 16);
        round(g, bx + 188, by, bx + 240, by + 16, hov ? 0xFF5C1F1F : 0xFF3A1B1B);
        g.drawString(font, "Leeren", bx + 196, by + 4, 0xFFFFFFFF);
        hit(bx + 188, by, bx + 240, by + 16, b -> BaseData.clear());

        int top = cy + 44, view = ch - 48;
        List<BaseData.Info> list = BaseData.flagged();
        if (list.isEmpty()) {
            g.drawString(font, "Noch nichts gefunden. BaseScanner einschalten und herumlaufen.", cx + 8, top + 4, 0xFF8888A0);
            g.drawString(font, "Rot = Kisten/Spawner, Gelb = Tunnel/Schaechte, Orange = gebaute Bloecke.", cx + 8, top + 18, 0xFF8888A0);
            return;
        }
        int rowH = 24, content = list.size() * rowH;
        bscroll = Math.max(0, Math.min(bscroll, Math.max(0, content - view)));
        g.enableScissor(cx, top, x1, cy + ch);
        for (int i = 0; i < list.size(); i++) {
            BaseData.Info in = list.get(i);
            int y = top + i * rowH - (int) bscroll;
            if (y + rowH < top || y > cy + ch) continue;
            boolean hv = hover(cx + 4, Math.max(y, top), x1 - 4, Math.min(y + rowH - 2, cy + ch));
            round(g, cx + 4, y, x1 - 4, y + rowH - 2, hv ? 0xFF24243A : 0xFF1B1B26);
            int dot = (in.storage > 0 || in.spawners > 0) ? 0xFFFF4444 : ((in.tunnels > 0 || in.shafts > 0) ? 0xFFFFD84D : 0xFFFF9A3C);
            g.fill(cx + 8, y + 7, cx + 14, y + 13, dot);
            g.drawString(font, in.x + " " + in.y + " " + in.z + "   " + (int) in.dist + "m   score " + in.score(), cx + 20, y + 3, 0xFFFFFFFF);
            g.drawString(font, "kisten " + in.storage + "  spawner " + in.spawners + "  gebaut " + in.placed + "  tunnel " + in.tunnels + "  schacht " + in.shafts,
                cx + 20, y + 13, 0xFF8888A0);
            hit(cx + 4, y, x1 - 4, y + rowH - 2, top, cy + ch, b -> {
                String s = in.x + " " + in.y + " " + in.z;
                Minecraft.getInstance().keyboardHandler.setClipboard(s);
                if (Minecraft.getInstance().player != null)
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("[Axiom] Kopiert: " + s), false);
            });
        }
        g.disableScissor();
    }

    // ---------- input ----------
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x(), y = event.y();
        int b = event.button();
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (x >= h.x1() && x < h.x2() && y >= h.y1() && y < h.y2()) { h.act().accept(b); return true; }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override public boolean mouseScrolled(double x, double y, double sx, double sy) {
        double d = -sy * 16;
        if (tab.equals("Bases") && query.isBlank()) bscroll += d;
        else if (selected != null && x >= setX) sscroll += d;
        else scroll += d;
        return true;
    }

    @Override public boolean isPauseScreen() { return false; }
}
