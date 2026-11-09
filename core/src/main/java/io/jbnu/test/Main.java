

package io.jbnu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.Locale;

public class Main extends ApplicationAdapter {

    // =========================
    // WORLD / PLAYER
    // =========================

    private static final float WORLD_W = 2048f;
    private static final float WORLD_H = 682f;

    private static final float VIEW_W = 1100f;
    private static final float VIEW_H = 620f;

    private static final float PLAYER_W = 54f;
    private static final float PLAYER_H = 76f;
    private static final float PLAYER_SPEED = 325f;
    private static final float JUMP_SPEED = 850f;
    private static final float GRAVITY = 1250f;

    // =========================
    // STAGE SETTINGS
    // =========================

    private static final int[] TARGETS = {
        30, 50, 70
    };

    private static final float[] TIME_LIMITS = {
        60f, 60f, 80f
    };

    private int stage = 1;

    private int target() {
        return TARGETS[stage - 1];
    }

    private float timeLimit() {
        return TIME_LIMITS[stage - 1];
    }

    // =========================
    // PLATFORM COORDINATES
    // =========================

    // BLUE ROOM
    private final Rectangle[] bluePlatforms = {
        new Rectangle(390, WORLD_H - 344, 334, 13),
        new Rectangle(817, WORLD_H - 231, 348, 13),
        new Rectangle(1234, WORLD_H - 344, 312, 13),
        new Rectangle(1582, WORLD_H - 231, 293, 13)
    };

    private final Rectangle blueFloor =
        new Rectangle(0, 86, WORLD_W, 12);

    // YELLOW ROOM
    private final Rectangle[] yellowPlatforms = {
        new Rectangle(7, WORLD_H - 510, 331, 12),
        new Rectangle(449, WORLD_H - 488, 316, 12),
        new Rectangle(951, WORLD_H - 385, 307, 12),
        new Rectangle(1425, WORLD_H - 350, 193, 12),
        new Rectangle(1683, WORLD_H - 485, 357, 12)
    };

    private final Rectangle yellowFloor =
        new Rectangle(0, 8, WORLD_W, 12);

    // PINK ROOM
    // Coordinates measured from the supplied
    // 2048 x 682 pink background.
    // Includes large furniture tops and small platforms.
    private final Rectangle[] pinkPlatforms = {
        new Rectangle(0, WORLD_H - 413, 293, 12),
        new Rectangle(360, WORLD_H - 383, 80, 12),
        new Rectangle(517, WORLD_H - 500, 93, 12),
        new Rectangle(653, WORLD_H - 308, 98, 12),
        new Rectangle(853, WORLD_H - 252, 171, 12),
        new Rectangle(1093, WORLD_H - 362, 97, 12),
        new Rectangle(1163, WORLD_H - 193, 81, 12),
        new Rectangle(1261, WORLD_H - 525, 94, 12),
        new Rectangle(1433, WORLD_H - 228, 175, 12),
        new Rectangle(1701, WORLD_H - 188, 77, 12),
        new Rectangle(1786, WORLD_H - 410, 262, 12)
    };

    private final Rectangle pinkFloor =
        new Rectangle(0, 0, WORLD_W, 8);

    private Rectangle[] activePlatforms() {
        if (stage == 1) return bluePlatforms;
        if (stage == 2) return yellowPlatforms;
        return pinkPlatforms;
    }

    private Rectangle activeFloor() {
        if (stage == 1) return blueFloor;
        if (stage == 2) return yellowFloor;
        return pinkFloor;
    }

    // =========================
    // WEAPONS
    // =========================

    private static final int SWATTER = 0;
    private static final int FLAME = 1;
    private static final int INSECTICIDE = 2;

    private static final float ATTACK_REACH = 115f;
    private static final float ATTACK_COOLDOWN = 0.20f;

    private static final float FLAME_REACH = 170f;
    private static final float INSECT_REACH = 340f;

    private static final float DOUBLE_CLICK = 0.30f;
    private static final float SPRAY_DURATION = 0.28f;
    private static final float SPRAY_TICK = 0.18f;

    private int weapon = SWATTER;

    private float hitCooldown;
    private float swingTimer;
    private float sprayTimer;
    private float sprayTickTimer;
    private float lastSprayClick = -10f;
    private float gameClock;

    private boolean continuousSpray;

    // =========================
    // GRAPHICS
    // =========================

    private SpriteBatch batch;

    private OrthographicCamera worldCamera;
    private OrthographicCamera uiCamera;

    private FitViewport worldViewport;
    private FitViewport uiViewport;

    private Texture blueBackground;
    private Texture yellowBackground;
    private Texture pinkBackground;

    private Texture player;
    private Texture americaTexture;
    private Texture japanTexture;
    private Texture germanTexture;

    private Texture swatter;
    private Texture flame;
    private Texture runFlame;
    private Texture insecticide;
    private Texture runInsecticide;

    private Texture startImage;
    private Texture gameRule1, gameRule2, level2Rule, level3Rule;
    private BitmapFont ruleFont;
    private BitmapFont pauseTitleFont;
    private BitmapFont pauseInfoFont;
    private final GlyphLayout pauseLayout = new GlyphLayout();
    private Texture successImage;
    private Texture failImage;
    private Texture white;

    private Sound hitSound;

    private BitmapFont font;
    private BitmapFont dodgeFont;

    private boolean koreanFontAvailable;

    private final Vector3 pointer = new Vector3();

    // =========================
    // PLAYER STATE
    // =========================

    private float playerX;
    private float playerY;
    private float verticalSpeed;

    private float aimX = 1f;
    private float aimY = 0f;

    private boolean grounded;
    private float dropTimer;

    private float elapsedTime;
    private int killed;

    private enum State {
        START, RULE1, RULE2, LEVEL2_RULE, LEVEL3_RULE, PLAY, PAUSE, CLEAR, FAIL
    }

    private State state = State.START;

    // =========================
    // COCKROACH
    // =========================

    private static class Bug {

        float x, y;
        float w, h;
        float speed;

        Rectangle surface;

        int type;
        int health;

        float dodgeTimer;

        // 0 = American
        // 1 = Japanese
        // 2 = German

        Bug(
            float x,
            float y,
            float w,
            float h,
            float speed,
            Rectangle surface,
            int type
        ) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.speed = speed;
            this.surface = surface;
            this.type = type;

            health = type == 1 ? 2 : 1;
        }

        void update(float dt) {

            x += speed * dt;

            dodgeTimer = Math.max(
                0f, dodgeTimer - dt
            );

            float left = surface.x + 5f;
            float right =
                surface.x + surface.width - w - 5f;

            if (right < left) {
                right = left;
            }

            if (x <= left) {
                x = left;
                speed = Math.abs(speed);
            }

            if (x >= right) {
                x = right;
                speed = -Math.abs(speed);
            }
        }
    }

    private final Array<Bug> bugs = new Array<>();

    // =========================
    // ASSET LOADING
    // =========================

    private Texture optionalTexture(String path) {

        if (Gdx.files.internal(path).exists()) {
            return new Texture(
                Gdx.files.internal(path)
            );
        }

        return null;
    }

    @Override
    public void create() {

        batch = new SpriteBatch();

        blueBackground =
            new Texture("backgroundblue.png");

        yellowBackground =
            new Texture("backgroundyellow.png");

        pinkBackground =
            new Texture("backgroundpink.png");

        player = new Texture("player.png");

        americaTexture =
            new Texture("crochamerica.png");

        japanTexture =
            new Texture("crochjapan.png");

        germanTexture =
            optionalTexture("crochgermany.png");

        swatter = optionalTexture("pariche.png");

        flame = optionalTexture("fire.png");
        runFlame = optionalTexture("runfire.png");

        insecticide =
            optionalTexture("killbug.png");

        runInsecticide =
            optionalTexture("runkillbug.png");

        startImage =
            optionalTexture("startscreen.png");
        gameRule1 = optionalTexture("gamerule1.png");
        gameRule2 = optionalTexture("gamerule3.png");
        level2Rule = optionalTexture("level2rule.png");
        level3Rule = optionalTexture("level3rule.png");

        successImage =
            optionalTexture("successscreen.png");

        failImage =
            optionalTexture("failscreen.png");

        if (Gdx.files.internal("pang.wav").exists()) {
            hitSound = Gdx.audio.newSound(
                Gdx.files.internal("pang.wav")
            );
        }

        Pixmap pixel = new Pixmap(
            1, 1, Pixmap.Format.RGBA8888
        );

        pixel.setColor(Color.WHITE);
        pixel.fill();

        white = new Texture(pixel);
        pixel.dispose();

        font = new BitmapFont();
        font.getData().setScale(2f);

        createDodgeFont();
        createRuleFont();
        createPauseFonts();

        worldCamera = new OrthographicCamera();

        worldViewport = new FitViewport(
            VIEW_W, VIEW_H, worldCamera
        );

        uiCamera = new OrthographicCamera();

        uiViewport = new FitViewport(
            VIEW_W, VIEW_H, uiCamera
        );

        resize(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        resetStage();
        state = State.START;
    }

    private void createDodgeFont() {

        dodgeFont = new BitmapFont();
        koreanFontAvailable = false;

        com.badlogic.gdx.files.FileHandle file = null;

        if (Gdx.files.internal("malgun.ttf").exists()) {
            file = Gdx.files.internal("malgun.ttf");
        } else if (
            Gdx.files.absolute(
                "C:/Windows/Fonts/malgun.ttf"
            ).exists()
        ) {
            file = Gdx.files.absolute(
                "C:/Windows/Fonts/malgun.ttf"
            );
        }

        if (file != null) {

            FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(file);

            FreeTypeFontGenerator.FreeTypeFontParameter p =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

            p.size = 26;
            p.characters = "회피!";
            p.color = Color.RED;

            dodgeFont.dispose();

            dodgeFont = generator.generateFont(p);

            generator.dispose();

            koreanFontAvailable = true;
        }
    }

    // =========================
    // STAGE RESET / SPAWN
    // =========================

    private void resetStage() {

        playerX = 105f;

        playerY =
            activeFloor().y + activeFloor().height;

        verticalSpeed = 0f;
        grounded = true;
        dropTimer = 0f;

        aimX = 1f;
        aimY = 0f;

        elapsedTime = 0f;
        killed = 0;

        hitCooldown = 0f;
        swingTimer = 0f;

        weapon = SWATTER;

        sprayTimer = 0f;
        sprayTickTimer = 0f;
        lastSprayClick = -10f;
        continuousSpray = false;

        gameClock = 0f;

        bugs.clear();

        Rectangle[] surfaces;
        int[] counts;

        if (stage == 1) {

            surfaces = new Rectangle[] {
                blueFloor,
                bluePlatforms[0],
                bluePlatforms[1],
                bluePlatforms[2],
                bluePlatforms[3]
            };

            counts = new int[] {
                10, 5, 5, 5, 5
            };

        } else if (stage == 2) {

            surfaces = new Rectangle[] {
                yellowFloor,
                yellowPlatforms[0],
                yellowPlatforms[1],
                yellowPlatforms[2],
                yellowPlatforms[3],
                yellowPlatforms[4]
            };

            counts = new int[] {
                14, 7, 8, 8, 6, 7
            };

        } else {

            surfaces = new Rectangle[
                pinkPlatforms.length + 1
                ];

            surfaces[0] = pinkFloor;

            for (int i = 0;
                 i < pinkPlatforms.length;
                 i++) {

                surfaces[i + 1] = pinkPlatforms[i];
            }

            // 20 on ground + 50 on platforms
            counts = new int[] {
                20, 6, 4, 4, 4, 6, 4,
                4, 4, 6, 4, 4
            };
        }

        int bugNumber = 0;

        for (int s = 0; s < surfaces.length; s++) {

            Rectangle surface = surfaces[s];

            for (int i = 0; i < counts[s]; i++) {

                int type;

                if (stage == 1) {
                    type = 0;
                } else if (stage == 2) {
                    type = bugNumber % 2;
                } else {
                    type = bugNumber % 3;
                }

                bugNumber++;

                int size = MathUtils.random(0, 2);

                float width =
                    size == 0 ? 32f :
                        size == 1 ? 47f : 62f;

                Texture texture = bugTexture(type);

                float height =
                    width * texture.getHeight()
                        / (float) texture.getWidth();

                float slotW =
                    surface.width / counts[s];

                float x =
                    surface.x
                        + slotW * (i + 0.5f)
                        - width / 2f;

                x += MathUtils.random(
                    -slotW * 0.20f,
                    slotW * 0.20f
                );

                float minX = surface.x + 5f;
                float maxX =
                    surface.x + surface.width
                        - width - 5f;

                x = MathUtils.clamp(
                    x, minX, Math.max(minX, maxX)
                );

                float speed =
                    MathUtils.random(35f, 75f);

                // German cockroaches are 2x faster.
                if (type == 2) {
                    speed *= 2f;
                }

                if (MathUtils.randomBoolean()) {
                    speed = -speed;
                }

                bugs.add(new Bug(
                    x,
                    surface.y + surface.height,
                    width,
                    height,
                    speed,
                    surface,
                    type
                ));
            }
        }

        worldCamera.position.set(
            VIEW_W / 2f,
            WORLD_H / 2f,
            0
        );

        worldCamera.update();
    }

    private Texture bugTexture(int type) {

        if (type == 1) {
            return japanTexture;
        }

        if (type == 2 && germanTexture != null) {
            return germanTexture;
        }

        return americaTexture;
    }

    // =========================
    // MAIN LOOP
    // =========================

    @Override
    public void render() {

        float dt = Math.min(
            Gdx.graphics.getDeltaTime(),
            1f / 30f
        );

        boolean enter = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (state == State.PLAY) state = State.PAUSE;
            else if (state == State.PAUSE) state = State.PLAY;
        }
        if (state == State.START) {
            if (startButtonClicked() || enter) state = State.RULE1;
        } else if (state == State.RULE1) {
            if (enter) state = State.RULE2;
        } else if (state == State.RULE2) {
            if (enter) { stage = 1; resetStage(); state = State.PLAY; }
        } else if (state == State.LEVEL2_RULE) {
            if (enter) { stage = 2; resetStage(); state = State.PLAY; }
        } else if (state == State.LEVEL3_RULE) {
            if (enter) { stage = 3; resetStage(); state = State.PLAY; }
        } else if (state == State.PLAY) {
            updateGame(dt);
        } else if (state == State.CLEAR) {
            if (enter) {
                if (stage == 1) state = State.LEVEL2_RULE;
                else if (stage == 2) state = State.LEVEL3_RULE;
                else { stage = 1; resetStage(); state = State.START; }
            }
        } else if (state == State.FAIL) {
            if (enter) { stage = 1; resetStage(); state = State.START; }
        }

        ScreenUtils.clear(
            0.08f, 0.16f, 0.28f, 1f
        );

        if (state == State.PLAY || state == State.PAUSE) {
            renderWorld();
        }

        renderUI();
    }

    private boolean startButtonClicked() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return false;
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(mouse);
        return mouse.x >= VIEW_W * 0.29f && mouse.x <= VIEW_W * 0.71f
            && mouse.y >= VIEW_H * 0.09f && mouse.y <= VIEW_H * 0.45f;
    }

    private void createRuleFont() {
        ruleFont = new BitmapFont();
        com.badlogic.gdx.files.FileHandle file = null;
        if (Gdx.files.internal("malgun.ttf").exists())
            file = Gdx.files.internal("malgun.ttf");
        else if (Gdx.files.absolute("C:/Windows/Fonts/malgun.ttf").exists())
            file = Gdx.files.absolute("C:/Windows/Fonts/malgun.ttf");
        if (file != null) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(file);
            FreeTypeFontGenerator.FreeTypeFontParameter param =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.size = 26;
            param.color = Color.RED;
            param.characters = " 아래에enter키를눌러넘어가기보드2번3아이템선택가능";
            ruleFont.dispose();
            ruleFont = generator.generateFont(param);
            generator.dispose();
        } else {
            ruleFont.getData().setScale(1.8f);
        }
    }

    private void drawRule(Texture image, String caption, String fallback) {
        drawScreenBackground(image);
        // Pale panel makes the red caption legible on any rule image.
        batch.setColor(1f, 1f, 1f, 0.88f);
        batch.draw(white, 80f, 20f, VIEW_W - 160f, 65f);
        batch.setColor(Color.RED);
        if (Gdx.files.internal("malgun.ttf").exists() ||
            Gdx.files.absolute("C:/Windows/Fonts/malgun.ttf").exists()) {
            ruleFont.draw(batch, caption, 110f, 62f);
        } else {
            font.draw(batch, fallback, 110f, 62f);
        }
        batch.setColor(Color.WHITE);
    }

    private boolean confirmPressed() {

        return
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            );
    }

    private void updateGame(float dt) {

        updatePlayer(dt);

        for (Bug bug : bugs) {
            bug.update(dt);
        }

        hitCooldown = Math.max(
            0f, hitCooldown - dt
        );

        swingTimer = Math.max(
            0f, swingTimer - dt
        );

        gameClock += dt;

        updateAim();
        updateWeaponSelection();

        if (Gdx.input.isButtonJustPressed(
            Input.Buttons.LEFT
        )) {

            if (weapon == SWATTER) {

                if (hitCooldown <= 0f) {

                    swatterAttack();

                    hitCooldown = ATTACK_COOLDOWN;
                    swingTimer = 0.14f;
                }

            } else {

                handleSprayClick();
            }
        }

        updateSpray(dt);

        if (bugs.size == 0) {

            state = State.CLEAR;

        } else {

            elapsedTime += dt;

            if (elapsedTime >= timeLimit()) {
                elapsedTime = timeLimit();
                state = State.FAIL;
            }
        }

        followCamera(dt);
    }

    // =========================
    // PLAYER MOVEMENT
    // =========================

    private void updatePlayer(float dt) {

        float move = 0f;

        if (
            Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        ) {
            move -= 1f;
        }

        if (
            Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        ) {
            move += 1f;
        }

        playerX = MathUtils.clamp(
            playerX + move * PLAYER_SPEED * dt,
            PLAYER_W / 2f,
            WORLD_W - PLAYER_W / 2f
        );

        boolean jump =
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        boolean down =
            Gdx.input.isKeyPressed(Input.Keys.S)
                || Gdx.input.isKeyPressed(Input.Keys.DOWN);

        dropTimer = Math.max(
            0f, dropTimer - dt
        );

        float floorTop =
            activeFloor().y + activeFloor().height;

        if (
            grounded && jump && down
                && playerY > floorTop + 4f
        ) {

            dropTimer = 0.35f;

            playerY -= 6f;
            verticalSpeed = -120f;
            grounded = false;

        } else if (grounded && jump) {

            verticalSpeed = JUMP_SPEED;
            grounded = false;
        }

        float previousFeet = playerY;

        verticalSpeed -= GRAVITY * dt;
        playerY += verticalSpeed * dt;

        grounded = false;

        if (
            verticalSpeed <= 0f
                && dropTimer <= 0f
        ) {

            float landing = -1f;

            for (Rectangle p : activePlatforms()) {

                boolean overlap =
                    playerX + PLAYER_W * 0.35f > p.x
                        && playerX - PLAYER_W * 0.35f
                        < p.x + p.width;

                float top = p.y + p.height;

                if (
                    overlap
                        && previousFeet >= top - 3f
                        && playerY <= top
                        && top > landing
                ) {
                    landing = top;
                }
            }

            if (landing >= 0f) {

                playerY = landing;
                verticalSpeed = 0f;
                grounded = true;
            }
        }

        if (playerY <= floorTop) {

            playerY = floorTop;
            verticalSpeed = 0f;
            grounded = true;
        }
    }

    // =========================
    // CAMERA
    // =========================

    private void followCamera(float dt) {

        float halfW = VIEW_W / 2f;
        float halfH = VIEW_H / 2f;

        float targetX = MathUtils.clamp(
            playerX,
            halfW,
            WORLD_W - halfW
        );

        float targetY = MathUtils.clamp(
            playerY + PLAYER_H * 0.5f + 105f,
            halfH,
            WORLD_H - halfH
        );

        float lerp =
            1f - (float) Math.exp(-6f * dt);

        worldCamera.position.x = MathUtils.lerp(
            worldCamera.position.x,
            targetX,
            lerp
        );

        worldCamera.position.y = MathUtils.lerp(
            worldCamera.position.y,
            targetY,
            lerp
        );

        worldCamera.update();
    }

    // =========================
    // AIM / INVENTORY
    // =========================

    private void updateAim() {

        pointer.set(
            Gdx.input.getX(),
            Gdx.input.getY(),
            0
        );

        worldViewport.unproject(pointer);

        float dx = pointer.x - playerX;

        float dy =
            pointer.y - (playerY + PLAYER_H * 0.5f);

        float length = (float) Math.sqrt(
            dx * dx + dy * dy
        );

        if (length > 0.001f) {

            aimX = dx / length;
            aimY = dy / length;
        }
    }

    private void updateWeaponSelection() {

        if (
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)
                || Gdx.input.isKeyJustPressed(
                Input.Keys.NUMPAD_1
            )
        ) {
            selectWeapon(SWATTER);
        }

        if (
            stage >= 2
                && (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.NUM_2
                )
                    || Gdx.input.isKeyJustPressed(
                    Input.Keys.NUMPAD_2
                )
            )
        ) {
            selectWeapon(FLAME);
        }

        if (
            stage >= 3
                && (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.NUM_3
                )
                    || Gdx.input.isKeyJustPressed(
                    Input.Keys.NUMPAD_3
                )
            )
        ) {
            selectWeapon(INSECTICIDE);
        }
    }

    private void selectWeapon(int next) {

        weapon = next;

        continuousSpray = false;
        sprayTimer = 0f;
        sprayTickTimer = 0f;
        lastSprayClick = -10f;
    }

    // =========================
    // DAMAGE / DODGE
    // =========================

    private void damageBug(Bug bug) {

        // Japanese cockroach dodges the first hit.
        if (bug.type == 1 && bug.health == 2) {

            bug.dodgeTimer = 0.8f;
        }

        bug.health--;
    }

    private void playHitSound(float volume) {

        if (hitSound != null) {
            hitSound.play(volume);
        }
    }

    // =========================
    // SWATTER ATTACK
    // =========================

    private void swatterAttack() {

        float originX = playerX;

        float originY =
            playerY + PLAYER_H * 0.5f;

        float bestDistance = Float.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i < bugs.size; i++) {

            Bug bug = bugs.get(i);

            float dx =
                bug.x + bug.w * 0.5f - originX;

            float dy =
                bug.y + bug.h * 0.5f - originY;

            float projection =
                dx * aimX + dy * aimY;

            float perpendicular = Math.abs(
                dx * aimY - dy * aimX
            );

            float radius =
                Math.max(bug.w, bug.h) * 0.40f;

            if (
                projection >= -radius
                    && projection <= ATTACK_REACH + radius
                    && perpendicular <= 36f + radius
                    && projection < bestDistance
            ) {

                bestIndex = i;
                bestDistance = projection;
            }
        }

        if (bestIndex >= 0) {

            Bug hit = bugs.get(bestIndex);

            damageBug(hit);

            if (hit.health <= 0) {

                bugs.removeIndex(bestIndex);
                killed++;
            }

            playHitSound(0.65f);
        }
    }

    // =========================
    // SPRAY CONTROLS
    // =========================

    private void handleSprayClick() {

        if (
            gameClock - lastSprayClick
                <= DOUBLE_CLICK
        ) {

            continuousSpray = !continuousSpray;

            sprayTimer =
                continuousSpray ? 0f : 0f;

        } else {

            continuousSpray = false;
            sprayTimer = SPRAY_DURATION;
        }

        lastSprayClick = gameClock;
        sprayTickTimer = 0f;
    }

    private void updateSpray(float dt) {

        if (weapon == SWATTER) {
            return;
        }

        boolean spraying =
            continuousSpray || sprayTimer > 0f;

        if (!spraying) {
            return;
        }

        if (!continuousSpray) {

            sprayTimer = Math.max(
                0f, sprayTimer - dt
            );
        }

        sprayTickTimer -= dt;

        if (sprayTickTimer <= 0f) {

            sprayAttack();
            sprayTickTimer = SPRAY_TICK;
        }
    }

    private void sprayAttack() {

        float reach =
            weapon == FLAME
                ? FLAME_REACH
                : INSECT_REACH;

        float originY =
            playerY + PLAYER_H * 0.5f;

        for (int i = bugs.size - 1; i >= 0; i--) {

            Bug bug = bugs.get(i);

            float dx =
                bug.x + bug.w * 0.5f - playerX;

            float dy =
                bug.y + bug.h * 0.5f - originY;

            float projection =
                dx * aimX + dy * aimY;

            float perpendicular = Math.abs(
                dx * aimY - dy * aimX
            );

            float radius =
                Math.max(bug.w, bug.h) * 0.40f;

            if (
                projection >= -radius
                    && projection <= reach + radius
                    && perpendicular <= 45f + radius
            ) {

                damageBug(bug);

                if (bug.health <= 0) {

                    bugs.removeIndex(i);
                    killed++;
                }

                playHitSound(0.25f);
            }
        }
    }

    // =========================
    // WORLD RENDERING
    // =========================

    private Texture currentBackground() {

        if (stage == 1) {
            return blueBackground;
        }

        if (stage == 2) {
            return yellowBackground;
        }

        return pinkBackground;
    }

    private void renderWorld() {

        worldViewport.apply();

        batch.setProjectionMatrix(
            worldCamera.combined
        );

        batch.begin();

        batch.setColor(Color.WHITE);

        batch.draw(
            currentBackground(),
            0, 0,
            WORLD_W, WORLD_H
        );

        drawBugs();
        drawPlayer();

        batch.end();
    }

    private void drawBugs() {

        for (Bug bug : bugs) {

            Texture texture =
                bugTexture(bug.type);

            batch.setColor(Color.WHITE);

            batch.draw(
                texture,
                bug.x,
                bug.y,
                bug.w / 2f,
                bug.h / 2f,
                bug.w,
                bug.h,
                1f,
                1f,
                bug.speed >= 0f ? -90f : 90f,
                0,
                0,
                texture.getWidth(),
                texture.getHeight(),
                false,
                false
            );

            if (bug.dodgeTimer > 0f) {

                if (koreanFontAvailable) {

                    dodgeFont.draw(
                        batch,
                        "회피!",
                        bug.x,
                        bug.y + bug.h + 34f
                    );

                } else {

                    font.draw(
                        batch,
                        "DODGE!",
                        bug.x,
                        bug.y + bug.h + 34f
                    );
                }
            }
        }
    }

    private Texture heldWeapon() {

        if (weapon == SWATTER) {
            return swatter;
        }

        boolean active =
            continuousSpray || sprayTimer > 0f;

        if (weapon == FLAME) {

            if (active && runFlame != null) {
                return runFlame;
            }

            return flame;
        }

        if (active && runInsecticide != null) {
            return runInsecticide;
        }

        return insecticide;
    }

    private void drawPlayer() {

        batch.setColor(
            0f, 0f, 0f, 0.20f
        );

        batch.draw(
            white,
            playerX - 25f,
            playerY - 4f,
            50f,
            6f
        );

        batch.setColor(Color.WHITE);

        batch.draw(
            player,
            playerX - PLAYER_W / 2f,
            playerY,
            PLAYER_W,
            PLAYER_H
        );

        Texture held = heldWeapon();

        if (held == null) {
            return;
        }

        float aspect =
            held.getWidth()
                / (float) held.getHeight();

        float h =
            weapon == SWATTER
                ? (swingTimer > 0f ? 100f : 80f)
                : 115f;

        float w = h * aspect;

        float wx =
            playerX + aimX * 55f;

        float wy =
            playerY + PLAYER_H * 0.5f
                + aimY * 55f;

        float angle =
            MathUtils.atan2(aimY, aimX)
                * MathUtils.radiansToDegrees
                - 90f;

        batch.draw(
            held,
            wx - w / 2f,
            wy - h / 2f,
            w / 2f,
            h / 2f,
            w,
            h,
            1f,
            1f,
            angle,
            0,
            0,
            held.getWidth(),
            held.getHeight(),
            false,
            false
        );
    }

    // =========================
    // UI
    // =========================

    private void renderUI() {

        uiViewport.apply();

        batch.setProjectionMatrix(
            uiCamera.combined
        );

        batch.begin();

        batch.setColor(Color.WHITE);

        if (state == State.START) {
            // The START text and button are already part of startscreen.png.
            drawScreenBackground(startImage);
        } else if (state == State.RULE1) {
            drawRule(gameRule1, "아래에 enter키를 눌러 넘어가기", "PRESS ENTER TO CONTINUE");
        } else if (state == State.RULE2) {
            drawRule(gameRule2, "아래에 enter키를 눌러 넘어가기", "PRESS ENTER TO START");
        } else if (state == State.LEVEL2_RULE) {
            drawRule(level2Rule, "키보드 2번 버튼을 누르면 아이템 선택 가능", "PRESS 2 TO SELECT FLAMETHROWER - ENTER");
        } else if (state == State.LEVEL3_RULE) {
            drawRule(level3Rule, "키보드 3번을 누르면 아이템 선택가능", "PRESS 3 TO SELECT INSECTICIDE - ENTER");
        } else if (state == State.PAUSE) {
            drawHUD();
            drawInventory();
            drawPauseOverlay();
        } else if (state == State.CLEAR) {

            drawScreenBackground(successImage);

            drawOverlay(0.45f);

            font.draw(
                batch,
                "LEVEL " + stage + " CLEAR!  "
                    + target() + " / " + target(),
                270, 380
            );

            String nextText =
                stage < 3
                    ? "ENTER / CLICK FOR LEVEL " + (stage + 1)
                    : "ALL CLEAR! ENTER TO RESTART";

            font.draw(
                batch,
                nextText,
                265, 300
            );

        } else if (state == State.FAIL) {

            drawScreenBackground(failImage);

            drawOverlay(0.55f);

            font.draw(
                batch,
                "TIME OVER! "
                    + killed + " / " + target(),
                330, 380
            );

            font.draw(
                batch,
                "ENTER / CLICK TO RESTART",
                290, 300
            );

        } else {

            drawHUD();
            drawInventory();
            drawPauseHint();
        }

        batch.end();
    }


    private void createPauseFonts() {
        pauseTitleFont = new BitmapFont();
        pauseInfoFont = new BitmapFont();
        com.badlogic.gdx.files.FileHandle file = null;
        if (Gdx.files.internal("malgun.ttf").exists())
            file = Gdx.files.internal("malgun.ttf");
        else if (Gdx.files.absolute("C:/Windows/Fonts/malgun.ttf").exists())
            file = Gdx.files.absolute("C:/Windows/Fonts/malgun.ttf");
        if (file != null) {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(file);
            FreeTypeFontGenerator.FreeTypeFontParameter p =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.characters = "일시정지다를눌러해제Esc ";
            p.color = Color.RED;
            p.size = 64;
            pauseTitleFont.dispose();
            pauseTitleFont = gen.generateFont(p);
            p.size = 26;
            pauseInfoFont.dispose();
            pauseInfoFont = gen.generateFont(p);
            gen.dispose();
        } else {
            pauseTitleFont.getData().setScale(4f);
            pauseInfoFont.getData().setScale(1.6f);
        }
    }

    private void drawCentered(BitmapFont useFont, String message, float centerY) {
        pauseLayout.setText(useFont, message);
        useFont.draw(batch, pauseLayout, (VIEW_W - pauseLayout.width) / 2f, centerY);
    }

    private void drawPauseHint() {
        String message = "Esc를 눌러 일시정지";
        pauseLayout.setText(pauseInfoFont, message);
        float x = VIEW_W - pauseLayout.width - 16f;
        float y = VIEW_H - 88f;
        batch.setColor(1f, 1f, 1f, 0.85f);
        batch.draw(white, x - 9f, y - pauseLayout.height - 5f,
            pauseLayout.width + 18f, pauseLayout.height + 15f);
        batch.setColor(Color.RED);
        pauseInfoFont.draw(batch, pauseLayout, x, y);
        batch.setColor(Color.WHITE);
    }

    private void drawPauseOverlay() {
        batch.setColor(0.48f, 0.48f, 0.48f, 0.90f);
        batch.draw(white, 0f, 0f, VIEW_W, VIEW_H);
        batch.setColor(Color.RED);
        drawCentered(pauseTitleFont, "일시정지", VIEW_H / 2f + 35f);
        drawCentered(pauseInfoFont, "다시 Esc를 눌러 일시정지 해제", VIEW_H / 2f - 35f);
        batch.setColor(Color.WHITE);
    }

    private void drawScreenBackground(
        Texture screen
    ) {

        Texture image =
            screen != null
                ? screen
                : currentBackground();

        batch.draw(
            image,
            0, 0,
            VIEW_W, VIEW_H
        );
    }

    private void drawOverlay(float alpha) {

        batch.setColor(
            0f, 0.10f, 0.22f, alpha
        );

        batch.draw(
            white,
            0, 0,
            VIEW_W, VIEW_H
        );

        batch.setColor(Color.WHITE);
    }

    private void drawHUD() {

        batch.setColor(
            0.05f, 0.24f, 0.49f, 0.85f
        );

        batch.draw(
            white,
            15,
            VIEW_H - 70,
            610,
            55
        );

        batch.setColor(Color.WHITE);

        int remaining = (int) Math.ceil(
            Math.max(
                0f,
                timeLimit() - elapsedTime
            )
        );

        String clock = String.format(
            Locale.ROOT,
            "%02d:%02d",
            remaining / 60,
            remaining % 60
        );

        font.draw(
            batch,
            "LEVEL " + stage
                + "    " + clock
                + "    " + killed
                + " / " + target(),
            30,
            VIEW_H - 31
        );
    }

    private void drawInventory() {

        float size = 48f;
        float gap = 8f;

        float x0 = 680f;
        float y = VIEW_H - 69f;

        Texture[] icons = {
            swatter,
            flame,
            insecticide
        };

        for (int i = 0; i < 3; i++) {

            float x =
                x0 + i * (size + gap);

            // Selected slot border
            batch.setColor(
                i == weapon
                    ? Color.GOLD
                    : Color.valueOf("2365A1")
            );

            batch.draw(
                white,
                x - 3f,
                y - 3f,
                size + 6f,
                size + 6f
            );

            // White slot
            batch.setColor(Color.WHITE);

            batch.draw(
                white,
                x,
                y,
                size,
                size
            );

            // Only unlocked weapons are visible.
            if (i < stage && icons[i] != null) {

                Texture icon = icons[i];

                float scale = Math.min(
                    (size - 10f) / icon.getWidth(),
                    (size - 10f) / icon.getHeight()
                );

                float w =
                    icon.getWidth() * scale;

                float h =
                    icon.getHeight() * scale;

                batch.draw(
                    icon,
                    x + (size - w) / 2f,
                    y + (size - h) / 2f,
                    w,
                    h
                );
            }
        }
    }

    // =========================
    // RESIZE / DISPOSE
    // =========================

    @Override
    public void resize(int width, int height) {

        worldViewport.update(
            width, height, false
        );

        uiViewport.update(
            width, height, true
        );
    }

    private void disposeTexture(Texture texture) {

        if (texture != null) {
            texture.dispose();
        }
    }

    @Override
    public void dispose() {

        batch.dispose();

        disposeTexture(blueBackground);
        disposeTexture(yellowBackground);
        disposeTexture(pinkBackground);

        disposeTexture(player);

        disposeTexture(americaTexture);
        disposeTexture(japanTexture);
        disposeTexture(germanTexture);

        disposeTexture(swatter);

        disposeTexture(flame);
        disposeTexture(runFlame);

        disposeTexture(insecticide);
        disposeTexture(runInsecticide);

        disposeTexture(startImage);
        disposeTexture(gameRule1);
        disposeTexture(gameRule2);
        disposeTexture(level2Rule);
        disposeTexture(level3Rule);
        disposeTexture(successImage);
        disposeTexture(failImage);

        disposeTexture(white);

        if (hitSound != null) {
            hitSound.dispose();
        }

        font.dispose();
        dodgeFont.dispose();
        ruleFont.dispose();
        pauseTitleFont.dispose();
        pauseInfoFont.dispose();
    }
}


