package com.engine.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.Resources;
import com.engine.root.GdxWrapper;

/** box2d physics. There are two ways to determine collision. One use global listener {@link ContactListener} provided from box2d.
 * Other, set body user data with {@link TouchEvent} instance. If you set user data on body with {@link TouchEvent} instance then
 * listener only triggers when this body collides. */
public class Physics implements Disposable{
	private final World world;
	/** Multiply word coordinates to this to get absolute camera coordinates. */
	public static final float worldToPixel = Resources.getPropertyFloat("worldRatio", 40f);
	/** Multiply absolute camera coordinates to this to get world coordinates. */
	public static final float pixelToWord = 1 / worldToPixel;

//	private BodyType type = BodyType.StaticBody; // default parametrai kuno kurimui.
//	private float restitution = 1f, friction = 0, density = 0.5f, angle;
	private static final Vector2 temp = new Vector2(); // temp vector, del position ir t.t.
//	private boolean isStepping; // jei world step daro.
	private final Array<Body> destroyBody; // kunu kurimas ir naikinimas
	private final Array<Joint> destroyJoint;
	private final Array<PhysicsCreator> creator;

	// default para
	private int velocityIterations = Resources.getPropertyInt("velocityIterations", 8);
	private int positionIterations = Resources.getPropertyInt("positionIterations", 3);
//	private float CONSTANT_STEP;
//
//	private float accumulator = 0;
//	private float alpha = 0;

	private ContactListener contactListener; // listener

	/** Converts absolute camera coordinates to world coordinates
	 * @return world coordinates */
	public static Vector2 pixelToWorld(float x, float y){
		return temp.set(x * pixelToWord, y * pixelToWord);
	}

	/** Converts Box2D world coordinates to absolute camera coordinates
	 * @return camera coordinates */
	public static Vector2 worldToPixel(Vector2 e){
		return temp.set(e.x * worldToPixel, e.y * worldToPixel);
	}

	/** Converts Box2D world coordinates to absolute camera coordinates
	 * @return camera coordinates */
	public static Vector2 worldToPixel(float x, float y){
		return temp.set(x * worldToPixel, y * worldToPixel);
	}

	/** Creates world without gravity. You can always change gravity later. */
	public Physics(){
		this(new Vector2(0, 0));
	}

	/** Creates Box2D world with given gravity. You can always change gravity later. */
	public Physics(Vector2 gravity){
//		CONSTANT_STEP = MoreUtils.abs(Resources.getPropertyFloat("constant_step", 1/60f));
//		CONSTANT_STEP = 1/(GdxPongy.getFrameRate()*1f);
		world = new World(gravity, true);
		world.setContactListener(new ContactListener(){
			@Override
			public void beginContact(Contact contact) {
				Object a = contact.getFixtureA().getBody().getUserData();
				if (a instanceof TouchEvent){
					((TouchEvent) a).beginContact(contact.getFixtureB().getBody(), contact);
				}
				Object b = contact.getFixtureB().getBody().getUserData();
				if (b instanceof TouchEvent){
					((TouchEvent) b).beginContact(contact.getFixtureA().getBody(), contact);
				}
				if (contactListener != null)
					contactListener.beginContact(contact);
			}

			@Override
			public void endContact(Contact contact) {
				Object a = contact.getFixtureA().getBody().getUserData();
				if (a instanceof TouchEvent){
					((TouchEvent) a).endContact(contact.getFixtureB().getBody(), contact);
				}
				Object b = contact.getFixtureB().getBody().getUserData();
				if (b instanceof TouchEvent){
					((TouchEvent) b).endContact(contact.getFixtureA().getBody(), contact);
				}
				if (contactListener != null)
					contactListener.endContact(contact);
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				Object a = contact.getFixtureA().getBody().getUserData();
				if (a instanceof SolveEvent){
					((SolveEvent) a).preSolve(contact.getFixtureB().getBody(), contact, oldManifold);
				}
				Object b = contact.getFixtureB().getBody().getUserData();
				if (b instanceof SolveEvent){
					((SolveEvent) b).preSolve(contact.getFixtureA().getBody(), contact, oldManifold);
				}
				if (contactListener != null)
					contactListener.preSolve(contact, oldManifold);
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				Object a = contact.getFixtureA().getBody().getUserData();
				if (a instanceof SolveEvent){
					((SolveEvent) a).postSolve(contact.getFixtureB().getBody(), contact, impulse);
				}
				Object b = contact.getFixtureB().getBody().getUserData();
				if (b instanceof SolveEvent){
					((SolveEvent) b).postSolve(contact.getFixtureA().getBody(), contact, impulse);
				}
				if (contactListener != null)
					contactListener.postSolve(contact, impulse);
			}
		});
		destroyBody = new Array<>();
		destroyJoint = new Array<>();
		creator = new Array<>();
	}

	/** Steps world with delta from <code>GdxPongy.getDelta()</code>. With this delta, world speed can be manipulated with
	 * {@link GdxWrapper#setWorldSpeed(float)} */
	public void step(){
		step(GdxWrapper.getDelta());
	}

	/** Steps world with given delta and default iteration settings.
	 * Usually those settings are:
	 * VelocityIterations: 8
	 * positionIterations: 3
	 * You can change those settings in config file or at runtime with methods {@link #setVelocityIterations(int)}, {@link #setPositionIterations(int)}.*/
	public void step(float delta){
		step(delta, velocityIterations, positionIterations);
	}

	/** Steps world with given settings. Creates and destroys bodies and/or joints after step. */
	public void step(float delta, int velocityIterations, int positionIterations){
		synchronized (world) {
//			isStepping = true;
			float frameTime = Math.min(delta, 0.25f); // max 4fps, jei kils daugiau, suletes simuliacija. avoiding spiral death.
//			accumulator += frameTime;
//			while (accumulator >= CONSTANT_STEP) {
//				world.step(CONSTANT_STEP, velocityIterations, positionIterations);
			world.step(frameTime, velocityIterations, positionIterations); // constant step - flickering.
//				accumulator -= CONSTANT_STEP;
//			}
//			alpha = accumulator/CONSTANT_STEP;
//			isStepping = false;
			flushLists();
		}
	}

	/** @return Box2D world instance. */
	public World getWorld(){
		return world;
	}

	// išvalys pasaulį.
	/** clears world. Deletes all bodies and joints. */
	public void clearWorld(){
//		if (isStepping){
//			GdxPongy.getInstance().setError("World cannot be cleared while world is stepping", ErrorType.GameError);
//			return;
//		}
//		destroyBody.clear();
//		destroyJoint.clear();
		Array<Body> e = new Array<>();
		Array<Joint> j = new Array<>();
		world.getBodies(e);
		world.getJoints(j);
		synchronized (destroyJoint){
			destroyJoint.addAll(j);
		}
		synchronized (destroyBody){
			destroyBody.addAll(e);
		}
//		for (int a = 0; a < e.size; a++){
//			world.destroyBody(e.get(a));
//		}
		e.clear();
		j.clear();

		flushLists();
	}

	/** All waiting objects will be called if world is not stepping. All joints and bodies added to list will be destroyed. */
	public void flushLists(){
		if (!world.isLocked()){
			for (int a = destroyJoint.size - 1; a >= 0; a--) { // pirmiausiai naikinsim jointus. Pries body naikinima turi but sunaikinami joints.
				Joint e = destroyJoint.get(a);
				destroyJoint.removeIndex(a);
				world.destroyJoint(e);
			}
			for (int a = destroyBody.size - 1; a >= 0; a--) { // kai sunaikinti joint, galima naikint ir pacius bodies.
				Body e = destroyBody.get(a);
				destroyBody.removeIndex(a);
				world.destroyBody(e);
			}
			for (int a = creator.size - 1; a >= 0; a--) { // galu gale kazka pridesim i world.
				// kartais meta index out of bounds.
				if (creator.size == 0){ // speju sitas gali ivykt kazkur creator viduj, todel mes padarom taip apsidrausdami.
					break;
				}
				PhysicsCreator e = creator.get(a);
				creator.removeIndex(a);
				e.createBody(world);
			}
		}
	}

//	/** creates body with default values and places it in given coordinates. */
//	public Body createBody(Vector2 e){
//		return createBody(e.x, e.y);
//	}

	// custom body. pagal nurodytas cord. cord pixeliais.
//	/** creates body with default values and places it in given coordinates. */
//	public Body createBody(float x, float y){
//		return createBody(x, y, angle);
//	}

//	/** creates body with default values and places it in given coordinates. */
//	public Body createBody(float x, float y, float angle){
//		BodyDef body = new BodyDef();
//		body.position.set(x * pixelToWord, y * pixelToWord);
//		body.type = type;
//		body.angle = angle;
//		if (world.isLocked()){
//			Engine.getInstance().setError("Physics: Cannot create body. World is stepping!", ErrorMenu.ErrorType.ControlsError);
//			return null;
//		}
//		return world.createBody(body);
//	}

	public Body createBody(BodyDef def){
		if (def == null){
//			GdxPongy.getInstance().setError("BodyDef cannot be null", ErrorType.GameError);
			Gdx.app.log("Physics", "createBody doesn't accept null value. Ignoring...");
			return null;
		}
		if (world.isLocked()){
			Engine.getInstance().setError("Physics: Cannot create body. World is stepping!", ErrorMenu.ErrorType.ControlsError);
			return null;
		}
		return world.createBody(def);
	}

	public Joint createJoint(JointDef e){
		if (e == null){
//			GdxPongy.getInstance().setError("JointDef cannot be null", ErrorType.GameError);
			Gdx.app.log("Physics", "createJoint doesn't accept null value. Ignoring...");
			return null;
		}
		if (world.isLocked()){
			Engine.getInstance().setError("Physics: Cannot create joint. World is stepping!", ErrorMenu.ErrorType.ControlsError);
			return null;
		}
		return world.createJoint(e);
	}


	// duota default nustatymus.
//	/** @return FixtureDef with default values. */
//	public FixtureDef getFixtureDef(){
//		FixtureDef e = new FixtureDef();
//		e.restitution = restitution;
//		e.friction = friction;
//		e.density = density;
//		return e;
//	}
//	/** dėžė su apvaliais kampais pagal default density, restitution ir friction.*/
//	public Body roundedBox(float x, float y, float width, float height, float radius){
//		return roundedBox(x, y, width, height, radius, density, restitution, friction);
//	}

//	/** leis sukurt stačiakampį su suktais kampais.*/
//	public Body roundedBox(float x, float y, float width, float height, float radius, float density, float restitution, float friction){ // rutulys
//		if (width/2 < radius || height/2 < radius){
//			System.out.println("Boom, klaida kuriant staciakampi.");
//			return null;
//		}
//
//		if (world.isLocked()){
//			Engine.getInstance().setError("Physics: Cannot create body. World is stepping!", ErrorMenu.ErrorType.ControlsError);
//			return null;
//		}
//		x = x * pixelToWord;
//		y = y * pixelToWord;
//		width = width/ worldToPixel;
//		height = height/ worldToPixel;
//		radius = radius/ worldToPixel;
//		BodyDef body = new BodyDef();
//		body.type = type;
//		body.angle = angle;
//		body.position.set(x, y);
//
//		Body rbody = world.createBody(body);
//
//		PolygonShape box = new PolygonShape();
//		box.setAsBox((width-radius*2)/2, height/2);
//
//		FixtureDef fixtureDef = new FixtureDef();
//		fixtureDef.density = density;
//		fixtureDef.friction = friction;
//		fixtureDef.restitution = restitution;
//
//		fixtureDef.shape = box;
//
//		rbody.createFixture(fixtureDef);
//
//		box.setAsBox(width/2, (height - radius*2)/2);
//		fixtureDef.shape = box;
//		rbody.createFixture(fixtureDef);
//
//		CircleShape circle = new CircleShape();
//		circle.setAngle(radius);
//
//		Vector2[] places = new Vector2[4];
//		places[0] = new Vector2(-width/2+radius, -height/2+radius); // apacia desinys
//		places[1] = new Vector2(-width/2+radius, height/2-radius); // virsus desinė
//		places[2] = new Vector2(width/2-radius, -height/2+radius); // apačia kairė
//		places[3] = new Vector2(width/2-radius, height/2-radius);  // viršus kairė
//		for (Vector2 e : places){
//			circle.setPosition(e);
//			fixtureDef.shape = circle;
//			rbody.createFixture(fixtureDef);
//		}
//
//		box.dispose();
//		circle.dispose();
//		return rbody;
//	}


//	/** Sukurs rutulį pasaulyje.*/
//	public Body ball(float x, float y, float radius){
//		return ball(x, y, radius, density, friction, restitution);
//	}

//	public Body ball(float x, float y, float radius, float density, float friction, float restitution){ // rutulys, kamuolys.
//		if (world.isLocked()){
//			Engine.getInstance().setError("Physics: Cannot create body. World is stepping!", ErrorMenu.ErrorType.ControlsError);
//			return null;
//		}
//		x = x * pixelToWord;
//		y = y * pixelToWord;
//		radius = radius * pixelToWord;
//		BodyDef body = new BodyDef();
//		body.type = type;
//		body.position.set(x, y);
//
//		Body rbody = world.createBody(body);
//
//
//		CircleShape circle = new CircleShape();
//		circle.setAngle(radius);
//
//		FixtureDef fixtureDef = new FixtureDef();
//		fixtureDef.shape = circle;
//		fixtureDef.density = density; // mase
//		fixtureDef.friction = friction; // ciuozimas per objekta..
//		fixtureDef.restitution = restitution; // tipo bounce.
//		rbody.createFixture(fixtureDef);
//		circle.dispose();
//		return rbody;
//	}

//	/** sets default value of bodyType. */
//	public void setBodyType(BodyType e){
//		if (e == null){
//			System.out.println("Negali būt null");
//			return;
//		}
//		type = e;
//	}

//	/** sets default value. */
//	public void setRestitution(float e){
//		restitution = e;
//	}
//
//	/** sets default value. */
//	public void setFriction(float e){
//		friction = e;
//	}
//
//	/** sets default value. */
//	public void setDensity(float e){
//		density = e;
//	}
//
//	/** sets default value. */
//	public void setAngle(float angle){
//		this.angle = angle;
//	}

	/** velocity iterations used in {@link #step()} method. */
	public void setVelocityIterations(int velocityIterations){
		this.velocityIterations = velocityIterations;
	}

	/** position iterations used in {@link #step()} method. */
	public void setPositionIterations(int positionIterations){
		this.positionIterations = positionIterations;
	}

	/** default velocity iterations used in {@link #step()} */
	public int getVelocityIterations(){
		return velocityIterations;
	}

	/** default position iterations used in {@link #step()} */
	public int getPositionIterations(){
		return positionIterations;
	}

	/**
	 * Returns body coordinates translated in absolute camera units.
	 * @param e body which coordinates will be returned
	 * @return body coordinates in absolute camera units
     */
	public static Vector2 getPosition(Body e){
		temp.set(e.getPosition());
		temp.set(temp.x * worldToPixel, temp.y * worldToPixel);
		return temp;
	}

	/** Is world currently stepping. If world is stepping you cannot create or destroy bodies. Use {@link #create(PhysicsCreator)} to create bodies during
	 * stepping. Use methods: {@link #destroyBody(Body)}, {@link #destroyJoint(Joint)} to destroy body and/or joint during stepping (Creation and destruction will
	 * occur immediately after step).*/
	public boolean isWorldStepping(){
		return world.isLocked();
	}

	/** Adds joint to waiting list. Joint will be destroyed after world step. */
	public void destroyJoint(Joint e){
		if (e == null){
			return;
		}
		synchronized (this) { // per flush list sito liest negalima.
			for (Joint a : destroyJoint) {
				if (a == e) {
					return;
				}
			}
			destroyJoint.add(e);
		}
	}

	/** Adds body to waiting list. Body will be destroyed after world step. */
	public void destroyBody(Body e){
		if (e == null){
//			GdxPongy.getInstance().setError("Body cannot be null", ErrorType.GameError);
			Gdx.app.log("Physics", "destroyBody doesn't accept null value. Ignoring...");
			return; // nedarom error
		}
		synchronized (this) {
			for (Body a : destroyBody) {
				if (a == e) {
					return;
				}
			}
			destroyBody.add(e);
		}
	}

	/** Adds creator to list. Given instance method {@link PhysicsCreator#createBody(World)} will be called after world step. */
	public void create(PhysicsCreator e){
		if (e == null){
			return;
		}
		synchronized (this) {
			for (PhysicsCreator a : creator) {
				if (a == e) {
					return;
				}
			}
			creator.add(e);
		}
	}

	/** sets main contact listener for all collisions.*/
	public void setContactListener(ContactListener e){
		contactListener = e;
	}

//	/** reset default values. */
//	public void reset(){
//		restitution = 1f;
//		friction = 0;
//		density = 0.5f;
//		angle = 0;
//		type = BodyType.StaticBody;
//	}

	@Override
	public void dispose(){
		if (world != null)
			world.dispose();
	}
}
