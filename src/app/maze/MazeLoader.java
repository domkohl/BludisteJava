package app.maze;

import app.fileReader.FileReader;
import utils.GLCamera;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MazeLoader extends FileReader {
        private int pocetKrychli;
        private int delkaHrany;
        private int[][] rozlozeniBludiste,rozlozeniBludisteBackUp,rozlozeniBludisteNoEnemy;
        private Box[][] boxes;
        private int jednaHrana;
        private int spawnI,spawnJ;
        private int finishI,finishJ;
        private int currenI,currenJ;
        private double spawnX,spawnZ;
        private ArrayList<Box> helpBoxes;
        private float zmenseni;
        private boolean mazeLoadError;
        private String mazeLoadErrorMessage;

    public MazeLoader() {
        helpBoxes = new ArrayList<>();
//        parseFile("src/res/proportions/maze");
        parseFile(System.getProperty("user.dir")+"/maze");
        if(mazeLoadError)
            loadDefaultMaze();
        createMaze();
        currenI = spawnI;
        currenJ = spawnJ;
        zmenseni = 0.04f;
    }

    private void loadDefaultMaze() {
        rozlozeniBludiste = new int[][]{
                {1, 0, 1, 1, 2, 1, 1, 0, 1, 1},
                {1, 0, 0, 0, 0, 4, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 0, 1, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 0, 1},
                {0, 1, 0, 0, 1, 0, 0, 0, 0, 1},
                {0, 1, 0, 0, 0, 1, 1, 1, 0, 1},
                {0, 0, 0, 1, 1, 1, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 1},
                {1, 0, 1, 1, 1, 1, 3, 1, 1, 1}
        };
        pocetKrychli = 10;
        delkaHrany = 200;
        boxes = new Box[10][10];
        jednaHrana = delkaHrany / pocetKrychli;
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                boxes[i][j] = new Box(i, j, jednaHrana);
            }
        }

    }

    //Funkce vytvoreni bludiste
    private void createMaze() {
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
                //pradni bloku kolem npc/cesty kdyz je na kraji mapy
                if (rozlozeniBludiste[i][j] == 0 || rozlozeniBludiste[i][j] == 4) {
                    addBoxIfPossible(i, j + 1);
                    addBoxIfPossible(i + 1, j);
                    addBoxIfPossible(i - 1, j);
                    addBoxIfPossible(i, j - 1);
                }
                if (rozlozeniBludiste[i][j] == 3) {
                    finishI = i;
                    finishJ = j;
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

    //Nacteni bludiste ze souboru
    @Override
    public void parseFile(String filename) {
        try{
//            String data = readFromFile(filename, "txt");
            String data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, "txt"))));;
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
                String[] attributes = lines[i + 2].split("!");
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

            int  finishCount = 0;
            int  spawnCount = 0;
            int  enemyCount = 0;
            for (int i = 0; i < pocetKrychli; i++) {
                for (int j = 0; j < pocetKrychli; j++) {
                    if(rozlozeniBludiste[i][j] == 3)
                        finishCount++;
                    if(rozlozeniBludiste[i][j] == 2)
                        spawnCount++;
                    if(rozlozeniBludiste[i][j] == 4)
                        enemyCount++;
                }
            }

            mazeLoadError = enemyCount != 1 || spawnCount != 1 || finishCount < 1;

//            System.out.println(mazeLoadError);
        }catch(Exception e) {
            mazeLoadError = true;
//            e.printStackTrace();
        }

    }

    //Funkce pro kolize
    // 0-jsem v bludisti, 1 - jsem blizko zdi, 2 - jsem v cili
    public int isOutside(GLCamera cam) {
        double camX = cam.getPosition().getX();
        double camY = cam.getPosition().getY();
        double camZ = cam.getPosition().getZ();
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 1) {
                    if (boxes[i][j].getxMin() * zmenseni * 0.98<= camX && camX <= boxes[i][j].getxMax() * zmenseni * 1.02 &&
                            boxes[i][j].getyMin() * zmenseni * 0.98  <= camY && camY <= boxes[i][j].getyMax() * zmenseni * 1.02 &&
                            boxes[i][j].getzMin() * zmenseni * 0.98 <= camZ && camZ <= boxes[i][j].getzMax() * zmenseni * 1.02)
                        return 1;
                }
                if (rozlozeniBludiste[i][j] == 3) {
                    if (boxes[i][j].getxMin() * zmenseni * 0.98 <= camX && camX <= boxes[i][j].getxMax() * zmenseni * 1.02 &&
                            boxes[i][j].getyMin() * zmenseni * 0.98 <= camY && camY <= boxes[i][j].getyMax() * zmenseni * 1.02 &&
                            boxes[i][j].getzMin() * zmenseni * 0.98 <= camZ && camZ <= boxes[i][j].getzMax() * zmenseni * 1.02)
                        return 2;
                }
                if (rozlozeniBludiste[i][j] == 0 || rozlozeniBludiste[i][j] == 5 || rozlozeniBludiste[i][j] == 2 || rozlozeniBludiste[i][j] == 4) {
                    if (boxes[i][j].getxMin() * zmenseni <= camX && camX <= boxes[i][j].getxMax() * zmenseni &&
                            boxes[i][j].getyMin() * zmenseni <= camY && camY <= boxes[i][j].getyMax() * zmenseni &&
                            boxes[i][j].getzMin() * zmenseni <= camZ && camZ <= boxes[i][j].getzMax() * zmenseni) {
                        currenI = i;
                        currenJ = j;
                    }
                }
            }
        }
//        System.out.println(helpBoxes.toString());
        for (Box box : helpBoxes) {
            // TODo konmtrola zda funfiji kolize s help a i vsevhny
//            System.out.println(box.getxMin());
            if (box.getxMin() * zmenseni  * 0.98 <= camX && camX <= box.getxMax() * zmenseni  * 1.02 &&
                    box.getyMin() * zmenseni * 0.98  <= camY && camY <= box.getyMax() * zmenseni * 1.02 &&
                    box.getzMin() * zmenseni * 0.98 <= camZ && camZ <= box.getzMax() * zmenseni* 1.02 )
                return 1;

        }
        return 0;
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

    public void setRozlozeniBludiste(int i, int j, int value) {
        this.rozlozeniBludiste[i][j] = value;
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

    public void setCurrenI(int currenI) {
        this.currenI = currenI;
    }

    public void setCurrenJ(int currenJ) {
        this.currenJ = currenJ;
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

    public int getRozlozeniBludisteBackUp(int i,int j) {
        return rozlozeniBludisteBackUp[i][j];
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

    public int getCurrenI() {
        return currenI;
    }

    public int getCurrenJ() {
        return currenJ;
    }

    public int getFinishI() {
        return finishI;
    }

    public int getFinishJ() {
        return finishJ;
    }

    public float getZmenseni() {
        return zmenseni;
    }

    public boolean isMazeLoadError() {
        return mazeLoadError;
    }

    public String getMazeLoadErrorMessage() {
        return mazeLoadErrorMessage;
    }
}
