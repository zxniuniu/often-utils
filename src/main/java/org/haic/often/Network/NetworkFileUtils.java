package org.haic.often.Network;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.haic.often.*;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Multithread.ParameterizedThread;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection.Response;

import net.lingala.zip4j.model.enums.RandomAccessFileMode;

/**
 * 网络文件 工具类
 *
 * @author haicdust
 * @version 1.5
 * @since 2020/2/25 18:45
 */
public final class NetworkFileUtils {

	private String url; // 请求URL
	private String filename; // 文件名
	private String referrer; // 上一页
	private String proxyHost; // 代理服务器地址
	private String hash; // hash值,md5算法
	private int proxyPort; // 代理服务器端口
	private int MILLISECONDS_SLEEP; // 重试等待时间
	private int retry; // 请求异常重试次数
	private int MAX_THREADS; // 多线程下载
	private int bufferSize; // 缓冲区大小
	private int PIECE_MAX_SIZE; // 块最大值
	private boolean unlimitedRetry;// 请求异常无限重试
	private boolean errorExit; // 错误退出
	private Map<String, String> cookies = new HashMap<>(); // cookies
	private List<String> downInfo; // dwon文件信息

	private NetworkFileUtils() {
		bufferSize = 8192; // 默认缓冲区大小
		MAX_THREADS = 16; // 默认16线程
		PIECE_MAX_SIZE = 1048576; // 默认块大小
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
	 * 获取新的 NetworkFileUtils 对象
	 *
	 * @return this
	 */
	@Contract(pure = true)
	private static NetworkFileUtils config() {
		return new NetworkFileUtils();
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
	 * 设置文件名
	 *
	 * @param filename
	 *            文件名
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils filename(final @NotNull String filename) {
		this.filename = filename;
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
	 * 设置 cookies
	 *
	 * @param cookies
	 *            cookie集合
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils coolkies(final @NotNull Map<String, String> cookies) {
		this.cookies = cookies;
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
	 * 设置多线程下载，默认16线程
	 *
	 * @param thread
	 *            线程最大值
	 * @return this
	 */
	@Contract(pure = true)
	public NetworkFileUtils multithread(final int thread) {
		this.MAX_THREADS = thread;
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
		Response response = JsoupUtils.connect(url).header("Content-Type", "multipart/form-data").file(file).proxy(proxyHost, proxyPort).cookies(cookies).referrer(referrer)
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
		Response response = JsoupUtils.connect(url).proxy(proxyHost, proxyPort).cookies(cookies).referrer(referrer).retry(retry, MILLISECONDS_SLEEP).retry(unlimitedRetry).errorExit(errorExit)
				.GetResponse();
		int statusCode = Judge.isNull(response) ? HttpStatus.SC_REQUEST_TIMEOUT : response.statusCode();
		if (!URIUtils.statusIsOK(statusCode)) {
			return statusCode;
		}
		if (Judge.isEmpty(filename)) {
			String content_disposition = Objects.requireNonNull(response).header("Content-Disposition");
			filename = Judge.isNull(content_disposition) ? url.contains("?") ? url.substring(url.lastIndexOf("/") + 1, url.indexOf("?")) : url.substring(url.lastIndexOf("/") + 1)
					: content_disposition.substring(content_disposition.indexOf("filename=") + 10);
		}
		filename = FilesUtils.illegalFileName(TranscodUtils.decodeByURL(filename));
		if (filename.length() > 200) {
			throw new RuntimeException("URL: " + url + " Error: File name length is greater than 200");
		}
		FilesUtils.createFolder(folder);
		File down = new File(folder.getPath(), filename + ".down"); // 获取其file对象
		File file = new File(folder.getPath(), filename); // 获取其file对象
		if (file.isFile() && !down.exists()) {
			return statusCode;
		}
		downInfo = ReadWriteUtils.orgin(down).list();
		int fileSize = Integer.parseInt(response.header("Content-Length"));
		if (fileSize > PIECE_MAX_SIZE * MAX_THREADS) {
			int MAX_PIECE_COUNT = (int) Math.ceil((double) fileSize / (double) PIECE_MAX_SIZE);
			List<Boolean> result = new CopyOnWriteArrayList<>();
			ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
			for (int i = 0; i < MAX_PIECE_COUNT; i++) {
				executorService.submit(new ParameterizedThread<>(i, (index) -> { // 执行多线程程
					result.add(writePiece(file, down, index * PIECE_MAX_SIZE, (index + 1) * PIECE_MAX_SIZE - 1));
				}));
			}
			MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
			if (result.contains(false)) {
				return HttpStatus.SC_REQUEST_TIMEOUT;
			}
		} else {
			List<Integer> piece = IntStream.rangeClosed(0, MAX_THREADS - 1).boxed().collect(Collectors.toList());
			if (piece.parallelStream().map(index -> writePiece(file, down, index * fileSize / MAX_THREADS, (index + 1) * fileSize / MAX_THREADS - 1)).collect(Collectors.toList()).contains(false)) {
				return HttpStatus.SC_REQUEST_TIMEOUT;
			}
		}
		down.delete();
		// 效验文件完整性
		if (!Judge.isEmpty(hash)) {
			try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
				if (!DigestUtils.md5Hex(inputStream).equals(hash)) {
					file.delete();
					return HttpStatus.SC_REQUEST_TIMEOUT;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return statusCode;
	}

	/**
	 * 下载获取文件区块信息并写入文件
	 *
	 * @param file
	 *            文件
	 * @param down
	 *            down文件
	 * @param start
	 *            块起始位
	 * @param end
	 *            块结束位
	 * @return 下载并写入是否成功
	 */
	private boolean writePiece(File file, File down, int start, int end) {
		String pointer = start + "-" + end;
		if (!downInfo.contains(pointer)) {
			Response piece = JsoupUtils.connect(url).proxy(proxyHost, proxyPort).header("Range", "bytes=" + pointer).cookies(cookies).referrer(referrer).retry(retry, MILLISECONDS_SLEEP)
					.retry(unlimitedRetry).errorExit(errorExit).GetResponse();
			if (!Judge.isNull(piece) && URIUtils.statusIsOK(piece.statusCode())) {
				try (BufferedInputStream inputStream = piece.bodyStream(); RandomAccessFile output = new RandomAccessFile(file, RandomAccessFileMode.WRITE.getValue())) {
					output.seek(start);
					byte[] buffer = bufferSize > PIECE_MAX_SIZE ? new byte[bufferSize] : new byte[PIECE_MAX_SIZE];
					int length;
					while (!Judge.isMinusOne(length = inputStream.read(buffer))) {
						output.write(buffer, 0, length);
					}
					ReadWriteUtils.orgin(down).text(pointer);
				} catch (IOException e) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

}
