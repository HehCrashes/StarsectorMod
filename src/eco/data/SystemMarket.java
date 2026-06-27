package eco.data;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemMarket {
    private StarSystemAPI system;
    private Map<PlanetAPI, PlanetMarket> planetMarkets = new HashMap<>();
    // 产能/消耗 总和
    private List<Trade> supply = new ArrayList<>();
    private List<Trade> demand = new ArrayList<>();
    // 生产/进口 预订单
    private List<Trade> supplyList = new ArrayList<>();
    private List<Trade> demandList = new ArrayList<>();
    public SystemMarket(StarSystemAPI system){
        this.system = system;
    }
    public List<Trade> getSupply() {
        return supply;
    }
    public List<Trade> getDemand() {
        return demand;
    }
    public List<Trade> getSupplyList() {
        return supplyList;
    }
    public List<Trade> getDemandList() {
        return demandList;
    }
    public Map<PlanetAPI, PlanetMarket> getPlanetMarkets(){
        return planetMarkets;
    }
    public void addSupplyList(Trade trade){
        supplyList.add(trade);
    }
    public void addDemandList(Trade trade){
        demandList.add(trade);
    }
    public void addSupply(Trade trade) {
        supply.add(trade);
    }
    public void addDemand(Trade trade) {
        demand.add(trade);
    }
    public void addPlanetMarket(PlanetAPI planetAPI, PlanetMarket planetMarket){
        planetMarkets.put(planetAPI,planetMarket);
    }
    public void updateSupplyAndDemand(){
        supplyList.clear();
        demandList.clear();
        for(Map.Entry<PlanetAPI, PlanetMarket> planetMarketPair : planetMarkets.entrySet()){
            PlanetMarket planetMarket = planetMarketPair.getValue();
            for (CommodityOnMarketAPI item : planetMarket.getMarket().getAllCommodities()) {
                if (item.isNonEcon()) continue;
                if (item.getCommodity().isMeta()) continue;

                String itemId = item.getId();
                int supply = planetMarket.getSupply(itemId);
                int demand = planetMarket.getDemand(itemId);
                if (supply > 0) {
                    this.addSupplyList(new Trade(planetMarket, itemId, supply));
                }
                if (demand > 0) {
                    this.addDemandList(new Trade(planetMarket, itemId, demand));
                }
            }
        }
    }
    public void cleanTrade() {
        supplyList.removeIf(trade -> trade.getItemNum() <= 0);
        demandList.removeIf(trade -> trade.getItemNum() <= 0);
    }
}
