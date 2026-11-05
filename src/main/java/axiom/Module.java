package axiom;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();
    public final String name;
    public boolean enabled;
    public final Map<String, String> settings = new LinkedHashMap<>();

    protected Module(String name) { this.name = name; }

    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }
    public void toggle() { setEnabled(!enabled); }
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    protected double num(String k, double d) {
        try { return Double.parseDouble(settings.getOrDefault(k, String.valueOf(d)).trim()); }
        catch (NumberFormatException e) { return d; }
    }
    protected String str(String k, String d) { return settings.getOrDefault(k, d); }
}
