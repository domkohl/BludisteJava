package app.fileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class FileReader {

    //Pomocna funkce pro cteni ze souboru
    public String readFromFile(String filename, String extension){
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, extension))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void parseFile(String filename) {
        try{
            String data = readFromFile(filename, "obj");
            String[] lines = data.split("\n");

            for (String s:lines ){
                System.out.println(s);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
