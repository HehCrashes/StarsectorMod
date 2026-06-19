package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import org.json.JSONArray;
import org.json.JSONObject;
import tests.NewSolar;

import java.util.HashSet;
import java.util.Set;

public class CoreCrackingModPlugin extends BaseModPlugin {
    public static Set<String> blacklistIds = new HashSet<>();
    @Override
    public void onApplicationLoad() throws Exception {
        Global.getSettings().getScriptClassLoader();

        JSONArray data = Global.getSettings().getMergedSpreadsheetData("id", "data/config/genocideBlacklist.csv");
        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            blacklistIds.add(row.getString("id"));
        }
    }


    @Override
    public void onNewGame() {
        SectorAPI sector = Global.getSector();
        (new NewSolar()).generate(sector);
    }
}
