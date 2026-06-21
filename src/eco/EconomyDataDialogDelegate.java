package eco;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.*;

public class EconomyDataDialogDelegate implements CustomVisualDialogDelegate {

    private DialogCallbacks callbacks;

    @Override
    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.callbacks = callbacks;

        float pw = panel.getPosition().getWidth();
        float ph = panel.getPosition().getHeight();
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color nh = Misc.getNegativeHighlightColor();
        Color pos = Misc.getPositiveHighlightColor();
        Color sectionColor = Misc.getBasePlayerColor();
        Color bgColor = new Color(21, 65, 77);
        Color fgColor = new Color(143, 187, 214);

        TooltipMakerAPI title = panel.createUIElement(pw, 20f, false);
        int month = Global.getSector().getClock().getMonth();
        title.addSectionHeading("星系经济数据 (第" + month + "月)", fgColor, bgColor, Alignment.MID, 0f);
        panel.addUIElement(title).inTL(0f, 5f);

        Map<String, List<MarketAPI>> systemMap = groupMarketsBySystem();
        int totalLines = 3;
        for (Map.Entry<String, List<MarketAPI>> entry : systemMap.entrySet()) {
            totalLines += 2;
            for (MarketAPI m : entry.getValue()) {
                PlanetTradeData data = SystemEconomyService.getTradeData(m);
                if (data == null) continue;
                totalLines += 3;
                totalLines += data.intraSystemExports.size() + data.intraSystemImports.size();
                totalLines += data.interSystemExports.size() + data.interSystemImports.size();
            }
        }
        float contentH = Math.max(ph - 60f, totalLines * 14f + 40f);

        float imgAreaH = ph - 60f;
        CustomPanelAPI contentPanel = Global.getSettings().createCustom(pw, contentH, null);
        TooltipMakerAPI imgElement = panel.createUIElement(pw, imgAreaH, false);

        TooltipMakerAPI content = contentPanel.createUIElement(pw - 20f, contentH, true);
        content.setParaSmallInsignia();
        content.setParaFontDefault();

        for (Map.Entry<String, List<MarketAPI>> entry : systemMap.entrySet()) {
            String systemName = getSystemName(entry.getKey());
            List<MarketAPI> markets = entry.getValue();

            content.addSectionHeading(systemName, fgColor, bgColor, Alignment.MID, opad);

            for (MarketAPI market : markets) {
                PlanetTradeData data = SystemEconomyService.getTradeData(market);
                if (data == null) continue;

                String factionName = market.getFaction() != null ? market.getFaction().getDisplayName() : "?";
                content.addPara("%s [%s]", opad, h, market.getName(), factionName);

                if (!data.intraSystemExports.isEmpty()) {
                    content.addPara("  星系出口 (向同星系市场供应):", 3f);
                    for (TradeRecord tr : data.intraSystemExports) {
                        content.addPara("    %s x%s -> %s (%s)", 1f, pos,
                                tr.getCommodityName(), "" + tr.quantity,
                                tr.destMarketName, tr.destFactionId);
                    }
                }
                if (!data.intraSystemImports.isEmpty()) {
                    content.addPara("  星系进口 (从同星系市场获取):", 3f);
                    for (TradeRecord tr : data.intraSystemImports) {
                        content.addPara("    %s x%s <- %s (%s)", 1f, pos,
                                tr.getCommodityName(), "" + tr.quantity,
                                tr.sourceMarketName, tr.sourceFactionId);
                    }
                }
                if (!data.interSystemExports.isEmpty()) {
                    content.addPara("  跨星系出口 (星系内无法消耗的产出):", 3f);
                    for (TradeRecord tr : data.interSystemExports) {
                        content.addPara("    %s x%s -> 外部市场", 1f, pos,
                                tr.getCommodityName(), "" + tr.quantity);
                    }
                }
                if (!data.interSystemImports.isEmpty()) {
                    content.addPara("  跨星系进口 (星系内无法满足的需求):", 3f);
                    for (TradeRecord tr : data.interSystemImports) {
                        content.addPara("    %s x%s <- 外部市场", 1f, nh,
                                tr.getCommodityName(), "" + tr.quantity);
                    }
                }
            }
        }

        contentPanel.addUIElement(content).inTL(10f, 0f);
        imgElement.addCustom(contentPanel, 0f);
        panel.addUIElement(imgElement).inTL(0f, 25f);

        float btnW = 200f;
        float btnH = 20f;
        TooltipMakerAPI button = panel.createUIElement(pw, btnH, false);
        button.addButton("关闭", "close_dialog", fgColor, bgColor, Alignment.MID, CutStyle.ALL, btnW, btnH, 0f);
        panel.addUIElement(button).inTL((pw - btnW) / 2f, ph - btnH - 15f);
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return new CustomUIPanelPlugin() {
            @Override
            public void positionChanged(PositionAPI position) {}
            @Override
            public void renderBelow(float alpha) {}
            @Override
            public void render(float alpha) {}
            @Override
            public void advance(float amount) {}
            @Override
            public void processInput(List<InputEventAPI> events) {}
            @Override
            public void buttonPressed(Object buttonId) {
                if ("close_dialog".equals(buttonId)) {
                    if (callbacks != null) callbacks.dismissDialog();
                }
            }
        };
    }

    @Override
    public float getNoiseAlpha() {
        return 0f;
    }

    @Override
    public void advance(float amount) {}

    @Override
    public void reportDismissed(int reason) {}

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

    public static void show() {
        EconomyDataDialogDelegate.show(680f, 520f);
    }

    public static void show(float width, float height) {
        Global.getSector().addTransientScript(new EveryFrameScript() {
            boolean done = false;
            @Override
            public void advance(float amount) {
                if (done) return;
                done = true;
                InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
                if (dialog == null) return;
                dialog.showCustomVisualDialog(width, height, new EconomyDataDialogDelegate());
            }
            @Override
            public boolean isDone() {
                return done;
            }
            @Override
            public boolean runWhilePaused() {
                return true;
            }
        });
    }
}
