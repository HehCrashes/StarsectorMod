package eco.data;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class Trade{
    private StarSystemAPI system;
    private PlanetAPI planet;
    private MarketAPI market;
    private FactionAPI faction;
    private String itemId;
    private int itemNum;
    public Trade(PlanetMarket planetMarket, String itemId, int itemNum){
        this.system = planetMarket.getSystem();
        this.planet = planetMarket.getPlanet();
        this.market = planetMarket.getMarket();
        this.faction = planetMarket.getFaction();
        this.itemId = itemId;
        this.itemNum = itemNum;
    }
    public StarSystemAPI getSystem() {
        return system;
    }
    public PlanetAPI getPlanet() {
        return planet;
    }
    public MarketAPI getMarket() {
        return market;
    }
    public FactionAPI getFaction() {
        return faction;
    }
    public String getItemId() {
        return itemId;
    }
    public int getItemNum() {
        return itemNum;
    }
    public void addItemNum(int itemNum) { this.itemNum += itemNum;}
}
