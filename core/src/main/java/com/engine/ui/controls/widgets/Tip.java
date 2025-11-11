package com.engine.ui.controls.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.Engine;
import com.engine.core.Resources;
import com.engine.ui.controls.Balloon;
import com.engine.ui.controls.Field;
import com.engine.ui.controls.Control;
import com.engine.root.GdxWrapper;
import com.engine.root.TextContainer;

/**
 * Simple controller tip.
 */
public class Tip extends Balloon {
    private final TextContainer controller;
    private float maxW, maxH, oldAlphaText;
    private Drawable background;
    private Color tint;

    public Tip(){
        this("This is Tip.");
    }

    public Tip(String text){
        this(text, Resources.getPropertyFloat("TipTextSize", 60), GdxWrapper.getInstance().getFont(),
                Resources.getPropertyInt("TipTextColor", 0xFFFFFFFF), null, GdxWrapper.getInstance().getScreenWidth(),
                GdxWrapper.getInstance().getScreenHeight(), Align.left, Align.top);
    }

    public Tip(String text, float textSize, BitmapFont font, int textColor, Drawable background,
               float maxWidth, float maxHeight, int horizontalAlign, int verticalAlign){
        this.background = background;
        maxW = maxWidth;
        maxH = maxHeight;
        controller = new TextContainer();
        controller.setTextSize(textSize);
        controller.setStyle(font);
        controller.setTextColor(textColor);
        controller.setTextAlign(horizontalAlign, verticalAlign);
        oldAlphaText = controller.getTextColor().a;
        setText(text);
        tint = new Color(1, 1, 1, 1);
    }

    /**
     * Do not change color directly. Use <code>setTextColor()</code>.
     * @return text controller used to draw text on tip.
     */
    public TextContainer getTextController(){
        return controller;
    }

    public void setTextColor(int color){
        controller.setTextColor(color);
        oldAlphaText = controller.getTextColor().a;
    }

    public void setText(String text){
        autoSize(text);
        this.controller.setText(text);
    }

    public void show(float x, float y){
        setPosition(x, y);
        show();
    }

    public void show(Control e){
        show(e, 0, 0);
    }

    public void show(Control e, float offsetX, float offsetY){
        Vector2 point;
        if (e instanceof Field){
            point = ((Field) e).getMiddlePoint();
        }else {
            point = e.getPosition();
        }
        show(point.x + offsetX, point.y + offsetY);
    }

    @Override
    protected void draw(float x, float y, float width, float height, float offsetX, float offsetY) {
        Engine p = GdxWrapper.getInstance();
        p.tint(tint);
        background.draw(p.getBatch(), x, y, width, height);
        controller.drawText(x, y);
        p.noTint();
    }

    @Override
    protected void showing(float progress) {
        super.showing(progress);
        animateTextColor(progress);
    }

    @Override
    protected void hiding(float progress) {
        super.hiding(progress);
        animateTextColor(progress);
    }

    private void animateTextColor(float progress){
        Color a = controller.getTextColor();
        a.a = progress * oldAlphaText;
        controller.setTextColor(a);
    }

    private void autoSize(String text){
        BitmapFont font = this.controller.getFontScaled();
        Engine p = GdxWrapper.getInstance();
        float width = Math.min(p.textWidth(text, font), maxW);
        float height = Math.min(p.textHeight(), maxH);
        setSize(width, height);
        controller.setBounds(width, height);
    }
}
