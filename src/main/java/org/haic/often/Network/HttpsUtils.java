package org.haic.often.Network;

import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.StreamUtils;
import org.haic.often.URIUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Https 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/3/9 14:26
 */
public class HttpsUtils {

	protected String url; // URL
	protected String params; // 参数
	protected String requestBody; // JSON请求参数
	protected String referrer; // 上一页
	protected int retry; // 请求异常重试次数
	protected int MILLISECONDS_SLEEP; // 重试等待时间
	protected int timeout; // 连接超时时间
	protected boolean unlimitedRetry;// 请求异常无限重试
	protected boolean errorExit; // 错误退出
	protected boolean followRedirects = true; // 重定向
	protected Proxy proxy = Proxy.NO_PROXY; // 代理
	protected HttpURLConnection conn; // HttpURLConnection对象

	protected Map<String, String> headers = new HashMap<>(); // 请求头
	protected List<Integer> excludeErrorStatusCodes = new ArrayList<>(); // 排除错误状态码,不重试

	protected HttpsUtils() {
		headers.put("user-agent", UserAgent.randomChrome()); // 设置随机请求头
		headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
		excludeErrorStatus(HttpStatus.SC_NOT_FOUND, HttpStatus.SC_TOO_MANY_REQUEST);
	}

	/**
	 * 设置 URL
	 *
	 * @param url 请求的URL
	 * @return this
	 */
	@Contract(pure = true) public static HttpsUtils connect(@NotNull String url) {
		return config().url(url);
	}

	/**
	 * 获取新的 HttpsUtils 对象
	 *
	 * @return new HttpsUtils
	 */
	@Contract(pure = true) protected static HttpsUtils config() {
		return new HttpsUtils();
	}

	/**
	 * 设置 URL
	 *
	 * @param url 请求的URL
	 * @return this
	 */
	@Contract(pure = true) protected HttpsUtils url(@NotNull String url) {
		this.url = url;
		return this;
	}

	@Contract(pure = true) public HttpsUtils referrer(@NotNull String referrer) {
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
	 * 设置 userAgent
	 *
	 * @param userAgent userAgent
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils userAgent(@NotNull String userAgent) {
		return header("user-agent", userAgent);
	}

	/**
	 * 访问移动端页面
	 *
	 * @param isPhone true or false
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils isPhone(boolean isPhone) {
		return isPhone ? header("user-agent", UserAgent.randomChromeAsPE()) : header("user-agent", UserAgent.randomChrome());
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
	 * 排除错误码,在指定状态发生时,不进行重试,可指定多个
	 *
	 * @param statusCode 状态码
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils excludeErrorStatus(final int... statusCode) {
		for (int code : statusCode) {
			excludeErrorStatusCodes.add(code);
		}
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
	@Contract(pure = true) public HttpsUtils header(@NotNull String name, @NotNull String value) {
		headers.put(name, value);
		return this;
	}

	/**
	 * 设置 请求头
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils headers(@NotNull Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies cookies
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils cookies(@NotNull Map<String, String> cookies) {
		headers.put("cookie", "");
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
	@Contract(pure = true) public HttpsUtils cookie(@NotNull String name, @NotNull String value) {
		String cookies = headers.get("Cookie");
		cookies = Judge.isEmpty(cookies) ? name + "=" + value : cookies + "&" + name + "=" + value;
		headers.put("Cookie", cookies);
		return this;
	}

	/**
	 * 设置 Socks代理
	 *
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils socks(@NotNull String proxyHost, final int proxyPort) {
		return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort)));
	}

	/**
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils proxy(@NotNull String proxyHost, final int proxyPort) {
		return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
	}

	/**
	 * 设置 代理
	 *
	 * @param proxy 要使用的代理
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils proxy(@NotNull Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	/**
	 * 设置 请求参数
	 *
	 * @param params 请求参数，Map集合 的形式
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils data(@NotNull Map<String, String> params) {
		this.params = "";
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
	@Contract(pure = true) public HttpsUtils data(@NotNull String name, @NotNull String value) {
		params = Judge.isEmpty(params) ? name + "=" + value : params + "&" + name + "=" + value;
		return this;
	}

	/**
	 * 设置 请求参数
	 *
	 * @param params 请求参数，name1=value1 的形式
	 * @return this
	 */
	@Contract(pure = true) public HttpsUtils params(@NotNull String params) {
		this.params = params;
		return this;
	}

	@Contract(pure = true) public HttpsUtils requestBody(@NotNull String requestBody) {
		if (URIUtils.isJson(requestBody)) {
			headers.put("content-type", "application/json;charset=UTF-8");
		}
		this.requestBody = requestBody;
		return this;
	}

	/**
	 * 运行程序，获取 Document 结果
	 *
	 * @return 响应结果
	 */
	@Contract(pure = true) public Document post() {
		return get(HttpMethod.POST);
	}

	/**
	 * 运行程序，获取 Document 结果
	 *
	 * @return 响应结果
	 */
	@Contract(pure = true) public Document get() {
		return get(HttpMethod.GET);
	}

	/**
	 * 运行程序，获取 Document 结果
	 *
	 * @param method 请求方法 HttpMethod
	 * @return 响应结果
	 */
	@Contract(pure = true) public Document get(@NotNull HttpMethod method) {
		String result = execute(method).body();
		return Judge.isEmpty(result) ? null : Jsoup.parse(result);
	}

	/**
	 * 运行程序，获取 HttpsUtils 对象
	 *
	 * @return HttpURLConnection
	 */
	@Contract(pure = true) public HttpsResult execute() {
		return execute(HttpMethod.GET);
	}

	/**
	 * 运行程序，获取 HttpsUtils 对象
	 *
	 * @param method 请求方法 HttpMethod
	 * @return HttpURLConnection
	 */
	@Contract(pure = true) public HttpsResult execute(@NotNull HttpMethod method) {
		executeProgram(method);
		int statusCode = executeProgram(method).statusCode();
		for (int i = 0;
			 !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode) && !excludeErrorStatusCodes.contains(statusCode) && (i < retry
					 || unlimitedRetry); i++) {
			MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP); // 程序等待
			statusCode = executeProgram(method).statusCode();
		}
		if (errorExit && !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode)) {
			throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + url);
		}
		//	new HttpsResult(conn.getURL(), conn);
		return new HttpsResult(url, conn);
	}

	/**
	 * 主程序
	 *
	 * @param method http响应类型 HttpMethod
	 * @return this
	 */
	@Contract(pure = true) protected HttpsResult executeProgram(@NotNull HttpMethod method) {
		return executeProgram(url, method);
	}

	/**
	 * 主程序
	 *
	 * @param url    请求的URL
	 * @param method http响应类型 HttpMethod
	 * @return this
	 */
	@Contract(pure = true) protected HttpsResult executeProgram(@NotNull String url, @NotNull HttpMethod method) {
		try {
			if (method == HttpMethod.GET && !Judge.isEmpty(params)) {
				url = url + "?" + params;
			}

			// 打开和URL之间的连接
			conn = (HttpURLConnection) URIUtils.getURL(url).openConnection(proxy);

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
				conn.setRequestMethod(method.name()); // POST方法
			}

			conn.setReadTimeout(timeout); // 设置超时

			// 设置通用的请求属性
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}

			// 设置上一页
			if (!Judge.isEmpty(referrer)) {
				headers.put("referer", referrer);
			}

			// 建立连接
			if (method == HttpMethod.POST) {
				// 获取URLConnection对象对应的输出流
				try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
					// 发送请求参数
					if (!Judge.isEmpty(params)) {
						out.write(params);
					}
					if (!Judge.isEmpty(requestBody)) {
						out.write(requestBody);
					}
					// flush输出流的缓冲
					out.flush();
				} catch (IOException e) {
					return new HttpsResult(url, conn);
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
			return new HttpsResult(url, conn);
		}

		return new HttpsResult(url, conn);
	}

	protected SSLContext MyX509TrustManagerUtils() {
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

	/**
	 * 方法名常量
	 */
	public enum HttpMethod {
		/**
		 * GET请求方法
		 */
		GET(true),
		/**
		 * POST请求方法
		 */
		POST(true);

		private final boolean hasBody;

		HttpMethod(final boolean hasBody) {
			this.hasBody = hasBody;
		}

		/**
		 * 获得 枚举方法的值
		 *
		 * @return value
		 */
		@Contract(pure = true) public final boolean hasBody() {
			return hasBody;
		}
	}

	/*
	 * HTTPS忽略证书验证,防止高版本jdk因证书算法不符合约束条件,使用继承X509ExtendedTrustManager的方式
	 */
	protected static class MyX509TrustManager extends X509ExtendedTrustManager {

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

	public static class HttpsResult {
		protected String url; // URL
		protected HttpURLConnection conn; // HttpURLConnection对象

		/**
		 * Constructor for the HttpURLConnection.
		 *
		 * @param conn HttpURLConnection
		 */
		protected HttpsResult(String url, HttpURLConnection conn) {
			this.url = url;
			this.conn = conn;
		}

		/**
		 *
		 */
		@Contract(pure = true) public String url() {
			return url;
		}

		/**
		 * 获取 HttpURLConnection
		 *
		 * @return HttpURLConnection
		 */
		@Contract(pure = true) public HttpURLConnection connection() {
			return conn;
		}

		/**
		 * 获取 请求响应代码
		 *
		 * @return 请求响应代码
		 */
		@Contract(pure = true) public int statusCode() {
			int statusCode;
			try {
				statusCode = conn.getResponseCode();
			} catch (IOException e) {
				statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
			}
			return statusCode;
		}

		/**
		 * 获取 请求头的值
		 *
		 * @return 请求头的值
		 */
		@Contract(pure = true) public String header(String name) {
			return conn.getHeaderField(name);
		}

		/**
		 * 获取 请求头
		 *
		 * @return 请求头
		 */
		@Contract(pure = true) public Map<String, String> headers() {
			return conn.getHeaderFields().entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, stringListEntry -> stringListEntry.getValue().toString()));
		}

		/**
		 * 获取 cookies
		 *
		 * @return cookies
		 */
		@Contract(pure = true) public Map<String, String> cookies() {
			Map<String, String> cookies = new HashMap<>();
			for (String str : conn.getHeaderFields().get("set-cookie")) {
				String[] cookie = str.split("=");
				cookies.put(cookie[0], Judge.isEmpty(cookie[1]) ? "" : cookie[1]);
			}
			return cookies;
		}

		/**
		 * 获取 响应正文
		 *
		 * @return 响应正文
		 */
		@Contract(pure = true) public String body() {
			String result;
			try (InputStream inputStream = bodyStream()) {
				result = StreamUtils.stream(inputStream).getString();
			} catch (IOException e) {
				return null;
			}
			return result;
		}

		/**
		 * 获取 响应流
		 *
		 * @return 响应流
		 */
		@Contract(pure = true) public InputStream bodyStream() throws IOException {
			return URIUtils.statusIsOK(statusCode()) ? conn.getInputStream() : conn.getErrorStream();
		}

		/**
		 * 获取 响应流
		 *
		 * @return 响应流
		 */
		@Contract(pure = true) public byte[] bodyAsBytes() {
			byte[] result;
			try (InputStream inputStream = bodyStream()) {
				result = StreamUtils.stream(inputStream).toByteArray();
			} catch (IOException e) {
				return null;
			}
			return result;
		}

	}

}