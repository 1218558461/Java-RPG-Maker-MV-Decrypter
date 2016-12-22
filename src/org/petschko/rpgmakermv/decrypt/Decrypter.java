package org.petschko.rpgmakermv.decrypt;

import com.sun.istack.internal.NotNull;
import java.nio.file.FileSystemException;
import java.util.ArrayList;

/**
 * Author: Peter Dragicevic [peter-91@hotmail.de]
 * Authors-Website: http://petschko.org/
 * Date: 21.12.2016
 * Time: 15:12
 * Update: -
 * Version: 0.0.1
 *
 * Notes: Decrypter class
 */
class Decrypter {
	private String decryptCode = null;
	private String[] realDecryptCode = null;
	private int headerLen = 16;
	private int xhrOk = 400;
	private String signature = "5250474d56000000";
	private String version = "000301";
	private String remain = "0000000000";

	/**
	 * Creates a new Decrypter instance
	 */
	public Decrypter() {
	}

	/**
	 * Creates a new Decrypter instance with a Decryption Code
	 *
	 * @param decryptCode - Decryption-Code
	 */
	public Decrypter(@NotNull String decryptCode) {
		this.setDecryptCode(decryptCode);
	}

	/**
	 * Return the Decrypt-Code
	 *
	 * @return DecryptCode or null if not set
	 */
	public String getDecryptCode() {
		return decryptCode;
	}

	/**
	 * Sets the Decrypt-Code
	 *
	 * @param decryptCode - Decrypt-Code
	 */
	public void setDecryptCode(@NotNull String decryptCode) {
		this.decryptCode = decryptCode;
	}

	/**
	 * Returns the Real Decrypt-Code as Array
	 *
	 * @return Real Decrypt-Code as Array
	 */
	private String[] getRealDecryptCode() {
		if(this.realDecryptCode == null)
			this.calcRealDecryptionCode();

		return realDecryptCode;
	}

	/**
	 * Sets the Real Decrypt-Code as Array
	 *
	 * @param realDecryptCode - Real Decrypt-Code as Array
	 */
	private void setRealDecryptCode(@NotNull String[] realDecryptCode) {
		this.realDecryptCode = realDecryptCode;
	}

	/**
	 * Returns the Byte-Length of the File-Header
	 *
	 * @return - File-Header Length in Bytes
	 */
	public int getHeaderLen() {
		return headerLen;
	}

	/**
	 * Sets the File-Header Length in Bytes
	 *
	 * @param headerLen - File-Header Length in Bytes
	 */
	public void setHeaderLen(@NotNull int headerLen) {
		this.headerLen = headerLen;
	}

	/**
	 * Returns the xhrOk Value
	 *
	 * @return - xhrOk Value
	 */
	public int getXhrOk() {
		return xhrOk;
	}

	/**
	 * Sets the xhrOk Value
	 *
	 * @param xhrOk - xhrOk Value
	 */
	public void setXhrOk(@NotNull int xhrOk) {
		this.xhrOk = xhrOk;
	}

	/**
	 * Returns the Signature
	 *
	 * @return - Signature
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets the Signature
	 *
	 * @param signature - Signature
	 */
	public void setSignature(@NotNull String signature) {
		this.signature = signature;
	}

	/**
	 * Returns the Version
	 *
	 * @return - Version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the Version
	 *
	 * @param version - Version
	 */
	public void setVersion(@NotNull String version) {
		this.version = version;
	}

	/**
	 * Returns Remain
	 *
	 * @return - Remain
	 */
	public String getRemain() {
		return remain;
	}

	/**
	 * Sets Remain
	 *
	 * @param remain - Remain
	 */
	public void setRemain(@NotNull String remain) {
		this.remain = remain;
	}

	/**
	 * Reads the Decrypt-Code into an Array with 2 Paired Strings
	 *
	 * @throws NullPointerException - Decrypt-Code is null
	 */
	private void calcRealDecryptionCode() throws NullPointerException {
		if(this.getDecryptCode() == null)
			throw new NullPointerException("DecryptCode");

		String[] decryptArray = this.getDecryptCode().split("/(.{2})/");
		ArrayList<String> verifiedDecryptArray = new ArrayList<>();

		// Remove empty parts
		for(String aDecryptArray : decryptArray) {
			if(! aDecryptArray.equals(""))
				verifiedDecryptArray.add(aDecryptArray);
		}

		this.setRealDecryptCode(verifiedDecryptArray.toArray(new String[verifiedDecryptArray.size()]));
	}

	public void decryptFile(File file) throws Exception {
		try {
			if(! file.load())
				throw new FileSystemException(file.getFilePath(), "", "Can't load File-Content...");
		} catch(Exception e) {
			e.printStackTrace();

			return;
		}

		// Check if all required external stuff is here
		if(this.getDecryptCode() == null)
			throw new NullPointerException("Decryption-Code is not set!");
		if(file.getContent() == null)
			throw new NullPointerException("File-Content is not loaded!");

		// Get Content
		byte[] content = file.getContent();
		// Check Header
		byte[] header = Decrypter.getByteArray(content, 0, this.getHeaderLen());
		byte[] refBytes = new byte[this.getHeaderLen()];
		String refStr = this.getSignature() + this.getVersion() + this.getRemain();

		// Generate reference bytes
		for(int i = 0; i < this.getHeaderLen(); i++) {
			int substrStart = i * 2;
			refBytes[i] = (byte) Integer.parseInt(refStr.substring(substrStart, substrStart + 2), 16);
		}

		// Verify header (Check if its an encrypted file)
		for(int i = 0; i < this.getHeaderLen(); i++) {
			if(refBytes[i] != header[i])
				throw new Exception("Header is invalid");
		}

		// Remove Header from rest
		content = Decrypter.getByteArray(content, this.getHeaderLen());

		// Decrypt
		if(content.length > 0) {
			for(int i = 0; i < this.getHeaderLen(); i++) {
				content[i] = (byte) (content[i] ^ (byte) Integer.parseInt(this.getRealDecryptCode()[i], 16));
			}
		}

		file.setContent(content);
		//file.save(true); // todo change file extension and may file save dir
	}

	/**
	 * Get a new Byte-Array with given start pos and length
	 *
	 * @param byteArray - Byte-Array where to extract a new Byte-Array
	 * @param startPos - Start-Position on the Byte-Array (0 is first pos)
	 * @param length - Length of the new Array (Values below 0 means to Old-Array end)
	 * @return - New Byte-Array
	 */
	private static byte[] getByteArray(byte[] byteArray, int startPos, int length) {
		// Don't allow start-values below 0
		if(startPos < 0)
			startPos = 0;

		// Check if length is to below 0 (to end of array)
		if(length < 0)
			length = byteArray.length - startPos;

		byte[] newByteArray = new byte[length];
		int n = 0;

		for(int i = startPos; i < (startPos + length); i++) {
			// Check if byte array is on the last pos and return shorter byte array if
			if(byteArray.length < i)
				return getByteArray(newByteArray, 0, n);

			newByteArray[n] = byteArray[i];
			n++;
		}

		return newByteArray;
	}

	/**
	 * Get a new Byte-Array from the given start pos to the end of the array
	 *
	 * @param byteArray - Byte-Array where to extract a new Byte-Array
	 * @param startPos - Start-Position on the Byte-Array (0 is first pos)
	 * @return - New Byte-Array
	 */
	private static byte[] getByteArray(byte[] byteArray, int startPos) {
		return getByteArray(byteArray, startPos, -1);
	}

	/**
	 * Returns the real Extension of the current fake extension
	 *
	 * @param fakeExt - Fake Extension where you want to get the real Extension
	 * @return - Real File-Extension
	 */
	private static String realExtByFakeExt(String fakeExt) {
		switch(fakeExt.toLowerCase()) {
			case "rpgmvp":
				return "png";
			case "rpgmvm":
				return "m4a";
			case "rpgmvo":
				return "ogg";
			default:
				return "unknown";
		}
	}
}
