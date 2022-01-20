package org.haic.often;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串常用工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/27 15:12
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

	/**
	 * 换行符
	 */
	public static final String LINE_SEPARATOR = "\n";

	/**
	 * 双引号
	 */
	public static final String DOUBLE_QUOTES = "\"";

	/**
	 * 单引号
	 */
	public static final String APOSTROPHE = "'";

	/**
	 * 等于号
	 */
	public static final String EQUAL_SIGN = "=";

	/**
	 * 替换最后一个匹配的字符串
	 *
	 * @param str         源字符串
	 * @param regex       待匹配的字符串
	 * @param replacement 替换的字符串
	 * @return 替换后的字符串
	 */
	@Contract(pure = true) public static String replaceLast(final @NotNull String str, final @NotNull @NonNls String regex, final String replacement) {
		return str.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

	/**
	 * 生成一个随机邮箱
	 *
	 * @return 随机邮箱
	 */
	@Contract(pure = true) public static String randomEmail() {
		return (RandomStringUtils.randomAlphanumeric(8, 16) + (char) 64 + RandomStringUtils.randomAlphabetic(4, 8) + (char) 46
				+ RandomStringUtils.randomAlphabetic(2, 4)).toLowerCase();
	}

	/**
	 * 生成一个指定域名的随机邮箱
	 *
	 * @param domain 域名
	 * @return 指定域名的随机邮箱
	 */
	@Contract(pure = true) public static String randomEmail(@NotNull String domain) {
		domain = Judge.isEmpty(domain.indexOf(46)) ? domain.substring(1) : domain;
		int count = StringUtils.countMatches(domain, (char) 46);
		if (Judge.isEmpty(count)) {
			throw new RuntimeException(domain + " not is domain");
		}
		String[] subdomain = domain.split("\\.");
		return (RandomStringUtils.randomAlphanumeric(8, 16) + (char) 64 + subdomain[subdomain.length - 2] + (char) 46 + subdomain[subdomain.length
				- 1]).toLowerCase();
	}

	/**
	 * 生成一个随机手机号
	 *
	 * @return 随机手机号
	 */
	@Contract(pure = true) public static String getPhoneNumber() {
		String[] identifier = { "134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159", "182", "183", "184", "187", "188", "178",
				"147", "172", "198", "130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166", "133", "149", "153", "173",
				"177", "180", "181", "189", "199" };
		return identifier[(int) (Math.random() * identifier.length)] + RandomStringUtils.randomNumeric(8);
	}

	/**
	 * 正则提取第一个匹配的字符串
	 *
	 * @param str   待提取的字符串
	 * @param regex 正则表达式
	 * @return 匹配的字符串
	 */
	@Contract(pure = true) public static String extractRegex(final @NotNull String str, final @NotNull @NonNls String regex) {
		String result = null;
		Matcher matcher = Pattern.compile(regex).matcher(str);
		if (matcher.find()) {
			result = matcher.group();
		}
		return result;
	}

	/**
	 * 正则提取所有匹配的字符串
	 *
	 * @param str   待提取的字符串
	 * @param regex 正则表达式
	 * @return 匹配的字符串列表
	 */
	@Contract(pure = true) public static List<String> extractRegexList(final @NotNull String str, final @NotNull @NonNls String regex) {
		List<String> result = new ArrayList<>();
		Matcher matcher = Pattern.compile(regex).matcher(str);
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}

	/**
	 * 正则提取最后一个匹配的字符串
	 *
	 * @param str   待提取的字符串
	 * @param regex 正则表达式
	 * @return 匹配的字符串
	 */
	@Contract(pure = true) public static String extractLastRegex(final @NotNull String str, final @NotNull @NonNls String regex) {
		String result = null;
		Matcher matcher = Pattern.compile(regex).matcher(str);
		while (matcher.find()) {
			result = matcher.group();
		}
		return result;
	}

	/**
	 * 删除字符串头部和尾部符合条件的字符串
	 *
	 * @param str  字符串
	 * @param term 条件字符串
	 * @return 处理后的字符串
	 */
	public static String deletePefixAndSuffix(@NotNull String str, final @NotNull String term) {
		return deleteSuffix(deletePefix(str, term), term);
	}

	/**
	 * 删除字符串头部和尾部符合条件的字符串
	 *
	 * @param str  字符串
	 * @param term 条件字符
	 * @return 处理后的字符串
	 */
	public static String deletePefixAndSuffix(@NotNull String str, final char term) {
		return deletePefixAndSuffix(str, String.valueOf(term));
	}

	/**
	 * 删除字符串头部符合条件的字符串
	 *
	 * @param str  字符串
	 * @param term 条件字符串
	 * @return 处理后的字符串
	 */
	public static String deletePefix(@NotNull String str, final @NotNull String term) {
		while (!Judge.isEmpty(term) && str.startsWith(term)) {
			str = str.substring(term.length());
		}
		return str;
	}

	/**
	 * 删除字符串头部符合条件的字符串
	 *
	 * @param str  字符串
	 * @param term 条件字符
	 * @return 处理后的字符串
	 */
	public static String deletePefix(@NotNull String str, final char term) {
		return deletePefix(str, String.valueOf(term));
	}

	/**
	 * 删除字符串尾部符合条件的字符串
	 *
	 * @param str  字符串
	 * @param term 条件字符串
	 * @return 处理后的字符串
	 */
	public static String deleteSuffix(@NotNull String str, final @NotNull String term) {
		while (!Judge.isEmpty(term) && str.endsWith(term)) {
			str = str.substring(0, str.length() - term.length());
		}
		return str;
	}

	/**
	 * 删除字符串尾部符合条件的字符串
	 *
	 * @param str  字符串
	 * @param term 条件字符
	 * @return 处理后的字符串
	 */
	public static String deleteSuffix(@NotNull String str, final char term) {
		return deleteSuffix(str, String.valueOf(term));
	}

}