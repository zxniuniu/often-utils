package org.haic.often;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base64工具类 编码,解码
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/7 17:29
 */
public class Base64Utils {

	/**
	 * 普通字符串转Base64编码格式的字符串
	 *
	 * @param str
	 *            普通字符串
	 * @return base64编码格式的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String encryptToBase64(final @NotNull String str) {
		return encryptToBase64(str, StandardCharsets.UTF_8);
	}

	/**
	 * 普通字符串转Base64编码格式的字符串
	 *
	 * @param str
	 *            普通字符串
	 * @param charsetName
	 *            需要转换的字符集编码格式
	 * @return base64编码格式的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String encryptToBase64(final @NotNull String str, String charsetName) {
		return encryptToBase64(str, Charset.forName(charsetName));
	}

	/**
	 * 普通字符串转Base64编码格式的字符串
	 *
	 * @param str
	 *            普通字符串
	 * @param charset
	 *            需要转换的字符集编码格式
	 * @return base64编码格式的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String encryptToBase64(final @NotNull String str, Charset charset) {
		return Base64.getEncoder().encodeToString(str.getBytes(charset));
	}

	/**
	 * Array转Base64编码格式的字符串
	 *
	 * @param bytes
	 *            byte类型
	 * @return Base64编码格式的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String encryptToBase64(final byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * 文件转Base64编码格式的字符串
	 *
	 * @param filePath
	 *            文件路径
	 * @return Base64编码格式的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String fileEncryptToBase64(final @NotNull String filePath) {
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(Paths.get(filePath));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return encryptToBase64(bytes);
	}

	/**
	 * Base64编码格式的字符串转文件
	 *
	 * @param base64
	 *            base64编码格式的字符串
	 * @param filePath
	 *            文件路径
	 */
	@Contract(pure = true)
	public static void fileEncryptByBase64(final @NotNull String base64, final @NotNull String filePath) {
		try {
			Files.write(Paths.get(filePath), Base64.getDecoder().decode(base64), StandardOpenOption.CREATE);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Base64编码格式的字符串转普通字符串
	 *
	 * @param base64
	 *            base64编码格式的字符串
	 * @return 转换后的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String encryptByBase64(final @NotNull String base64) {
		return encryptByBase64(base64, StandardCharsets.UTF_8);
	}

	/**
	 * Base64编码格式的字符串转普通字符串
	 *
	 * @param base64
	 *            base64编码格式的字符串
	 * @param charsetName
	 *            需要转换的字符集编码格式
	 * @return 转换后的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String encryptByBase64(final @NotNull String base64, final @NotNull String charsetName) {
		return encryptByBase64(base64, Charset.forName(charsetName));
	}

	/**
	 * Base64编码格式的字符串转普通字符串
	 *
	 * @param base64
	 *            base64编码格式的字符串
	 * @param charset
	 *            需要转换的字符集编码格式
	 * @return 转换后的字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String encryptByBase64(final @NotNull String base64, Charset charset) {
		return new String(Base64.getDecoder().decode(base64), charset);
	}

	/**
	 * 判断字符串是否为Base64编码格式
	 *
	 * @param str
	 *            需要判断的字符串
	 * @return 判断结果
	 */
	@Contract(pure = true)
	public static boolean isBase64(final @NotNull String str) {
		return Pattern.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$", str);
	}

}
