package simple2;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Class {

	private static SecretKeySpec secretKey;
	private static byte[] key;

	public static void main(String[] args) {
		final String secretKey = "secret_key_to_encrypt_message";
		String originalString = "secret_message_to_smart_device";
		String encryptedString = Class.encrypt(originalString, secretKey);
		String decryptedString = Class.decrypt(encryptedString, secretKey);  
//		try {
//			String hashValue = Class.hashing("password");
//			System.out.println("Digest(in hex format):: " + hashValue);
//			sign(originalString,secretKey);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
	}
	
	//The method that signs the data using the private key that is stored in keyFile path
	public static byte[] sign(String data, String myKey) throws InvalidKeyException, Exception{
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(myKey.getBytes("UTF-8"));
		KeyFactory kf = KeyFactory.getInstance("RSA");		
		Signature rsa = Signature.getInstance("SHA1withRSA"); 
		rsa.initSign(kf.generatePrivate(spec));
		rsa.update(data.getBytes());
		return rsa.sign();
	}
	

	public static String hashing(String password) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(password.getBytes());
		byte byteData[] = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static void setKey(String myKey) {////dictionary  setKey
		MessageDigest sha = null;
		try {
			key = myKey.getBytes("UTF-8");//key = myKey
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);//myKey
			byte[] hashkey = Arrays.copyOf(key, 16);//hashkey = key = myKey
			secretKey = new SecretKeySpec(hashkey, "AES");//secretKey = hashkey = key = myKey
			System.out.println(secretKey.getFormat());//secretKey = myKey
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static String encrypt(String strToEncrypt, String secret) {//dictionary  encrypt
		try {
			setKey(secret);//myKey = secret
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);//secretKey = myKey = secret, cipher.key=secretKey
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));//encrypt(strToEncrypt,cipher.key)
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	public static String decrypt(String strToDecrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}
}