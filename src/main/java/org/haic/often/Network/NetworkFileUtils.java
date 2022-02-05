package org.haic.often.Network;

import com.alibaba.fastjson.JSONObject;
import net.lingala.zip4j.model.enums.RandomAccessFileMode;
import org.apache.http.HttpStatus;
import org.haic.often.*;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Multithread.ParameterizedThread;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网络文件 工具类 默认16线程下载
 *
 * @author haicdust
 * @version 1.8.2
 * @since 2021/12/24 23:07
 */
public class NetworkFileUtils {

	private String url; // 请求URL
	private String fileName; // 文件名
	private String referrer; // 上一页
	private String hash; // hash值,md5算法
	private String authorization; // 授权码
	private int MILLISECONDS_SLEEP; // 重试等待时间
	private int retry; // 请求异常重试次数
	private int MAX_THREADS; // 多线程下载
	private int bufferSize; // 缓冲区大小
	private int fileSize; // 文件大小
	private int PIECE_MAX_SIZE; // 块最大值
	private int interval; // 异步访问间隔
	private boolean unlimitedRetry;// 请求异常无限重试
	private boolean errorExit; // 错误退出
	private Proxy proxy; // 代理
	private File storage; // 本地存储文件
	private File conf; // 配置信息文件
	private List<String> infos = new ArrayList<>(); // 文件信息
	private Map<String, String> headers = new HashMap<>(); // headers
	private Map<String, String> cookies = new HashMap<>(); // cookies
	private ExecutorService executorService; // 下载线程池
	private Method method;// 下载模式

	protected NetworkFileUtils() {
		MAX_THREADS = 16; // 默认16线程下载
		interval = 50; // 默认异步访问间隔50毫秒
		bufferSize = 8192; // 默认缓冲区大小
		PIECE_MAX_SIZE = 1048576; // 默认块大小，1M
		headers.put("accept-encoding", "gzip, deflate, br");
		headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
		method = Method.MULTITHREAD;
	}

	/**
	 * 连接 URI
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true) public static NetworkFileUtils connect(@NotNull final String url) {
		return config().url(url);
	}

	/**
	 * 获取新的NetworkFileUtils对象并设置配置文件<br/>
	 * 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param conf down文件
	 * @return new NetworkFileUtils
	 */
	@Contract(pure = true) public static NetworkFileUtils file(@NotNull final String conf) {
		return file(new File(conf));
	}

	/**
	 * 获取新的NetworkFileUtils对象并设置配置文件<br/>
	 * 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param conf down文件
	 * @return new NetworkFileUtils
	 */
	@Contract(pure = true) public static NetworkFileUtils file(@NotNull final File conf) {
		return config().setConf(conf);
	}

	/**
	 * 获取新的 NetworkFileUtils 对象
	 *
	 * @return this
	 */
	@Contract(pure = true) private static NetworkFileUtils config() {
		return new NetworkFileUtils();
	}

	/**
	 * 设置 URI
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true) private NetworkFileUtils url(@NotNull final String url) {
		this.url = url;
		return this;
	}

	/**
	 * 设置 配置文件
	 *
	 * @param conf 配置文件
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils setConf(@NotNull final File conf) {
		this.method = Method.FILE;
		this.conf = conf;
		return this;
	}

	/**
	 * 设置 userAgent
	 *
	 * @param userAgent userAgent
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils userAgent(@NotNull final String userAgent) {
		this.header("User-Agent", userAgent);
		return this;
	}

	/**
	 * 设置 下载模式
	 *
	 * @param method 下载模式
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils method(@NotNull final NetworkFileUtils.Method method) {
		this.method = method;
		return this;
	}

	/**
	 * 设置文件名
	 *
	 * @param filename 文件名
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils filename(@NotNull final String filename) {
		this.fileName = filename;
		return this;
	}

	/**
	 * 设置错误退出
	 *
	 * @param errorExit 启用错误退出
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils errorExit(final boolean errorExit) {
		this.errorExit = errorExit;
		return this;
	}

	/**
	 * 设置上一页
	 *
	 * @param referrer 上一页
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils referrer(@NotNull final String referrer) {
		this.referrer = referrer;
		return this;
	}

	/**
	 * 添加 cookie
	 *
	 * @param name  cookie-名称
	 * @param value cookie-值
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils cookies(@NotNull final String name, @NotNull final String value) {
		cookies.put(name, value);
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies cookie集合
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils cookies(@NotNull final Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

	/**
	 * 添加 请求头
	 *
	 * @param name  请求头-名称
	 * @param value 请求头-值
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils header(@NotNull final String name, @NotNull final String value) {
		headers.put(name, value);
		return this;
	}

	/**
	 * 设置 headers
	 *
	 * @param headers 请求头集合
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils headers(@NotNull final Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置 Socks代理
	 *
	 * @param proxyHost 代理地址
	 * @param proxyPort 代理端口
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils socks(@NotNull final String proxyHost, final int proxyPort) {
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
	@Contract(pure = true) public NetworkFileUtils proxy(@NotNull final String proxyHost, final int proxyPort) {
		proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
		return this;
	}

	/**
	 * 设置 代理
	 *
	 * @param proxy 要使用的代理
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils proxy(@NotNull final Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	/**
	 * 设置 重试次数和重试等待时间
	 *
	 * @param retry              重试次数
	 * @param MILLISECONDS_SLEEP 重试等待时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils retry(final int retry, final int MILLISECONDS_SLEEP) {
		this.retry = retry;
		this.MILLISECONDS_SLEEP = MILLISECONDS_SLEEP;
		return this;
	}

	/**
	 * 设置 重试次数
	 *
	 * @param retry 重试次数
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils retry(final int retry) {
		this.retry = retry;
		return this;
	}

	/**
	 * 设置 请求异常时无限重试
	 *
	 * @param unlimitedRetry     启用无限重试
	 * @param MILLISECONDS_SLEEP 重试等待时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils retry(final boolean unlimitedRetry, final int MILLISECONDS_SLEEP) {
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
	@Contract(pure = true) public NetworkFileUtils retry(final boolean unlimitedRetry) {
		this.unlimitedRetry = unlimitedRetry;
		return this;
	}

	/**
	 * 上传文件时，设置服务器需要的授权码
	 *
	 * @param auth 授权码
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils authorization(@NotNull final String auth) {
		this.authorization = auth;
		return this;
	}

	/**
	 * 设置多线程下载，线程数不小于1，否则抛出异常
	 *
	 * @param nThread 线程最大值
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils multithread(final int nThread) {
		if (nThread < 1) {
			throw new RuntimeException("thread Less than 1");
		}
		this.MAX_THREADS = nThread;
		return this;
	}

	/**
	 * 多线程下载使用并发访问，会导致数据丢失，使用异步访问可以解决数据丢失、数据错误问题，如果存在问题，请增大访问间隔（默认50毫秒,应不低于36毫秒）
	 *
	 * @param interval 异步访问间隔
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils interval(final int interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * 设置写入文件时缓冲区大小
	 *
	 * @param bufferSize 缓冲区大小
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils bufferSize(final int bufferSize) {
		this.bufferSize = bufferSize;
		return this;
	}

	/**
	 * 设置md5算法hash值进行文件完整性效验
	 *
	 * @param hash 文件md5值
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils hash(@NotNull final String hash) {
		this.hash = hash;
		return this;
	}

	/**
	 * 设置多线程分块大小
	 *
	 * @param pieceSize 指定块大小(KB)
	 * @return this
	 */
	@Contract(pure = true) public NetworkFileUtils pieceSize(final int pieceSize) {
		this.PIECE_MAX_SIZE = pieceSize * 1024;
		return this;
	}

	/**
	 * 上传网络文件,返回状态码
	 *
	 * @param filePath 待上传的文件路径
	 * @return 上传状态码
	 */
	@Contract(pure = true) public int Upload(@NotNull final String filePath) {
		return Upload(new File(filePath));
	}

	/**
	 * 上传网络文件,返回状态码
	 *
	 * @param file 待上传的文件对象
	 * @return 上传状态码
	 */
	@Contract(pure = true) public int Upload(@NotNull final File file) {
		if (!Judge.isEmpty(authorization)) {
			headers.put("Authorization", authorization);
		}
		Response response = JsoupUtils.connect(url).headers(headers).header("content-type", "multipart/form-data").file(file).proxy(proxy).cookies(cookies)
				.referrer(referrer).retry(retry, MILLISECONDS_SLEEP).retry(unlimitedRetry).errorExit(errorExit).execute();
		return Judge.isNull(response) ? HttpStatus.SC_REQUEST_TIMEOUT : response.statusCode();
	}

	/**
	 * 下载网络文件,返回状态码
	 *
	 * @param folderPath 文件存放目录路径
	 * @return 下载状态码
	 */
	@Contract(pure = true) public int download(@NotNull final String folderPath) {
		return download(new File(folderPath));
	}

	/**
	 * 下载网络文件,返回状态码
	 *
	 * @return 下载状态码
	 */
	@Contract(pure = true) public int download() {
		String[] value = RunCmd.dos("REG", "QUERY", "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders", "/v",
				"{374DE290-123F-4565-9164-39C4925E467B}").readInfo().split(" ");
		return download(new File(value[value.length - 1]));
	}

	/**
	 * 下载网络文件,返回状态码
	 *
	 * @param folder 文件存放目录对象
	 * @return 下载状态码
	 */
	@Contract(pure = true) public int download(@NotNull final File folder) {
		Response response = null;
		JSONObject fileInfo = new JSONObject();

		switch (method) { // 配置信息
		case FILE -> {
			if (conf.isFile()) { // 如果设置配置文件下载，并且配置文件存在，获取信息
				infos = ReadWriteUtils.orgin(conf).list();
				fileInfo = JSONObject.parseObject(infos.get(0));
				url = fileInfo.getString("URL");
				fileName = fileInfo.getString("fileName");
				fileSize = fileInfo.getInteger("Content-Length");
				hash = fileInfo.getString("X-COS-META-MD5");
				referrer = fileInfo.getString("referrer");
				if (Judge.isEmpty(url) || Judge.isEmpty(fileName) || Judge.isEmpty(fileSize)) {
					throw new RuntimeException("Info is error -> " + conf);
				}
				MAX_THREADS = fileInfo.getInteger("threads");
				method = Method.valueOf(fileInfo.getString("method"));
				storage = new File(folder.getPath(), fileName); // 获取其file对象
				infos.remove(0); // 删除信息行
			} else { // 配置文件不存在，抛出异常
				if (errorExit) {
					throw new RuntimeException("Not found or not is file " + conf);
				} else {
					return 0;
				}
			}
		}
		case FULL, PIECE, MULTITHREAD -> {
			// 获取文件信息
			response = JsoupUtils.connect(url).proxy(proxy).headers(headers).cookies(cookies).referrer(referrer).retry(retry, MILLISECONDS_SLEEP)
					.retry(unlimitedRetry).errorExit(errorExit).execute();
			// 获取URL连接状态
			int statusCode = Judge.isNull(response) ? HttpStatus.SC_REQUEST_TIMEOUT : response.statusCode();
			if (!URIUtils.statusIsOK(statusCode)) {
				return statusCode;
			}
			// 获取文件名
			if (Judge.isEmpty(fileName)) {
				String disposition = Objects.requireNonNull(response).header("Content-Disposition");
				fileName = Judge.isNull(disposition) ?
						TranscodUtils.decodeByURL(
								url.contains("?") ? url.substring(url.lastIndexOf("/") + 1, url.indexOf("?")) : url.substring(url.lastIndexOf("/") + 1)) :
						URIUtils.getFileNameForDisposition(disposition);
			}
			// 文件名排除非法字符
			fileName = FilesUtils.illegalFileName(fileName);
			// 文件名长度检验
			if (fileName.length() > 200) {
				throw new RuntimeException("Error: File name length is greater than 200 URL: " + url + " FileName: " + fileName);
			}
			// 获取待下载文件和配置文件对象
			storage = new File(folder.getPath(), fileName); // 获取其file对象
			// 配置信息文件后缀
			conf = new File(folder.getPath(), fileName + ".haic");
			// 文件已存在，结束下载
			if (storage.isFile() && !conf.exists()) {
				return HttpStatus.SC_OK;
			}
			// 获取文件大小
			String contentLength = response.header("Content-Length");
			fileSize = Judge.isEmpty(contentLength) ? 0 : Integer.parseInt(Objects.requireNonNull(contentLength));
			hash = Judge.isEmpty(hash) ? response.header("X-COS-META-MD5") : hash; // 获取文件MD5
			if (conf.isFile()) { // 读取文件配置信息
				infos = ReadWriteUtils.orgin(conf).list();
				infos.remove(0); // 删除配置行
			} else if (conf.exists()) { // 文件存在但不是文件，抛出异常
				throw new RuntimeException("Not is file " + conf);
			} else { // 创建并写入文件配置信息
				fileInfo.put("URL", url);
				fileInfo.put("fileName", fileName);
				fileInfo.put("Content-Length", String.valueOf(fileSize));
				fileInfo.put("X-COS-META-MD5", hash);
				fileInfo.put("referrer", referrer);
				fileInfo.put("threads", MAX_THREADS);
				fileInfo.put("method", method.getValue());
				if (!ReadWriteUtils.orgin(conf).text(fileInfo.toJSONString())) {
					throw new RuntimeException("Configuration file creation failed");
				}
			}
		}
		}

		method = Judge.isEmpty(fileSize) ? Method.FULL : method;// 如果文件大小获取失败或线程为1，使用全量下载模式
		FilesUtils.createFolder(folder); // 创建文件夹

		int statusCode = 0;
		switch (method) {  // 开始下载
		case FULL -> statusCode = Judge.isNull(response) ? writeFull() : writeFull(response);
		case PIECE -> {
			int PIECE_COUNT = (int) Math.ceil((double) fileSize / (double) PIECE_MAX_SIZE);
			statusCode = MULTITHREAD(PIECE_COUNT, PIECE_MAX_SIZE);
		}
		case MULTITHREAD -> {
			int PIECE_COUNT = Math.min((int) Math.ceil((double) fileSize / (double) PIECE_MAX_SIZE), MAX_THREADS);
			int PIECE_SIZE = (int) Math.ceil((double) fileSize / (double) PIECE_COUNT);
			statusCode = MULTITHREAD(PIECE_COUNT, PIECE_SIZE);
		}
		}
		if (!URIUtils.statusIsOK(statusCode)) { // 验证下载状态
			if (errorExit) {
				throw new RuntimeException("文件下载失败，状态码: " + statusCode + " URL: " + url);
			}
			return statusCode;
		}

		// 效验文件完整性
		String md5;
		if (!Judge.isEmpty(hash) && !(md5 = FilesUtils.getMD5(storage)).equals(hash)) {
			storage.delete(); // 删除下载错误的文件
			if (!ReadWriteUtils.orgin(conf).append(false).text(fileInfo.toJSONString())) { // 重置信息文件
				throw new RuntimeException("Configuration file reset information failed");
			}
			if (errorExit) {
				throw new RuntimeException("文件效验不正确，URL md5:" + hash + " 本地文件 md5: " + md5 + " URL: " + url);
			} else {
				try {
					throw new RuntimeException("文件效验不正确，URL md5:" + hash + " 本地文件 md5: " + md5 + " URL: " + url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return HttpStatus.SC_REQUEST_TIMEOUT;
		}

		conf.delete(); // 删除信息文件
		return HttpStatus.SC_OK;
	}

	@Contract(pure = true) private int MULTITHREAD(int PIECE_COUNT, int PIECE_SIZE) {
		final List<Integer> statusCodes = new CopyOnWriteArrayList<>();
		executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程;
		for (int i = 0; i < PIECE_COUNT; i++, MultiThreadUtils.WaitForThread(interval)) {
			executorService.execute(new ParameterizedThread<>(i, (index) -> { // 执行多线程程
				int start = index * PIECE_SIZE;
				int end = (index + 1 == PIECE_COUNT ? fileSize : (index + 1) * PIECE_SIZE) - 1;
				int statusCode = addPiece(start, end);
				statusCodes.add(statusCode);
				if (!URIUtils.statusIsOK(statusCode)) {
					executorService.shutdownNow(); // 结束未开始的线程，并关闭线程池
				}
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
		// 判断下载状态
		for (int statusCode : statusCodes) {
			if (!URIUtils.statusIsOK(statusCode)) {
				if (errorExit) {
					throw new RuntimeException("文件下载失败，状态码: " + statusCode + " URL: " + url);
				}
				return statusCode;
			}
		}
		return HttpStatus.SC_OK;
	}

	/**
	 * 添加区块线程
	 *
	 * @param start 起始位
	 * @param end   结束位
	 * @return 状态码
	 */
	@Contract(pure = true) private int addPiece(int start, int end) {
		if (infos.contains(start + "-" + end)) {
			return HttpStatus.SC_PARTIAL_CONTENT;
		}
		int statusCode = writePiece(start, end);
		for (int j = 0; !URIUtils.statusIsOK(statusCode) && (j < retry || unlimitedRetry); j++) {
			MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP); // 程序等待
			statusCode = writePiece(start, end);
		}
		return statusCode;
	}

	/**
	 * 全量下载，下载获取文件信息并写入文件
	 *
	 * @return 下载并写入是否成功(状态码)
	 */
	@Contract(pure = true) private int writeFull() {
		return writeFull(
				JsoupUtils.connect(url).proxy(proxy).headers(headers).cookies(cookies).referrer(referrer).retry(retry, MILLISECONDS_SLEEP).retry(unlimitedRetry)
						.errorExit(errorExit).execute());
	}

	/**
	 * 全量下载，下载获取文件信息并写入文件
	 *
	 * @param response 网页Response对象
	 * @return 下载并写入是否成功(状态码)
	 */
	@Contract(pure = true) private int writeFull(final Response response) {
		try (BufferedInputStream bufferedInputStream = response.bodyStream();
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(storage))) {
			IOUtils.copy(bufferedInputStream, bufferedOutputStream, bufferSize);
		} catch (Exception e) {
			return HttpStatus.SC_REQUEST_TIMEOUT;
		}
		return HttpStatus.SC_OK;
	}

	/**
	 * 分块下载，下载获取文件区块信息并写入文件
	 *
	 * @param start 块起始位
	 * @param end   块结束位
	 * @return 下载并写入是否成功(状态码)
	 */
	@Contract(pure = true) private int writePiece(final int start, final int end) {
		Response piece = JsoupUtils.connect(url).proxy(proxy).headers(headers).header("range", "bytes=" + start + "-" + end).cookies(cookies).referrer(referrer)
				.execute();
		return Judge.isNull(piece) ?
				HttpStatus.SC_REQUEST_TIMEOUT :
				URIUtils.statusIsOK(piece.statusCode()) ? writePiece(start, end, piece) : piece.statusCode();
	}

	/**
	 * 下载获取文件区块信息并写入文件
	 *
	 * @param start 块起始位
	 * @param end   块结束位 * @param piece 块Response对象
	 * @return 下载并写入是否成功(状态码)
	 */
	@Contract(pure = true) private int writePiece(final int start, final int end, final Response piece) {
		try (BufferedInputStream inputStream = piece.bodyStream();
				RandomAccessFile output = new RandomAccessFile(storage, RandomAccessFileMode.WRITE.getValue())) {
			output.seek(start);
			byte[] buffer = new byte[bufferSize];
			int count = 0;
			for (int length; !Judge.isMinusOne(length = inputStream.read(buffer)); count += length) {
				output.write(buffer, 0, length);
			}
			if (end - start + 1 == count) {
				ReadWriteUtils.orgin(conf).text(start + "-" + end);
				return piece.statusCode();
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return HttpStatus.SC_REQUEST_TIMEOUT;
	}

	/**
	 * 下载方法名常量<br/>
	 * FILE - 配置文件下载<br/>
	 * FULL - 全量下载模式<br/>
	 * MULTITHREAD - 多线程模式<br/>
	 * INTELLIGENT - 智能多线程模式
	 */
	public enum Method {
		FILE("file"), // 配置文件下载
		FULL("full"),  //全量下载模式
		PIECE("piece"), // 分块多线程模式
		MULTITHREAD("multithread");  // 经典多线程模式

		private final String value;

		Method(final String value) {
			this.value = value;
		}

		public final String getValue() {
			return value;
		}
	}

}