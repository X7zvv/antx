package axiom.modules;

import axiom.Module;

public class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint");
        settings.put("omni", "0");        // 1 = sprint in every direction
        settings.put("minFood", "6");
    }
    @Override public void onTick() {
        var p = mc.player;
        var o = mc.options;
        boolean moving = o.keyUp.isDown() || (num("omni", 0) >= 1 && (o.keyDown.isDown() || o.keyLeft.isDown() || o.keyRight.isDown()));
        if (moving && !p.isShiftKeyDown() && !p.horizontalCollision && !p.isUsingItem() && p.getFoodData().getFoodLevel() > num("minFood", 6))
            p.setSprinting(true);
    }
}
