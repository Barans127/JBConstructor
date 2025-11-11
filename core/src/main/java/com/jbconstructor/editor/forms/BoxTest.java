package com.jbconstructor.editor.forms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.engine.animations.CameraSlider;
import com.engine.core.Engine;
import com.engine.ui.controls.Form;
import com.engine.jbconstructor.Constructor;
import com.engine.jbconstructor.Entity;
import com.engine.jbconstructor.Template;
import com.engine.physics.Physics;
import com.engine.root.GdxWrapper;

public class BoxTest extends Form {
    private Box2DDebugRenderer renderer;
    private Physics physics;

//    private Drawable aah;
//
//    Body e;
//
//    float widt = 1/200f, height = 1f/202f;
    private Template temp;
    private CameraSlider slider;
    private Entity main;

    public BoxTest(){
        renderer = new Box2DDebugRenderer();
        physics = new Physics(new Vector2(0, -2f));
        GdxWrapper.addDisposable(physics);
        GdxWrapper.addDisposable(renderer);

        Constructor e = new Constructor();
        e.decode("textures/export/ExportTestas.export.jbcml", true, true);
//        e.initializeResources(true);
        Template template = e.getTemplate(0);
        template.loadPhysicsBodies(physics);
        temp = template;
        setFormsTemplate(template);

        main = template.getEntity("veikejas");
        slider = new CameraSlider();
//        slider.setAccelerationTime(0);
//        slider.setFollowPointBounds(0, 0, -1, -1);
//        slider.setPointOffset(20, 20);
//        Engine p = GdxPongy.getInstance();
//        slider.setCameraBounds(-p.getScreenWidth(), -p.getScreenHeight(), p.getScreenWidth()*2, p.getScreenHeight()*2);

//        template.getEntity("juokutis").setVisible(false);
//        slider.followPoint(main.getPosition());
    }

    @Override
    protected void background() {
        super.background();
        Engine p = GdxWrapper.getInstance();
//        aah.draw(p.getBatch(), 100, 100, 300, 220);
//        ((TransformDrawable) aah).draw(p.getBatch(), 100, 100, 0, 0, 100, 150, 1f, 1f, 20);
//        p.noFill();
//        p.strokeWeight(2f);
//        p.stroke(0);
//        p.ellipse(100, 100, 5, 5);
//        temp.handle();
        p.getBatch().end();
        renderer.render(physics.getWorld(), GdxWrapper.getInstance().getAbsoluteCamera().combined);
        p.getBatch().begin();


        physics.step(); // nepamirstam steppint.
        Gdx.graphics.requestRendering(); // nes nesuka sitam projekte tai reik pasakyt, kad suktus non stop.
    }

    @Override
    public boolean beforeKeyDown(int keycode) {
//        e.applyAngularImpulse(2000000000000000000f, true);
        if (keycode == Input.Keys.R){
            Engine p = GdxWrapper.getInstance();
            OrthographicCamera cam = p.getAbsoluteCamera();
            if (cam.zoom == 1f) {
                cam.zoom = Physics.pixelToWord;
                cam.position.set(p.getScreenWidth() * Physics.pixelToWord / 2, p.getScreenHeight() * Physics.pixelToWord / 2, 0);
                cam.update();
            }else {
                cam.zoom = 1f;
                cam.position.set(p.getScreenWidth()/2, p.getScreenHeight()/2, 0);
                cam.update();
            }
            return true;
        }else if (keycode == Input.Keys.V){ // isjungiam matomuma.
            Entity e = temp.getEntity(0); // tiesiog 1, test,,,
            e.setVisible(!e.isVisible()); // sukeisim ir tiek.
        }else if (keycode == Input.Keys.F){
            slider.followPoint(main.getPosition());
        }else if (keycode == Input.Keys.W){
            slider.moveTo(90f);
        }else if (keycode == Input.Keys.S){
            slider.moveTo(-90f);
        }else if (keycode == Input.Keys.A){
            slider.moveTo(180f);
        }else if (keycode == Input.Keys.D){
            slider.moveTo(0);
        }else if (keycode == Input.Keys.N){
            slider.stop();
        }else if (keycode == Input.Keys.P){
            slider.moveToPoint(100,100);
        }else if (keycode == Input.Keys.PERIOD){
            main.getBody().applyAngularImpulse(-20f, true);
        }else if (keycode == Input.Keys.COMMA){
            main.getBody().applyAngularImpulse(20f, true);
        }
        return super.beforeKeyDown(keycode);
    }
}
