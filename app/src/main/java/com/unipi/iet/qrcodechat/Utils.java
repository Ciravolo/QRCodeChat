package com.unipi.iet.qrcodechat;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import org.apache.commons.codec.binary.Hex;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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


public class Utils {

    private byte[] xorWithKey(byte[] k1, byte[] k2){
        byte[] out = new byte[k1.length];
        for (int i = 0; i < k1.length; i++) {
            out[i] = (byte) (k1[i] ^ k2[i%k2.length]);
        }
        return out;
    }

    public String xorKeys(String myKey, String hisKey){
        //do an xor operation of both keys
        byte[] bytesMyKey = myKey.getBytes();
        byte[] bytesHisKey = hisKey.getBytes();

        return new String(Base64.encode(xorWithKey(bytesMyKey, bytesHisKey), Base64.DEFAULT));
    }


    public String generateRandomizedString128Bits() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);
        String randomGeneratedString = new String(Hex.encodeHex(bytes));
        randomGeneratedString.replace('+','-').replace('/','_');
        return randomGeneratedString;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeFileWithContent(String fileName, String content) throws IOException {

        File sd = Environment.getExternalStorageDirectory();
        File f = new File(sd, fileName);

        FileWriter fw = null;
        BufferedWriter bw = null;
        try{
            fw = new FileWriter(f, true);
            bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readContentFromFile(String fileName) {

        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), fileName);

        FileReader fr = null;
        BufferedReader br = null;
        String line = "";
        StringBuilder result = new StringBuilder();

        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            while((line = br.readLine()) != null) {
                result.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("privatekey: ", result.toString());
        return result.toString();
    }


        //To save a private key on device
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
