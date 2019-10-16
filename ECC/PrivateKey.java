package ECC;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * The private key of the El Gamal Elliptic Curve Cryptography.
 *
 * The key consists of: c, the elliptic curve used in the calculations, k is the
 * private key, a randomly-generated integer, satisfying 1 <= k < p-1.
 */
public class PrivateKey {

    private ECGroup group;
    private BigInteger k;

    public PrivateKey(ECGroup group, BigInteger k) {
        this.group = group;
        this.k = k;
    }

    public void setCurve(ECGroup group) {
        this.group = group;
    }

    public ECGroup getGroup() {
        return group;
    }

    public void setKey(BigInteger k) {
        this.k = k;
    }

    public BigInteger getKey() {
        return k;
    }

    public Point getGenerator() {
        return group.getGenerator();
    }

    public String toString(int base) {
        return k.toString(base);
    }

    public String toString() {
        return k.toString();
    }
}
