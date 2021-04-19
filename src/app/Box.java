package app;

import transforms.Point3D;

/**
 * Třída pro vytvoření boxu pro zed/podlahu
 */

public class Box {

    private final Point3D bH, b2, b3, b4, bUp1, bUp2, bUp3, bUp4;
    private final double xMin, xMax, yMin, yMax, zMin, zMax;

    public Box(int x, int z, int jednaHrana) {
        //vypocet bodu Boxu
        this.bH = new Point3D(jednaHrana + x * jednaHrana, 0, jednaHrana + z * jednaHrana);
        this.b2 = new Point3D(bH.getX(), 0, bH.getZ() - jednaHrana);
        this.b3 = new Point3D(bH.getX() - jednaHrana, 0, bH.getZ() - jednaHrana);
        this.b4 = new Point3D(bH.getX() - jednaHrana, 0, bH.getZ());

        this.bUp1 = new Point3D(jednaHrana + x * jednaHrana, 10f, jednaHrana + z * jednaHrana);
        this.bUp2 = new Point3D(bH.getX(), 10f, bH.getZ() - jednaHrana);
        this.bUp3 = new Point3D(bH.getX() - jednaHrana, 10f, bH.getZ() - jednaHrana);
        this.bUp4 = new Point3D(bH.getX() - jednaHrana, 10f, bH.getZ());

        //zjisteni max/min hodnot pro kolize
        double[] allX = {bH.getX(), b2.getX(), b3.getX(), b4.getX(), bUp1.getX(), bUp2.getX(), bUp3.getX(), bUp4.getX()};
        double[] allY = {bH.getY(), b2.getY(), b3.getY(), b4.getY(), bUp1.getY(), bUp2.getY(), bUp3.getY(), bUp4.getY()};
        double[] allZ = {bH.getZ(), b2.getZ(), b3.getZ(), b4.getZ(), bUp1.getZ(), bUp2.getZ(), bUp3.getZ(), bUp4.getZ()};

        this.xMin = getMin(allX);
        this.xMax = getMax(allX);
        this.yMin = getMin(allY);
        this.yMax = getMax(allY);
        this.zMin = getMin(allZ);
        this.zMax = getMax(allZ);
    }

    //Get
    private double getMin(double[] pole) {
        double min = pole[0];
        for (int i = 1; i < 8; i++)
            min = Math.min(min, pole[i]);
        return min;
    }

    private double getMax(double[] pole) {
        double max = pole[0];
        for (int i = 1; i < 8; i++)
            max = Math.max(max, pole[i]);
        return max;
    }

    public double getxMin() {
        return xMin;
    }

    public double getxMax() {
        return xMax;
    }

    public double getyMin() {
        return yMin;
    }

    public double getyMax() {
        return yMax;
    }

    public double getzMin() {
        return zMin;
    }

    public double getzMax() {
        return zMax;
    }

    public Point3D getbH() {
        return bH;
    }

    public Point3D getB2() {
        return b2;
    }

    public Point3D getB3() {
        return b3;
    }

    public Point3D getB4() {
        return b4;
    }

    public Point3D getbUp1() {
        return bUp1;
    }

    public Point3D getbUp2() {
        return bUp2;
    }

    public Point3D getbUp3() {
        return bUp3;
    }

    public Point3D getbUp4() {
        return bUp4;
    }
}

