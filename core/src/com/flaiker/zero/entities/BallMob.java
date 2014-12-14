package com.flaiker.zero.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.flaiker.zero.helper.ContactCallback;
import com.flaiker.zero.screens.GameScreen;

/**
 * Created by Flaiker on 13.12.2014.
 */
public class BallMob extends AbstractMob {
    private static final float MAX_SPEED_X    = 2f;
    private static final float ACCELERATION_X = 100f;

    private boolean wallRight = false;
    private boolean wallLeft = false;

    public BallMob(World world, float xPosMeter, float yPosMeter) {
        super(world, "ballMob.png", xPosMeter, yPosMeter, 5);
    }

    @Override
    protected Body createBody(World world) {
        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();

        // create body
        bdef.position.set(getSpriteX(), getSpriteY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        Body body = world.createBody(bdef);
        body.setLinearDamping(5f);
        body.setUserData(this);

        // create rotating circle
        shape.setRadius(getEntityWidth() / 2f);
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 10000f;
        fdef.restitution = 0f;
        body.createFixture(fdef);
        fdef.density = 0f;
        fdef.friction = 0f;

        // create sensors on different body
        Body sensorBody = world.createBody(bdef);
        PolygonShape boxShape = new PolygonShape();

        // sensor right
        boxShape.setAsBox(0.01f, sprite.getHeight() / GameScreen.PIXEL_PER_METER / 2f - 0.2f,
                          new Vector2(sprite.getWidth() / GameScreen.PIXEL_PER_METER / 2f, 0), 0);
        fdef.shape = boxShape;
        fdef.isSensor = true;
        sensorBody.createFixture(fdef).setUserData(new ContactCallback() {
            @Override
            public void onContactStart() {
                wallRight = true;
            }

            @Override
            public void onContactStop() {
                wallRight = false;
            }
        });

        // sensor left
        boxShape.setAsBox(-0.01f, sprite.getHeight() / GameScreen.PIXEL_PER_METER / 2f - 0.2f,
                          new Vector2(-sprite.getWidth() / GameScreen.PIXEL_PER_METER / 2f, 0), 0);
        fdef.shape = boxShape;
        fdef.isSensor = true;
        sensorBody.createFixture(fdef).setUserData(new ContactCallback() {
            @Override
            public void onContactStart() {
                wallLeft = true;
            }

            @Override
            public void onContactStop() {
                wallLeft = false;
            }
        });

        // join bodies together
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.bodyA = body;
        revoluteJointDef.bodyB = sensorBody;
        revoluteJointDef.enableLimit = false;
        revoluteJointDef.collideConnected = false;
        world.createJoint(revoluteJointDef);

        return body;
    }

    @Override
    public void update() {
        super.update();
        if (getRequestedDirection() == Direction.NONE) setRequestedDirection(Direction.RIGHT);
        aiWalk();
    }

    private void aiWalk() {
        if (wallRight && getRequestedDirection() == Direction.RIGHT) {
            setRequestedDirection(Direction.LEFT);
        } else if (wallLeft && getRequestedDirection() == Direction.LEFT) {
            setRequestedDirection(Direction.RIGHT);
        }
    }

    @Override
    protected float getMaxSpeedX() {
        return MAX_SPEED_X;
    }

    @Override
    protected float getAccelerationX() {
        return ACCELERATION_X;
    }
}
