package org.haic.often.Network;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.apache.commons.logging.LogFactory;
import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.URIUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * HtmlUnit 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/25 21:05
 */
public class HtmlUnitUtils {

	protected String url; // 请求URL
	protected String proxyHost; // 代理服务器地址
	protected int proxyPort; // 代理服务器端口
	protected String username; // 代理服务器账户
	protected String password; // 代理服务器密码
	protected String referrer; // 上一页
	protected String requestBody; // 请求数据(JSON)
	protected boolean enableCSS; // CSS支持
	protected boolean errorExit; // 错误退出
	protected boolean unlimitedRetry;// 请求异常无限重试
	protected boolean followRedirects = true; // 重定向
	protected boolean isSocksProxy; // 是否Socks代理
	protected boolean isCloseWebClient = true; // 是否关闭WebClient
	protected int waitJSTime = 1000; // JS最大运行时间
	protected int retry; // 请求异常重试次数
	protected int MILLISECONDS_SLEEP; // 重试等待时间
	protected int timeout; // 连接超时时间

	protected Map<String, String> headers = new HashMap<>(); // 请求头参数
	protected Map<String, String> cookies = new HashMap<>(); // cookies
	protected List<NameValuePair> params = new ArrayList<>(); // params
	protected List<Integer> excludeErrorStatusCodes = new ArrayList<>(); // 排除错误状态码,不重试

	protected WebRequest request; // 会话
	protected WebClient webClient; // HtmlUnit
	protected Page page; // Page

	protected HtmlUnitUtils() {
		headers.put("user-agent", UserAgent.randomChrome()); // 设置随机请求头
		headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
		excludeErrorStatus(HttpStatus.SC_NOT_FOUND, HttpStatus.SC_TOO_MANY_REQUEST);
	}

	/**
	 * 获取新的 HtmlUnitUtils 对象
	 *
	 * @return new HtmlUnitUtils
	 */
	@Contract(pure = true) protected static HtmlUnitUtils config() {
		return new HtmlUnitUtils();
	}

	/**
	 * 连接 URL
	 *
	 * @param url URL
	 * @return this
	 */
	@Contract(pure = true) public static HtmlUnitUtils connect(@NotNull String url) {
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
	@Contract(pure = true) public HtmlUnitUtils requestBody(@NotNull String requestBody) {
		if (URIUtils.isJson(requestBody)) {
			headers.put("content-type", "application/json;charset=UTF-8");
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
	@Contract(pure = true) public HtmlUnitUtils referrer(@NotNull String referrer) {
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
	@Contract(pure = true) public HtmlUnitUtils userAgent(@NotNull String userAgent) {
		return header("user-agent", userAgent);
	}

	/**
	 * 访问移动端页面
	 *
	 * @param isPhone true or false
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils isPhone(boolean isPhone) {
		return isPhone ? header("user-agent", UserAgent.randomChromeAsPE()) : header("user-agent", UserAgent.randomChrome());
	}

	/**
	 * 设置 params
	 *
	 * @param params data参数集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils data(@NotNull Map<String, String> params) {
		this.params = new ArrayList<>();
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
	@Contract(pure = true) public HtmlUnitUtils data(@NotNull String name, @NotNull String value) {
		params.add(new NameValuePair(name, value));
		return this;
	}

	/**
	 * 设置 URL
	 *
	 * @param url URL
	 * @return this
	 */
	@Contract(pure = true) protected HtmlUnitUtils url(@NotNull String url) {
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
			excludeErrorStatusCodes.add(code);
		}
		return this;
	}

	@Contract(pure = true) public HtmlUnitUtils request(@NotNull WebRequest request) {
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
	@Contract(pure = true) public HtmlUnitUtils socks(@NotNull String proxyHost, final int proxyPort) {
		isSocksProxy = true;
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
	@Contract(pure = true) public HtmlUnitUtils socks(@NotNull String proxyHost, final int proxyPort, @NotNull String username, @NotNull String password) {
		isSocksProxy = true;
		return proxy(proxyHost, proxyPort, username, password);
	}

	/**
	 * 不需要验证的代理服务器
	 *
	 * @param proxyHost 代理URL
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils proxy(@NotNull String proxyHost, final int proxyPort) {
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
	@Contract(pure = true) public HtmlUnitUtils proxy(@NotNull String proxyHost, final int proxyPort, @NotNull String username, @NotNull String password) {
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
	@Contract(pure = true) public HtmlUnitUtils cookie(@NotNull String name, @NotNull String value) {
		this.cookies.put(name, value);
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies Map集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils cookies(@NotNull Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies Set集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils cookies(@NotNull Set<Cookie> cookies) {
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
	@Contract(pure = true) public HtmlUnitUtils header(@NotNull String name, @NotNull String value) {
		headers.put(name, value);
		return this;
	}

	/**
	 * 设置新的请求头集合
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils headers(@NotNull Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置请求头参数
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils headers(@NotNull List<NameValuePair> headers) {
		for (NameValuePair header : headers) {
			this.headers.put(header.getName(), header.getValue());
		}
		return this;
	}

	/**
	 * 设置 JavaScript 最大运行时间,默认1000毫秒.值为0则不加载JS
	 *
	 * @param waitJSTime JS超时时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils waitJSTime(final int waitJSTime) {
		this.waitJSTime = waitJSTime;
		return this;
	}

	/**
	 * 设置 在会话结束后是否关闭WebClient
	 *
	 * @param isCloseWebClient 状态
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils closeWebClient(final boolean isCloseWebClient) {
		this.isCloseWebClient = isCloseWebClient;
		return this;
	}

	/**
	 * 关闭 WebClient
	 */
	@Contract(pure = true) public void closeWebClient() {
		webClient.close();
	}

	/**
	 * 获取 enableJS
	 *
	 * @return boolean
	 */
	protected boolean enableJS() {
		return !Judge.isEmpty(waitJSTime);
	}

	/**
	 * 获取 cookies
	 *
	 * @return Map
	 */
	@Contract(pure = true) public Map<String, String> cookies() {
		return headers().entrySet().stream().filter(l -> l.getKey().equals("set-cookie")).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	/**
	 * 获取 headers
	 *
	 * @return Map
	 */
	@Contract(pure = true) public Map<String, String> headers() {
		return page.getWebResponse().getResponseHeaders().stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
	}

	/**
	 * 获取 Document
	 *
	 * @param method HttpMethod类型
	 * @return Document
	 */
	@Contract(pure = true) public Document get(@NotNull HttpMethod method) {
		Page page = execute(method);
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
	@Contract(pure = true) public HtmlPage getHtmlPage(@NotNull HttpMethod method) {
		Page page = execute(method);
		return Judge.isNull(page) ? null : (HtmlPage) page;
	}

	/**
	 * 运行并获取 Page
	 *
	 * @return Page
	 */
	@Contract(pure = true) public Page execute() {
		return execute(HttpMethod.GET);
	}

	/**
	 * 运行并获取 Page
	 *
	 * @param method HttpMethod类型
	 * @return Page
	 */
	@Contract(pure = true) public Page execute(@NotNull HttpMethod method) {
		page = executeProgram(method);
		int statusCode = Judge.isNull(page) ? HttpStatus.SC_REQUEST_TIMEOUT : page.getWebResponse().getStatusCode();
		for (int i = 0;
			 !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode) && !excludeErrorStatusCodes.contains(statusCode) && (i < retry
					 || unlimitedRetry); i++) {
			MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP);
			statusCode = Judge.isNull(page) ? HttpStatus.SC_REQUEST_TIMEOUT : page.getWebResponse().getStatusCode();
		}
		if (errorExit && !URIUtils.statusIsOK(statusCode) && !URIUtils.statusIsRedirect(statusCode)) {
			throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + url);
		}
		return page;
	}

	/**
	 * 主程序
	 *
	 * @param method Method类型
	 * @return Response
	 */
	@Contract(pure = true) protected Page executeProgram(@NotNull HttpMethod method) {
		if (Judge.isNull(webClient)) {
			setWebClient(); // 创建HtmlUnit
		}

		webClient.getOptions().setRedirectEnabled(followRedirects); // 是否启用重定向
		webClient.getOptions().setCssEnabled(enableCSS);// 是否启用CSS
		webClient.getOptions().setJavaScriptEnabled(enableJS()); // 是否启用JS
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

		try { // 获得页面
			page = webClient.getPage(Judge.isNull(request) ? getWebRequest(method) : request);
		} catch (IOException e) {
			return page;
		}

		if (!Judge.isEmpty(waitJSTime)) { // 设置JS超时时间
			webClient.waitForBackgroundJavaScriptStartingBefore(waitJSTime);
		}

		// 关闭webClient
		if (isCloseWebClient) {
			webClient.close();
		}

		return page;
	}

	/**
	 * 创建 基本参数的 HtmlUnit WebClient
	 *
	 * @return this
	 */
	@Contract(pure = true) public HtmlUnitUtils setWebClient() {
		// 屏蔽HtmlUnit等系统 log
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http.client").setLevel(Level.OFF);

		webClient = new WebClient(BrowserVersion.CHROME);
		// HtmlUnit 模拟浏览器,浏览器基本设置
		webClient.getCookieManager().setCookiesEnabled(true); // 启动cookie
		webClient.getOptions().setThrowExceptionOnScriptError(false);// 当JS执行出错的时候是否抛出异常
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);// 当HTTP的状态非200时是否抛出异常
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());// 设置支持AJAX

		return this;
	}

	/**
	 * 获取 WebRequest
	 *
	 * @param method HttpMethod类型
	 * @return WebRequest
	 */
	@Contract(pure = true) protected WebRequest getWebRequest(@NotNull HttpMethod method) {
		WebRequest webRequest = new WebRequest(URIUtils.getURL(url), method);
		webRequest.setAdditionalHeaders(headers);    // 设置headers
		if (!params.isEmpty()) {
			webRequest.setRequestParameters(params);
		}
		if (!Judge.isEmpty(requestBody)) {
			webRequest.setRequestBody(requestBody);
		}
		if (!Judge.isEmpty(referrer)) { // 设置请求报文头里的 Referer 字段
			webRequest.setAdditionalHeader("referer", referrer);
		}
		if (!cookies.isEmpty()) { // 设置cookies
			webRequest.setAdditionalHeader("cookie", cookies.toString().replaceAll(",", ";").replace("{", "").replace("}", ""));
		}
		return webRequest;
	}

}