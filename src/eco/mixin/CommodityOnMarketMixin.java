package eco.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.campaign.econ.Market;

@Mixin(targets = "com.fs.starfarer.campaign.econ.CommodityOnMarket")
public abstract class CommodityOnMarketMixin {

    @Shadow
    private int maxSupply;

    @Shadow
    private int maxDemand;

    @Shadow
    private Market market;

    @Inject(method = "updateMaxSupplyAndDemand", at = @At("RETURN"))
    private void ccOverrideAfterUpdate(CallbackInfo ci) {
        try {
            if (market == null || !(market instanceof MarketAPI)) {
                return;
            }
            maxSupply = 2;
            maxDemand = 2;
        } catch (Throwable ignored) {
        }
    }
}
