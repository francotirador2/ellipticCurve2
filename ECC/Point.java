package ECC;

import java.math.BigInteger;

public class Point {

    private BigInteger x;
    private BigInteger y;
    private boolean isInfinity;
    
    private static Point INFINITY;

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
        isInfinity = false;
    }
    public Point(Point p) {
        this.x = p.getX();
        this.y = p.getY();
        isInfinity = p.isInfinity(); 
    }
    public Point(long x, long y) {
        this( BigInteger.valueOf(x), BigInteger.valueOf(y));
    }
    public Point() {
        this.x = this.y = BigInteger.ZERO;
        isInfinity = true;
    }
    public static Point make(String array, int bitLength){
        BigInteger x = new BigInteger(array.substring(0,bitLength),2);
        BigInteger y = new BigInteger(array.substring(bitLength,bitLength*2),2);
        return new Point(x,y);
    }

   

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public boolean isInfinity() {
        return isInfinity;
    }
    
    public Point negate() {
        if (isInfinity()) {
            return getInfinity();
        }
        return new Point(x, y.negate());
    }
    
    public static Point getInfinity() {
        if(INFINITY == null)
            INFINITY = new Point();
        return INFINITY;
    }
    
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Point) {
            if (this.x != null) {
                Point otherPoint = (Point)other;
                // no need to check for null in this case
                return this.x.equals(otherPoint.x) &&
                       this.y.equals(otherPoint.y);
            } else {
                return other == getInfinity();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (isInfinity()) {
            return "INFINITY";
        } else {
            return "(" + x.toString() + ", " + y.toString() + ")";
        }
    }

    public String toBinaryString(int bitLength) {
        String x = this.x.toString(2);
        String y = this.y.toString(2);

        if (x.length() < bitLength) {
            String tmp = x;
            for (int i = 0; i<bitLength-tmp.length(); i++) x = '0' + x;
        }
        
        if (y.length() < bitLength) {
            String tmp = y;
            for (int i = 0; i<bitLength-tmp.length(); i++) y = '0' + y;
        }

        return x + y;
    }
    
    @Override
    public int hashCode(){        
        if (this.isInfinity) {
            return x.hashCode() * 31 + x.hashCode();
        }
        return 11;
    }
}
