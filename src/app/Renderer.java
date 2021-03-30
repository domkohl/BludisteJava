package app;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.Vec3D;
import utils.AbstractRenderer;
import utils.GLCamera;


import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static utils.GluUtils.gluPerspective;
import static utils.GluUtils.gluLookAt;
import static org.lwjgl.opengl.GL11.*;

/**
 * Simple scene rendering
 *
 * @author PGRF FIM UHK
 * @version 3.1
 * @since 2020-01-20
 */
public class Renderer extends AbstractRenderer {
    private GLCamera camera;
    double vpredVzad = 0.0;

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

        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    double x = 0,y = 0;
//                    glfwGetCursorPos(window, x, y);

                    System.out.println(x+" test kliku  "+y);
                }
            }

        };

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
//                System.out.println("glfwCursorPosCallback  "+x+"   "+y);
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
                if (key == GLFW_KEY_W && action == GLFW_PRESS)
                    camera.forward(0.1);
                if (key == GLFW_KEY_S && action == GLFW_PRESS)
                    camera.backward(0.1);
//                    System.out.println(dopredu);

            }
        };

    }

    @Override
    public void init() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glEnable(GL_DEPTH_TEST);

        glFrontFace(GL_CCW);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_LINE);
        glDisable(GL_CULL_FACE);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glMatrixMode(GL_MODELVIEW);

        glLoadIdentity();

        camera = new GLCamera();
        camera.setPosition(new Vec3D(0,0,1));
    }

    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glClearColor(0f, 0f, 0f, 1f);


        //Mdoelovaci
//        glMatrixMode(GL_MODELVIEW);
//        glLoadIdentity();
//        glScalef(5,5,5);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, width / (float) height, 0.1f, 100.0f);

//        gluLookAt(0., 0., -10., 0., 0., 0., 1., 1., 0.);

//        gluLookAt(vpredVzad, vpredVzad, 1, 0, 1, 0.5, 0, 0, 1);

//        camera.setFirstPerson(false);
//        camera.setRadius(5);
//        camera.setMatrix();

//        camera.setPosition(new Vec3D(0,0,1));
        camera.setMatrix();

        // Rendering triangle by fixed pipeline
        glBegin(GL_QUADS);
        glColor3f(1f, 0f, 0f);

        glVertex3f(-1f, 1f,0f);
        glColor3f(0f, 0f, 1f);
        glVertex3f(-1f, -1f,0f);
        glVertex3f(1, -1,0f);
        glColor3f(0f, 1f, 0f);
        glVertex3f(1, 1,0f);

        glEnd();

    }

}
