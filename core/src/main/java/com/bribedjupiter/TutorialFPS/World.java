package com.bribedjupiter.TutorialFPS;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {
    private final Array<GameObject> gameObjects;
    private final SceneAsset sceneAsset;
    private final PhysicsWorld physicsWorld;
    private final PhysicsBodyFactory factory;

    public GameObject player;
    private boolean isDirty;

    public World(String modelFileName) {
        gameObjects = new Array<>();
        sceneAsset = new GLTFLoader().load(Gdx.files.internal(modelFileName));
        for (Node node : sceneAsset.scene.model.nodes) {
            Gdx.app.log("Node", node.id);
        }
        isDirty = true;
        physicsWorld = new PhysicsWorld();
        factory = new PhysicsBodyFactory(physicsWorld);
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void clear() {
        gameObjects.clear();
        player = null;
        isDirty = true;
    }

    public int getNumGameObjects() {
        return gameObjects.size;
    }

    public GameObject getGameObject(int index) {
        return gameObjects.get(index);
    }

    public GameObject spawnObject(boolean isStatic, String name, CollisionShapeType shape, Vector3 position, float mass) {
        Scene scene = new Scene(sceneAsset.scene, name);
        if (scene.modelInstance.nodes.size == 0) {
            Gdx.app.error("Cannot find node in GLTF", name);
            return null;
        }
        applyNodeTransform(scene.modelInstance, scene.modelInstance.nodes.first()); // inorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        PhysicsBody body = factory.createBody(scene.modelInstance, shape, mass, isStatic);
        GameObject go = new GameObject(scene, body);
        gameObjects.add(go);
        isDirty = true;
        return go;
    }

    private void applyNodeTransform(ModelInstance modelInstance, Node node) {
        modelInstance.transform.mul(node.globalTransform);
        node.translation.set(0, 0, 0);
        node.scale.set(1, 1, 1);
        node.rotation.idt();
        modelInstance.calculateTransforms();
    }

    public void removeObject(GameObject gameObject) {
        gameObjects.removeValue(gameObject, true);
        isDirty = true;
    }

    public void update(float deltaTime) {
        physicsWorld.update();
    }

    @Override
    public void dispose() {
        sceneAsset.dispose();
        physicsWorld.dispose();
    }
}
