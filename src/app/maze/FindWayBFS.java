package app.maze;

import java.util.LinkedList;

/**
 * Třída pro hledání nejkratší cesty z bludiště
 * Využívá algoritmus Prohledávání do šířky (BFS):
 * Algoritmus postupně prochází všechny sousedy startovací buňky a následně jejich sousedy atd.
 * Než projde všechny sousedy, do kterých může.
 * Algoritmus se šíří jako taková vlna a pamatuje si u buněk jak se tam dostat(rodiče) a cenu cesty.
 */

public class FindWayBFS {

    //Pomocná funkce pro návštěvu sousedního bloku
    static void visit(Cell[][] cells, LinkedList<Cell> queue, int x, int y, Cell parent) {
        //Zjišťuji, zda vůbec blok existuje
        if (x < 0 || x >= cells.length || y < 0 || y >= cells[0].length || cells[x][y] == null) {
            return;
        }
        //Vložím současný/navštěvovaný blok do fronty s o 1 větší vzdálenosti
        //a přidám rodiče (odkud jsem přisel) – pokud je nová cesta (vzdálenost) kratší
        int dist = parent.dist + 1;
        Cell p = cells[x][y];
        if (dist < p.dist) {
            p.dist = dist;
            p.prev = parent;
            queue.add(p);
        }
    }

    //Funkce, která vrací cestu do cíle, když cesta neexistuje vrací null hodnotu
    public int[][] shortestPath(int[][] matrix, int[] start, int[] end) {
        //Nastavíme start a cíl do proměnné
        int sx = start[0], sy = start[1];
        int dx = end[0], dy = end[1];

        //Počáteční blok nesmí byt zeď nebo nesmí byt cíl
        if (matrix[sx][sy] == 1 || matrix[dx][dy] != 3)
            return null;

        //Pomocná síť(matice) pro záznam kde jsme byli
        int m = matrix.length;
        int n = matrix[0].length;
        Cell[][] cells = new Cell[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                //Inicializuji matici, když tam mužů jít (všude kde není zeď) tak nastavím
                if (matrix[i][j] != 1) {
                    cells[i][j] = new Cell(i, j, Integer.MAX_VALUE, null);
                }
            }
        }

        //Fronta – FIFO – na první místě buňka, které prohledávám sousedy
        LinkedList<Cell> queue = new LinkedList<>();

        //Přidám do fronty startovací buňku
        Cell src = cells[sx][sy];
        src.dist = 0;
        queue.add(src);

        //Proměnná pro určení, zda jsem v cíli
        Cell dest = null;

        //Současný blok, kterému prohledávám sousedy
        Cell p;

        //Prohledáváme frontu, dokud není prázdna
        // + odebrání z fronty a přidáno do proměnné k prohledání
        while ((p = queue.poll()) != null) {

            //Když současný blok je cíl mám hotovo
            if (p.x == dx && p.y == dy) {
                dest = p;
                break;
            }

            //Současný blok není cil tak prohledám možnou cestu okolo:
            //Pohyb nahoru, dolů, vlevo a vpravo
            visit(cells, queue, p.x - 1, p.y, p);
            visit(cells, queue, p.x + 1, p.y, p);
            visit(cells, queue, p.x, p.y - 1, p);
            visit(cells, queue, p.x, p.y + 1, p);
        }

        //Po průchodu všech cest, nebo nalezení cíle:
        //Když jsem nenašel cíl vracím null –> k cíli nejde dojít
        if (dest == null) {
            return null;
        } else {
            //Cíl byl nalezen:
            //Přidávám cestu od konce, dokud nejsme na startu, kde je rodič null
            LinkedList<Cell> path = new LinkedList<>();
            p = dest;
            do {
                path.addFirst(p);
            } while ((p = p.prev) != null);

            //Odstraním start a cíl – pro lepší zpracování v bludišti
            path.removeFirst();
            path.removeLast();

            //Vracím pomocné pole s původním rozložením + cesta
            int m2 = matrix.length;
            int n2 = matrix[0].length;
            int[][] tmp = new int[m2][n2];
            for (int i = 0; i < m2; i++) {
                System.arraycopy(matrix[i], 0, tmp[i], 0, n2);
            }
            //Nahraní cesty do rozložení bludiště
            for (Cell c : path) {
                tmp[c.x][c.y] = 5;
            }
            return tmp;
        }
    }

    //Pomocná třida pro každý blok v matici
    private static class Cell {
        int x;
        int y;
        //Vzdálenost od začátku (počet bloků)
        int dist;
        //Rodičovsky – minulý blok
        Cell prev;

        //Konstruktor
        Cell(int x, int y, int dist, Cell prev) {
            this.x = x;
            this.y = y;
            this.dist = dist;
            this.prev = prev;
        }

    }

}
