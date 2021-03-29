package app;

import utils.AbstractRenderer;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import utils.GLCamera;

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

        glfwMouseButtonCallback = null; //glfwMouseButtonCallback do nothing

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                System.out.println("glfwCursorPosCallback  "+x+"   "+y);
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                //do nothing
            }
        };
    }

    @Override
    public void init() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        camera = new GLCamera();
    }

    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        //zapnuti Z-bufferu
        glEnable(GL_DEPTH_TEST);


        //Mdoelovaci
        glMatrixMode(GL_MODELVIEW);
//        glLoadIdentity();
//        gluLookAt(0, 0, -0.5, 0, 0, 0, 0, 0, -500.5);
        glLoadIdentity();
        camera.setFirstPerson(false);
        camera.setRadius(20);
        camera.setMatrix();


        //projekce
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, width / (float) height, 0.1f, 200.0f);





        // Rendering triangle by fixed pipeline
        glBegin(GL_TRIANGLES);
        glColor3f(1f, 0f, 0f);
        glVertex3f(-1f, -1,0);
        glColor3f(0f, 1f, 0f);
        glVertex3f(1, 0,0f);
        glColor3f(0f, 0f, 1f);
        glVertex3f(0, 1,0);

        glEnd();






    }

}
