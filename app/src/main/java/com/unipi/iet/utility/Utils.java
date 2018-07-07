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

    //To xor two keys in byte arrays.
    private byte[] xorWithKey(byte[] k1, byte[] k2){
        byte[] out = new byte[k1.length];
        for (int i = 0; i < k1.length; i++) {
            out[i] = (byte) (k1[i] ^ k2[i%k2.length]);
        }
        return out;
    }

    //Xor operation from two strings
    public String xorKeys(String myKey, String hisKey){
        //do an xor operation of both keys
        byte[] bytesMyKey = myKey.getBytes();
        byte[] bytesHisKey = hisKey.getBytes();

        return new String(Base64.encode(xorWithKey(bytesMyKey, bytesHisKey), Base64.DEFAULT));
    }

    //Generates a random string of 128 bits
    public String generateRandomizedString128Bits() {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);
        String randomGeneratedString = new String(Hex.encodeHex(bytes));
        randomGeneratedString.replace('+','-').replace('/','_');
        return randomGeneratedString;
    }

    //Check if the storage is available for writing operation
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    //Method to save a privatekey in a file.
    static public void saveToFile(File fileObj, BigInteger mod, BigInteger exp) {
        try (ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileObj)))) {
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Read a private key from file and obtain a private key object
    public static PrivateKey readPrivateKey(File fileObj) throws IOException {
        InputStream in = new FileInputStream(fileObj);
        try (ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in))) {
            BigInteger m = (BigInteger) oin.readObject();
            BigInteger e = (BigInteger) oin.readObject();
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey privKey = fact.generatePrivate(keySpec);
            return privKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
