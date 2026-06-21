package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.*;

public class EconomyDataIntel extends BaseIntelPlugin {

    @Override
    protected String getName() {
        int total = countMarketsWithData();
        return String.format("星系经济数据 (%d个市场)", total);
    }

    @Override
    public String getIcon() {
        return Global.getSector().getPlayerFaction().getCrest();
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getPlayerFaction();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("economy");
        return tags;
    }

    @Override
    public String getSortString() {
        return "星系经济数据";
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
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
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();

        Map<String, List<MarketAPI>> systemMap = groupMarketsBySystem();
        int systemCount = systemMap.size();
        int marketCount = 0;
        for (List<MarketAPI> list : systemMap.values()) marketCount += list.size();
        int month = Global.getSector().getClock().getMonth();

        info.addPara("当前月份: %s  |  共计 %s 个星系, %s 个市场参与经济计算",
                opad, h, "" + month, "" + systemCount, "" + marketCount);

        info.addPara("", opad);

        for (Map.Entry<String, List<MarketAPI>> entry : systemMap.entrySet()) {
            String systemName = getSystemName(entry.getKey());
            List<MarketAPI> markets = entry.getValue();
            info.addPara("%s: %s个市场", opad, h, systemName, "" + markets.size());

            for (MarketAPI market : markets) {
                PlanetTradeData data = SystemEconomyService.getTradeData(market);
                if (data == null) continue;
                int intraExp = data.intraSystemExports.size();
                int intraImp = data.intraSystemImports.size();
                int interExp = data.interSystemExports.size();
                int interImp = data.interSystemImports.size();
                info.addPara("  %s (%s) - 星内出:%s 星内进:%s 跨星出:%s 跨星进:%s",
                        3f, h,
                        market.getName(), market.getFaction().getDisplayName(),
                        "" + intraExp, "" + intraImp, "" + interExp, "" + interImp);
            }
        }
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        float opad = 10f;
        float pad = 3f;
        Color h = Misc.getHighlightColor();
        Color nh = Misc.getNegativeHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();

        Map<String, List<MarketAPI>> systemMap = groupMarketsBySystem();
        int totalLines = 2;
        for (Map.Entry<String, List<MarketAPI>> entry : systemMap.entrySet()) {
            totalLines += 2;
            for (MarketAPI m : entry.getValue()) {
                PlanetTradeData data = SystemEconomyService.getTradeData(m);
                if (data == null) continue;
                totalLines += 2;
                totalLines += data.intraSystemExports.size();
                totalLines += data.intraSystemImports.size();
                totalLines += data.interSystemExports.size();
                totalLines += data.interSystemImports.size();
            }
        }
        float contentHeight = Math.max(height, totalLines * 14f + 50f);

        TooltipMakerAPI content = panel.createUIElement(width, contentHeight, true);
        Color sectionColor = Misc.getBasePlayerColor();

        for (Map.Entry<String, List<MarketAPI>> entry : systemMap.entrySet()) {
            String systemName = getSystemName(entry.getKey());
            List<MarketAPI> markets = entry.getValue();

            content.addSectionHeading(systemName, sectionColor, Misc.scaleColor(sectionColor, 0.5f), Alignment.MID, opad);

            for (MarketAPI market : markets) {
                PlanetTradeData data = SystemEconomyService.getTradeData(market);
                if (data == null) continue;

                String factionName = market.getFaction() != null ? market.getFaction().getDisplayName() : "?";
                content.addPara("%s [%s]", opad, h, market.getName(), factionName);

                if (!data.intraSystemExports.isEmpty()) {
                    content.setParaFontDefault();
                    content.addPara("星系出口 (向同星系内市场供应):", pad);
                    for (TradeRecord tr : data.intraSystemExports) {
                        content.addPara("  %s x%s -> %s (%s)", 1f, pos,
                                tr.getCommodityName(), "" + tr.quantity,
                                tr.destMarketName, tr.destFactionId);
                    }
                }
                if (!data.intraSystemImports.isEmpty()) {
                    content.addPara("星系进口 (从同星系内市场获取):", pad);
                    for (TradeRecord tr : data.intraSystemImports) {
                        content.addPara("  %s x%s <- %s (%s)", 1f, pos,
                                tr.getCommodityName(), "" + tr.quantity,
                                tr.sourceMarketName, tr.sourceFactionId);
                    }
                }
                if (!data.interSystemExports.isEmpty()) {
                    content.addPara("跨星系出口 (星系内无法消耗的产出):", pad);
                    for (TradeRecord tr : data.interSystemExports) {
                        content.addPara("  %s x%s -> 外部市场", 1f, pos,
                                tr.getCommodityName(), "" + tr.quantity);
                    }
                }
                if (!data.interSystemImports.isEmpty()) {
                    content.addPara("跨星系进口 (星系内无法满足的需求):", pad);
                    for (TradeRecord tr : data.interSystemImports) {
                        content.addPara("  %s x%s <- 外部市场", 1f, nh,
                                tr.getCommodityName(), "" + tr.quantity);
                    }
                }
            }
        }

        panel.addUIElement(content).inTL(0f, 0f);
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    private int countMarketsWithData() {
        int count = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (SystemEconomyService.getTradeData(market) != null) count++;
        }
        return count;
    }

    private Map<String, List<MarketAPI>> groupMarketsBySystem() {
        Map<String, List<MarketAPI>> result = new LinkedHashMap<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!market.isInEconomy()) continue;
            StarSystemAPI system = market.getStarSystem();
            if (system == null) continue;
            if (SystemEconomyService.getTradeData(market) == null) continue;
            result.computeIfAbsent(system.getId(), k -> new ArrayList<>()).add(market);
        }
        return result;
    }

    private String getSystemName(String systemId) {
        StarSystemAPI system = Global.getSector().getStarSystem(systemId);
        return system != null ? system.getName() : systemId;
    }

    public static void ensureExists() {
        if (Global.getSector().getIntelManager() == null) return;
        if (Global.getSector().getIntelManager().hasIntelOfClass(EconomyDataIntel.class)) return;
        Global.getSector().getIntelManager().addIntel(new EconomyDataIntel(), false, null);
    }
}
