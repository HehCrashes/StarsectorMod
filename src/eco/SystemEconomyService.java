package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import eco.data.PlanetMarket;
import eco.data.SystemMarket;
import eco.data.Trade;
import eco.data.TradePair;

import java.util.*;

public class SystemEconomyService implements EconomyTickListener {
    /**{@code <systemId, PlanetMarkets in system> }*/
    private static Map<StarSystemAPI, List<PlanetMarket>> planetMarkets = new HashMap<>();
    /**{@code <systemId, SystemMarket> }*/
    private static Map<StarSystemAPI, SystemMarket> systemMarkets = new HashMap<>();
    /** 从allMarkets到system->Markets*/
    private static Map<StarSystemAPI, List<PlanetMarket>> getMarkets(List<MarketAPI> allMarkets) {
        Map<StarSystemAPI, List<PlanetMarket>> result = new HashMap<>();
        for (MarketAPI market : allMarkets) {
            if (!market.isInEconomy()) continue;

            StarSystemAPI system = market.getStarSystem();
            PlanetAPI planet = market.getPlanetEntity();
            FactionAPI faction = market.getFaction();
            if (system == null || planet == null) continue;

            PlanetMarket planetMarket = new PlanetMarket(system, planet, market, faction);
            planetMarket.updateSupplyAndDemand();
            result.computeIfAbsent(system, k -> new ArrayList<>()).add(planetMarket);
        }
        return result;
    }
    /** 从system->Markets到systemMarkets*/
    private static void getSystemMarkets() {
        systemMarkets = new HashMap<>();
        for(Map.Entry<StarSystemAPI, List<PlanetMarket>> systemPMs : planetMarkets.entrySet()){
            SystemMarket systemMarket = new SystemMarket(systemPMs.getValue().get(0).getSystem());
            for (PlanetMarket planetMarket : systemPMs.getValue()) {
                systemMarket.addPlanetMarket(planetMarket.getPlanet(), planetMarket);
            }
            systemMarket.updateSupplyAndDemand();
            systemMarkets.put(systemPMs.getKey(), systemMarket);
        }
    }
    /** 匹配systemMarkets中订单*/
    private static void matchSystemTrade(SystemMarket systemMarket){
        Map<String,Map<FactionAPI,List<Trade>>> supplyTrades = new HashMap<>();
        Map<String,Map<FactionAPI,List<Trade>>> demandTrades = new HashMap<>();
        for(Trade trade : systemMarket.getSupplyList()){
            supplyTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
        }
        for(Trade trade : systemMarket.getDemandList()){
            demandTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
        }
        //遍历每个物品
        for(Map.Entry<String,Map<FactionAPI,List<Trade>>> sftPair : supplyTrades.entrySet()){
            String itemID = sftPair.getKey();
            if (demandTrades.get(itemID) == null) continue;
            //遍历每个物品的每个势力
            for(Map.Entry<FactionAPI,List<Trade>> ftPair : sftPair.getValue().entrySet()){
                FactionAPI faction = ftPair.getKey();
                if (demandTrades.get(itemID).get(faction) == null) continue;
                //遍历每个物品的每个势力的每个供应单
                for(Trade supplyTrade : ftPair.getValue()){
                    if (supplyTrade.getItemNum() <= 0) continue;
                    //遍历该物品的同势力的每个供应单
                    for(Trade demandTrade : demandTrades.get(itemID).get(faction)){
                        if (demandTrade.getItemNum() <= 0) continue;
                        //达成交易
                        int itemNum = Math.min(supplyTrade.getItemNum(), demandTrade.getItemNum());
                        TradePair tradePair = new TradePair(supplyTrade,demandTrade,itemID,itemNum);
                        supplyTrade.addItemNum(-itemNum);
                        demandTrade.addItemNum(-itemNum);
                        systemMarket.getPlanetMarkets().get(supplyTrade.getPlanet()).addSupplyTrade(tradePair);
                        systemMarket.getPlanetMarkets().get(demandTrade.getPlanet()).addDemandTrade(tradePair);
                        if (supplyTrade.getItemNum() <= 0) break;
                    }
                }
            }
        }
        //遍历每个物品
        for(Map.Entry<String,Map<FactionAPI,List<Trade>>> sftPair : supplyTrades.entrySet()){
            String itemID = sftPair.getKey();
            if (demandTrades.get(itemID) == null) continue;
            //遍历每个物品的每个势力
            for(Map.Entry<FactionAPI,List<Trade>> ftPair : sftPair.getValue().entrySet()){
                //遍历每个物品的每个势力的每个供应单
                for(Trade supplyTrade : ftPair.getValue()){
                    if (supplyTrade.getItemNum() <= 0) continue;
                    //遍历该物品的每个势力
                    for(Map.Entry<FactionAPI,List<Trade>> dftPair : demandTrades.get(itemID).entrySet()){
                        for(Trade demandTrade : dftPair.getValue()){
                            if (demandTrade.getItemNum() <= 0) continue;
                            //达成交易
                            int itemNum = Math.min(supplyTrade.getItemNum(), demandTrade.getItemNum());
                            TradePair tradePair = new TradePair(supplyTrade,demandTrade,itemID,itemNum);
                            supplyTrade.addItemNum(-itemNum);
                            demandTrade.addItemNum(-itemNum);
                            systemMarket.getPlanetMarkets().get(supplyTrade.getPlanet()).addSupplyTrade(tradePair);
                            systemMarket.getPlanetMarkets().get(demandTrade.getPlanet()).addDemandTrade(tradePair);
                            if (supplyTrade.getItemNum() <= 0) break;
                        }
                        if (supplyTrade.getItemNum() <= 0) break;
                    }
                }
            }
        }
        systemMarket.cleanTrade();
        for (Trade trade : systemMarket.getSupplyList()) {
            if (trade.getItemNum() > 0) systemMarket.addSupply(trade);
        }
        for (Trade trade : systemMarket.getDemandList()) {
            if (trade.getItemNum() > 0) systemMarket.addDemand(trade);
        }
    }
    /** 匹配跨systemMarkets订单*/
    private static void matchInterSystemTrade(){
        Map<String,Map<FactionAPI,List<Trade>>> supplyTrades = new HashMap<>();
        Map<String,Map<FactionAPI,List<Trade>>> demandTrades = new HashMap<>();
        for(Map.Entry<StarSystemAPI,SystemMarket> systemMarketPair : systemMarkets.entrySet()){
            for(Trade trade : systemMarketPair.getValue().getSupply()){
                supplyTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
            }
            for(Trade trade : systemMarketPair.getValue().getDemand()){
                demandTrades.computeIfAbsent(trade.getItemId(), i -> new HashMap<>()).computeIfAbsent(trade.getFaction(), j -> new ArrayList<>()).add(trade);
            }
        }
        //遍历每个物品
        for(Map.Entry<String,Map<FactionAPI,List<Trade>>> sftPair : supplyTrades.entrySet()){
            String itemID = sftPair.getKey();
            if (demandTrades.get(itemID) == null) continue;
            //遍历每个物品的每个势力
            for(Map.Entry<FactionAPI,List<Trade>> ftPair : sftPair.getValue().entrySet()){
                FactionAPI faction = ftPair.getKey();
                if (demandTrades.get(itemID).get(faction) == null) continue;
                //遍历每个物品的每个势力的每个供应单
                for(Trade supplyTrade : ftPair.getValue()){
                    if (supplyTrade.getItemNum() <= 0) continue;
                    //遍历该物品的同势力的每个供应单
                    for(Trade demandTrade : demandTrades.get(itemID).get(faction)){
                        if (demandTrade.getItemNum() <= 0) continue;
                        //达成交易
                        int itemNum = Math.min(supplyTrade.getItemNum(), demandTrade.getItemNum());
                        TradePair tradePair = new TradePair(supplyTrade,demandTrade,itemID,itemNum);
                        tradePair.setIntraSystem(false);
                        supplyTrade.addItemNum(-itemNum);
                        demandTrade.addItemNum(-itemNum);
                        systemMarkets.get(supplyTrade.getSystem()).getPlanetMarkets().get(supplyTrade.getPlanet()).addSupplyTrade(tradePair);
                        systemMarkets.get(demandTrade.getSystem()).getPlanetMarkets().get(demandTrade.getPlanet()).addDemandTrade(tradePair);
                        if (supplyTrade.getItemNum() <= 0) break;
                    }
                }
            }
        }
        //遍历每个物品
        for(Map.Entry<String,Map<FactionAPI,List<Trade>>> sftPair : supplyTrades.entrySet()){
            String itemID = sftPair.getKey();
            if (demandTrades.get(itemID) == null) continue;
            //遍历每个物品的每个势力
            for(Map.Entry<FactionAPI,List<Trade>> ftPair : sftPair.getValue().entrySet()){
                //遍历每个物品的每个势力的每个供应单
                for(Trade supplyTrade : ftPair.getValue()){
                    if (supplyTrade.getItemNum() <= 0) continue;
                    //遍历该物品的每个势力
                    for(Map.Entry<FactionAPI,List<Trade>> dftPair : demandTrades.get(itemID).entrySet()){
                        for(Trade demandTrade : dftPair.getValue()){
                            if (demandTrade.getItemNum() <= 0) continue;
                            //达成交易
                            int itemNum = Math.min(supplyTrade.getItemNum(), demandTrade.getItemNum());
                            TradePair tradePair = new TradePair(supplyTrade,demandTrade,itemID,itemNum);
                            tradePair.setIntraSystem(false);
                            supplyTrade.addItemNum(-itemNum);
                            demandTrade.addItemNum(-itemNum);
                            systemMarkets.get(supplyTrade.getSystem()).getPlanetMarkets().get(supplyTrade.getPlanet()).addSupplyTrade(tradePair);
                            systemMarkets.get(demandTrade.getSystem()).getPlanetMarkets().get(demandTrade.getPlanet()).addDemandTrade(tradePair);
                            if (supplyTrade.getItemNum() <= 0) break;
                        }
                        if (supplyTrade.getItemNum() <= 0) break;
                    }
                }
            }
        }
        for(Map.Entry<StarSystemAPI,SystemMarket> systemMarketPair : systemMarkets.entrySet()){
            systemMarketPair.getValue().cleanTrade();
        }
    }
    @Override
    public void reportEconomyTick(int iterIndex) {}
    @Override
    public void reportEconomyMonthEnd() {
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        planetMarkets = getMarkets(allMarkets);
        getSystemMarkets();
        for (Map.Entry<StarSystemAPI, SystemMarket> systemMarketPair : systemMarkets.entrySet()) {
            matchSystemTrade(systemMarketPair.getValue());
        }
        matchInterSystemTrade();
        persistTradeData();
        EconomyDataIntel.ensureIntelsForAllSystems();
    }

    private static void persistTradeData() {
        int currentMonth = Global.getSector().getClock().getMonth();
        for (Map.Entry<StarSystemAPI, List<PlanetMarket>> entry : planetMarkets.entrySet()) {
            for (PlanetMarket pm : entry.getValue()) {
                MarketAPI market = pm.getMarket();
                PlanetTradeData data = new PlanetTradeData(market.getId());
                data.lastComputedMonth = currentMonth;

                for (TradePair tp : pm.getSupplyTrade()) {
                    String srcName = market.getName();
                    String srcFac = pm.getFaction() != null ? pm.getFaction().getDisplayName() : "?";
                    String dstName = tp.getToMarket() != null ? tp.getToMarket().getName() : "外部市场";
                    String dstFac = tp.getToFaction() != null ? tp.getToFaction().getDisplayName() : "?";
                    TradeRecord tr = new TradeRecord(tp.getItemId(), tp.getItemNum(),
                            srcName, srcFac, dstName, dstFac, tp.isIntraSystem());
                    if (tp.isIntraSystem()) {
                        data.intraSystemExports.add(tr);
                    } else {
                        data.interSystemExports.add(tr);
                    }
                }

                for (TradePair tp : pm.getDemandTrade()) {
                    String srcName = tp.getFromMarket() != null ? tp.getFromMarket().getName() : "外部市场";
                    String srcFac = tp.getFromFaction() != null ? tp.getFromFaction().getDisplayName() : "?";
                    String dstName = market.getName();
                    String dstFac = pm.getFaction() != null ? pm.getFaction().getDisplayName() : "?";
                    TradeRecord tr = new TradeRecord(tp.getItemId(), tp.getItemNum(),
                            srcName, srcFac, dstName, dstFac, tp.isIntraSystem());
                    if (tp.isIntraSystem()) {
                        data.intraSystemImports.add(tr);
                    } else {
                        data.interSystemImports.add(tr);
                    }
                }

                market.getMemoryWithoutUpdate().set(MEM_KEY, data);
                market.getMemoryWithoutUpdate().set(MONTH_KEY, currentMonth);

                Map<String, String> commodityText = new LinkedHashMap<>();
                for (String id : pm.getCommodityIds()) {
                    int s = pm.getSupply(id);
                    int d = pm.getDemand(id);
                    String name = getCommodityName(id);
                    commodityText.put(id, name + "  供应+" + s + "  消耗-" + d);
                }
                market.getMemoryWithoutUpdate().set(MEM_KEY + "_display", commodityText);
            }
        }
    }

    private static final String MEM_KEY = "$corecracking_econ_data";
    private static final String MONTH_KEY = "$corecracking_econ_month";

    public static PlanetTradeData getTradeData(MarketAPI market) {
        if (market == null) return null;
        return (PlanetTradeData) market.getMemoryWithoutUpdate().get(MEM_KEY);
    }

    public static int getLastComputedMonth(MarketAPI market) {
        if (market == null) return -1;
        Integer month = (Integer) market.getMemoryWithoutUpdate().get(MONTH_KEY);
        return month != null ? month : -1;
    }

    public static String getCommodityName(String commodityId) {
        if (commodityId == null || commodityId.isEmpty()) return "?";
        try {
            return Global.getSettings().getCommoditySpec(commodityId).getName();
        } catch (Exception e) {
            return commodityId;
        }
    }

    public static PlanetMarket getPlanetMarket(MarketAPI market) {
        if (market == null) return null;
        for(Map.Entry<StarSystemAPI, List<PlanetMarket>> list : planetMarkets.entrySet()){
            for(PlanetMarket planetMarket : list.getValue()){
                if(planetMarket.getMarket() == market){
                    return planetMarket;
                }
            }
        }
        return null;
    }
}
