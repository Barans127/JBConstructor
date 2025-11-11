package com.engine.physics;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Collision detection from Box2D.
 */

public interface TouchEvent {
    public void beginContact(Body collided, Contact contact);
    public void endContact(Body collided, Contact contact);
}
