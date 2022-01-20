package org.haic.often.ChromeBrowser;

import com.alibaba.fastjson.JSONObject;
import com.github.windpapi4j.WinDPAPI;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.haic.often.Judge;
import org.haic.often.ReadWriteUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.Security;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 获取本地浏览器cookie
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/12/24 23:15
 */
public class LocalCookies {
	private File userHome;
	private File cookieStore;

	private LocalCookies() {
		userHome = new File(System.getProperty("user.home"), "AppData\\Local\\Microsoft\\Edge\\User Data");
	}

	/**
	 * 用户文件夹路径 C:\\users\\xxx\\AppData\\Local\\Microsoft\\Edge\\User Data
	 *
	 * @return new ChromeBrowser
	 */
	@Contract(pure = true) public static ChromeBrowser home() {
		return config().chromeBrowser();
	}

	/**
	 * 自定义 用户文件夹路径
	 *
	 * @param userHome 用户文件夹路径
	 * @return new ChromeBrowser
	 */
	@NotNull @Contract(pure = true) public static ChromeBrowser home(@NotNull final String userHome) {
		return home(new File(userHome));
	}

	/**
	 * 自定义 用户文件夹路径
	 *
	 * @param userHome 用户文件夹路径
	 * @return new ChromeBrowser
	 */
	@NotNull @Contract(pure = true) public static ChromeBrowser home(@NotNull final File userHome) {
		return config().chromeBrowser(userHome);
	}

	@NotNull @Contract(pure = true) private static LocalCookies config() {
		return new LocalCookies();
	}

	@NotNull @Contract(pure = true) private ChromeBrowser chromeBrowser() {
		return chromeBrowser(userHome);
	}

	@NotNull @Contract(pure = true) private ChromeBrowser chromeBrowser(@NotNull final File userHome) {
		return new ChromeBrowser(userHome);
	}

	@Contract(pure = true) private void userHome(@NotNull final File userHome) {
		this.userHome = userHome;
		this.cookieStore = new File(new File(userHome, "Default"), "Cookies");
	}

	public static abstract class Cookie {

		protected String name;
		protected byte[] encryptedValue;
		protected Date expires;
		protected String path;
		protected String domain;
		protected File cookieStore;

		public Cookie(String name, byte[] encryptedValue, String domain, File cookieStore) {
			this.name = name;
			this.encryptedValue = encryptedValue;
			this.domain = domain;
			this.cookieStore = cookieStore;
		}

		public Cookie(String name, byte[] encryptedValue, Date expires, String path, String domain, File cookieStore) {
			this.name = name;
			this.encryptedValue = encryptedValue;
			this.expires = expires;
			this.path = path;
			this.domain = domain;
			this.cookieStore = cookieStore;
		}

		public String getName() {
			return name;
		}

		public byte[] getEncryptedValue() {
			return encryptedValue;
		}

		public Date getExpires() {
			return expires;
		}

		public String getPath() {
			return path;
		}

		public String getDomain() {
			return domain;
		}

		public File getCookieStore() {
			return cookieStore;
		}

		public String getValue() {
			return new String(encryptedValue);
		}

		public abstract boolean isDecrypted();

	}

	public static class DecryptedCookie extends Cookie {

		private final String decryptedValue;

		public DecryptedCookie(String name, byte[] encryptedValue, String decryptedValue, Date expires, String path, String domain, File cookieStore) {
			super(name, encryptedValue, expires, path, domain, cookieStore);
			this.decryptedValue = decryptedValue;
		}

		public String getDecryptedValue() {
			return decryptedValue;
		}

		@Override public boolean isDecrypted() {
			return true;
		}

		@Override public String toString() {
			return "Cookie [name=" + name + ", value=" + decryptedValue + "]";
		}

		@Override public String getValue() {
			return decryptedValue;
		}

	}

	public static class EncryptedCookie extends Cookie {

		public EncryptedCookie(String name, byte[] encryptedValue, Date expires, String path, String domain, File cookieStore) {
			super(name, encryptedValue, expires, path, domain, cookieStore);
		}

		@Override public boolean isDecrypted() {
			return false;
		}

		@Override public String toString() {
			return "Cookie [name=" + name + " (encrypted)]";
		}

	}

	public abstract class Browser {
		/**
		 * A file that should be used to make a temporary copy of the browser's cookie store
		 */
		protected File cookieStoreCopy = new File(".cookies.db");

		/**
		 * Returns all cookies
		 */
		public Map<String, String> getCookies() {
			return processCookies(cookieStore, null);
		}

		/**
		 * Returns cookies for a given domain
		 */
		public Map<String, String> getCookiesForDomain(String domain) {
			return processCookies(cookieStore, domain);
		}

		/**
		 * Processes all cookies in the cookie store for a given domain or all
		 * domains if domainFilter is null
		 *
		 * @param cookieStore  cookieStore
		 * @param domainFilter 域名
		 * @return cookie set
		 */
		protected abstract Map<String, String> processCookies(File cookieStore, String domainFilter);

		/**
		 * Decrypts an encrypted cookie
		 *
		 * @param encryptedCookie decrypted cookie
		 * @return decrypted cookie
		 */
		protected abstract DecryptedCookie decrypt(EncryptedCookie encryptedCookie);

	}

	public class ChromeBrowser extends Browser {
		public ChromeBrowser(File userHome) {
			userHome(userHome);
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

		/**
		 * Processes all cookies in the cookie store for a given domain or all
		 * domains if domainFilter is null
		 *
		 * @param cookieStore  cookie
		 * @param domainFilter domain
		 * @return decrypted cookie
		 */
		@Override protected Map<String, String> processCookies(File cookieStore, String domainFilter) {
			HashSet<Cookie> cookies = new HashSet<>();
			Connection connection = null;
			try {
				cookieStoreCopy.delete();
				Files.copy(cookieStore.toPath(), cookieStoreCopy.toPath());
				// load the sqlite-JDBC driver using the current class loader
				Class.forName("org.sqlite.JDBC");
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + cookieStoreCopy.getAbsolutePath());
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 seconds
				ResultSet result;
				if (domainFilter == null || domainFilter.isEmpty()) {
					result = statement.executeQuery("select * from cookies");
				} else {
					result = statement.executeQuery("select * from cookies where host_key like \"%" + domainFilter + "%\"");
				}
				while (result.next()) {
					String name = result.getString("name");
					byte[] encryptedBytes = result.getBytes("encrypted_value");
					String path = result.getString("path");
					String domain = result.getString("host_key");
					Date expires = result.getDate("expires_utc");
					EncryptedCookie encryptedCookie = new EncryptedCookie(name, encryptedBytes, expires, path, domain, cookieStore);
					DecryptedCookie decryptedCookie = decrypt(encryptedCookie);
					cookies.add(Objects.requireNonNullElse(decryptedCookie, encryptedCookie));
				}
			} catch (Exception e) {
				e.printStackTrace();
				// if the error message is "out of memory",
				// it probably means no database file is found
			} finally {
				try { // 关闭数据库
					if (connection != null) {
						connection.close();
					}
				} catch (SQLException e) {
					// connection close failed
				}
				cookieStoreCopy.delete(); // 删除备份
			}
			return cookies.parallelStream().filter(cookie -> !Judge.isEmpty(cookie.getValue()))
					.collect(Collectors.toMap(LocalCookies.Cookie::getName, LocalCookies.Cookie::getValue, (e1, e2) -> e1));
		}

		/**
		 * Decrypts an encrypted cookie
		 *
		 * @param encryptedCookie encrypted cookie
		 * @return decrypted cookie
		 */
		@Override protected DecryptedCookie decrypt(EncryptedCookie encryptedCookie) {
			String encryptedKey = getEncryptedKey(userHome);
			byte[] decryptedBytes = encryptedValueDecrypt(encryptedCookie.encryptedValue, encryptedKey);
			if (decryptedBytes == null) {
				return null;
			} else {
				return new DecryptedCookie(encryptedCookie.getName(), encryptedCookie.getEncryptedValue(), new String(decryptedBytes),
						encryptedCookie.getExpires(), encryptedCookie.getPath(), encryptedCookie.getDomain(), encryptedCookie.getCookieStore());
			}
		}

	}

}