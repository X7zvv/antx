package axiom.modules;

import axiom.Module;
import axiom.Schematic;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Places blocks from a .litematic (files go in .minecraft/schematics). Blocks must be in the hotbar. */
public class AutoBuilder extends Module {
    private final List<Schematic.Entry> pending = new ArrayList<>();
    private BlockPos anchor;
    private int timer, idle;
    private String missing = "";
    private boolean paused;
    public boolean togglePause() { paused = !paused; return paused; }

    public AutoBuilder() {
        super("AutoBuilder");
        settings.put("delay", "2");      // ticks between placements
        settings.put("reach", "4.5");
        settings.put("rotate", "0");     // 1 = look at the block before placing
    }

    public int remaining() { return pending.size(); }

    public String start(Path file) {
        if (mc.player == null) return "not in a world";
        try {
            Schematic s = Schematic.load(file);
            if (s.blocks.isEmpty()) return "schematic is empty or unreadable";
            pending.clear();
            pending.addAll(s.blocks);
            anchor = mc.player.blockPosition().relative(mc.player.getDirection(), 2);
            timer = 0; idle = 0; missing = ""; paused = false;
            setEnabled(true);
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

    @Override public void onDisable() { pending.clear(); }

    private static String key(BlockState s) { return BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString(); }

    @Override public void onTick() {
        if (mc.gameMode == null || anchor == null || paused) return;
        if (pending.isEmpty()) {
            mc.player.displayClientMessage(Component.literal("[Axiom] Build finished."), false);
            setEnabled(false);
            return;
        }
        if (++timer < num("delay", 2)) return;
        timer = 0;

        double reach2 = num("reach", 4.5) * num("reach", 4.5);
        Vec3 eye = mc.player.getEyePosition();
        int scanned = 0;
        for (int i = 0; i < pending.size() && scanned < 4000; i++, scanned++) {
            Schematic.Entry e = pending.get(i);
            BlockPos pos = anchor.offset(e.x(), e.y(), e.z());
            BlockState cur = mc.level.getBlockState(pos);
            if (key(cur).equals(e.block())) { pending.remove(i--); continue; }
            if (!cur.isAir() && !cur.canBeReplaced()) continue;
            if (eye.distanceToSqr(Vec3.atCenterOf(pos)) > reach2) continue;
            if (place(pos, e.block())) { idle = 0; pending.remove(i); return; }
        }
        if (++idle % 100 == 0) {
            String why = missing.isEmpty() ? "remaining blocks out of reach, walk closer" : "put " + missing + " in your hotbar";
            mc.player.displayClientMessage(Component.literal("[Axiom] Waiting: " + why + " (" + pending.size() + " left)"), true);
        }
    }

    private boolean place(BlockPos pos, String block) {
        var inv = mc.player.getInventory();
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            var st = inv.getItem(i);
            if (st.getItem() instanceof BlockItem bi && BuiltInRegistries.BLOCK.getKey(bi.getBlock()).toString().equals(block)) { slot = i; break; }
        }
        if (slot < 0) { missing = block; return false; }
        missing = "";
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            BlockState ns = mc.level.getBlockState(n);
            if (ns.isAir() || !ns.getFluidState().isEmpty() || ns.canBeReplaced()) continue;
            Direction face = d.getOpposite();
            Vec3 hitPos = Vec3.atCenterOf(n).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
            if (inv.getSelectedSlot() != slot) {
                inv.setSelectedSlot(slot);
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
            }
            if (num("rotate", 0) >= 1) {
                Vec3 eye = mc.player.getEyePosition();
                double dx = hitPos.x - eye.x, dy = hitPos.y - eye.y, dz = hitPos.z - eye.z;
                mc.player.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
                mc.player.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
            }
            var r = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(hitPos, face, n, false));
            if (r.consumesAction()) { mc.player.swing(InteractionHand.MAIN_HAND); return true; }
        }
        return false;
    }
}
