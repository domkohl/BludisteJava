package app;

import org.lwjgl.system.CallbackI;
import transforms.Point3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Box {
    private int x,y;
    private Point3D bH,b2,b3,b4,bUp1,bUp2,bUp3,bUp4;
    private int jednaHrana;

    private double xMin,xMax,yMin,yMax,zMin,zMax;


    public Box(int x, int y , int jednaHrana) {
        this.x = x;
        this.y = y;
        this.jednaHrana = jednaHrana;

        this.bH = new Point3D(jednaHrana + x * jednaHrana,jednaHrana + y * jednaHrana,0);
        this.b2 = new Point3D(bH.getX(),bH.getY() - jednaHrana,0);
        this.b3 = new Point3D(bH.getX() - jednaHrana,bH.getY() - jednaHrana,0);
        this.b4 = new Point3D(bH.getX() - jednaHrana,bH.getY(),0);

        this.bUp1 = new Point3D(jednaHrana + x * jednaHrana,jednaHrana + y * jednaHrana,10f);
        this.bUp2 = new Point3D(bH.getX(),bH.getY() - jednaHrana,10f);
        this.bUp3 = new Point3D(bH.getX() - jednaHrana,bH.getY() - jednaHrana,10f);
        this.bUp4 = new Point3D(bH.getX() - jednaHrana,bH.getY(),10f);



        double[] allX = {bH.getX(),b2.getX(),b3.getX(),b4.getX(), bUp1.getX(), bUp2.getX(), bUp3.getX(), bUp4.getX()};
        double[] allY = {bH.getY(),b2.getY(),b3.getY(),b4.getY(), bUp1.getY(), bUp2.getY(), bUp3.getY(), bUp4.getY()};
        double[] allZ = {bH.getZ(),b2.getZ(),b3.getZ(),b4.getZ(), bUp1.getZ(), bUp2.getZ(), bUp3.getZ(), bUp4.getZ()};

        this.xMin = getMin(allX,8);
        this.xMax = getMax(allX,8);
        this.yMin = getMin(allY,8);
        this.yMax = getMax(allY,8);
        this.zMin = getMin(allZ,8);
        this.zMax = getMax(allZ,8);

    }

    private double getMin(double[] pole, int n) {
        double min = pole[0];
        for (int i = 1; i < n; i++)
            min = Math.min(min, pole[i]);
        return min;
    }

    private double getMax(double[] pole, int n) {
        double max = pole[0];
        for (int i = 1; i < n; i++)
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

