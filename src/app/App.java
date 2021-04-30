package app;

import app.renderer.Renderer;
import utils.LwjglWindow;

/**
 * Třída pro spousteni bludiste
 */
public class App {

    public static void main(String[] args) {
        new LwjglWindow(1280, 720, new Renderer(), false);
    }

}


