package org.haic.often.Network;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpStatus;
import org.haic.often.*;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Multithread.ParameterizedThread;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection.Response;

import com.alibaba.fastjson.JSONObject;

import net.lingala.zip4j.model.enums.RandomAccessFileMode;

/**
 * 网络文件 工具类
 *
 * @author haicdust
 * @version 1.8
 * @since 2020/2/25 18:45
 */
public final class NetworkFileUtils {

	private String url; // 请求URL
	private String fileName; // 文件名
	private String referrer; // 上一页
	private String proxyHost; // 代理服务器地址
	private String hash; // hash值,md5算法
	private String authorization; // 授权码
	private int proxyPort; // 代理服务器端口
	private int MILLISECONDS_SLEEP; // 重试等待时间
	private int retry; // 请求异常重试次数
	private int MAX_THREADS; // 多线程下载
	private int bufferSize; // 缓冲区大小
	private int fileSize; // 文件大小
	private int PIECE_MAX_SIZE; // 块最大值
	private int interval; // 异步访问间隔
	private boolean unlimitedRetry;// 请求异常无限重试
	private boolean errorExit; // 错误退出
	private File file; // 文件
	private File down; // dwon文件
	private List<String> downInfo = new ArrayList<>(); // dwon文件信息
	private Map<String, String> headers = new HashMap<>(); // cookies
	private Map<String, String> cookies = new HashMap<>(); // cookies

	private NetworkFileUtils() {
		MAX_THREADS = 1; // 默认单线程下载
		interval = 32; // 默认异步访问间隔32毫秒
		bufferSize = 8192; // 默认缓冲区大小
		PIECE_MAX_SIZE = 1048576; // 默认块大小，1M
	}

	/**
	 * 连接 URI
	 *
	 * @param url
	 *            链接
	 * @return this
	 */
	@Contract(pure = true)
	public static NetworkFileUtils connect(final @NotNull String url) {
		return config().url(url);
	}

	/**
	 * 设置 URI
	 *
	 * @param url
	 *            链接
	 * @return this
	 */
	@Contract(pure = true)
	private NetworkFileUtils url(final @NotNull String url) {
		this.url = url;
		return this;
	}

	/**
	 * 获取新的NetworkFileUtils对象并设置down文件<br/>
	 * down -> 包含待下载文件的下载信息的文件
	 *
	 * @param down
	 *            down文件
	 * @return new NetworkFileUtils
	 */
	@Contract(pure = true)
	public static NetworkFileUtils down(final String down) {
		return down(new File(down));
	}

	/**
	 * 获取新的NetworkFileUtils对象并设置down文件<br/>
	 * down -> 包含待下载文件的下载信息的文件
	 *
	 * @param down
	 *            down文件
	 * @return new NetworkFileUtils
	 */
	@Contract(pure = true)
	public static NetworkFileUtils down(final File down) {
		return config().setDown(down);
	}

	/**
	 * 设置 down文件
	 *
	 * @param down
	 *            down文件
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils setDown(final File down) {
		this.down = down;
		return this;
	}

	/**
	 * 获取新的 NetworkFileUtils 对象
	 *
	 * @return this
	 */
	@Contract(pure = true)
	private static NetworkFileUtils config() {
		return new NetworkFileUtils();
	}

	/**
	 * 设置文件名
	 *
	 * @param filename
	 *            文件名
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils filename(final @NotNull String filename) {
		this.fileName = filename;
		return this;
	}

	/**
	 * 设置错误退出
	 *
	 * @param errorExit
	 *            启用错误退出
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils errorExit(final boolean errorExit) {
		this.errorExit = errorExit;
		return this;
	}

	/**
	 * 设置上一页
	 *
	 * @param referrer
	 *            上一页
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils referrer(final @NotNull String referrer) {
		this.referrer = referrer;
		return this;
	}

	/**
	 * 添加 cookie
	 *
	 * @param name
	 *            cookie-名称
	 * @param value
	 *            cookie-值
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils cookies(final @NotNull String name, final @NotNull String value) {
		cookies.put(name, value);
		return this;
	}

	/**
	 * 设置 cookies
	 *
	 * @param cookies
	 *            cookie集合
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils cookies(final @NotNull Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

	/**
	 * 添加 请求头
	 *
	 * @param name
	 *            请求头-名称
	 * @param value
	 *            请求头-值
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils header(final @NotNull String name, final @NotNull String value) {
		headers.put(name, value);
		return this;
	}

	/**
	 * 设置 headers
	 *
	 * @param headers
	 *            请求头集合
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils headers(final @NotNull Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * 设置 代理
	 *
	 * @param proxyHost
	 *            代理地址
	 * @param proxyPort
	 *            代理端口
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils proxy(final String proxyHost, final int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		return this;
	}

	/**
	 * 设置 重试次数和重试等待时间
	 *
	 * @param retry
	 *            重试次数
	 * @param MILLISECONDS_SLEEP
	 *            重试等待时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils retry(final int retry, final int MILLISECONDS_SLEEP) {
		this.retry = retry;
		this.MILLISECONDS_SLEEP = MILLISECONDS_SLEEP;
		return this;
	}

	/**
	 * 设置 重试次数
	 *
	 * @param retry
	 *            重试次数
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils retry(final int retry) {
		this.retry = retry;
		return this;
	}

	/**
	 * 设置 请求异常时无限重试
	 *
	 * @param unlimitedRetry
	 *            启用无限重试
	 * @param MILLISECONDS_SLEEP
	 *            重试等待时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils retry(final boolean unlimitedRetry, final int MILLISECONDS_SLEEP) {
		this.unlimitedRetry = unlimitedRetry;
		this.MILLISECONDS_SLEEP = MILLISECONDS_SLEEP;
		return this;
	}

	/**
	 * 设置 请求异常时无限重试
	 *
	 * @param unlimitedRetry
	 *            启用无限重试
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils retry(final boolean unlimitedRetry) {
		this.unlimitedRetry = unlimitedRetry;
		return this;
	}

	/**
	 * 上传文件时，设置服务器需要的授权码
	 *
	 * @param auth
	 *            授权码
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils authorization(String auth) {
		this.authorization = auth;
		return this;
	}

	/**
	 * 设置多线程下载，线程数不小于1，否则抛出异常
	 *
	 * @param nThread
	 *            线程最大值
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils multithread(final int nThread) {
		if (nThread < 1) {
			throw new RuntimeException("thread Less than 1");
		}
		this.MAX_THREADS = nThread;
		return this;
	}

	/**
	 * 多线程下载使用并发访问，会导致数据丢失，使用异步访问可以解决数据丢失、数据错误问题，如果存在问题，请增大访问间隔（默认32毫秒）
	 *
	 * @param interval
	 *            异步访问间隔
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils interval(final int interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * 设置写入文件时缓冲区大小
	 *
	 * @param bufferSize
	 *            缓冲区大小
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils bufferSize(final int bufferSize) {
		this.bufferSize = bufferSize;
		return this;
	}

	/**
	 * 设置md5算法hash值进行文件完整性效验
	 *
	 * @param hash
	 *            文件md5值
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils hash(final @NotNull String hash) {
		this.hash = hash;
		return this;
	}

	/**
	 * 设置多线程分块大小
	 *
	 * @param pieceSize
	 *            指定块大小(KB)
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils pieceSize(final int pieceSize) {
		this.PIECE_MAX_SIZE = pieceSize * 1024;
		return this;
	}

	/**
	 * 上传网络文件,返回状态码
	 *
	 * @param filePath
	 *            待上传的文件路径
	 * @return 上传状态码
	 */
	@Contract(pure = true)
	public int Upload(final @NotNull String filePath) {
		return Upload(new File(filePath));
	}

	/**
	 * 上传网络文件,返回状态码
	 *
	 * @param file
	 *            待上传的文件对象
	 * @return 上传状态码
	 */
	@Contract(pure = true)
	public int Upload(final @NotNull File file) {
		if (!Judge.isEmpty(authorization)) {
			headers.put("Authorization", authorization);
		}
		Response response = JsoupUtils.connect(url).headers(headers).header("Content-Type", "multipart/form-data").file(file).proxy(proxyHost, proxyPort).cookies(cookies).referrer(referrer)
				.retry(retry, MILLISECONDS_SLEEP).retry(unlimitedRetry).errorExit(errorExit).GetResponse();
		return Judge.isNull(response) ? HttpStatus.SC_REQUEST_TIMEOUT : response.statusCode();
	}

	/**
	 * 下载网络文件,返回状态码
	 *
	 * @param folderPath
	 *            文件存放目录路径
	 * @return 下载状态码
	 */
	@Contract(pure = true)
	public int download(final @NotNull String folderPath) {
		return download(new File(folderPath));
	}

	/**
	 * 下载网络文件,返回状态码
	 *
	 * @param folder
	 *            文件存放目录对象
	 * @return 下载状态码
	 */
	@Contract(pure = true)
	public int download(final @NotNull File folder) {
		if (Judge.isNull(down)) {
			// 获取文件信息
			Response response = JsoupUtils.connect(url).proxy(proxyHost, proxyPort).headers(headers).cookies(cookies).referrer(referrer).retry(retry, MILLISECONDS_SLEEP).retry(unlimitedRetry)
					.errorExit(errorExit).GetResponse();
			// 获取URL连接状态
			int statusCode = Judge.isNull(response) ? HttpStatus.SC_REQUEST_TIMEOUT : response.statusCode();
			if (!URIUtils.statusIsOK(statusCode)) {
				return statusCode;
			}
			// 获取文件名
			if (Judge.isEmpty(fileName)) {
				String disposition = Objects.requireNonNull(response).header("Content-Disposition");
				fileName = Judge.isNull(disposition) ? url.contains("?") ? url.substring(url.lastIndexOf("/") + 1, url.indexOf("?")) : url.substring(url.lastIndexOf("/") + 1)
						: disposition.substring(disposition.indexOf("filename=") + 10);
				fileName = TranscodUtils.decodeByURL(fileName);
			}
			// 文件名排除非法字符
			fileName = FilesUtils.illegalFileName(fileName);
			// 文件名长度检验
			if (fileName.length() > 200) {
				throw new RuntimeException("URL: " + url + " Error: File name length is greater than 200");
			}
			// 获取待下载文件和down文件对象
			file = new File(folder.getPath(), fileName); // 获取其file对象
			down = new File(folder.getPath(), fileName + ".down");
			// 文件已存在，结束下载
			if (file.isFile() && !down.exists()) {
				return statusCode;
			}
			fileSize = Integer.parseInt(Objects.requireNonNull(response).header("Content-Length")); // 获取文件大小
			hash = response.header("X-COS-META-MD5"); // 获取文件MD5
			if (down.isFile()) { // 读取down文件信息
				downInfo = ReadWriteUtils.orgin(down).list();
				downInfo.remove(0); // 删除信息行
			} else if (down.exists()) { // 文件存在但不是文件，抛出异常
				throw new RuntimeException("Not is file " + down);
			} else { // 创建并写入down文件信息
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("URL", url);
				jsonObject.put("fileName", fileName);
				jsonObject.put("fileSize", String.valueOf(fileSize));
				jsonObject.put("md5", hash);
				ReadWriteUtils.orgin(down).text(jsonObject.toJSONString());
			}
		} else if (down.isFile()) { // 如果设置down文件下载，并且down文件存在，获取信息
			downInfo = ReadWriteUtils.orgin(down).list();
			JSONObject jsonObject = JSONObject.parseObject(downInfo.get(0));
			url = jsonObject.getString("URL");
			fileName = jsonObject.getString("fileName");
			fileSize = jsonObject.getInteger("fileSize");
			hash = jsonObject.getString("md5");
			if (Judge.isEmpty(url) || Judge.isEmpty(fileName) || Judge.isEmpty(fileSize)) {
				throw new RuntimeException("Info is error -> " + down);
			}
			file = new File(folder.getPath(), fileName); // 获取其file对象
			downInfo.remove(0); // 删除信息行
		} else { // down文件不存在，返回404错误
			if (errorExit) { // 结束抛出
				throw new RuntimeException("Not found or not is file " + down);
			}
			return HttpStatus.SC_NOT_FOUND;
		}
		// 开始下载
		FilesUtils.createFolder(folder);
		List<Integer> statusCodes = new CopyOnWriteArrayList<>();
		int MAX_PIECE_COUNT = (int) Math.ceil((double) fileSize / (double) PIECE_MAX_SIZE);
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程;
		for (int i = 0; i < MAX_PIECE_COUNT; i++) {
			executorService.execute(new ParameterizedThread<>(i, (index) -> { // 执行多线程程
				int statusCode = writePiece(index * PIECE_MAX_SIZE, ((index + 1) == MAX_PIECE_COUNT ? fileSize : (index + 1) * PIECE_MAX_SIZE) - 1);
				for (int j = 0; !URIUtils.statusIsOK(statusCode) && (j < retry || unlimitedRetry); j++) {
					if (!Judge.isEmpty(MILLISECONDS_SLEEP)) {
						MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP); // 程序等待
					}
					statusCode = writePiece(index * PIECE_MAX_SIZE, ((index + 1) == MAX_PIECE_COUNT ? fileSize : (index + 1) * PIECE_MAX_SIZE) - 1);
				}
				statusCodes.add(statusCode);
				if (!URIUtils.statusIsOK(statusCode)) {
					executorService.shutdownNow(); // 结束未开始的线程，并关闭线程池
				}
			}));
			if (MAX_THREADS > 1) {
				MultiThreadUtils.WaitForThread(interval);
			}
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
		// 判断下载状态
		for (int statusCode : statusCodes) {
			if (!URIUtils.statusIsOK(statusCode)) {
				if (errorExit) {
					System.exit(1);
					throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + url);
				}
				return statusCode;
			}
		}
		down.delete(); // 删除信息文件
		// 效验文件完整性
		if (!Judge.isEmpty(hash) && !FilesUtils.GetMD5(file).equals(hash)) {
			file.delete();
			if (errorExit) {
				throw new RuntimeException("文件效验不正确 URL: " + url);
			}
			return HttpStatus.SC_REQUEST_TIMEOUT;
		}
		return HttpStatus.SC_OK;
	}

	/**
	 * 下载获取文件区块信息并写入文件
	 *
	 * @param start
	 *            块起始位
	 * @param end
	 *            块结束位
	 * @return 下载并写入是否成功
	 */
	private int writePiece(int start, int end) {
		String pointer = start + "-" + end;
		if (downInfo.contains(pointer)) {
			return HttpStatus.SC_PARTIAL_CONTENT;
		}
		Response piece = JsoupUtils.connect(url).proxy(proxyHost, proxyPort).headers(headers).header("Range", "bytes=" + pointer).cookies(cookies).referrer(referrer).GetResponse();
		if (!Judge.isNull(piece)) {
			if (URIUtils.statusIsOK(piece.statusCode())) {
				try (BufferedInputStream inputStream = piece.bodyStream(); RandomAccessFile output = new RandomAccessFile(file, RandomAccessFileMode.WRITE.getValue())) {
					output.seek(start);
					byte[] buffer = new byte[bufferSize];
					int sum = 0;
					for (int length; !Judge.isMinusOne(length = inputStream.read(buffer)); sum += length) {
						output.write(buffer, 0, length);
					}
					if (end - start + 1 == sum) {
						ReadWriteUtils.orgin(down).text(pointer);
						return HttpStatus.SC_PARTIAL_CONTENT;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				return piece.statusCode();
			}
		}
		return HttpStatus.SC_REQUEST_TIMEOUT;
	}

}
