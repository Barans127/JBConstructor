package com.jbconstructor.main.forms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.Engine;
import com.engine.interfaces.controls.Form;
import com.engine.root.GdxPongy;

public class SpriterTest extends Form {
    private SpriterDrawable ex;

    public SpriterTest(){
        ex = new SpriterDrawable("textures/zvng/test.psd", "wteResources.txt");



        setCustomCameraSettings(0, 0, 1f);
    }

    @Override
    protected void background() {
        super.background();
        Engine p = GdxPongy.getInstance();

//        p.stroke(0);
//        p.strokeWeight(3f);
//        drawer.drawBones(player.getFirstPlayer());
        p.stroke(0);
        p.noFill();

        p.line(-640, 0, 640, 0);
        p.line(0, -320, 0, 320);

        ex.draw(p.getBatch(), 0, 0, 200, 250);
        Gdx.graphics.requestRendering();
    }

    @Override
    public boolean beforeKeyDown(int keycode) {
        if (keycode == Input.Keys.PLUS){
            GdxPongy.setWorldSpeed(GdxPongy.getWorldSpeed()*1.1f);
        }else if (keycode == Input.Keys.MINUS){
            GdxPongy.setWorldSpeed(GdxPongy.getWorldSpeed()*0.9f);
        }else if (keycode == Input.Keys.R){ // topo run
            ex.switchAnimations(1, 1);
//            switchAnimations(1, 1);
        }else if (keycode == Input.Keys.I){ // idle
            ex.switchAnimations(0, 0.5f);
//            switchAnimations(0, 0.5f);
        }else if (keycode == Input.Keys.J){ // jump
            ex.switchAnimations(2, 1);
//            switchAnimations(2, 1);
        }
        return super.beforeKeyDown(keycode);
    }
}
