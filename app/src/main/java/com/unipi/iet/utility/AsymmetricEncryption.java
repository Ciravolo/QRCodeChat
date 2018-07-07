package com.unipi.iet.utility;

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

    private String privateKey;
    private String publicKey;
    private PrivateKey prKey;
    private PublicKey  pbKey;

    //Generation of the RSA keys
    public void getRSAKeys() throws Exception{

        //Using RSA for the creation of the keys
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //KeyPair object is obtained
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //Both keys are obtained on different references
        prKey = keyPair.getPrivate();
        pbKey = keyPair.getPublic();

        //Same keys as before but in String format
        privateKey = new String(Hex.encodeHex(prKey.getEncoded()));
        publicKey  = new String(Hex.encodeHex(pbKey.getEncoded()));

    }

    //Encrypt with the Public Key
    public String encryptAsymmetric(byte[] message, PublicKey key) {

        byte[] encrypted;

        try{
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(message);
            return new String(Hex.encodeHex(encrypted));
        }
        catch(NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException e){
            e.printStackTrace();
            return null;
        }
    }

    //Decrypt with the Private Key
     public String decryptAsymmetric(byte[] message, PrivateKey key)  {

        byte[] decryptedBytes;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedBytes = cipher.doFinal(message);
            return new String(decryptedBytes);
        }
        catch( Exception e){
            e.printStackTrace();
            return null;
        }
    }


    //Getters and setters
    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrKey() {
        return prKey;
    }

    public PublicKey getPbKey() {
        return pbKey;
    }
}
