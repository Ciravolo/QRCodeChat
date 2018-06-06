package com.unipi.iet.qrcodechat;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static android.content.Context.MODE_PRIVATE;

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
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            fileOutputStream.write(content.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!file.mkdirs()) {
            Log.e("error:", "Directory not created");
        }
    }

    public String readContentFromFile(String fileName){


        // This will reference one line at a time
        String line = "";

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                line += line;
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return line;
    }

}
