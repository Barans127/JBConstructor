package com.engine.physics;

import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by jbara on 2016-11-01.
 */

public interface PhysicsCreator {
    void createBody(World world);
}
