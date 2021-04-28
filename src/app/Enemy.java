package app;

import java.util.ArrayList;

public class Enemy {
    private int[] source;
    private ArrayList<int[]> allVisitedEnemy = new ArrayList<>();
    int delkaHrany;

    public Enemy(int delkaHrany) {
        this.delkaHrany = delkaHrany;
    }

    public int[] possibleWaysEnemyGetDestination(int i, int j, int[][] rozlozeniBludisteF) {
        source = new int[]{i, j};
        ArrayList<int[]> possbileWays = new ArrayList<>();
        // 1 do prava,2 do levam, 3 nahoru,4 dolu
        if (j + 1 < delkaHrany && j + 1 >= 0 && isNotInsideEnemyWay(i, j + 1)) {
            if ((rozlozeniBludisteF[i][j + 1] == 0 || rozlozeniBludisteF[i][j + 1] == 5)) {
                int[] tmp = {i, j + 1, 1, rozlozeniBludisteF[i][j + 1]};
                possbileWays.add(tmp);
            }
        }

        if (j - 1 < delkaHrany && j - 1 >= 0 && isNotInsideEnemyWay(i, j - 1)) {
            if (rozlozeniBludisteF[i][j - 1] == 0 || rozlozeniBludisteF[i][j - 1] == 5) {
                int[] tmp = {i, j - 1, 2, rozlozeniBludisteF[i][j - 1]};
                possbileWays.add(tmp);
            }
        }
        if (i + 1 < delkaHrany && i + 1 >= 0 && isNotInsideEnemyWay(i + 1, j)) {
            if (rozlozeniBludisteF[i + 1][j] == 0 || rozlozeniBludisteF[i + 1][j] == 5) {
                int[] tmp = {i + 1, j, 3, rozlozeniBludisteF[i + 1][j]};
                possbileWays.add(tmp);
            }
        }
        if (i - 1 < delkaHrany && i - 1 >= 0 && isNotInsideEnemyWay(i - 1, j)) {
            if (rozlozeniBludisteF[i - 1][j] == 0 || rozlozeniBludisteF[i - 1][j] == 5) {
                int[] tmp = {i - 1, j, 4, rozlozeniBludisteF[i - 1][j]};
                possbileWays.add(tmp);
            }
        }

        if (possbileWays.size() == 0 && allVisitedEnemy.size() != 0) {
            allVisitedEnemy.clear();
            allVisitedEnemy.add(new int[]{i, j, 0, rozlozeniBludisteF[i][j]});
            //TODO optimazilovat dat if do funcki a vratit list
            if (j + 1 < delkaHrany && j + 1 >= 0 && isNotInsideEnemyWay(i, j + 1)) {
                if (rozlozeniBludisteF[i][j + 1] == 0 || rozlozeniBludisteF[i][j + 1] == 5) {
                    int[] tmp = {i, j + 1, 1, rozlozeniBludisteF[i][j + 1]};
                    possbileWays.add(tmp);
                }
            }
            if (j - 1 < delkaHrany && j - 1 >= 0 && isNotInsideEnemyWay(i, j - 1)) {
                if (rozlozeniBludisteF[i][j - 1] == 0 || rozlozeniBludisteF[i][j - 1] == 5) {
                    int[] tmp = {i, j - 1, 2, rozlozeniBludisteF[i][j]};
                    possbileWays.add(tmp);
                }
            }
            if (i + 1 < delkaHrany && i + 1 >= 0 && isNotInsideEnemyWay(i + 1, j)) {
                if (rozlozeniBludisteF[i + 1][j] == 0 || rozlozeniBludisteF[i + 1][j] == 5) {
                    int[] tmp = {i + 1, j, 3, rozlozeniBludisteF[i + 1][j]};
                    possbileWays.add(tmp);
                }
            }
            if (i - 1 < delkaHrany && i - 1 >= 0 && isNotInsideEnemyWay(i - 1, j)) {
                if (rozlozeniBludisteF[i - 1][j] == 0 || rozlozeniBludisteF[i - 1][j] == 5) {
                    int[] tmp = {i - 1, j, 4, rozlozeniBludisteF[i - 1][j]};
                    possbileWays.add(tmp);
                }
            }

        }

        if (possbileWays.size() == 0)
            possbileWays.add(new int[]{i, j, 0, rozlozeniBludisteF[i][j]});

        int randomWay = (int) (Math.random() * possbileWays.size());

//        System.out.println(allVisitedEnemy.toString());
        allVisitedEnemy.add(possbileWays.get(randomWay));
        return possbileWays.get(randomWay);

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
}
