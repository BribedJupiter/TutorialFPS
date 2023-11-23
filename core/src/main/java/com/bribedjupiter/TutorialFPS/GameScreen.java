package com.bribedjupiter.TutorialFPS;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import com.bribedjupiter.TutorialFPS.Settings;
import com.bribedjupiter.TutorialFPS.CamController;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen extends ScreenAdapter {
    private PerspectiveCamera cam;
    private CamController camController;
    private Environment environment;
    private Model modelGround;
    private Texture textureGround;
    private Array<ModelInstance> instances;
    private ModelBatch modelBatch;

    public final Color BACKGROUND_COLOR = new Color(153f/255f, 1.0f, 236f/255f, 1.0f);

    @Override
    public void show() {
        // Prepare your screen here.
        // Setup camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, Settings.eyeHeight, 5f);
        cam.lookAt(0, Settings.eyeHeight, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        // Setup cam controller
        camController = new CamController(cam);
        Gdx.input.setInputProcessor(camController);

        // Setup environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Setup ground texture
        textureGround = new Texture(Gdx.files.internal("textures/Stylized_Stone_Floor_005_basecolor.jpg"), true);
        textureGround.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        textureGround.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        TextureRegion textureRegion = new TextureRegion(textureGround);
        int repeats = 10;
        textureRegion.setRegion(0, 0, textureGround.getWidth()*repeats, textureGround.getHeight()*repeats);

        // Setup model builder
        ModelBuilder modelBuilder = new ModelBuilder();

        // Setup ground model
        modelGround = modelBuilder.createBox(100f, 1f, 100f,
            new Material(TextureAttribute.createDiffuse(textureRegion)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        // Model instance - top flush with 0
        instances = new Array<>();
        instances.add(new ModelInstance(modelGround, 0, -1, 0));

        // Start modelBatch
        modelBatch = new ModelBatch();

        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        // Update
        camController.update(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        // Render
        ScreenUtils.clear(BACKGROUND_COLOR, true);
        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
        dispose();
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        modelBatch.dispose();
        modelGround.dispose();
        textureGround.dispose();
    }
}
