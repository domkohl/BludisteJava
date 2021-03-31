package app;

public class test {
    public static void main(String[] args) {
        int pocetKrychli =  5;
        int delkaHrany = 100;
        int jednaHrana = delkaHrany/pocetKrychli;
        int[][] rozlozeniBludiste = new int[pocetKrychli][pocetKrychli];

        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                rozlozeniBludiste[i][j] = 1;
            }
        }

        rozlozeniBludiste[3][0] = 0;
        rozlozeniBludiste[3][1] = 0;
        rozlozeniBludiste[3][2] = 0;
        rozlozeniBludiste[2][2] = 0;
        rozlozeniBludiste[2][3] = 0;
        rozlozeniBludiste[1][3] = 0;
        rozlozeniBludiste[1][4] = 0;


        Box[][] boxes = new Box[pocetKrychli][pocetKrychli];

        for (int i = 0; i < pocetKrychli; i++) {
            for (int j = 0; j < pocetKrychli; j++) {
                boxes[i][j] = new Box(i,j,jednaHrana);
            }
        }

        System.out.println("test");

    }
}
