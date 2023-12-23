package com.bribedjupiter.TutorialFPS;

import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

public class GameObject {
    public final Scene scene;
    public final PhysicsBody body;
    public final Vector3 direction;
    public boolean visible;

    public GameObject(Scene scene, PhysicsBody body) {
        this.scene = scene;
        this.body = body;
        direction = new Vector3();
        visible = true;
        body.geom.setData(this); // the geom has the user data to link back to GameObject for collision handling
    }

    public Vector3 getPosition() {
        return body.getPosition();
    }

    public Vector3 getDirection() {
        direction.set(0, 0, 1);
        direction.mul(body.getOrientation());
        return direction;
    }
}
