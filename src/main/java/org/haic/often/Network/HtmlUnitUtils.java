package org.haic.often.Network;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.URIUtils;
import org.haic.often.UserAgentUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * HtmlUnit 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/25 21:05
 */
public final class HtmlUnitUtils {

	private final List<NameValuePair> params = new ArrayList<>(); // params
	private final List<Integer> excludeErrorStatusCodes = new ArrayList<>(); // 排除错误状态码,不重试
	private String url; // 请求URL
	private String proxyHost; // 代理服务器地址
	private int proxyPort; // 代理服务器端口
	private String username; // 代理服务器账户
	private String password; // 代理服务器密码
	private String referrer; // 上一页
	private String requestBody; // 请求数据(JSON)
	private boolean enableCSS; // CSS支持
	private boolean errorExit; // 错误退出
	private boolean unlimitedRetry;// 请求异常无限重试
	private boolean followRedirects; // 重定向
	private boolean isSocksProxy; // 是否Socks代理
	private int waitJSTime; // 等待JS加载时间
	private int retry; // 请求异常重试次数
	private int MILLISECONDS_SLEEP; // 重试等待时间
	private int timeout; // 连接超时时间
	private int statusCode; // 状态码
	private Map<String, String> headers = new HashMap<>(); // 请求头参数
	private Map<String, String> cookies = new HashMap<>(); // cookies
	private WebRequest request; // 会话

	private HtmlUnitUtils() {
		followRedirects = true;
		headers.put("User-Agent", UserAgentUtils.random()); // 设置随机请求头
		headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
		excludeErrorStatusCodes.add(HttpStatus.SC_NOT_FOUND);
	}

	/**
	 * 获取新的 HtmlUnitUtils 对象
	 *
	 * @return new HtmlUnitUtils
	 */
	@Contract(pure = true) private static HtmlUnitUtils config() {
		return new HtmlUnitUtils();
	}

	/**
	 * 连接 URL
	 *
	 * @param url URL
	 * @return this
	 */
	@Contract(pure = true) public static HtmlUnitUtils connect(@NotNull final String url) {
		return HtmlUnitUtils.config().url(url);
	}

	/**
	 * 设置 是否启用重定向 (默认启用)
	 *
	 * @param followRedirects 启用重定向
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils followRedirects(final boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	/**
	 * 设置 requestBody
	 *
	 * @param requestBody 数据
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils requestBody(@NotNull final String requestBody) {
		if (URIUtils.isJson(requestBody)) {
			headers.put("Content-Type", "application/json;charset=UTF-8");
		}
		this.requestBody = requestBody;
		return this;
	}

	/**
	 * 上一页 URL
	 *
	 * @param referrer 上一页URL
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils referrer(@NotNull final String referrer) {
		this.referrer = referrer;
		return this;
	}

	/**
	 * 设置错误退出
	 *
	 * @param errorExit 启用错误退出
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils errorExit(final boolean errorExit) {
		this.errorExit = errorExit;
		return this;
	}

	/**
	 * 设置 userAgent
	 *
	 * @param userAgent userAgent
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils userAgent(@NotNull final String userAgent) {
		this.header("User-Agent", userAgent);
		return this;
	}

	/**
	 * 设置 params
	 *
	 * @param params data参数集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils data(@NotNull final Map<String, String> params) {
		this.params.clear();
		for (Entry<String, String> param : params.entrySet()) {
			data(param.getKey(), param.getValue());
		}
		return this;
	}

	/**
	 * 添加 param
	 *
	 * @param name  key
	 * @param value value
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils data(@NotNull final String name, @NotNull final String value) {
		this.params.add(new NameValuePair(name, value));
		return this;
	}

	/**
	 * 设置 URL
	 *
	 * @param url URL
	 * @return this
	 */
	@Contract(pure = true) private HtmlUnitUtils url(@NotNull final String url) {
		this.url = url;
		return this;
	}

	/**
	 * 排除错误码,在指定状态发生时,不进行重试,可指定多个
	 *
	 * @param statusCode 状态码
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils excludeErrorStatus(final int... statusCode) {
		for (int code : statusCode) {
			this.excludeErrorStatusCodes.add(code);
		}
		return this;
	}

	@Contract(pure = true) public HtmlUnitUtils request(@NotNull final WebRequest request) {
		this.request = request;
		return this;
	}

	/**
	 * 设置重试次数
	 *
	 * @param retry 重试次数
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils retry(final int retry) {
		this.retry = retry;
		return this;
	}

	/**
	 * 设置重试次数和重试等待时间
	 *
	 * @param retry              重试次数
	 * @param MILLISECONDS_SLEEP 重试等待时间
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils retry(final int retry, final int MILLISECONDS_SLEEP) {
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
	@Contract(pure = true) public HtmlUnitUtils retry(final boolean unlimitedRetry, final int MILLISECONDS_SLEEP) {
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
	@Contract(pure = true) public HtmlUnitUtils retry(final boolean unlimitedRetry) {
		this.unlimitedRetry = unlimitedRetry;
		return this;
	}

	/**
	 * 不需要验证的代理服务器
	 *
	 * @param proxyHost 代理URL
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils socks(@NotNull final String proxyHost, final int proxyPort) {
		this.isSocksProxy = true;
		return proxy(proxyHost, proxyPort);
	}

	/**
	 * 需要验证的代理服务器
	 *
	 * @param proxyHost 代理URL
	 * @param proxyPort 代理端口
	 * @param username  代理用户名
	 * @param password  代理用户密码
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils socks(@NotNull final String proxyHost, final int proxyPort, @NotNull final String username,
			@NotNull final String password) {
		this.isSocksProxy = true;
		return proxy(proxyHost, proxyPort, username, password);
	}

	/**
	 * 不需要验证的代理服务器
	 *
	 * @param proxyHost 代理URL
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils proxy(@NotNull final String proxyHost, final int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		return this;
	}

	/**
	 * 需要验证的代理服务器
	 *
	 * @param proxyHost 代理URL
	 * @param proxyPort 代理端口
	 * @param username  代理用户名
	 * @param password  代理用户密码
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils proxy(@NotNull final String proxyHost, final int proxyPort, @NotNull final String username,
			@NotNull final String password) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.username = username;
		this.password = password;
		return this;
	}

	/**
	 * 设置使用CSS
	 *
	 * @param enableCSS 启用CSS
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils enableCSS(final boolean enableCSS) {
		this.enableCSS = enableCSS;
		return this;
	}

	/**
	 * 设置超时
	 *
	 * @param timeout 超时时间
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils timeout(final int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * 添加 cookie
	 *
	 * @param name  key
	 * @param value value
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils cookie(@NotNull final String name, @NotNull final String value) {
		this.cookies.put(name, value);
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies Map集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils cookies(@NotNull final Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies Set集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils cookies(@NotNull final Set<Cookie> cookies) {
		for (Cookie cookie : cookies) {
			cookie(cookie.getName(), cookie.getValue());
		}
		return this;
	}

	/**
	 * 添加请求头参数
	 *
	 * @param name  key
	 * @param value value
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils header(@NotNull final String name, @NotNull final String value) {
		this.headers.put(name, value);
		return this;
	}

	/**
	 * 设置新的请求头集合
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils headers(@NotNull final Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置请求头参数
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils headers(@NotNull final List<NameValuePair> headers) {
		for (NameValuePair header : headers) {
			this.headers.put(header.getName(), header.getValue());
		}
		return this;
	}

	/**
	 * 设置 JavaScript 运行时间
	 *
	 * @param waitJSTime JS运行时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils waitJSTime(final int waitJSTime) {
		this.waitJSTime = waitJSTime;
		return this;
	}

	/**
	 * 获取 url
	 *
	 * @return 链接
	 */
	@Contract(pure = true) public String url() {
		return url;
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
	 * 获取 proxyHost
	 *
	 * @return String
	 */
	@Contract(pure = true) public String proxyHost() {
		return proxyHost;
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
	 * 获取 username
	 *
	 * @return String
	 */
	@Contract(pure = true) public String username() {
		return username;
	}

	/**
	 * 获取 password
	 *
	 * @return String
	 */
	@Contract(pure = true) public String password() {
		return password;
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
	 * 获取 requestBody
	 *
	 * @return String
	 */
	@Contract(pure = true) public String requestBody() {
		return requestBody;
	}

	/**
	 * 获取 enableCSS
	 *
	 * @return boolean
	 */
	@Contract(pure = true) public boolean enableCSS() {
		return enableCSS;
	}

	/**
	 * 获取 enableJS
	 *
	 * @return boolean
	 */
	private boolean enableJS() {
		return !Judge.isEmpty(waitJSTime);
	}

	@Contract(pure = true) public int statusCode() {
		return statusCode;
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
	 * 获取 headers
	 *
	 * @return Map
	 */
	@Contract(pure = true) public Map<String, String> headers() {
		return headers;
	}

	/**
	 * 获取 Document
	 *
	 * @param method HttpMethod类型
	 * @return Document
	 */
	@Contract(pure = true) public Document get(final HttpMethod method) {
		Page page = getPage(method);
		return Judge.isNull(page) ? null : page.isHtmlPage() ? Jsoup.parse(((HtmlPage) page).asXml()) : Jsoup.parse(page.getWebResponse().getContentAsString());
	}

	/**
	 * 获取 Document
	 *
	 * @return Document
	 */
	@Contract(pure = true) public Document get() {
		return get(HttpMethod.GET);
	}

	/**
	 * 获取 Document
	 *
	 * @return Document
	 */
	@Contract(pure = true) public Document post() {
		return get(HttpMethod.POST);
	}

	/**
	 * 获取 HtmlPage
	 *
	 * @return HtmlPage
	 */
	@Contract(pure = true) public HtmlPage getHtmlPage() {
		return getHtmlPage(HttpMethod.GET);
	}

	/**
	 * 获取 HtmlPage
	 *
	 * @param method HttpMethod类型
	 * @return HtmlPage
	 */
	@Contract(pure = true) public HtmlPage getHtmlPage(final HttpMethod method) {
		Page page = getPage(method);
		return Judge.isNull(page) ? null : (HtmlPage) page;
	}

	/**
	 * 获取 Page
	 *
	 * @return Page
	 */
	@Contract(pure = true) public Page getPage() {
		return getPage(HttpMethod.GET);
	}

	/**
	 * 获取 Page
	 *
	 * @param method HttpMethod类型
	 * @return Page
	 */
	@Contract(pure = true) public Page getPage(final HttpMethod method) {
		Page page = executeProgram(method);
		for (int i = 0;
			 !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode) && !excludeErrorStatusCodes.contains(statusCode) && (i < retry
					 || unlimitedRetry); i++) {
			MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP);
			page = executeProgram(method);
		}
		if (errorExit && !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode)) {
			throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + url);
		}
		return page;
	}

	/**
	 * 主程序
	 *
	 * @param method HttpMethod类型
	 */
	@Contract(pure = true) private Page executeProgram(final HttpMethod method) {
		// 屏蔽HtmlUnit等系统 log
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http.client").setLevel(Level.OFF);

		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		// HtmlUnit 模拟浏览器,浏览器基本设置
		webClient.getCookieManager().setCookiesEnabled(true); // 启动cookie
		webClient.getOptions().setThrowExceptionOnScriptError(false);// 当JS执行出错的时候是否抛出异常
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);// 当HTTP的状态非200时是否抛出异常
		webClient.getOptions().setRedirectEnabled(followRedirects); // 是否启用重定向
		webClient.getOptions().setCssEnabled(enableCSS);// 是否启用CSS
		webClient.getOptions().setJavaScriptEnabled(enableJS()); // 是否启用JS
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());// 设置支持AJAX
		webClient.getOptions().setTimeout(timeout); // 设置连接超时时间

		if (!Judge.isEmpty(proxyHost) && !Judge.isEmpty(proxyPort)) { // 设置代理
			ProxyConfig proxyConfig = new ProxyConfig();
			proxyConfig.setProxyHost(proxyHost);
			proxyConfig.setProxyPort(proxyPort);
			if (isSocksProxy) { // 设置socks
				proxyConfig.setSocksProxy(true);
			}
			webClient.getOptions().setProxyConfig(proxyConfig);
			// 需要验证的代理服务器
			if (!Judge.isEmpty(username)) {
				((DefaultCredentialsProvider) webClient.getCredentialsProvider()).addCredentials(username, password);
			}
		}

		if (!Judge.isEmpty(referrer)) { // 设置请求报文头里的 Referer 字段
			webClient.addRequestHeader("Referer", referrer);
		}

		if (!cookies.isEmpty()) { // 设置cookies
			webClient.getCookieManager().setCookiesEnabled(true);
			for (Entry<String, String> cookie : cookies.entrySet()) {
				webClient.getCookieManager().addCookie(new Cookie(URIUtils.getDomain(url), cookie.getKey(), cookie.getValue()));
			}
		}

		if (!headers.isEmpty()) { // 设置headers
			for (Map.Entry<String, String> header : headers.entrySet()) {
				webClient.addRequestHeader(header.getKey(), header.getValue());
			}
		}

		Page page;
		try { // 获取网页信息
			page = webClient.getPage(getWebRequest(method));
		} catch (final IOException e) {
			statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
			return null;
		}

		if (!Judge.isEmpty(waitJSTime)) { // 设置JS运行时间
			webClient.waitForBackgroundJavaScript(waitJSTime);
		}

		// 获取headers和cookies
		statusCode = page.getWebResponse().getStatusCode();
		cookies(webClient.getCookieManager().getCookies());
		headers(page.getWebResponse().getResponseHeaders());

		webClient.close(); // 关闭webClient
		return page;
	}

	/**
	 * 获取 WebRequest
	 *
	 * @param method HttpMethod类型
	 * @return WebRequest
	 */
	@Contract(pure = true) private WebRequest getWebRequest(final HttpMethod method) {
		WebRequest webRequest = new WebRequest(URIUtils.getURL(url), method);
		if (!Judge.isNull(request)) {
			webRequest.setAdditionalHeader("Set-Cookie", request.getAdditionalHeader("Set-Cookie"));
		}
		if (!params.isEmpty()) {
			webRequest.setRequestParameters(params);
		}
		if (!Judge.isEmpty(requestBody)) {
			webRequest.setRequestBody(requestBody);
		}
		return webRequest;
	}

}