package com.bribedjupiter.TutorialFPS;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.ScreenUtils;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
import com.bribedjupiter.TutorialFPS.GameView;
import com.bribedjupiter.TutorialFPS.GridView;
import com.bribedjupiter.TutorialFPS.PhysicsView;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen extends ScreenAdapter {
    private CamController camController;
    private GameView gameView;
    private World world;
    private GridView gridView;
    private PhysicsView physicsView;
    private boolean debugRender = false;

    @Override
    public void show() {
        // Prepare your screen here.
        Gdx.input.setCatchKey(Input.Keys.F1, true);
        // Setup cam controller
        world = new World("models/step4a.gltf");
        Populator.populate(world);
        gameView = new GameView(world);
        gridView = new GridView();
        physicsView = new PhysicsView(world);

        camController = new CamController(gameView.getCamera());
        Gdx.input.setInputProcessor(camController);

        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        // Update
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        if (Gdx.input.isKeyJustPressed((Input.Keys.R))) {
            Populator.populate(world);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            world.shootBall();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugRender = !debugRender;
        }

        // Render
        camController.update(Gdx.graphics.getDeltaTime());
        world.update(delta);
        gameView.render(delta);
        if (debugRender) {
            gridView.render(gameView.getCamera());
            physicsView.render(gameView.getCamera());
        }
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        gameView.resize(width, height);
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
        dispose();
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        gameView.dispose();
        world.dispose();
        gridView.dispose();
        physicsView.dispose();
    }
}
