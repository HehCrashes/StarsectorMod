package data.scripts.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.sql.Timestamp;

import static data.scripts.data.DialogData.*;

public class PlanetCrackedIntel extends BaseIntelPlugin {
    private final String systemName;
    private final String planetName;
    private final String factionName;
    private final SectorEntityToken newPlanet;
    private final CampaignClockAPI time;

    public PlanetCrackedIntel(String systemName, String planetName, String factionName, SectorEntityToken newPlanet) {
        this.systemName = systemName;
        this.planetName = planetName;
        this.factionName = factionName;
        this.newPlanet = newPlanet;
        this.time = Global.getSector().getClock();
    }

    @Override
    protected String getName() {
        return planetName + "已被完全摧毁";
    }

    @Override
    public String getIcon() {
        return Global.getSector().getPlayerFaction().getCrest();
    }
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return newPlanet;
    }
    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara(
                "%s %s(%s)于%s被%s使用地爆天星摧毁",
                opad,
                highlight,
                systemName,
                planetName,
                factionName,
                time.getDateString(),
                Global.getSector().getPlayerFaction().getDisplayName()
        );
        info.addPara("{%s}",opad,fatalWarn,"该星球已在烈焰中毁灭");
        info.addPara(
                "发生时间：%s",
                opad,
                highlight,
                time.getDateString()
        );
    }
}
