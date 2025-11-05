package com.engine.jbconstructor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.physics.Physics;
import com.engine.physics.PhysicsCreator;

/** class which holds body for {@link Entity} class. */
public class PolygonBody {
    /* shapes of this polygon body. */
//    private Array<SavedFileDecoder.PhysicsFixtureShapes> shapes;
    private SavedFileDecoder.PhysicsShapesGroup physicsShapesGroup;

    /* bendri. */
    private Body body;
    private Physics world;
    private Entity owner;

    // physics parametrai.
    private BodyDef.BodyType bodyType;
//    private boolean isOriginMiddle;
//    private Vector2 bodyOrigin;
    private boolean fixedRotation;
    private boolean bullet;
    private float linearDamping;
    private float angularDamping;
    private float gravityScale;
    private float angularVelocity;
    private Vector2 linearVelocity;

    private boolean isLoaded = false; // ar uzloadinta.
    private Creator creator; // kad nebutu double call.

    PolygonBody(SavedFileDecoder.PhysicsShapesGroup group, Entity owner) {
//        this.shapes = new Array<>();
//        if (shapes != null){
//            this.shapes.addAll(shapes);
//        }
        this.physicsShapesGroup = group;
        this.owner = owner;

        linearVelocity = new Vector2();

        reset();
    }

    /* fiziku parametrai */
    /// getters.

    /** Parameter used to create body. You can modify it or you can reset it to starting point via method: {@link #reset()} */
    public boolean isFixedRotation(){
        return fixedRotation;
    }

    /** Parameter used to create body. You can modify it or you can reset it to starting point via method: {@link #reset()} */
    public boolean isBullet(){
        return bullet;
    }

    /** Parameter used to create body. You can modify it or you can reset it to starting point via method: {@link #reset()} */
    public float getLinearDamping(){
        return linearDamping;
    }

    /** Parameter used to create body. You can modify it or you can reset it to starting point via method: {@link #reset()} */
    public float getAngularDamping(){
        return angularDamping;
    }

    /** Parameter used to create body. You can modify it or you can reset it to starting point via method: {@link #reset()} */
    public float getGravityScale(){
        return gravityScale;
    }

    /** Parameter used to create body. You can modify it or you can reset it to starting point via method: {@link #reset()} */
    public float getAngularVelocity(){
        return angularVelocity;
    }

    /** Parameter used to create body. You can modify it or you can reset it to starting point via method: {@link #reset()} */
    public Vector2 getLinearVelocity(){
        return linearVelocity;
    }

    /// setters

    /** In order to take effect you must recreate body. */
    public void setFixedRotation(boolean fixedRotation){
        this.fixedRotation = fixedRotation;
    }

    /** In order to take effect you must recreate body. */
    public void setBullet(boolean bullet){
        this.bullet = bullet;
    }

    /** In order to take effect you must recreate body. */
    public void setLinearDamping(float linearDamping){
        this.linearDamping = linearDamping;
    }

    /** In order to take effect you must recreate body. */
    public void setAngularDamping(float angularDamping){
        this.angularDamping = angularDamping;
    }

    /** In order to take effect you must recreate body. */
    public void setGravityScale(float gravityScale){
        this.gravityScale = gravityScale;
    }

    /** In order to take effect you must recreate body. */
    public void setAngularVelocity(float angularVelocity){
        this.angularVelocity = angularVelocity;
    }

    /** In order to take effect you must recreate body. */
    public void setLinearVelocity(float x, float y){
        linearVelocity.set(x, y);
    }

    // veikimas

    /** Resets all physics to save file level. NOTE: physics must be recreated to take effect.
     * Body origin is saved in {@link Entity} itself. It is not reset! */
    public void reset(){
        if (physicsShapesGroup != null) {
            bodyType = physicsShapesGroup.bodyType;
            fixedRotation = physicsShapesGroup.fixedRotation;
            bullet = physicsShapesGroup.bullet;
            linearDamping = physicsShapesGroup.linearDamping;
            angularDamping = physicsShapesGroup.angularDamping;
            gravityScale = physicsShapesGroup.gravityScale;
            angularVelocity = physicsShapesGroup.angularVelocity;
            linearVelocity.set(physicsShapesGroup.linearVelocity);
        }
    }

    /** creates physics bodies. Adds itself to body user data. */
    void initializeShapes(Physics e){
        if (!isLoaded){
            if (physicsShapesGroup != null && creator == null) { // neapsimoka kurt, jei nera fixturu.
                // pasizymim, kad creator idetas, kad nekurtu du kart.
                e.create(creator = new Creator());
                world = e;
            }
        }
    }

    /** Destroys body. Changes physics state to not loaded. */
    void destroyBody(){
        if (isLoaded && world != null){
            world.destroyBody(body);
            body = null;
            world = null;
            isLoaded = false;
        }

    }

    /* gavimas */

    public Physics getWorld() {
        return world;
    }

    public Entity getOwner() {
        return owner;
    }

    /** @return physics body. if body is not loaded yet then null is returned. */
    public Body getBody() {
        return body;
    }

    public boolean isLoaded(){
        return isLoaded;
    }

    /** @return info about fixture shapes. This info was read from save file. If this body doesn't have physics then null is returned. */
    public Array<SavedFileDecoder.PhysicsFixtureShapes> getShapesInfo() {
        return physicsShapesGroup == null ? null : physicsShapesGroup.shapes;
    }

    /** Info about physics for this object. If there is no physics then null. */
    public SavedFileDecoder.PhysicsShapesGroup getPhysicsShapesGroup() {
        return physicsShapesGroup;
    }

    /** Does this polygon body has shapes. */
    public boolean hasShapes(){
//        return shapes.size > 0;
        return physicsShapesGroup != null;
    }

    private class Creator implements PhysicsCreator{

        private void readFixture(SavedFileDecoder.PhysicsFixtureShapes e, FixtureDef def){
            def.density = e.density;
            def.friction = e.friction;
            def.restitution = e.restitution;
            def.filter.categoryBits = e.categoryBits;
            def.filter.groupIndex = e.groupIndex;
            def.filter.maskBits = e.maskBits;
            def.isSensor = e.isSensor;
        }

        @Override
        public void createBody(World world) { // body bus tik vienas, o fixturu daug gali but.
            if (!isLoaded){ // du kart nereik.
                if (physicsShapesGroup == null){
                    return;
                }
                if (owner.getResource() == null){
                    Engine.getInstance().setError("PolygonBody: Entity doesn't have resource! Initialize resources before creating physics!", ErrorMenu.ErrorType.ControlsError);
                    return;
                }
                float realWidth = owner.getResource().getMinWidth();
                float realHeight = owner.getResource().getMinHeight();
                if (realWidth == 0 || realHeight == 0){
                    Engine.getInstance().setError("PolygonBody", "Width or height of resource is 0! It cannot be 0!");
                    return;
                }
                float widthRatio = owner.getWidth() / realWidth;
                float heightRatio = owner.getHeight() / realHeight; // ratio skirtas skirtingiems dydziams. yra bendras, o su ratio galima lengvai suzaist.
                if (owner.getWidth() == 0 || owner.getHeight() == 0){
                    Engine.getInstance().setError("PolygonBody", "Entity has width and/or height 0! Cannot create physics with 0 value!");
                    return;
                }

                // nuskaitom body def.
                BodyDef bodyDef = new BodyDef(); // bendras body def.
//                bodyDef.type = owner.getBodyType();
                bodyDef.type = bodyType;
                bodyDef.angle = owner.getAngle();
                bodyDef.active = owner.isVisible(); // jeigu nematomas, tai kam dar det objekta uz kurio kazkas uzkliut gali.
                bodyDef.fixedRotation = fixedRotation;
                bodyDef.bullet = bullet;
                bodyDef.linearDamping = linearDamping;
                bodyDef.angularDamping = angularDamping;
                bodyDef.gravityScale = gravityScale;
                bodyDef.angularVelocity = angularVelocity;
                bodyDef.linearVelocity.set(linearVelocity);

                float startX, startY;
                Vector2 pos = owner.getPosition();
                if (owner.isBodyOriginMiddle()){ // body pozicijos nutatymas.
                    startX = owner.getWidth() / 2; // nieko ypatingo, viskas vidury.
                    startY = owner.getHeight() / 2;
                }else {
                    Vector2 origin = owner.getBodyOrigin();
                    float angle = owner.getAngle(); // pozicija perskaiciuot tik jei body pasuktas.
                    if (angle != 0) { // perskaiciuojam pozicija, nes tikrai netiks pagal origina ir neatitiks isvaizdos pagal JBConstructoriu.
                       float middleX = owner.getWidth()/2 + pos.x;
                       float middleY = owner.getHeight()/2 + pos.y; // susirandam vidurio taska
                       float ox = origin.x + pos.x, oy = origin.y + pos.y; // origin taskas.
                       float rot = MathUtils.atan2(oy - middleY, ox - middleX) + angle; // kampas tarp ju ir posukis
                       float dist = MoreUtils.dist(ox, oy, middleX, middleY); // ilgis
                       float rx = MathUtils.cos(rot) * dist + middleX; // naujas origin taskas
                       float ry = MathUtils.sin(rot) * dist + middleY;
                        // naujo origin tasko nenaudosim. Ji naudosim tik perskaiciuot cord.
                       pos.set(rx - origin.x, ry - origin.y); // perskaiciuojam body pozicija.

                    }
                    startX = origin.x; // origin paliekam sena
                    startY = origin.y;
                }
                bodyDef.position.set((pos.x+startX) * Physics.pixelToWord, (pos.y+startY)* Physics.pixelToWord);


                FixtureDef def = new FixtureDef(); // bendras fixutre def.
                body = world.createBody(bodyDef); // sukuriam kuna.
                body.setUserData(PolygonBody.this); // bet kada gales pasikeist. Tiesiog, kad zinotu, kad sitas.
                for (int a = 0; a < physicsShapesGroup.shapes.size; a++){ // fixturu kurimas.
                    SavedFileDecoder.PhysicsFixtureShapes e = physicsShapesGroup.shapes.get(a);
                    switch (e.type){
                        case SavedFileDecoder.POLYGON: {
                            PolygonShape shape = new PolygonShape();
                            float[] pol = new float[e.x.size * 2];
                            for (int k = 0, b = 0; k < e.x.size; k++, b += 2) {
                                Vector2 np = Physics.pixelToWorld(e.x.get(k)*widthRatio - startX, e.y.get(k)*heightRatio - startY);
                                pol[b] = np.x;
                                pol[b + 1] = np.y;
                            }
                            shape.set(pol);
                            def.shape = shape;
                            readFixture(e, def);
                            body.createFixture(def);
                            shape.dispose(); // nebereik.
                            break;
                        }
                        case SavedFileDecoder.CHAIN: {
                            ChainShape shape1 = new ChainShape();
                            float[] points = new float[e.x.size * 2];
                            for (int k = 0, b = 0; k < e.x.size; k++, b += 2) {
                                Vector2 np = Physics.pixelToWorld(e.x.get(k)*widthRatio - startX, e.y.get(k)*heightRatio - startY);
                                points[b] = np.x;
                                points[b + 1] = np.y;
                            }
                            shape1.createChain(points);
                            def.shape = shape1;
                            readFixture(e, def);
                            body.createFixture(def);
                            shape1.dispose(); // naikinam.
                            break;
                        }
                        case SavedFileDecoder.CIRCLE: {
                            CircleShape shape = new CircleShape();
                            shape.setRadius(Math.min(widthRatio, heightRatio)*(e.radius/2)*Physics.pixelToWord);
                            shape.setPosition(Physics.pixelToWorld(e.x.get(0)*widthRatio - startX, e.y.get(0)*heightRatio - startY));
                            def.shape = shape;
                            readFixture(e, def);
                            body.createFixture(def);
                            shape.dispose(); // baba
                            break;
                        }
                        case SavedFileDecoder.EDGE: {
                            EdgeShape shape = new EdgeShape();
                            float [] cc = new float[4];
                            Vector2 pp = Physics.pixelToWorld(e.x.get(0)*widthRatio - startX, e.y.get(0)*heightRatio - startY);
                            cc[0] = pp.x;
                            cc[1] = pp.y;
                            pp = Physics.pixelToWorld(e.x.get(1)*widthRatio - startX, e.y.get(1)*heightRatio - startY);
                            cc[2] = pp.x;
                            cc[3] = pp.y; // buvo palikta klaida... imtas 2..
                            shape.set(cc[0], cc[1], cc[2], cc[3]);
                            def.shape = shape;
                            readFixture(e, def);
                            body.createFixture(def);
                            shape.dispose(); // visada trinam lauk.
                            break;
                        }
                    }
                }

                isLoaded = true;
                creator = null; // pametam.
            }
        }
    }
}
