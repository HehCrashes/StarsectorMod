package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import eco.data.PlanetMarket;
import eco.data.SystemMarket;
import eco.data.Trade;
import eco.data.TradePair;
import org.lwjgl.Sys;

import java.util.*;

public class SystemEconomyService implements EconomyTickListener {
    /**{@code <systemId, PlanetMarkets in system> }*/
    private Map<StarSystemAPI, List<PlanetMarket>> planetMarkets;
    /**{@code <systemId, SystemMarket> }*/
    private Map<StarSystemAPI, SystemMarket> systemMarkets;
    /** 从allMarkets到system->Markets*/
    private Map<StarSystemAPI, List<PlanetMarket>> getMarkets(List<MarketAPI> allMarkets) {
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
    private void getSystemMarkets() {
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
    private void matchSystemTrade(SystemMarket systemMarket){
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
    private void matchInterSystemTrade(){
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
        for(Map.Entry<StarSystemAPI,SystemMarket> systemMarketPair : systemMarkets.entrySet()){
            matchSystemTrade(systemMarketPair.getValue());
        }

    }
}
