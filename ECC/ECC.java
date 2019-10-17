package ECC;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

import java.util.Arrays;

import java.io.*;



public class ECC {

    private static final int SUF_SIZE = 4096;

    public static int PAD = 5;
    public static final Random r = new Random();
    private ECGroup group;

    public ECC(ECGroup group) {
        this.group = group;
    }

    public static Random getRandom() {
        return r;
    }

    private String symmetric(String msg, String key) {

        byte [] keyBytes = key.getBytes();
        byte [] msgBytes = msg.getBytes();
        byte [] result = new byte [msgBytes.length];

        for (int i  = 0; i<msgBytes.length; i++) {
            result[i] = (byte)(msgBytes[i] ^ keyBytes[i%keyBytes.length]);
        }

        return new String(result);
    }

    private String encryptString(String plainText, PublicKey key) {

        ECGroup group = key.getGroup();
        BigInteger p = group.getP();
        Point g = group.getGenerator();

        BigInteger k;
        do {
            k = new BigInteger(p.bitLength(),getRandom());
        } while (!k.gcd(p).equals(BigInteger.ONE));

        Point sharedSecret = group.multiply(key.getKey(),k);
        Point keyHint = group.multiply(g,k);

        String cipherText = symmetric(plainText,sharedSecret.toBinaryString(p.bitLength()));
        cipherText = keyHint.toBinaryString(p.bitLength()) + cipherText;
        return cipherText;

    }

    public String decryptString(String cipherText, PrivateKey key) {

        ECGroup group = key.getGroup();
        BigInteger p = group.getP();
        
        Point keyHint = Point.make(cipherText,p.bitLength());
        Point sharedSecret = group.multiply(keyHint,key.getKey());

        cipherText = cipherText.substring(keyHint.toBinaryString(p.bitLength()).length(),cipherText.length());
        String plainText = symmetric(cipherText,sharedSecret.toBinaryString(p.bitLength()));
        return plainText;
    }

     
    public KeyPair generateKeyPair() {
        
        BigInteger p = group.getP();
        BigInteger privateKey;
        do {
            privateKey = new BigInteger(p.bitLength(), getRandom());
        } while (privateKey.gcd(p).compareTo(BigInteger.ONE) != 0);

        Point g = group.getGenerator();
        Point publicKey = group.multiply(g, privateKey);

        return new KeyPair(
                new PublicKey(group, publicKey),
                new PrivateKey(group, privateKey)
        );
    }

    private void encryptFile(String srcFilePath, String destFilePath, PublicKey key) {

        try {

            ECGroup group = key.getGroup();
            Point g = group.getGenerator();
            BigInteger p = group.getP();
            BigInteger k;
            int numBits = p.bitLength();

            do {
                k = new BigInteger(numBits, getRandom());
            } while (!k.gcd(p).equals(BigInteger.ONE));

            Point sharedSecret = group.multiply(key.getKey(),k);
            //System.out.println("Shared secret during encryption: " + sharedSecret.toString());
            Point keyHint = group.multiply(g,k);

            BufferedInputStream epis = new BufferedInputStream(new FileInputStream(srcFilePath));
            BufferedOutputStream epos = new BufferedOutputStream(new FileOutputStream(destFilePath));

            byte [] suf = keyHint.toBinaryString(p.bitLength()).getBytes();
            epos.write(suf);
            //System.out.println("KeyHintString: " + new String(suf));

            int x, last = 0;
            suf = new byte [SUF_SIZE];

            while ((x = epis.read()) != -1) {

                if (last < SUF_SIZE) suf[last++] = (byte)x;
                else {

                    String cipherText = symmetric(new String(suf), sharedSecret.toBinaryString(p.bitLength()));
                    String plainText = symmetric(cipherText, sharedSecret.toBinaryString(p.bitLength()));
                    System.out.println(plainText);
                    epos.write(cipherText.getBytes()); epos.flush();
                    suf = new byte [SUF_SIZE];
                    last = 0;
                }
            }

            if (last > 0) {
                suf = Arrays.copyOfRange(suf,0,last);
                String cipherText = symmetric(new String(suf),sharedSecret.toBinaryString(p.bitLength()));
                String plainText = symmetric(cipherText,sharedSecret.toBinaryString(p.bitLength()));
                epos.write(cipherText.getBytes());
            }

            epos.flush();
            epis.close();
            epos.close();
         
        } catch (Exception e) {

            System.out.println("Exception " + e);

        }

    }

    private void decryptFile(String srcFilePath, String destFilePath, PrivateKey key) {

         try {

            BufferedInputStream epis = new BufferedInputStream(new FileInputStream(srcFilePath));
            BufferedOutputStream epos = new BufferedOutputStream(new FileOutputStream(destFilePath));

            ECGroup group = key.getGroup();
            BigInteger privateKey = key.getKey();
            BigInteger p = group.getP();

            String keyHintString = "";
            for (int i=0; i<p.bitLength()*2; i++)  {
                int x = epis.read();
                keyHintString += (char)x;
            }

            //System.out.println("KeyHintString: " + keyHintString);

            Point keyHint = Point.make(keyHintString,p.bitLength());
            Point sharedSecret = group.multiply(keyHint,privateKey);
            ///System.out.println("Shared secret during decryption: " + sharedSecret);

            int x, last = 0;
            byte [] suf = new byte [SUF_SIZE];

            while ((x = epis.read()) != -1) {
                if (last < SUF_SIZE) suf[last++] = (byte)x;
                else {
                    String plainText = symmetric(new String(suf), sharedSecret.toBinaryString(p.bitLength()));
                    epos.write(plainText.getBytes());
                    epos.flush();
                    suf = new byte [SUF_SIZE];
                    last = 0;
                }
            }

            if (last > 0) {

                suf = Arrays.copyOfRange(suf,0,last);
                String plainText = symmetric(new String(suf),sharedSecret.toBinaryString(p.bitLength()));
                epos.write(plainText.getBytes());

            }

            epos.flush();
            epis.close();
            epos.close();

        } catch (Exception e)  {

                System.out.println("Exception " + e);
        }


    }



    public static void main(String[] args) {

        ECGroup group = new ECGroup(4, 20, 29, new Point(1, 5));
        //ECGroup group = new ECGroup(1, 1, 13, new Point(1, 4));
        ECC ecc = new ECC(group);
        String msg = "Hello     \nWorld";
        KeyPair keys = ecc.generateKeyPair();

        
        String src = "ECC/PrivateKey.java";
        String encr = "ECC/PrivateKeyEncr.txt";
        String decr = "ECC/PrivateKeyDecr.txt";

        ecc.encryptFile(src,encr,keys.getPublicKey());
        ecc.decryptFile(encr,decr,keys.getPrivateKey()); 
        
    
        /*
        String encrypted = ecc.encryptString(msg,keys.getPublicKey());
        System.out.println(encrypted);
        String decrypted = ecc.decryptString(encrypted,keys.getPrivateKey());
        System.out.println(decrypted); */

    }
}
