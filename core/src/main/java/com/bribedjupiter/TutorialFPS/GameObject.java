package com.bribedjupiter.TutorialFPS;

import net.mgsx.gltf.scene3d.scene.Scene;

public class GameObject {
    public final Scene scene;
    public final PhysicsBody body;

    public GameObject(Scene scene, PhysicsBody body) {
        this.scene = scene;
        this.body = body;
        body.geom.setData(this); // the geom has the user data to link back to GameObject for collision handling
    }
}
