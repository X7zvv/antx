package axiom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGui extends Screen {
    public ClickGui() { super(Component.literal("Axiom")); }

    private static Component label(Module m) {
        return Component.literal((m.enabled ? "[ON] " : "") + m.name);
    }

    @Override protected void init() {
        int cols = 3, w = 120, h = 20, gap = 4;
        int x0 = (width - (cols * w + (cols - 1) * gap)) / 2, y0 = 40;
        for (int i = 0; i < AxiomClient.MODULES.size(); i++) {
            Module m = AxiomClient.MODULES.get(i);
            int x = x0 + (i % cols) * (w + gap), y = y0 + (i / cols) * (h + gap);
            addRenderableWidget(Button.builder(label(m), b -> { m.toggle(); b.setMessage(label(m)); })
                .bounds(x, y, w, h).build());
        }
    }

    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        super.render(g, mx, my, delta);
        g.drawCenteredString(font, "Axiom Client  |  click to toggle  |  /axiom set <module> <key> <value> for settings", width / 2, 20, 0xFFFFFFFF);
    }

    @Override public boolean isPauseScreen() { return false; }
}
