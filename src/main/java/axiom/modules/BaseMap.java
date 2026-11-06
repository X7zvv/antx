package axiom.modules;

import axiom.AxiomClient;
import axiom.BaseData;
import axiom.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.ChunkPos;

/** HUD top-down chunk map: red = storage/spawners, yellow = tunnels/shafts, orange = player-made blocks, grey = scanned. */
public class BaseMap extends Module {
    public BaseMap() {
        super("BaseMap");
        settings.put("radius", "8");
        settings.put("cell", "5");
        settings.put("showScanned", "1");
        settings.put("x", "6");
        settings.put("y", "40");
    }

    public static void render(GuiGraphics g) {
        if (AxiomClient.find("BaseMap") instanceof BaseMap m && m.enabled && mc.player != null && !mc.options.hideGui) m.draw(g);
    }

    private void draw(GuiGraphics g) {
        int r = Math.max(2, (int) num("radius", 8)), cell = Math.max(2, (int) num("cell", 5));
        int size = (2 * r + 1) * cell, x0 = (int) num("x", 6), y0 = (int) num("y", 40);
        g.fill(x0 - 1, y0 - 1, x0 + size + 1, y0 + size + 1, 0xCC000000);
        int pcx = mc.player.blockPosition().getX() >> 4, pcz = mc.player.blockPosition().getZ() >> 4;
        boolean showScanned = num("showScanned", 1) >= 1;
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            long key = ChunkPos.asLong(pcx + dx, pcz + dz);
            int color = 0;
            BaseData.Info in = BaseData.CHUNKS.get(key);
            if (in != null && in.score() >= BaseData.minScore) {
                color = (in.storage > 0 || in.spawners > 0) ? 0xFFFF4444 : ((in.tunnels > 0 || in.shafts > 0) ? 0xFFFFD84D : 0xFFFF9A3C);
            } else if (showScanned && BaseData.SCANNED.contains(key)) color = 0x44FFFFFF;
            int px = x0 + (dx + r) * cell, py = y0 + (dz + r) * cell;
            if (color != 0) g.fill(px, py, px + cell - 1, py + cell - 1, color);
        }
        int c = r * cell + cell / 2;
        g.fill(x0 + c - 1, y0 + c - 1, x0 + c + 2, y0 + c + 2, 0xFFFFFFFF);
        var list = BaseData.flagged();
        String line = list.isEmpty() ? "no base found" : "nearest: " + list.get(0).x + " " + list.get(0).z + "  (" + (int) list.get(0).dist + "m)";
        g.drawString(mc.font, line, x0, y0 + size + 4, 0xFFFFFFFF);
    }
}
