package eco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.*;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.plugins.AutofitPlugin;

public class CoreCrackingCampaignPlugin implements CampaignPlugin, ColonyInteractionListener {

    public CoreCrackingCampaignPlugin() {
        Global.getSector().getListenerManager().addListener(this);
    }

    @Override
    public String getId() {
        return "corecracking_campaign";
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        return null;
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(Object param, SectorEntityToken interactionTarget) {
        return null;
    }

    @Override
    public PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(SectorEntityToken opponent) {
        return null;
    }

    @Override
    public PluginPick<BattleAutoresolverPlugin> pickBattleAutoresolverPlugin(BattleAPI battle) {
        return null;
    }

    @Override
    public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, String factionId) {
        return null;
    }

    @Override
    public PluginPick<ReputationActionResponsePlugin> pickReputationActionResponsePlugin(Object action, PersonAPI person) {
        return null;
    }

    @Override
    public PluginPick<AssignmentModulePlugin> pickAssignmentAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
        return null;
    }

    @Override
    public PluginPick<StrategicModulePlugin> pickStrategicAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
        return null;
    }

    @Override
    public PluginPick<TacticalModulePlugin> pickTacticalAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
        return null;
    }

    @Override
    public PluginPick<NavigationModulePlugin> pickNavigationAIModule(CampaignFleetAPI fleet, ModularFleetAIAPI ai) {
        return null;
    }

    @Override
    public PluginPick<AbilityAIPlugin> pickAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai) {
        return null;
    }

    @Override
    public PluginPick<FleetStubConverterPlugin> pickStubConverter(FleetStubAPI stub) {
        return null;
    }

    @Override
    public PluginPick<FleetStubConverterPlugin> pickStubConverter(CampaignFleetAPI fleet) {
        return null;
    }

    @Override
    public PluginPick<AutofitPlugin> pickAutofitPlugin(FleetMemberAPI member) {
        return null;
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickRespawnPlugin() {
        return null;
    }

    @Override
    public PluginPick<ImmigrationPlugin> pickImmigrationPlugin(MarketAPI market) {
        return null;
    }

    @Override
    public PluginPick<AICoreAdminPlugin> pickAICoreAdminPlugin(String commodityId) {
        return null;
    }

    @Override
    public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
        return null;
    }

    @Override
    public PluginPick<FleetInflater> pickFleetInflater(CampaignFleetAPI fleet, Object params) {
        return null;
    }

    @Override
    public void updateEntityFacts(SectorEntityToken entity, MemoryAPI memory) {}

    @Override
    public void updatePersonFacts(PersonAPI person, MemoryAPI memory) {}

    @Override
    public void updateFactionFacts(FactionAPI faction, MemoryAPI memory) {}

    @Override
    public void updateGlobalFacts(MemoryAPI memory) {}

    @Override
    public void updatePlayerFacts(MemoryAPI memory) {}

    @Override
    public void updateMarketFacts(MarketAPI market, MemoryAPI memory) {}

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {}

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {}

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
        PlanetTradeData data = SystemEconomyService.getTradeData(market);
        if (data == null) return;
        String lastShownKey = "$corecracking_econ_shown_month";
        Integer lastShownMonth = (Integer) market.getMemoryWithoutUpdate().get(lastShownKey);
        int currentMonth = Global.getSector().getClock().getMonth();
        if (lastShownMonth != null && lastShownMonth == currentMonth) return;
        market.getMemoryWithoutUpdate().set(lastShownKey, currentMonth);
        EconomyDataDialogDelegate.show(680f, 520f);
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {}
}
