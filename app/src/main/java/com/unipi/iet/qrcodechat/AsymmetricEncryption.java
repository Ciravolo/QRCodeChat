package com.unipi.iet.qrcodechat;

import android.util.Base64;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.Charset;
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

    //Generazione delle chiavi
    public void getRSAKeys() throws Exception{


        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        prKey = keyPair.getPrivate();
        pbKey = keyPair.getPublic();

        privateKey = new String(Hex.encodeHex(prKey.getEncoded()));
        publicKey  = new String(Hex.encodeHex(pbKey.getEncoded()));

        Log.i("privkey on generation: ", privateKey);
        Log.i("pubKey on generation: ", publicKey);

    }

    //Criptaggio
    public String encryptAsymmetric(byte[] message, PublicKey key) {

        byte[] encrypted = null;

        try{
            Cipher cipher = Cipher.getInstance("RSA");
            if (key!=null) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                encrypted = cipher.doFinal(message);
                return new String(Base64.encode(encrypted, Base64.DEFAULT), Charset.forName("UTF-8"));
            }
            else
                return "";

        } catch(NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException e){
            e.printStackTrace();
            return null;
        }
    }

    //Decriptaggio
    public String decryptAsymmetric(byte[] message, PrivateKey key)  {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(Base64.decode(cipher.doFinal(message), Base64.DEFAULT), Charset.forName("UTF-8"));
        }
        catch( Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String encryptAsymmetricPublicKey(byte[] message, PublicKey key) {

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



     public String decryptWithPrivateKey(byte[] message, PrivateKey key)  {

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


    //Get pr key
    public String getPrivateKey() {
        return privateKey;
    }
    //Get pb key
    public String getPublicKey() {
        return publicKey;
    }

    //Get private key
    public PrivateKey getPrKey() {
        return prKey;
    }
    //Get public key
    public PublicKey getPbKey() {
        return pbKey;
    }
}
