package org.haic.often.Network;

import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Tuple.ThreeTuple;
import org.haic.often.Tuple.TupleUtil;
import org.haic.often.URIUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Request;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

/**
 * Jsoup 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/25 18:40
 */
public class JsoupUtils {

	protected String url; // 请求URL
	protected String referrer; // 上一页
	protected String requestBody; // 请求数据(JSON)
	protected int retry; // 请求异常重试次数
	protected int MILLISECONDS_SLEEP; // 重试等待时间
	protected int timeout; // 超时
	protected int maxBodySize; // 数据大小
	protected boolean unlimitedRetry;// 请求异常无限重试
	protected boolean errorExit; // 错误退出
	protected boolean followRedirects = true; // 重定向
	protected Proxy proxy = Proxy.NO_PROXY; // 代理

	protected Map<String, String> headers = new HashMap<>(); // 请求头参数
	protected Map<String, String> cookies = new HashMap<>(); // cookies
	protected Map<String, String> params = new HashMap<>(); // params
	protected List<Integer> excludeErrorStatusCodes = new ArrayList<>(); // 排除错误状态码,不重试

	protected Request request; // 会话
	protected ThreeTuple<String, String, InputStream> stream; // 数据流

	protected JsoupUtils() {
		headers.put("user-agent", UserAgent.randomChrome()); // 设置随机请求头
		headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
		excludeErrorStatus(HttpStatus.SC_NOT_FOUND, HttpStatus.SC_TOO_MANY_REQUEST);
	}

	/**
	 * 连接 URI
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true) public static JsoupUtils connect(@NotNull String url) {
		return JsoupUtils.config().url(url);
	}

	/**
	 * 获取新的 JsoupUtils 对象
	 *
	 * @return this
	 */
	@Contract(pure = true) protected static JsoupUtils config() {
		return new JsoupUtils();
	}

	/**
	 * 设置 URI
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true) protected JsoupUtils url(@NotNull String url) {
		this.url = url;
		return this;
	}

	/**
	 * 设置 文件
	 *
	 * @param file 文件对象
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils file(@NotNull File file) {
		try {
			return data("file", file.getName(), new BufferedInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * 设置 文件
	 *
	 * @param filePath 文件路径
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils file(@NotNull String filePath) {
		return file(new File(filePath));
	}

	/**
	 * 设置 会话
	 *
	 * @param request 会话
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils request(@NotNull Request request) {
		this.request = request;
		return this;
	}

	/**
	 * 设置 JSON参数
	 *
	 * @param requestBody 数据
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils requestBody(@NotNull String requestBody) {
		if (URIUtils.isJson(requestBody)) {
			headers.put("content-type", "application/json;charset=UTF-8");
		}
		this.requestBody = requestBody;
		return this;
	}

	/**
	 * 设置 userAgent
	 *
	 * @param userAgent userAgent
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils userAgent(@NotNull String userAgent) {
		header("User-Agent", userAgent);
		return this;
	}

	/**
	 * 设置 错误退出
	 *
	 * @param errorExit 启用错误退出
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils errorExit(final boolean errorExit) {
		this.errorExit = errorExit;
		return this;
	}

	/**
	 * 设置 是否重定向
	 *
	 * @param followRedirects 启用重定向
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils followRedirects(final boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	/**
	 * 设置上一页
	 *
	 * @param referrer 上一页
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils referrer(final String referrer) {
		this.referrer = referrer;
		return this;
	}

	/**
	 * 设置 Socks代理
	 *
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils socks(@NotNull String proxyHost, final int proxyPort) {
		proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort)));
		return this;
	}

	/**
	 * 设置 代理
	 *
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils proxy(@NotNull String proxyHost, final int proxyPort) {
		proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
		return this;
	}

	/**
	 * 设置 代理
	 *
	 * @param proxy 要使用的代理
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils proxy(@NotNull Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	/**
	 * 设置 重试次数
	 *
	 * @param retry 重试次数
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils retry(final int retry) {
		this.retry = retry;
		return this;
	}

	/**
	 * 设置 重试次数和重试等待时间
	 *
	 * @param retry              重试次数
	 * @param MILLISECONDS_SLEEP 重试等待时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils retry(final int retry, final int MILLISECONDS_SLEEP) {
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
	@Contract(pure = true) public JsoupUtils retry(final boolean unlimitedRetry, final int MILLISECONDS_SLEEP) {
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
	@Contract(pure = true) public JsoupUtils retry(final boolean unlimitedRetry) {
		this.unlimitedRetry = unlimitedRetry;
		return this;
	}

	/**
	 * 设置 超时
	 *
	 * @param timeout 超时
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils timeout(final int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * 设置 数据大小
	 *
	 * @param maxBodySize 数据大小
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils maxBodySize(final int maxBodySize) {
		this.maxBodySize = maxBodySize;
		return this;
	}

	/**
	 * 设置 新的请求头集合
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils headers(@NotNull Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies cookie集合
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils cookies(@NotNull Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

	/**
	 * 添加 请求头参数
	 *
	 * @param name  标签
	 * @param value 值
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils header(@NotNull String name, @NotNull String value) {
		this.headers.put(name, value);
		return this;
	}

	/**
	 * 添加 cookie
	 *
	 * @param name  标签
	 * @param value 值
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils cookie(@NotNull String name, @NotNull String value) {
		this.cookies.put(name, value);
		return this;
	}

	/**
	 * 设置 params
	 *
	 * @param params 参数集合
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils data(@NotNull Map<String, String> params) {
		this.params = params;
		return this;
	}

	/**
	 * 添加 param
	 *
	 * @param name  标签
	 * @param value 值
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils data(@NotNull String name, @NotNull String value) {
		this.params.put(name, value);
		return this;
	}

	/**
	 * 排除错误码,在指定状态发生时,不进行重试,可指定多个
	 *
	 * @param statusCode 状态码
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils excludeErrorStatus(int... statusCode) {
		for (int code : statusCode) {
			excludeErrorStatusCodes.add(code);
		}
		return this;
	}

	/**
	 * 排除错误码,在指定状态发生时,不进行重试,可指定多个
	 *
	 * @param excludeErrorStatusCodes 状态码列表
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils excludeErrorStatus(List<Integer> excludeErrorStatusCodes) {
		this.excludeErrorStatusCodes = excludeErrorStatusCodes;
		return this;
	}

	@Contract(pure = true) public JsoupUtils data(@NotNull String key, @NotNull String filename, @NotNull InputStream inputStream) {
		stream = TupleUtil.tuple(key, filename, inputStream);
		return this;
	}

	/**
	 * 获取 Document
	 *
	 * @return Document
	 */
	@Contract(pure = true) public Document post() {
		return get(Method.POST);
	}

	/**
	 * 获取 Document
	 *
	 * @return Document
	 */
	@Contract(pure = true) public Document get() {
		return get(Method.GET);
	}

	/**
	 * 获取 Document
	 *
	 * @param method Method类型
	 * @return Document
	 */
	@Contract(pure = true) public Document get(@NotNull Method method) {
		Response response = execute(method);
		return Judge.isNull(response) ? null : Jsoup.parse(response.body());
	}

	/**
	 * 获取 Response
	 *
	 * @return Response
	 */
	@Contract(pure = true) public Response execute() {
		return execute(Method.GET);
	}

	/**
	 * 获取 Response
	 *
	 * @param method Method类型
	 * @return Response
	 */
	@Contract(pure = true) public Response execute(@NotNull Method method) {
		Response response = executeProgram(method);
		int statusCode = Judge.isNull(response) ? HttpStatus.SC_REQUEST_TIMEOUT : Objects.requireNonNull(response).statusCode();
		for (int i = 0;
			 !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode) && !excludeErrorStatusCodes.contains(statusCode) && (i < retry
					 || unlimitedRetry); i++) {
			MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP); // 程序等待
			response = executeProgram(method);
			statusCode = Judge.isNull(response) ? statusCode : Objects.requireNonNull(response).statusCode();
		}
		if (errorExit && !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode)) {
			throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + url);
		}
		return response;
	}

	/**
	 * 主程序
	 *
	 * @param method Method类型
	 * @return Response
	 */
	@Contract(pure = true) protected Response executeProgram(@NotNull Method method) {
		Connection conn = Jsoup.connect(url).headers(headers).proxy(proxy).timeout(timeout).method(method).maxBodySize(maxBodySize)
				.followRedirects(followRedirects);
		conn = Judge.isNull(request) ? conn : conn.request(request);
		conn = cookies.isEmpty() ? conn : conn.cookies(cookies);
		conn = params.isEmpty() ? conn : conn.data(params);
		conn = Judge.isNull(stream) ? conn : conn.data(stream.first, stream.second, stream.third);
		conn = Judge.isEmpty(referrer) ? conn : conn.referrer(referrer);
		conn = Judge.isEmpty(requestBody) ? conn : conn.requestBody(requestBody);
		Response response;
		try {
			response = conn.ignoreContentType(true).ignoreHttpErrors(true).execute();
			if (!Judge.isNull(stream)) {
				stream.third.close();
			}
		} catch (IOException e) {
			return null;
		}
		return response;
	}

}