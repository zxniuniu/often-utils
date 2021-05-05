package org.haic.often.Compress;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.haic.often.FilesUtils;
import org.haic.often.IOUtils;
import org.haic.often.Judge;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AbstractFileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.*;

/**
 * @author haicdust
 * @version 1.0
 * @since 2021/3/31 10:03
 */
public class Zip4jUtils {

	private File archive; // 压缩包
	private char[] passwd; // 压缩包密码
	private CompressionMethod method = CompressionMethod.STORE;// 压缩方式
	private CompressionLevel level = CompressionLevel.FASTEST;// 压缩级别
	private Charset charset = StandardCharsets.UTF_8; // 字符集编码格式
	private final ZipParameters params = new ZipParameters(); // 压缩参数

	private Zip4jUtils() {
		params.setCompressionMethod(method); // 压缩方式
		params.setCompressionLevel(level); // 压缩级别
		params.setIncludeRootFolder(false); // 包含根文件夹
	}

	/**
	 * 获取新的Zip4jUtils对象并设置压缩包文件
	 *
	 * @param archive
	 *            压缩包文件路径
	 * @return new Zip4jUtils
	 */
	public static Zip4jUtils origin(final @NotNull String archive) {
		return origin(new File(archive));
	}

	/**
	 * 获取新的Zip4jUtils对象并设置压缩包文件
	 *
	 * @param archive
	 *            压缩包文件
	 * @return new Zip4jUtils
	 */
	public static Zip4jUtils origin(final @NotNull File archive) {
		return config().archive(archive);

	}

	/**
	 * 获取新的 Zip4jUtils
	 *
	 * @return new Zip4jUtils
	 */
	private static Zip4jUtils config() {
		return new Zip4jUtils();
	}

	/**
	 * 设置 压缩包
	 *
	 * @param archive
	 *            压缩包
	 * @return this
	 */
	private Zip4jUtils archive(final @NotNull File archive) {
		this.archive = archive;
		return this;
	}

	/**
	 * 设置 压缩包密码
	 *
	 * @param passwd
	 *            压缩包密码
	 * @return this
	 */
	public Zip4jUtils passwd(final @NotNull String passwd) {
		this.passwd = passwd.toCharArray();
		params.setEncryptFiles(true); // 设置文件加密
		params.setEncryptionMethod(EncryptionMethod.AES); // 加密方式
		params.setAesVersion(AesVersion.TWO); // 用于加密的AES格式版本
		params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256); // AES加密密钥的密钥强度
		return this;
	}

	/**
	 * 压缩文件夹时，包含根目录
	 *
	 * @param includeRoot
	 *            启用 包含根目录
	 * @return this
	 */
	public Zip4jUtils includeRoot(boolean includeRoot) {
		params.setIncludeRootFolder(includeRoot);
		return this;
	}

	/**
	 * 设置 压缩方式
	 *
	 * @param method
	 *            压缩方式
	 * @return this
	 */
	public Zip4jUtils method(final @NotNull CompressionMethod method) {
		this.method = method;
		return this;
	}

	/**
	 * 设置 压缩级别
	 *
	 * @param level
	 *            压缩级别
	 * @return this
	 */
	public Zip4jUtils level(final @NotNull CompressionLevel level) {
		this.level = level;
		return this;
	}

	/**
	 * 设置 字符集编码格式
	 *
	 * @param charset
	 *            集编码格式
	 * @return this
	 */
	public Zip4jUtils charset(final @NotNull Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 字符集编码格式
	 *
	 * @param charsetName
	 *            集编码格式名称
	 * @return this
	 */
	public Zip4jUtils charset(final @NotNull String charsetName) {
		this.charset = Charset.forName(charsetName);
		return this;
	}

	/**
	 * 压缩文件或文件夹到指定压缩包
	 *
	 * @param origin
	 *            文件或文件夹路径
	 * @return 压缩的文件列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> compress(final @NotNull String origin) {
		return compress(new File(origin));
	}

	/**
	 * 压缩文件或文件夹到指定压缩包
	 *
	 * @param origin
	 *            文件或文件夹
	 * @return 压缩的文件列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> compress(final @NotNull File origin) {
		if (!origin.exists()) {
			throw new RuntimeException("Not found " + origin);
		}
		ZipFile zipFile = new ZipFile(archive);
		zipFile.setCharset(charset);
		if (!Judge.isEmpty(passwd)) {
			zipFile.setPassword(passwd);
		}
		List<String> result = new ArrayList<>();
		try {
			result = zipFile.getFileHeaders().parallelStream().map(AbstractFileHeader::getFileName).collect(Collectors.toList());
			if (origin.isFile()) {
				zipFile.addFile(origin, params);
			} else {
				zipFile.addFolder(origin, params);
			}
			result = IOUtils.listDeduplication(zipFile.getFileHeaders().parallelStream().map(AbstractFileHeader::getFileName), result);
		} catch (ZipException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 解压压缩包到指定目录
	 *
	 * @param out
	 *            输出文件夹路径
	 * @return 解压的文件列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> deCompress(final @NotNull String out) {
		return deCompress(new File(out));
	}

	/**
	 * 解压压缩包到指定目录
	 *
	 * @param out
	 *            输出文件夹
	 * @return 解压的文件列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> deCompress(final @NotNull File out) {
		if (!archive.isFile()) {
			throw new RuntimeException("Not found or not file " + archive);
		}
		if (out.isFile()) {
			throw new RuntimeException("That is a file " + out);
		}
		FilesUtils.createFolder(out);
		ZipFile zipFile = new ZipFile(archive);
		zipFile.setCharset(charset);
		if (!zipFile.isValidZipFile()) {
			throw new RuntimeException("压缩文件不合法,可能被损坏");
		}
		List<String> result = new ArrayList<>();
		try {
			if (zipFile.isEncrypted()) { // 3.判断是否已加密
				zipFile.setPassword(passwd);
			}
			zipFile.extractAll(out.getPath()); // 4.解压所有文件
			result = zipFile.getFileHeaders().parallelStream().map(AbstractFileHeader::getFileName).collect(Collectors.toList());
		} catch (ZipException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 添加文件或文件夹到压缩包中
	 *
	 * @param origin
	 *            文件或文件夹路径
	 * @return 添加的文件列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> addFiles(final @NotNull String origin) {
		return addFiles(new File(origin));
	}

	/**
	 * 添加文件或文件夹到压缩包中
	 *
	 * @param origin
	 *            文件或文件夹
	 * @return 添加的文件列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> addFiles(final @NotNull File origin) {
		if (!origin.exists()) {
			throw new RuntimeException("Not found " + origin);
		}
		ZipFile zipFile = new ZipFile(archive);
		zipFile.setCharset(charset);
		if (origin.isFile()) {
			remove(origin.getName());
		} else {
			remove(FilesUtils.iterateFiles(origin).parallelStream().map(file -> file.getAbsolutePath().substring(origin.getAbsolutePath().length() + 1).replaceAll("\\\\", "/"))
					.collect(Collectors.toList()));
		}
		return compress(origin);
	}

	/**
	 * 添加流至压缩包
	 *
	 * @param inputStream
	 *            流
	 * @param entryName
	 *            文件名或路径
	 * @return 添加的文件列表
	 */
	public String addStream(final @NotNull ByteArrayInputStream inputStream, String entryName) {
		ZipFile zipFile = new ZipFile(archive);
		zipFile.setCharset(charset);
		if (!Judge.isEmpty(passwd)) {
			zipFile.setPassword(passwd);
		}
		try {
			params.setFileNameInZip(entryName);
			zipFile.addStream(inputStream, params);
		} catch (ZipException e) {
			e.printStackTrace();
		}
		return entryName;
	}

	/**
	 * 添加流至压缩包
	 *
	 * @param origin
	 *            集合 -> 文件名或路径、byte数组
	 * @return 添加的文件列表
	 */
	public List<String> addStream(final @NotNull Map<String, byte[]> origin) {
		return origin.entrySet().parallelStream().map(entry -> addStream(new ByteArrayInputStream(entry.getValue()), entry.getKey())).collect(Collectors.toList());
	}

	/**
	 * 删除压缩包中的文件,注意路径以"/"分割
	 *
	 * @param origin
	 *            压缩包中的文件路径
	 * @return 删除是否成功
	 */
	@Contract(pure = true)
	public boolean remove(final @NotNull String origin) {
		if (!archive.isFile()) {
			throw new RuntimeException("Not found or not file " + archive);
		}
		try {
			ZipFile zipFile = new ZipFile(archive);
			zipFile.removeFile(origin);
		} catch (ZipException e) {
			return false;
		}
		return true;
	}

	/**
	 * 批量删除压缩包中的文件,注意路径以"/"分割
	 *
	 * @param origin
	 *            压缩包中的文件路径列表
	 * @return 删除是否成功
	 */
	@Contract(pure = true)
	public boolean remove(final @NotNull List<String> origin) {
		if (!archive.isFile()) {
			throw new RuntimeException("Not found or not file " + archive);
		}
		try {
			ZipFile zipFile = new ZipFile(archive);
			zipFile.removeFiles(origin);
		} catch (ZipException e) {
			return false;
		}
		return true;
	}

}
