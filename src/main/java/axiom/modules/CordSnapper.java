package axiom.modules;

import axiom.Module;
import net.minecraft.network.chat.Component;

/** Enable it once: copies your coordinates to the clipboard, then switches itself off. */
public class CordSnapper extends Module {
    public CordSnapper() { super("CordSnapper"); }
    @Override public void onTick() {
        String s = mc.player.blockPosition().getX() + " " + mc.player.blockPosition().getY() + " " + mc.player.blockPosition().getZ();
        mc.keyboardHandler.setClipboard(s);
        mc.player.displayClientMessage(Component.literal("[Axiom] Copied: " + s), false);
        setEnabled(false);
    }
}
