package com.engine.security;

/*
 * http://www.java2s.com/Code/Java/Security/EncryptingaStringwithDES.htm
 * https://stackoverflow.com/questions/23561104/how-to-encrypt-and-decrypt-string-with-my-passphrase-in-java-pc-not-mobile-plat
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;

import java.io.UnsupportedEncodingException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 *
 * @author zchumager
 * Encrypts and decrypts simple text. AES encryption is being used.
 */
public class Encryptor {

    private Cipher ecipher;
    private Cipher dcipher;

    /** Encryption with given key. */
    public Encryptor(SecretKey key) throws Exception {
        ecipher = Cipher.getInstance("AES");
        dcipher = Cipher.getInstance("AES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        dcipher.init(Cipher.DECRYPT_MODE, key);
    }

    /** Encryption with given password. Use it to encrypt or decrypt <Code>String</Code> messages. */
    public Encryptor(String passKey) throws Exception{
        this(new SecretKeySpec(passKey.getBytes(), "AES"));
    }

    /** Encrypts text.
     * @return encrypted message. Null if error occurs.*/
    public String encrypt(String str) {
        // Encode the string into bytes using utf-8
        byte[] utf8;
        try {
            utf8 = str.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) { // neturetu sito ismest
            e.printStackTrace();
            Gdx.app.log("Encryption", "Failed to encrypt message");
            return null;
        }

        // Encrypt
        byte[] enc;
        try {
            enc = ecipher.doFinal(utf8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            Gdx.app.log("Encryption", "Failed to encrypt message");
            return null;
        }

        // Encode bytes to base64 to get a string
        return new String(Base64Coder.encode(enc));
    }

    /** Decrypts given encrypted message.
     * @return decrypted message. Null if error occurs. */
    public String decrypt(String str) {
        // Decode base64 to get bytes
        byte[] dec;
        try {
            dec = Base64Coder.decode(str);
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
            return null;
        }
//        try {
//            dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Gdx.app.log("Encryption", "Cannot decrypt message");
//            return null;
//        }

        byte[] utf8;
        try {
            utf8 = dcipher.doFinal(dec);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            Gdx.app.log("Encryption", "Cannot decrypt message");
            return null;
        }

        // Decode using utf-8
        try {
            return new String(utf8, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Gdx.app.log("Encryption", "Cannot decrypt message");
            return null;
        }
    }

    /* example. */

//    public static void main(String args []) throws Exception
//    {
//
//        String data = "Some secret text";
//        String k = "Bar12345Bar12345";
//
//        //SecretKey key = KeyGenerator.getInstance("AES").generateKey();
//        SecretKey key = new SecretKeySpec(k.getBytes(), "AES");
//        Encryptor encrypter = new Encryptor(key);
//
//
//        System.out.println("Original String: " + data);
//
//        String encrypted = encrypter.encrypt(data);
//
//        System.out.println("Encrypted String: " + encrypted);
//
//        String decrypted = encrypter.decrypt(encrypted);
//
//        System.out.println("Decrypted String: " + decrypted);
//    }
}