package org.haic.often;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/2/15 1:05
 */
public class Base64Utils {

	/**
	 * 普通字符串转Base64编码的字符串
	 *
	 * @param str 普通字符串
	 * @return base64编码格式的字符串
	 */
	@NotNull @Contract(pure = true) public static String encryptToBase64(@NotNull String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	/**
	 * Array转Base64编码数组
	 *
	 * @param bytes byte类型
	 * @return Base64编码格式的字符串
	 */
	@Contract(pure = true) public static byte[] encryptToBase64(byte[] bytes) {
		return Base64.getEncoder().encode(bytes);
	}

	/**
	 * 文件转Base64编码数组
	 *
	 * @param filePath 文件路径
	 * @return Base64编码格式的字符串
	 */
	@NotNull @Contract(pure = true) public static String encryptToBase64ByFile(@NotNull String filePath) {
		return encryptToBase64ByFile(new File(filePath));
	}

	/**
	 * 文件转Base64编码的字符串
	 *
	 * @param file 文件
	 * @return Base64编码格式的字符串
	 */
	@NotNull @Contract(pure = true) public static String encryptToBase64ByFile(@NotNull File file) {
		return Base64.getEncoder().encodeToString(ReadWriteUtils.orgin(file).bytes());
	}

	/**
	 * 解密Base64编码的字符串转文件
	 *
	 * @param base64   base64编码格式的字符串
	 * @param filePath 文件路径
	 * @return 写入是否成功
	 */
	@Contract(pure = true) public static boolean decryptByBase64ToFile(byte[] base64, @NotNull String filePath) {
		return decryptByBase64ToFile(base64, new File(filePath));
	}

	/**
	 * 解密Base64编码的字符串转文件
	 *
	 * @param base64 base64编码格式的字符串
	 * @param file   文件
	 * @return 写入是否成功
	 */
	@Contract(pure = true) public static boolean decryptByBase64ToFile(byte[] base64, @NotNull File file) {
		return ReadWriteUtils.orgin(file).bytes(decryptByBase64(base64));
	}

	/**
	 * 解密Base64编码的字符串转文件
	 *
	 * @param base64   base64编码格式的字符串
	 * @param filePath 文件路径
	 * @return 写入是否成功
	 */
	@Contract(pure = true) public static boolean decryptByBase64ToFile(@NotNull String base64, @NotNull String filePath) {
		return decryptByBase64ToFile(base64, new File(filePath));
	}

	/**
	 * 解密Base64编码的字符串转文件
	 *
	 * @param base64 base64编码格式的字符串
	 * @param file   文件
	 * @return 写入是否成功
	 */
	@Contract(pure = true) public static boolean decryptByBase64ToFile(@NotNull String base64, @NotNull File file) {
		return decryptByBase64ToFile(base64.getBytes(), file);
	}

	/**
	 * Base64编码的字符串转普通字符串
	 *
	 * @param base64 base64编码格式的字符串
	 * @return 转换后的字符串
	 */
	@NotNull @Contract(pure = true) public static String decryptByBase64(@NotNull String base64) {
		return new String(decryptByBase64(base64.getBytes()), StandardCharsets.UTF_8);
	}

	/**
	 * Base64编码的字符串转普通字符串
	 *
	 * @param base64      base64编码格式的字符串
	 * @param charsetName 需要转换的字符集编码格式
	 * @return 转换后的字符串
	 */
	@NotNull @Contract(pure = true) public static String decryptByBase64(@NotNull String base64, @NotNull String charsetName) {
		return decryptByBase64(base64, Charset.forName(charsetName));
	}

	/**
	 * Base64编码的字符串转普通字符串
	 *
	 * @param base64  base64编码格式的字符串
	 * @param charset 需要转换的字符集编码格式
	 * @return 转换后的字符串
	 */
	@NotNull @Contract(pure = true) public static String decryptByBase64(@NotNull String base64, Charset charset) {
		return new String(decryptByBase64(base64.getBytes()), charset);
	}

	/**
	 * Base64编码解密
	 *
	 * @param base64 base64编码
	 * @return 转换后的字符串
	 */
	@Contract(pure = true) public static byte[] decryptByBase64(byte[] base64) {
		return Base64.getDecoder().decode(base64);
	}

	/**
	 * 判断字符串是否为Base64编码
	 *
	 * @param str 需要判断的字符串
	 * @return 判断结果
	 */
	@Contract(pure = true) public static boolean isBase64(@NotNull String str) {
		return Pattern.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$", str);
	}

}