package tests.ui.plugin;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinatorialPlugin implements CustomUIPanelPlugin {
    private List<CustomUIPanelPlugin> plugins;
    public CombinatorialPlugin(CustomUIPanelPlugin... plugins) {
        this.plugins = new ArrayList<>(Arrays.asList(plugins));
    }
    public void addPlugin(CustomUIPanelPlugin plugin) {
        plugins.add(plugin);
    }
    @Override
    public void positionChanged(PositionAPI position) {
        for (CustomUIPanelPlugin p : plugins){
            p.positionChanged(position);
        }
    }
    @Override
    public void renderBelow(float alpha) {
        for (CustomUIPanelPlugin p : plugins){
            p.renderBelow(alpha);
        }
    }
    @Override
    public void render(float alpha) {
        for (CustomUIPanelPlugin p : plugins){
            p.render(alpha);
        }
    }
    @Override
    public void advance(float amount) {
        for (CustomUIPanelPlugin p : plugins){
            p.advance(amount);
        }
    }
    @Override
    public void processInput(List<InputEventAPI> events) {
        for (CustomUIPanelPlugin p : plugins){
            p.processInput(events);
        }
    }
    @Override
    public void buttonPressed(Object buttonId) {
        for (CustomUIPanelPlugin p : plugins){
            p.buttonPressed(buttonId);
        }
    }
}
