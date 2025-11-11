package com.engine.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.core.Engine;
import com.engine.core.Resources;
import com.engine.ui.controls.Form;
import com.engine.ui.controls.TopPainter;
import com.engine.ui.controls.Window;
import com.engine.ui.listeners.MainDraw;

/** Animation switch between Engine Windows. Usually between Forms.
 * This is fade out switch. Means forms will be switched by fading one out and fading one in.*/
public class FadeAway implements SwitchAnimation, MainDraw {
    private int newState;
    private final Engine p = Engine.getInstance();
    //	private boolean start, inform;
    private boolean start;
    private float filler;

    // musu drawable.
    private final Drawable white;

    // kad ignorintu viska, kai vyksta perjungimo animacija.
    private Window currentForm;

    // -3 busena yra zaidimo langu perjungimo animacija.
    public FadeAway() {
        white = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
    }

    // keis zaidimo busena su fadeaway animacija.
    @Override
    public void changeState(int index) {
        newState = index;
//		oldState = Engine.getActiveFormId();
//		Engine.changeState(animationIndex);
        start = true;
        TopPainter.addPaintOnTop(this);
//		inform = true;
        filler = 0;

        // jei kartais labai greit viskas vykst.
        if (currentForm != null){
            if (currentForm instanceof Form){
                ((Form) currentForm).removeTopItem(this);
            }
            currentForm = null;
        }
        // padarom, kad ignorintu paspaudimus.
        currentForm = p.getActiveForm();
        if (currentForm instanceof Form){
            ((Form) currentForm).addOnTop(this);
        }
    }

    @Override
    public void action() {}

    @Override
    public void fixAction() {}

    @Override
    public void draw() {
        float expectedTimeFrame = 60f;
        final float speed = 15 * expectedTimeFrame; // animacijos greitis.
        if (start) {
            if (filler >= 255) {
                start = false;
                filler = 255;
                p.changeState(newState, true);
                if (currentForm instanceof Form){
                    ((Form) currentForm).removeTopItem(this);
                }
//				TopPainter.addPaintOnTop(this); // nes po pakeitimo pameta...
            } else {
                filler += speed * Gdx.graphics.getDeltaTime();
            }
        } else if (filler > 0) {
            filler -= speed * Gdx.graphics.getDeltaTime();
        } else {
            TopPainter.removeTopPaint(this);
            currentForm = null;
        }

        // piesiam
        if (white != null){
            p.tint(0x98, 0x93, 0x93, (int) filler);
            float offsetX = (p.getPreferredScreenWidth() - p.getScreenWidth())/2;
            float offsetY = (p.getPreferredScreenHeight() - p.getScreenHeight())/2;
            white.draw(p.getBatch(), offsetX, offsetY, p.getScreenWidth(), p.getScreenHeight());
            p.noTint();
        }else {
            // nera sisteminio image?? Sukame kitu budu.
            p.noStroke();
            p.fill(0x98, 0x93, 0x93, (int) filler);
            p.rect(0, 0, p.getScreenWidth(), p.getScreenHeight());
        }
    }

    @Override
    public boolean drop(int reason) {
        return true; // visada turi veikt.
    }
}
