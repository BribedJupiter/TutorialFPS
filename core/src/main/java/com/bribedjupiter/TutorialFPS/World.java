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
    private final Vector3 dir = new Vector3();
    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootDirection = new Vector3();
    private final PlayerController playerController;

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
        playerController = new PlayerController();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void clear() {
        physicsWorld.reset();
        playerController.reset();

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

    public GameObject getPlayer() {
        return player;
    }

    public void setPlayer (GameObject player) {
        this.player = player;
        player.body.setPlayerCharacteristics();
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public void shootBall() {
        dir.set(player.getDirection());
        spawnPos.set(dir);
        spawnPos.add(player.getPosition()); // spawn from 1 unit in front of the player
        GameObject ball = spawnObject(false, "ball", CollisionShapeType.SPHERE, true, spawnPos, Settings.ballMass);
        shootDirection.set(dir);
        shootDirection.y += 0.5f;
        shootDirection.scl(Settings.ballForce);
        ball.body.applyForce(shootDirection);
    }

    public GameObject spawnObject(boolean isStatic, String name, CollisionShapeType shape, boolean resetPosition, Vector3 position, float mass) {
        Scene scene = new Scene(sceneAsset.scene, name);
        if (scene.modelInstance.nodes.size == 0) {
            Gdx.app.error("Cannot find node in GLTF", name);
            return null;
        }
        applyNodeTransform(resetPosition, scene.modelInstance, scene.modelInstance.nodes.first()); // incorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        PhysicsBody body = factory.createBody(scene.modelInstance, shape, mass, isStatic);
        GameObject go = new GameObject(scene, body);
        gameObjects.add(go);
        isDirty = true;
        return go;
    }

    private void applyNodeTransform(boolean resetPosition, ModelInstance modelInstance, Node node) {
        if (!resetPosition) { // Set resetPosition to false if the object will always be in the same place
            modelInstance.transform.mul(node.globalTransform); // multiply matrices
        }
        node.translation.set(0, 0, 0); // reset to origin
        node.scale.set(1, 1, 1); // reset scale
        node.rotation.idt(); // reset quaternion
        modelInstance.calculateTransforms(); // reset transformations
    }

    public void removeObject(GameObject gameObject) {
        gameObjects.removeValue(gameObject, true);
        isDirty = true;
    }

    public void update(float deltaTime) {
        playerController.update(player, deltaTime);
        physicsWorld.update();
        syncToPhysics();
    }

    private void syncToPhysics() {
        for (GameObject go : gameObjects) {
            if (go.body.geom.getBody() != null) {
                go.scene.modelInstance.transform.set(go.body.getPosition(), go.body.getOrientation());
            }
        }
        // the player model is an exception, use information from the player controller
        player.scene.modelInstance.transform.setToRotation(Vector3.Z, playerController.getForwardDirection());
        player.scene.modelInstance.transform.setTranslation(player.body.getPosition());
    }

    @Override
    public void dispose() {
        sceneAsset.dispose();
        physicsWorld.dispose();
    }
}
