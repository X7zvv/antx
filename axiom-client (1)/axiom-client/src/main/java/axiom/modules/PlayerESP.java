package axiom.modules;

import axiom.Module;

public class PlayerESP extends Module {
    public static volatile boolean active;
    public PlayerESP() { super("PlayerESP"); }
    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }
}
