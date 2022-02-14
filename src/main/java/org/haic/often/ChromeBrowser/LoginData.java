package org.haic.often.ChromeBrowser;

import org.haic.often.Judge;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/1/20 20:04
 */
public class LoginData {

	/**
	 * 用户文件夹路径 C:\\users\\xxx\\AppData\\Local\\Microsoft\\Edge\\User Data
	 *
	 * @return new ChromeBrowser
	 */
	@Contract(pure = true) public static ChromeBrowser home() {
		return home(new File(System.getProperty("user.home"), "AppData\\Local\\Microsoft\\Edge\\User Data"));
	}

	/**
	 * 自定义 用户文件夹路径
	 *
	 * @param userHome 用户文件夹路径
	 * @return new ChromeBrowser
	 */
	@NotNull @Contract(pure = true) public static ChromeBrowser home(@NotNull String userHome) {
		return home(new File(userHome));
	}

	/**
	 * 自定义 用户文件夹路径
	 *
	 * @param userHome 用户文件夹路径
	 * @return new ChromeBrowser
	 */
	@NotNull @Contract(pure = true) public static ChromeBrowser home(@NotNull File userHome) {
		return new ChromeBrowser(userHome);
	}

	public static abstract class Cookie extends LocalCookies.Cookie {

		protected Date created;

		public Cookie(String name, byte[] encryptedValue, Date created, String domain, File cookieStore) {
			super(name, encryptedValue, domain, cookieStore);
			this.name = name;
			this.encryptedValue = encryptedValue;
			this.created = created;
			this.domain = domain;
			this.cookieStore = cookieStore;
		}

	}

	public static class DecryptedCookie extends Cookie {

		protected String decryptedValue;

		public DecryptedCookie(String name, byte[] encryptedValue, String decryptedValue, Date created, String domain, File cookieStore) {
			super(name, encryptedValue, created, domain, cookieStore);
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

		public EncryptedCookie(String name, byte[] encryptedValue, Date created, String domain, File cookieStore) {
			super(name, encryptedValue, created, domain, cookieStore);
		}

		@Override public boolean isDecrypted() {
			return false;
		}

		@Override public String toString() {
			return "Cookie [name=" + name + " (encrypted)]";
		}

	}

	public abstract static class Browser {
		protected File userHome;
		protected File cookieStore;

		/**
		 * A file that should be used to make a temporary copy of the browser's cookie store
		 */
		protected File cookieStoreCopy = new File(".cookies.db");

		/**
		 * Returns all cookies
		 */
		public Set<Cookie> getLoginDatas() {
			return processCookies(cookieStore, null);
		}

		/**
		 * Returns cookies for a given domain
		 */
		public Map<String, String> getLoginDatasForDomain(String domain) {
			return processCookies(cookieStore, domain).parallelStream().filter(cookie -> !Judge.isEmpty(cookie.getValue()))
					.collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (e1, e2) -> e1));
		}

		/**
		 * Processes all cookies in the cookie store for a given domain or all
		 * domains if domainFilter is null
		 *
		 * @param cookieStore  cookieStore
		 * @param domainFilter 域名
		 * @return cookie set
		 */
		protected abstract Set<Cookie> processCookies(File cookieStore, String domainFilter);

		/**
		 * Decrypts an encrypted cookie
		 *
		 * @param encryptedCookie decrypted cookie
		 * @return decrypted cookie
		 */
		protected abstract DecryptedCookie decrypt(EncryptedCookie encryptedCookie);

	}

	public static class ChromeBrowser extends Browser {

		public ChromeBrowser(File userHome) {
			this.userHome = userHome;
			cookieStore = new File(new File(userHome, "Default"), "Login Data");
		}

		/**
		 * Processes all cookies in the cookie store for a given domain or all
		 * domains if domainFilter is null
		 *
		 * @param cookieStore  cookie
		 * @param domainFilter domain
		 * @return decrypted cookie
		 */
		@Override protected Set<Cookie> processCookies(File cookieStore, String domainFilter) {
			Set<Cookie> cookies = new HashSet<>();
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
					result = statement.executeQuery("select * from logins");
				} else {
					result = statement.executeQuery("select * from logins where signon_realm like \"%" + domainFilter + "%\"");
				}
				while (result.next()) {
					String name = result.getString("username_value");
					byte[] encryptedBytes = result.getBytes("password_value");
					String domain = result.getString("signon_realm");
					Date created = result.getDate("date_created");
					EncryptedCookie encryptedCookie = new EncryptedCookie(name, encryptedBytes, created, domain, cookieStore);
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
					e.printStackTrace();// connection close failed
				}
				cookieStoreCopy.delete(); // 删除备份
			}
			return cookies;
		}

		/**
		 * Decrypts an encrypted cookie
		 *
		 * @param encryptedCookie encrypted cookie
		 * @return decrypted cookie
		 */
		@Override protected DecryptedCookie decrypt(EncryptedCookie encryptedCookie) {
			String encryptedKey = LocalCookies.getEncryptedKey(userHome);
			byte[] decryptedBytes = LocalCookies.ChromeBrowser.encryptedValueDecrypt(encryptedCookie.encryptedValue, encryptedKey);
			if (decryptedBytes == null) {
				return null;
			} else {
				return new DecryptedCookie(encryptedCookie.getName(), encryptedCookie.getEncryptedValue(), new String(decryptedBytes),
						encryptedCookie.getExpires(), encryptedCookie.getDomain(), encryptedCookie.getCookieStore());
			}
		}

	}

}