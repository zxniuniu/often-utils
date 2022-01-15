package org.haic.often.Network;

import org.apache.http.HttpStatus;
import org.haic.often.IOUtils;
import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Tuple.ThreeTuple;
import org.haic.often.Tuple.TupleUtil;
import org.haic.often.URIUtils;
import org.haic.often.UserAgentUtils;
import org.jetbrains.annotations.Contract;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Request;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Jsoup 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/25 18:40
 */
public final class JsoupUtils {

	private String url; // 请求URL
	private String referrer; // 上一页
	private String requestBody; // 请求数据(JSON)
	private String proxyHost; // 代理服务器地址
	private int proxyPort; // 代理服务器端口
	private int retry; // 请求异常重试次数
	private int MILLISECONDS_SLEEP; // 重试等待时间
	private int timeout; // 超时
	private int maxBodySize; // 数据大小
	private boolean unlimitedRetry;// 请求异常无限重试
	private boolean errorExit; // 错误退出
	private boolean followRedirects; // 重定向
	private Map<String, String> headers = new HashMap<>(); // 请求头参数
	private Map<String, String> cookies = new HashMap<>(); // cookies
	private Map<String, String> params = new HashMap<>(); // params
	private Request request; // 会话
	private ThreeTuple<String, String, InputStream> stream; // 数据流
	private final List<Integer> excludeErrorStatusCodes = new ArrayList<>(); // 排除错误状态码,不重试

	private JsoupUtils() {
		followRedirects = true;
		headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
		excludeErrorStatusCodes.add(HttpStatus.SC_NOT_FOUND);
	}

	/**
	 * 连接 URI
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true) public static JsoupUtils connect(final String url) {
		return JsoupUtils.config().url(url);
	}

	/**
	 * 获取新的 JsoupUtils 对象
	 *
	 * @return this
	 */
	@Contract(pure = true) private static JsoupUtils config() {
		return new JsoupUtils();
	}

	/**
	 * 设置 URI
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true) private JsoupUtils url(final String url) {
		this.url = url;
		return this;
	}

	/**
	 * 设置 文件
	 *
	 * @param file 文件对象
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils file(final File file) {
		return data("file", file.getName(), IOUtils.GetFileInputStream(file));
	}

	/**
	 * 设置 文件
	 *
	 * @param filePath 文件路径
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils file(final String filePath) {
		return file(new File(filePath));
	}

	/**
	 * 设置 会话
	 *
	 * @param request 会话
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils request(final Request request) {
		this.request = request;
		return this;
	}

	/**
	 * 设置 JSON参数
	 *
	 * @param requestBody 数据
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils requestBody(final String requestBody) {
		if (URIUtils.isJson(requestBody)) {
			headers.put("Accept", "application/json, text/javascript, */*");
			headers.put("Content-Type", "application/x-www-form-urlencoded");
		}
		this.requestBody = requestBody;
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
	 * 设置 代理
	 *
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils proxy(final String proxyHost, final int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
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
	@Contract(pure = true) public JsoupUtils headers(final Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies cookie集合
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils cookies(final Map<String, String> cookies) {
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
	@Contract(pure = true) public JsoupUtils header(final String name, String value) {
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
	@Contract(pure = true) public JsoupUtils cookie(final String name, String value) {
		this.cookies.put(name, value);
		return this;
	}

	/**
	 * 设置 params
	 *
	 * @param params 参数集合
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils data(final Map<String, String> params) {
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
	@Contract(pure = true) public JsoupUtils data(final String name, final String value) {
		this.params.put(name, value);
		return this;
	}

	/**
	 * 排除错误码,在指定状态发生时,不进行重试,可指定多个
	 *
	 * @param statusCode 状态码
	 * @return this
	 */
	@Contract(pure = true) public JsoupUtils excludeErrorStatus(final int... statusCode) {
		for (int code : statusCode) {
			this.excludeErrorStatusCodes.add(code);
		}
		return this;
	}

	@Contract(pure = true) public JsoupUtils data(final String key, final String filename, final InputStream inputStream) {
		stream = TupleUtil.Tuple(key, filename, inputStream);
		return this;
	}

	/**
	 * 获取 followRedirects
	 *
	 * @return boolean
	 */
	@Contract(pure = true) public boolean followRedirects() {
		return followRedirects;
	}

	/**
	 * 获取 errorExit
	 *
	 * @return boolean
	 */
	@Contract(pure = true) public boolean errorExit() {
		return errorExit;
	}

	/**
	 * 获取 url
	 *
	 * @return String
	 */
	@Contract(pure = true) public String url() {
		return url;
	}

	/**
	 * 获取 referrer
	 *
	 * @return String
	 */
	@Contract(pure = true) public String referrer() {
		return referrer;
	}

	/**
	 * 获取 proxyHost
	 *
	 * @return String
	 */
	@Contract(pure = true) public String proxyHost() {
		return proxyHost;
	}

	/**
	 * 获取 requestBody
	 *
	 * @return String
	 */
	@Contract(pure = true) public String requestBody() {
		return requestBody;
	}

	/**
	 * 获取 proxyPort
	 *
	 * @return int
	 */
	@Contract(pure = true) public int proxyPort() {
		return proxyPort;
	}

	/**
	 * 获取 retry
	 *
	 * @return int
	 */
	@Contract(pure = true) public int retry() {
		return retry;
	}

	/**
	 * 获取 MILLISECONDS_SLEEP
	 *
	 * @return int
	 */
	@Contract(pure = true) public int MILLISECONDS_SLEEP() {
		return MILLISECONDS_SLEEP;
	}

	/**
	 * 获取 timeout
	 *
	 * @return int
	 */
	@Contract(pure = true) public int timeout() {
		return timeout;
	}

	/**
	 * 获取 maxBodySize
	 *
	 * @return int
	 */
	@Contract(pure = true) public int maxBodySize() {
		return maxBodySize;
	}

	/**
	 * 获取 headers
	 *
	 * @return Map
	 */
	@Contract(pure = true) public Map<String, String> headers() {
		return headers;
	}

	/**
	 * 获取 cookies
	 *
	 * @return Map
	 */
	@Contract(pure = true) public Map<String, String> cookies() {
		return cookies;
	}

	/**
	 * 获取 params
	 *
	 * @return Map
	 */
	@Contract(pure = true) public Map<String, String> params() {
		return params;
	}

	/**
	 * 获取 Request
	 *
	 * @return Request
	 */
	@Contract(pure = true) public Request request() {
		return request;
	}

	/**
	 * 获取 Document
	 *
	 * @return Document
	 */
	@Contract(pure = true) public Document GetDocument() {
		return GetDocument(Method.GET);
	}

	/**
	 * 获取 Document
	 *
	 * @param method Method类型
	 * @return Document
	 */
	@Contract(pure = true) public Document GetDocument(final Method method) {
		Response response = GetResponse(method);
		return Judge.isNull(response) ? null : Jsoup.parse(response.body());
	}

	/**
	 * 获取 Response
	 *
	 * @return Response
	 */
	@Contract(pure = true) public Response GetResponse() {
		return GetResponse(Method.GET);
	}

	/**
	 * 获取 Response
	 *
	 * @param method Method类型
	 * @return Response
	 */
	@Contract(pure = true) public Response GetResponse(Method method) {
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
	@Contract(pure = true) private Response executeProgram(final Method method) {
		Connection conn = Jsoup.connect(url).userAgent(UserAgentUtils.random());
		conn = Judge.isNull(request) ? conn : conn.request(request);
		conn = headers.isEmpty() ? conn : conn.headers(headers);
		conn = cookies.isEmpty() ? conn : conn.cookies(cookies);
		conn = params.isEmpty() ? conn : conn.data(params);
		conn = Judge.isNull(stream) ? conn : conn.data(stream.first, stream.second, stream.third);
		conn = Judge.isEmpty(referrer) ? conn : conn.referrer(referrer);
		conn = Judge.isEmpty(proxyHost) || Judge.isEmpty(proxyPort) ? conn : conn.proxy(proxyHost, proxyPort);
		conn = Judge.isEmpty(requestBody) ? conn : conn.requestBody(requestBody);
		conn = conn.timeout(timeout).method(method).maxBodySize(maxBodySize).followRedirects(followRedirects);
		Response response;
		try {
			response = conn.ignoreContentType(true).ignoreHttpErrors(true).execute();
			if (!Judge.isNull(stream)) {
				stream.third.close();
			}
		} catch (final IOException e) {
			return null;
		}
		return response;
	}

}