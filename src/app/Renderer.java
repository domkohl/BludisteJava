package app;

import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.Vec3D;
import utils.AbstractRenderer;
import utils.GLCamera;

import java.io.IOException;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static utils.GluUtils.gluPerspective;
import static utils.GlutUtils.glutWireCube;


/**
 * Simple scene rendering
 *
 * @author PGRF FIM UHK
 * @version 3.1
 * @since 2020-01-20
 */
public class Renderer extends AbstractRenderer {
    int pocetKrychli = 5;
    int delkaHrany = 100;
    int jednaHrana = delkaHrany / pocetKrychli;
    int[][] rozlozeniBludiste = new int[pocetKrychli][pocetKrychli];
    Box[][] boxes = new Box[pocetKrychli][pocetKrychli];
    private GLCamera camera;
    private boolean mouseButton1 = false;
    private float dx, dy, ox, oy;
//    private float zenit = -1.5707963267948966f ;
//    private float azimut = -3.141587327267613f ;

    private OGLTexture2D[] textureCube;

    private float azimut, zenit;

    private OGLTexture2D texture1, texture2;
    private OGLTexture2D.Viewer textureViewer;

    public Renderer() {
        super();


        glfwWindowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
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
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);

                mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    ox = (float) x;
                    oy = (float) y;
                    System.out.println(x + " , " + y);
                }
            }

        };

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseButton1) {
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


//
//                    camera.addAzimuth(Math.PI / 2 * (dx) / width);
//                    camera.addZenith(Math.PI / 2 * (dx) / width);

                    dx = 0;
                    dy = 0;
//                    System.out.println(x + " , " + y);
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
                if (key == GLFW_KEY_W && action == GLFW_PRESS) {
                    GLCamera tmp = new GLCamera(camera);
                    tmp.forward(0.1);
                    if (isOutside(tmp))
                        camera.forward(0.1);
                    System.out.println(camera.getPosition().toString());
                }

                if (key == GLFW_KEY_S && action == GLFW_PRESS){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.backward(0.1);
                    if (isOutside(tmp))
                        camera.backward(0.1);
                }

                if (key == GLFW_KEY_A && action == GLFW_PRESS){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.left(0.1);
                    if (isOutside(tmp))
                        camera.left(0.1);
                }
                if (key == GLFW_KEY_D && action == GLFW_PRESS){
                    GLCamera tmp = new GLCamera(camera);
                    tmp.right(0.1);
                    if (isOutside(tmp))
                        camera.right(0.1);
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
//        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glMatrixMode(GL_MODELVIEW);
//        glActiveTexture(GL_TEXTURE0);

        glLoadIdentity();

        textureCube = new OGLTexture2D[6];

        try {
            texture1 = new OGLTexture2D("textures/floor.jpg"); // vzhledem k adresari res v projektu
            texture2 = new OGLTexture2D("textures/wall.png"); // vzhledem k adresari res v projektu

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
        camera.setPosition(new Vec3D(30 * 0.04, 5 * 0.04, 100 * 0.04));

//        camera.setAzimuth(20);
//        camera.setPosition(new Vec3D(0,0,10));
//        camera.setAzimuth(-3.141587327267613);
//        camera.setZenith(-1.5707963267948966);


        skyBox();
        createMaze();

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
        Vec3D cameraFixedZ = camera.getPosition();
        camera.setPosition(cameraFixedZ.withY(5 * 0.04));
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

    private boolean isOutside(GLCamera cam) {
        double camX = cam.getPosition().getX();
        double camY = cam.getPosition().getY();
        double camZ = cam.getPosition().getZ();

        System.out.println("testuji");
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 1) {
                    if (    boxes[i][j].getxMin()* 0.04 * 0.98 <= camX && camX <= boxes[i][j].getxMax()* 0.04 * 1.02 &&
                            boxes[i][j].getyMin()* 0.04 * 0.98 <= camY && camY <= boxes[i][j].getyMax()* 0.04 * 1.02 &&
                            boxes[i][j].getzMin()* 0.04 * 0.98 <= camZ && camZ <= boxes[i][j].getzMax()* 0.04 * 1.02)
                        return false;
                }
            }
        }
        return true;
    }

    private void renderMaze() {
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 0) {
                    renderPlate(i, j);
                } else {
                    renderBox(i, j);
                }
            }
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

    private void createMaze() {
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                rozlozeniBludiste[i][j] = 1;
                boxes[i][j] = new Box(i, j, jednaHrana);
            }
        }

        rozlozeniBludiste[3][0] = 0;
        rozlozeniBludiste[3][1] = 0;
        rozlozeniBludiste[3][2] = 0;
        rozlozeniBludiste[2][2] = 0;
        rozlozeniBludiste[2][3] = 0;
        rozlozeniBludiste[1][3] = 0;
        rozlozeniBludiste[1][4] = 0;

    }

    private void drawSimpleScene() {
        texture1.bind();


//        // Rendering triangle by fixed pipeline
        glBegin(GL_QUADS);
        glTexCoord2f(0.1f, 0.1f);
        glVertex3f(0.0f, 10.0f, 0.0f);
        glTexCoord2f(0.0f, 0.9f);
        glVertex3f(0.0f, 10.0f, 10.0f);
        glTexCoord2f(1.1f, 0.8f);
        glVertex3f(0.0f, 0.0f, 10.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glEnd();

//        glBegin(GL_TRIANGLES);
//        glTexCoord2f(0, 0);
//        glColor3f(1f, 0f, 0f);
//        glVertex3f(-1f, -1, 0.9f);
//
//        glTexCoord2f(0, 1);
//        glColor3f(0f, 1f, 0f);
//        glVertex3f(1, 0, 0.9f);
//
//        glTexCoord2f(1, 0);
//        glColor3f(0f, 0f, 1f);
//        glVertex3f(0, 1, 0.9f);
//        glEnd();


    }

    private void skyBox() {
//        glNewList(0, GL_COMPILE);
        glPushMatrix();
        glColor3d(0.5, 0.5, 0.5);
        int size = 250;
        glutWireCube(size); //neni nutne, pouze pro znazorneni tvaru skyboxu

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

}
