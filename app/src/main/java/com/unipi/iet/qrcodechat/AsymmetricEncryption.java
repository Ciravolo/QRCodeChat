package com.unipi.iet.qrcodechat;

import android.util.Base64;

import org.apache.commons.codec.binary.Hex;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AsymmetricEncryption {

    static private String privateKey;
    static private String publicKey;
    static private PrivateKey prKey;
    static private PublicKey  pbKey;

    //Generazione delle chiavi
    public static void getRSAKeys() throws Exception{


        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey prKey = keyPair.getPrivate();
        PublicKey  pbKey = keyPair.getPublic();

        privateKey = new String(Hex.encodeHex(prKey.getEncoded()));
        publicKey  = new String(Hex.encodeHex(pbKey.getEncoded()));
    }

    //Criptaggio
    public String encryptSymmetric(byte[] message, PrivateKey key) {

        byte[] encrypted = null;

        try{
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(message);
            return new String(Base64.encode(encrypted, Base64.DEFAULT));

        } catch(NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException e){
            e.printStackTrace();
            return null;
        }
    }

    //Decriptaggio
    public String decryptSymmetric(byte[] message, PublicKey key)  {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(Hex.encodeHex(cipher.doFinal(message)));
        }
        catch( Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
