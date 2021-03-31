package app;

import org.lwjgl.system.CallbackI;
import transforms.Point3D;

public class Box {
    private int x,y;
    private Point3D bH,b2,b3,b4,bUp1,bUp2,bUp3,bUp4;
    private int jednaHrana;

    public Box(int x, int y , int jednaHrana) {
        this.x = x;
        this.y = y;
        this.jednaHrana = jednaHrana;

        this.bH = new Point3D(jednaHrana + x * jednaHrana,jednaHrana + y * jednaHrana,0);
        this.b2 = new Point3D(bH.getX(),bH.getY() - jednaHrana,0);
        this.b3 = new Point3D(bH.getX() - jednaHrana,bH.getY() - jednaHrana,0);
        this.b4 = new Point3D(bH.getX() - jednaHrana,bH.getY(),0);

        this.bUp1 = new Point3D(jednaHrana + x * jednaHrana,jednaHrana + y * jednaHrana,1);
        this.bUp2 = new Point3D(bH.getX(),bH.getY() - jednaHrana,1);
        this.bUp3 = new Point3D(bH.getX() - jednaHrana,bH.getY() - jednaHrana,1);
        this.bUp4 = new Point3D(bH.getX() - jednaHrana,bH.getY(),1);


    }
}

