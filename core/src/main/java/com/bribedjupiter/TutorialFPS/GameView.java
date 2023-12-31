package com.bribedjupiter.TutorialFPS;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class GameView implements Disposable {
    private final World world;
    private final SceneManager sceneManager;
    private final PerspectiveCamera cam;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private SceneSkybox skybox;
    private final CameraController camController;

    public GameView(World world) {
        this.world = world;
        sceneManager = new SceneManager();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, Settings.eyeHeight, 5f);
        cam.lookAt(0, Settings.eyeHeight, 0);
        cam.near = 0.1f; // todo make in settings
        cam.far = 300f; // todo make in settings
        cam.update();

        sceneManager.setCamera(cam);
        camController = new CameraController(cam);
        camController.setThirdPersonMode(true);

        // Setup light
        DirectionalLightEx light = new net.mgsx.gltf.scene3d.lights.DirectionalShadowLight(Settings.shadowMapSize, Settings.shadowMapSize)
            .setViewport(50, 50, 10, 100);
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);
        light.intensity = 3f;
        sceneManager.environment.add(light);

        // Setup quick Image Based Lighting (IBL)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.setAmbientLight(1f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // Setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);
    }

    public PerspectiveCamera getCamera() {
        return cam;
    }

    public CameraController getCameraController() {
        return camController;
    }

    public void refresh() {
        sceneManager.getRenderableProviders().clear(); // remove all scenes

        // add scene for each game object
        int num = world.getNumGameObjects();
        for (int i = 0; i < num; i++) {
            Scene scene = world.getGameObject(i).scene;
            if (world.getGameObject(i).visible)
                sceneManager.addScene(scene, false);
        }
    }

    public void render (float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            boolean thirdPersonView = !camController.getThirdPersonMode();
            camController.setThirdPersonMode(thirdPersonView);
            world.getPlayer().visible = thirdPersonView;
            refresh();
        }

        camController.update(world.getPlayer().getPosition(), world.getPlayerController().getViewingDirection());
        cam.update();
        if (world.isDirty())
            refresh();
        sceneManager.update(delta);

        // render
        ScreenUtils.clear(Color.PURPLE, true); // clear color hidden by skybox
        sceneManager.render();
    }

    public void resize(int width, int height) {
        sceneManager.updateViewport(width, height);
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();
    }
}
