package axiom.modules;

import axiom.Module;

public class AutoSprint extends Module {
    public AutoSprint() { super("AutoSprint"); }
    @Override public void onTick() {
        var p = mc.player;
        if (mc.options.keyUp.isDown() && !p.isShiftKeyDown() && !p.horizontalCollision
                && !p.isUsingItem() && p.getFoodData().getFoodLevel() > 6)
            p.setSprinting(true);
    }
}
