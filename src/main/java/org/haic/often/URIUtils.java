package org.haic.often;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

/**
 * URI工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:13
 */
public class URIUtils {

	/**
	 * 获取URI
	 *
	 * @param url URL
	 * @return URI对象
	 */
	@NotNull @Contract(pure = true) public static URI getURI(@NotNull final String url) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		return Objects.requireNonNull(uri);
	}

	/**
	 * 获取URL
	 *
	 * @param url URL
	 * @return URL对象
	 */
	@NotNull @Contract(pure = true) public static URL getURL(@NotNull final String url) {
		URL uri = null;
		try {
			uri = new URL(url);
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		return Objects.requireNonNull(uri);
	}

	/**
	 * 获取域名
	 *
	 * @param url URL
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String getDomain(@NotNull final String url) {
		return getURI(url).getHost();
	}

	/**
	 * 判断连接是否正常
	 *
	 * @param statusCode 状态码
	 * @return 连接状态正常 boolean
	 */
	@Contract(pure = true) public static boolean statusIsOK(final int statusCode) {
		return statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT;
	}

	/**
	 * 判断连接是否超时,或中断
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	@Contract(pure = true) public static boolean statusIsTimeout(final int statusCode) {
		return statusCode == HttpStatus.SC_REQUEST_TIMEOUT || Judge.isEmpty(statusCode);
	}

	/**
	 * 判断连接是否重定向
	 *
	 * @param statusCode 状态码
	 * @return 连接状态重定向 boolean
	 */
	@Contract(pure = true) public static boolean statusIsRedirect(final int statusCode) {
		return statusCode >= HttpStatus.SC_MULTIPLE_CHOICES && statusCode < HttpStatus.SC_BAD_REQUEST;
	}

	/**
	 * 判断连接是否请求错误
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	@Contract(pure = true) public static boolean statusIsError(final int statusCode) {
		return statusCode >= HttpStatus.SC_BAD_REQUEST && statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode != HttpStatus.SC_REQUEST_TIMEOUT
				&& statusCode != HttpStatus.SC_GONE && statusCode != HttpStatus.SC_NOT_FOUND;
	}

	/**
	 * 判断连接是否请求错误
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	@Contract(pure = true) public static boolean statusIsServerError(final int statusCode) {
		return statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}

	/**
	 * 判断字符串是否是JSON
	 *
	 * @param str 字符串
	 * @return 判断后结果
	 */
	@Contract(pure = true) public static boolean isJson(@NotNull final String str) {
		boolean result;
		try {
			JSONObject.parseObject(str);
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * CMD命令获取IP连接状态
	 *
	 * @param host 域名或IP
	 * @return 连接状态
	 */
	@Contract(pure = true) public static boolean pingIp(@NotNull final String host) {
		return RunCmd.dos("ping", host, "-n", "1", "-w", "5000").execute();
	}

	/**
	 * 获取HOST连接状态
	 *
	 * @param host 域名或IP
	 * @return 连接状态
	 */
	@Contract(pure = true) public static boolean pingHost(@NotNull final String host) {
		return pingHost(host, 80);
	}

	/**
	 * 获取HOST连接状态
	 *
	 * @param host 域名或IP
	 * @param port 端口
	 * @return 连接状态
	 */
	@Contract(pure = true) public static boolean pingHost(@NotNull final String host, final int port) {
		boolean isReachable;
		try (Socket socket = new Socket()) {
			InetSocketAddress endpointSocketAddr = new InetSocketAddress(host, port);
			socket.connect(endpointSocketAddr, 5000);
			isReachable = socket.isConnected();
		} catch (final IOException e) {
			isReachable = false;
		}
		return isReachable;
	}

	/**
	 * 获取 URL请求头Content-Disposition文件名属性
	 *
	 * @param disposition ontent-Disposition
	 * @return 文件名
	 */
	@Contract(pure = true) public static String getFileNameForDisposition(@NotNull final String disposition) {
		String filename = disposition.substring(disposition.lastIndexOf("filename"));
		filename = filename.substring(filename.indexOf("=") + 1).replaceAll("\"", "");
		filename = filename.contains("'") ? filename.substring(filename.lastIndexOf("'") + 1) : filename;
		return TranscodUtils.decodeByURL(filename);
	}

	/**
	 * 迅雷磁链转换直链
	 *
	 * @param thunder 迅雷磁力链接
	 * @return URL直链
	 */
	@NotNull @Contract(pure = true) public static String thunderToURL(@NotNull final String thunder) {
		String thunderUrl = Base64Utils.encryptByBase64(StringUtils.deleteSuffix(thunder, StringUtils.EQUAL_SIGN).replaceFirst("thunder://", ""), "GBK");
		return thunderUrl.substring(2, thunderUrl.length() - 2);
	}

}