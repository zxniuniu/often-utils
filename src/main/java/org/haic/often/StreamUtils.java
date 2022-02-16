package org.haic.often;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java 8 Stream 流工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/13 12:01
 */
public class StreamUtils {

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组
	 * @param second 第二个动态数组
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(List<T> frist, List<T> second) {
		return listDeduplication(frist.parallelStream(), second.parallelStream());
	}

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组
	 * @param second 第二个动态数组 流
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(List<T> frist, Stream<T> second) {
		return listDeduplication(frist.parallelStream(), second);
	}

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组 流
	 * @param second 第二个动态数组
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(Stream<T> frist, List<T> second) {
		return listDeduplication(frist, second.parallelStream());
	}

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组 流
	 * @param second 第二个动态数组 流
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(Stream<T> frist, Stream<T> second) {
		return frist.filter(one -> second.noneMatch(two -> Objects.equals(one, two))).collect(Collectors.toList());
	}

	/**
	 * 流排序，隐式处理
	 *
	 * @param list 动态数组
	 * @param <E>  泛型
	 * @return 无排序的数组
	 */
	@NotNull @Contract(pure = true) public static <E> List<E> sort(@NotNull List<E> list) {
		return list.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * 流排序，隐式处理
	 *
	 * @param list       动态数组
	 * @param <E>        泛型
	 * @param comparator Comparator排序参数
	 * @return 无排序的数组
	 */
	@NotNull @Contract(pure = true) public static <E> List<E> sort(@NotNull List<E> list, @NotNull Comparator<E> comparator) {
		return list.stream().sorted(comparator).collect(Collectors.toList());
	}

	/**
	 * 去重无排序,流排序，隐式处理
	 *
	 * @param list 动态数组
	 * @param <E>  泛型
	 * @return 无排序的数组
	 */
	@NotNull @Contract(pure = true) public static <E> List<E> streamSet(@NotNull List<E> list) {
		return list.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * int类型数组转换Integer类型动态数组
	 *
	 * @param nums int类型静态数组
	 * @return Integer类型动态数组
	 */
	@NotNull @Contract(pure = true) public static List<Integer> intToInteger(final int[] nums) {
		return Arrays.stream(nums).boxed().collect(Collectors.toList());
	}

	/**
	 * 字符串转流
	 *
	 * @param str 字符串
	 * @return 流
	 */
	@NotNull @Contract(pure = true) public static InputStream streamByString(@NotNull String str) {
		return new ByteArrayInputStream(str.getBytes());
	}

	/**
	 * InputStream 流工具
	 *
	 * @param inputStream InputStream
	 * @return new InputStreamUtil
	 */
	@NotNull @Contract(pure = true) public static InputStreamUtil stream(@NotNull InputStream inputStream) {
		return new InputStreamUtil(inputStream);
	}

	/**
	 * InputStreamReader 流工具
	 *
	 * @param inputStream InputStreamReader
	 * @return new InputStreamReaderUtil
	 */
	@NotNull @Contract(pure = true) public static InputStreamReaderUtil stream(@NotNull InputStreamReader inputStream) {
		return new InputStreamReaderUtil(inputStream);
	}

	/**
	 * BufferedInputStream 流工具
	 *
	 * @param inputStream BufferedInputStream
	 * @return new InputStreamReaderUtil
	 */
	@NotNull @Contract(pure = true) public static BufferedInputStreamUtil stream(@NotNull BufferedInputStream inputStream) {
		return new BufferedInputStreamUtil(inputStream);
	}

	/**
	 * 流工具类
	 */
	public abstract static class StreamUtil {
		protected int bufferSize = 8192;
		protected Charset charset = StandardCharsets.UTF_8;

		protected StreamUtil() {
		}

		/**
		 * 设置 缓冲区大小(默认8192)
		 *
		 * @param bufferSize 缓冲区大小
		 * @return this
		 */
		public abstract StreamUtil bufferSize(int bufferSize);

		/**
		 * 设置 字符集编码(默认UTF8)
		 *
		 * @param charsetName 字符集编码名称
		 * @return this
		 */
		@Contract(pure = true) public abstract StreamUtil charset(@NotNull String charsetName);

		/**
		 * 设置 字符集编码(默认UTF8)
		 *
		 * @param charset 字符集编码
		 * @return this
		 */
		@Contract(pure = true) public abstract StreamUtil charset(@NotNull Charset charset);

		/**
		 * 获取 流中字符串信息
		 *
		 * @return 字符串文本
		 */
		@Contract(pure = true) public abstract String getString() throws IOException;

	}

	/**
	 * InputStreamUtil 工具类
	 */
	public static class InputStreamUtil extends StreamUtil {
		protected InputStream inputStream;

		protected InputStreamUtil(@NotNull InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Contract(pure = true) public InputStreamUtil bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		@Contract(pure = true) public InputStreamUtil charset(@NotNull String charsetName) {
			this.charset = Charset.forName(charsetName);
			return this;
		}

		@Contract(pure = true) public InputStreamUtil charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
		}

		@NotNull @Contract(pure = true) public String getString() throws IOException {
			return toByteArrayOutputStream().toString(charset);
		}

		/**
		 * 获取 流中字符串信息
		 *
		 * @return 字符串
		 */
		@NotNull @Contract(pure = true) public List<String> getStringAsLine() throws IOException {
			return stream(new InputStreamReader(inputStream, charset)).bufferSize(bufferSize).getStringAsLine();
		}

		/**
		 * 获取 Stream 中字符信息
		 *
		 * @return bytes
		 */
		@Contract(pure = true) public byte[] toByteArray() throws IOException {
			return stream(new BufferedInputStream(inputStream, bufferSize)).toByteArray();
		}

		/**
		 * 转换为 ByteArrayOutputStream
		 *
		 * @return ByteArrayOutputStream
		 */
		@NotNull @Contract(pure = true) public ByteArrayOutputStream toByteArrayOutputStream() throws IOException {
			return stream(new BufferedInputStream(inputStream)).bufferSize(bufferSize).toByteArrayOutputStream();
		}

	}

	/**
	 * InputStreamReader 工具类
	 */
	public static class InputStreamReaderUtil extends StreamUtil {
		protected InputStreamReader inputStream;

		protected InputStreamReaderUtil(@NotNull InputStreamReader inputStream) {
			this.inputStream = inputStream;
		}

		@Override @Contract(pure = true) public InputStreamReaderUtil bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		@Contract(pure = true) public InputStreamReaderUtil charset(@NotNull String charsetName) {
			this.charset = Charset.forName(charsetName);
			return this;
		}

		@Contract(pure = true) public InputStreamReaderUtil charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
		}

		@NotNull @Contract(pure = true) public String getString() throws IOException {
			StringWriter result = new StringWriter();
			char[] buffer = new char[bufferSize];
			int length;
			while (!Judge.isMinusOne(length = inputStream.read(buffer))) {
				result.write(buffer, 0, length);
			}
			return String.valueOf(result);
		}

		/**
		 * 获取 Stream 中字符串信息
		 *
		 * @return 字符串列表(按行分割)
		 */
		@NotNull @Contract(pure = true) public List<String> getStringAsLine() throws IOException {
			List<String> result = new ArrayList<>();
			BufferedReader bufferedReader = new BufferedReader(inputStream, bufferSize);
			String line;
			while (!Judge.isNull(line = bufferedReader.readLine())) {
				result.add(line);
			}
			return result;
		}

	}

	/**
	 * BufferedInputStreamUtil 工具类
	 */
	public static class BufferedInputStreamUtil extends StreamUtil {
		protected BufferedInputStream inputStream;

		protected BufferedInputStreamUtil(BufferedInputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Contract(pure = true) public BufferedInputStreamUtil bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		@Contract(pure = true) public BufferedInputStreamUtil charset(@NotNull String charsetName) {
			this.charset = Charset.forName(charsetName);
			return this;
		}

		@Contract(pure = true) public BufferedInputStreamUtil charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
		}

		@NotNull @Contract(pure = true) public String getString() throws IOException {
			return toByteArrayOutputStream().toString(charset);
		}

		/**
		 * 转换为 ByteArrayOutputStream
		 *
		 * @return ByteArrayOutputStream
		 */
		@NotNull @Contract(pure = true) public ByteArrayOutputStream toByteArrayOutputStream() throws IOException {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[bufferSize];
			int length;
			while (!Judge.isMinusOne(length = inputStream.read(buffer))) {
				result.write(buffer, 0, length);
			}
			return result;
		}

		/**
		 * 获取 Stream 中字符信息
		 *
		 * @return bytes
		 */
		@Contract(pure = true) public byte[] toByteArray() throws IOException {
			return toByteArrayOutputStream().toByteArray();
		}

	}

}