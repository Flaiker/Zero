package com.flaiker.zero.entities;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.flaiker.zero.screens.GameScreen;

/**
 * Created by Flaiker on 22.12.2014.
 */
public class LampRope extends AbstractEntity {
    private ConeLight coneLight;
    private Sprite    ropeSprite;
    private float     height;
    private float     initialPanMargin;

    public LampRope(RayHandler rayHandler, float xPosMeter, float yPosMeter, float height, float initialPanMargin) {
        super("lamprope", xPosMeter, yPosMeter);
        if (height < 1) throw new IllegalArgumentException("Height needs to be >=1");
        this.height = height;
        this.initialPanMargin = initialPanMargin;
        createLight(rayHandler);
        ropeSprite = new Sprite(AbstractEntity.ENTITY_TEXTURE_ATLAS.findRegion("lamprope-rope"));
        ropeSprite.setSize(ropeSprite.getWidth(), height * GameScreen.PIXEL_PER_METER - sprite.getHeight());
        ropeSprite.setOrigin(ropeSprite.getWidth() / 2f, ropeSprite.getHeight());
        ropeSprite.setPosition(xPosMeter * GameScreen.PIXEL_PER_METER - ropeSprite.getWidth() / 2f,
                               yPosMeter * GameScreen.PIXEL_PER_METER - ropeSprite.getHeight());
    }

    private void createLight(RayHandler rayHandler) {
        coneLight = new ConeLight(rayHandler, 25, new Color(1, 1, 1, 0.9f), 7, 5, 7.5f, -90, 40);
    }

    @Override
    public void update() {
        super.update();
        coneLight.setPosition(body.getPosition().x, body.getPosition().y);
        coneLight.setDirection(body.getAngle() * MathUtils.radiansToDegrees - 90);
        ropeSprite.setRotation(sprite.getRotation());
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
        ropeSprite.draw(batch);
    }

    @Override
    protected Body createBody(World world) {
        Vector2 spawnVector = getSpawnVector();

        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        bdef.position.set(spawnVector.x, spawnVector.y);
        bdef.type = BodyDef.BodyType.StaticBody;
        Body anchorBody = world.createBody(bdef);
        shape.setAsBox(0.01f, 0.01f);
        fdef.shape = shape;
        fdef.isSensor = true;
        anchorBody.createFixture(fdef);
        fdef.isSensor = false;

        bdef.position.set(spawnVector.x + initialPanMargin, spawnVector.y - height - sprite.getHeight() / GameScreen.PIXEL_PER_METER / 2f);
        bdef.type = BodyDef.BodyType.DynamicBody;
        Body lampBody = world.createBody(bdef);
        lampBody.setUserData(this);
        shape.set(new Vector2[]{
                new Vector2(-sprite.getWidth() / 2f / GameScreen.PIXEL_PER_METER, -sprite.getHeight() / 2 / GameScreen.PIXEL_PER_METER),
                new Vector2(sprite.getWidth() / 2f / GameScreen.PIXEL_PER_METER, -sprite.getHeight() / 2 / GameScreen.PIXEL_PER_METER),
                new Vector2(0, sprite.getHeight() / 2 / GameScreen.PIXEL_PER_METER)});
        fdef.shape = shape;
        fdef.density = 100;
        lampBody.createFixture(fdef);

        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.initialize(anchorBody, lampBody, anchorBody.getWorldCenter());
        revoluteJointDef.localAnchorA.set(0, 0f);
        revoluteJointDef.localAnchorB.set(0, height - sprite.getHeight() / GameScreen.PIXEL_PER_METER);
        revoluteJointDef.enableLimit = false;
        world.createJoint(revoluteJointDef);

        return lampBody;
    }
}
