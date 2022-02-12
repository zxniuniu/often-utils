package org.haic.often.Network;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.haic.often.FilesUtils;
import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.StringUtils;
import org.haic.often.URIUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Contract;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.Map.Entry;

/**
 * Aria2 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 18:43
 */
public class Aria2Utils {

	protected final String jsonrpc;
	protected Map<String, Map<String, String>> urlsMap = new HashMap<>();
	protected Map<String, String> mixinparams = new HashMap<>();
	protected String aria2RpcUrl;
	protected String token; // 密钥
	protected String proxyHost;
	protected int proxyPort;
	protected String result;

	/**
	 * 默认设置
	 */
	protected Aria2Utils() {
		jsonrpc = "2.0";
		token = "";
		mixinparams.put("all-proxy", "");
	}

	/**
	 * aria2RpcUrl: http://localhost:6800/jsonrpc
	 *
	 * @return this
	 */
	@Contract(pure = true) public static Aria2Utils connect() {
		return connect("localhost", 6800);
	}

	/**
	 * 设置 aria2RpcUrl
	 *
	 * @param host URL
	 * @param port 端口
	 * @return this
	 */
	@Contract(pure = true) public static Aria2Utils connect(final String host, int port) {
		return connect(URIMethod.HTTP, host, port);
	}

	/**
	 * 设置 aria2RpcUrl: localhost:6800
	 *
	 * @param method URI类型
	 * @return this
	 */
	@Contract(pure = true) public static Aria2Utils connect(final URIMethod method) {
		return connect(method, "localhost", 6800);
	}

	/**
	 * 设置 aria2RpcUrl
	 *
	 * @param method URI类型
	 * @param host   URL
	 * @param port   端口
	 * @return this
	 */
	@Contract(pure = true) public static Aria2Utils connect(final URIMethod method, final String host, final int port) {
		return config().setAria2RpcUrl(method.value + "://" + host + ":" + port + "/jsonrpc");
	}

	/**
	 * 获取新的 Aria2Utils 对象
	 *
	 * @return new Aria2Utils
	 */
	@Contract(pure = true) private static Aria2Utils config() {
		return new Aria2Utils();
	}

	/**
	 * 设置 文件夹路径
	 *
	 * @param folderPath 文件夹路径
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils setFolderPath(final String folderPath) {
		this.setMiXinParams("dir", folderPath);
		return this;
	}

	/**
	 * 设置 公共参数
	 *
	 * @param name  key
	 * @param value value
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils setMiXinParams(final String name, final String value) {
		this.mixinparams.put(name, value);
		return this;
	}

	/**
	 * 设置 aria2RpcUrl
	 *
	 * @param aria2RpcUrl RpcUrl
	 * @return this
	 */
	@Contract(pure = true) private Aria2Utils setAria2RpcUrl(final String aria2RpcUrl) {
		this.aria2RpcUrl = aria2RpcUrl;
		return this;
	}

	/**
	 * 设置密钥
	 *
	 * @param token 密钥
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils setToken(final String token) {
		this.token = token;
		return this;
	}

	/**
	 * 设置Aria2代理
	 *
	 * @param proxyHost 代理URL
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils setProxy(final String proxyHost, final int proxyPort) {
		this.mixinparams.put("all-proxy", proxyHost + ":" + proxyPort);
		return this;
	}

	/**
	 * 设置访问PRC接口代理
	 *
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils proxy(final String proxyHost, final int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		return this;
	}

	/**
	 * 添加 URL和参数
	 *
	 * @param url    URL
	 * @param params dir:文件夹路径 out:文件名 referer:上一页
	 * @return this
	 */

	@Contract(pure = true) public Aria2Utils addUrl(final String url, final Map<String, String> params) {
		this.urlsMap.put(url, params);
		return this;
	}

	/**
	 * 添加 Url or Magnet 数组
	 *
	 * @param urls URL数组
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils addUrl(final List<String> urls) {
		for (String url : urls) {
			this.addUrl(url);
		}
		return this;
	}

	/**
	 * 添加 Url or Magnet
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils addUrl(final String url) {
		this.addUrl(url, new HashMap<>());
		return this;
	}

	/**
	 * 添加 Url or Magnet
	 *
	 * @param url      链接
	 * @param filename 文件名
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils addUrl(final String url, final String filename) {
		this.addUrl(url, filename, "*");
		return this;
	}

	/**
	 * 添加 Url or Magnet
	 *
	 * @param url      链接
	 * @param filename 文件名
	 * @param referrer 上一页
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils addUrl(final String url, String filename, final String referrer) {
		Map<String, String> params = new HashMap<>();
		filename = FilesUtils.illegalFileName(filename);
		if (filename.length() > 240) {
			throw new RuntimeException("URL: " + url + " Error: File name length is greater than 240");
		}
		if (!Judge.isEmpty(filename)) {
			params.put("out", filename);
		}
		params.put("referer", referrer);
		this.addUrl(url, params);
		return this;
	}

	/**
	 * 添加 Torrent or Metalink 文件路径
	 *
	 * @param torrentpath 种子路径
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils addTorrent(final String torrentpath) {
		this.addUrl(StringUtils.encryptToBase64(torrentpath));
		return this;
	}

	/**
	 * 添加 Torrent or Metalink 文件路径数组
	 *
	 * @param torrentpath_lists 种子路径数组
	 * @return this
	 */
	@Contract(pure = true) public Aria2Utils addTorrent(final List<String> torrentpath_lists) {
		for (String torrentpath : torrentpath_lists) {
			this.addTorrent(torrentpath);
		}
		return this;
	}

	/**
	 * Socket推送 JSON数据
	 *
	 * @return 返回的json信息
	 */
	@Contract(pure = true) public String send() {
		WebSocketClient socket = new WebSocketClient(URIUtils.getURI(aria2RpcUrl)) {
			@Override @Contract(pure = true) public void onOpen(ServerHandshake handshakedata) {
				send(getJsonArray().toJSONString());
			}

			@Override @Contract(pure = true) public void onMessage(String message) {
				result = message;
				close();
			}

			@Override @Contract(pure = true) public void onError(Exception e) {

			}

			@Override @Contract(pure = true) public void onClose(int code, String reason, boolean remote) {

			}
		};
		if (!Judge.isEmpty(proxyHost) && !Judge.isEmpty(proxyHost)) {
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
			socket.setProxy(proxy);
		}
		socket.connect();
		// 判断连接状态
		while (socket.getReadyState() == ReadyState.NOT_YET_CONNECTED) {
			MultiThreadUtils.WaitForThread(1000);
		}
		return result;
	}

	/**
	 * GET请求 JSON数据
	 *
	 * @return result or webstatus
	 */
	@Contract(pure = true) public String get() {
		Response response = JsoupUtils.connect(aria2RpcUrl).data("params", StringUtils.encryptToBase64(getJsonArray().toJSONString()))
				.proxy(proxyHost, proxyPort).execute();
		int statusCode = Judge.isNull(response) ? 0 : response.statusCode();
		return URIUtils.statusIsOK(statusCode) ? response.body() : String.valueOf(statusCode);
	}

	/**
	 * POST请求 JSON数据
	 *
	 * @return result or webstatus
	 */
	@Contract(pure = true) public String post() {
		Response response = JsoupUtils.connect(aria2RpcUrl).header("Content-Type", "application/json;charset=UTF-8").requestBody(getJsonArray().toJSONString())
				.proxy(proxyHost, proxyPort).execute(Method.POST);
		int statusCode = Judge.isNull(response) ? 0 : response.statusCode();
		return URIUtils.statusIsOK(statusCode) ? response.body() : String.valueOf(statusCode);
	}

	/**
	 * 获取链接类型
	 *
	 * @param url 链接
	 * @return Aria2Method
	 */
	@Contract(pure = true) private Aria2Method getType(final String url) {
		Aria2Method method = Aria2Method.ADD_URI;
		if (url.endsWith("torrent") || StringUtils.isBase64(url)) {
			method = Aria2Method.ADD_TORRENT;
		} else if (url.endsWith("xml")) {
			method = Aria2Method.ADD_METALINK;
		}
		return method;
	}

	/***
	 * 获取 JSONArray
	 *
	 * @return JSONArray
	 */
	@Contract(pure = true) private JSONArray getJsonArray() {
		JSONArray jsonArray = new JSONArray();
		for (Entry<String, Map<String, String>> urlinfo : urlsMap.entrySet()) {
			String url = urlinfo.getKey();
			Map<String, String> params = urlinfo.getValue();
			params.putAll(mixinparams);
			jsonArray.add(getJsonObject(getType(url), url, params));
		}
		return jsonArray;
	}

	/**
	 * 获取Aria2 jsonArray对象
	 *
	 * @return JSONObject
	 */
	@Contract(pure = true) private JSONObject getJsonObject(final Aria2Method method, final String url, final Map<String, String> params) {
		JSONArray jsonArray = new JSONArray();
		jsonArray.add("token:" + token);
		jsonArray.add(Collections.singletonList(url));
		jsonArray.add(params);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", UUID.randomUUID().toString());
		jsonObject.put("jsonrpc", jsonrpc);
		jsonObject.put("method", method.hasBody());
		jsonObject.fluentPut("params", jsonArray);
		return jsonObject;
	}

	/**
	 * 方法名常量
	 */
	public enum Aria2Method {
		ADD_URI("aria2.addUri"), ADD_TORRENT("aria2.addTorrent"), ADD_METALINK("aria2.addMetalink");

		private final String hasBody;

		Aria2Method(final String hasBody) {
			this.hasBody = hasBody;
		}

		@Contract(pure = true) public final String hasBody() {
			return hasBody;
		}
	}

	/**
	 * URI协议常量
	 */
	public enum URIMethod {
		/**
		 * http 协议
		 */
		HTTP("http"),
		/**
		 * https 协议
		 */
		HTTPS("https"),
		/**
		 * ws 协议
		 */
		WS("ws"),
		/**
		 * wws 协议
		 */
		WWS("wws");

		private final String value;

		URIMethod(String value) {
			this.value = value;
		}

		/**
		 * 获得 枚举方法的值
		 *
		 * @return value
		 */
		@Contract(pure = true) public final String getValue() {
			return value;
		}
	}

}