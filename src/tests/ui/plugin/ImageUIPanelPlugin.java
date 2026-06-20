package tests.ui.plugin;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class ImageUIPanelPlugin implements CustomUIPanelPlugin {
    private SpriteAPI sprite;
    private Vector2f render,panel;
    private Vector2f screenSpace;
    private Vector2f lastScreenSpace;

    public ImageUIPanelPlugin(String category, String key, Vector2f panel) {
        sprite = Global.getSettings().getSprite(category, key);
        float scale = Math.min(panel.x / sprite.getWidth(), panel.y / sprite.getHeight());
        sprite.setSize(sprite.getWidth() * scale, sprite.getHeight() * scale);
        this.panel = panel;
        this.screenSpace = new Vector2f();
        this.lastScreenSpace = new Vector2f(-1,-1);
        this.render = new Vector2f();
    }

    @Override
    public void positionChanged(PositionAPI position) {
        screenSpace.x = position.getX();
        screenSpace.y = position.getY();
    }
    @Override
    public void renderBelow(float alpha) {
        sprite.render(render.x,render.y);
    }
    @Override
    public void render(float opacity) {
    }
    @Override
    public void advance(float amount) {
        if(screenSpace.x != lastScreenSpace.x || screenSpace.y != lastScreenSpace.y){
            render.x = screenSpace.x - 5f + (panel.x - sprite.getWidth())/2;
            render.y = screenSpace.y + (panel.y - sprite.getHeight())/2;

            lastScreenSpace.x = screenSpace.x;
            lastScreenSpace.y = screenSpace.y;
        }
    }
    @Override
    public void processInput(List<InputEventAPI> events) {
    }
    @Override
    public void buttonPressed(Object buttonId) {
    }
}
