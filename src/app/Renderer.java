package app;

import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
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

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static utils.GluUtils.gluPerspective;


/**
 * Třída pro Renderování bludište a práce s ním
 */
public class Renderer extends AbstractRenderer {
    int pocetKrychli;
    int delkaHrany;
    int jednaHrana;
    int[][] rozlozeniBludiste;
    Box[][] boxes;
    ArrayList<Box> spawnHelpBoxes = new ArrayList<>();
    double spawnX, spawnZ;
    int spawnI, spawnJ;
    boolean showCursor = true;
    private GLCamera camera;
    private float dx, dy, ox, oy;
    private OGLTexture2D[] textureCube;
    private float azimut, zenit;
    private OGLTexture2D texture1, texture2, textureFinish, textureStart;


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

    OGLModelOBJ model;
    int shaderProgram;


    FindWayBFS findWay = new FindWayBFS();




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
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                //TODO
            }
        };

        //rozhlizeni
        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
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


        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                //do nothing
            }
        };
        //Pohyb
        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_W) {
                    GLCamera tmp = new GLCamera(camera);
                    tmp.forward(0.04);
                    if (isOutside(tmp) == 0)
                        camera.forward(0.04);
                    if (isOutside(tmp) == 2)
                        System.out.println("Gratuluji jsi v cíli");
                }

                if (key == GLFW_KEY_S) {
                    GLCamera tmp = new GLCamera(camera);
                    tmp.backward(0.04);
                    if (isOutside(tmp) == 0)
                        camera.backward(0.04);
                    if (isOutside(tmp) == 2)
                        System.out.println("Gratuluji jsi v cíli");
                }

                if (key == GLFW_KEY_A) {
                    GLCamera tmp = new GLCamera(camera);
                    tmp.left(0.04);
                    if (isOutside(tmp) == 0)
                        camera.left(0.04);
                    if (isOutside(tmp) == 2)
                        System.out.println("Gratuluji jsi v cíli");
                }
                if (key == GLFW_KEY_D) {
                    GLCamera tmp = new GLCamera(camera);
                    tmp.right(0.04);
                    if (isOutside(tmp) == 0)
                        camera.right(0.04);
                    if (isOutside(tmp) == 2)
                        System.out.println("Gratuluji jsi v cíli");
                }
                if (key == GLFW_KEY_R && action == GLFW_PRESS) {
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
                if (key == GLFW_KEY_E && action == GLFW_PRESS) {
                    animateStart = !animateStart;
                }
            }
        };
    }

    //Inicializace bludiste
    @Override
    public void init() {

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
            texture1 = new OGLTexture2D("textures/floor.jpg");
            texture2 = new OGLTexture2D("textures/wall.png");
            textureFinish = new OGLTexture2D("textures/finish.jpg");
            textureStart = new OGLTexture2D("textures/start.jpg");
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
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        camera = new GLCamera();
        skyBox();
        createMaze();
        camera.setPosition(new Vec3D(spawnX * 0.04, 5 * 0.04, spawnZ * 0.04));

        //???
        Arrays.fill(modelMatrixEnemy, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScalef(0.04f, 0.04f, 0.04f);
        glGetFloatv(GL_MODELVIEW_MATRIX,modelMatrixEnemy);

        //objekt

        model = new OGLModelOBJ("/obj/ducky.obj");
        shaderProgram = ShaderUtils.loadProgram("/shaders/ducky");
    }


    //Funkce pro neustale vykreslovani
    @Override
    public void display() {
        // vypocet fps, nastaveni rychlosti otaceni podle rychlosti prekresleni
        long mils = System.currentTimeMillis();
        if ((mils - oldFPSmils) > 300) {
            fps = 1000 / (double) (mils - oldmils + 1);
            oldFPSmils = mils;
        }
        String textInfo = String.format(Locale.US, "FPS %3.1f", fps);

        //System.out.println(fps);
        float speed = 20; // pocet stupnu rotace za vterinu
        step = speed * (mils - oldmils) / 1000.0f; // krok za jedno
        oldmils = mils;


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
//        camera.setPosition(cameraFixedY.withY(0.20));
        camera.setMatrix();

        texture1.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

        skyBox();


//        rozlozeniBludiste = findWay.shortestPath(rozlozeniBludiste,new int[]{1,4},new int[]{9,5});


        renderMaze();


//        glUseProgram(shaderProgram);
//        model.getBuffers().draw(model.getTopology(), shaderProgram);
//        glDeleteProgram(shaderProgram);

    }

    private void renderEnemy(int x,int y) {
        if (!animateStart) return;
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        //zajisteni naharani matice pro npc
        if(newMove){
            glLoadIdentity();
            glScalef(0.04f, 0.04f, 0.04f);
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
        textureFinish.bind();

        glBegin(GL_QUADS);

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbUp1().getX()-zmenseni, (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ()-zmenseni);
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getbUp2().getX()-zmenseni, (float) boxes[x][y].getbUp2().getY(), (float) boxes[x][y].getbUp2().getZ()+zmenseni);
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp3().getX()+zmenseni, (float) boxes[x][y].getbUp3().getY(), (float) boxes[x][y].getbUp3().getZ()+zmenseni);
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX()+zmenseni, (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ()-zmenseni);

        glTexCoord2f(0, 0);
        glVertex3f((float) boxes[x][y].getbH().getX()-zmenseni, (float) boxes[x][y].getbH().getY(), (float) boxes[x][y].getbH().getZ()-zmenseni);
        glTexCoord2f(1, 0);
        glVertex3f((float) boxes[x][y].getbUp1().getX()-zmenseni, (float) boxes[x][y].getbUp1().getY(), (float) boxes[x][y].getbUp1().getZ()-zmenseni);
        glTexCoord2f(1, 1);
        glVertex3f((float) boxes[x][y].getbUp4().getX()+zmenseni, (float) boxes[x][y].getbUp4().getY(), (float) boxes[x][y].getbUp4().getZ()-zmenseni);
        glTexCoord2f(0, 1);
        glVertex3f((float) boxes[x][y].getB4().getX()+zmenseni, (float) boxes[x][y].getB4().getY(), (float) boxes[x][y].getB4().getZ()-zmenseni);
//        glUseProgram(shaderProgram);
//        model.getBuffers().draw(model.getTopology(), shaderProgram);

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
                if (rozlozeniBludiste[i][j] == 0) {
                    if (boxes[i][j].getxMin() * 0.04  <= camX && camX <= boxes[i][j].getxMax() * 0.04  &&
                            boxes[i][j].getyMin() * 0.04 <= camY && camY <= boxes[i][j].getyMax() * 0.04  &&
                            boxes[i][j].getzMin() * 0.04  <= camZ && camZ <= boxes[i][j].getzMax() * 0.04 )
                        System.out.println("jsem i: "+i +"  a j: "+j );
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

                    if(firstTimeRenderEnemy){
                        allVisitedEnemy.add(new int[]{i,j,0});
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
                    }else{
                        renderEnemy(i, j);
                    }
                }else if (rozlozeniBludiste[i][j] == 5) {
                    renderFinish(i, j);
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
        if(j+1 < delkaHrany && j+1>=0){
            if(rozlozeniBludiste[i][j+1] == 0 && isNotInsideEnemyWay(i,j+1)){
            int[] tmp = {i,j+1,1};
            possbileWays.add(tmp);
            }
        }

        if(j-1 < delkaHrany && j-1>=0){
        if(rozlozeniBludiste[i][j-1] == 0 && isNotInsideEnemyWay(i,j-1)){
            int[] tmp = {i,j-1,2};
            possbileWays.add(tmp);
            }
        }
        if(i+1 < delkaHrany && i+1>=0 && isNotInsideEnemyWay(i+1,j)){
        if(rozlozeniBludiste[i+1][j] == 0){
            int[] tmp = {i+1,j,3};
            possbileWays.add(tmp);
            }
        }
        if(i-1 < delkaHrany && i-1>=0 && isNotInsideEnemyWay(i-1,j)){
        if(rozlozeniBludiste[i-1][j] == 0){
            int[] tmp = {i-1,j,4};
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
            allVisitedEnemy.add(new int[]{i,j,0});
            //TODO optimazilovat dat if do funcki a vratit list
            if(j+1 < delkaHrany && j+1>=0){
                if(rozlozeniBludiste[i][j+1] == 0 && isNotInsideEnemyWay(i,j+1)){
                    int[] tmp = {i,j+1,1};
                    possbileWays.add(tmp);
                }
            }

            if(j-1 < delkaHrany && j-1>=0){
                if(rozlozeniBludiste[i][j-1] == 0 && isNotInsideEnemyWay(i,j-1)){
                    int[] tmp = {i,j-1,2};
                    possbileWays.add(tmp);
                }
            }
            if(i+1 < delkaHrany && i+1>=0 && isNotInsideEnemyWay(i+1,j)){
                if(rozlozeniBludiste[i+1][j] == 0){
                    int[] tmp = {i+1,j,3};
                    possbileWays.add(tmp);
                }
            }
            if(i-1 < delkaHrany && i-1>=0 && isNotInsideEnemyWay(i-1,j)){
                if(rozlozeniBludiste[i-1][j] == 0){
                    int[] tmp = {i-1,j,4};
                    possbileWays.add(tmp);
                }
            }

        }

        if(possbileWays.size() == 0)
            possbileWays.add(new int[]{i,j,0});

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

        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
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

        rozlozeniBludiste = findWay.shortestPath(rozlozeniBludiste,new int[]{1,4},new int[]{9,5});
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
}
