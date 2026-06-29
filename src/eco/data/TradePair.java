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
    private boolean intraSystem = true;
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
    public StarSystemAPI getFromSystem() { return fromSystem; }
    public PlanetAPI getFromPlanet() { return fromPlanet; }
    public MarketAPI getFromMarket() { return fromMarket; }
    public FactionAPI getFromFaction() { return fromFaction; }
    public StarSystemAPI getToSystem() { return toSystem; }
    public PlanetAPI getToPlanet() { return toPlanet; }
    public MarketAPI getToMarket() { return toMarket; }
    public FactionAPI getToFaction() { return toFaction; }
    public String getItemId() { return itemId; }
    public int getItemNum() { return itemNum; }
    public boolean isIntraSystem() { return intraSystem; }
    public void setIntraSystem(boolean intraSystem) { this.intraSystem = intraSystem; }
}
