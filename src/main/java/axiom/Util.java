package axiom;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class Util {
    private static final Minecraft mc = Minecraft.getInstance();
    public static final Set<String> FRIENDS = new HashSet<>();

    private Util() {}

    public static boolean isFriend(Player p) {
        return FRIENDS.contains(p.getName().getString().toLowerCase());
    }

    public static Player nearest(double range) {
        Player best = null;
        double bd = range;
        for (Player p : mc.level.players()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator() || isFriend(p)) continue;
            double d = mc.player.distanceTo(p);
            if (d <= bd) { bd = d; best = p; }
        }
        return best;
    }

    public static void lookAt(Vec3 t) {
        Vec3 e = mc.player.getEyePosition();
        double dx = t.x - e.x, dy = t.y - e.y, dz = t.z - e.z;
        mc.player.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
    }

    public static int findHotbar(Predicate<ItemStack> f) {
        for (int i = 0; i < 9; i++) if (f.test(mc.player.getInventory().getItem(i))) return i;
        return -1;
    }

    public static void select(int slot) {
        if (slot < 0 || slot > 8) return;
        var inv = mc.player.getInventory();
        if (inv.getSelectedSlot() != slot) {
            inv.setSelectedSlot(slot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    public static void releaseKeys() {
        var o = mc.options;
        o.keyUp.setDown(false); o.keyDown.setDown(false);
        o.keyLeft.setDown(false); o.keyRight.setDown(false);
        o.keyJump.setDown(false); o.keySprint.setDown(false);
    }

    public static BlockPos miningPos;

    /** Picks the best hotbar tool, looks at the block and keeps breaking it. Call every tick. */
    public static void mine(BlockPos p) {
        BlockState st = mc.level.getBlockState(p);
        int best = -1; float bestSpeed = 1f;
        for (int i = 0; i < 9; i++) {
            float sp = mc.player.getInventory().getItem(i).getDestroySpeed(st);
            if (sp > bestSpeed) { bestSpeed = sp; best = i; }
        }
        select(best);
        lookAt(Vec3.atCenterOf(p));
        Vec3 e = mc.player.getEyePosition();
        Direction face = Direction.getApproximateNearest(e.x - (p.getX() + 0.5), e.y - (p.getY() + 0.5), e.z - (p.getZ() + 0.5));
        if (!p.equals(miningPos)) { mc.gameMode.startDestroyBlock(p, face); miningPos = p; }
        else mc.gameMode.continueDestroyBlock(p, face);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}
