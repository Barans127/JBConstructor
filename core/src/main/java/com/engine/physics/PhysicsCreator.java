package com.engine.physics;

import com.badlogic.gdx.physics.box2d.World;

/**
 * Listener for physics creation for Box2D. Because creating physics bodies, chains, joints during
 * world step is not allowed this interface can be used for safe call.
 * Executed after world step or immediately if Box2D world is not stepping.
 */

public interface PhysicsCreator {
    void createBody(World world);
}
