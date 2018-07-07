package com.unipi.iet.utility;
import android.os.Environment;
import android.util.Base64;
import org.apache.commons.codec.binary.Hex;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.PrivateKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.security.spec.RSAPrivateKeySpec;
import java.security.KeyFactory;
import java.io.InputStream;

/**
 * Class that contains all the utilities used in all of the other classes.
 */
public class Utils {

    /**
     * To xor two keys in byte arrays.
     * @param k1 first byte array
     * @param k2 second byte array
     * @return a xor operation as a result of the both byte arrays
     */
    private byte[] xorWithKey(byte[] k1, byte[] k2){
        byte[] out = new byte[k1.length];
        for (int i = 0; i < k1.length; i++) {
            out[i] = (byte) (k1[i] ^ k2[i%k2.length]);
        }
        return out;
    }

    /**
     * Xor operation from two strings
     * @param myKey first key in String format
     * @param hisKey second key in Strinc format
     * @return a key in String format composed of the xor operation of the both keys
     */
    public String xorKeys(String myKey, String hisKey){
        //do an xor operation of both keys
        byte[] bytesMyKey = myKey.getBytes();
        byte[] bytesHisKey = hisKey.getBytes();

        return new String(Base64.encode(xorWithKey(bytesMyKey, bytesHisKey), Base64.DEFAULT));
    }

    /**
     * Generates a random string of 128 bits
     * @return random string of 128 bits
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public String generateRandomizedString128Bits() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);
        String randomGeneratedString = new String(Hex.encodeHex(bytes));
        randomGeneratedString.replace('+','-').replace('/','_');
        return randomGeneratedString;
    }

    /**
     * Check if the storage is available for writing operation
     * @return bool, true or false depending if it is able to write on device
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    /**
     * Method to save a privatekey in a file.
     * @param fileObj The file object related to the file to be recorded on device
     * @param mod modulus of the private key
     * @param exp exponent of the private key
     * @throws IOException
     */
    public static void saveToFile(File fileObj, BigInteger mod, BigInteger exp)
    throws IOException {
            ObjectOutputStream oout = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileObj)));
            try {
                oout.writeObject(mod);
                oout.writeObject(exp);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                oout.close();
            }

    }

    /**
     * Read a private key from file and obtain a private key object
     * @param fileObj the object that corresponds to the file to be read
     * @return Privatekey on PrivateKey format
     * @throws IOException
     */
     public static PrivateKey readPrivateKey(File fileObj) throws IOException {
        InputStream in = new FileInputStream(fileObj);
        ObjectInputStream oin =
                new ObjectInputStream(new BufferedInputStream(in));
        try {
            BigInteger m = (BigInteger) oin.readObject();
            BigInteger e = (BigInteger) oin.readObject();
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey privKey = fact.generatePrivate(keySpec);
            return privKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            oin.close();
        }
    }

}
