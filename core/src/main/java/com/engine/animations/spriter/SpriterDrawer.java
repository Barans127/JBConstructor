package com.engine.animations.spriter;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Loader;
import com.brashmonkey.spriter.Timeline;
import com.engine.core.Engine;
import com.engine.root.GdxPongy;

/**
 * Created by jbara on 2017-02-23.
 */

public class SpriterDrawer extends Drawer<TextureRegion> implements Disposable {
    private final Engine p = GdxPongy.getInstance();

    /**
     * Creates a new drawer based on the given loader.
     *
     * @param loader the loader containing resources
     */
    public SpriterDrawer(SpriterLoader loader) {
        super(loader);
    }

    public SpriterLoader getLoader(){
        return (SpriterLoader) loader;
    }

    @Override
    public void setLoader(Loader<TextureRegion> loader) { }

    public void setLoader(SpriterLoader loader){
        super.setLoader(loader);
    }

    public void dispose(){
        loader.dispose();
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        p.strokef(r, g, b, a);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        p.noFill();
        p.line(x1, y1, x2, y2);
    }

    @Override
    public void rectangle(float x, float y, float width, float height) {
        p.noFill();
        p.rect(x, y, width, height);
    }

    @Override
    public void circle(float x, float y, float radius) {
        p.noFill();
        p.ellipse(x, y, radius, radius);
    }

//    @Override
//    public void draw(Iterator<Timeline.Key.Object> it, Entity.CharacterMap[] maps){
//        super.draw(it, maps);
//        if (!Gdx.graphics.isContinuousRendering()){
//            Gdx.graphics.requestRendering();
//        }
//    }

    @Override
    public void draw(Timeline.Key.Object object) {
        TextureRegion e = loader.get(object.ref);
        Color system = p.getBatch().getColor();
        p.tintf(system.r, system.g, system.b, object.alpha);
        float newPivotX = (e.getRegionWidth() * object.pivot.x);
        float newX = object.position.x - newPivotX;
        float newPivotY = (e.getRegionHeight() * object.pivot.y);
        float newY = object.position.y - newPivotY;
//        p.getBatch().draw(e, object.position.x, object.position.y,0, 0, object.pivot.x, object.pivot.y, object.scale.x, object.scale.y, object.angle,
//                0, 0, e.getRegionWidth(), e.getRegionHeight(), false, false);
        p.getBatch().draw(e, newX, newY, newPivotX, newPivotY,
                e.getRegionWidth(), e.getRegionHeight(),
                object.scale.x, object.scale.y,
                object.angle);
//        p.getBatch().draw(e, object.position.x, object.position.y, object.pivot.x, object.pivot.y, object.);
        p.noTint();
    }
}
