package com.engine.jbconstructor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.engine.physics.Physics;
import com.engine.physics.PhysicsCreator;

/** holds chain body info */
public class ChainBody {
    private final SavedFileDecoder.ProjectChain chain;
    private boolean isLoaded;

    private Body chainBody;
    private Physics world;

    ChainBody(SavedFileDecoder.ProjectChain e){
        chain = e;
    }

    /** creates chain to given physics instance. */
    public void initializeChain(Physics e){
        if (!isLoaded){
            e.create(new Creator());
            world = e;
        }
    }

    /** destroys chain body. changes chain state to not loaded. */
    public void destroyChain(){
        if (isLoaded){
            if (world != null){
                world.destroyBody(chainBody);
                chainBody = null;
                world = null;
                isLoaded = false;
            }
        }
    }

    /** @return this chain info which are read from save file.*/
    public SavedFileDecoder.ProjectChain getChainInfo() {
        return chain;
    }

    /** @return box2d body of this chain. This body is always static. return null if chain was no loaded or there was something wrong with chain info. */
    public Body getBody() {
        return chainBody;
    }

    public String getName(){
        return chain.name;
    }

    /** @return true if chain body was created. false otherwise */
    public boolean isLoaded(){
        return isLoaded;
    }

//    public void createChain(Physics e){
////        BodyDef bdef = new BodyDef();
////        bdef.
////        bdef.type = BodyDef.BodyType.StaticBody;
////        FixtureDef def = new FixtureDef();
////        def.shape = new ChainShape();
////        ChainShape a = (ChainShape) def.shape;
////        a.createLoop();
//        e.createBody(new Creator());
//    }

    private class Creator implements PhysicsCreator{

        @Override
        public void createBody(World world) {
            if (!isLoaded) {
                if (chain.x.size <= 1 || chain.x.size != chain.y.size){
                    // nera chain. tiesiog jos ner. kazzkokia klaida. Arba dydziai ne tokie.
                    // arba turi viena taska, o tai per mazai sukurt chain.
                    isLoaded = true;
                }else {
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    float x = chain.x.get(0), y = chain.y.get(0);
                    bodyDef.position.set(x * Physics.pixelToWord, y * Physics.pixelToWord); // pirmasis elementa bus atskaitos taskas
                    chainBody = world.createBody(bodyDef); // sukuriam world.
                    chainBody.setUserData(ChainBody.this);
                    FixtureDef def = new FixtureDef(); // paruosiam fixture
                    ChainShape shape = new ChainShape(); // sukuriam pagrinda.
                    float[] points = new float[chain.x.size*2]; // turetu tikt ir x, ir y. *2 nes cia ir x, ir y kartu eina.
                    for (int a = 0, b = 0; a < chain.x.size; a++, b += 2){ // sudedam taskus kaip priklauso.
                        Vector2 cord = Physics.pixelToWorld(chain.x.get(a) - x, chain.y.get(a) - y);
                        points[b] = cord.x;
                        points[b + 1] = cord.y;
                    }
                    if (chain.loop){
                        shape.createLoop(points);
                    }else {
                        shape.createChain(points); // idedam taskus
                    }
                    def.shape = shape;
                    chainBody.createFixture(def); // sukuriam fixtura.
                    shape.dispose(); // nebereik sitos shape daugiau.
                }
                isLoaded = true; // isejo ar ne, bet vistiek bande, du kart nereik.
            }
        }
    }
}
