package app.npc;


import app.fileReader.FileReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Třída pro načtení objektu(NPC) ze souboru
 */

public class ObjReader extends FileReader {

    private final List<float[]> vrcholy;
    private final List<float[]> textury;
    private final List<int[]> indices;

    public ObjReader() {
        this.vrcholy = new ArrayList<>();
        this.textury = new ArrayList<>();
        this.indices = new ArrayList<>();

        //Pro generování jar souboru
        parseFile("src/res/obj/chess");
//        parseFile(System.getProperty("user.dir") + "/obj/chess");

    }

    //Načtení objektu ze souboru
    @Override
    public void parseFile(String filename) {

        try {
            String data = readFromFile(filename, "obj");
            String[] lines = data.split("\n");

            for (String s : lines) {
                String[] curretnLine = s.split(" ");
                //Vrcholy
                if (s.startsWith("v ")) {
                    float[] vertex = new float[]{Float.parseFloat(curretnLine[1]), Float.parseFloat(curretnLine[2]), Float.parseFloat(curretnLine[3])};
                    vrcholy.add(vertex);

                    //Textury
                } else if (s.startsWith("vt ")) {
                    float[] texture = new float[]{Float.parseFloat(curretnLine[1]), Float.parseFloat(curretnLine[2])};
                    textury.add(texture);
                    //Jak spojit
                } else if (s.startsWith("f ")) {
                    String[] vertex1 = curretnLine[1].split("/");
                    String[] vertex2 = curretnLine[2].split("/");
                    String[] vertex3 = curretnLine[3].split("/");
                    String[] vertex4 = curretnLine[4].split("/");

                    //Z quadu vytvořit trojúhelník pro vykreslení
                    int[] indice1 = new int[]{Integer.parseInt(vertex1[0]), Integer.parseInt(vertex1[1]),
                            Integer.parseInt(vertex2[0]), Integer.parseInt(vertex2[1]),
                            Integer.parseInt(vertex3[0]), Integer.parseInt(vertex3[1])};

                    int[] indice2 = new int[]{Integer.parseInt(vertex1[0]), Integer.parseInt(vertex1[1]),
                            Integer.parseInt(vertex3[0]), Integer.parseInt(vertex3[1]),
                            Integer.parseInt(vertex4[0]), Integer.parseInt(vertex4[1])};

                    //Přidat je do seznamu pro vykreslení
                    indices.add(indice1);
                    indices.add(indice2);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Get
    public List<float[]> getVrcholy() {
        return vrcholy;
    }

    public List<float[]> getTextury() {
        return textury;
    }

    public List<int[]> getIndices() {
        return indices;
    }
}
