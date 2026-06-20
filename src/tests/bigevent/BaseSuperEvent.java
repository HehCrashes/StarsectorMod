package tests.bigevent;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import tests.ui.dialog.SuperEventDialogDelegate;
import tests.ui.plugin.CloseUIPanelPlugin;

import java.awt.Color;
import java.util.List;

public class BaseSuperEvent {

    public static void show(String planteName) {
        show(640f, 480f, planteName);
    }
    public static void show(float width, float height,String planteName) {
        Global.getSector().addTransientScript(new EveryFrameScript() {
            boolean done = false;
            @Override
            public void advance(float amount) {
                if (done) return;
                done = true;
                InteractionDialogAPI d = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
                if (d == null) return;
                d.showCustomVisualDialog(width, height, new SuperEventDialogDelegate(planteName));
            }
            @Override
            public boolean isDone() {
                return done;
            }
            @Override
            public boolean runWhilePaused() {
                return true;
            }
        });
    }
}
