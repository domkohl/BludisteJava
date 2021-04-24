package app;

import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.Vec3D;
import utils.AbstractRenderer;
import utils.GLCamera;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static utils.GluUtils.gluLookAt;
import static utils.GluUtils.gluPerspective;


/**
 * Třída pro Renderování bludište a práce s ním
 */
public class Renderer extends AbstractRenderer {
    int pocetKrychli;
    int delkaHrany;
    int jednaHrana;
    int[][] rozlozeniBludiste;
    int[][] rozlozeniBludisteBackUp;
    int[][] rozlozeniBludisteNoEnemy;
    Box[][] boxes;
    ArrayList<Box> spawnHelpBoxes = new ArrayList<>();
    double spawnX, spawnZ;
    int spawnI, spawnJ;
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
    boolean animaceStop;
    boolean prechodhrana;
    private float startBod,finishBod;
    private int[] destiantion;
    private int[] source;
    private ArrayList<int[]> allVisitedEnemy = new ArrayList<>();
    boolean newMove;
    boolean firstTimeRenderEnemy = true;
    boolean showHelp,pauseGame,inFinish,isPlayerDead,savedTeleportPosition,loadedTeleportPosition;
    long milsSave,millsTeleport;

    FindWayBFS findWay = new FindWayBFS();
    int currenI, currenJ;
    int enemyI, enemyJ;


    OBJreader obj;


    OGLTexture2D.Viewer textureViewer;

    public Renderer() {
        super();
        //Zakladni ovladani prostredi
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
                if(pauseGame) return;
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
//                    System.out.println("Zenith: "+camera.getZenith());
//                    System.out.println("Azimtuh: "+camera.getAzimuth());
//                    System.out.println("VpX "+Math.cos(camera.getAzimuth())*Math.cos(camera.getZenith()));
                }

            }
        };
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {}};

        //Pohyb
        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
//                glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE);
                if (key == GLFW_KEY_R && action == GLFW_PRESS && pauseGame) {

                    if(isPlayerDead)
                        countOfDeads++;
                    if (inFinish)
                        countOfDeads = 0;


                    Arrays.fill(modelMatrixEnemy, 1);
                    firstTimeRenderEnemy = true;
                    animaceRun = false;

                    for (int i = 0; i < pocetKrychli; i++) {
                        for (int j = 0; j < pocetKrychli; j++) {
//                            if(rozlozeniBludiste[i][j] ==4){
//                                rozlozeniBludiste[i][j] = 4;
//                            }else {
//                                if(rozlozeniBludisteBackUp[i][j] !=4)
                            rozlozeniBludiste[i][j] = rozlozeniBludisteBackUp[i][j];
                        }
                    }

                    camera.setAzimuth(0);
                    camera.setZenith(0);
                    azimut = 0;
                    zenit = 0;
                    camera.setPosition(new Vec3D(spawnX * 0.04, 5 * 0.04, spawnZ * 0.04));
                    showHelp = false;

                    pauseGame = false;
                    isPlayerDead = false;
                    inFinish = false;

                    showCursor = false;
                    currenI = spawnI;
                    currenJ = spawnJ;
                    DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xBuffer, yBuffer);
                    double x = xBuffer.get(0);
                    double y = yBuffer.get(0);
                    ox = (float) x;
                    oy = (float) y;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

                }
                if (key == GLFW_KEY_K && action == GLFW_PRESS && pauseGame) {
                    glfwSetWindowShouldClose(window, true);
                    glfwFreeCallbacks(window);
                    glfwDestroyWindow(window);
//                    glfwTerminate();
                    dispose();
                    // Free the window callbacks and destroy the window
                    glfwFreeCallbacks(window);
                    glfwDestroyWindow(window);
//                try{
//                    // Free the window callbacks and destroy the window
//                    glfwFreeCallbacks(window);
//                    glfwDestroyWindow(window);
//
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                } finally {
//                    // Terminate GLFW and free the error callback
//                    glfwTerminate();
//                    glfwSetErrorCallback(null).free();
//                }
                }
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

                if(pauseGame) return;

                //ulozeni pozice pro teleport
                if (key == GLFW_KEY_U && action == GLFW_PRESS) {
                    milsSave =System.currentTimeMillis();
                    savedTeleportPosition = true;
                    azimutTeport = azimut;
                    zenitTeleport = zenit;
                    cameraTeleport = new GLCamera(camera);
                }
                //ulozeni pozice pro teleport
                if (key == GLFW_KEY_T && action == GLFW_PRESS) {
                    if(cameraTeleport != null){
                        millsTeleport = System.currentTimeMillis();
                        loadedTeleportPosition = true;
                        azimut = azimutTeport;
                        zenit = zenitTeleport;
                        camera = new GLCamera(cameraTeleport);
                    }else {
                        System.out.println("Nejdrive nastav misto pro teleport");
                    }
                }


                //W
                if (key == GLFW_KEY_W && glfwGetKey(window, GLFW_KEY_D) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_A) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_S) != GLFW_PRESS) {
                    System.out.println("rovne");
                    GLCamera tmp = new GLCamera(camera);
                    tmp.forward(0.04);
                    if (isOutside(tmp) == 0)
                        camera.forward(0.04);
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                    if (isOutside(tmp) == 1){
                        camera.backward(0.04);
                        camera.move( new Vec3D(
                                -Math.sin(camera.getAzimuth()),
                                0.0f,
                                Math.cos(camera.getAzimuth()))
                                .mul(-0.04));
                    }

                }
//                if (glfwGetKey(window, GLFW_KEY_D) == GLFW_RELEASE){
//                    checkKey(window,key);
//                }
                //S
                if (key == GLFW_KEY_S && glfwGetKey(window, GLFW_KEY_A) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_D) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_W) != GLFW_PRESS) {
                    GLCamera tmp = new GLCamera(camera);
                    tmp.backward(0.04);
                    if (isOutside(tmp) == 0)
                        camera.backward(0.04);
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                }
                //A
                if (key == GLFW_KEY_A && glfwGetKey(window, GLFW_KEY_D) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_S) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_W) != GLFW_PRESS) {
                    GLCamera tmp = new GLCamera(camera);
                    tmp.left(0.04);
                    if (isOutside(tmp) == 0)
                        camera.left(0.04);
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                }
                //D
                if (key == GLFW_KEY_D && glfwGetKey(window, GLFW_KEY_W) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_A) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_S) != GLFW_PRESS) {
                    System.out.println("doprava");
                    GLCamera tmp = new GLCamera(camera);
                    tmp.right(0.04);
                    if (isOutside(tmp) == 0)
                        camera.right(0.04);
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                }
                //W+D
                if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS &&
                        glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS && glfwGetKey(window, GLFW_KEY_A) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_S) != GLFW_PRESS){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.move( new Vec3D(
                            -Math.sin(camera.getAzimuth() - 3f/4*Math.PI ),
                            0.0f,
                            +Math.cos(camera.getAzimuth() - 3f/4*Math.PI ))
                            .mul(0.04));
                    if (isOutside(tmp) == 0)
                        camera.move( new Vec3D(
                                -Math.sin(camera.getAzimuth() - 3f/4*Math.PI ),
                                0.0f,
                                +Math.cos(camera.getAzimuth() - 3f/4*Math.PI ))
                                .mul(0.04));
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                }
                //W+A
                if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS &&
                        glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS && glfwGetKey(window, GLFW_KEY_D) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_S) != GLFW_PRESS){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.move( new Vec3D(
                            -Math.sin(camera.getAzimuth() - Math.PI/4 ),
                            0.0f,
                            +Math.cos(camera.getAzimuth() - Math.PI/4 ))
                            .mul(-0.04));
                    if (isOutside(tmp) == 0)
                        camera.move( new Vec3D(
                                -Math.sin(camera.getAzimuth() - Math.PI/4 ),
                                0.0f,
                                +Math.cos(camera.getAzimuth() - Math.PI/4 ))
                                .mul(-0.04));
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                }
                //S+A
                if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS &&
                        glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS && glfwGetKey(window, GLFW_KEY_W) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_D) != GLFW_PRESS){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.move( new Vec3D(
                            -Math.sin(camera.getAzimuth() - 3f/4*Math.PI ),
                            0.0f,
                            +Math.cos(camera.getAzimuth() - 3f/4*Math.PI ))
                            .mul(-0.04));
                    if (isOutside(tmp) == 0)
                        camera.move( new Vec3D(
                                -Math.sin(camera.getAzimuth() - 3f/4*Math.PI ),
                                0.0f,
                                +Math.cos(camera.getAzimuth() - 3f/4*Math.PI ))
                                .mul(-0.04));
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                }
                //S+D
                if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS &&
                        glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS && glfwGetKey(window, GLFW_KEY_W) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_A) != GLFW_PRESS){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.move( new Vec3D(
                            -Math.sin(camera.getAzimuth() - Math.PI/4 ),
                            0.0f,
                            +Math.cos(camera.getAzimuth() - Math.PI/4 ))
                            .mul(0.04));
                    if (isOutside(tmp) == 0)
                        camera.move( new Vec3D(
                                -Math.sin(camera.getAzimuth() - Math.PI/4 ),
                                0.0f,
                                +Math.cos(camera.getAzimuth() - Math.PI/4 ))
                                .mul(0.04));
                    if (isOutside(tmp) == 2){
                        pauseGame = true;
                        inFinish= true;
                        System.out.println("Gratuluji jsi v cíli");
                    }
                }
                if (key == GLFW_KEY_E && action == GLFW_PRESS) {
                    animateStart = !animateStart;
                }

                if (key == GLFW_KEY_H && action == GLFW_PRESS) {
                    showHelp = !showHelp;
                    if (!showHelp){
                        Arrays.fill(modelMatrixEnemy, 1);
                        firstTimeRenderEnemy = true;
                        animaceRun = false;

                        for (int i = 0; i < pocetKrychli; i++) {
                            for (int j = 0; j < pocetKrychli; j++) {
//                            if(rozlozeniBludiste[i][j] ==4){
//                                rozlozeniBludiste[i][j] = 4;
//                            }else {
//                                if(rozlozeniBludisteBackUp[i][j] !=4)
                                rozlozeniBludiste[i][j] = rozlozeniBludisteBackUp[i][j];
                            }
                        }
                    }
                }
//                System.out.println("jsem tu");
//                System.out.println(rozlozeniBludiste);
//                System.out.println(Arrays.deepToString(rozlozeniBludisteBackUp));
//                System.out.println(currenI+"  "+currenJ);
//                System.out.println(enemyI+"  "+enemyJ);
                //zapiani a vypinani pomoci
                //nahrani bludiscte pok akzdem kliku
                if(showHelp){
                    enemyJ = -1;
                    enemyI = -1;
                    int[][] tmpBludiste = findWay.shortestPath(rozlozeniBludisteNoEnemy,new int[]{currenI,currenJ},new int[]{9,5});
                    //prida enmyho do pole
//                    tmpBludiste[enemyI][enemyJ] = 4;
                    for (int i = 0; i < pocetKrychli; i++) {
                        for (int j = 0; j < pocetKrychli; j++) {
//                            if(rozlozeniBludisteBackUp[i][j]!=4)
                                rozlozeniBludiste[i][j] = tmpBludiste[i][j];
                        }
                    }
//                }else{
//                    for (int i = 0; i < pocetKrychli; i++) {
//                        for (int j = 0; j < pocetKrychli; j++) {
////                            if(rozlozeniBludiste[i][j] ==4){
////                                rozlozeniBludiste[i][j] = 4;
////                            }else {
////                                if(rozlozeniBludisteBackUp[i][j] !=4)
//                                    rozlozeniBludiste[i][j] = rozlozeniBludisteBackUp[i][j];
//                            }
//                        }
//                    }
                }

            }
        };
    }

    //Inicializace bludiste
    @Override
    public void init() {
//        super.init();
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glFrontFace(GL_CCW);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_FILL);
        glDisable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        textureCube = new OGLTexture2D[6];
        try {
            texture1 = new OGLTexture2D("textures/floor.png");
            texture2 = new OGLTexture2D("textures/wall.jpg");
            textureFinish = new OGLTexture2D("textures/finish.jpg");
            textureStart = new OGLTexture2D("textures/start.jpg");
            textureHelp = new OGLTexture2D("textures/help.png");
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
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        camera = new GLCamera();
        skyBox();
        createMaze();
        camera.setPosition(new Vec3D(spawnX * 0.04, 5 * 0.04, spawnZ * 0.04));

        //vytvorim jednotkovou matici
        Arrays.fill(modelMatrixEnemy, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScalef(0.04f, 0.04f, 0.04f);
        glGetFloatv(GL_MODELVIEW_MATRIX,modelMatrixEnemy);

        //objekt
        obj = new OBJreader();


        currenI = spawnI;
        currenJ = spawnJ;

        pauseGame = true;

        textRenderer = new OGLTextRenderer(width, height);
//        textureViewer = new OGLTexture2D.Viewer();

        countOfDeads = 0;
    }


    //Funkce pro neustale vykreslovani
    @Override
    public void display() {



        if(pauseGame){
            texturePause.bind();
            if(isPlayerDead)
                textureIsDead.bind();
            if(inFinish)
                texturePauseFinish.bind();
//            System.out.println(height);
            glViewport(0, 0, width, height);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
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

            textRenderer.resize(width,height);
            textRenderer.clear();
            textRenderer.addStr2D(2, 17, "Počet smrtí: "+countOfDeads);
            textRenderer.draw();
            return;
        }

        // vypocet fps, nastaveni rychlosti otaceni podle rychlosti prekresleni
        long mils = System.currentTimeMillis();
        System.out.println(mils);
        if ((mils - oldFPSmils) > 300) {
            fps = 1000 / (double) (mils - oldmils + 1);
            oldFPSmils = mils;
        }
        String textInfo = String.format(Locale.US, "FPS %3.1f", fps);

//        System.out.println(fps);
        float speed = 20; // pocet stupnu rotace za vterinu
//        System.out.println(step);
        step = speed * (mils - oldmils) / 1000.0f; // krok za jedno
        oldmils = mils;

        if(savedTeleportPosition && (mils - milsSave)>1000)
            savedTeleportPosition=false;
        if(loadedTeleportPosition &&(mils - millsTeleport)>1000)
            loadedTeleportPosition = false;


        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(0f, 0f, 0f, 1f);

        //Modelovaci
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScalef(0.04f, 0.04f, 0.04f);
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


//        rozlozeniBludiste = findWay.shortestPath(rozlozeniBludiste,new int[]{1,4},new int[]{9,5});
//        textureViewer.view(texture1, 0, 0);

        renderMaze();
        renderObj();
        if(currenI == enemyI && currenJ == enemyJ){
//            inFinish = true;
            isPlayerDead = true;
            pauseGame = true;
            System.out.println("jsi mrtvy");
        }

        textRenderer.resize(width,height);
        textRenderer.clear();
        textRenderer.addStr2D(2, 17, textInfo);
        textRenderer.addStr2D(2, 38, "Počet smrtí: "+countOfDeads);
        if(savedTeleportPosition)
            textRenderer.addStr2D(2, height - 3,"Pozice pro teleport nastavena.");
        if(loadedTeleportPosition)
            textRenderer.addStr2D(2, height - 3, "Byl jsi teleportován.");
        textRenderer.addStr2D(width -315, height - 3, "Semestrální projekt – Dominik Kohl(c) PGRF2 UHK 2021");
        textRenderer.draw();

    }

    private void renderEnemy(int x,int y) {
//        enemyI = x;
//        enemyJ =y;
        if (!animateStart) return;
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        //zajisteni naharani matice pro npc
        if(newMove){
            glLoadIdentity();
            glScalef(0.04f, 0.04f, 0.04f);
            glTranslatef(jednaHrana/2f+x*jednaHrana,0f,jednaHrana/2f+y*jednaHrana);
            glGetFloatv(GL_MODELVIEW_MATRIX,modelMatrixEnemy);
            newMove = false;
        }
        glLoadMatrixf(modelMatrixEnemy);

//            glTranslatef(0,0,step);
        switch (destiantion[2]) {
            case 1 -> glTranslatef(0,0,step);
            case 2 -> glTranslatef(0,0,-step);
            case 3 ->  glTranslatef(step,0,0);
            case 4 -> glTranslatef(-step,0,0);
            default ->  glTranslatef(0,0,0);
        }

        float zmenseni = jednaHrana/3f;
//        zmenseni = 0f;
        textureKing.bind();

        glBegin(GL_TRIANGLES);

        for (int[] indice: obj.getIndices() ) {
            glTexCoord2f(obj.getTextury().get(indice[1]-1)[0], obj.getTextury().get(indice[1]-1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[0]-1)[0],obj.getVrcholy().get(indice[0]-1)[1],obj.getVrcholy().get(indice[0]-1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[3]-1)[0], obj.getTextury().get(indice[3]-1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[2]-1)[0],obj.getVrcholy().get(indice[2]-1)[1],obj.getVrcholy().get(indice[2]-1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[5]-1)[0], obj.getTextury().get(indice[5]-1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[4]-1)[0],obj.getVrcholy().get(indice[4]-1)[1],obj.getVrcholy().get(indice[4]-1)[2]);
        }

        glEnd();
        glGetFloatv(GL_MODELVIEW_MATRIX,modelMatrixEnemy);
        glPopMatrix();

        //precahzim hranu nastavuji jiny papametry site
        if(startBod>=jednaHrana/2f ){
            prechodhrana = true;
            rozlozeniBludiste[source[0]][source[1]] = 0;
            rozlozeniBludiste[destiantion[0]][destiantion[1]] = 4;
        }
//        System.out.println(startBod);
        if(startBod<finishBod)
            startBod = startBod+step;
//        System.out.println(startBod);
        if(startBod >= finishBod){
            System.out.println(startBod);
            animaceRun = false;
//            animaceStop = true;
            prechodhrana = false;
//            source = destiantion;
//            destiantion = possibleWaysEnemy(x,y);
//            animateStart = false;
        }
    }

    //Funkce pro kolize
    // 0-jsem v bludisti, 1 - jsem blizko zdi, 2 - jsem v cili
    private int isOutside(GLCamera cam) {
        double camX = cam.getPosition().getX();
        double camY = cam.getPosition().getY();
        double camZ = cam.getPosition().getZ();
        //TODO optimalizovat do funkci if statmenty
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 1) {
                    if (boxes[i][j].getxMin() * 0.04 * 0.98 <= camX && camX <= boxes[i][j].getxMax() * 0.04 * 1.02 &&
                            boxes[i][j].getyMin() * 0.04 * 0.98 <= camY && camY <= boxes[i][j].getyMax() * 0.04 * 1.02 &&
                            boxes[i][j].getzMin() * 0.04 * 0.98 <= camZ && camZ <= boxes[i][j].getzMax() * 0.04 * 1.02)
                        return 1;
                }
                if (rozlozeniBludiste[i][j] == 3) {
                    if (boxes[i][j].getxMin() * 0.04 * 0.98 <= camX && camX <= boxes[i][j].getxMax() * 0.04 * 1.02 &&
                            boxes[i][j].getyMin() * 0.04 * 0.98 <= camY && camY <= boxes[i][j].getyMax() * 0.04 * 1.02 &&
                            boxes[i][j].getzMin() * 0.04 * 0.98 <= camZ && camZ <= boxes[i][j].getzMax() * 0.04 * 1.02)
                        return 2;
                }
                if (rozlozeniBludiste[i][j] == 0 || rozlozeniBludiste[i][j] == 5 ||rozlozeniBludiste[i][j] == 2 ||rozlozeniBludiste[i][j] == 4 ) {
                    if (boxes[i][j].getxMin() * 0.04  <= camX && camX <= boxes[i][j].getxMax() * 0.04  &&
                            boxes[i][j].getyMin() * 0.04 <= camY && camY <= boxes[i][j].getyMax() * 0.04  &&
                            boxes[i][j].getzMin() * 0.04  <= camZ && camZ <= boxes[i][j].getzMax() * 0.04 ){
                        currenI = i;
                        currenJ = j;
                    }
                }
            }
        }
        for (Box box : spawnHelpBoxes) {
            if (box.getxMin() * 0.04 * 0.98 <= camX && camX <= box.getxMax() * 0.04 * 1.02 &&
                    box.getyMin() * 0.04 * 0.98 <= camY && camY <= box.getyMax() * 0.04 * 1.02 &&
                    box.getzMin() * 0.04 * 0.98 <= camZ && camZ <= box.getzMax() * 0.04 * 1.02)
                return 1;

        }
        return 0;
    }

    //Vykresleni bludiste
    private void renderMaze() {
//        rozlozeniBludiste[3][7] = 3;
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 0) {
                    renderPlate(i, j);
                } else if (rozlozeniBludiste[i][j] == 3) {
                    renderFinish(i, j);
                } else if (rozlozeniBludiste[i][j] == 2) {
                    renderStart(i, j);
                } else if (rozlozeniBludiste[i][j] == 4) {
//                    System.out.println("Animace RUN"+animaceRun);
//                    System.out.println(""animaceStop);
                    enemyI = i;
                    enemyJ =j;
                    if(firstTimeRenderEnemy){
                        allVisitedEnemy.add(new int[]{i,j,rozlozeniBludiste[i][j]});
                        firstTimeRenderEnemy = false;
                    }
                    if(!animaceRun){
                        destiantion = possibleWaysEnemy(i,j);
                        startBod = 0f;
                        finishBod = jednaHrana;
                        animaceRun = true;
                        newMove =true;
                    }
                    if(prechodhrana){
                        //ta predchozi, rpoze jeste nedoberhla animace ale blobk uz je prehozeni
                        renderEnemy(source[0],source[1]);
                    } else{
                        renderEnemy(i, j);
                    }
                }else if (rozlozeniBludiste[i][j] == 5) {
                    renderPlateHelp(i, j);
                } else {
                    renderBox(i, j);
                }
            }
        }
        for (Box box : spawnHelpBoxes) {
            renderBox(box);
        }

    }

    private int[] possibleWaysEnemy(int i, int j) {
        source = new int[]{i, j};
        ArrayList<int[]> possbileWays = new ArrayList<>();
        // 1 do prava,2 do levam, 3 nahoru,4 dolu
        if(j+1 < delkaHrany && j+1>=0 && isNotInsideEnemyWay(i,j+1)){
            if((rozlozeniBludiste[i][j+1] == 0 || rozlozeniBludiste[i][j+1] == 5)){
                int[] tmp = {i,j+1,1,rozlozeniBludiste[i][j+1]};
                possbileWays.add(tmp);
            }
        }

        if(j-1 < delkaHrany && j-1>=0 && isNotInsideEnemyWay(i,j-1)){
            if(rozlozeniBludiste[i][j-1] == 0 || rozlozeniBludiste[i][j-1] == 5){
                int[] tmp = {i,j-1,2,rozlozeniBludiste[i][j-1]};
                possbileWays.add(tmp);
            }
        }
        if(i+1 < delkaHrany && i+1>=0 && isNotInsideEnemyWay(i+1,j)){
            if(rozlozeniBludiste[i+1][j] == 0 || rozlozeniBludiste[i+1][j] == 5){
                int[] tmp = {i+1,j,3,rozlozeniBludiste[i+1][j]};
                possbileWays.add(tmp);
            }
        }
        if(i-1 < delkaHrany && i-1>=0 && isNotInsideEnemyWay(i-1,j)){
            if(rozlozeniBludiste[i-1][j] == 0 || rozlozeniBludiste[i-1][j] == 5){
                int[] tmp = {i-1,j,4,rozlozeniBludiste[i-1][j]};
                possbileWays.add(tmp);
            }
        }

        if(possbileWays.size() == 0 && allVisitedEnemy.size() != 0){
//            possbileWays.add(allVisitedEnemy.get(allVisitedEnemy.size()-1));
//            int tmpPosun = 0;
//            int[] tmp = allVisitedEnemy.get(allVisitedEnemy.size()-1);
//                if(tmp[2] == 1)
//                    tmpPosun = 2;
//                if(tmp[2] == 2)
//                    tmpPosun = 1;
//                if(tmp[2] == 3)
//                    tmpPosun = 4;
//                if(tmp[2] == 4)
//                    tmpPosun = 3;
//
//            possbileWays.add(new int[]{tmp[0],tmp[1],tmpPosun});
//            allVisitedEnemy.clear();
//            source = new int[]{tmp[0], tmp[1]};
            allVisitedEnemy.clear();
            allVisitedEnemy.add(new int[]{i,j,0,rozlozeniBludiste[i][j]});
            //TODO optimazilovat dat if do funcki a vratit list
            if(j+1 < delkaHrany && j+1>=0 && isNotInsideEnemyWay(i,j+1)){
                if(rozlozeniBludiste[i][j+1] == 0 || rozlozeniBludiste[i][j+1] == 5){
                    int[] tmp = {i,j+1,1,rozlozeniBludiste[i][j+1]};
                    possbileWays.add(tmp);
                }
            }

            if(j-1 < delkaHrany && j-1>=0 && isNotInsideEnemyWay(i,j-1)){
                if(rozlozeniBludiste[i][j-1] == 0 || rozlozeniBludiste[i][j-1] == 5){
                    int[] tmp = {i,j-1,2,rozlozeniBludiste[i][j]};
                    possbileWays.add(tmp);
                }
            }
            if(i+1 < delkaHrany && i+1>=0 && isNotInsideEnemyWay(i+1,j)){
                if(rozlozeniBludiste[i+1][j] == 0 || rozlozeniBludiste[i+1][j] == 5){
                    int[] tmp = {i+1,j,3,rozlozeniBludiste[i+1][j]};
                    possbileWays.add(tmp);
                }
            }
            if(i-1 < delkaHrany && i-1>=0 && isNotInsideEnemyWay(i-1,j)){
                if(rozlozeniBludiste[i-1][j] == 0 || rozlozeniBludiste[i-1][j] == 5){
                    int[] tmp = {i-1,j,4,rozlozeniBludiste[i-1][j]};
                    possbileWays.add(tmp);
                }
            }

        }

        if(possbileWays.size() == 0)
            possbileWays.add(new int[]{i,j,0,rozlozeniBludiste[i][j]});

//        System.out.println(possbileWays.toString());
        int randomWay = (int)(Math.random() * possbileWays.size());
//        System.out.println(Arrays.toString(possbileWays.get(randomWay)));

        System.out.println(allVisitedEnemy.toString());
        allVisitedEnemy.add(possbileWays.get(randomWay));
        return possbileWays.get(randomWay);

    }

    private boolean isNotInsideEnemyWay(int i, int j) {
        if(allVisitedEnemy.size() <=0){
            return true;
        }else{
            for (int[] blok: allVisitedEnemy) {
                if(blok[0] == i && blok[1] == j)
                    return false;
            }
        }
        return true;
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

    //Funkce vytvoreni bludiste
    private void createMaze() {

        parseTxt("src/res/proportions/maze");
        rozlozeniBludisteBackUp = new int[pocetKrychli][pocetKrychli];
        rozlozeniBludisteNoEnemy = new int[pocetKrychli][pocetKrychli];
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if(rozlozeniBludiste[i][j] ==4)
                    rozlozeniBludisteNoEnemy[i][j] = 0;
                    else
                rozlozeniBludisteNoEnemy[i][j] = rozlozeniBludiste[i][j];

                rozlozeniBludisteBackUp[i][j] = rozlozeniBludiste[i][j];
                if (rozlozeniBludiste[i][j] == 2) {
                    spawnI = i;
                    spawnJ = j;
                    spawnX = (boxes[i][j].getbH().getX() +
                            boxes[i][j].getB2().getX() +
                            boxes[i][j].getB3().getX() +
                            boxes[i][j].getB4().getX() +
                            boxes[i][j].getbUp4().getX() +
                            boxes[i][j].getbUp3().getX() +
                            boxes[i][j].getbUp2().getX() +
                            boxes[i][j].getbUp1().getX()
                    ) / 8;

                    spawnZ = (boxes[i][j].getbH().getZ() +
                            boxes[i][j].getB2().getZ() +
                            boxes[i][j].getB3().getZ() +
                            boxes[i][j].getB4().getZ() +
                            boxes[i][j].getbUp4().getZ() +
                            boxes[i][j].getbUp3().getZ() +
                            boxes[i][j].getbUp2().getZ() +
                            boxes[i][j].getbUp1().getZ()
                    ) / 8;
                }

                if (rozlozeniBludiste[i][j] == 0) {
                    addBoxIfPossible(i, j + 1);
                    addBoxIfPossible(i + 1, j);
                    addBoxIfPossible(i - 1, j);
                    addBoxIfPossible(i, j - 1);
                }
            }
        }
        addBoxIfPossible(spawnI, spawnJ + 1);
        addBoxIfPossible(spawnI + 1, spawnJ);
        addBoxIfPossible(spawnI - 1, spawnJ);
        addBoxIfPossible(spawnI, spawnJ - 1);


//        rozlozeniBludisteBackUp = rozlozeniBludiste;
    }

    //Funkce zjistujici zda musime vykresli box, aby hrac nemohl ven z mapy
    private void addBoxIfPossible(int i, int j) {
        try {
            double tmp = boxes[i][j].getxMax();
        } catch (ArrayIndexOutOfBoundsException e) {
            //mimo-vytvorim novy box
            Box tmp = new Box(i, j, jednaHrana);
            spawnHelpBoxes.add(tmp);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    //Vykresleni skyboxu
    private void skyBox() {

        glPushMatrix();
        glColor3d(0.5, 0.5, 0.5);
        int size = 6 * delkaHrany;

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

    //Pomocna funkce pro cteni ze souboru
    public String readFromFile(String filename, String extension) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, extension))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Nacteni bludiste ze souboru
    public void parseTxt(String filename) {
        String data = readFromFile(filename, "txt");
        String[] lines = data.split("\n");
        String[] velikostString = lines[0].split("!");
        String[] velikostString2 = lines[1].split("!");
        pocetKrychli = Integer.parseInt(velikostString[1]);
        delkaHrany = Integer.parseInt(velikostString2[1]);
        rozlozeniBludiste = new int[pocetKrychli][pocetKrychli];
        boxes = new Box[pocetKrychli][pocetKrychli];
        jednaHrana = delkaHrany / pocetKrychli;

        for (int i = 0; i < pocetKrychli; i++) {
            // rozdeleni radku na jednotlive segmenty
            String[] attributes = lines[i + 2].split(" ! ");
            for (int j = 0; j < pocetKrychli; j++) {
                switch (attributes[j]) {
                    case "c" -> rozlozeniBludiste[i][j] = 0;
                    case "S" -> rozlozeniBludiste[i][j] = 2;
                    case "K" -> rozlozeniBludiste[i][j] = 3;
                    case "E" -> rozlozeniBludiste[i][j] = 4;
                    default -> rozlozeniBludiste[i][j] = 1;
                }
                boxes[i][j] = new Box(i, j, jednaHrana);
            }
        }
    }

    public void renderObj(){

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(0.04f, 0.04f, 0.04f);
//        glRotatef(270,1,0,0);
//        glTranslatef(8.4f,1.1f,0);
        glTranslatef(10f,0f,10f);
        glEnable(GL_TEXTURE_2D);
//        glEnable(GL_LIGHTING);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        textureKing.bind();
        glBegin(GL_TRIANGLES);
//        glBegin(GL_QUAD_STRIP);
        glColor3f(1f, 1f, 1f);
//        glScalef(0.04f, 0.04f, 0.04f);


        for (int[] indice: obj.getIndices() ) {
            glTexCoord2f(obj.getTextury().get(indice[1]-1)[0], obj.getTextury().get(indice[1]-1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[0]-1)[0],obj.getVrcholy().get(indice[0]-1)[1],obj.getVrcholy().get(indice[0]-1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[3]-1)[0], obj.getTextury().get(indice[3]-1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[2]-1)[0],obj.getVrcholy().get(indice[2]-1)[1],obj.getVrcholy().get(indice[2]-1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[5]-1)[0], obj.getTextury().get(indice[5]-1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[4]-1)[0],obj.getVrcholy().get(indice[4]-1)[1],obj.getVrcholy().get(indice[4]-1)[2]);
        }
        glEnd();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glPopMatrix();
    }


}

