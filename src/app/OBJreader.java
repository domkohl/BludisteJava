package app;

import org.lwjgl.ovr.OVRVector2f;
import org.lwjgl.ovr.OVRVector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OBJreader {

    private List<float[]> vrcholy;
    private List<float[]> textury;
    private List<int[]> indices;

    public OBJreader() {
        this.vrcholy =  new ArrayList<>();
        this.textury = new ArrayList<>();
        this.indices =  new ArrayList<>();
        loadObj("src/res/obj/chess");
//        loadObj("src/res/obj/woodentets1");
//        loadObj("src/res/obj/fdfsfs");
//        loadObj("src/res/obj/testchecss");
//        loadObj("src/res/obj/woodenchess");
    }

    public static String readFromFile(String filename, String extension) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, extension))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Nacteni bludiste ze souboru
    public void loadObj(String filename) {
        String data = readFromFile(filename, "obj");
        String[] lines = data.split("\n");

        int linenumb = 0;
        for (String s:lines ){
//            System.out.println(s);

//            System.out.println(linenumb++);

            String[] curretnLine = s.split(" ");
            //vrchol
            if(s.startsWith("v ")){
//                System.out.println(Float.parseFloat(curretnLine[1]));
                //kdyz nejde pidat jedna u prvniho modelu
                float[] vertex = new float[]{Float.parseFloat(curretnLine[1]),Float.parseFloat(curretnLine[2]),Float.parseFloat(curretnLine[3])};
//                float[] vertex = new float[]{Float.parseFloat(curretnLine[2]),Float.parseFloat(curretnLine[3]),Float.parseFloat(curretnLine[4])};
                vrcholy.add(vertex);
                //terxtura
            }else if(s.startsWith("vt ")){
                float[] texture = new float[]{Float.parseFloat(curretnLine[1]),Float.parseFloat(curretnLine[2])};
                textury.add(texture);
                //jak spojit
            }else if(s.startsWith("f ")){
//                System.out.println(curretnLine);
                String[] vertex1 = curretnLine[1].split("/");
                String[] vertex2 = curretnLine[2].split("/");
                String[] vertex3 = curretnLine[3].split("/");
                String[] vertex4 = curretnLine[4].split("/");
//                System.out.println(Arrays.toString(vertex1));
                //vrchol, textura  ....... 4x pro 4 vrcholy polygon
                ///old prvni verze
//                int[] indice = new int[]{Integer.parseInt(vertex1[0]),Integer.parseInt(vertex1[1]),
//                                        Integer.parseInt(vertex2[0]),Integer.parseInt(vertex2[1]),
//                                        Integer.parseInt(vertex3[0]),Integer.parseInt(vertex3[1]),
//                                        Integer.parseInt(vertex4[0]),Integer.parseInt(vertex4[1])};
                // z quadu na trojuhelniky
                int[] indice1 = new int[]{Integer.parseInt(vertex1[0]),Integer.parseInt(vertex1[1]),
                                        Integer.parseInt(vertex2[0]),Integer.parseInt(vertex2[1]),
                                        Integer.parseInt(vertex3[0]),Integer.parseInt(vertex3[1])};

                int[] indice2 = new int[]{Integer.parseInt(vertex1[0]),Integer.parseInt(vertex1[1]),
                                        Integer.parseInt(vertex3[0]),Integer.parseInt(vertex3[1]),
                                        Integer.parseInt(vertex4[0]),Integer.parseInt(vertex4[1])};

                indices.add(indice1);
                indices.add(indice2);

            }



        }
//        System.out.println(Arrays.toString(vrcholy.get(0)));
//        System.out.println(Arrays.toString(textury.get(0)));
//        System.out.println(Arrays.toString(indices.get(0)));

    }

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
