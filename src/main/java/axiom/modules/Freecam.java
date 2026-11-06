package axiom.modules;

import axiom.AxiomClient;
import axiom.Module;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

/**
 * Detached camera. A hidden client-side entity becomes the camera, your real player is frozen (its input is replaced by an
 * empty one). Fly with WASD, Space = up, Shift = down, Ctrl = faster. Your body stays where it is; turning the mouse also
 * turns your real player's head. Attack/use clicks are blocked while Freecam is on.
 */
public class Freecam extends Module {
    private ArmorStand cam;
    private ClientInput oldInput;
    private Vec3 pos = Vec3.ZERO;

    public Freecam() {
        super("Freecam");
        settings.put("speed", "1.0");
        settings.put("sprintMult", "2.5");
    }

    private void place() { cam.setPos(pos.x, pos.y - cam.getEyeHeight(), pos.z); }

    @Override public void onEnable() {
        if (mc.player == null || mc.level == null) { enabled = false; return; }
        cam = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
        cam.setNoGravity(true);
        cam.setInvisible(true);
        pos = mc.player.getEyePosition();
        place();
        cam.xo = cam.getX(); cam.yo = cam.getY(); cam.zo = cam.getZ();
        frame();
        oldInput = mc.player.input;
        mc.player.input = new ClientInput();
        mc.setCameraEntity(cam);
    }

    @Override public void onDisable() {
        if (mc.player != null) {
            if (oldInput != null) mc.player.input = oldInput;
            mc.setCameraEntity(mc.player);
        }
        cam = null;
        oldInput = null;
    }

    /** Runs at the start of every client tick: swallow attack/use so nothing is done from the camera's point of view. */
    public void preTick() {
        var o = mc.options;
        while (o.keyAttack.consumeClick()) { }
        while (o.keyUse.consumeClick()) { }
        o.keyAttack.setDown(false);
        o.keyUse.setDown(false);
    }

    /** Runs every rendered frame: camera looks where the player's mouse looks, without interpolation lag. */
    public static void frame() {
        if (AxiomClient.find("Freecam") instanceof Freecam f && f.enabled && f.cam != null && mc.player != null) f.syncRotation();
    }
    private void syncRotation() {
        cam.setYRot(mc.player.getYRot());
        cam.setXRot(mc.player.getXRot());
        cam.yRotO = cam.getYRot();
        cam.xRotO = cam.getXRot();
    }

    @Override public void onTick() {
        if (cam == null || cam.level() != mc.level) { setEnabled(false); return; }
        var o = mc.options;
        double yaw = mc.player.getYRot();
        Vec3 look = Vec3.directionFromRotation(mc.player.getXRot(), (float) yaw);
        double yr = Math.toRadians(yaw);
        Vec3 right = new Vec3(-Math.cos(yr), 0, -Math.sin(yr));
        Vec3 d = Vec3.ZERO;
        if (mc.screen == null) {
            if (o.keyUp.isDown()) d = d.add(look);
            if (o.keyDown.isDown()) d = d.subtract(look);
            if (o.keyRight.isDown()) d = d.add(right);
            if (o.keyLeft.isDown()) d = d.subtract(right);
            if (o.keyJump.isDown()) d = d.add(0, 1, 0);
            if (o.keyShift.isDown()) d = d.add(0, -1, 0);
        }
        if (d.lengthSqr() > 1e-6) {
            double sp = num("speed", 1.0) * (o.keySprint.isDown() ? num("sprintMult", 2.5) : 1.0);
            d = d.normalize().scale(sp);
        }
        cam.xo = cam.getX(); cam.yo = cam.getY(); cam.zo = cam.getZ();
        pos = pos.add(d);
        place();
        syncRotation();
    }
}
