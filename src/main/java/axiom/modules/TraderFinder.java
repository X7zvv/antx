package axiom.modules;

import axiom.Module;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class TraderFinder extends Module {
    private int cd;
    public TraderFinder() { super("TraderFinder"); }
    @Override public void onTick() {
        if (cd > 0) { cd--; return; }
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e.getType() == EntityType.WANDERING_TRADER || e.getType() == EntityType.TRADER_LLAMA) {
                mc.player.displayClientMessage(Component.literal("[Axiom] " + e.getType().getDescription().getString() + " at "
                    + (int) e.getX() + " " + (int) e.getY() + " " + (int) e.getZ()), false);
                mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0f, 1.0f);
                cd = 400;
                return;
            }
        }
    }
}
