package tests.ui.plugin;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.List;
import java.util.function.Supplier;

public class CloseUIPanelPlugin implements CustomUIPanelPlugin {
    private Supplier<CustomVisualDialogDelegate.DialogCallbacks> supplier;
    public CloseUIPanelPlugin(Supplier<CustomVisualDialogDelegate.DialogCallbacks> supplier) {
        this.supplier = supplier;
    }
    @Override
    public void positionChanged(PositionAPI position) {}
    @Override
    public void renderBelow(float opacity) {}
    @Override
    public void render(float opacity) {}
    @Override
    public void advance(float amount) {}
    @Override
    public void processInput(List<InputEventAPI> events) {}
    @Override
    public void buttonPressed(Object buttonId) {
        CustomVisualDialogDelegate.DialogCallbacks cb = supplier != null ? supplier.get() : null;
        if ("close".equals(buttonId) && cb != null) {
            cb.dismissDialog();
        }
    }
}
