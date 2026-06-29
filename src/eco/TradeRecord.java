package eco;

import com.fs.starfarer.api.Global;

public class TradeRecord {
    public String commodityId;
    public int quantity;
    public String sourceMarketName;
    public String sourceFactionId;
    public String destMarketName;
    public String destFactionId;
    public boolean isIntraSystem;

    public TradeRecord(String commodityId, int quantity,
                       String sourceMarketName, String sourceFactionId,
                       String destMarketName, String destFactionId,
                       boolean isIntraSystem) {
        this.commodityId = commodityId;
        this.quantity = quantity;
        this.sourceMarketName = sourceMarketName;
        this.sourceFactionId = sourceFactionId;
        this.destMarketName = destMarketName;
        this.destFactionId = destFactionId;
        this.isIntraSystem = isIntraSystem;
    }

    public String getCommodityName() {
        if (commodityId == null || commodityId.isEmpty()) return "?";
        try {
            return Global.getSettings().getCommoditySpec(commodityId).getName();
        } catch (Exception e) {
            return commodityId;
        }
    }
}
