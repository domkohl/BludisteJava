package app.renderer;

import app.maze.Box;
import app.maze.FindWayBFS;
import app.maze.MazeLoader;
import app.npc.Enemy;
import app.npc.OBJreader;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.Vec3D;
import utils.AbstractRenderer;
import utils.GLCamera;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Locale;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static utils.GluUtils.gluPerspective;


/**
 * Třída pro Renderování bludište a práce s ním
 */
public class Renderer extends AbstractRenderer {

    Box[][] boxes;
    boolean showCursor = true;
    private GLCamera camera;
    private GLCamera cameraTeleport;
    private float azimutTeport, zenitTeleport;
    private float dx, dy, ox, oy;
    private OGLTexture2D[] textureCube;
    private float azimut, zenit;
    private OGLTexture2D texture1, texture2, textureFinish, textureStart,textureHelp,textureKing,texturePause,texturePauseFinish,textureIsDead;
    private int countOfDeads;


    private long oldmils;
    private long oldFPSmils;
    private double fps;
    float step;
    private float[] modelMatrixEnemy = new float[16];
    boolean animateStart;
    boolean animaceRun;

    boolean prechodhrana;
    private float startBod,finishBod;
    boolean newMove;
    boolean firstTimeRenderEnemy = true;
    boolean showHelp,pauseGame,inFinish,isPlayerDead,savedTeleportPosition,loadedTeleportPosition,loadedTeleportFailed,renderObjV;
    long milsSave,millsTeleport,millsTeleportFailed;

    FindWayBFS findWay = new FindWayBFS();

    OBJreader obj;

    Enemy enemy;


    //Klavesnice
    boolean isPressedW,isPressedA,isPressedS,isPressedD;

    MazeLoader maze;


    public Renderer() {
        super();


        //Základni ovladaní prostredi - zmensovani zvetsovani okna
        glfwWindowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        };

        //rozhlizeni
        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (pauseGame) return;
                if (!showCursor) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    dx = (float) x - ox;
                    dy = (float) y - oy;
                    ox = (float) x;
                    oy = (float) y;
                    zenit -= dy / width * 180;
                    if (zenit > 90)
                        zenit = 90;
                    if (zenit <= -90)
                        zenit = -90;
                    azimut += dx / height * 180;
                    azimut = azimut % 360;
                    camera.setAzimuth(Math.toRadians(azimut));
                    camera.setZenith(Math.toRadians(zenit));
                    dx = 0;
                    dy = 0;
                }
            }
        };
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
            }
        };



        //Pohyb
        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_R && action == GLFW_PRESS && pauseGame) {

                    isPressedW = false;
                    isPressedA = false;
                    isPressedS= false;
                    isPressedD = false;
                    if (isPlayerDead)
                        countOfDeads++;
                    if (inFinish)
                        countOfDeads = 0;
                    Arrays.fill(modelMatrixEnemy, 1);
                    firstTimeRenderEnemy = true;
                    animaceRun = false;

                    for (int i = 0; i < maze.getPocetKrychli(); i++) {
                        for (int j = 0; j < maze.getPocetKrychli(); j++) {
                            maze.setRozlozeniBludiste(i,j,maze.getRozlozeniBludisteBackUp(i,j));
                        }
                    }
                    camera.setAzimuth(0);
                    camera.setZenith(0);
                    azimut = 0;
                    zenit = 0;
                    camera.setPosition(new Vec3D(maze.getSpawnX() * maze.getZmenseni(), 5 * maze.getZmenseni(), maze.getSpawnZ() * maze.getZmenseni()));
                    showHelp = false;

                    pauseGame = false;
                    isPlayerDead = false;
                    inFinish = false;

                    showCursor = false;

                    maze.setCurrenI(maze.getSpawnI());
                    maze.setCurrenJ(maze.getSpawnJ());
                    DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xBuffer, yBuffer);
                    double x = xBuffer.get(0);
                    double y = yBuffer.get(0);
                    ox = (float) x;
                    oy = (float) y;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

                }
                // Vypnutí hry, když je hra pozastavena
                if (key == GLFW_KEY_K && action == GLFW_PRESS && pauseGame) {
                    glfwSetWindowShouldClose(window, true);
                    System.exit(0);
                }
                // Pozastavení hry
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    pauseGame = !pauseGame;
                    showCursor = !showCursor;
                    if (showCursor) {
                        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    } else {
                        DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                        DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                        glfwGetCursorPos(window, xBuffer, yBuffer);
                        double x = xBuffer.get(0);
                        double y = yBuffer.get(0);
                        ox = (float) x;
                        oy = (float) y;
                        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    }
                }

                // Kontrola, zda je hra pozastavena, když ano vracím se a dále nic nekontroluji
                if (pauseGame) return;

                // Uložení pozice kamery pro teleport
                if (key == GLFW_KEY_U && action == GLFW_PRESS) {
                    milsSave = System.currentTimeMillis();
                    savedTeleportPosition = true;
                    azimutTeport = azimut;
                    zenitTeleport = zenit;
                    cameraTeleport = new GLCamera(camera);
                }

                // Teleportace - nahraní uložené kamery
                if (key == GLFW_KEY_T && action == GLFW_PRESS) {
                    if (cameraTeleport != null) {
                        millsTeleport = System.currentTimeMillis();
                        loadedTeleportPosition = true;
                        azimut = azimutTeport;
                        zenit = zenitTeleport;
                        camera = new GLCamera(cameraTeleport);
                    } else {
                        millsTeleportFailed = System.currentTimeMillis();
                        loadedTeleportFailed = true;
                        System.out.println("Nejdrive nastav misto pro teleport");
                    }
                }


                // Pohyb hráče
                //vice ifu kvuli tomu ze press se resetuje asi po chbyli a zacne hlasit flase
                //W
                if (key == GLFW_KEY_W && action == GLFW_PRESS)
                    isPressedW = true;
                if (key == GLFW_KEY_W && action == GLFW_RELEASE)
                    isPressedW = false;
                //S
                if (key == GLFW_KEY_S && action == GLFW_PRESS)
                    isPressedS = true;
                if (key == GLFW_KEY_S && action == GLFW_RELEASE)
                    isPressedS = false;
                //A
                if (key == GLFW_KEY_A && action == GLFW_PRESS)
                    isPressedA = true;
                if (key == GLFW_KEY_A && action == GLFW_RELEASE)
                    isPressedA = false;
                //D
                if (key == GLFW_KEY_D && action == GLFW_PRESS)
                    isPressedD = true;
                if (key == GLFW_KEY_D && action == GLFW_RELEASE)
                    isPressedD = false;

                // Vypnutí NPC -- dočasné
                // TODO odstranit
                if (key == GLFW_KEY_E && action == GLFW_PRESS) {
                    animateStart = !animateStart;
                }
                if (key == GLFW_KEY_Q && action == GLFW_PRESS) {
                    renderObjV = !renderObjV;
                }
                // Přepínání mezi režimem s nápovědou a s NPC
                if (key == GLFW_KEY_H && action == GLFW_PRESS) {
                    showHelp = !showHelp;
                    if (!showHelp) {
                        Arrays.fill(modelMatrixEnemy, 1);
                        firstTimeRenderEnemy = true;
                        animaceRun = false;
                        for (int i = 0; i < maze.getPocetKrychli(); i++) {
                            for (int j = 0; j < maze.getPocetKrychli(); j++) {
//                                rozlozeniBludiste[i][j] = rozlozeniBludisteBackUp[i][j];
                                maze.setRozlozeniBludiste(i,j,maze.getRozlozeniBludisteBackUp(i,j));
                            }
                        }
                    }else{
                        enemy.setEnemyPosI(-1);
                        enemy.setEnemyPosJ(-1);
                        int[][] tmpBludiste = findWay.shortestPath(maze.getRozlozeniBludisteNoEnemy(), new int[]{maze.getCurrenI(), maze.getCurrenJ()}, new int[]{maze.getFinishI(), maze.getFinishJ()});
                        for (int i = 0; i < maze.getPocetKrychli(); i++) {
                            for (int j = 0; j < maze.getPocetKrychli(); j++) {
                                maze.setRozlozeniBludiste(i,j,tmpBludiste[i][j]);
                            }
                        }
                    }
                }
            }
        };
    }

    //Inicializace bludiste
    @Override
    public void init() {
//        super.init();

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//        glPolygonMode(GL_FRONT, GL_FILL);
//        glPolygonMode(GL_BACK, GL_FILL);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        textureCube = new OGLTexture2D[6];
        try {
            texture1 = new OGLTexture2D("textures/floor.jpg");
            texture2 = new OGLTexture2D("textures/wall.jpg");
            textureFinish = new OGLTexture2D("textures/finish.jpg");
            textureStart = new OGLTexture2D("textures/start.jpg");
            textureHelp = new OGLTexture2D("textures/help.jpg");
            textureKing = new OGLTexture2D("textures/king.jpg");
            texturePause = new OGLTexture2D("textures/pause.png");
            texturePauseFinish = new OGLTexture2D("textures/pauseFinish.png");
            textureIsDead = new OGLTexture2D("textures/youDied.png");
            textureCube[0] = new OGLTexture2D("textures/right.png");
            textureCube[1] = new OGLTexture2D("textures/left.png");
            textureCube[2] = new OGLTexture2D("textures/top.png");
            textureCube[3] = new OGLTexture2D("textures/bottom.png");
            textureCube[4] = new OGLTexture2D("textures/front.png");
            textureCube[5] = new OGLTexture2D("textures/back.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        //TODO zeptat se
//        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_POINT);
//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        maze = new MazeLoader();

        camera = new GLCamera();

        camera.setPosition(new Vec3D(maze.getSpawnX() * maze.getZmenseni(), 5 * maze.getZmenseni(), maze.getSpawnZ() * maze.getZmenseni()));

        boxes = maze.getBoxes();

        //vytvorim jednotkovou matici
        Arrays.fill(modelMatrixEnemy, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScalef(maze.getZmenseni(), maze.getZmenseni(), maze.getZmenseni());
        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrixEnemy);

        //objekt
        obj = new OBJreader();

        pauseGame = true;

        textRenderer = new OGLTextRenderer(width, height);

        countOfDeads = 0;
        animateStart = true;

        enemy = new Enemy(maze.getDelkaHrany());
    }


    //Funkce pro neustale vykreslovani
    @Override
    public void display() {

        // kontroluji stavy hry a podle toho se chovám
        if (pauseGame || inFinish || isPlayerDead) {
            texturePause.bind();
            if (isPlayerDead)
                textureIsDead.bind();
            if (inFinish)
                texturePauseFinish.bind();
            glViewport(0, 0, width, height);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            // TODO zeptat se ?
//            glOrtho(1,1,1,1,0.1,20);
//            gluLookAt(
//                    1, 0, 5,
//                    0, 0,0,
//                    0, 0, 1
//            );
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2f(-1f, -1f);
            glTexCoord2f(1, 0);
            glVertex2f(1, -1);
            glTexCoord2f(1, 1);
            glVertex2f(1, 1);
            glTexCoord2f(0, 1);
            glVertex2f(-1, 1);
            glEnd();

            textRenderer.resize(width, height);
            textRenderer.clear();
            textRenderer.addStr2D(2, 17, "Počet smrtí: " + countOfDeads);
            textRenderer.draw();
            return;
        }

        // vypocet fps, nastaveni rychlosti animace podle rychlosti prekresleni
        long mils = System.currentTimeMillis();
//        System.out.println(mils);
        if ((mils - oldFPSmils) > 300) {
            fps = 1000 / (double) (mils - oldmils + 1);
            oldFPSmils = mils;
        }
        String textInfo = String.format(Locale.US, "FPS %3.1f", fps);

//        System.out.println(fps);
        float speed = 20; // pocet stupnu rotace za vterinu
//        System.out.println(step);
        step = speed * (mils - oldmils) / 1000.0f; // krok za jedno
        float stepCamera = speed * (mils - oldmils) / 1000.0f; // krok za jedno
        oldmils = mils;



        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(0f, 0f, 0f, 1f);

        //Modelovaci
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScalef(maze.getZmenseni(), maze.getZmenseni(), maze.getZmenseni());
        //Projekcni
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, width / (float) height, 0.01f, 10000.0f);

        camera.setFirstPerson(true);
        Vec3D cameraFixedY = camera.getPosition();
        camera.setPosition(cameraFixedY.withY(0.20));
        camera.setMatrix();

        texture1.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

        skyBox();


        renderMaze();
        if(renderObjV)
//            Executors.newSingleThreadExecutor().execute(new Runnable() {
//                @Override
//                public void run() {
//                    synchronized (this){
//                    glfwMakeContextCurrent(window);
//                    renderObj();
//                    glfwMakeContextCurrent(MemoryUtil.NULL);
//                    }
//                }
//            });
        renderObj();

        //zjisteni zda me zasahlo np kdyz ano zareaguji
        if (maze.getCurrenI() == enemy.getEnemyPosI() && maze.getCurrenJ() == enemy.getEnemyPosJ()) {
//            inFinish = true;
            isPlayerDead = true;
            pauseGame = true;
            System.out.println("jsi mrtvy");
        }


        //Ovládani klávesnice zda kvůli lepší simulaci ovládání jako ve hře
        //W
        if (isPressedW && !isPressedD && !isPressedA && !isPressedS) {
            GLCamera tmp = new GLCamera(camera);
            tmp.forward(0.03);
            if (maze.isOutside(tmp) == 0)
                camera.forward(0.03);
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }
//
        //S
        if (isPressedS && !isPressedD && !isPressedA && !isPressedW) {
            GLCamera tmp = new GLCamera(camera);
            tmp.backward(0.03);
            if (maze.isOutside(tmp) == 0)
                camera.backward(0.03);
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }
        //A
        if (isPressedA && !isPressedD && !isPressedW && !isPressedS) {
            GLCamera tmp = new GLCamera(camera);
            tmp.left(0.03);
            if (maze.isOutside(tmp) == 0)
                camera.left(0.03);
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }
        //D
        if (isPressedD && !isPressedW && !isPressedA && !isPressedS) {
            GLCamera tmp = new GLCamera(camera);
            tmp.right(0.03);
            if (maze.isOutside(tmp) == 0)
                camera.right(0.03);
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }
        //W+D
        if (isPressedW && isPressedD && !isPressedA && !isPressedS) {
            GLCamera tmp = new GLCamera(camera);
            tmp.move(new Vec3D(
                    -Math.sin(camera.getAzimuth() - 3f / 4 * Math.PI),
                    0.0f,
                    +Math.cos(camera.getAzimuth() - 3f / 4 * Math.PI))
                    .mul(0.03));
            if (maze.isOutside(tmp) == 0)
                camera.move(new Vec3D(
                        -Math.sin(camera.getAzimuth() - 3f / 4 * Math.PI),
                        0.0f,
                        +Math.cos(camera.getAzimuth() - 3f / 4 * Math.PI))
                        .mul(0.03));
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }

        //W+A
        if (isPressedW && isPressedA && !isPressedD && !isPressedS) {
            GLCamera tmp = new GLCamera(camera);
            tmp.move(new Vec3D(
                    -Math.sin(camera.getAzimuth() - Math.PI / 4),
                    0.0f,
                    +Math.cos(camera.getAzimuth() - Math.PI / 4))
                    .mul(-0.03));
            if (maze.isOutside(tmp) == 0)
                camera.move(new Vec3D(
                        -Math.sin(camera.getAzimuth() - Math.PI / 4),
                        0.0f,
                        +Math.cos(camera.getAzimuth() - Math.PI / 4))
                        .mul(-0.03));
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }

        //S+D
        if (isPressedS && isPressedD && !isPressedW && !isPressedA) {
            GLCamera tmp = new GLCamera(camera);
            tmp.move(new Vec3D(
                    -Math.sin(camera.getAzimuth() - Math.PI / 4),
                    0.0f,
                    +Math.cos(camera.getAzimuth() - Math.PI / 4))
                    .mul(0.03));
            if (maze.isOutside(tmp) == 0)
                camera.move(new Vec3D(
                        -Math.sin(camera.getAzimuth() - Math.PI / 4),
                        0.0f,
                        +Math.cos(camera.getAzimuth() - Math.PI / 4))
                        .mul(0.03));
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }
        //S+A
        if (isPressedS && isPressedA && !isPressedW && !isPressedD) {
            GLCamera tmp = new GLCamera(camera);
            tmp.move(new Vec3D(
                    -Math.sin(camera.getAzimuth() - 3f / 4 * Math.PI),
                    0.0f,
                    +Math.cos(camera.getAzimuth() - 3f / 4 * Math.PI))
                    .mul(-0.03));
            if (maze.isOutside(tmp) == 0)
                camera.move(new Vec3D(
                        -Math.sin(camera.getAzimuth() - 3f / 4 * Math.PI),
                        0.0f,
                        +Math.cos(camera.getAzimuth() - 3f / 4 * Math.PI))
                        .mul(-0.03));
            if (maze.isOutside(tmp) == 2) {
                pauseGame = true;
                inFinish = true;
                System.out.println("Gratuluji jsi v cíli");
            }
        }

        //zapiani a vypinani pomoci
        //nahrani bludiscte pok akzdem kliku/drzeni klavesy
        if (showHelp && (isPressedS||isPressedA||isPressedD||isPressedW)) {
            int[][] tmpBludiste = findWay.shortestPath(maze.getRozlozeniBludisteNoEnemy(), new int[]{maze.getCurrenI(), maze.getCurrenJ()}, new int[]{maze.getFinishI(), maze.getFinishJ()});
            for (int i = 0; i < maze.getPocetKrychli(); i++) {
                for (int j = 0; j < maze.getPocetKrychli(); j++) {
                    maze.setRozlozeniBludiste(i,j,tmpBludiste[i][j]);
                }
            }
        }

        // zmizení textu po 1s pro oznaméní informace o teleportu
        if (savedTeleportPosition && (mils - milsSave) > 1000)
            savedTeleportPosition = false;
        if (loadedTeleportPosition && (mils - millsTeleport) > 1000)
            loadedTeleportPosition = false;
        if (loadedTeleportFailed && (mils - millsTeleportFailed) > 1000)
            loadedTeleportFailed = false;

        // Zobrazováni textu na obrazovce
        textRenderer.resize(width, height);
        textRenderer.clear();
        textRenderer.addStr2D(2, 17, textInfo);
        textRenderer.addStr2D(2, 38, "Počet smrtí: " + countOfDeads);
        if (savedTeleportPosition)
            textRenderer.addStr2D(2, height - 3, "Pozice pro teleport nastavena.");
        if (loadedTeleportPosition)
            textRenderer.addStr2D(2, height - 3, "Byl jsi teleportován.");
        if (loadedTeleportFailed)
            textRenderer.addStr2D(2, height - 3, "Nejdříve nastav místo pro teleportaci.");

        textRenderer.addStr2D(width - 315, height - 3, "Semestrální projekt – Dominik Kohl(c) PGRF2 UHK 2021");
        textRenderer.draw();

    }


    // Funkce pro vykreslení NPC - funkce se stará o to aby se npc(obj) pohybovalo ve spravném směru
    private void renderEnemy(int x, int y) {
//        System.out.println( "vlakno");
        if (!animateStart) return;
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        //zajisteni naharani matice pro npc
        if (newMove) {
            glLoadIdentity();
            glScalef(maze.getZmenseni(), maze.getZmenseni(), maze.getZmenseni());
            glTranslatef(maze.getJednaHrana() / 2f + x * maze.getJednaHrana(), 0f, maze.getJednaHrana() / 2f + y * maze.getJednaHrana());
            glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrixEnemy);
            newMove = false;
        }
        glLoadMatrixf(modelMatrixEnemy);

        //ptám se jaký smerem ma npc jit hodnota je uloze jako posledni v poli
        switch (enemy.getCurrentDestinationBlock()[2]) {
            case 1 -> glTranslatef(0, 0, step);
            case 2 -> glTranslatef(0, 0, -step);
            case 3 -> glTranslatef(step, 0, 0);
            case 4 -> glTranslatef(-step, 0, 0);
            default -> glTranslatef(0, 0, 0);
        }

        textureKing.bind();

        glBegin(GL_TRIANGLES);

        for (int[] indice : obj.getIndices()) {
//            System.out.println("test");
            glTexCoord2f(obj.getTextury().get(indice[1] - 1)[0], obj.getTextury().get(indice[1] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[0] - 1)[0], obj.getVrcholy().get(indice[0] - 1)[1], obj.getVrcholy().get(indice[0] - 1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[3] - 1)[0], obj.getTextury().get(indice[3] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[2] - 1)[0], obj.getVrcholy().get(indice[2] - 1)[1], obj.getVrcholy().get(indice[2] - 1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[5] - 1)[0], obj.getTextury().get(indice[5] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[4] - 1)[0], obj.getVrcholy().get(indice[4] - 1)[1], obj.getVrcholy().get(indice[4] - 1)[2]);
        }
//        for (int[] indice : obj.getIndices()) {
////            System.out.println("test");
//            glTexCoord2f(obj.getTextury().get(indice[1] - 1)[0], obj.getTextury().get(indice[1] - 1)[1]);
//            glVertex3f(obj.getVrcholy().get(indice[0] - 1)[0], obj.getVrcholy().get(indice[0] - 1)[1], obj.getVrcholy().get(indice[0] - 1)[2]);
//            glTexCoord2f(obj.getTextury().get(indice[3] - 1)[0], obj.getTextury().get(indice[3] - 1)[1]);
//            glVertex3f(obj.getVrcholy().get(indice[2] - 1)[0], obj.getVrcholy().get(indice[2] - 1)[1], obj.getVrcholy().get(indice[2] - 1)[2]);
//            glTexCoord2f(obj.getTextury().get(indice[5] - 1)[0], obj.getTextury().get(indice[5] - 1)[1]);
//            glVertex3f(obj.getVrcholy().get(indice[4] - 1)[0], obj.getVrcholy().get(indice[4] - 1)[1], obj.getVrcholy().get(indice[4] - 1)[2]);
//        }

        glEnd();
        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrixEnemy);
        glPopMatrix();

        //precahzim hranu nastavuji jiny papametry site
        if (startBod >= maze.getJednaHrana() / 2f) {
            prechodhrana = true;
//            rozlozeniBludiste[source[0]][source[1]] = 0;
            maze.setRozlozeniBludiste(enemy.getSource()[0],enemy.getSource()[1],0);

            maze.setRozlozeniBludiste(enemy.getCurrentDestinationBlock()[0],enemy.getCurrentDestinationBlock()[1],4);
        }
        if (startBod < finishBod)
            startBod = startBod + step;

        //vim ze animace skoncila a muzu najit  dalsi mozny blok pro pohyb
        if (startBod >= finishBod) {
            animaceRun = false;
            prechodhrana = false;
        }
    }

    //Vykresleni bludiste
    private void renderMaze() {
//        rozlozeniBludiste[3][7] = 3;
        for (int i = 0; i < maze.getPocetKrychli(); i++) {
            for (int j = 0; j < maze.getPocetKrychli(); j++) {
                if (maze.getRozlozeniBludiste(i,j) == 0) {
                    renderPlate(i, j);
                } else if (maze.getRozlozeniBludiste(i,j) == 3) {
                    renderFinish(i, j);
                } else if (maze.getRozlozeniBludiste(i,j) == 2) {
                    renderStart(i, j);
                } else if (maze.getRozlozeniBludiste(i,j) == 4) {
                    //ulozeni hodnot kde se nachazi enemy
                    enemy.setEnemyPosI(i);
                    enemy.setEnemyPosJ(j);

                    //kdyz renderuji enmy poprve v kazdem novem pohybbu nastavim mu prvni blok v poli puvodni aby se nemohol vratit ani tma od kud zacal
                    if (firstTimeRenderEnemy) {
                        enemy.getAllVisitedEnemy().add(new int[]{i, j, maze.getRozlozeniBludiste(i,j)});
                        firstTimeRenderEnemy = false;
                    }
                    //nove zapnuti pohybu vyhleadm blok a resetuji hodnoty pro poybu pro funkci kterea ho vykresluje
                    if (!animaceRun) {
                        enemy.possibleWaysEnemyGetDestination(i, j,maze.getRozlozeniBludiste());
                        startBod = 0f;
                        finishBod = maze.getJednaHrana();
                        animaceRun = true;
                        newMove = true;
                    }
                    if (prechodhrana) {
                        //ta predchozi, rpoze jeste nedoberhla animace ale blobk uz je prehozeni
//                        Executors.newSingleThreadExecutor().execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                glfwMakeContextCurrent(glfwGetPrimaryMonitor());
//                                renderEnemy(source[0], source[1]);
//                                glfwMakeContextCurrent(MemoryUtil.NULL);
//                            }
//                        });
                        renderEnemy(enemy.getSource()[0], enemy.getSource()[1]);
                    } else {
//                        int finalI = i;
//                        int finalJ = j;
//                        Executors.newSingleThreadExecutor().execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                glfwMakeContextCurrent(glfwGetPrimaryMonitor());
//                                renderEnemy(finalI, finalJ);
//                                glfwMakeContextCurrent(MemoryUtil.NULL);
//                            }
//                        });
                        renderEnemy(i, j);
                    }
                } else if (maze.getRozlozeniBludiste(i,j) == 5) {
                    renderPlateHelp(i, j);
                } else {
                    renderBox(i, j);
                }
            }
        }
        for (Box box : maze.getHelpBoxes()) {
            renderBox(box);
        }

    }
    //Vykresleni boxu/zdi matice
    private void renderBox(int x, int y) {
        texture2.bind();
        glBegin(GL_QUADS);
        glColor3f(0f, 1f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbUp1().getX(), (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getbUp2().getX(), (float) boxes[x][y].getbUp2().getY(), (float) boxes[x][y].getbUp2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp3().getX(), (float) boxes[x][y].getbUp3().getY(), (float) boxes[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX(), (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getbUp1().getX(), (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX(), (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp2().getX(), (float) boxes[x][y].getbUp2().getY(), (float) boxes[x][y].getbUp2().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp1().getX(), (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp3().getX(), (float) boxes[x][y].getbUp3().getY(), (float) boxes[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp2().getX(), (float) boxes[x][y].getbUp2().getY(), (float) boxes[x][y].getbUp2().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp3().getX(), (float) boxes[x][y].getbUp3().getY(), (float) boxes[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX(), (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ());

        glEnd();
    }

    //Vykresleni boxu/zdi pomoci boxu
    private void renderBox(Box box) {
        texture2.bind();
        glBegin(GL_QUADS);
        glColor3f(0f, 1f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) box.getbH().getX(), (float) box.getbH().getY(), (float) box.getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) box.getB2().getX(), (float) box.getB2().getY(), (float) box.getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) box.getB3().getX(), (float) box.getB3().getY(), (float) box.getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) box.getB4().getX(), (float) box.getB4().getY(), (float) box.getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) box.getbUp1().getX(), (float) box.getbUp1().getY(), (float) box.getbUp1().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) box.getbUp2().getX(), (float) box.getbUp2().getY(), (float) box.getbUp2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) box.getbUp3().getX(), (float) box.getbUp3().getY(), (float) box.getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) box.getbUp4().getX(), (float) box.getbUp4().getY(), (float) box.getbUp4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) box.getbH().getX(), (float) box.getbH().getY(), (float) box.getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) box.getbUp1().getX(), (float) box.getbUp1().getY(), (float) box.getbUp1().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) box.getbUp4().getX(), (float) box.getbUp4().getY(), (float) box.getbUp4().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) box.getB4().getX(), (float) box.getB4().getY(), (float) box.getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) box.getbH().getX(), (float) box.getbH().getY(), (float) box.getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) box.getB2().getX(), (float) box.getB2().getY(), (float) box.getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) box.getbUp2().getX(), (float) box.getbUp2().getY(), (float) box.getbUp2().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) box.getbUp1().getX(), (float) box.getbUp1().getY(), (float) box.getbUp1().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) box.getB2().getX(), (float) box.getB2().getY(), (float) box.getB2().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) box.getB3().getX(), (float) box.getB3().getY(), (float) box.getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) box.getbUp3().getX(), (float) box.getbUp3().getY(), (float) box.getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) box.getbUp2().getX(), (float) box.getbUp2().getY(), (float) box.getbUp2().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) box.getB4().getX(), (float) box.getB4().getY(), (float) box.getB4().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) box.getB3().getX(), (float) box.getB3().getY(), (float) box.getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) box.getbUp3().getX(), (float) box.getbUp3().getY(), (float) box.getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) box.getbUp4().getX(), (float) box.getbUp4().getY(), (float) box.getbUp4().getZ());

        glEnd();
    }

    //Vykresleni podlahy
    private void renderPlate(int x, int y) {
        texture1.bind();
        glBegin(GL_QUADS);
        glColor3f(1f, 0f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());

        glEnd();
    }

    private void renderPlateHelp(int x, int y) {
        textureHelp.bind();
        glBegin(GL_QUADS);
        glColor3f(1f, 0f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());

        glEnd();
    }

    //Vykresleni cile
    private void renderFinish(int x, int y) {
        textureFinish.bind();
        glBegin(GL_QUADS);
        glColor3f(0f, 1f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbUp1().getX(), (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getbUp2().getX(), (float) boxes[x][y].getbUp2().getY(), (float) boxes[x][y].getbUp2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp3().getX(), (float) boxes[x][y].getbUp3().getY(), (float) boxes[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX(), (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getbUp1().getX(), (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX(), (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp2().getX(), (float) boxes[x][y].getbUp2().getY(), (float) boxes[x][y].getbUp2().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp1().getX(), (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp3().getX(), (float) boxes[x][y].getbUp3().getY(), (float) boxes[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp2().getX(), (float) boxes[x][y].getbUp2().getY(), (float) boxes[x][y].getbUp2().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp3().getX(), (float) boxes[x][y].getbUp3().getY(), (float) boxes[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX(), (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ());

        glEnd();
    }

    //Vykresleni startu
    private void renderStart(int x, int y) {
        textureStart.bind();
        glBegin(GL_QUADS);
        glColor3f(1f, 0f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX(), (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getB2().getX(), (float) boxes[x][y].getB2().getY(), (float) boxes[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getB3().getX(), (float) boxes[x][y].getB3().getY(), (float) boxes[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX(), (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ());

        glEnd();
    }

    //Vykresleni skyboxu
    private void skyBox() {

        glPushMatrix();
        glColor3d(0.5, 0.5, 0.5);
        int size = 6 * maze.getDelkaHrany();

        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

        textureCube[1].bind(); //-x  (left)
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size, size, size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size, -size, size);
        glEnd();

        textureCube[0].bind();//+x  (right)
        glBegin(GL_QUADS);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size, size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, -size);
        glEnd();

        textureCube[3].bind(); //-y bottom
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, -size, size);
        glEnd();

        textureCube[2].bind(); //+y  top
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, size, size);
        glEnd();

        textureCube[5].bind(); //-z (back)
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size, size, -size);
        glEnd();

        textureCube[4].bind(); //+z (front)
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, size);
        glEnd();

        glPopMatrix();

        glEndList();
    }
    //TODO odstranit
    public void renderObj() {
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(maze.getZmenseni(), maze.getZmenseni(), maze.getZmenseni());
//        glRotatef(270,1,0,0);
//        glTranslatef(8.4f,1.1f,0);
        glTranslatef(10f, 0f, 10f);
        glEnable(GL_TEXTURE_2D);
//        glEnable(GL_LIGHTING);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        textureKing.bind();
        glBegin(GL_TRIANGLES);
//        glBegin(GL_QUAD_STRIP);
        glColor3f(1f, 1f, 1f);



        for (int[] indice : obj.getIndices()) {
            glTexCoord2f(obj.getTextury().get(indice[1] - 1)[0], obj.getTextury().get(indice[1] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[0] - 1)[0], obj.getVrcholy().get(indice[0] - 1)[1], obj.getVrcholy().get(indice[0] - 1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[3] - 1)[0], obj.getTextury().get(indice[3] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[2] - 1)[0], obj.getVrcholy().get(indice[2] - 1)[1], obj.getVrcholy().get(indice[2] - 1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[5] - 1)[0], obj.getTextury().get(indice[5] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[4] - 1)[0], obj.getVrcholy().get(indice[4] - 1)[1], obj.getVrcholy().get(indice[4] - 1)[2]);
        }
        glEnd();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glPopMatrix();
    }


}

