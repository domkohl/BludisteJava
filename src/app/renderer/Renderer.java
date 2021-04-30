package app.renderer;

import app.maze.Box;
import app.maze.FindWayBFS;
import app.maze.MazeLoader;
import app.npc.Enemy;
import app.npc.ObjReader;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
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
import static utils.GlutUtils.glutWireCube;


/**
 * Třída pro Renderování bludište a práce s ním
 */
public class Renderer extends AbstractRenderer {
    //Ovládání
    private boolean showCursor;
    private GLCamera camera;
    private GLCamera cameraTeleport;
    private float azimutTeport, zenitTeleport;
    private float dx, dy, ox, oy;
    private float azimut, zenit;
    private boolean animaceRun;
    private boolean showHelp,pauseGame,inFinish,isPlayerDead,savedTeleportPosition,loadedTeleportPosition,loadedTeleportFailed,wayToFinishExist;
    //Textury
    private OGLTexture2D[] textureCube;
    private OGLTexture2D textureFloor, textureWall, textureFinish, textureStart,textureHelp,textureKing,texturePause,texturePauseFinish,textureIsDead;
    //NPC+čas
    private int countOfDeads;
    private long oldmils;
    private long oldFPSmils;
    private double fps;
    private float step;
    private float[] modelMatrixEnemy;
    private boolean prechodHrana;
    private float startBod,finishBod;
    private boolean newMove;
    private boolean firstTimeRenderEnemy;
    private long milsSave,milsTeleport,milsTeleportFailed;
    //Klávesnice
    private boolean isPressedW,isPressedA,isPressedS,isPressedD;
    //Objekty pro bludiště
    private MazeLoader maze;
    private FindWayBFS findWay;
    private ObjReader obj;
    private Enemy enemy;

    public Renderer() {
        super();
        //Základní ovládaní prostředí – zmenšovaní/zvětšovaní okna
        glfwWindowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        };

        //Rozhlížení
        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                //Když hra pozastavena nerozhlížím se
                if (pauseGame) return;
                //Rozhlížení kamery
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

        //Pohyb hráče po bludišti + ovládání hry
        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                //Hráč chce resetovat hru, když je pozastavena vše resetuji a počítadlo v závislosti kdy byla hra pozastavena nastavím
                if (key == GLFW_KEY_R && action == GLFW_PRESS && pauseGame) {
                    //Reset pohybu
                    isPressedW = false;
                    isPressedA = false;
                    isPressedS= false;
                    isPressedD = false;
                    //Nastavení počítadla
                    if (isPlayerDead)
                        countOfDeads++;
                    if (inFinish)
                        countOfDeads = 0;
                    //Reset pohybu npc
                    Arrays.fill(modelMatrixEnemy, 1);
                    firstTimeRenderEnemy = true;
                    animaceRun = false;
                    //Základní rozložení bludiště
                    for (int i = 0; i < maze.getPocetKrychli(); i++) {
                        for (int j = 0; j < maze.getPocetKrychli(); j++) {
                            maze.setRozlozeniBludiste(i,j,maze.getRozlozeniBludisteBackUp(i,j));
                        }
                    }
                    //Reset kamery
                    camera.setAzimuth(0);
                    camera.setZenith(0);
                    azimut = 0;
                    zenit = 0;
                    camera.setPosition(new Vec3D(maze.getSpawnX() * maze.getZmenseni(), maze.getJednaHrana()/4f* maze.getZmenseni(), maze.getSpawnZ() * maze.getZmenseni()));
                    //Reset proměnných
                    showHelp = false;
                    pauseGame = false;
                    isPlayerDead = false;
                    inFinish = false;
                    showCursor = false;
                    //Nastavení současné polohy
                    maze.setCurrenI(maze.getSpawnI());
                    maze.setCurrenJ(maze.getSpawnJ());
                    //Skrytí kurzoru
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
                // Pozastavení hry + zobrazení kurzoru
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
                        milsTeleport = System.currentTimeMillis();
                        loadedTeleportPosition = true;
                        azimut = azimutTeport;
                        zenit = zenitTeleport;
                        camera = new GLCamera(cameraTeleport);
                    } else {
                        milsTeleportFailed = System.currentTimeMillis();
                        loadedTeleportFailed = true;
                    }
                }

                // Pohyb hráče
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
                            //TODo napsat kdyz null tka neexistuje cesta z bludiste
                            //null -> neexistuje cesta z bludiště
                            if(tmpBludiste != null){
                                for (int i = 0; i < maze.getPocetKrychli(); i++) {
                                    for (int j = 0; j < maze.getPocetKrychli(); j++) {
                                        maze.setRozlozeniBludiste(i,j,tmpBludiste[i][j]);
                                    }
                                }
                            }else{
                                for (int i = 0; i < maze.getPocetKrychli(); i++) {
                                    for (int j = 0; j < maze.getPocetKrychli(); j++) {
                                        maze.setRozlozeniBludiste(i,j,maze.getRozlozeniBludisteNoEnemy()[i][j]);
                                    }
                                }
                                wayToFinishExist = false;
                            }
                    }
                }
            }
        };
        //Základní chování (odstranění výpisů)
        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                //do nothing
            }
        };
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {//do nothing
            }
        };

    }

    //Inicializace bludiště
    @Override
    public void init() {
        //Základní nastavení
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        glEnable(GL_DEPTH_TEST);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_POINT);
        glFrontFace(GL_CCW);

        //Načtení potřebných textur
        textureCube = new OGLTexture2D[6];
        try {
            textureFloor = new OGLTexture2D("textures/floor.jpg");
            textureWall = new OGLTexture2D("textures/wall.png");
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

        //Prvotní nastavení proměnných a objektu:
        maze = new MazeLoader();
        camera = new GLCamera();
        camera.setPosition(new Vec3D(maze.getSpawnX() * maze.getZmenseni(), maze.getJednaHrana()/4f* maze.getZmenseni(), maze.getSpawnZ() * maze.getZmenseni()));
        findWay = new FindWayBFS();
        //Vytvoření jednotkové matice pro pohyb NPC
        modelMatrixEnemy = new float[16];
        Arrays.fill(modelMatrixEnemy, 1);
        obj = new ObjReader();
        pauseGame = true;
        textRenderer = new OGLTextRenderer(width, height);
        countOfDeads = 0;
        enemy = new Enemy(maze.getPocetKrychli());
        wayToFinishExist = true;
        showCursor = true;
        firstTimeRenderEnemy = true;
    }


    //Funkce pro neustále vykreslováni
    @Override
    public void display() {
        //Kontroluji stavy hry a podle toho se chovám
        if (pauseGame || inFinish || isPlayerDead) {
            //Vykreslení menu podle stavu hry
            texturePause.bind();
            if (isPlayerDead)
                textureIsDead.bind();
            if (inFinish)
                texturePauseFinish.bind();

            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();

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
            if (maze.isMazeLoadError())
                textRenderer.addStr2D(width - 800, height -3, "!! Nepodařolo se náhrat ze souboru !! -> bylo nahráno základní bludiště");
            textRenderer.addStr2D(2, 17, "Počet smrtí: " + countOfDeads);
            textRenderer.draw();
            return;
        }

        //Výpočet fps, nastaveni rychlosti pohybu NPC podle rychlosti překreslení
        long mils = System.currentTimeMillis();
        if ((mils - oldFPSmils) > 300) {
            fps = 1000 / (double) (mils - oldmils + 1);
            oldFPSmils = mils;
        }
        String textInfo = String.format(Locale.US, "FPS %3.1f", fps);
        float speed = maze.getJednaHrana(); // počet jednotek za vteřinu
        step = speed * (mils - oldmils) / 1000.0f; // krok za jedno
        oldmils = mils;

        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(0f, 0f, 0f, 1f);

        //Modelovací
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScalef(maze.getZmenseni(), maze.getZmenseni(), maze.getZmenseni());
        //Projekční
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, width / (float) height, 0.01f, 100.0f);

        //Kamera
        camera.setFirstPerson(true);
        Vec3D cameraFixedY = camera.getPosition();
        // TODo otesotvat pro jine rozmery bludiste a zmenil jsem v boxech zpuisob vytvareni vysky boxu
//        camera.setPosition(cameraFixedY.withY(0.20));
        camera.setPosition(cameraFixedY.withY(maze.getJednaHrana()/4f* maze.getZmenseni()));
        camera.setMatrix();

        skyBox();
        renderMaze();

        //Zjištěni, zda hráče zasáhlo npc
        if (maze.getCurrenI() == enemy.getEnemyPosI() && maze.getCurrenJ() == enemy.getEnemyPosJ()) {
            isPlayerDead = true;
            pauseGame = true;
        }

        //Ovládaní klávesnice zde kvůli lepší simulaci ovládání jako ve hře:
        //W
        if (isPressedW && !isPressedD && !isPressedA && !isPressedS)
            tryMoveSingleClick(1);
        //S
        if (isPressedS && !isPressedD && !isPressedA && !isPressedW)
            tryMoveSingleClick(2);
        //A
        if (isPressedA && !isPressedD && !isPressedW && !isPressedS)
            tryMoveSingleClick(3);
        //D
        if (isPressedD && !isPressedW && !isPressedA && !isPressedS)
            tryMoveSingleClick(4);
        //W+D
        if (isPressedW && isPressedD && !isPressedA && !isPressedS) {
            tryMoveDoubleClick(0.03,1);
        }
        //W+A
        if (isPressedW && isPressedA && !isPressedD && !isPressedS)
            tryMoveDoubleClick(-0.03,2);
        //S+D
        if (isPressedS && isPressedD && !isPressedW && !isPressedA)
            tryMoveDoubleClick(0.03,2);
        //S+A
        if (isPressedS && isPressedA && !isPressedW && !isPressedD)
            tryMoveDoubleClick(-0.03,1);

        //Zapínaní a vypínaní pomoci
        //Nahraní bludiště po každém kliku/držení klávesy
        if (showHelp && (isPressedS||isPressedA||isPressedD||isPressedW)) {
            int[][] tmpBludiste = findWay.shortestPath(maze.getRozlozeniBludisteNoEnemy(), new int[]{maze.getCurrenI(), maze.getCurrenJ()}, new int[]{maze.getFinishI(), maze.getFinishJ()});
            if(tmpBludiste != null) {
                for (int i = 0; i < maze.getPocetKrychli(); i++) {
                    for (int j = 0; j < maze.getPocetKrychli(); j++) {
                        maze.setRozlozeniBludiste(i, j, tmpBludiste[i][j]);
                    }
                }
            }
        }

        //Zmizení textu po 1s pro oznámení informace o teleportu
        if (savedTeleportPosition && (mils - milsSave) > 1000)
            savedTeleportPosition = false;
        if (loadedTeleportPosition && (mils - milsTeleport) > 1000)
            loadedTeleportPosition = false;
        if (loadedTeleportFailed && (mils - milsTeleportFailed) > 1000)
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
        if (!wayToFinishExist)
            textRenderer.addStr2D(width - 800, height -3, "Z tohoto bludiště neexistuje cesta ven.");
        textRenderer.addStr2D(width - 315, height - 3, "Semestrální projekt – Dominik Kohl(c) PGRF2 UHK 2021");
        textRenderer.draw();

    }

    //Pomocná funkce pro pohyb v jednom směru
    private void tryMoveSingleClick(int i) {
        GLCamera tmp = new GLCamera(camera);
        switch (i) {
            case 1 -> tmp.forward(0.03);
            case 2 -> tmp.backward(0.03);
            case 3 -> tmp.left(0.03);
            case 4 -> tmp.right(0.03);
            default -> tmp.getPosition();
        }
        if (maze.isOutside(tmp) == 0){
            switch (i) {
                case 1 -> camera.forward(0.03);
                case 2 -> camera.backward(0.03);
                case 3 -> camera.left(0.03);
                case 4 -> camera.right(0.03);
                default -> camera.getPosition();
            }
        }
        if (maze.isOutside(tmp) == 2) {
            pauseGame = true;
            inFinish = true;
        }
    }
    //Pomocná funkce pro pohyb do stran
    private void tryMoveDoubleClick(double v, int i) {
        GLCamera tmp = new GLCamera(camera);
        switch (i) {
            case 1 -> tmp.move(new Vec3D(-Math.sin(camera.getAzimuth() - 3f / 4 * Math.PI), 0.0f, +Math.cos(camera.getAzimuth() - 3f / 4 * Math.PI)).mul(v));
            case 2 -> tmp.move(new Vec3D(-Math.sin(camera.getAzimuth() - Math.PI / 4), 0.0f, +Math.cos(camera.getAzimuth() - Math.PI / 4)).mul(v));
            default -> tmp.getPosition();
        }
        if (maze.isOutside(tmp) == 0){
            switch (i) {
                case 1 -> camera.move(new Vec3D(-Math.sin(camera.getAzimuth() - 3f / 4 * Math.PI), 0.0f, +Math.cos(camera.getAzimuth() - 3f / 4 * Math.PI)).mul(v));
                case 2 -> camera.move(new Vec3D(-Math.sin(camera.getAzimuth() - Math.PI / 4), 0.0f, +Math.cos(camera.getAzimuth() - Math.PI / 4)).mul(v));
                default -> camera.getPosition();
            }
        }
        if (maze.isOutside(tmp) == 2) {
            pauseGame = true;
            inFinish = true;
        }
    }

    // Funkce pro vykreslení NPC - funkce se stará o to aby se npc(obj) pohybovalo ve spravném směru
    private void renderEnemy(int x, int y) {
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        //Zajištěni nahraní nové matice pro npc na středu bloku
        if (newMove) {
            glLoadIdentity();
            glScalef(maze.getZmenseni(), maze.getZmenseni(), maze.getZmenseni());
            glTranslatef(maze.getJednaHrana() / 2f + x * maze.getJednaHrana(), 0f, maze.getJednaHrana() / 2f + y * maze.getJednaHrana());
            //Uložení matice do proměnné
            glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrixEnemy);
            newMove = false;
        }
        //Nahrání modelovací matice pro NPC
        glLoadMatrixf(modelMatrixEnemy);

        //Ptám se jaký směrem má npc jít hodnota je uložena v poli
        switch (enemy.getCurrentDestinationBlock()[2]) {
            case 1 -> glTranslatef(0, 0, step);
            case 2 -> glTranslatef(0, 0, -step);
            case 3 -> glTranslatef(step, 0, 0);
            case 4 -> glTranslatef(-step, 0, 0);
            default -> glTranslatef(0, 0, 0);
        }

        //Vykreslení objektu
        textureKing.bind();
        glBegin(GL_TRIANGLES);
        for (int[] indice : obj.getIndices()) {
            glTexCoord2f(obj.getTextury().get(indice[1] - 1)[0], obj.getTextury().get(indice[1] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[0] - 1)[0], obj.getVrcholy().get(indice[0] - 1)[1], obj.getVrcholy().get(indice[0] - 1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[3] - 1)[0], obj.getTextury().get(indice[3] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[2] - 1)[0], obj.getVrcholy().get(indice[2] - 1)[1], obj.getVrcholy().get(indice[2] - 1)[2]);
            glTexCoord2f(obj.getTextury().get(indice[5] - 1)[0], obj.getTextury().get(indice[5] - 1)[1]);
            glVertex3f(obj.getVrcholy().get(indice[4] - 1)[0], obj.getVrcholy().get(indice[4] - 1)[1], obj.getVrcholy().get(indice[4] - 1)[2]);
        }
        glEnd();
        //Uložení matice pro další pohyb
        glGetFloatv(GL_MODELVIEW_MATRIX, modelMatrixEnemy);
        glPopMatrix();
        //NPC přechází hranu s jiným blokem upravím podle toho rozložení bludiště
        if (startBod >= maze.getJednaHrana() / 2f) {
            prechodHrana = true;
            //Starý blok nastavím na cestu a nový na enemy
            maze.setRozlozeniBludiste(enemy.getSource()[0],enemy.getSource()[1],0);
            maze.setRozlozeniBludiste(enemy.getCurrentDestinationBlock()[0],enemy.getCurrentDestinationBlock()[1],4);
        }
        //Posouvám, dokud není npc na středu dalšího bloku
        if (startBod < finishBod)
            startBod = startBod + step;
        //Vím ze animace skončila a mužů najit další možný blok pro pohyb
        if (startBod >= finishBod) {
            animaceRun = false;
            prechodHrana = false;
        }
    }

    //Vykresluji bludiště podle hodnot v matici
    private void renderMaze() {
        for (int i = 0; i < maze.getPocetKrychli(); i++) {
            for (int j = 0; j < maze.getPocetKrychli(); j++) {
                if (maze.getRozlozeniBludiste(i,j) == 0) {
                    renderPlate(i, j);
                } else if (maze.getRozlozeniBludiste(i,j) == 3) {
                    renderFinish(i, j);
                } else if (maze.getRozlozeniBludiste(i,j) == 2) {
                    renderStart(i, j);
                } else if (maze.getRozlozeniBludiste(i,j) == 4) {
                    //Uložení hodnot, kde se nachází enemy
                    enemy.setEnemyPosI(i);
                    enemy.setEnemyPosJ(j);
                    //Když renderuji enemy poprvé v každém novem pohybu mu nastavím první blok v poli navštívených původní blok,
                    // aby se nemohl vrátit ani tam kde začal
                    if (firstTimeRenderEnemy) {
                        enemy.getAllVisitedEnemy().add(new int[]{i, j, maze.getRozlozeniBludiste(i,j)});
                        firstTimeRenderEnemy = false;
                    }
                    //Nové zapnutí pohybu vyhledám blok a resetuji hodnoty pro pohyb a to i ve funkci která ho vykresluje
                    if (!animaceRun) {
                        enemy.possibleWaysEnemyGetDestination(i, j,maze.getRozlozeniBludiste());
                        startBod = 0f;
                        finishBod = maze.getJednaHrana();
                        animaceRun = true;
                        newMove = true;
                    }
                    if (prechodHrana) {
                        //Enemy přesel na jiný blok, ale animace ještě neskončila, proto vykresluji ještě objekt podle zdrojového bloku
                        renderEnemy(enemy.getSource()[0], enemy.getSource()[1]);
                    } else {
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
    //Vykreslení boxu/zdi matice
    private void renderBox(int x, int y) {
        textureWall.bind();
        glBegin(GL_QUADS);
        glColor3f(0f, 1f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp1().getX(), (float) maze.getBoxes()[x][y].getbUp1().getY(), (float) maze.getBoxes()[x][y].getbUp1().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp2().getX(), (float) maze.getBoxes()[x][y].getbUp2().getY(), (float) maze.getBoxes()[x][y].getbUp2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp3().getX(), (float) maze.getBoxes()[x][y].getbUp3().getY(), (float) maze.getBoxes()[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp4().getX(), (float) maze.getBoxes()[x][y].getbUp4().getY(), (float) maze.getBoxes()[x][y].getbUp4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp1().getX(), (float) maze.getBoxes()[x][y].getbUp1().getY(), (float) maze.getBoxes()[x][y].getbUp1().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp4().getX(), (float) maze.getBoxes()[x][y].getbUp4().getY(), (float) maze.getBoxes()[x][y].getbUp4().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp2().getX(), (float) maze.getBoxes()[x][y].getbUp2().getY(), (float) maze.getBoxes()[x][y].getbUp2().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp1().getX(), (float) maze.getBoxes()[x][y].getbUp1().getY(), (float) maze.getBoxes()[x][y].getbUp1().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp3().getX(), (float) maze.getBoxes()[x][y].getbUp3().getY(), (float) maze.getBoxes()[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp2().getX(), (float) maze.getBoxes()[x][y].getbUp2().getY(), (float) maze.getBoxes()[x][y].getbUp2().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp3().getX(), (float) maze.getBoxes()[x][y].getbUp3().getY(), (float) maze.getBoxes()[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp4().getX(), (float) maze.getBoxes()[x][y].getbUp4().getY(), (float) maze.getBoxes()[x][y].getbUp4().getZ());

        glEnd();
    }

    //Vykreslení boxu/zdi pomoci boxu
    private void renderBox(Box box) {
        textureWall.bind();
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

    //Vykreslení podlahy
    private void renderPlate(int x, int y) {
        textureFloor.bind();
        glBegin(GL_QUADS);
        glColor3f(1f, 0f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());

        glEnd();
    }

    //Vykreslení podlahy označující cestu z bludiště
    private void renderPlateHelp(int x, int y) {
        textureHelp.bind();
        glBegin(GL_QUADS);
        glColor3f(1f, 0f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());

        glEnd();
    }

    //Vykreslení cile
    private void renderFinish(int x, int y) {
        textureFinish.bind();
        glBegin(GL_QUADS);
        glColor3f(0f, 1f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp1().getX(), (float) maze.getBoxes()[x][y].getbUp1().getY(), (float) maze.getBoxes()[x][y].getbUp1().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp2().getX(), (float) maze.getBoxes()[x][y].getbUp2().getY(), (float) maze.getBoxes()[x][y].getbUp2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp3().getX(), (float) maze.getBoxes()[x][y].getbUp3().getY(), (float) maze.getBoxes()[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp4().getX(), (float) maze.getBoxes()[x][y].getbUp4().getY(), (float) maze.getBoxes()[x][y].getbUp4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp1().getX(), (float) maze.getBoxes()[x][y].getbUp1().getY(), (float) maze.getBoxes()[x][y].getbUp1().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp4().getX(), (float) maze.getBoxes()[x][y].getbUp4().getY(), (float) maze.getBoxes()[x][y].getbUp4().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp2().getX(), (float) maze.getBoxes()[x][y].getbUp2().getY(), (float) maze.getBoxes()[x][y].getbUp2().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp1().getX(), (float) maze.getBoxes()[x][y].getbUp1().getY(), (float) maze.getBoxes()[x][y].getbUp1().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp3().getX(), (float) maze.getBoxes()[x][y].getbUp3().getY(), (float) maze.getBoxes()[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp2().getX(), (float) maze.getBoxes()[x][y].getbUp2().getY(), (float) maze.getBoxes()[x][y].getbUp2().getZ());

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp3().getX(), (float) maze.getBoxes()[x][y].getbUp3().getY(), (float) maze.getBoxes()[x][y].getbUp3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getbUp4().getX(), (float) maze.getBoxes()[x][y].getbUp4().getY(), (float) maze.getBoxes()[x][y].getbUp4().getZ());

        glEnd();
    }

    //Vykreslení startu
    private void renderStart(int x, int y) {
        textureStart.bind();
        glBegin(GL_QUADS);
        glColor3f(1f, 0f, 0f);

        glTexCoord2f(0, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getbH().getX(), (float) maze.getBoxes()[x][y].getbH().getY(), (float) maze.getBoxes()[x][y].getbH().getZ());
        glTexCoord2f(1, 0);
        glVertex3f((float) maze.getBoxes()[x][y].getB2().getX(), (float) maze.getBoxes()[x][y].getB2().getY(), (float) maze.getBoxes()[x][y].getB2().getZ());
        glTexCoord2f(1, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB3().getX(), (float) maze.getBoxes()[x][y].getB3().getY(), (float) maze.getBoxes()[x][y].getB3().getZ());
        glTexCoord2f(0, 1);
        glVertex3f((float) maze.getBoxes()[x][y].getB4().getX(), (float) maze.getBoxes()[x][y].getB4().getY(), (float) maze.getBoxes()[x][y].getB4().getZ());

        glEnd();
    }

    //Vykreslení skyboxu
    private void skyBox() {

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        glRotatef(90,1.0f,0.f,0.f);
        glRotatef(270,0.f,1.0f,0.f);

        glColor3d(0.5, 0.5, 0.5);
        int size = 6 * maze.getDelkaHrany();

        glutWireCube(size);
        glEnable(GL_TEXTURE_2D);

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
}
