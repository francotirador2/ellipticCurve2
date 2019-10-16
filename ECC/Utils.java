package ECC;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;

import java.io.*;

public class Utils {

    private static final int CHUNK_SIZE = 256; //bytes

    public BigInteger random(BigInteger low, BigInteger high) {
        BigInteger range = high.subtract(low).add(BigInteger.ONE);
        int bitLength = range.bitLength();
        while (true) {
            BigInteger tmp = new BigInteger(bitLength,new Random());
            if (tmp.compareTo(range) < 1) return tmp.add(low);
        }
    }

    public static boolean [] stringToBits(String string) {
        byte bytes [] = string.getBytes();
        boolean bits [] = new boolean [bytes.length*8];
        for (int i = 0; i<bytes.length; i++) {
            for (int j = 0; j<8; j++) {
                bits[i*8+j] = ((bytes[i] & (1 << j)) > 0);
            }
        }
        return bits;
    }

  
    public static String bitsToString(boolean bits[]) {

        byte bytes [] = new byte [bits.length/8];
        for (int i = 0; i<bits.length/8; i++) {
            for (int j = 0; j<8; j++) {
                if (bits[i*8+j]) bytes[i] += (byte)(1<<j);
            }
        }
        return new String(bytes);
    }

    
    public static boolean [] padded(boolean [] bits, int reqBlockLength) {

        int size = bits.length / reqBlockLength * reqBlockLength + (bits.length % reqBlockLength > 0 ? reqBlockLength : 0);
        boolean [] res = new boolean [size];
        for (int i = 0; i<bits.length; i++) bits[i] = res[i];
        return res;
    }

    public static void printBoolean(boolean [] bits) {
        for (boolean b : bits) {
            System.out.print(b?1:0);
        }
        System.out.println();
    }

    public static boolean [] bytesToBits(byte [] bytes) {
        boolean [] bits = new boolean [bytes.length*8];
        for (int i = 0; i<bytes.length; i++) {
            for (int j = 0; j<8; j++) bits[i*8+j] = (((byte)(1 << j) & bytes[i]) > 0);
        }
        return bits;
    }

    public static byte [] bitsToBytes(boolean [] bits) {
        byte [] bytes = new byte [bits.length/8];
        for (int i = 0; i<bits.length/8; i++) {
            for (int j = 0; j<8; j++) {
                if (bits[i*8+j]) bytes[i] += (byte)(1<<j);
            }
        }
        return bytes;
    }

      
    public static boolean [] processWord(boolean [] word, BigInteger key) {

        String keyBits = key.toString(2);
        boolean [] result = new boolean [word.length];

        for (int i = 0; i<result.length; i++) {
            if (keyBits.charAt(i%keyBits.length()) == '0') result[i] = word[i];
            else result[i] = (word[i] ^ true);
        }

        return result;
    }

    
    public static boolean [] processChunk(boolean [] plainTextChunk, BigInteger key) {

        int wordLength = key.bitLength();
        boolean [] pad = Utils.padded(plainTextChunk,wordLength);
        boolean [] result = new boolean [pad.length];

        for (int i = 0; i<pad.length/wordLength; i++) {

            boolean block [] = Arrays.copyOfRange(pad,i*wordLength,(i+1)*wordLength);
            boolean encrypted [] = processWord(block,key);
            for (int j = 0; j<wordLength; j++) result[i*wordLength+j] = encrypted[j];
        }

        return result;
    }

    public static void encryptFile(String srcFilePath, String destFilePath, BigInteger key) {

        try {

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFilePath));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFilePath));
            
            int x, last = 0;
            byte [] buffer = new byte [CHUNK_SIZE];
            int wordLength = key.bitLength()-1;

            System.out.println("Encrypting!! Please Wait...");

            while ((x = bis.read()) != -1) {

                if (last == CHUNK_SIZE) {

                    boolean [] encrypted = processChunk(padded(bytesToBits(buffer),wordLength),key);
                    byte [] encrBytes = bitsToBytes(padded(encrypted,8));
                    bos.write(encrBytes);
                    buffer = new byte [CHUNK_SIZE];
                    last = 0;

                } else buffer[last++] = (byte)x;
                
            }

            if (last > 0) {

                byte [] rem = Arrays.copyOfRange(buffer,0,last);
                boolean [] encrypted = processChunk(padded(bytesToBits(buffer),wordLength),key);
                byte [] encrBytes = bitsToBytes(padded(encrypted,8));
                bos.write(encrBytes);

            }

            bos.flush();
            bos.close();
            bis.close();

            System.out.println("Done");

        } catch (Exception e) {

            System.out.println(e);
        }
    }

    public static void decryptFile(String srcFilePath, String destFilePath, BigInteger key) {

        int wordLength = key.bitLength() - 1;
        int chunkSize = (CHUNK_SIZE*8)/wordLength * wordLength + (((CHUNK_SIZE*8) % wordLength > 0) ? wordLength : 0);
        chunkSize = chunkSize / 8 + ((chunkSize % 8 > 0) ? 1 : 0);

        try {
        
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFilePath));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFilePath));
            
            byte [] buffer = new byte [chunkSize];
            int x, last = 0;

            System.out.println("Decrypting!! Please wait...");

            while ((x = bis.read()) != -1) {

                if (last == chunkSize) {

                    boolean [] bits = Arrays.copyOfRange(bytesToBits(buffer),0,chunkSize*8/wordLength*wordLength);
                    boolean [] decrypted = processChunk(bits,key);
                    boolean [] decrBits = Arrays.copyOfRange(decrypted,0,CHUNK_SIZE*8);
                    byte [] decrBytes = bitsToBytes(decrBits);
                    bos.write(decrBytes);
                    last = 0;
                    buffer = new byte [chunkSize];

                } else buffer[last++] = (byte)x;
            }

            if (last > 0) {

                int nBits = ((last*8)/wordLength) * wordLength;
                boolean [] bits = Arrays.copyOfRange(bytesToBits(buffer),0,nBits);
                boolean [] decrypted = processChunk(bits,key);
                boolean [] decrBits = Arrays.copyOfRange(decrypted,0,(nBits/8)*8);
                byte [] decrBytes = bitsToBytes(decrBits);
                bos.write(decrBytes);
            }

            bos.flush();
            bos.close();
            bis.close();

            System.out.println("Done");

        } catch (Exception e) {

            System.out.println(e);
        }
    }

    public static BigInteger largestPrimeFactor(BigInteger n) {
    
        BigInteger x = n;
        BigInteger p = BigInteger.ZERO;

        while (x.compareTo(BigInteger.ONE) > 0) {

            BigInteger f = BigInteger.ONE;
            while (f.multiply(f).compareTo(x) < 1) {

                if (x.mod(f).equals(BigInteger.ZERO)) {

                    BigInteger cf = x.divide(f);
                    if (cf.isProbablePrime(25)) return cf;
                    else {

                        if (f.compareTo(BigInteger.ONE) > 0) {
                            while (x.mod(f).equals(BigInteger.ZERO)) x = x.divide(f); 
                            if (f.isProbablePrime(25) && f.compareTo(p) > 0) p = f; 
                        }
                    }
                }

                f = f.add(BigInteger.ONE);
            }
            
        }

        return p;
    }

    public static void main(String args[]) {

/*
        String srcFile = "echapackage/Test.java";
        String destFile = "echapackage/TestEncr";
        BigInteger key = BigInteger.valueOf(97);
        encryptFile(srcFile,destFile,key); 
        decryptFile(destFile,"echapackage/TestDecr.txt",key);*/

        BigInteger key = BigInteger.valueOf(19);

        String pt = "abcd";
        boolean [] ct = processChunk(stringToBits(pt),key);
        boolean decr[] = processChunk(ct,key);
        String decrText = bitsToString(decr);
        System.out.println(decrText);
    }

  
}