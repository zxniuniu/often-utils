package org.haic.often;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编码转换工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:13
 */
public class TranscodUtils {

    /**
     * UrlEncode转中文
     *
     * @param url 链接
     * @return String
     */
    @NotNull
    @Contract(pure = true)
    public static String decodeByURL(@NotNull final String url) {
        String prevURL = "";
        String decodeURL = url;
        while (!prevURL.equals(decodeURL)) {
            prevURL = decodeURL;
            decodeURL = URLDecoder.decode(decodeURL, StandardCharsets.UTF_8);
        }
        return decodeURL;
    }

    /**
     * 中文转UrlEncode
     *
     * @param url 链接
     * @return String
     */
    @NotNull
    @Contract(pure = true)
    public static String encodeByURL(@NotNull final String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    /**
     * 含有unicode 的字符串转一般字符串
     *
     * @param unicodeStr 混有 Unicode 的字符串
     * @return 一般字符串
     */
    @NotNull
    @Contract(pure = true)
    public static String unicodeStr2String(@NotNull final String unicodeStr) {
        final String regex = "\\\\u[a-f0-9A-F]{1,4}";
        int count = 0;
        Matcher matcher = Pattern.compile(regex).matcher(unicodeStr);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String oldChar = matcher.group();// 原本的Unicode字符
            String newChar = unicode2String(oldChar);// 转换为普通字符
            // 在遇见重复出现的unicode代码的时候会造成从源字符串获取非unicode编码字符的时候截取索引越界等
            int index = matcher.start();
            result.append(unicodeStr, count, index);// 添加前面不是unicode的字符
            result.append(newChar);// 添加转换后的字符
            count = index + oldChar.length();// 统计下标移动的位置
        }
        result.append(unicodeStr, count, unicodeStr.length());// 添加末尾不是Unicode的字符
        return String.valueOf(result);
    }

    /**
     * 字符串转换unicode
     *
     * @param str 一般字符串
     * @return Unicode字符串
     */
    @NotNull
    @Contract(pure = true)
    public static String string2Unicode(@NotNull final String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            result.append("\\u").append(Integer.toHexString(str.charAt(i))); // 转换为unicode
        }
        return String.valueOf(result);
    }

    /**
     * unicode 转字符串
     *
     * @param unicode 全为 Unicode 的字符串
     * @return 一般字符串
     */
    @NotNull
    @Contract(pure = true)
    public static String unicode2String(@NotNull final String unicode) {
        StringBuilder result = new StringBuilder();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            result.append((char) Integer.parseInt(hex[i], 16)); // 转换出每一个代码点
        }
        return String.valueOf(result);
    }

    /**
     * gbk编码格式字符串转换为utf8编码格式字符串
     *
     * @param str gbk编码格式字符串
     * @return utf8编码格式字符串
     */
    @NotNull
    @Contract(pure = true)
    public static String utf8ByGBK(@NotNull final String str) {
        String result = null;
        try {
            result = new String(str.getBytes("GBK"), StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(result);
    }

    /**
     * utf8编码格式字符串转换为gbk编码格式字符串
     *
     * @param str utf8编码格式字符串
     * @return gbk编码格式字符串
     */
    @NotNull
    @Contract(pure = true)
    public static String utf8ToGBK(@NotNull final String str) {
        String result = null;
        try {
            result = new String(str.getBytes(StandardCharsets.UTF_8), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(result);
    }

}