package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;

import java.util.*;

public class SystemEconomyService implements EconomyTickListener {

    private static final String MEM_KEY = "$corecracking_econ_data";
    private static final String MONTH_KEY = "$corecracking_econ_month";

    @Override
    public void reportEconomyTick(int iterIndex) {}

    @Override
    public void reportEconomyMonthEnd() {
        runEconomyCalculation();
    }

    private void runEconomyCalculation() {
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        Map<String, List<MarketAPI>> marketsBySystem = groupMarketsByStarSystem(allMarkets);
        int currentMonth = Global.getSector().getClock().getMonth();

        for (Map.Entry<String, List<MarketAPI>> entry : marketsBySystem.entrySet()) {
            String systemId = entry.getKey();
            List<MarketAPI> systemMarkets = entry.getValue();

            Map<String, List<SupplyEntry>> supplyByCommodity = new HashMap<>();
            Map<String, List<DemandEntry>> demandByCommodity = new HashMap<>();

            collectSupplyAndDemand(systemMarkets, supplyByCommodity, demandByCommodity);

            Map<String, List<SupplyEntry>> supplyCopy = deepCopySupply(supplyByCommodity);
            Map<String, List<DemandEntry>> demandCopy = deepCopyDemand(demandByCommodity);

            matchSameFaction(supplyCopy, demandCopy);
            matchCrossFaction(supplyCopy, demandCopy);

            storeResults(systemMarkets, supplyCopy, demandCopy, supplyByCommodity, demandByCommodity, currentMonth);
        }
    }

    private Map<String, List<MarketAPI>> groupMarketsByStarSystem(List<MarketAPI> allMarkets) {
        Map<String, List<MarketAPI>> result = new HashMap<>();
        for (MarketAPI market : allMarkets) {
            if (!market.isInEconomy()) continue;
            StarSystemAPI system = market.getStarSystem();
            if (system == null) continue;
            String systemId = system.getId();
            result.computeIfAbsent(systemId, k -> new ArrayList<>()).add(market);
        }
        return result;
    }

    private void collectSupplyAndDemand(List<MarketAPI> systemMarkets,
                                         Map<String, List<SupplyEntry>> supplyByCommodity,
                                         Map<String, List<DemandEntry>> demandByCommodity) {
        for (MarketAPI market : systemMarkets) {
            String marketId = market.getId();
            String marketName = market.getName();
            String factionId = market.getFactionId();
            if (market.getFaction() != null) {
                factionId = market.getFaction().getId();
            }

            for (CommodityOnMarketAPI com : market.getAllCommodities()) {
                if (com.isNonEcon()) continue;
                if (com.getCommodity().isMeta()) continue;

                String commodityId = com.getId();
                com.updateMaxSupplyAndDemand();
                int supply = com.getMaxSupply();
                int demand = com.getMaxDemand();

                if (supply > 0) {
                    supplyByCommodity.computeIfAbsent(commodityId, k -> new ArrayList<>())
                            .add(new SupplyEntry(commodityId, supply, marketId, marketName, factionId));
                }
                if (demand > 0) {
                    demandByCommodity.computeIfAbsent(commodityId, k -> new ArrayList<>())
                            .add(new DemandEntry(commodityId, demand, marketId, marketName, factionId));
                }
            }
        }
    }

    private Map<String, List<SupplyEntry>> deepCopySupply(Map<String, List<SupplyEntry>> source) {
        Map<String, List<SupplyEntry>> copy = new HashMap<>();
        for (Map.Entry<String, List<SupplyEntry>> entry : source.entrySet()) {
            List<SupplyEntry> listCopy = new ArrayList<>();
            for (SupplyEntry se : entry.getValue()) {
                listCopy.add(new SupplyEntry(se.commodityId, se.quantity, se.marketId, se.marketName, se.factionId));
            }
            copy.put(entry.getKey(), listCopy);
        }
        return copy;
    }

    private Map<String, List<DemandEntry>> deepCopyDemand(Map<String, List<DemandEntry>> source) {
        Map<String, List<DemandEntry>> copy = new HashMap<>();
        for (Map.Entry<String, List<DemandEntry>> entry : source.entrySet()) {
            List<DemandEntry> listCopy = new ArrayList<>();
            for (DemandEntry de : entry.getValue()) {
                listCopy.add(new DemandEntry(de.commodityId, de.quantity, de.marketId, de.marketName, de.factionId));
            }
            copy.put(entry.getKey(), listCopy);
        }
        return copy;
    }

    private void matchSameFaction(Map<String, List<SupplyEntry>> supplyCopy,
                                   Map<String, List<DemandEntry>> demandCopy) {
        for (String commodityId : supplyCopy.keySet()) {
            List<SupplyEntry> supplies = supplyCopy.get(commodityId);
            List<DemandEntry> demands = demandCopy.get(commodityId);
            if (demands == null) continue;

            for (DemandEntry demand : demands) {
                if (demand.quantity <= 0) continue;
                for (SupplyEntry supply : supplies) {
                    if (supply.quantity <= 0) continue;
                    if (!supply.factionId.equals(demand.factionId)) continue;
                    executeTrade(supply, demand, true);
                    if (demand.quantity <= 0) break;
                }
            }
        }
    }

    private void matchCrossFaction(Map<String, List<SupplyEntry>> supplyCopy,
                                    Map<String, List<DemandEntry>> demandCopy) {
        for (String commodityId : supplyCopy.keySet()) {
            List<SupplyEntry> supplies = supplyCopy.get(commodityId);
            List<DemandEntry> demands = demandCopy.get(commodityId);
            if (demands == null) continue;

            for (DemandEntry demand : demands) {
                if (demand.quantity <= 0) continue;
                for (SupplyEntry supply : supplies) {
                    if (supply.quantity <= 0) continue;
                    executeTrade(supply, demand, true);
                    if (demand.quantity <= 0) break;
                }
            }
        }
    }

    private void executeTrade(SupplyEntry supply, DemandEntry demand, boolean isIntraSystem) {
        int tradeQty;
        if (demand.quantity > supply.quantity) {
            tradeQty = supply.quantity;
            demand.quantity -= supply.quantity;
            supply.quantity = 0;
        } else if (demand.quantity < supply.quantity) {
            tradeQty = demand.quantity;
            supply.quantity -= demand.quantity;
            demand.quantity = 0;
        } else {
            tradeQty = supply.quantity;
            supply.quantity = 0;
            demand.quantity = 0;
        }

        supply.tradeRecords.add(new TradeRecord(
                supply.commodityId, tradeQty,
                supply.marketId, supply.marketName, supply.factionId,
                demand.marketId, demand.marketName, demand.factionId,
                isIntraSystem
        ));
        demand.tradeRecords.add(new TradeRecord(
                demand.commodityId, tradeQty,
                supply.marketId, supply.marketName, supply.factionId,
                demand.marketId, demand.marketName, demand.factionId,
                isIntraSystem
        ));
    }

    private void storeResults(List<MarketAPI> systemMarkets,
                               Map<String, List<SupplyEntry>> supplyCopy,
                               Map<String, List<DemandEntry>> demandCopy,
                               Map<String, List<SupplyEntry>> supplyOriginal,
                               Map<String, List<DemandEntry>> demandOriginal,
                               int currentMonth) {
        Map<String, PlanetTradeData> dataMap = new HashMap<>();
        for (MarketAPI market : systemMarkets) {
            dataMap.put(market.getId(), new PlanetTradeData(market.getId()));
        }
        for (Map.Entry<String, PlanetTradeData> entry : dataMap.entrySet()) {
            entry.getValue().lastComputedMonth = currentMonth;
        }

        // Collect intra-system exports from supply entries' tradeRecords
        for (List<SupplyEntry> supplyList : supplyCopy.values()) {
            for (SupplyEntry se : supplyList) {
                PlanetTradeData data = dataMap.get(se.marketId);
                if (data == null) continue;
                for (TradeRecord tr : se.tradeRecords) {
                    data.intraSystemExports.add(tr);
                }
            }
        }

        // Collect intra-system imports from demand entries' tradeRecords
        for (List<DemandEntry> demandList : demandCopy.values()) {
            for (DemandEntry de : demandList) {
                PlanetTradeData data = dataMap.get(de.marketId);
                if (data == null) continue;
                for (TradeRecord tr : de.tradeRecords) {
                    data.intraSystemImports.add(tr);
                }
            }
        }

        // Unmatched supplies -> inter-system exports
        for (List<SupplyEntry> supplyList : supplyCopy.values()) {
            for (SupplyEntry se : supplyList) {
                if (se.quantity > 0) {
                    PlanetTradeData data = dataMap.get(se.marketId);
                    if (data == null) continue;
                    data.interSystemExports.add(new TradeRecord(
                            se.commodityId, se.quantity,
                            se.marketId, se.marketName, se.factionId,
                            "", "外部市场", "",
                            false
                    ));
                }
            }
        }

        // Unmatched demands -> inter-system imports
        for (List<DemandEntry> demandList : demandCopy.values()) {
            for (DemandEntry de : demandList) {
                if (de.quantity > 0) {
                    PlanetTradeData data = dataMap.get(de.marketId);
                    if (data == null) continue;
                    data.interSystemImports.add(new TradeRecord(
                            de.commodityId, de.quantity,
                            "", "外部市场", "",
                            de.marketId, de.marketName, de.factionId,
                            false
                    ));
                }
            }
        }

        // Store in market memory
        for (PlanetTradeData data : dataMap.values()) {
            MarketAPI market = Global.getSector().getEconomy().getMarket(data.marketId);
            if (market != null) {
                market.getMemoryWithoutUpdate().set(MEM_KEY, data);
                market.getMemoryWithoutUpdate().set(MONTH_KEY, currentMonth);
            }
        }

        EconomyDataIntel.ensureIntelsForAllSystems();
    }

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
}
