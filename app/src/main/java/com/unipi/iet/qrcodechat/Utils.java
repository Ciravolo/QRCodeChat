package com.unipi.iet.qrcodechat;

import android.util.Base64;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

}
