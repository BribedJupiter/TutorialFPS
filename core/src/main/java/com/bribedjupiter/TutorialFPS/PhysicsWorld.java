package com.bribedjupiter.TutorialFPS;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.github.antzGames.gdx.ode4j.ode.*;

import static com.github.antzGames.gdx.ode4j.ode.OdeConstants.*;

public class PhysicsWorld implements Disposable {
    // Note: for ODE Z axis is up
    DWorld world;
    public DSpace space;
    private final DJointGroup contactgroup;

    public PhysicsWorld() {
        OdeHelper.initODE2(0);
        Gdx.app.log("ODE version", OdeHelper.getVersion());
        Gdx.app.log("ODE config", OdeHelper.getConfiguration());
        contactgroup = OdeHelper.createJointGroup();
        reset();
    }

    // reset world - note: this invalidates & orphans all rigid bodies and geoms so should
    // be used in combination with deleting all game objects.
    public void reset() {
        if (world != null) {
            world.destroy();
        }
        if (space != null) {
            space.destroy();
        }

        world = OdeHelper.createWorld();
        space = OdeHelper.createSapSpace(null, DSapSpace.AXES.XYZ);

        world.setGravity(0, 0, Settings.gravity);
        world.setCFM(1e-5);
        world.setERP(0.4);
        world.setQuickStepNumIterations(40);
        world.setAngularDamping(0.5f);

        // make inactive objs go to sleep
        world.setAutoDisableFlag(true);
        world.setAutoDisableLinearThreshold(0.1);
        world.setAutoDisableAngularThreshold(0.01);
        world.setAutoDisableTime(2);
    }

    // Update physics with one (fixed) time step
    public void update() {
        space.collide(null, nearCallback); // Check for collisions
        world.quickStep(0.05f); // Simulate collisions
        contactgroup.empty(); // empty collision group
    }

    private DGeom.DNearCallback nearCallback = new DGeom.DNearCallback() {
        @Override
        public void call(Object data, DGeom o1, DGeom o2) {
            DBody b1 = o1.getBody();
            DBody b2 = o2.getBody();
            if (b1 != null && b2 != null && OdeHelper.areConnected(b1, b2)) return;

            final int N = 8;
            DContactBuffer contacts = new DContactBuffer(N);

            int n = OdeHelper.collide(o1, o2, N, contacts.getGeomBuffer());
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    DContact contact = contacts.get(i);
                    contact.surface.mode = dContactSlip1 | dContactSlip2 | dContactSoftERP | dContactSoftCFM | dContactApprox1;
                    if (o1 instanceof DSphere || o2 instanceof DSphere || o1 instanceof DCapsule || o2 instanceof DCapsule) {
                        contact.surface.mu = 0.01; // Low friction for balls & capsules
                    }
                    else {
                        contact.surface.mu = 0.5;
                    }

                    contact.surface.slip1 = 0.0;
                    contact.surface.slip2 = 0.0;
                    contact.surface.soft_erp = 0.8;
                    contact.surface.soft_cfm = 0.01;

                    DJoint c = OdeHelper.createContactJoint(world, contactgroup, contact);
                    c.attach(o1.getBody(), o2.getBody());
                }
            }
        }
    };

    @Override
    public void dispose() {
        contactgroup.destroy();
        space.destroy();
        world.destroy();
        OdeHelper.closeODE();
    }
}
