package com.engine.physics;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;

public interface SolveEvent {
    void preSolve(Body collidedWith, Contact contact, Manifold oldManifold);
    void postSolve(Body collidedWith, Contact contact, ContactImpulse impulse);
}
