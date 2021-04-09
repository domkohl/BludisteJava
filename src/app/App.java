package app;

import utils.LwjglWindow;

/**
 * Třída pro spousteni bludiste
 */
public class App {

    public static void main(String[] args) {
        new LwjglWindow(new Renderer(), false);
    }

}


