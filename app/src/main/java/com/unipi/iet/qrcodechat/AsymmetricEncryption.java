package com.unipi.iet.qrcodechat;

import android.util.Log;
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
import org.apache.commons.codec.DecoderException;

public class AsymmetricEncryption {

    private String privateKey;
    private String publicKey;
    private PrivateKey prKey;
    private PublicKey  pbKey;

    /**
     * Generation of the RSA keys
     * @throws Exception
     */
    public void getRSAKeys() throws Exception{

        //using RSA for the creation of the keys
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //KeyPair object is obtained
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //both keys are obtained on different references
        prKey = keyPair.getPrivate();
        pbKey = keyPair.getPublic();

        //same keys as before but in String format
        privateKey = new String(Hex.encodeHex(prKey.getEncoded()));
        publicKey  = new String(Hex.encodeHex(pbKey.getEncoded()));

    }

    /**
     *  Encrypt with the Public Key
     * @param message in byte array
     * @param key in PublicKey object
     * @return the encrypted string
     */
    public String encryptAsymmetric(byte[] message, PublicKey key) {

        byte[] encrypted = null;

        try{
            String strBeforeEncryption = new String(Hex.encodeHex(message));
            Log.i("before enc:", strBeforeEncryption);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(message);
            String strEncrypted = new String(Hex.encodeHex(encrypted));

            Log.i("after enc: ", strEncrypted);

            byte[] bytesEncMessage = Hex.decodeHex(strEncrypted.toCharArray());
            int i =bytesEncMessage.length;
            Log.i("bytes of enc message: ", String.valueOf(i));
            return strEncrypted;
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }catch (DecoderException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypt with the Private Key
     * @param message on a byte array
     * @param key on a PrivateKey object
     * @return the decrypted String
     */
     public String decryptAsymmetric(byte[] message, PrivateKey key)  {

        String clearText = "";
        byte[] decryptedBytes;
        try {
            String strEncrypted = new String(Hex.encodeHex(message));
            Log.i("before dec:", strEncrypted);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedBytes = cipher.doFinal(message);

            Log.i("str decrypted", new String(Hex.encodeHex(decryptedBytes)));

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
