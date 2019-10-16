package ECC;

import java.math.BigInteger;

public class ECGroup {

    private BigInteger a, b; //coeffs of the equation
    private BigInteger P; //prime
    private BigInteger N; //order of the group
    private Point G; //generator

    //ZERO point (not on the plane)
    //set to (P,P) since this point does not occur anywhere else in the group
    public final Point ZERO;


    private boolean contains(Point p) {
        BigInteger x = p.getX(), y = p.getY();
        BigInteger lhs = y.multiply(y).mod(P);
        BigInteger rhs = x.multiply(x).multiply(x).add(a.multiply(x)).add(b).mod(P);
        return lhs.equals(rhs);
    } 

    public Point getRandomPoint() {
        BigInteger x,y;
        for (x=BigInteger.ONE; x.compareTo(P) < 0; x=x.add(BigInteger.ONE)) {
            for (y=BigInteger.ONE; y.compareTo(P) < 0; y=y.add(BigInteger.ONE)) {
                if (this.contains(new Point(x,y))) return new Point(x,y);
            }
        }
        return ZERO;
    }

    public BigInteger getP() { return P; }


    public BigInteger computeOrder() {

        BigInteger order = BigInteger.ZERO;
        for (BigInteger y = BigInteger.ZERO; y.compareTo(this.P) == -1; y = y.add(BigInteger.valueOf(1))) {
            for (BigInteger x = BigInteger.ZERO; x.compareTo(this.P) == -1; x = x.add(BigInteger.valueOf(1))) {
                //System.out.println(y+" "+x);
                BigInteger lhs = y.multiply(y).mod(this.P);
                BigInteger rhs = x.multiply(x).multiply(x).add(this.a.multiply(x)).add(this.b).mod(this.P);
                if (lhs.compareTo(rhs) == 0) order = order.add(BigInteger.ONE);
            }
        }
        return order;
    }

    private BigInteger getSlopeAtPoint(Point Q) {
        BigInteger x = Q.getX(), y = Q.getY();
        BigInteger num = BigInteger.valueOf(3).multiply(x).multiply(x).add(this.a).mod(this.P);
        BigInteger den = BigInteger.valueOf(2).multiply(y).mod(this.P);
        return num.multiply(den.modInverse(this.P)).mod(this.P);
    }

    public ECGroup(BigInteger a, BigInteger b, BigInteger P) {

        this.a = a;
        this.b = b;
        this.P = P;
        this.ZERO = new Point(P,P);
        this.N = computeOrder();
        this.G = findGenerator();
    }

    public ECGroup(long a, long b, long P, Point G) {
        this.a = BigInteger.valueOf(a);
        this.b = BigInteger.valueOf(b);
        this.P = BigInteger.valueOf(P);
        this.ZERO = new Point(P,P);
        this.N = computeOrder();
        this.G = G;
    }

    public ECGroup(long a, long b, long P) {
        this(BigInteger.valueOf(a),BigInteger.valueOf(b),BigInteger.valueOf(P));
    }

    private Point findGenerator() {
        BigInteger n = Utils.largestPrimeFactor(N);
        BigInteger h = N.divide(n);
        Point p = getRandomPoint();
        Point g = multiply(p,h);
        return g;
    }

    public Point getGenerator() {
        return G;
    }

    

    public Point inverse(Point A) {
        return new Point(A.getX(), P.subtract(A.getY().mod(P)));
    }

    public BigInteger getOrder() { return N; }

    public Point add(Point A, Point B) {

        //0 + B = B
        if (A.equals(this.ZERO)) return B;

        //A + 0 = A
        if (B.equals(this.ZERO)) return A;

        //A + (-A) = 0
        if (A.equals(this.inverse(B))) return this.ZERO;

        BigInteger Xa = A.getX(), Ya = A.getY();
        BigInteger Xb = B.getX(), Yb = B.getY();

        

        if (A.equals(B)) { //same point

            //if both at origin
            //System.out.println("HERE\n");
            if (Xa.equals(BigInteger.ZERO) && Ya.equals(BigInteger.ZERO)) return this.ZERO;

            BigInteger m = getSlopeAtPoint(A);
            //System.out.println("Slope at point: " + m);
            BigInteger Xc = m.multiply(m).subtract(Xa).subtract(Xb).mod(P);
            BigInteger Yc = Ya.add(m.multiply(Xc.subtract(Xa))).mod(P);
            return new Point(Xc,Yc);

        } else {

            //vertical line
            if (Xa.equals(Xb)) return this.ZERO;

            BigInteger ma = getSlopeAtPoint(A);
            BigInteger mb = getSlopeAtPoint(B);

            BigInteger num = Yb.subtract(Ya).mod(P);
            BigInteger den = Xb.subtract(Xa).mod(P);
            BigInteger m = num.multiply(den.modInverse(P)).mod(P);

            //if line is tangent to curve at point A
            if (m.equals(ma)) return this.inverse(B);

            //if line is tanget to curve at point B
            if (m.equals(mb)) return this.inverse(A);

            BigInteger Xc = m.multiply(m).subtract(Xa).subtract(Xb).mod(P);
            BigInteger Yc = Ya.add(m.multiply(Xc.subtract(Xa))).mod(P);
            return new Point(Xc,Yc);

        }

    }

    public Point multiply(Point A, BigInteger K) {

        String bin = K.toString(2);
        Point Q = new Point(A);
        Point R = this.ZERO;

        //multiply using repeated doubling 
        //log K time to multiply with K
        for (int i = bin.length()-1; i>=0; i--) {
            if (bin.charAt(i) == '1') R = this.add(R,Q);
            Q = this.add(Q,Q);
        }

        return R;
    } 

    public static void main(String args[]) {

/*
        ECGroup group = new ECGroup(BigInteger.valueOf(2),BigInteger.valueOf(3),BigInteger.valueOf(97));
        Point P1 = new Point(3,6);
        Point P2 = group.add(P1,P1);
        Point P3 = group.add(P2,P1);
        Point P4 = group.add(P3,P1);
        Point P5 = group.add(P4,P1);
        Point P6 = group.add(P5,P1);

        System.out.println(P1);
        System.out.println(P2);
        System.out.println(P3);
        System.out.println(P4);
        System.out.println(P5);
        System.out.println(P6+"\n");

        System.out.println(group.multiply(P1, BigInteger.valueOf(1)));
        System.out.println(group.multiply(P1, BigInteger.valueOf(2)));
        System.out.println(group.multiply(P1, BigInteger.valueOf(3)));
        System.out.println(group.multiply(P1, BigInteger.valueOf(4)));
        System.out.println(group.multiply(P1, BigInteger.valueOf(5)));   */

        /* BigInteger x = BigInteger.valueOf(15);
        BigInteger y = x;
        y = y.add(BigInteger.valueOf(10));
        System.out.println(x+" "+y); */


        /*
        ECGroup group = new ECGroup(1,1,13);
        System.out.println(group.getOrder());

        Point g = group.getGenerator();
        Point p = g, q = g;
        int i = 2;
        System.out.println("1 " + p.toString());
        p = group.add(p,g);
        while (true) {
            System.out.println(i + " " + p.toString());
            if (p.equals(g)) break;
            p = group.add(p,g);
            //q = group.multiply(g,BigInteger.valueOf(i+1));
            i++;
        } */
    }
}