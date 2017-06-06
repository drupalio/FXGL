/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.physics.box2d.dynamics;

import com.almasb.fxgl.physics.box2d.collision.shapes.Shape;

/**
 * A fixture definition is used to create a fixture.
 * This class defines an abstract fixture definition.
 * You can reuse fixture definitions safely.
 *
 * @author daniel
 */
public final class FixtureDef {
    /**
     * The shape, this must be set. The shape will be cloned, so you can create the shape on the
     * stack.
     */
    private Shape shape = null;

    /**
     * Use this to store application specific fixture data.
     */
    private Object userData = null;

    /**
     * The friction coefficient, usually in the range [0,1].
     */
    private float friction = 0.2f;

    /**
     * The restitution (elasticity) usually in the range [0,1].
     */
    private float restitution = 0;

    /**
     * The density, usually in kg/m^2
     */
    private float density = 0;

    /**
     * A sensor shape collects contact information but never generates a collision response.
     */
    private boolean isSensor = false;

    /**
     * Contact filtering data;
     */
    private Filter filter = new Filter();

    /**
     * The shape, this must be set. The shape will be cloned, so you can create the shape on the
     * stack.
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * The shape, this must be set. The shape will be cloned, so you can create the shape on the
     * stack.
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    /**
     * Use this to store application specific fixture data.
     */
    public Object getUserData() {
        return userData;
    }

    /**
     * Use this to store application specific fixture data.
     */
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    /**
     * The friction coefficient, usually in the range [0,1].
     */
    public float getFriction() {
        return friction;
    }

    /**
     * The friction coefficient, usually in the range [0,1].
     */
    public void setFriction(float friction) {
        this.friction = friction;
    }

    /**
     * The restitution (elasticity) usually in the range [0,1].
     */
    public float getRestitution() {
        return restitution;
    }

    /**
     * The restitution (elasticity) usually in the range [0,1].
     */
    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }

    /**
     * The density, usually in kg/m^2
     */
    public float getDensity() {
        return density;
    }

    /**
     * The density, usually in kg/m^2
     */
    public void setDensity(float density) {
        this.density = density;
    }

    /**
     * A sensor shape collects contact information but never generates a collision response.
     */
    public boolean isSensor() {
        return isSensor;
    }

    /**
     * A sensor shape collects contact information but never generates a collision response.
     */
    public void setSensor(boolean isSensor) {
        this.isSensor = isSensor;
    }

    /**
     * @return contact filtering data
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Set contact filtering data.
     *
     * @param filter filter
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /* FLUENT API */

    public FixtureDef friction(float friction) {
        this.friction = friction;
        return this;
    }

    public FixtureDef restitution(float restitution) {
        this.restitution = restitution;
        return this;
    }

    public FixtureDef density(float density) {
        this.density = density;
        return this;
    }

    public FixtureDef filter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public FixtureDef shape(Shape shape) {
        this.shape = shape;
        return this;
    }

    public FixtureDef sensor(boolean isSensor) {
        this.isSensor = isSensor;
        return this;
    }

    public FixtureDef userData(Object userData) {
        this.userData = userData;
        return this;
    }
}
