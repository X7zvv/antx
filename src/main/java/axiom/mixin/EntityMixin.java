package axiom.mixin;

import axiom.modules.PlayerESP;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void axiom$esp(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerESP.active && (Object) this instanceof Player p && p != Minecraft.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
