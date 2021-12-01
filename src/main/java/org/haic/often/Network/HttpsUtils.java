package org.haic.often.Network;

import org.apache.http.HttpStatus;
import org.haic.often.IOUtils;
import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.URIUtils;
import org.haic.often.UserAgentUtils;
import org.jetbrains.annotations.Contract;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Https 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/3/9 14:26
 */
public final class HttpsUtils {
	private String url; // URL
	private String params; // 参数
	private String referrer; // 上一页
	private int retry; // 请求异常重试次数
	private int MILLISECONDS_SLEEP; // 重试等待时间
	private int timeout; // 连接超时时间
	private String proxyHost; // 代理地址
	private int proxyPort; // 代理端口
	private boolean unlimitedRetry;// 请求异常无限重试
	private boolean errorExit; // 错误退出
	private boolean followRedirects; // 重定向
	private HttpURLConnection conn; // HttpURLConnection对象
	private Map<String, String> headers = new HashMap<>(); // 请求头

	/**
	 * 方法名常量
	 */
	public enum HttpMethod {
		GET("GET"), POST("POST");

		private final String hasBody;

		HttpMethod(final String hasBody) {
			this.hasBody = hasBody;
		}

		public final String hasBody() {
			return hasBody;
		}
	}

	private HttpsUtils() {
		followRedirects = true;
		headers.put("user-agent", UserAgentUtils.randomPCUserAgent()); // 设置随机请求头
		headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
	}

	/**
	 * 设置 URL
	 *
	 * @param url 请求的URL
	 * @return this
	 */
	@Contract(pure = true) public static HttpsUtils connect(final String url) {
		return config().url(url);
	}

	/**
	 * 获取新的 HttpsUtils 对象
	 *
	 * @return new HttpsUtils
	 */
	@Contract(pure = true) private static HttpsUtils config() {
		return new HttpsUtils();
	}

	/**
	 * 设置 URL
	 *
	 * @param url 请求的URL
	 * @return this
	 */
	@Contract(pure = true) private HttpsUtils url(final String url) {
		this.url = url;
		return this;
	}

	@Contract(pure = true) public HttpsUtils referrer(final String referrer) {
		this.referrer = referrer;
		return this;
	}

	/**
	 * 设置 是否重定向
	 *
	 * @param followRedirects 启用重定向
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils followRedirects(final boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	/**
	 * 设置 重试次数
	 *
	 * @param retry 重试次数
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils retry(final int retry) {
		this.retry = retry;
		return this;
	}

	/**
	 * 设置 重试次数和重试等待时间
	 *
	 * @param retry              重试次数
	 * @param MILLISECONDS_SLEEP 重试等待时间
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils retry(final int retry, final int MILLISECONDS_SLEEP) {
		this.retry = retry;
		this.MILLISECONDS_SLEEP = MILLISECONDS_SLEEP;
		return this;
	}

	/**
	 * 设置 请求异常时无限重试
	 *
	 * @param unlimitedRetry     启用无限重试
	 * @param MILLISECONDS_SLEEP 重试等待时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils retry(final boolean unlimitedRetry, final int MILLISECONDS_SLEEP) {
		this.unlimitedRetry = unlimitedRetry;
		this.MILLISECONDS_SLEEP = MILLISECONDS_SLEEP;
		return this;
	}

	/**
	 * 设置 请求异常时无限重试
	 *
	 * @param unlimitedRetry 启用无限重试
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils retry(final boolean unlimitedRetry) {
		this.unlimitedRetry = unlimitedRetry;
		return this;
	}

	/**
	 * 设置 超时
	 *
	 * @param timeout 超时时间
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils timeout(final int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * 启用 错误退出
	 *
	 * @param errorExit 错误退出
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils errorExit(final boolean errorExit) {
		this.errorExit = errorExit;
		return this;
	}

	/**
	 * 添加 请求头
	 *
	 * @param name  header键
	 * @param value header值
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils header(final String name, final String value) {
		headers.put(name, value);
		return this;
	}

	/**
	 * 设置 请求头
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils headers(final Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies cookies
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils cookies(final Map<String, String> cookies) {
		headers.put("Cookie", "");
		for (Map.Entry<String, String> cookie : cookies.entrySet()) {
			cookie(cookie.getKey(), cookie.getValue());
		}
		return this;
	}

	/**
	 * 添加 cookie
	 *
	 * @param name  cookie键
	 * @param value cookie值
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils cookie(final String name, final String value) {
		String cookies = headers.get("Cookie");
		cookies = Judge.isEmpty(cookies) ? name + "=" + value : cookies + "&" + name + "=" + value;
		headers.put("Cookie", cookies);
		return this;
	}

	/**
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils proxy(final String proxyHost, final int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		return this;
	}

	/**
	 * 设置 请求参数
	 *
	 * @param params 请求参数，Map集合 的形式
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils data(final Map<String, String> params) {
		this.params = null;
		for (Map.Entry<String, String> param : params.entrySet()) {
			data(param.getKey(), param.getValue());
		}
		return this;
	}

	/**
	 * 设置 请求参数
	 *
	 * @param name  请求参数 name
	 * @param value 请求参数 value
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils data(final String name, final String value) {
		params = Judge.isEmpty(params) ? name + "=" + value : params + "&" + name + "=" + value;
		return this;
	}

	/**
	 * 设置 请求参数
	 *
	 * @param params 请求参数，name1=value1 的形式
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils params(final String params) {
		this.params = params;
		return this;
	}

	/**
	 * 获取 请求头
	 *
	 * @return 请求头
	 */
	@Contract(pure = true) public Map<String, String> headers() {
		return conn.getHeaderFields().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, stringListEntry -> stringListEntry.getValue().toString()));
	}

	/**
	 * 获取 cookies
	 *
	 * @return cookies
	 */
	@Contract(pure = true) public Map<String, String> cookies() {
		Map<String, String> cookies = new HashMap<>();
		Map<String, List<String>> headers = conn.getHeaderFields();
		List<String> list = headers.get("Set-Cookie");
		if (!list.isEmpty()) {
			for (String str : list) {
				String[] cookie = str.split("=");
				cookies.put(cookie[0], Judge.isEmpty(cookie[1]) ? "" : cookie[1]);
			}
		}
		return cookies;
	}

	/**
	 * 获取 conn
	 *
	 * @return conn
	 */
	@Contract(pure = true) public HttpURLConnection conn() {
		return conn;
	}

	/**
	 * 获取 连接状态
	 *
	 * @return 状态码
	 */
	@Contract(pure = true) public int statusCode() {
		int statusCode;
		try {
			statusCode = conn().getResponseCode();
		} catch (final IOException e) {
			statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
		}
		return statusCode;
	}

	/**
	 * 运行程序，获取 Document 结果
	 *
	 * @return 响应结果
	 */
	@Contract(pure = true) public Document GetDocument() {
		return GetDocument(HttpMethod.GET);
	}

	/**
	 * 运行程序，获取 Document 结果
	 *
	 * @param method 请求方法 HttpMethod
	 * @return 响应结果
	 */
	@Contract(pure = true) public Document GetDocument(final HttpMethod method) {
		String htmStr = GetResult(method);
		return Judge.isEmpty(htmStr) ? null : Jsoup.parse(Objects.requireNonNull(htmStr));
	}

	/**
	 * 获取 响应结果
	 *
	 * @return 响应结果
	 */
	@Contract(pure = true) public String GetResult() {
		return GetResult(HttpMethod.GET);

	}

	/**
	 * 获取 响应结果
	 *
	 * @param method 请求方法 HttpMethod
	 * @return 响应结果
	 */
	@Contract(pure = true) public String GetResult(final HttpMethod method) {
		HttpURLConnection conn = execute(method).conn();
		String result;
		try (InputStreamReader inputStream = URIUtils.statusIsOK(conn.getResponseCode()) ?
				new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8) :
				new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)) {
			result = IOUtils.streamToString(inputStream);
		} catch (final IOException e) {
			return null;
		}
		return result;
	}

	/**
	 * 运行程序，获取 HttpsUtils 对象
	 *
	 * @return 响应结果
	 */
	@Contract(pure = true) public HttpsUtils execute() {
		return execute(HttpMethod.GET);
	}

	/**
	 * 运行程序，获取 HttpsUtils 对象
	 *
	 * @param method 请求方法 HttpMethod
	 * @return 响应结果
	 */
	@Contract(pure = true) public HttpsUtils execute(final HttpMethod method) {
		int statusCode = executeProgram(method).statusCode();
		for (int i = 0;!URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode) && (i < retry || unlimitedRetry); i++) {
			MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP); // 程序等待
			statusCode = executeProgram(method).statusCode();
		}
		if (errorExit && !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode)) {
			throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + url);
		}
		return this;
	}

	/**
	 * 主程序
	 *
	 * @param method http响应类型 HttpMethod
	 * @return 响应结果
	 */
	@Contract(pure = true) private HttpsUtils executeProgram(final HttpMethod method) {
		return executeProgram(url, method);
	}

	/**
	 * 主程序
	 *
	 * @param url    请求的URL
	 * @param method http响应类型 HttpMethod
	 * @return 响应结果
	 */
	@Contract(pure = true) private HttpsUtils executeProgram(String url, final HttpMethod method) {
		try {
			if (method == HttpMethod.GET && !Judge.isEmpty(params)) {
				url = url + "?" + params;
			}

			// 打开和URL之间的连接
			if (Judge.isEmpty(proxyHost) || Judge.isEmpty(proxyPort)) {
				conn = (HttpURLConnection) URIUtils.GetURL(url).openConnection();
			} else { // 使用代理模式
				@SuppressWarnings("static-access") Proxy proxy = new Proxy(Proxy.Type.DIRECT.HTTP, new InetSocketAddress(proxyHost, proxyPort));
				conn = (HttpURLConnection) URIUtils.GetURL(url).openConnection(proxy);
			}

			// https 忽略证书验证
			if (url.startsWith("https")) {
				SSLContext ctx = MyX509TrustManagerUtils();
				((HttpsURLConnection) conn).setSSLSocketFactory(ctx.getSocketFactory());
				// 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。
				((HttpsURLConnection) conn).setHostnameVerifier((arg0, arg1) -> true);
			}

			// 发送POST请求必须设置如下两行
			if (method == HttpMethod.POST) {
				conn.setUseCaches(false); // POST请求不能使用缓存（POST不能被缓存）
				conn.setDoOutput(true); // 设置是否向HttpUrlConnction输出，因为这个是POST请求，参数要放在http正文内，因此需要设为true，默认情况下是false
				conn.setDoInput(true); // 设置是否向HttpUrlConnection读入，默认情况下是true
				conn.setRequestMethod(method.hasBody()); // POST方法
			}

			conn.setReadTimeout(timeout); // 设置超时

			// 设置上一页

			if (!headers.isEmpty()) {
				headers.put("Referer", referrer);
			}

			// 设置通用的请求属性
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}

			// 建立连接
			if (method == HttpMethod.POST) {
				// 获取URLConnection对象对应的输出流
				try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
					// 发送请求参数
					out.write(params);
					// flush输出流的缓冲
					out.flush();
				} catch (IOException e) {
					return this;
				}
			} else {
				conn.connect();
			}

			// 重定向
			String redirectUrl;
			if (followRedirects && URIUtils.statusIsRedirect(conn.getResponseCode()) && !Judge.isEmpty(redirectUrl = conn.getHeaderField("Location"))) {
				executeProgram(redirectUrl, method);
			}
		} catch (IOException e) {
			return this;
		}

		return this;
	}

	/*
	 * HTTPS忽略证书验证,防止高版本jdk因证书算法不符合约束条件,使用继承X509ExtendedTrustManager的方式
	 */
	private static class MyX509TrustManager extends X509ExtendedTrustManager {

		@Override public void checkClientTrusted(X509Certificate[] arg0, String arg1) {

		}

		@Override public void checkServerTrusted(X509Certificate[] arg0, String arg1) {

		}

		@Override public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) {

		}

		@Override public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) {

		}

		@Override public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) {

		}

		@Override public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) {

		}

	}

	private SSLContext MyX509TrustManagerUtils() {
		TrustManager[] tm = { new MyX509TrustManager() };
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, tm, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ctx;
	}

}
