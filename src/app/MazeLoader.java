package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MazeLoader {
        private int pocetKrychli;
        private int delkaHrany;
        private int[][] rozlozeniBludiste,rozlozeniBludisteBackUp,rozlozeniBludisteNoEnemy;
        Box[][] boxes;
        //TODo mzenit na double/float vsude ?
        private int jednaHrana;
        private int spawnI,spawnJ;
        private double spawnX,spawnZ;
        private ArrayList<Box> helpBoxes;

    public MazeLoader() {
        helpBoxes = new ArrayList<>();
        createMaze();
    }

    //Funkce vytvoreni bludiste
    private void createMaze() {

        parseMazeFromTxt("src/res/proportions/maze");
        rozlozeniBludisteBackUp = new int[pocetKrychli][pocetKrychli];
        rozlozeniBludisteNoEnemy = new int[pocetKrychli][pocetKrychli];
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 4)
                    rozlozeniBludisteNoEnemy[i][j] = 0;
                else
                    rozlozeniBludisteNoEnemy[i][j] = rozlozeniBludiste[i][j];

                rozlozeniBludisteBackUp[i][j] = rozlozeniBludiste[i][j];
                if (rozlozeniBludiste[i][j] == 2) {
                    spawnI = i;
                    spawnJ = j;
                    spawnX = (boxes[i][j].getbH().getX() +
                            boxes[i][j].getB2().getX() +
                            boxes[i][j].getB3().getX() +
                            boxes[i][j].getB4().getX() +
                            boxes[i][j].getbUp4().getX() +
                            boxes[i][j].getbUp3().getX() +
                            boxes[i][j].getbUp2().getX() +
                            boxes[i][j].getbUp1().getX()
                    ) / 8;

                    spawnZ = (boxes[i][j].getbH().getZ() +
                            boxes[i][j].getB2().getZ() +
                            boxes[i][j].getB3().getZ() +
                            boxes[i][j].getB4().getZ() +
                            boxes[i][j].getbUp4().getZ() +
                            boxes[i][j].getbUp3().getZ() +
                            boxes[i][j].getbUp2().getZ() +
                            boxes[i][j].getbUp1().getZ()
                    ) / 8;
                }

                if (rozlozeniBludiste[i][j] == 0) {
                    addBoxIfPossible(i, j + 1);
                    addBoxIfPossible(i + 1, j);
                    addBoxIfPossible(i - 1, j);
                    addBoxIfPossible(i, j - 1);
                }
            }
        }
        addBoxIfPossible(spawnI, spawnJ + 1);
        addBoxIfPossible(spawnI + 1, spawnJ);
        addBoxIfPossible(spawnI - 1, spawnJ);
        addBoxIfPossible(spawnI, spawnJ - 1);

    }

    //Funkce zjistujici zda musime vykresli box, aby hrac nemohl ven z mapy
    private void addBoxIfPossible(int i, int j) {
        try {
            double tmp = boxes[i][j].getxMax();
        } catch (ArrayIndexOutOfBoundsException e) {
            //mimo-vytvorim novy box
            Box tmp = new Box(i, j, jednaHrana);
            helpBoxes.add(tmp);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }






    //Pomocna funkce pro cteni ze souboru
    public String readFromFile(String filename, String extension) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, extension))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Nacteni bludiste ze souboru
    public void parseMazeFromTxt(String filename) {
        String data = readFromFile(filename, "txt");
        String[] lines = data.split("\n");
        String[] velikostString = lines[0].split("!");
        String[] velikostString2 = lines[1].split("!");
        pocetKrychli = Integer.parseInt(velikostString[1]);
        delkaHrany = Integer.parseInt(velikostString2[1]);
        rozlozeniBludiste = new int[pocetKrychli][pocetKrychli];
        boxes = new Box[pocetKrychli][pocetKrychli];
        jednaHrana = delkaHrany / pocetKrychli;

        for (int i = 0; i < pocetKrychli; i++) {
            // rozdeleni radku na jednotlive segmenty
            String[] attributes = lines[i + 2].split(" ! ");
            for (int j = 0; j < pocetKrychli; j++) {
                switch (attributes[j]) {
                    case "c" -> rozlozeniBludiste[i][j] = 0;
                    case "S" -> rozlozeniBludiste[i][j] = 2;
                    case "K" -> rozlozeniBludiste[i][j] = 3;
                    case "E" -> rozlozeniBludiste[i][j] = 4;
                    default -> rozlozeniBludiste[i][j] = 1;
                }
                boxes[i][j] = new Box(i, j, jednaHrana);
            }
        }
    }

    public void setPocetKrychli(int pocetKrychli) {
        this.pocetKrychli = pocetKrychli;
    }

    public void setDelkaHrany(int delkaHrany) {
        this.delkaHrany = delkaHrany;
    }

    public void setRozlozeniBludiste(int[][] rozlozeniBludiste) {
        this.rozlozeniBludiste = rozlozeniBludiste;
    }

    public void setRozlozeniBludisteBackUp(int[][] rozlozeniBludisteBackUp) {
        this.rozlozeniBludisteBackUp = rozlozeniBludisteBackUp;
    }

    public void setRozlozeniBludisteNoEnemy(int[][] rozlozeniBludisteNoEnemy) {
        this.rozlozeniBludisteNoEnemy = rozlozeniBludisteNoEnemy;
    }

    public void setBoxes(Box[][] boxes) {
        this.boxes = boxes;
    }

    public void setJednaHrana(int jednaHrana) {
        this.jednaHrana = jednaHrana;
    }

    public void setSpawnI(int spawnI) {
        this.spawnI = spawnI;
    }

    public void setSpawnJ(int spawnJ) {
        this.spawnJ = spawnJ;
    }

    public void setSpawnX(double spawnX) {
        this.spawnX = spawnX;
    }

    public void setSpawnZ(double spawnZ) {
        this.spawnZ = spawnZ;
    }

    public void setHelpBoxes(ArrayList<Box> helpBoxes) {
        this.helpBoxes = helpBoxes;
    }

    public int getPocetKrychli() {
        return pocetKrychli;
    }

    public int getDelkaHrany() {
        return delkaHrany;
    }

    public int getRozlozeniBludiste(int i ,int j) {
        return rozlozeniBludiste[i][j];
    }

    public int[][] getRozlozeniBludisteBackUp() {
        return rozlozeniBludisteBackUp;
    }

    public int[][] getRozlozeniBludisteNoEnemy() {
        return rozlozeniBludisteNoEnemy;
    }

    public Box[][] getBoxes() {
        return boxes;
    }

    public int getJednaHrana() {
        return jednaHrana;
    }

    public int getSpawnI() {
        return spawnI;
    }

    public int getSpawnJ() {
        return spawnJ;
    }

    public double getSpawnX() {
        return spawnX;
    }

    public double getSpawnZ() {
        return spawnZ;
    }

    public ArrayList<Box> getHelpBoxes() {
        return helpBoxes;
    }

    public int[][] getRozlozeniBludiste() {
        return rozlozeniBludiste;
    }
}
