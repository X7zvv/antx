package axiom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen extends Screen {
    private final Module m;
    private final Screen parent;

    public SettingsScreen(Module m, Screen parent) {
        super(Component.literal(m.name));
        this.m = m;
        this.parent = parent;
    }

    @Override protected void init() {
        int y = 60;
        for (String k : m.settings.keySet()) {
            EditBox eb = new EditBox(font, width / 2 - 10, y, 170, 18, Component.literal(k));
            eb.setMaxLength(128);
            eb.setValue(m.settings.get(k));
            eb.setResponder(v -> m.settings.put(k, v));
            addRenderableWidget(eb);
            y += 24;
        }
        addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> Minecraft.getInstance().setScreen(parent))
            .bounds(width / 2 - 50, y + 12, 100, 20).build());
    }

    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xAA000000);
        g.drawCenteredString(font, m.name + " - Einstellungen", width / 2, 30, 0xFFFFFFFF);
        int y = 60;
        for (String k : m.settings.keySet()) { g.drawString(font, k, width / 2 - 160, y + 5, 0xFFCCCCCC); y += 24; }
        super.render(g, mx, my, delta);
    }

    @Override public void removed() { ConfigManager.save(); }
    @Override public boolean isPauseScreen() { return false; }
}
