package eco;

import java.util.ArrayList;
import java.util.List;

public class PlanetTradeData {
    public String marketId;
    public int lastComputedMonth;
    public List<TradeRecord> intraSystemExports = new ArrayList<>();
    public List<TradeRecord> intraSystemImports = new ArrayList<>();
    public List<TradeRecord> interSystemExports = new ArrayList<>();
    public List<TradeRecord> interSystemImports = new ArrayList<>();

    public PlanetTradeData(String marketId) {
        this.marketId = marketId;
    }
}
