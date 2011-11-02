package org.oobium.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.oobium.utils.Base64;

public class PasswordUtils {

	public static final String CHARSET = "UTF-8";
	
	public static String[] encrypt(String plaintext) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] salt = getSalt();
		return new String[] {
			encrypt(plaintext, salt, 1000),
			new String(Base64.encode(salt), CHARSET)
		};
	}
	
	public static String encrypt(String plaintext, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return encrypt(plaintext, salt, 1000);
	}
	
	public static String encrypt(String plaintext, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return encrypt(plaintext, Base64.decode(salt.getBytes(CHARSET)), 1000);
	}
	
	/**
	 * Process:<br>
	 *  1. Encrypt passwords using one-way techniques, that is, digests.<br>
	 *  2. Match input and stored passwords by comparing digests, not unencrypted strings.<br>
	 *  3. Use a salt containing at least 8 random bytes, and attach these random bytes, undigested, to the result.<br>
	 *  4. Iterate the hash function at least 1,000 times.<br>
	 *  5. Prior to digesting, perform string-to-byte sequence translation using a fixed encoding, preferably UTF-8.<br>
	 *  6. Finally, apply BASE64 encoding and store the digest as an US-ASCII character string.<br>
	 * @param salt
	 * @param plaintext
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String encrypt(String plaintext, byte[] salt, int iterations) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.reset();
		md.update(salt);
		byte[] input = md.digest(plaintext.getBytes(CHARSET));
		for(int i = 0; i < iterations; i++) {
			md.reset();
			input = md.digest(input);
		}
		byte[] output = new byte[salt.length + input.length];
		System.arraycopy(salt, 0, output, 0, salt.length);
		System.arraycopy(input, 0, output, salt.length, input.length);
		return new String(Base64.encode(output), CHARSET);
	}

	public static byte[] getSalt() throws NoSuchAlgorithmException {
		return getSalt(System.currentTimeMillis(), 8);
	}
	
	public static byte[] getSalt(long seed) throws NoSuchAlgorithmException {
		return getSalt(seed, 8);
	}
	
	public static byte[] getSalt(long seed, int length) throws NoSuchAlgorithmException {
		byte[] salt = new byte[length];
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		sr.nextBytes(salt);
		return salt;
	}
	
}
