package eco.data;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class TradePair{
    private StarSystemAPI fromSystem;
    private PlanetAPI fromPlanet;
    private MarketAPI fromMarket;
    private FactionAPI fromFaction;
    private StarSystemAPI toSystem;
    private PlanetAPI toPlanet;
    private MarketAPI toMarket;
    private FactionAPI toFaction;
    private String itemId;
    private int itemNum;
    public TradePair(StarSystemAPI fromSystem, PlanetAPI fromPlanet, MarketAPI fromMarket, FactionAPI fromFaction, StarSystemAPI toSystem, PlanetAPI toPlanet, MarketAPI toMarket, FactionAPI toFaction, String itemId, int itemNum) {
        this.fromSystem = fromSystem;
        this.fromPlanet = fromPlanet;
        this.fromMarket = fromMarket;
        this.fromFaction = fromFaction;
        this.toSystem = toSystem;
        this.toPlanet = toPlanet;
        this.toMarket = toMarket;
        this.toFaction = toFaction;
        this.itemId = itemId;
        this.itemNum = itemNum;
    }
    public TradePair(Trade from, Trade to, String itemId, int itemNum) {
        this.fromSystem = from.getSystem();
        this.fromPlanet = from.getPlanet();
        this.fromMarket = from.getMarket();
        this.fromFaction = from.getFaction();
        this.toSystem = to.getSystem();
        this.toPlanet = to.getPlanet();
        this.toMarket = to.getMarket();
        this.toFaction = to.getFaction();
        this.itemId = itemId;
        this.itemNum = itemNum;
    }
}
