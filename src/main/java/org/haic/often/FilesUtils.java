package org.haic.often;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/7 17:29
 */
public class FilesUtils {

	/**
	 * 如果文件存在，删除文件
	 *
	 * @param filepath 文件路径
	 * @return 删除是否成功
	 */
	public static boolean deteleFile(String filepath) {
		return deteleFile(new File(filepath));
	}

	/**
	 * 如果文件存在，删除文件
	 *
	 * @param file 文件
	 * @return 删除是否成功
	 */
	@Contract(pure = true) public static boolean deteleFile(File file) {
		return file.exists() && file.delete();
	}

	/**
	 * 删除列表文件
	 *
	 * @param files 文件列表
	 * @return 删除的文件列表
	 */
	@Contract(pure = true) public static List<String> deteleFiles(List<File> files) {
		return files.parallelStream().filter(FilesUtils::deteleFile).map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 打开资源管理器窗口
	 *
	 * @param folderPath 文件夹路径
	 * @return 操作是否成功
	 */
	@Contract(pure = true) public static boolean openDesktop(@NotNull final String folderPath) {
		File folder = new File(folderPath);
		if (folder.isDirectory()) {
			try {
				Desktop.getDesktop().open(new File(folderPath));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 删除空文件夹
	 *
	 * @param filesPath 文件夹路径
	 * @return 删除的空文件夹路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> deleteBlankDirectory(@NotNull final String filesPath) {
		return deleteBlankDirectory(new File(filesPath));
	}

	/**
	 * 删除空文件夹
	 *
	 * @param files 文件夹
	 * @return 删除的空文件夹路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> deleteBlankDirectory(@NotNull final File files) {
		return files.exists() && !files.isFile() ?
				Arrays.stream(Objects.requireNonNull(files.listFiles())).parallel()
						.flatMap(file -> isBlankDirectory(file) && file.delete() ? Stream.of(file.getPath()) : deleteBlankDirectory(file).parallelStream())
						.collect(Collectors.toList()) :
				new ArrayList<>();
	}

	/**
	 * 判断是否为空文件夹
	 *
	 * @param folderPath 需要判断的文件夹路径
	 * @return 判断结果
	 */
	@Contract(pure = true) public static boolean isBlankDirectory(@NotNull final String folderPath) {
		return isBlankDirectory(new File(folderPath));
	}

	/**
	 * 判断是否为空文件夹
	 *
	 * @param folder 需要判断的文件夹
	 * @return 判断结果
	 */
	@Contract(pure = true) public static boolean isBlankDirectory(@NotNull final File folder) {
		return folder.isDirectory() && Judge.isEmpty(folder.list());
	}

	/**
	 * 删除文件夹
	 *
	 * @param folderPath 文件夹路径
	 * @return 删除文件夹是否成功
	 */
	@Contract(pure = true) public static boolean deleteDirectory(@NotNull final String folderPath) {
		return deleteDirectory(new File(folderPath));
	}

	/**
	 * 删除文件夹
	 *
	 * @param folder 文件夹对象
	 * @return 删除文件夹是否成功
	 */
	@Contract(pure = true) public static boolean deleteDirectory(@NotNull final File folder) {
		return folder.exists() && !Arrays.stream(Objects.requireNonNull(folder.listFiles())).parallel()
				.map(file -> file.isDirectory() ? deleteDirectory(file) : file.delete()).toList().contains(false) && folder.delete();
	}

	/**
	 * 删除文件夹内指定后缀文件
	 *
	 * @param folderPath 文件夹路径
	 * @param suffix     后缀
	 * @return 删除的文件路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> deleteSuffixFiles(@NotNull final String folderPath, @NotNull final String suffix) {
		return deleteSuffixFiles(new File(folderPath), suffix);
	}

	/**
	 * 删除文件夹内指定后缀文件
	 *
	 * @param files  文件夹对象
	 * @param suffix 后缀
	 * @return 删除的文件路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> deleteSuffixFiles(@NotNull final File files, @NotNull final String suffix) {
		return iterateSuffixFiles(files, suffix).parallelStream().filter(File::delete).map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 判断是否为指定后缀
	 *
	 * @param filePath 文件路径
	 * @param suffix   后缀
	 * @return boolean
	 */
	@Contract(pure = true) public static boolean isSuffixFile(@NotNull final String filePath, @NotNull final String suffix) {
		return filePath.endsWith((char) 46 + suffix);
	}

	/**
	 * 判断是否为指定后缀
	 *
	 * @param file   文件对象
	 * @param suffix 后缀
	 * @return boolean
	 */
	@Contract(pure = true) public static boolean isSuffixFile(@NotNull final File file, @NotNull final String suffix) {
		return file.isFile() && isSuffixFile(file.getName(), suffix);
	}

	/**
	 * 获取文件后缀
	 *
	 * @param fileName 文件名
	 * @return 文件后缀
	 */
	@Contract(pure = true) public static String getFileSuffix(@NotNull final String fileName) {
		return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(46) + 1) : null;
	}

	/**
	 * 获取文件后缀
	 *
	 * @param file 文件对象
	 * @return 文件后缀
	 */
	@Contract(pure = true) public static String getFileSuffix(@NotNull final File file) {
		return getFileSuffix(file.getName());
	}

	/**
	 * 修改文件后缀
	 *
	 * @param file   文件对象
	 * @param suffix 后缀
	 * @return 修改后缀是否成功
	 */
	@Contract(pure = true) public static boolean afterFileSuffix(@NotNull final File file, @NotNull final String suffix) {
		final String fileName = file.getName();
		final File newfile;
		if (fileName.contains(".")) {
			newfile = new File(file.getParent(), fileName.substring(0, fileName.lastIndexOf(46) + 1) + suffix);
		} else {
			newfile = new File(file.getParent(), fileName + (char) 46 + suffix);
		}
		return !newfile.exists() && file.renameTo(newfile);
	}

	/**
	 * 修改文件后缀
	 *
	 * @param filePath 文件路径
	 * @param suffix   后缀
	 * @return 修改后缀是否成功
	 */
	@Contract(pure = true) public static boolean afterFileSuffix(@NotNull final String filePath, @NotNull final String suffix) {
		return afterFileSuffix(new File(filePath), suffix);
	}

	/**
	 * 重命名文件
	 *
	 * @param file     文件对象
	 * @param fileName 文件名
	 * @return 重命名是否成功
	 */
	@Contract(pure = true) public static boolean afterFileName(@NotNull final File file, @NotNull final String fileName) {
		final File newfile = new File(file.getParent(), fileName);
		return !newfile.exists() && file.renameTo(newfile);
	}

	/**
	 * 重命名文件
	 *
	 * @param filePath 文件路径
	 * @param fileName 新的文件名
	 * @return 重命名是否成功
	 */
	@Contract(pure = true) public static boolean afterFileName(@NotNull final String filePath, @NotNull final String fileName) {
		return afterFileName(new File(filePath), fileName);
	}

	/**
	 * 获取文件夹所有文件对象列表
	 *
	 * @param filesPath 文件夹或文件路径
	 * @return 文件对象列表
	 */
	@NotNull @Contract(pure = true) public static List<File> iterateFiles(@NotNull final String filesPath) {
		return iterateFiles(new File(filesPath));
	}

	/**
	 * 获取文件夹所有文件对象列表
	 *
	 * @param files 文件夹或文件对象
	 * @return 文件对象列表
	 */
	@NotNull @Contract(pure = true) public static List<File> iterateFiles(@NotNull final File files) {
		return files.isDirectory() ?
				Arrays.stream(Objects.requireNonNull(files.listFiles())).parallel().flatMap(file -> iterateFiles(file).parallelStream())
						.collect(Collectors.toList()) :
				files.isFile() ? Collections.singletonList(files) : new ArrayList<>();
	}

	/**
	 * @param files  文件夹或文件对象
	 * @param suffix 文件后缀名
	 * @return 文件对象列表
	 */
	@NotNull @Contract(pure = true) public static List<File> iterateFilesOfSuffix(@NotNull final File files, @NotNull final String suffix) {
		return iterateFilesPathOfSuffix(files, suffix).parallelStream().map(File::new).collect(Collectors.toList());
	}

	/**
	 * @param filesPath 文件夹或文件路径
	 * @param suffix    文件后缀名
	 * @return 文件对象列表
	 */
	@NotNull @Contract(pure = true) public static List<File> iterateFilesOfSuffix(@NotNull final String filesPath, @NotNull final String suffix) {
		return iterateFilesOfSuffix(new File(filesPath), suffix);
	}

	/**
	 * @param filesPath 文件夹或文件路径
	 * @param suffix    文件后缀名
	 * @return 文件路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> iterateFilesPathOfSuffix(@NotNull final String filesPath, @NotNull final String suffix) {
		return iterateFilesPathOfSuffix(new File(filesPath), suffix);
	}

	/**
	 * @param files  文件夹或文件对象
	 * @param suffix 文件后缀名
	 * @return 文件路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> iterateFilesPathOfSuffix(@NotNull final File files, @NotNull final String suffix) {
		return iterateFilesPath(files).parallelStream().filter(file -> file.endsWith((char) 46 + suffix)).collect(Collectors.toList());
	}

	/**
	 * 获取文件夹所有文件路径列表
	 *
	 * @param filesPath 文件夹或文件路径
	 * @return 文件路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> iterateFilesPath(@NotNull final String filesPath) {
		return iterateFilesPath(new File(filesPath));
	}

	/**
	 * 获取文件夹所有文件路径列表
	 *
	 * @param files 文件夹或文件对象
	 * @return 文件路径列表
	 */
	@NotNull @Contract(pure = true) public static List<String> iterateFilesPath(@NotNull final File files) {
		return iterateFiles(files).parallelStream().map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 获取文件夹所有指定后缀的文件路径列表
	 *
	 * @param filesPath 文件夹或文件路径
	 * @param suffix    文件后缀
	 * @return 文件路径列表
	 */
	@NotNull public static List<File> iterateSuffixFiles(@NotNull final String filesPath, @NotNull final String suffix) {
		return iterateSuffixFiles(new File(filesPath), suffix);
	}

	/**
	 * 获取文件夹所有指定后缀的文件路径列表
	 *
	 * @param files  文件夹或文件
	 * @param suffix 文件后缀
	 * @return 文件路径列表
	 */
	@NotNull @Contract(pure = true) public static List<File> iterateSuffixFiles(@NotNull final File files, @NotNull final String suffix) {
		return files.isDirectory() ?
				Arrays.stream(Objects.requireNonNull(files.listFiles())).parallel().flatMap(file -> iterateSuffixFiles(file, suffix).parallelStream())
						.collect(Collectors.toList()) :
				isSuffixFile(files, suffix) ? Collections.singletonList(files) : new ArrayList<>();
	}

	/**
	 * 获取文件夹所有指定后缀的文件列表
	 *
	 * @param filesPath 文件夹或文件路径
	 * @param suffix    文件后缀
	 * @return 文件列表
	 */
	public static List<String> iterateSuffixFilesPath(@NotNull final String filesPath, @NotNull final String suffix) {
		return iterateSuffixFilesPath(new File(filesPath), suffix);
	}

	/**
	 * 获取文件夹所有指定后缀的文件列表
	 *
	 * @param files  文件夹或文件
	 * @param suffix 文件后缀
	 * @return 文件列表
	 */
	public static List<String> iterateSuffixFilesPath(@NotNull final File files, @NotNull final String suffix) {
		return iterateSuffixFiles(files, suffix).parallelStream().map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 获取桌面对象
	 *
	 * @return 文件对象
	 */
	@Contract(pure = true) public static File getDesktop() {
		return FileSystemView.getFileSystemView().getHomeDirectory();
	}

	/**
	 * 获取桌面路径
	 *
	 * @return 路径
	 */
	@NotNull @Contract(pure = true) public static String getDesktopPath() {
		return getDesktop().toString();
	}

	/**
	 * 创建文件
	 *
	 * @param filePath 文件路径
	 */
	@Contract(pure = true) public static void createFile(@NotNull final String filePath) {
		final File file = new File(filePath);
		if (!file.exists()) { // 文件不存在则创建文件，先创建目录
			createFolder(file.getParent());
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建文件夹
	 *
	 * @param folderPath 文件夹路径
	 */
	@Contract(pure = true) public static void createFolder(@NotNull final String folderPath) {
		createFolder(new File(folderPath));
	}

	/**
	 * 创建文件夹
	 *
	 * @param folder 文件夹对象
	 * @return 创建文件是否成功
	 */
	@Contract(pure = true) public static boolean createFolder(@NotNull final File folder) {
		return !folder.exists() && folder.mkdirs();
	}

	/**
	 * 修改非法的Windows文件名
	 *
	 * @param fileName 文件名
	 * @return 正常的Windows文件名
	 */
	@NotNull @Contract(pure = true) public static String illegalFileName(@NotNull final String fileName) {
		return fileName.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("[_]{2,}", "_");
	}

	/**
	 * 传入路径，返回是否是绝对路径，是绝对路径返回true，反之
	 *
	 * @param path 路径
	 * @return 判断结果
	 */
	@Contract(pure = true) public static boolean isAbsolutePath(@NotNull final String path) {
		return path.startsWith("/") || path.charAt(1) == 58;
	}

	/**
	 * 传入路径，返回绝对路径
	 *
	 * @param path 路径
	 * @return 绝对路径
	 */
	@NotNull @Contract(pure = true) public static String getAbsolutePath(@NotNull final String path) {
		return new File(path).getAbsolutePath();
	}

	/**
	 * 获取文件MD5值
	 *
	 * @param filePath 文件路径
	 * @return MD5值
	 */
	@NotNull @Contract(pure = true) public static String getMD5(@NotNull final String filePath) {
		return getMD5(new File(filePath));
	}

	/**
	 * 获取文件 MD5 值
	 *
	 * @param file 文件
	 * @return MD5 值
	 */
	@NotNull @Contract(pure = true) public static String getMD5(@NotNull final File file) {
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			return DigestUtils.md5Hex(inputStream);
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * 获取文件 SHA1 值
	 *
	 * @param filePath 文件路径
	 * @return SHA1 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA1(@NotNull final String filePath) {
		return getSHA1(new File(filePath));
	}

	/**
	 * 获取文件 SHA1 值
	 *
	 * @param file 文件
	 * @return SHA1 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA1(@NotNull final File file) {
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			return DigestUtils.sha1Hex(inputStream);
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * 获取文件 SHA256 值
	 *
	 * @param filePath 文件路径
	 * @return SHA256 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA256(@NotNull final String filePath) {
		return getSHA256(new File(filePath));
	}

	/**
	 * 获取文件 SHA256 值
	 *
	 * @param file 文件
	 * @return SHA256 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA256(@NotNull final File file) {
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			return DigestUtils.sha256Hex(inputStream);
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * 获取文件 SHA384 值
	 *
	 * @param filePath 文件路径
	 * @return SHA384 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA384(@NotNull final String filePath) {
		return getSHA384(new File(filePath));
	}

	/**
	 * 获取文件 SHA384 值
	 *
	 * @param file 文件
	 * @return SHA384 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA384(@NotNull final File file) {
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			return DigestUtils.sha384Hex(inputStream);
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * 获取文件 SHA512 值
	 *
	 * @param filePath 文件路径
	 * @return SHA512 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA512(@NotNull final String filePath) {
		return getSHA512(new File(filePath));
	}

	/**
	 * 获取文件 SHA512 值
	 *
	 * @param file 文件
	 * @return SHA512 值
	 */
	@NotNull @Contract(pure = true) public static String getSHA512(@NotNull final File file) {
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			return DigestUtils.sha512Hex(inputStream);
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * 复制文件
	 *
	 * @param input  来源文件对象
	 * @param output 输出路径对象
	 * @return 复制是否成功
	 */
	@Contract(pure = true) public static boolean copyFile(@NotNull final File input, @NotNull final File output) {
		try {
			FileUtils.copyFile(input, output);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 复制文件夹
	 *
	 * @param input  来源文件夹对象
	 * @param output 输出路径对象
	 * @return 复制是否成功
	 */
	@Contract(pure = true) public static boolean copyDirectory(@NotNull final File input, @NotNull final File output) {
		try {
			FileUtils.copyDirectory(input, output);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}