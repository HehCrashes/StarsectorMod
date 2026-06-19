package tests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.util.IntervalUtil;

public class MySystemMusicScript implements com.fs.starfarer.api.EveryFrameScript {

    private IntervalUtil timer = new IntervalUtil(1f, 2f);
    private StarSystemAPI system;
    private float lastCheck;
    private String currentTheme;

    public MySystemMusicScript(StarSystemAPI system) {
        this.system = system;
    }

    public void advance(float amount) {
        if (system == null) return;

        timer.advance(amount);
        if (!timer.intervalElapsed()) return;

        float rep = Global.getSector().getPlayerFleet().getFaction().getRelationship("pcp");
        String theme;

        if (rep < -0.1f) {
            theme = "aoi_theme";
        } else {
            theme = "new_solar_system";
        }

        if (!theme.equals(currentTheme)) {
            currentTheme = theme;
            system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, theme);
        }
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}
