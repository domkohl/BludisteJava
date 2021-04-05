package app;

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
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static utils.GluUtils.gluPerspective;



/**
 * Simple scene rendering
 *
 * @author PGRF FIM UHK
 * @version 3.1
 * @since 2020-01-20
 */
public class Renderer extends AbstractRenderer {
    int pocetKrychli;
    int delkaHrany;
    int jednaHrana;
    int[][] rozlozeniBludiste;
    Box[][] boxes;
    ArrayList<Box> spawnHelpBoxes = new ArrayList<>();
    private GLCamera camera;
    private float dx, dy, ox, oy;

    private OGLTexture2D[] textureCube;

    private float azimut, zenit;
    private OGLTexture2D texture1, texture2,textureFinish,textureStart;

    double spawnX,spawnZ;
    int spawnI,spawnJ;

    boolean showCursor = true;
    public Renderer() {
        super();


        glfwWindowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
//                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            }
        };

        /*used default glfwKeyCallback */
//        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
//
//            @Override
//            public void invoke(long window, int button, int action, int mods) {
//
//                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
//                    double x = 0,y = 0;
////                    glfwGetCursorPos(window, x, y);
//
//                    System.out.println(x+" test kliku  "+y);
//                }
//            }
//
//        };

        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                //TODO
            }

        };

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

                if (key == GLFW_KEY_S){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.backward(0.04);
                    if (isOutside(tmp) == 0)
                        camera.backward(0.04);
                    if (isOutside(tmp) == 2)
                        System.out.println("Gratuluji jsi v cíli");
                }

                if (key == GLFW_KEY_A){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.left(0.04);
                    if (isOutside(tmp) == 0)
                        camera.left(0.04);
                    if (isOutside(tmp) == 2)
                        System.out.println("Gratuluji jsi v cíli");
                }
                if (key == GLFW_KEY_D){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.right(0.04);
                    if (isOutside(tmp) == 0)
                        camera.right(0.04);
                    if (isOutside(tmp) == 2)
                        System.out.println("Gratuluji jsi v cíli");
                }
                if (key == GLFW_KEY_R && action == GLFW_PRESS){
                    showCursor = !showCursor;
                    if(showCursor){
                        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    } else{
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
            }
        };
    }

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
            texture1 = new OGLTexture2D("textures/floor.jpg"); // vzhledem k adresari res v projektu
            texture2 = new OGLTexture2D("textures/wall.png"); // vzhledem k adresari res v projektu
            textureFinish = new OGLTexture2D("textures/finish.jpg"); // vzhledem k adresari res v projektu
            textureStart = new OGLTexture2D("textures/start.jpg"); // vzhledem k adresari res v projektu
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
        camera.setPosition(new Vec3D(spawnX* 0.04, 5 * 0.04, spawnZ* 0.04));
    }


    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(0f, 0f, 0f, 1f);

        //Mdoelovaci
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glScalef(0.04f, 0.04f, 0.04f);
//        glScalef(0.5f,0.5f,0.5f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, width / (float) height, 0.01f, 10000.0f);

//        gluLookAt(0., 0., -10., 0., 0., 0., 1., 1., 0.);

//        gluLookAt(0, 0, 1, 0, 1, 0.5, 0, 0, 1);
//        glLoadIdentity();
        camera.setFirstPerson(true);
        Vec3D cameraFixedY = camera.getPosition();
//        System.out.println(camera.toString());
        camera.setPosition(cameraFixedY.withY(0.20));
//        System.out.println("dvojka"+camera.toString());
//        camera.setRadius(5);
        camera.setMatrix();
//        System.out.println("azimuth: "+camera.getAzimuth());
//        System.out.println("zenith: "+camera.getZenith());

//        renderMaze();

        texture1.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

//        drawSimpleScene();


        skyBox();
        renderMaze();

    }

    // 0-jsemv bludsiti...1-jsem blizko zdi...2--jsem v cili
    private int isOutside(GLCamera cam) {
        double camX = cam.getPosition().getX();
        double camY = cam.getPosition().getY();
        double camZ = cam.getPosition().getZ();
        //TODO optimalizovat do funci if statmenty
        System.out.println("testuji");
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 1) {
                    if (    boxes[i][j].getxMin()* 0.04 * 0.98 <= camX && camX <= boxes[i][j].getxMax()* 0.04 * 1.02 &&
                            boxes[i][j].getyMin()* 0.04 * 0.98 <= camY && camY <= boxes[i][j].getyMax()* 0.04 * 1.02 &&
                            boxes[i][j].getzMin()* 0.04 * 0.98 <= camZ && camZ <= boxes[i][j].getzMax()* 0.04 * 1.02)
                        return 1;
                }
                if (rozlozeniBludiste[i][j] == 3) {
                    if (    boxes[i][j].getxMin()* 0.04 * 0.98 <= camX && camX <= boxes[i][j].getxMax()* 0.04 * 1.02 &&
                            boxes[i][j].getyMin()* 0.04 * 0.98 <= camY && camY <= boxes[i][j].getyMax()* 0.04 * 1.02 &&
                            boxes[i][j].getzMin()* 0.04 * 0.98 <= camZ && camZ <= boxes[i][j].getzMax()* 0.04 * 1.02)
                        return 2;
                }
            }
        }
        for (Box box:spawnHelpBoxes) {
                if (    box.getxMin()* 0.04 * 0.98 <= camX && camX <= box.getxMax()* 0.04 * 1.02 &&
                        box.getyMin()* 0.04 * 0.98 <= camY && camY <= box.getyMax()* 0.04 * 1.02 &&
                        box.getzMin()* 0.04 * 0.98 <= camZ && camZ <= box.getzMax()* 0.04 * 1.02)
                    return 1;

        }
        return 0;
    }

    private void renderMaze() {
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 0) {
                    renderPlate(i, j);
                } else if (rozlozeniBludiste[i][j] == 3){
                    renderFinish(i,j);
                }else if(rozlozeniBludiste[i][j] == 2){
                    renderStart(i,j);
                }
                else {
                    renderBox(i, j);
                }
            }
        }

        for (Box box : spawnHelpBoxes) {
            renderBox(box);
        }

    }

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

    private void renderFinish(int x, int y){
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


        //TODO: nevykresluje nenevim proc overi u jinych bludist--> vykresluje ale ne kdyz opoblisz box zdi
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

    private void renderStart(int x, int y){
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

    private void createMaze() {
//        for (int i = 0; i < pocetKrychli; i++) {
//            for (int j = 0; j < pocetKrychli; j++) {
//                rozlozeniBludiste[i][j] = 1;
//                boxes[i][j] = new Box(i, j, jednaHrana);
//            }
//        }
//        // 0 cesta, 1 blok, 2 start, 3 cil
//        rozlozeniBludiste[3][0] = 3;
//        rozlozeniBludiste[3][1] = 0;
//        rozlozeniBludiste[3][2] = 0;
//        rozlozeniBludiste[2][2] = 0;
//        rozlozeniBludiste[2][3] = 0;
//        rozlozeniBludiste[1][3] = 0;
//        rozlozeniBludiste[0][4] = 2;
//        rozlozeniBludiste[1][4] = 0;

        parseTxt("src/res/proportions/maze");

        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if(rozlozeniBludiste[i][j] == 2){
                    spawnI = i;
                    spawnJ = j;
                    spawnX = (boxes[i][j].getbH().getX()+
                            boxes[i][j].getB2().getX()+
                            boxes[i][j].getB3().getX()+
                            boxes[i][j].getB4().getX()+
                            boxes[i][j].getbUp4().getX()+
                            boxes[i][j].getbUp3().getX()+
                            boxes[i][j].getbUp2().getX()+
                            boxes[i][j].getbUp1().getX()
                    )/8;

                    spawnZ = (boxes[i][j].getbH().getZ()+
                            boxes[i][j].getB2().getZ()+
                            boxes[i][j].getB3().getZ()+
                            boxes[i][j].getB4().getZ()+
                            boxes[i][j].getbUp4().getZ()+
                            boxes[i][j].getbUp3().getZ()+
                            boxes[i][j].getbUp2().getZ()+
                            boxes[i][j].getbUp1().getZ()
                    )/8;;
                }

                if(rozlozeniBludiste[i][j] == 0){
                    addBoxIfPossible(i,j+1);
                    addBoxIfPossible(i+1,j);
                    addBoxIfPossible(i-1,j);
                    addBoxIfPossible(i,j-1);
                }

            }
        }


        //TODo rozsirit pro vsehny 4 strany a oprimalizovat kod opakovani
//        try {
//             double tmp = boxes[spawnI][spawnJ+1].getxMax();
//                System.out.println("v proadku");
//        }
//        catch(ArrayIndexOutOfBoundsException e){
//            System.out.println("arrray prekrocen");
//
//            Box tmp = new Box(spawnI,spawnJ+1,jednaHrana);
//            spawnHelpBoxes.add(tmp);
//
//        }
//        catch(Exception e){
//            System.out.println(e.toString());
//        }

        addBoxIfPossible(spawnI,spawnJ+1);
        addBoxIfPossible(spawnI+1,spawnJ);
        addBoxIfPossible(spawnI-1,spawnJ);
        addBoxIfPossible(spawnI,spawnJ-1);

    }

    private void addBoxIfPossible(int i, int j){
        try {
            double tmp = boxes[i][j].getxMax();
            System.out.println("v proadku");
        }
        catch(ArrayIndexOutOfBoundsException e){
            System.out.println("arrray prekrocen");
            Box tmp = new Box(i,j,jednaHrana);
            spawnHelpBoxes.add(tmp);
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }

    private void skyBox() {
//        glNewList(0, GL_COMPILE);
        glPushMatrix();
        glColor3d(0.5, 0.5, 0.5);
        int size = 250;
//        glutWireCube(size); //neni nutne, pouze pro znazorneni tvaru skyboxu

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

//        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glEndList();
    }

    public String readFromFile(String filename, String extension) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, extension))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void parseTxt(String filename) {
        String data = readFromFile(filename, "txt");
        String[] lines = data.split("\n");
        String[] velikostString = lines[0].split("!");
        String[] velikostString2 = lines[1].split("!");
        pocetKrychli = Integer.parseInt(velikostString[1]);
        delkaHrany = Integer.parseInt(velikostString2[1]);
        rozlozeniBludiste = new int[pocetKrychli][pocetKrychli];
        boxes = new Box[pocetKrychli][pocetKrychli];
        jednaHrana = delkaHrany/pocetKrychli;

        for (int i = 0;i<pocetKrychli;i++ ) {
            // rozdeleni radku na jednotlive segmenty
            String[] attributes = lines[i+2].split(" ! ");
            for (int j = 0; j<pocetKrychli;j++){
                switch (attributes[j]) {
                    case "c" -> rozlozeniBludiste[i][j] = 0;
                    case "S" -> rozlozeniBludiste[i][j] = 2;
                    case "K" -> rozlozeniBludiste[i][j] = 3;
                    default -> rozlozeniBludiste[i][j] = 1;
                }
                boxes[i][j] = new Box(i, j, jednaHrana);
            }
        }
        System.out.println();
    }

}
