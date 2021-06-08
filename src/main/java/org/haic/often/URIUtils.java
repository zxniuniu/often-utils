package org.haic.often;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpStatus;
import org.haic.often.Network.HtmlUnitUtils;
import org.haic.often.Network.JsoupUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
	 * @param url
	 *            URL
	 * @return URI对象
	 */
	@NotNull
	@Contract(pure = true)
	public static URI GetURI(final @NotNull String url) {
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
	 * @param url
	 *            URL
	 * @return URL对象
	 */
	@NotNull
	@Contract(pure = true)
	public static URL GetURL(final @NotNull String url) {
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
	 * @param url
	 *            URL
	 * @return 字符串
	 */
	@NotNull
	@Contract(pure = true)
	public static String GetDomain(final @NotNull String url) {
		return GetURI(url).getHost();
	}

	/**
	 * 判断连接是否正常
	 *
	 * @param statusCode
	 *            状态码
	 * @return 连接状态正常 boolean
	 */
	@Contract(pure = true)
	public static boolean statusIsOK(final int statusCode) {
		return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
	}

	/**
	 * 判断连接是否超时,或中断
	 *
	 * @param statusCode
	 *            状态码
	 * @return 连接状态 boolean
	 */
	@Contract(pure = true)
	public static boolean statusIsTimeout(final int statusCode) {
		return statusCode == HttpStatus.SC_REQUEST_TIMEOUT || Judge.isEmpty(statusCode);
	}

	/**
	 * 判断连接是否重定向
	 *
	 * @param statusCode
	 *            状态码
	 * @return 连接状态重定向 boolean
	 */
	@Contract(pure = true)
	public static boolean statusIsRedirect(final int statusCode) {
		return statusCode >= HttpStatus.SC_MULTIPLE_CHOICES && statusCode < HttpStatus.SC_BAD_REQUEST;
	}

	/**
	 * 判断连接是否请求错误
	 *
	 * @param statusCode
	 *            状态码
	 * @return 连接状态 boolean
	 */
	@Contract(pure = true)
	public static boolean statusIsError(final int statusCode) {
		return statusCode >= HttpStatus.SC_BAD_REQUEST && statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}

	/**
	 * 判断字符串是否是JSON
	 *
	 * @param str
	 *            字符串
	 * @return 判断后结果
	 */
	@Contract(pure = true)
	public static boolean isJson(final @NotNull String str) {
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
	 * @param host
	 *            域名或IP
	 * @return 连接状态
	 */
	@Contract(pure = true)
	public static boolean pingIp(final @NotNull String host) {
		return RunCmd.execute("ping " + host + " -n 1 -w " + 5000);
	}

	/**
	 * 获取HOST连接状态
	 *
	 * @param host
	 *            域名或IP
	 * @return 连接状态
	 */
	@Contract(pure = true)
	public static boolean pingHost(final @NotNull String host) {
		return pingHost(host, 80);
	}

	/**
	 * 获取HOST连接状态
	 *
	 * @param host
	 *            域名或IP
	 * @param port
	 *            端口
	 * @return 连接状态
	 */
	@Contract(pure = true)
	public static boolean pingHost(final @NotNull String host, final int port) {
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
	 * 迅雷磁链转换直链
	 *
	 * @param thunder
	 *            迅雷磁力链接
	 * @return URL直链
	 */
	@NotNull
	@Contract(pure = true)
	public static String thunderToURL(@NotNull String thunder) {
		thunder = Base64Utils.encryptByBase64(StringUtils.deleteSuffix(thunder, StringUtils.EQUAL_SIGN).replaceFirst("thunder://", ""), "GBK");
		return thunder.substring(2, thunder.length() - 2);
	}

	/**
	 * 获取蓝奏云URL直链
	 *
	 * @param lanzouUrl
	 *            蓝奏云文件链接
	 * @return 蓝奏云URL直链
	 */
	@Contract(pure = true)
	public static String lanzouStraight(final @NotNull String lanzouUrl) {
		return JsoupUtils.connect(HtmlUnitUtils.connect("https://wws.lanzoux.com" + JsoupUtils.connect(lanzouUrl).retry(true).GetDocument().selectFirst("iframe[class='ifr2']").attr("src"))
				.waitJSTime(1000).retry(true).GetDocument().selectFirst("div[id='go'] a").attr("href")).followRedirects(false).retry(true).GetResponse().header("Location");
	}

	/**
	 * 获取页面文件信息集合
	 *
	 * @param lanzurl
	 *            蓝奏URL
	 * @return 文件信息集合
	 */
	public static Map<String, String> lanzouPageInfos(String lanzurl) {
		Map<String, String> result = new HashMap<>();
		HtmlUnitUtils.connect(lanzurl).waitJSTime(1000).retry(true).GetDocument().select("div[id='name']")
				.forEach(name -> result.put(name.text(), "https://wws.lanzoux.com" + name.select("a").attr("href")));
		return result;
	}

	/**
	 * 获取页面文件信息集合
	 *
	 * @param lanzurl
	 *            蓝奏URL
	 * @param passwd
	 *            访问密码
	 * @return 文件信息集合
	 */
	public static Map<String, String> lanzouPageInfos(String lanzurl, String passwd) {
		String javascript = String.valueOf(JsoupUtils.connect(lanzurl).retry(true).GetDocument().select("script[type='text/javascript']").get(1));
		String infos = javascript.substring(154, javascript.indexOf("隐藏") - 60).replaceAll("'*", "");
		// 获取post参数
		Map<String, String> params = new HashMap<>();
		for (String data : StringUtils.extractRegex(infos.replaceAll("\t*\n*", ""), "data[\\s\\S]*pwd").substring(9).split(",")) {
			String[] entry = data.split(":");
			params.put(entry[0], entry[1]);
		}
		// 获取修正后的参数
		String pgs = StringUtils.extractRegex(infos, "pgs =.*;").replaceAll(" ", "");
		pgs = pgs.substring(pgs.indexOf("=") + 1, pgs.length() - 1);
		String t = StringUtils.extractRegex(infos, params.get("t") + " =.*;").replaceAll(" ", "");
		t = t.substring(t.indexOf("=") + 1, t.length() - 1);
		String k = StringUtils.extractRegex(infos, params.get("k") + " =.*;").replaceAll(" ", "");
		k = k.substring(k.indexOf("=") + 1, k.length() - 1);
		// 修正post参数
		params.put("pg", pgs);
		params.put("t", t);
		params.put("k", k);
		params.put("pwd", passwd);
		// 处理json数据
		String lanzouUrl = "https://wws.lanzoux.com/";
		JSONArray jsonArray = JSONObject.parseObject(JsoupUtils.connect(lanzouUrl+"filemoreajax.php").data(params).retry(true).GetResponse(Connection.Method.POST).body())
				.getJSONArray("text");
		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject info = jsonArray.getJSONObject(i);
			result.put(info.getString("name_all"), lanzouUrl + info.getString("id"));
		}
		return result;
	}

}
