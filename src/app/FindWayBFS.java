package app;

import java.util.Arrays;
import java.util.LinkedList;

public class FindWayBFS {

    //Pomocná trida pro kazdy blok
    private static class Cell  {
        int x;
        int y;
        int dist;  	//vzdalenost od zacatku
        Cell prev;  //  rodicovsky minuly blok

        Cell(int x, int y, int dist, Cell prev) {
            this.x = x;
            this.y = y;
            this.dist = dist;
            this.prev = prev;
        }

        @Override
        public String toString(){
            return "(" + x + "," + y + ")";
        }
    }

    //BFS
    public int[][] shortestPath(int[][] matrix, int[] start, int[] end) {
        //Nastavim start a cil do promenyc
        int sx = start[0], sy = start[1];
        int dx = end[0], dy = end[1];

        //pocatecni blok nesmi byt zed nebo  end nesmi byt cil
        if (matrix[sx][sy] == 1 || matrix[dx][dy] != 3)
            return null;

        // pomocny sit pro zaznam kde jsme byli
        int m = matrix.length;
        int n = matrix[0].length;
        Cell[][] cells = new Cell[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                //inicializuji sit kdyz tam muzu jit tka nastavim
                if (matrix[i][j] != 1) {
                    cells[i][j] = new Cell(i, j, Integer.MAX_VALUE, null);
                }
            }
        }

        //fronta
        LinkedList<Cell> queue = new LinkedList<>();
        // pridam do fronty start
        Cell src = cells[sx][sy];
        src.dist = 0;
        queue.add(src);
        //promena pro urceni zda jsem v cili
        Cell dest = null;
        //soucazny blok kde jsem ted
        Cell p;
        // PRohledavam frontu dokud neni prazdna
        while ((p = queue.poll()) != null) {
            //kdyz soucasny blok se cil mam hotovo
            if (p.x == dx && p.y == dy) {
                dest = p;
                break;
            }
            //Souccasny blok neni cil tak prohledam moznou cestu okolo:
            // pohyb nahoru
            visit(cells, queue, p.x - 1, p.y, p);

            // pohyb dolu
            visit(cells, queue, p.x + 1, p.y, p);

            // pohyb vlelo
            visit(cells, queue, p.x, p.y - 1, p);

            //pohyb vpravo
            visit(cells, queue, p.x, p.y + 1, p);
        }

        //kdyz nejsem v cili vracim null k cili nejde dojit
        if (dest == null) {
            return null;
        } else {
            // ansli jsme cil
            // pridamvma cestu od zdanu dokud nejsme na startu kde je rodic null
            LinkedList<Cell> path = new LinkedList<>();
            p = dest;
            do {
                path.addFirst(p);
            } while ((p = p.prev) != null);
//            System.out.println(path);
            path.removeFirst();
            path.removeLast();
            //vrati pole ne linked list

            int m2 = matrix.length;
            int n2 = matrix[0].length;
            int[][] tmp = new int[m2][n2];
            for (int i = 0; i < m2; i++) {
                for (int j = 0; j < n2; j++) {
                    tmp[i][j] = matrix[i][j];
                }
            }
//            int[][] tmp = matrix;
            for (Cell c:path) {
                tmp[c.x][c.y] = 5;
//          }
//                System.out.println(Arrays.deepToString(matrix));
//                System.out.println(Arrays.deepToString(tmp));


        }return tmp;
    }
    }

    //nastaveni navtivenych bloku
    static void visit(Cell[][] cells, LinkedList<Cell> queue, int x, int y, Cell parent) {
        //zjistiuji zda vubec blok existuje
        if (x < 0 || x >= cells.length || y < 0 || y >= cells[0].length || cells[x][y] == null) {
            return;
        }

        //vlozim soucasny blok do do fronty s o 1 vesti vzdalsenosti
        // a prdaime rodice(od kud jsme prisel)
        int dist = parent.dist + 1;
        Cell p = cells[x][y];
        if (dist < p.dist) {
            p.dist = dist;
            p.prev = parent;
            queue.add(p);
        }
    }

//    public static void main(String[] args) {
//        int[][] matrix = {
//                {2, 0, 0, 0, 0},
//                {0, 1, 4, 1, 0},
//                {0, 0, 0, 1, 0},
//                {1, 1, 0, 1, 0},
//                {1, 1, 0, 3, 0}
//        };
//        int[] start = {0, 2};
//        int[] end = {4, 3};
//        LinkedList<Cell> shortestWay = shortestPath(matrix, start, end);
//        System.out.println(shortestPath(matrix, start, end));
//        System.out.println(Arrays.deepToString(matrix));
//        assert shortestWay != null;
//        for (Cell c:shortestWay) {
//            matrix[c.x][c.y] = 5;
//        }
//        System.out.println(Arrays.deepToString(matrix));
//
//    }
}
