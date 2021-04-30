package app.maze;

import app.fileReader.FileReader;
import utils.GLCamera;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Třída pro načtení bludiště ze souboru a následnou práci s bludištěm
 */

public class MazeLoader extends FileReader {
    private final float zmenseni;
    private final ArrayList<Box> helpBoxes;
    private int pocetKrychli;
    private int delkaHrany;
    private int[][] rozlozeniBludiste, rozlozeniBludisteBackUp, rozlozeniBludisteNoEnemy;
    private int jednaHrana;
    private int spawnI, spawnJ;
    private int finishI, finishJ;
    private double spawnX, spawnZ;
    private boolean mazeLoadError;
    //Proměnná, kde se nachází hráč
    private int currenI, currenJ;
    //Boxy pro bludiště
    private Box[][] boxes;

    //Konstruktor
    public MazeLoader() {
        helpBoxes = new ArrayList<>();
        //Pro generování jar souboru
        parseFile("src/res/proportions/maze");
//        parseFile(System.getProperty("user.dir") + "/maze");
        if (mazeLoadError)
            loadDefaultMaze();
        createMaze();
        currenI = spawnI;
        currenJ = spawnJ;
        zmenseni = 0.04f;
    }

    //Pří chybě se souborem nahraji základní bludiště
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

    //Funkce pro vytvořeni bludiště
    //Rozložení matice: 0 = cesta, 1 = zeď, 2 = spawn, 3 = cíl, 4 = enemy, 5 = cesta do cíle
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
                //Přidání bloků kolem npc/cesty, když je na kraji mapy
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
        //Přidání bloků kolem spawnu, když je na kraji mapy
        addBoxIfPossible(spawnI, spawnJ + 1);
        addBoxIfPossible(spawnI + 1, spawnJ);
        addBoxIfPossible(spawnI - 1, spawnJ);
        addBoxIfPossible(spawnI, spawnJ - 1);
    }

    //Funkce zjišťující, zda musíme vykresli box, aby hráč nemohl ven z mapy
    private void addBoxIfPossible(int i, int j) {
        try {
            double tmp = boxes[i][j].getxMax();
        } catch (ArrayIndexOutOfBoundsException e) {
            //Mimo – vytvořím nový pomocný box
            Box tmp = new Box(i, j, jednaHrana);
            helpBoxes.add(tmp);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    //Načtení bludiště ze souboru – když nějaká chyba načtu základní bludiště a uživatele upozorním
    @Override
    public void parseFile(String filename) {
        try {
            String data = new String(Files.readAllBytes(Paths.get(String.format("%s.%s", filename, "txt"))));
            String[] lines = data.split("\n");
            String[] velikostString = lines[0].split("!");
            String[] velikostString2 = lines[1].split("!");
            pocetKrychli = Integer.parseInt(velikostString[1]);
            delkaHrany = Integer.parseInt(velikostString2[1]);
            rozlozeniBludiste = new int[pocetKrychli][pocetKrychli];
            boxes = new Box[pocetKrychli][pocetKrychli];
            jednaHrana = delkaHrany / pocetKrychli;
            for (int i = 0; i < pocetKrychli; i++) {
                //Rozděleni řádku na jednotlivé segmenty
                String[] attributes = lines[i + 2].split("!");
                for (int j = 0; j < pocetKrychli; j++) {
                    switch (attributes[j]) {
                        case "c" -> rozlozeniBludiste[i][j] = 0;
                        case "S" -> rozlozeniBludiste[i][j] = 2;
                        case "K" -> rozlozeniBludiste[i][j] = 3;
                        case "E" -> rozlozeniBludiste[i][j] = 4;
                        default -> rozlozeniBludiste[i][j] = 1;
                    }
                    //Vytváření boxů pro bludiště
                    boxes[i][j] = new Box(i, j, jednaHrana);
                }
            }
            //Kontrola správnosti bludiště
            int finishCount = 0;
            int spawnCount = 0;
            int enemyCount = 0;
            for (int i = 0; i < pocetKrychli; i++) {
                for (int j = 0; j < pocetKrychli; j++) {
                    if (rozlozeniBludiste[i][j] == 3)
                        finishCount++;
                    if (rozlozeniBludiste[i][j] == 2)
                        spawnCount++;
                    if (rozlozeniBludiste[i][j] == 4)
                        enemyCount++;
                }
            }
            mazeLoadError = enemyCount != 1 || spawnCount != 1 || finishCount != 1;
        } catch (Exception e) {
            mazeLoadError = true;
        }
    }

    //Funkce pro kolize
    // 0 - jsem v bludišti, 1 - jsem blízko zdi, 2 - jsem v cíli
    //Rozložení matice: 0 = cesta, 1 = zeď, 2 = spawn, 3 = cíl, 4 = enemy, 5 = cesta do cíle
    public int isOutside(GLCamera cam) {
        double camX = cam.getPosition().getX();
        double camY = cam.getPosition().getY();
        double camZ = cam.getPosition().getZ();
        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                if (rozlozeniBludiste[i][j] == 1) {
                    if (boxes[i][j].getxMin() * zmenseni * 0.98 <= camX && camX <= boxes[i][j].getxMax() * zmenseni * 1.02 &&
                            boxes[i][j].getyMin() * zmenseni * 0.98 <= camY && camY <= boxes[i][j].getyMax() * zmenseni * 1.02 &&
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
        for (Box box : helpBoxes) {
            if (box.getxMin() * zmenseni * 0.98 <= camX && camX <= box.getxMax() * zmenseni * 1.02 &&
                    box.getyMin() * zmenseni * 0.98 <= camY && camY <= box.getyMax() * zmenseni * 1.02 &&
                    box.getzMin() * zmenseni * 0.98 <= camZ && camZ <= box.getzMax() * zmenseni * 1.02)
                return 1;
        }
        return 0;
    }

    //Get/Set
    public void setRozlozeniBludiste(int i, int j, int value) {
        this.rozlozeniBludiste[i][j] = value;
    }

    public int getPocetKrychli() {
        return pocetKrychli;
    }

    public int getDelkaHrany() {
        return delkaHrany;
    }

    public int getRozlozeniBludiste(int i, int j) {
        return rozlozeniBludiste[i][j];
    }

    public int getRozlozeniBludisteBackUp(int i, int j) {
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

    public void setCurrenI(int currenI) {
        this.currenI = currenI;
    }

    public int getCurrenJ() {
        return currenJ;
    }

    public void setCurrenJ(int currenJ) {
        this.currenJ = currenJ;
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

}
