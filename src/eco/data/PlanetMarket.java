package eco.data;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.*;

public class PlanetMarket{
    private StarSystemAPI system;
    private PlanetAPI planet;
    private MarketAPI market;
    private FactionAPI faction;
    // 产能/消耗 总和
    private Map<String, Integer> supply = new HashMap<>();
    private Map<String, Integer> demand = new HashMap<>();
    // 生产/进口 订单
    private List<TradePair> supplyTrade = new ArrayList<>();
    private List<TradePair> demandTrade = new ArrayList<>();
    private float getPeopleScale(int size) {
        if (size <= 1) return 0.01f;
        if (size == 2) return 0.10f;
        if (size == 3) return 1.0f;

        float result = 1.0f;
        final float r = 0.772f;
        for (int s = 4; s <= size; s++) {
            result *= (1.0f + 9.0f * (float) Math.pow(r, s - 4));
        }
        return result;
    }

    public PlanetMarket(StarSystemAPI system, PlanetAPI planet, MarketAPI market, FactionAPI faction){
        this.system = system;
        this.planet = planet;
        this.market = market;
        this.faction = faction;
    }

    public void updateSupplyAndDemand() {
        supply.clear();
        demand.clear();
        for (CommodityOnMarketAPI item : market.getAllCommodities()) {
            if (item.isNonEcon() || item.getCommodity().isMeta()) continue;

            int totalSupply = 0;
            int totalDemand = 0;
            boolean isPrimary = item.getCommodity().isPrimary();
            for (Industry ind : market.getIndustries()) {
                totalSupply += ind.getSupply(item.getId()).getQuantity().getModifiedInt();
                if (isPrimary) {
                    totalDemand += ind.getDemand(item.getId()).getQuantity().getModifiedInt();
                }
            }
            totalSupply = (int)(totalSupply * getPeopleScale(market.getSize()));
            totalDemand  = (int)(totalDemand * getPeopleScale(market.getSize()));

            int sd = totalSupply - totalDemand;
            if (sd > 0) supply.put(item.getId(), sd);
            if (sd < 0) demand.put(item.getId(), -sd);
        }
    }

    public int getSupply(String commodityId)    { return supply.getOrDefault(commodityId, 0); }
    public int getDemand(String commodityId)    { return demand.getOrDefault(commodityId, 0); }
    public MarketAPI getMarket() {
        return market;
    }
    public StarSystemAPI getSystem() {
        return system;
    }
    public PlanetAPI getPlanet() {
        return planet;
    }
    public FactionAPI getFaction() {
        return faction;
    }
    public List<TradePair> getSupplyTrade() {
        return supplyTrade;
    }
    public List<TradePair> getDemandTrade() {
        return demandTrade;
    }
    public void addSupplyTrade(TradePair supplyTradePair) {
        this.supplyTrade.add(supplyTradePair);
    }
    public void addDemandTrade(TradePair demandTradePair) {
        this.demandTrade.add(demandTradePair);
    }
    public Set<String> getCommodityIds() {
        Set<String> ids = new HashSet<>();
        ids.addAll(supply.keySet());
        ids.addAll(demand.keySet());
        return ids;
    }
}
