package app.npc;

import java.util.ArrayList;

/**
 * Třída práci s NPC
 */

public class Enemy {
    private final ArrayList<int[]> allVisitedEnemy;
    private final int[] currentDestinationBlock;
    int pocetKrychli;
    private int[] source;
    private int enemyPosI, enemyPosJ;
    private ArrayList<int[]> possbileWays;

    //Konstruktor
    public Enemy(int pocetKrychli) {
        //Pomocné pole uchovávající hodnoty: 1. i, 2. j a 3. směr odkud jdu
        currentDestinationBlock = new int[3];
        allVisitedEnemy = new ArrayList<>();
        this.pocetKrychli = pocetKrychli;
        possbileWays = new ArrayList<>();
    }

    //Funkce přidělení další pozice pro pohyb npc
    public void possibleWaysEnemyGetDestination(int i, int j, int[][] rozlozeniBludiste) {
        source = new int[]{i, j};
        possbileWays = new ArrayList<>();

        //Směr: 1 = vpravo, 2 = vlevo, 3 = nahoru, 4 = dolů
        helpAddPossibleWay(i, j + 1, 1, rozlozeniBludiste);
        helpAddPossibleWay(i, j - 1, 2, rozlozeniBludiste);
        helpAddPossibleWay(i + 1, j, 3, rozlozeniBludiste);
        helpAddPossibleWay(i - 1, j, 4, rozlozeniBludiste);

        //Když už nemůže npc nikam jít jede znovu (+ vymažu cesty kde jsem byl)
        if (possbileWays.size() == 0 && allVisitedEnemy.size() != 0) {
            allVisitedEnemy.clear();
            allVisitedEnemy.add(new int[]{i, j, 0, rozlozeniBludiste[i][j]});

            helpAddPossibleWay(i, j + 1, 1, rozlozeniBludiste);
            helpAddPossibleWay(i, j - 1, 2, rozlozeniBludiste);
            helpAddPossibleWay(i + 1, j, 3, rozlozeniBludiste);
            helpAddPossibleWay(i - 1, j, 4, rozlozeniBludiste);

        }
        //NPC nemá kam jít
        if (possbileWays.size() == 0)
            possbileWays.add(new int[]{i, j, 0, rozlozeniBludiste[i][j]});

        //Náhodné vybrání cesty, přidání do navštívených a nastavení pro pohyb
        int randomWay = (int) (Math.random() * possbileWays.size());
        allVisitedEnemy.add(possbileWays.get(randomWay));
        setCurrentDestinationBlock(possbileWays.get(randomWay)[0], possbileWays.get(randomWay)[1], possbileWays.get(randomWay)[2]);
    }

    //Pomocná funkce pro zjištění, zda se npc může pohnout tím to směrem
    private void helpAddPossibleWay(int i, int j, int direction, int[][] rozlozeniBludiste) {
        if (j < pocetKrychli && j >= 0 && i < pocetKrychli && i >= 0 && isNotInsideEnemyWay(i, j)) {
            //Npc se může hýbat jen po cestách (0)
            if (rozlozeniBludiste[i][j] == 0) {
                int[] tmp = {i, j, direction, rozlozeniBludiste[i][j]};
                possbileWays.add(tmp);
            }
        }
    }

    //Pomocná funkce pro zjištění, zda se jsem už na tomto bloku nebyl
    private boolean isNotInsideEnemyWay(int i, int j) {
        if (allVisitedEnemy.size() <= 0) {
            return true;
        } else {
            for (int[] blok : allVisitedEnemy) {
                if (blok[0] == i && blok[1] == j)
                    return false;
            }
        }
        return true;
    }

    //Get/Set
    public int[] getSource() {
        return source;
    }

    public ArrayList<int[]> getAllVisitedEnemy() {
        return allVisitedEnemy;
    }

    public int getEnemyPosI() {
        return enemyPosI;
    }

    public void setEnemyPosI(int enemyPosI) {
        this.enemyPosI = enemyPosI;
    }

    public int[] getCurrentDestinationBlock() {
        return currentDestinationBlock;
    }

    public int getEnemyPosJ() {
        return enemyPosJ;
    }

    public void setEnemyPosJ(int enemyPosJ) {
        this.enemyPosJ = enemyPosJ;
    }

    public void setCurrentDestinationBlock(int i, int j, int direction) {
        this.currentDestinationBlock[0] = i;
        this.currentDestinationBlock[1] = j;
        this.currentDestinationBlock[2] = direction;
    }
}
