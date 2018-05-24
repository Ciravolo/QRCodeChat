package com.unipi.iet.qrcodechat;

import android.util.Base64;

import org.apache.commons.codec.binary.Hex;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {

public String encryptSymmetric(byte[] message, byte[] key) {

        byte[] encrypted = null;

        try{
            SecretKey secretKeySpec = new SecretKeySpec(key, "AES");				//An AES key is generated using the data contained inside the key variable (2 byte * keyLength)
            Cipher cipher = Cipher.getInstance("AES");										//This class returns an istance of a Cipher encapsulating a CipherSpi implementation from the first registered provider, for the specified algorithm
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);								//Here we are using the cipher to set the encryption mode and the key used to ecrypt
            encrypted = cipher.doFinal(message);											//Here the message is encrypted
            String strEncrypted = new String(Base64.encode(encrypted, Base64.DEFAULT));		//All the char out of Base64 standard are rejected
            return strEncrypted;															//This is the final encrypted text returned

        } catch(NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException e){
            e.printStackTrace();
            return null;
        }
}

    public String decryptSymmetric(byte[] message, byte[] key)  {

        byte[] clearText = null;

        try {

            SecretKey secretKeySpec = new SecretKeySpec(key, "AES");

            byte[] keyBytes = secretKeySpec.getEncoded();
            String strKeyBytes = new String(Hex.encodeHex(keyBytes));

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            clearText = cipher.doFinal(message);

            return new String(Hex.encodeHex(clearText));
        }
        catch( Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
