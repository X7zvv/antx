package axiom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGui extends Screen {
    public static final String[] CATS = { "Combat", "Movement", "Render", "Donut", "Finder", "Auto", "Misc" };
    private static String current = "Combat";

    public ClickGui() { super(Component.literal("Axiom")); }

    private static Component label(Module m) {
        return m.enabled
            ? Component.literal(m.name).withStyle(ChatFormatting.GREEN)
            : Component.literal(m.name).withStyle(ChatFormatting.GRAY);
    }

    @Override protected void init() {
        int cw = 70, ch = 20, gap = 4;
        int tabsW = CATS.length * cw + (CATS.length - 1) * gap;
        int x0 = (width - tabsW) / 2;
        for (int i = 0; i < CATS.length; i++) {
            String c = CATS[i];
            Component lab = c.equals(current) ? Component.literal(c).withStyle(ChatFormatting.YELLOW) : Component.literal(c);
            addRenderableWidget(Button.builder(lab, b -> { current = c; rebuildWidgets(); })
                .bounds(x0 + i * (cw + gap), 34, cw, ch).build());
        }
        int mw = 112, gw = 20, cols = 3, pitch = mw + gw + gap * 2;
        int gx = (width - (cols * pitch - gap * 2)) / 2, y0 = 70, idx = 0;
        for (Module m : AxiomClient.MODULES) {
            if (!m.category.equals(current)) continue;
            int x = gx + (idx % cols) * pitch, y = y0 + (idx / cols) * (ch + gap);
            addRenderableWidget(Button.builder(label(m), b -> { m.toggle(); b.setMessage(label(m)); })
                .bounds(x, y, mw, ch).build());
            if (!m.settings.isEmpty())
                addRenderableWidget(Button.builder(Component.literal("..."), b -> Minecraft.getInstance().setScreen(new SettingsScreen(m, this)))
                    .bounds(x + mw + 2, y, gw, ch).build());
            idx++;
        }
    }

    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xAA000000);
        g.fill(width / 2 - 250, 26, width / 2 + 250, 28, 0xFF55FFFF);
        g.drawCenteredString(font, "AXIOM CLIENT  -  grün = an, \"...\" = Einstellungen, ESC = schließen", width / 2, 12, 0xFFFFFFFF);
        super.render(g, mx, my, delta);
    }

    @Override public boolean isPauseScreen() { return false; }
}
