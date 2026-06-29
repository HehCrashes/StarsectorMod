package eco;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.ui.U;
import com.fs.starfarer.ui.oo0O;
import com.fs.starfarer.ui.voidsuper;
import eco.data.PlanetMarket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mixin(value = com.fs.starfarer.campaign.ui.marketinfo.CommodityPanel.class)
public abstract class CommodityPanelMixin extends voidsuper implements U, oo0O.o{
    @Shadow protected MarketAPI market;
    @Shadow protected Color color;

    public CommodityPanelMixin(String s) {
        super(s);
    }

    /**
     * @author a
     * @reason b
     */
    @Overwrite
    public void sizeChanged(float width, float height) {
        this.clearChildren();

        if (this.title != null) {
            if (!this.getChildrenNonCopy().contains(this.title))
                this.add(this.title).inTL(0f, 0f);
            this.title.setSize(width, this.title.getHeight());
            this.title.getPosition().recompute();
        }

        // ★ 不引用任何 mod 类，只用 MarketAPI + java.util.Map
        Object raw = this.market.getMemoryWithoutUpdate().get("$corecracking_econ_data_display");
        if (!(raw instanceof Map)) return;

        Map<String, String> data = (Map<String, String>) raw;
        if (data.isEmpty()) return;

        float pad = 10f, gap = 3f;
        com.fs.starfarer.ui.c prev = null;
        for (String text : data.values()) {
            com.fs.starfarer.ui.d label = com.fs.starfarer.ui.d.create(text, this.color);
            label.setSize(width - pad * 2, label.getLineHeight());

            if (prev == null)
                this.add(label).inTL(pad, this.titleHeight + pad);
            else
                this.add(label).belowLeft(prev, gap);
            prev = label;
        }
    }
}
