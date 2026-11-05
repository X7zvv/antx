package axiom.modules;

import axiom.Module;
import axiom.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoTool extends Module {
    public AutoTool() { super("AutoTool"); }
    @Override public void onTick() {
        if (!mc.options.keyAttack.isDown() || mc.screen != null) return;
        if (!(mc.hitResult instanceof BlockHitResult b) || b.getType() == HitResult.Type.MISS) return;
        BlockState st = mc.level.getBlockState(b.getBlockPos());
        int best = -1; float bs = 1f;
        for (int i = 0; i < 9; i++) {
            float sp = mc.player.getInventory().getItem(i).getDestroySpeed(st);
            if (sp > bs) { bs = sp; best = i; }
        }
        Util.select(best);
    }
}
