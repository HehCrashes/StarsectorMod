package eco;

import java.util.ArrayList;
import java.util.List;

public class TradeRecord {
    public String commodityId;
    public int quantity;
    public String sourceMarketId;
    public String sourceMarketName;
    public String sourceFactionId;
    public String destMarketId;
    public String destMarketName;
    public String destFactionId;
    public boolean isIntraSystem;

    public TradeRecord() {}

    public TradeRecord(String commodityId, int quantity, String sourceMarketId, String sourceMarketName,
                       String sourceFactionId, String destMarketId, String destMarketName, String destFactionId,
                       boolean isIntraSystem) {
        this.commodityId = commodityId;
        this.quantity = quantity;
        this.sourceMarketId = sourceMarketId;
        this.sourceMarketName = sourceMarketName;
        this.sourceFactionId = sourceFactionId;
        this.destMarketId = destMarketId;
        this.destMarketName = destMarketName;
        this.destFactionId = destFactionId;
        this.isIntraSystem = isIntraSystem;
    }

    public String getCommodityName() {
        return SystemEconomyService.getCommodityName(commodityId);
    }
}

class PlanetTradeData {
    public String marketId;
    public int lastComputedMonth;
    public List<TradeRecord> intraSystemImports = new ArrayList<>();
    public List<TradeRecord> intraSystemExports = new ArrayList<>();
    public List<TradeRecord> interSystemImports = new ArrayList<>();
    public List<TradeRecord> interSystemExports = new ArrayList<>();

    public PlanetTradeData() {}

    public PlanetTradeData(String marketId) {
        this.marketId = marketId;
    }

    public void clear() {
        intraSystemImports.clear();
        intraSystemExports.clear();
        interSystemImports.clear();
        interSystemExports.clear();
    }
}

class SupplyEntry {
    public String commodityId;
    public int quantity;
    public String marketId;
    public String marketName;
    public String factionId;
    public List<TradeRecord> tradeRecords = new ArrayList<>();

    public SupplyEntry() {}

    public SupplyEntry(String commodityId, int quantity, String marketId, String marketName, String factionId) {
        this.commodityId = commodityId;
        this.quantity = quantity;
        this.marketId = marketId;
        this.marketName = marketName;
        this.factionId = factionId;
    }
}

class DemandEntry {
    public String commodityId;
    public int quantity;
    public String marketId;
    public String marketName;
    public String factionId;
    public List<TradeRecord> tradeRecords = new ArrayList<>();

    public DemandEntry() {}

    public DemandEntry(String commodityId, int quantity, String marketId, String marketName, String factionId) {
        this.commodityId = commodityId;
        this.quantity = quantity;
        this.marketId = marketId;
        this.marketName = marketName;
        this.factionId = factionId;
    }
}
