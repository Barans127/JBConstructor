package com.jbconstructor.main.root;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.core.ErrorMenu;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Field;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.listeners.MainDraw;

import java.util.List;

/**
 * Pies staciakampį, leis multiselectint ir t.t. Naudos lygiagreciai su FieldMover (veiks arba sitas, arba tas moveris).
 */

public class Selector extends Interface {
    private boolean isDrawing, isPressed;

    private Drawable back;
    private int tintColor;
    private DrawRectangle draw;

    private float x, y, width, height;

    private List<Interface> controls;
    private SelectionListener listener;

    public Selector(){
        this(new SelectorStyle());
    }

    public Selector(InterfaceStyle st){
        super(st);
        Color converter = new Color();
        Color.rgb888ToColor(converter, Resources.getPropertyInt("selectorColor", 0x0000FF));
        converter.a = 85f/255f;
        tintColor = Color.argb8888(converter);

//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        back = Resources.getDrawable("whiteSystemColor");
        draw = new DrawRectangle();
        setFocusable(false);
    }

    /** ARGB format */
    public void setBoxColor(int color){
        tintColor = color;
    }

    public void setSelectionListener(SelectionListener e){
        listener = e;
    }

    private void updateControlsList(){
        if (v == null){
            p.setError("Selector: InterfaceController cannot be null", ErrorMenu.ErrorType.ControlsError);
            release();
            return;
        }
        controls = v.getControls();
        if (controls == null){ // mest klaida ar ignoruot?
            p.setError("Selector: Cannot access controls", ErrorMenu.ErrorType.ControlsError);
            release();
        }
    }

    private void checkControlsLocation(){
        float startX, startY, endX, endY; // cord susigaudimas.
        if (width < 0) {
            startX = x + width;
            endX = x;
        }else {
            startX = x;
            endX = x + width;
        }
        if (height < 0){
            startY = y + height;
            endY = y;
        } else {
            startY = y;
            endY = y + height;
        }
        for (int a = controls.size() - 1; a >= 0; a--){ // nuo galo
            Interface e = controls.get(a);
            // turim tikrint pagal frustum ir t.t.
            if (e == this || !e.isVisibleOnScreen()){ // save ignoruos. arba neveiklius
                continue;
            }
            Vector2 pos;
            if (e instanceof Field){
                pos = ((Field) e).getMiddlePoint();
            }else
                pos = e.getPosition();
            // pozicijos nustatymas. kad nedarytu nesamoniu su skirtingo view controlem.
            int positioning = getPositioning() == Window.relativeView ? getController().getPositioning() : getPositioning();
            int clientPositioning = e.getPositioning() == Window.relativeView ? e.getController().getPositioning() : e.getPositioning();
//            float offsetX = 0, offsetY = 0;
            float ex = pos.x, ey = pos.y;
            if (positioning != clientPositioning){
//                Vector3 camera = p.getAbsoluteCamera().position;
//                float x = camera.x - p.getScreenWidth()/2, y = camera.y - p.getScreenHeight()/2;
//                if (positioning != Window.absoluteView) { // fixed, controles absolute.
//                    x = -x;
//                    y = -y;
//                }
//                offsetX = x;
//                offsetY = y;
                if (positioning == Window.fixedView){ // resizer fixed, kontrole absolute. // nelabai sitas bus
                    Vector3 cord = p.worldToScreenCoords(pos.x, pos.y);
                    cord = p.screenToFixedCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                    ex = cord.x;
                    ey = cord.y;
                }else { // resizer absolute, kontrole fixed.
                    Vector3 cord = p.fixedToScreendCoords(pos.x, pos.y);
                    cord = p.screenToWorldCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                    ex = cord.x;
                    ey = cord.y;
                }
            }

            if (ex >= startX && ex < endX
                    && ey >= startY && ey < endY){
                // kazka dayt.... kas bus jei viduj.
                if (listener != null)
                    listener.onSelect(e);
            }else if (listener != null){
                // interface isorej...
                listener.onDiselect(e);
            }
        }
    }

    @Override
    public void setEnabled(boolean enable) { // lyigiai taip pat kaip su FieldMover. ti kas ce per fujne????
        setVisible(enable);
    }

    @Override
    protected void isvaizda(float x, float y) {} // ner vaizdo, bent jau cia.

    @Override
    public void release() {
        isDrawing = false;
        isPressed = false;
        controls = null;
    }

    @Override
    protected void auto() {}

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (button == Input.Buttons.LEFT){
            this.x = x;
            this.y = y;
            isPressed = true;
            if (listener != null)
                listener.massDiselection();
            return true;
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (isPressed){
            if (!isDrawing){
                isDrawing = true;
                updateControlsList();
                TopPainter.addPaintOnTop(draw, false);
            }
            width = x - this.x;
            height = y - this.y;
            checkControlsLocation();
            return true;
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (isPressed){
            release();
            return true;
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (isPressed){
            release();
            return true;
        }
        return false;
    }

    public interface SelectionListener{
        public void onSelect(Interface e);
        public void onDiselect(Interface e);
        public void massDiselection();
    }

    private class DrawRectangle implements MainDraw{

        @Override
        public void draw() {
            if (isDrawing){
                p.tint(tintColor);
                back.draw(p.getBatch(), x, y, width, height);
                p.noTint();
            }else {
                TopPainter.removeTopPaint(this);
            }
        }

        @Override
        public boolean drop(int reason) {
            return false;
        }
    }

    @Override
    public InterfaceStyle getStyle() {
        return null;
    }

    public static class SelectorStyle extends InterfaceStyle{
        @Override
        public Interface createInterface() {
            return null;
        }
    }
}
