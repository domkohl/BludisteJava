package app.npc;

import java.util.ArrayList;
import java.util.Arrays;

public class Enemy {
    private int[] source;
    private final ArrayList<int[]> allVisitedEnemy;
    int delkaHrany;
    private int enemyPosI,enemyPosJ;

    private final int[] currentDestinationBlock;

    private ArrayList<int[]> possbileWays;

    public Enemy(int delkaHrany) {
        currentDestinationBlock = new int[3];
        allVisitedEnemy = new ArrayList<>();
        this.delkaHrany = delkaHrany;
        possbileWays = new ArrayList<>();
    }

    public void possibleWaysEnemyGetDestination(int i, int j, int[][] rozlozeniBludisteF) {
        source = new int[]{i, j};
        possbileWays = new ArrayList<>();
        // 1 do prava,2 do levam, 3 nahoru,4 dolu

        helpAddPossibleWay(i,j+1,1,rozlozeniBludisteF);

        helpAddPossibleWay(i,j-1,2,rozlozeniBludisteF);

        helpAddPossibleWay(i+1,j,3,rozlozeniBludisteF);

        helpAddPossibleWay(i-1,j,4,rozlozeniBludisteF);

        //kdyz uz nemuze npc nikam jit jede znovu
        if (possbileWays.size() == 0 && allVisitedEnemy.size() != 0) {
            allVisitedEnemy.clear();
            allVisitedEnemy.add(new int[]{i, j, 0, rozlozeniBludisteF[i][j]});

            helpAddPossibleWay(i,j+1,1,rozlozeniBludisteF);

            helpAddPossibleWay(i,j-1,2,rozlozeniBludisteF);

            helpAddPossibleWay(i+1,j,3,rozlozeniBludisteF);

            helpAddPossibleWay(i-1,j,4,rozlozeniBludisteF);

        }

        if (possbileWays.size() == 0)
            possbileWays.add(new int[]{i, j, 0, rozlozeniBludisteF[i][j]});

        int randomWay = (int) (Math.random() * possbileWays.size());

        allVisitedEnemy.add(possbileWays.get(randomWay));
        setCurrentDestinationBlock(possbileWays.get(randomWay)[0],possbileWays.get(randomWay)[1],possbileWays.get(randomWay)[2]);

    }

    private void helpAddPossibleWay(int i, int j, int direction, int[][] rozlozeniBludiste) {
        //TODo osetri mimo blok vyber na kraji zamezit m,inus hodnoty a davat jen ty co muzu
        if (j < delkaHrany && j >= 0 && isNotInsideEnemyWay(i, j)) {
            if (rozlozeniBludiste[i][j] == 0 || rozlozeniBludiste[i][j] == 5) {
                int[] tmp = {i, j, direction, rozlozeniBludiste[i][j]};
                possbileWays.add(tmp);
            }
        }
    }


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

    public int[] getSource() {
        return source;
    }

    public ArrayList<int[]> getAllVisitedEnemy() {
        return allVisitedEnemy;
    }

    public int getDelkaHrany() {
        return delkaHrany;
    }

    public int getEnemyPosI() {
        return enemyPosI;
    }

    public int[] getCurrentDestinationBlock() {
        return currentDestinationBlock;
    }

    public int getEnemyPosJ() {
        return enemyPosJ;
    }

    public void setEnemyPosI(int enemyPosI) {
        this.enemyPosI = enemyPosI;
    }

    public void setEnemyPosJ(int enemyPosJ) {
        this.enemyPosJ = enemyPosJ;
    }

    public void setCurrentDestinationBlock(int i ,int j , int valueBeforeEnter) {
        this.currentDestinationBlock[0] = i;
        this.currentDestinationBlock[1] = j;
        this.currentDestinationBlock[2] = valueBeforeEnter;
    }
}
