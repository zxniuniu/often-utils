package org.haic.often;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.lingala.zip4j.model.enums.RandomAccessFileMode;

/**
 * @author haicdust
 * @version 1.0
 * @since 2021/4/12 11:04
 */
public final class ReadWriteUtils {

	private File source; // 目标文件或文件夹
	private int bufferSize = 8192; // 缓冲区大小
	private Charset charset = StandardCharsets.UTF_8; // 字符集编码格式
	private boolean append = true; // 默认追加写入

	private ReadWriteUtils() {

	}

	/**
	 * 设置目标文件或文件夹并获取 new ReadWriteUtils
	 *
	 * @param source
	 *            文件或文件夹路径
	 * @return this
	 */
	@Contract(pure = true)
	public static ReadWriteUtils orgin(String source) {
		return orgin(new File(source));
	}

	/**
	 * 设置目标文件或文件夹并获取 new ReadWriteUtils
	 *
	 * @param source
	 *            文件或文件夹
	 * @return this
	 */
	@Contract(pure = true)
	public static ReadWriteUtils orgin(File source) {
		return config().file(source);
	}

	/**
	 * 获取 new ReadWriteUtils
	 *
	 * @return this
	 */
	@Contract(pure = true)
	private static ReadWriteUtils config() {
		return new ReadWriteUtils();
	}

	/**
	 * 设置 文件或文件夹
	 *
	 * @param source
	 *            文件或文件夹
	 * @return this
	 */
	@Contract(pure = true)
	private ReadWriteUtils file(File source) {
		this.source = source;
		return this;
	}

	/**
	 * 设置 缓冲区大小
	 *
	 * @param bufferSize
	 *            缓冲区大小
	 * @return this
	 */
	@Contract(pure = true)
	public ReadWriteUtils bufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		return this;
	}

	/**
	 * 设置 字符集编码格式
	 *
	 * @param charset
	 *            字符集编码格式
	 * @return this
	 */
	@Contract(pure = true)
	public ReadWriteUtils charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 字符集格式
	 *
	 * @param charsetName
	 *            字符集格式
	 * @return this
	 */
	@Contract(pure = true)
	public ReadWriteUtils charset(String charsetName) {
		return charset(Charset.forName(charsetName));
	}

	/**
	 * 设置 追加写入
	 *
	 * @param append
	 *            启用追加写入
	 * @return this
	 */
	@Contract(pure = true)
	public ReadWriteUtils append(boolean append) {
		this.append = append;
		return this;
	}

	// ================================================== WriteUtils ==================================================

	/**
	 * 将数组合为一行写入文件
	 * 
	 * @param lists
	 *            字符串数组
	 * @return 写入是否成功
	 */
	@Contract(pure = true)
	public boolean listToText(@NotNull final List<String> lists) {
		FilesUtils.createFolder(source.getParent());
		try (BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source, append), charset), bufferSize)) {
			outStream.write(StringUtils.join(lists, StringUtils.SPACE) + StringUtils.LINE_SEPARATOR); // 文件输出流用于将数据写入文件
			outStream.flush();
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * 将字符串写入文件
	 *
	 * @param str
	 *            字符串
	 * @return 写入是否成功
	 */
	@Contract(pure = true)
	public boolean text(@NotNull final String str) {
		FilesUtils.createFolder(source.getParent());
		try (BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source, append), charset), bufferSize)) {
			outStream.write(str + StringUtils.LINE_SEPARATOR); // 文件输出流用于将数据写入文件
			outStream.flush();
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * 将数组按行写入文件
	 *
	 * @param lists
	 *            字符串数组
	 * @return 写入是否成功
	 */
	@Contract(pure = true)
	public boolean list(@NotNull final List<String> lists) {
		FilesUtils.createFolder(source.getParent());
		try (BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source, append), charset), bufferSize)) {
			outStream.write(lists.parallelStream().collect(Collectors.joining(StringUtils.LINE_SEPARATOR)) + StringUtils.LINE_SEPARATOR);
			outStream.flush();
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * 将字符串写入二进制文件
	 * 
	 * @param str
	 *            字符串
	 * @return 写入是否成功
	 */
	@Contract(pure = true)
	public boolean binary(@NotNull final String str) {
		FilesUtils.createFolder(source.getParent());
		try (DataOutputStream outStream = new DataOutputStream(new FileOutputStream(source, append))) {
			for (byte b : (str + StringUtils.LF).getBytes()) {
				outStream.writeInt(b); // 文件输出流用于将数据写入文件
			}
			outStream.flush();
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * RandomAccessFile 写入文本
	 * 
	 * @param str
	 *            字符串
	 * @return 写入是否成功
	 */
	@Contract(pure = true)
	public boolean randomAccessText(@NotNull final String str) {
		FilesUtils.createFolder(source.getParent());
		try (RandomAccessFile randomAccess = new RandomAccessFile(source, RandomAccessFileMode.WRITE.getValue())) {
			if (append) {
				randomAccess.seek(source.length());
			}
			randomAccess.write((str + StringUtils.LINE_SEPARATOR).getBytes(charset));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * FileChannel 写入文件文本
	 * 
	 * @param str
	 *            字符串
	 * @return 写入是否成功
	 */
	@Contract(pure = true)
	public boolean channelText(@NotNull final String str) {
		FilesUtils.createFolder(source.getParent());
		try (FileChannel channel = new FileOutputStream(source, append).getChannel()) {
			channel.write(ByteBuffer.wrap((str + StringUtils.LINE_SEPARATOR).getBytes(charset)));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * MappedByteBuffer 内存映射方法写入文件文本
	 * 
	 * @param str
	 *            字符串
	 * @return 写入是否成功
	 */
	@Contract(pure = true)
	public boolean mappedText(String str) {
		FilesUtils.createFolder(source.getParent());
		byte[] params = (str + StringUtils.LINE_SEPARATOR).getBytes(charset);
		MappedByteBuffer mappedByteBuffer;
		if (append) {
			try (FileChannel fileChannel = FileChannel.open(source.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
				mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, source.length(), params.length);
				mappedByteBuffer.put(params);
			} catch (IOException e) {
				return false;
			}
		} else {
			try (FileChannel fileChannel = FileChannel.open(source.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
				mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, params.length);
				mappedByteBuffer.put(params);
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * FileChannel 文件复制
	 * 
	 * @param out
	 *            指定输出文件
	 * @return 文件复制状态
	 */
	public boolean channelCopy(String out) {
		return channelCopy(new File(out));
	}

	/**
	 * FileChannel 文件复制
	 * 
	 * @param out
	 *            指定输出文件
	 * @return 文件复制状态
	 */
	public boolean channelCopy(File out) {
		FilesUtils.createFolder(out.getParent());
		try (FileChannel inputChannel = new FileInputStream(source).getChannel(); FileChannel outputChannel = new FileOutputStream(out).getChannel()) {
			outputChannel.transferFrom(inputChannel, append ? out.length() : 0, inputChannel.size());
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * MappedByteBuffer 文件复制
	 *
	 * @param out
	 *            指定输出文件路径
	 * @return 文件复制状态
	 */
	public boolean mappedCopy(String out) {
		return mappedCopy(new File(out));
	}

	/**
	 * MappedByteBuffer 文件复制
	 *
	 * @param out
	 *            指定输出文件路径
	 * @return 文件复制状态
	 */
	public boolean mappedCopy(File out) {
		FilesUtils.createFolder(out.getParent());
		try (FileChannel inputChannel = new RandomAccessFile(source, RandomAccessFileMode.READ.getValue()).getChannel();
				FileChannel outChannel = new RandomAccessFile(out, RandomAccessFileMode.WRITE.getValue()).getChannel()) {
			long MAX_COUNT = (long) Math.ceil((double) source.length() / (double) Integer.MAX_VALUE);
			for (long count = 0; count < MAX_COUNT; count++) {
				long start = append ? count * Integer.MAX_VALUE + out.length() : count * Integer.MAX_VALUE;
				long size = count + 1 == MAX_COUNT ? source.length() % Integer.MAX_VALUE : Integer.MAX_VALUE;
				MappedByteBuffer intputMappedByteBuffer = inputChannel.map(FileChannel.MapMode.READ_ONLY, start, size);
				MappedByteBuffer outMappedByteBuffer = outChannel.map(FileChannel.MapMode.READ_WRITE, start, size);
				for (long i = 0; i < size; i++) {
					outMappedByteBuffer.put(intputMappedByteBuffer.get());
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	// ================================================== ReadUtils ==================================================

	/**
	 * 遍历文件或文件夹,按行读取内容
	 *
	 * @param file
	 *            文件或文件夹
	 * @return 文本信息列表
	 */
	@NotNull
	@Contract(pure = true)
	private List<String> list(@NotNull final File file) {
		List<String> result = new ArrayList<>();
		if (file.isFile()) {
			try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(file), charset)) {
				result = IOUtils.streamToArray(inputStream);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else if (file.isDirectory()) {
			result = mapList().values().parallelStream().flatMap(Collection::parallelStream).collect(Collectors.toList());
		}
		return result;
	}

	/**
	 * 遍历文件或文件夹,按行读取内容
	 *
	 * @return 文本信息列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> list() {
		return list(source);
	}

	/**
	 * 读取指定文件夹内所有文件的内容
	 *
	 * @return 集合 -> 文件路径 和 文本信息列表
	 */
	@NotNull
	@Contract(pure = true)
	public Map<String, List<String>> mapList() {
		return FilesUtils.iterateFiles(source).parallelStream().collect(Collectors.toMap(File::getPath, this::list));
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return 文本信息
	 */
	@NotNull
	@Contract(pure = true)
	private String text(@NotNull final File file) {
		String result = "";
		if (!file.isFile()) { // 判断文件是否存在
			return result;
		}
		try (InputStream inputStream = new FileInputStream(file)) {
			result = IOUtils.streamToString(inputStream, bufferSize);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return 文本信息
	 */
	@NotNull
	@Contract(pure = true)
	public String text() {
		return text(source);
	}

	/**
	 * 读取指定文件夹内所有文件的内容
	 *
	 * @return 文本信息列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> texts() {
		return new ArrayList<>(mapText().values());
	}

	/**
	 * 读取指定文件或文件夹内所有文件的内容
	 *
	 * @return 集合 -> 文件路径 和 文本信息
	 */
	@NotNull
	@Contract(pure = true)
	public Map<String, String> mapText() {
		return FilesUtils.iterateFiles(source).parallelStream().collect(Collectors.toMap(File::getPath, this::text));
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @param file
	 *            文件
	 * @return bytes
	 */
	@Contract(pure = true)
	private byte[] array(@NotNull final File file) {
		byte[] result = new byte[0];
		if (!file.isFile()) { // 判断文件是否存在
			return result;
		}
		try (InputStream inputStream = new FileInputStream(file)) {
			result = IOUtils.streamToByteArray(inputStream, bufferSize);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return bytes
	 */
	@Contract(pure = true)
	public byte[] array() {
		return array(source);
	}

	/**
	 * 读取指定文件夹内所有文件的内容
	 *
	 * @return bytes列表
	 */
	@NotNull
	@Contract(pure = true)
	public List<byte[]> arrays() {
		return new ArrayList<>(mapArray().values());
	}

	/**
	 * 读取指定文件或文件夹内所有文件的内容
	 *
	 * @return 集合 -> 文件路径 和 bytes
	 */
	@NotNull
	@Contract(pure = true)
	public Map<String, byte[]> mapArray() {
		return FilesUtils.iterateFiles(source).parallelStream().collect(Collectors.toMap(File::getPath, this::array));
	}

	/**
	 * 读取二进制文件信息
	 *
	 * @return 文件文本信息
	 */
	@NotNull
	@Contract(pure = true)
	public String binary() {
		StringBuilder result = new StringBuilder();
		if (!source.isFile()) {
			return String.valueOf(result);
		}
		try (DataInputStream inputStream = new DataInputStream(new FileInputStream(source))) {
			for (int i = 0; i < source.length() / 4; i++) {
				result.append((char) inputStream.readInt());
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return String.valueOf(result);
	}

	/**
	 * 按行读取二进制文件信息
	 *
	 * @return 文件文本信息
	 */
	@NotNull
	@Contract(pure = true)
	public List<String> binaryList() {
		return new ArrayList<>(Arrays.asList(binary().split(StringUtils.LF)));
	}

	/**
	 * RandomAccessFile 随机存储读取
	 *
	 * @return 文本
	 */
	@NotNull
	@Contract(pure = true)
	public String randomAccessText() {
		if (!source.isFile()) {
			return "";
		}
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (RandomAccessFile randomAccess = new RandomAccessFile(source, RandomAccessFileMode.READ.getValue())) {
			byte[] buffer = new byte[bufferSize];
			int length;
			while (!Judge.isMinusOne(length = randomAccess.read(buffer))) {
				result.write(buffer, 0, length);
			}
			result.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString(charset);
	}

	/**
	 * FileChannel 读取文件文本
	 *
	 * @return 文本字符串
	 */
	@NotNull
	@Contract(pure = true)
	public String channelText() {
		if (!source.isFile()) {
			return "";
		}
		CharBuffer result = null;
		try (FileChannel channel = new RandomAccessFile(source, RandomAccessFileMode.READ.getValue()).getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate(Math.toIntExact(source.length()));
			channel.read(buffer);
			buffer.flip();
			result = charset.decode(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return String.valueOf(result);
	}

	/**
	 * MappedByteBuffer 内存映射方法读取文件文本
	 *
	 * @return 文本
	 */
	@NotNull
	@Contract(pure = true)
	public String mappedText() {
		if (!source.isFile()) {
			return "";
		}
		CharBuffer result = CharBuffer.allocate(Math.toIntExact(source.length()));
		try (FileChannel channel = new RandomAccessFile(source, RandomAccessFileMode.READ.getValue()).getChannel()) {
			long MAX_COUNT = (int) Math.ceil((double) source.length() / (double) Integer.MAX_VALUE);
			for (long i = 0; i < MAX_COUNT; i++) {
				result.put(charset.decode(channel.map(FileChannel.MapMode.READ_ONLY, i * Integer.MAX_VALUE, i + 1 == MAX_COUNT ? source.length() % Integer.MAX_VALUE : Integer.MAX_VALUE)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return String.valueOf(result.array());
	}

}