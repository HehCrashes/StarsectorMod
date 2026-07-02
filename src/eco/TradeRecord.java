package eco;

import com.fs.starfarer.api.Global;

public class TradeRecord {
    public String commodityId;
    public int quantity;
    public String sourceMarketName;
    public String sourceFactionId;
    public String sourceSystemName;
    public String destMarketName;
    public String destFactionId;
    public String destSystemName;
    public boolean isIntraSystem;

    public TradeRecord(String commodityId, int quantity,
                       String sourceMarketName, String sourceFactionId, String sourceSystemName,
                       String destMarketName, String destFactionId, String destSystemName,
                       boolean isIntraSystem) {
        this.commodityId = commodityId;
        this.quantity = quantity;
        this.sourceMarketName = sourceMarketName;
        this.sourceFactionId = sourceFactionId;
        this.sourceSystemName = sourceSystemName;
        this.destMarketName = destMarketName;
        this.destFactionId = destFactionId;
        this.destSystemName = destSystemName;
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
