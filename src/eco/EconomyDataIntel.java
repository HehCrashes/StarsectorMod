package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EconomyDataIntel extends BaseIntelPlugin {

    private String systemId;
    private String systemName;

    public EconomyDataIntel(String systemId) {
        this.systemId = systemId;
        StarSystemAPI system = Global.getSector().getStarSystem(systemId);
        this.systemName = system != null ? system.getName() : systemId;
    }

    @Override
    protected String getName() {
        int marketCount = getMarketsInSystem().size();
        return String.format("%s (%d\u4e2a\u5e02\u573a)", systemName, marketCount);
    }

    @Override
    public String getIcon() {
        FactionAPI pf = Global.getSector().getPlayerFaction();
        return pf != null ? pf.getCrest() : null;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        FactionAPI pf = Global.getSector().getPlayerFaction();
        return pf != null ? pf : super.getFactionForUIColors();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        if (tags != null) {
            tags.add("economy");
        }
        return tags;
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public String getImportantIcon() {
        return getIcon();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        List<MarketAPI> markets = getMarketsInSystem();
        if (!markets.isEmpty()) {
            return markets.get(0).getPrimaryEntity();
        }
        return null;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();

        List<MarketAPI> markets = getMarketsInSystem();
        int month = Global.getSector().getClock().getMonth();

        info.addPara("\u661f\u7cfb: %s (\u7b2c%s\u6708)", opad, h, systemName, "" + month);
        info.addPara("\u5171 %s \u4e2a\u5e02\u573a:", opad, h, "" + markets.size());

        for (MarketAPI market : markets) {
            PlanetTradeData data = SystemEconomyService.getTradeData(market);
            if (data == null) continue;
            int intraExp = data.intraSystemExports.size();
            int intraImp = data.intraSystemImports.size();
            int interExp = data.interSystemExports.size();
            int interImp = data.interSystemImports.size();
            info.addPara("  %s (%s) - \u661f\u5185\u51fa:%s \u661f\u5185\u8fdb:%s \u8de8\u661f\u51fa:%s \u8de8\u661f\u8fdb:%s",
                    3f, h,
                    market.getName(), market.getFaction().getDisplayName(),
                    "" + intraExp, "" + intraImp, "" + interExp, "" + interImp);
        }

        info.addButton("\u67e5\u770b\u5168\u5c4f\u7ecf\u6d4e\u8be6\u8868", "show_economy_dialog",
                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                Alignment.MID, CutStyle.ALL, 300f, 20f, opad);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if ("show_economy_dialog".equals(buttonId)) {
            EconomyDataDialogDelegate.showDialogForSystem(systemId);
        }
    }

    @Override
    public boolean doesButtonHaveConfirmDialog(Object buttonId) {
        return false;
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        float opad = 10f;
        float pad = 3f;
        Color h = Misc.getHighlightColor();
        Color nh = Misc.getNegativeHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();

        List<MarketAPI> markets = getMarketsInSystem();
        int totalLines = 2;
        for (MarketAPI m : markets) {
            PlanetTradeData data = SystemEconomyService.getTradeData(m);
            if (data == null) continue;
            totalLines += 2;
            totalLines += data.intraSystemExports.size();
            totalLines += data.intraSystemImports.size();
            totalLines += data.interSystemExports.size();
            totalLines += data.interSystemImports.size();
        }
        float contentHeight = Math.max(height, totalLines * 14f + 50f);

        TooltipMakerAPI content = panel.createUIElement(width, contentHeight, true);
        Color sectionColor = Misc.getBasePlayerColor();

        content.addSectionHeading(systemName, sectionColor, Misc.scaleColor(sectionColor, 0.5f), Alignment.MID, opad);

        for (MarketAPI market : markets) {
            PlanetTradeData data = SystemEconomyService.getTradeData(market);
            if (data == null) continue;

            String factionName = market.getFaction() != null ? market.getFaction().getDisplayName() : "?";
            content.addPara("%s [%s]", opad, h, market.getName(), factionName);

            if (!data.intraSystemExports.isEmpty()) {
                content.setParaFontDefault();
                content.addPara("\u661f\u7cfb\u51fa\u53e3 (\u5411\u540c\u661f\u7cfb\u5185\u5e02\u573a\u4f9b\u5e94):", pad);
                for (TradeRecord tr : data.intraSystemExports) {
                    content.addPara("  %s x%s -> %s (%s)", 1f, pos,
                            tr.getCommodityName(), "" + tr.quantity,
                            tr.destMarketName, tr.destFactionId);
                }
            }
            if (!data.intraSystemImports.isEmpty()) {
                content.addPara("\u661f\u7cfb\u8fdb\u53e3 (\u4ece\u540c\u661f\u7cfb\u5185\u5e02\u573a\u83b7\u53d6):", pad);
                for (TradeRecord tr : data.intraSystemImports) {
                    content.addPara("  %s x%s <- %s (%s)", 1f, pos,
                            tr.getCommodityName(), "" + tr.quantity,
                            tr.sourceMarketName, tr.sourceFactionId);
                }
            }
            if (!data.interSystemExports.isEmpty()) {
                content.addPara("\u8de8\u661f\u7cfb\u51fa\u53e3 (\u661f\u7cfb\u5185\u65e0\u6cd5\u6d88\u8017\u7684\u4ea7\u51fa):", pad);
                for (TradeRecord tr : data.interSystemExports) {
                    content.addPara("  %s x%s -> \u5916\u90e8\u5e02\u573a", 1f, pos,
                            tr.getCommodityName(), "" + tr.quantity);
                }
            }
            if (!data.interSystemImports.isEmpty()) {
                content.addPara("\u8de8\u661f\u7cfb\u8fdb\u53e3 (\u661f\u7cfb\u5185\u65e0\u6cd5\u6ee1\u8db3\u7684\u9700\u6c42):", pad);
                for (TradeRecord tr : data.interSystemImports) {
                    content.addPara("  %s x%s <- \u5916\u90e8\u5e02\u573a", 1f, nh,
                            tr.getCommodityName(), "" + tr.quantity);
                }
            }
        }

        panel.addUIElement(content).inTL(0f, 0f);
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    private List<MarketAPI> getMarketsInSystem() {
        List<MarketAPI> result = new ArrayList<>();
        StarSystemAPI system = Global.getSector().getStarSystem(systemId);
        if (system == null) return result;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
            if (!market.isInEconomy()) continue;
            if (SystemEconomyService.getTradeData(market) != null) {
                result.add(market);
            }
        }
        return result;
    }

    public static void ensureIntelsForAllSystems() {
        if (Global.getSector().getIntelManager() == null) return;

        List<String> activeSystems = new ArrayList<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!market.isInEconomy()) continue;
            StarSystemAPI system = market.getStarSystem();
            if (system == null) continue;
            String sid = system.getId();
            if (!activeSystems.contains(sid)) activeSystems.add(sid);
        }

        List<String> existingSystems = new ArrayList<>();
        for (Object raw : Global.getSector().getIntelManager().getIntel()) {
            if (raw instanceof EconomyDataIntel) {
                existingSystems.add(((EconomyDataIntel) raw).systemId);
            }
        }

        for (String sid : activeSystems) {
            if (!existingSystems.contains(sid)) {
                Global.getSector().getIntelManager().addIntel(new EconomyDataIntel(sid), false, null);
            }
        }
    }
}
