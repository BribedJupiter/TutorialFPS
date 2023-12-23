package com.bribedjupiter.TutorialFPS;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;

public class PhysicsView implements Disposable {
    private final ModelBatch modelBatch;
    private final World world;

    public PhysicsView(World world) {
        this.world = world;
        modelBatch = new ModelBatch();
    }

    public void render(Camera cam) {
        modelBatch.begin(cam);
        int num = world.getNumGameObjects();
        for (int i = 0; i < num; i++) {
            GameObject go = world.getGameObject(i);
            if (go.visible) {
                go.body.render(modelBatch);
            }
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }
}
