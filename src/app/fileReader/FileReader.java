package app.fileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Pomocná abstraktní třída pro čtení a práci se soubory
 */

public abstract class FileReader {

    //Pomocná funkce pro ctěni ze souboru
    public String readFromFile(String filename, String extension) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, extension))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Základní funkce pro analýzu dat
    public void parseFile(String filename) {
        try {
            String data = readFromFile(filename, "obj");
            String[] lines = data.split("\n");

            for (String s : lines) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
