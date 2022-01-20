package org.haic.often;

import com.alibaba.fastjson.JSONObject;
import com.github.windpapi4j.WinDPAPI;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.haic.often.ChromeBrowser.LocalCookies;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.Security;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {

	public static final String proxyHost = "127.0.0.1";
	public static final int proxyPort = 7890;
	public static final String aliDataApi = "https://api.aliyundrive.com/adrive/v3/share_link/get_share_by_anonymous";
	public static final File userHome = new File(System.getProperty("user.home"), "AppData\\Local\\Microsoft\\Edge\\User Data");

	public static void main(String[] args) {
		//  String url = "https://files.yande.re/image/48126bd4124671beefdebe39b9b7bbf8/yande.re%20906157%20bikini%20breasts%20garter%20maid%20mignon%20nipples%20no_bra%20nopan%20pointy_ears%20see_through%20swimsuits%20tail%20thighhighs%20wet%20wet_clothes.png";
		//  url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/QQ9.5.3.28008.exe";
		//  int statusCode = NetworkFileUtils.connect(url)//
		// .proxy(proxyHost, proxyPort)//
		//  .interval(50).multithread(10).retry(true).download("F:\\Downloads");
		//for (int i = 0; i < 100; i++) {
		//System.out.println(UserAgentUtils.randomPE());
		//	System.exit(0);
		//LevelDBUtil levelDBUtil = new LevelDBUtil();
		//levelDBUtil.initLevelDB();
		//System.out.println(levelDBUtil.getKeys());
		//String path = "F:\\Downloads\\leveldb\\000005.ldb";
		//String xx = ReadWriteUtils.orgin(path).text();
		//String im = levelDBUtil.getKeys().get(0);
		//String om = im.split("\n")[10];
		//byte[] xz = encryptedValueDecrypt(im.getBytes(), getEncryptedKey(userHome));
		// String uc = new String(xz);
		//	System.out.println(uc);

	}

	public static void ssc() {
		Map<String, String> cookies2 = LocalCookies.home().getCookiesForDomain("aliyundrive.com");
		for (Map.Entry<String, String> entry : cookies2.entrySet()) {
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}

	}

	protected static byte[] encryptedValueDecrypt(byte[] encryptedValue, String encryptedKey) {
		Security.addProvider(new BouncyCastleProvider());

		int keyLength = 256 / 8;
		int nonceLength = 96 / 8;
		String kEncryptionVersionPrefix = "v10";
		int GCM_TAG_LENGTH = 16;

		try {
			byte[] encryptedKeyBytes = Base64.decodeBase64(encryptedKey);
			assertTrue(new String(encryptedKeyBytes).startsWith("DPAPI"));
			encryptedKeyBytes = Arrays.copyOfRange(encryptedKeyBytes, "DPAPI".length(), encryptedKeyBytes.length);
			WinDPAPI winDPAPI = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
			byte[] keyBytes = winDPAPI.unprotectData(encryptedKeyBytes);
			assertEquals(keyLength, keyBytes.length);
			byte[] nonce = Arrays.copyOfRange(encryptedValue, kEncryptionVersionPrefix.length(), kEncryptionVersionPrefix.length() + nonceLength);
			encryptedValue = Arrays.copyOfRange(encryptedValue, kEncryptionVersionPrefix.length() + nonceLength, encryptedValue.length);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
			encryptedValue = cipher.doFinal(encryptedValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return encryptedValue;
	}

	public static String getEncryptedKey(File userHome) {
		File userState = new File(userHome, "Local State");
		JSONObject jsonObject = JSONObject.parseObject(JSONObject.parseObject(ReadWriteUtils.orgin(userState).text()).getString("os_crypt"));
		return jsonObject.getString("encrypted_key");
	}
}