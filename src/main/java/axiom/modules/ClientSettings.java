package axiom.modules;

import axiom.AxiomClient;
import axiom.Module;

/** Look of the menu. Always "on" in spirit; only its settings matter. */
public class ClientSettings extends Module {
    public static final int[] THEMES = { 0xFF7C5CFF, 0xFF4DA3FF, 0xFF3DDC84, 0xFFFF5C5C, 0xFFFF6EC7, 0xFFFFA03C };
    public ClientSettings() {
        super("ClientSettings");
        settings.put("theme", "0");       // 0 purple, 1 blue, 2 green, 3 red, 4 pink, 5 orange
        settings.put("opacity", "242");   // menu background alpha 60-255
    }
    public static int accent() {
        if (AxiomClient.find("ClientSettings") instanceof ClientSettings c) {
            int t = (int) c.num("theme", 0);
            return THEMES[Math.floorMod(t, THEMES.length)];
        }
        return THEMES[0];
    }
    public static int opacity() {
        if (AxiomClient.find("ClientSettings") instanceof ClientSettings c) return Math.max(60, Math.min(255, (int) c.num("opacity", 242)));
        return 242;
    }
}
