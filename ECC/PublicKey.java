package ECC;

import java.io.File;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * The key consists of: c, the elliptic curve used in the calculations, pK, the
 * point obtained from k * G, where k is the corresponding private key and G is
 * the base point of c.
 */
public class PublicKey {

    private ECGroup group;
    private Point pK;

    public PublicKey(ECGroup group, Point pK) {
        this.group = group;
        this.pK = pK;
    }

    public ECGroup getGroup() {
        return this.group;
    }

    public void setGroup(ECGroup group) {
        this.group = group;
    }

    public Point getKey() {
        return pK;
    }

    public void setKey(Point pK) {
        this.pK = pK;
    }

    public Point getGenerator() {
        return group.getGenerator();
    }

    public String toString() {
        return pK.toString();
    }
}
