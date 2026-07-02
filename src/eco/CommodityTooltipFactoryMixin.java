package eco;

import com.fs.starfarer.O0OO;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.reach.CommodityMarketData;
import com.fs.starfarer.campaign.econ.reach.MarketShareData;
import com.fs.starfarer.campaign.ui.marketinfo.CommodityTooltipFactory;
import com.fs.starfarer.loading.SpecStore;
import com.fs.starfarer.settings.StarfarerSettings;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import com.fs.starfarer.ui.interfacenew;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

@Mixin(value = com.fs.starfarer.campaign.ui.marketinfo.CommodityTooltipFactory.class)
public abstract class CommodityTooltipFactoryMixin {



    @Unique
    private static Method getSprite$new;
    @Unique
    private static Method createIconLine$super1;
    @Unique
    private static Method createIconLine$super2;
    @Unique
    private static Field o_industry$new;
    @Unique
    private static final TooltipMakerAPI.StatModValueGetter MOD_GETTER;

    static{
        try {
            getSprite$new = StarfarerSettings.class.getDeclaredMethod("new", String.class, String.class);
            createIconLine$super1 = CommodityTooltipFactory.class.getDeclaredMethod("super", String.class, Color.class, boolean.class, String.class, float.class, float.class);
            createIconLine$super2 = CommodityTooltipFactory.class.getDeclaredMethod("super", CommodityOnMarketAPI.class, com.fs.starfarer.campaign.ui.marketinfo.f.o.class, String.class, float.class, float.class);

            o_industry$new = CommodityTooltipFactory.o.class.getDeclaredField("new");
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        MOD_GETTER = new TooltipMakerAPI.StatModValueGetter() {
            public String getPercentValue(MutableStat.StatMod statMod) {
                return null;
            }
            public String getMultValue(MutableStat.StatMod statMod) {
                return null;
            }
            public String getFlatValue(MutableStat.StatMod statMod) {
                return statMod.desc != null && !statMod.desc.isEmpty() ? null : "";
            }
            public Color getModColor(MutableStat.StatMod statMod) {
                return statMod.getValue() < 0.0F ? O0OO.ÒÓ0000 : null;
            }
        };
    }

    @Unique
    private static String CTF$getSpritePath(String a, String b){
        try{
            return (String) getSprite$new.invoke(a,b);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Unique
    private static interfacenew CTF$createIconLine(String iconPath, Color iconColor, boolean isSimple, String description, float width, float height){
        try{
            return (interfacenew) createIconLine$super1.invoke(iconPath,iconColor,isSimple,description,width,height);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Unique
    private static interfacenew CTF$createIconLine(CommodityOnMarketAPI commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o state, String description, float width, float height){
        try{
            return (interfacenew) createIconLine$super2.invoke(commodity, state, description, width, height);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Unique
    private static void CTF$setLineIndustry(CommodityTooltipFactory.o line, Industry industry) {
        try {
            o_industry$new.set(line, industry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Unique
    private static Industry CTF$getLineIndustry(CommodityTooltipFactory.o line) {
        try {
            return (Industry) o_industry$new.get(line);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "super(Lcom/fs/starfarer/api/campaign/econ/CommodityOnMarketAPI;)Lcom/fs/starfarer/ui/impl/StandardTooltipV2Expandable;", at = @At("HEAD"), cancellable = true)
    private static void onSuperReturn(CommodityOnMarketAPI commodity, CallbackInfoReturnable<StandardTooltipV2Expandable> callback) {
        StandardTooltipV2Expandable tooltip = new StandardTooltipV2Expandable(500.0F, true) {
            public void createImpl(boolean isExpanded) {
                MarketAPI market = commodity.getMarket();
                CommoditySpecAPI commoditySpec = commodity.getCommodity();
                if (commoditySpec.hasTag("codex_unlockable")) {
                    SharedUnlockData.get().reportPlayerAwareOfCommodity(commoditySpec.getId(), true);
                }

                this.setCodexEntryId(CodexDataV2.getCommodityEntryId(commoditySpec.getId()));
                this.setBgAlpha(0.9F);

                FactionAPI faction = market.getFaction();
                Color baseColor = faction.getBaseUIColor();
                Color darkColor = faction.getDarkUIColor();
                Color grayColor = Misc.getGrayColor();
                Color highlightColor = Misc.getHighlightColor();

                float smallGap = 3.0F;
                float paragraphGap = 10.0F;

                this.addTitle(commodity.getCommodity().getName(), baseColor);

                Description description = SpecStore.o00000(commodity.getId(), Description.Type.RESOURCE);
                this.addPara(description.getText1(), paragraphGap);

                Color hintColor = O0OO.õo0000;

                boolean allowAnyColony = Global.getSettings().getBoolean("allowPriceViewAtAnyColony");
                boolean canViewMarket = Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay() || allowAnyColony;
                if (canViewMarket) {
                    this.addPara("点击查看全星域市场信息", hintColor, paragraphGap);
                } else {
                    this.addPara("必须在通讯中继站有效范围内才可查看全星域市场信息", Misc.getNegativeHighlightColor(), paragraphGap);
                }

                String commodityId = commodity.getId();
                this.expandString = "按 {%s} 显示图例";
                this.unexpandString = "按 {%s} 返回";
                if (isExpanded) {
                    float height = 24.0F;
                    float width = this.width;
                    float negaGap = -6.0F;
                    this.addSectionHeading("图例", baseColor, darkColor, Alignment.MID, paragraphGap);
                    this.addCustom(CTF$createIconLine(CTF$getSpritePath("commodity_markers", "production"), null, false, "可通过当地生产来满足需求。测试文本1", width, height), smallGap);
                    this.addCustom(CTF$createIconLine(market.getFaction().getCrest(), null, false, "可通过势力之内的进口量来满足需求。测试文本2", width, height), negaGap);
                    this.addCustom(CTF$createIconLine(CTF$getSpritePath("commodity_markers", "imports"), null, false, "可通过势力之外的进口量来满足需求。测试文本3", width, height), negaGap);
                    this.addCustom(CTF$createIconLine(null, null, true, "走私或出售由非法企业生产的违禁品，将没有出口收益。测试文本4", width, height), negaGap);
                    this.addCustom(CTF$createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[0], "用于本地需求或用于出口的物资。测试文本5", width, height), negaGap);
                    this.addCustom(CTF$createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[1], "通过一次贸易，进口或事件获得的物资。测试文本6", width, height), negaGap);
                    this.addCustom(CTF$createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[2], "过剩，可自由获取但缺乏需求和出口。价格降低。测试文本7", width, height), negaGap);
                    this.addCustom(CTF$createIconLine(commodity, com.fs.starfarer.campaign.ui.marketinfo.f.o.values()[5], "短缺，有需求但无法获取。价格升高。测试文本8", width, height), negaGap);
                } else {
                    this.addSectionHeading("生产，进口与需求", baseColor, darkColor, Alignment.MID, paragraphGap);
                    ArrayList<CommodityTooltipFactory.o> supplyItemLines = new ArrayList<>();
                    ArrayList<CommodityTooltipFactory.o> demandItemLines = new ArrayList<>();



                    for (Industry industry : market.getIndustries()) {
                        int itemNum = industry.getSupply(commodityId).getQuantity().getModifiedInt();
                        CommodityTooltipFactory.o itemLine;
                        if (itemNum > 0) {
                            itemLine = new CommodityTooltipFactory.o();
                            CTF$setLineIndustry(itemLine, industry);
                            itemLine.o00000 = itemNum;
                            supplyItemLines.add(itemLine);
                        }

                        itemNum = industry.getDemand(commodityId).getQuantity().getModifiedInt();
                        if (itemNum > 0) {
                            itemLine = new CommodityTooltipFactory.o();
                            CTF$setLineIndustry(itemLine, industry);
                            itemLine.o00000 = itemNum;
                            demandItemLines.add(itemLine);
                        }
                    }

                    supplyItemLines.sort((o1, o2)-> Integer.compare(o2.o00000,o1.o00000));
                    demandItemLines.sort((o1, o2)-> Integer.compare(o2.o00000,o1.o00000));

                    CommodityMarketData marketData = ((CommodityOnMarket)commodity).getCommodityMarketData();
                    MarketShareData marketShareData = marketData.getMarketShareData(commodity.getMarket());
                    int availableItemNum = commodity.getAvailable();

                    this.addPara("库存量：{%s}", paragraphGap, highlightColor, "" + availableItemNum);

                    MutableStat modStats = new MutableStat(0.0F);

                    if (!supplyItemLines.isEmpty() && CTF$getLineIndustry(supplyItemLines.get(0)) != null) {
                        Industry industry = CTF$getLineIndustry(supplyItemLines.get(0));
                        MutableStat mutableStat = industry.getSupply(commodity.getId()).getQuantity();

                        for (MutableStat.StatMod mod : mutableStat.getFlatMods().values()) {
                            if ("ind_sb".equals(mod.source)) continue;
                            String desc = mod.desc;
                            if (desc != null && !desc.endsWith(")") && !desc.startsWith("管理员")) {
                                desc = desc + " (" + industry.getCurrentName() + ")";
                            }
                            modStats.modifyFlat("supply_" + mod.source, mod.value, desc);
                        }
                        for (MutableStat.StatMod mod : industry.getSupplyBonus().getFlatMods().values()) {
                            String desc = mod.desc;
                            if (desc != null
                                    && !desc.endsWith(")")
                                    && !desc.startsWith("管理员")
                                    && !industry.getSupplyBonusFromOther().getFlatMods().containsKey(mod.source)) {
                                desc = desc + " (" + industry.getCurrentName() + ")";
                            }
                            modStats.modifyFlat("bonus_" + mod.source, mod.value, desc);
                        }
                    }

                    for (MutableStat.StatMod flatMods : commodity.getAvailableStat().getFlatMods().values()) {
                        if (!CommodityMarketData.KEY_LOCAL.equals(flatMods.source)) {
                            modStats.modifyFlat("available_ " + flatMods.source, flatMods.value, flatMods.desc);
                        }
                    }

                    if (!modStats.isUnmodified()) {
                        this.addStatModGrid(450.0F, 30.0F, 10.0F, smallGap, modStats, MOD_GETTER);
                    }

                    this.addPara("在111本殖民地的供给关系中，只计算当地产量最大的供应源，同理，进口和走私的输入量 也不能超过来源殖民地产量最大的供应源。", grayColor, paragraphGap);
                    int gridLineIndex;
                    if (commodity.getMaxDemand() <= 0) {
                        this.addPara("无本地需求。", paragraphGap);
                    } else {
                        Color demandColor = O0OO.ÕO0000;
                        if (commodity.getMaxDemand() > availableItemNum) {
                            demandColor = O0OO.ÒÓ0000;
                        }

                        this.addPara("最大111需求量：{%s}", paragraphGap, demandColor, "" + commodity.getMaxDemand());
                        this.beginGridFlipped(450.0F, 1, 30.0F, paragraphGap);

                        gridLineIndex = 0;

                        for (CommodityTooltipFactory.o itemLine : demandItemLines) {
                            demandColor = O0OO.ÕO0000;
                            if (itemLine.o00000 > availableItemNum) {
                                demandColor = O0OO.ÒÓ0000;
                            }
                            this.addToGrid(0, gridLineIndex++, itemLine.Ò00000(), "" + itemLine.o00000, demandColor);
                        }

                        this.addGrid(smallGap);
                    }

                    int shippingCapacity = CommodityMarketData.getShippingCapacity(market, false);
                    this.addSectionHeading("出口", baseColor, darkColor, Alignment.MID, paragraphGap);
                    if (commodity.getMaxSupply() <= 0) {
                        this.addPara("无可出口的111本地产品。", paragraphGap);
                    } else if (marketShareData.isSourceIsIllegal()) {
                        this.addPara(market.getName() + " 拥有 {%s} 的出111口市场份额，但 " + commodity.getCommodity().getName() + "。" + "违禁品将不会产生任何收益。", paragraphGap, highlightColor, marketData.getExportMarketSharePercent(market) + "%");
                    } else {
                        gridLineIndex = commodity.getExportIncome();
                        int exporedQuantity = Math.min(shippingCapacity, commodity.getMaxSupply());
                        exporedQuantity = Math.min(commodity.getAvailable(), exporedQuantity);
                        String credits = Misc.getDGSCredits((float)gridLineIndex);

                        if (exporedQuantity <= 0) {
                            this.addPara(market.getName() + " 的出口量为 {%s} 单位 而这里的 " + commodity.getCommodity().getName() + " 仅占全星域市场份额的 {%s}。", paragraphGap, highlightColor, "" + exporedQuantity, marketData.getExportMarketSharePercent(market) + "%");
                        } else if (commodity.getCommodity().getExportValue() <= 0.0F) {
                            this.addPara(market.getName() + " 的出口量为 {%s} 单位 而这里的 " + commodity.getCommodity().getName() + " 占全星域市场份额的 {%s}。" + "因此这里的 " + commodity.getCommodity().getName() + " 将不会带来任何收益。", paragraphGap, highlightColor, "" + exporedQuantity, marketData.getExportMarketSharePercent(market) + "%");
                        } else {
                            this.addPara(market.getName() + " 的每月出口量为 {%s} 单位 而这里的 " + commodity.getCommodity().getName() + " 占全星域市场份额的 {%s}。且每月带来 {%s} 的收益。", paragraphGap, highlightColor, "" + exporedQuantity, marketData.getExportMarketSharePercent(market) + "%", credits);
                        }

                        MutableStat exportCredits = Global.getSector().getPlayerStats().getDynamic().getStat(Stats.getCommodityExportCreditsMultId(commodity.getId()));
                        if (!exportCredits.isUnmodified()) {
                            this.addStatModGrid(this.width, 50.0F, 10.0F, paragraphGap, exportCredits);
                        }

                        int exportShortfall = commodity.getMaxSupply() - exporedQuantity;
                        if (exportShortfall > 0) {
                            this.addPara("由于流通性不足，导致出口量降低 {%s}。", paragraphGap, Misc.getNegativeHighlightColor(), "" + exportShortfall, credits);
                        }

                        this.addPara("提高产量与殖民地的流通111性，将提高出口市场份额以及收入。", grayColor, paragraphGap);
                    }

                }
            }
        };
        callback.setReturnValue(tooltip);
        callback.cancel();
    }
}
